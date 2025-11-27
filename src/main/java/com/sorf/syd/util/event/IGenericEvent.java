package com.sorf.syd.util.event;

import java.lang.reflect.Type;

public interface IGenericEvent<T> {
    Type getGenericType();
}
