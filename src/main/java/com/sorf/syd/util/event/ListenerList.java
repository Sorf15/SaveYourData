package com.sorf.syd.util.event;

import java.util.*;
import com.google.common.collect.ImmutableList;


public class ListenerList
{
    private static ImmutableList<ListenerList> allLists = ImmutableList.of();
    private static int maxSize = 0;
    private ListenerListInst list = new ListenerListInst();

    public ListenerList()
    {
        extendMasterList(this);
    }

    private synchronized static void extendMasterList(ListenerList inst)
    {
        ImmutableList.Builder<ListenerList> builder = ImmutableList.builder();
        builder.addAll(allLists);
        builder.add(inst);
        allLists = builder.build();
    }

    public static void clearBusID()
    {
        for (ListenerList list : allLists)
        {
            list.list.dispose();
        }
    }

    protected ListenerListInst getInstance()
    {
        return list;
    }

    public IEventListener[] getListeners()
    {
        return list.getListeners();
    }

    public void register(EventPriority priority, IEventListener listener)
    {
        list.register(priority, listener);
    }

    public void unregister(IEventListener listener)
    {
        list.unregister(listener);
    }

    public static void unregisterAll(IEventListener listener)
    {
        for (ListenerList list : allLists)
        {
            list.unregister(listener);
        }
    }

    private class ListenerListInst
    {
        private boolean rebuild = true;
        private IEventListener[] listeners;
        private ArrayList<ArrayList<IEventListener>> priorities;


        private ListenerListInst() {
            int count = EventPriority.values().length;
            priorities = new ArrayList<ArrayList<IEventListener>>(count);

            for (int x = 0; x < count; x++) {
                priorities.add(new ArrayList<IEventListener>());
            }
        }

        public void dispose() {
            for (ArrayList<IEventListener> listeners : priorities) {
                listeners.clear();
            }
            priorities.clear();
            listeners = null;
        }

        /**
         * Returns a ArrayList containing all listeners for this eventy.
         *
         * @param priority The Priority to get
         * @return ArrayList containing listeners
         */
        public ArrayList<IEventListener> getListeners(EventPriority priority) {
            return new ArrayList<IEventListener>(priorities.get(priority.ordinal()));
        }

        /**
         * Returns a full list of all listeners for all priority levels.
         * Including all parent listeners.
         *
         * List is returned in proper priority order.
         *
         * Automatically rebuilds the internal Array cache if its information is out of date.
         *
         * @return Array containing listeners
         */
        public IEventListener[] getListeners() {
            if (shouldRebuild()) {
                buildCache();
            }
            return listeners;
        }

        protected boolean shouldRebuild() {
            return rebuild;// || (parent != null && parent.shouldRebuild());
        }

        protected void forceRebuild() {
            this.rebuild = true;
        }

        /**
         * Rebuild the local Array of listeners, returns early if there is no work to do.
         */
        private void buildCache() {
            ArrayList<IEventListener> ret = new ArrayList<IEventListener>();
            for (EventPriority value : EventPriority.values()) {
                List<IEventListener> listeners = getListeners(value);
                if (listeners.size() > 0) {
                    ret.add(value); //Add the priority to notify the event of it's current phase.
                    ret.addAll(listeners);
                }
            }
            listeners = ret.toArray(new IEventListener[ret.size()]);
            rebuild = false;
        }

        public void register(EventPriority priority, IEventListener listener) {
            priorities.get(priority.ordinal()).add(listener);
            this.forceRebuild();
        }

        public void unregister(IEventListener listener) {
            for(ArrayList<IEventListener> list : priorities) {
                if (list.remove(listener)) {
                    this.forceRebuild();
                }
            }
        }
    }
}