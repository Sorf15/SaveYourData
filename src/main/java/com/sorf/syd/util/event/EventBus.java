package com.sorf.syd.util.event;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.sorf.syd.util.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventBus implements IEventExceptionHandler {

    private ConcurrentHashMap<Object, ArrayList<IEventListener>> listeners = new ConcurrentHashMap<Object, ArrayList<IEventListener>>();
    private List<Object> listenerOwners = new LinkedList<>();
    private IEventExceptionHandler exceptionHandler;
    private boolean shutdown;

    public EventBus()
    {
        exceptionHandler = this;
    }

    public void register(Object target)
    {
        if (listeners.containsKey(target))
        {
            return;
        }

        listenerOwners.add(target);
        boolean isStatic = target.getClass() == Class.class;
        @SuppressWarnings("unchecked")
        Set<? extends Class<?>> supers = isStatic ? Sets.newHashSet((Class<?>)target) : TypeToken.of(target.getClass()).getTypes().rawTypes();
        for (Method method : (isStatic ? (Class<?>)target : target.getClass()).getMethods())
        {
            if (isStatic && !Modifier.isStatic(method.getModifiers()))
                continue;
            else if (!isStatic && Modifier.isStatic(method.getModifiers()))
                continue;

            for (Class<?> cls : supers)
            {
                try
                {
                    Method real = cls.getDeclaredMethod(method.getName(), method.getParameterTypes());
                    if (real.isAnnotationPresent(EventListener.class))
                    {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        if (parameterTypes.length != 1)
                        {
                            throw new IllegalArgumentException(
                                    "Method " + method + " has @EventListener annotation, but requires " + parameterTypes.length +
                                            " arguments.  Event handler methods must require a single argument."
                            );
                        }

                        Class<?> eventType = parameterTypes[0];

                        if (!Event.class.isAssignableFrom(eventType))
                        {
                            throw new IllegalArgumentException("Method " + method + " has @EventListener annotation, but takes a argument that is not an Event " + eventType);
                        }

                        register(eventType, target, real);
                        break;
                    }
                }
                catch (NoSuchMethodException e)
                {
                    ; // Eat the error, this is not unexpected
                }
            }
        }
    }

    private void register(Class<?> eventType, Object target, Method method)
    {
        try
        {
            Constructor<?> ctr = eventType.getConstructor();
            ctr.setAccessible(true);
            Event event = (Event)ctr.newInstance();
            final ASMEventHandler asm = new ASMEventHandler(target, method, IGenericEvent.class.isAssignableFrom(eventType));

            Logger.debug("Registering listener %s", asm);
            event.getListenerList().register(asm.getPriority(), asm);

            ArrayList<IEventListener> others = listeners.computeIfAbsent(target, k -> new ArrayList<>());
            others.add(asm);
        }
        catch (Exception e)
        {
            Logger.error("Error registering event handler: eventType:%s; method:%s, %s", eventType, method, e);
        }
    }

    public void unregister(Object object)
    {
        ArrayList<IEventListener> list = listeners.remove(object);
        if(list == null)
            return;
        for (IEventListener listener : list)
        {
            ListenerList.unregisterAll(listener);
        }
    }

    public boolean fire(@NotNull Event event)
    {
        if (shutdown) return false;

        IEventListener[] listeners = event.getListenerList().getListeners();
        //Logger.debug("Firing event: " + event + " to listeners: " + Arrays.toString(listeners));
        int index = 0;
        try
        {
            for (; index < listeners.length; index++)
            {
                listeners[index].invoke(event);
            }
        }
        catch (Throwable throwable)
        {
            exceptionHandler.handleException(this, event, listeners, index, throwable);
            throw new RuntimeException(throwable);
        }
        return event.isCancelable() && event.isCanceled();
    }

    public void shutdown()
    {
        Logger.warn("EventBus shutting down - future events will not be posted.");
        shutdown = true;
    }

    @Override
    public void handleException(EventBus bus, Event event, IEventListener[] listeners, int index, Throwable throwable)
    {
        Logger.error("Exception caught during firing event" + event + throwable);
        Logger.error("Index: " + index + " Listeners:" );
        for (int x = 0; x < listeners.length; x++)
        {
            Logger.error(x+ ": " + listeners[x]);
        }
    }
}
