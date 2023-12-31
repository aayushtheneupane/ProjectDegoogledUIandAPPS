package com.android.systemui.p006qs.tiles;

import com.android.systemui.p006qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

/* renamed from: com.android.systemui.qs.tiles.SyncTile_Factory */
public final class SyncTile_Factory implements Factory<SyncTile> {
    private final Provider<QSHost> hostProvider;

    public SyncTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    public SyncTile get() {
        return provideInstance(this.hostProvider);
    }

    public static SyncTile provideInstance(Provider<QSHost> provider) {
        return new SyncTile(provider.get());
    }

    public static SyncTile_Factory create(Provider<QSHost> provider) {
        return new SyncTile_Factory(provider);
    }
}
