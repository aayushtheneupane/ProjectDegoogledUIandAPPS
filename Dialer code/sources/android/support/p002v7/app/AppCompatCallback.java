package android.support.p002v7.app;

import android.support.p002v7.view.ActionMode;

/* renamed from: android.support.v7.app.AppCompatCallback */
public interface AppCompatCallback {
    void onSupportActionModeFinished(ActionMode actionMode);

    void onSupportActionModeStarted(ActionMode actionMode);

    ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback);
}
