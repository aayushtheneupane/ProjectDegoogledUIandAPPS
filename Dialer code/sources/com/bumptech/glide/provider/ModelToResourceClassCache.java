package com.bumptech.glide.provider;

import android.support.p000v4.util.ArrayMap;
import com.bumptech.glide.util.MultiClassKey;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ModelToResourceClassCache {
    private final ArrayMap<MultiClassKey, List<Class<?>>> registeredResourceClassCache = new ArrayMap<>();
    private final AtomicReference<MultiClassKey> resourceClassKeyRef = new AtomicReference<>();

    public List<Class<?>> get(Class<?> cls, Class<?> cls2) {
        List<Class<?>> list;
        MultiClassKey andSet = this.resourceClassKeyRef.getAndSet((Object) null);
        if (andSet == null) {
            andSet = new MultiClassKey(cls, cls2);
        } else {
            andSet.set(cls, cls2);
        }
        synchronized (this.registeredResourceClassCache) {
            list = this.registeredResourceClassCache.get(andSet);
        }
        this.resourceClassKeyRef.set(andSet);
        return list;
    }

    public void put(Class<?> cls, Class<?> cls2, List<Class<?>> list) {
        synchronized (this.registeredResourceClassCache) {
            this.registeredResourceClassCache.put(new MultiClassKey(cls, cls2), list);
        }
    }
}
