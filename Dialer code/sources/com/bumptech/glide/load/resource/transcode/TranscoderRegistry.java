package com.bumptech.glide.load.resource.transcode;

import java.util.ArrayList;
import java.util.List;

public class TranscoderRegistry {
    private final List<Entry<?, ?>> transcoders = new ArrayList();

    private static final class Entry<Z, R> {
        private final Class<Z> fromClass;
        private final Class<R> toClass;
        final ResourceTranscoder<Z, R> transcoder;

        Entry(Class<Z> cls, Class<R> cls2, ResourceTranscoder<Z, R> resourceTranscoder) {
            this.fromClass = cls;
            this.toClass = cls2;
            this.transcoder = resourceTranscoder;
        }

        public boolean handles(Class<?> cls, Class<?> cls2) {
            return this.fromClass.isAssignableFrom(cls) && cls2.isAssignableFrom(this.toClass);
        }
    }

    public synchronized <Z, R> ResourceTranscoder<Z, R> get(Class<Z> cls, Class<R> cls2) {
        if (cls2.isAssignableFrom(cls)) {
            return UnitTranscoder.get();
        }
        for (Entry next : this.transcoders) {
            if (next.handles(cls, cls2)) {
                return next.transcoder;
            }
        }
        String valueOf = String.valueOf(cls);
        String valueOf2 = String.valueOf(cls2);
        StringBuilder sb = new StringBuilder(valueOf.length() + 47 + valueOf2.length());
        sb.append("No transcoder registered to transcode from ");
        sb.append(valueOf);
        sb.append(" to ");
        sb.append(valueOf2);
        throw new IllegalArgumentException(sb.toString());
    }

    public synchronized <Z, R> List<Class<R>> getTranscodeClasses(Class<Z> cls, Class<R> cls2) {
        ArrayList arrayList = new ArrayList();
        if (cls2.isAssignableFrom(cls)) {
            arrayList.add(cls2);
            return arrayList;
        }
        for (Entry<?, ?> handles : this.transcoders) {
            if (handles.handles(cls, cls2)) {
                arrayList.add(cls2);
            }
        }
        return arrayList;
    }

    public synchronized <Z, R> void register(Class<Z> cls, Class<R> cls2, ResourceTranscoder<Z, R> resourceTranscoder) {
        this.transcoders.add(new Entry(cls, cls2, resourceTranscoder));
    }
}
