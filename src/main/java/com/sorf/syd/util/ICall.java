package com.sorf.syd.util;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ICall<V> {
    void call(@NotNull V v);
}
