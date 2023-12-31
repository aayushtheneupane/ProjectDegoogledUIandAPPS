package com.android.systemui.p006qs.tiles;

import com.android.systemui.p006qs.QSHost;
import com.android.systemui.statusbar.policy.RotationLockController;
import dagger.internal.Factory;
import javax.inject.Provider;

/* renamed from: com.android.systemui.qs.tiles.RotationLockTile_Factory */
public final class RotationLockTile_Factory implements Factory<RotationLockTile> {
    private final Provider<QSHost> hostProvider;
    private final Provider<RotationLockController> rotationLockControllerProvider;

    public RotationLockTile_Factory(Provider<QSHost> provider, Provider<RotationLockController> provider2) {
        this.hostProvider = provider;
        this.rotationLockControllerProvider = provider2;
    }

    public RotationLockTile get() {
        return provideInstance(this.hostProvider, this.rotationLockControllerProvider);
    }

    public static RotationLockTile provideInstance(Provider<QSHost> provider, Provider<RotationLockController> provider2) {
        return new RotationLockTile(provider.get(), provider2.get());
    }

    public static RotationLockTile_Factory create(Provider<QSHost> provider, Provider<RotationLockController> provider2) {
        return new RotationLockTile_Factory(provider, provider2);
    }
}
