package com.android.systemui.p006qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import com.android.systemui.p006qs.QSHost;
import com.android.systemui.p006qs.tileimpl.QSTileImpl;
import com.android.systemui.plugins.p005qs.DetailAdapter;
import com.android.systemui.plugins.p005qs.QSTile;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;

/* renamed from: com.android.systemui.qs.tiles.UserTile */
public class UserTile extends QSTileImpl<QSTile.State> implements UserInfoController.OnUserInfoChangedListener {
    private Pair<String, Drawable> mLastUpdate;
    private final UserInfoController mUserInfoController;
    private final UserSwitcherController mUserSwitcherController;

    public int getMetricsCategory() {
        return 260;
    }

    public void handleSetListening(boolean z) {
    }

    public UserTile(QSHost qSHost, UserSwitcherController userSwitcherController, UserInfoController userInfoController) {
        super(qSHost);
        this.mUserSwitcherController = userSwitcherController;
        this.mUserInfoController = userInfoController;
        this.mUserInfoController.observe(getLifecycle(), this);
    }

    public QSTile.State newTileState() {
        return new QSTile.State();
    }

    public Intent getLongClickIntent() {
        return new Intent("android.settings.USER_SETTINGS");
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        showDetail(true);
    }

    public DetailAdapter getDetailAdapter() {
        return this.mUserSwitcherController.userDetailAdapter;
    }

    public CharSequence getTileLabel() {
        return getState().label;
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.State state, Object obj) {
        final Pair<String, Drawable> pair = obj != null ? (Pair) obj : this.mLastUpdate;
        if (pair != null) {
            Object obj2 = pair.first;
            state.label = (CharSequence) obj2;
            state.contentDescription = (CharSequence) obj2;
            state.icon = new QSTile.Icon() {
                public Drawable getDrawable(Context context) {
                    return (Drawable) pair.second;
                }
            };
        }
    }

    public void onUserInfoChanged(String str, Drawable drawable, String str2) {
        this.mLastUpdate = new Pair<>(str, drawable);
        refreshState(this.mLastUpdate);
    }
}
