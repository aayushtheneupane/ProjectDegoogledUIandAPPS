package com.android.systemui.p006qs.tiles;

import com.android.systemui.p006qs.QSHost;
import com.android.systemui.statusbar.policy.NetworkController;
import dagger.internal.Factory;
import javax.inject.Provider;

/* renamed from: com.android.systemui.qs.tiles.DataSaverTile_Factory */
public final class DataSaverTile_Factory implements Factory<DataSaverTile> {
    private final Provider<QSHost> hostProvider;
    private final Provider<NetworkController> networkControllerProvider;

    public DataSaverTile_Factory(Provider<QSHost> provider, Provider<NetworkController> provider2) {
        this.hostProvider = provider;
        this.networkControllerProvider = provider2;
    }

    public DataSaverTile get() {
        return provideInstance(this.hostProvider, this.networkControllerProvider);
    }

    public static DataSaverTile provideInstance(Provider<QSHost> provider, Provider<NetworkController> provider2) {
        return new DataSaverTile(provider.get(), provider2.get());
    }

    public static DataSaverTile_Factory create(Provider<QSHost> provider, Provider<NetworkController> provider2) {
        return new DataSaverTile_Factory(provider, provider2);
    }
}
