package androidx.slice.compat;

import android.app.PendingIntent;
import android.app.slice.Slice;
import android.app.slice.SliceManager;
import android.app.slice.SliceProvider;
import android.app.slice.SliceSpec;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;
import androidx.remotecallback.CallbackHandlerRegistry;
import androidx.slice.SliceConvert;
import androidx.slice.SliceProviderWithCallbacks;
import java.util.Collection;
import java.util.Set;

public class SliceProviderWrapperContainer$SliceProviderWrapper extends SliceProvider {
    private String[] mAutoGrantPermissions;
    private SliceManager mSliceManager;
    private androidx.slice.SliceProvider mSliceProvider;

    public boolean onCreate() {
        return true;
    }

    public SliceProviderWrapperContainer$SliceProviderWrapper(androidx.slice.SliceProvider sliceProvider, String[] strArr) {
        super(strArr);
        this.mAutoGrantPermissions = (strArr == null || strArr.length == 0) ? null : strArr;
        this.mSliceProvider = sliceProvider;
    }

    public void attachInfo(Context context, ProviderInfo providerInfo) {
        this.mSliceProvider.attachInfo(context, providerInfo);
        super.attachInfo(context, providerInfo);
        this.mSliceManager = (SliceManager) context.getSystemService(SliceManager.class);
    }

    public PendingIntent onCreatePermissionRequest(Uri uri) {
        if (this.mAutoGrantPermissions != null) {
            checkPermissions(uri);
        }
        PendingIntent onCreatePermissionRequest = this.mSliceProvider.onCreatePermissionRequest(uri, getCallingPackage());
        if (onCreatePermissionRequest != null) {
            return onCreatePermissionRequest;
        }
        return super.onCreatePermissionRequest(uri);
    }

    public Bundle call(String str, String str2, Bundle bundle) {
        Uri uri;
        Intent intent;
        if (this.mAutoGrantPermissions != null) {
            if ("bind_slice".equals(str)) {
                if (bundle != null) {
                    uri = (Uri) bundle.getParcelable("slice_uri");
                    if (!(uri == null || this.mSliceManager.checkSlicePermission(uri, Binder.getCallingPid(), Binder.getCallingUid()) == 0)) {
                        checkPermissions(uri);
                    }
                }
            } else if ("map_slice".equals(str) && (intent = (Intent) bundle.getParcelable("slice_intent")) != null) {
                uri = onMapIntentToUri(intent);
                checkPermissions(uri);
            }
            uri = null;
            checkPermissions(uri);
        }
        if (!"androidx.remotecallback.method.PROVIDER_CALLBACK".equals(str) || !(this.mSliceProvider instanceof SliceProviderWithCallbacks)) {
            return super.call(str, str2, bundle);
        }
        CallbackHandlerRegistry.sInstance.invokeCallback(getContext(), (SliceProviderWithCallbacks) this.mSliceProvider, bundle);
        return null;
    }

    private void checkPermissions(Uri uri) {
        if (uri != null) {
            for (String str : this.mAutoGrantPermissions) {
                if (getContext().checkCallingPermission(str) == 0) {
                    this.mSliceManager.grantSlicePermission(str, uri);
                    getContext().getContentResolver().notifyChange(uri, (ContentObserver) null);
                    return;
                }
            }
        }
    }

    public Slice onBindSlice(Uri uri, Set<SliceSpec> set) {
        androidx.slice.SliceProvider.setSpecs(SliceConvert.wrap(set));
        try {
            return SliceConvert.unwrap(this.mSliceProvider.onBindSlice(uri));
        } catch (Exception e) {
            Log.wtf("SliceProviderWrapper", "Slice with URI " + uri.toString() + " is invalid.", e);
            return null;
        } finally {
            androidx.slice.SliceProvider.setSpecs((Set<androidx.slice.SliceSpec>) null);
        }
    }

    public void onSlicePinned(Uri uri) {
        this.mSliceProvider.onSlicePinned(uri);
        this.mSliceProvider.handleSlicePinned(uri);
    }

    public void onSliceUnpinned(Uri uri) {
        this.mSliceProvider.onSliceUnpinned(uri);
        this.mSliceProvider.handleSliceUnpinned(uri);
    }

    public Collection<Uri> onGetSliceDescendants(Uri uri) {
        return this.mSliceProvider.onGetSliceDescendants(uri);
    }

    public Uri onMapIntentToUri(Intent intent) {
        return this.mSliceProvider.onMapIntentToUri(intent);
    }
}
