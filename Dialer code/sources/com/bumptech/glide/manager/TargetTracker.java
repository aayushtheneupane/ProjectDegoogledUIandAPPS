package com.bumptech.glide.manager;

import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.util.Util;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

public final class TargetTracker implements LifecycleListener {
    private final Set<Target<?>> targets = Collections.newSetFromMap(new WeakHashMap());

    public void clear() {
        this.targets.clear();
    }

    public List<Target<?>> getAll() {
        return Util.getSnapshot(this.targets);
    }

    public void onDestroy() {
        for (T onDestroy : Util.getSnapshot(this.targets)) {
            onDestroy.onDestroy();
        }
    }

    public void onStart() {
        for (T onStart : Util.getSnapshot(this.targets)) {
            onStart.onStart();
        }
    }

    public void onStop() {
        for (T onStop : Util.getSnapshot(this.targets)) {
            onStop.onStop();
        }
    }

    public void track(Target<?> target) {
        this.targets.add(target);
    }

    public void untrack(Target<?> target) {
        this.targets.remove(target);
    }
}
