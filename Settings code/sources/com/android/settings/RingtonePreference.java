package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.UserHandle;
import android.telecom.PhoneAccountHandle;
import android.text.TextUtils;
import android.util.AttributeSet;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import com.android.internal.R;

public class RingtonePreference extends Preference {
    private PhoneAccountHandle mPhoneAccountHandle;
    private int mRingtoneType;
    private boolean mShowDefault;
    private boolean mShowSilent;
    protected Context mUserContext;
    protected int mUserId;

    public RingtonePreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.RingtonePreference, 0, 0);
        this.mRingtoneType = obtainStyledAttributes.getInt(0, 1);
        this.mShowDefault = obtainStyledAttributes.getBoolean(1, true);
        this.mShowSilent = obtainStyledAttributes.getBoolean(2, true);
        setIntent(new Intent("android.intent.action.RINGTONE_PICKER"));
        setUserId(UserHandle.myUserId());
        obtainStyledAttributes.recycle();
    }

    public void setUserId(int i) {
        this.mUserId = i;
        this.mUserContext = Utils.createPackageContextAsUser(getContext(), this.mUserId);
    }

    public int getUserId() {
        return this.mUserId;
    }

    public void setPhoneAccountHandle(PhoneAccountHandle phoneAccountHandle) {
        this.mPhoneAccountHandle = phoneAccountHandle;
    }

    public PhoneAccountHandle getPhoneAccountHandle() {
        return this.mPhoneAccountHandle;
    }

    public int getRingtoneType() {
        return this.mRingtoneType;
    }

    public void setRingtoneType(int i) {
        this.mRingtoneType = i;
    }

    public void onPrepareRingtonePickerIntent(Intent intent) {
        intent.putExtra("android.intent.extra.ringtone.EXISTING_URI", onRestoreRingtone());
        intent.putExtra("android.intent.extra.ringtone.SHOW_DEFAULT", this.mShowDefault);
        if (this.mShowDefault) {
            intent.putExtra("android.intent.extra.ringtone.DEFAULT_URI", RingtoneManager.getDefaultUri(getRingtoneType()));
        }
        intent.putExtra("android.intent.extra.ringtone.SHOW_SILENT", this.mShowSilent);
        intent.putExtra("android.intent.extra.ringtone.TYPE", this.mRingtoneType);
        intent.putExtra("android.intent.extra.ringtone.TITLE", getTitle());
        intent.putExtra("android.intent.extra.ringtone.AUDIO_ATTRIBUTES_FLAGS", 64);
    }

    /* access modifiers changed from: protected */
    public void onSaveRingtone(Uri uri) {
        persistString(uri != null ? uri.toString() : "");
    }

    /* access modifiers changed from: protected */
    public Uri onRestoreRingtone() {
        String persistedString = getPersistedString((String) null);
        if (!TextUtils.isEmpty(persistedString)) {
            return Uri.parse(persistedString);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public Object onGetDefaultValue(TypedArray typedArray, int i) {
        return typedArray.getString(i);
    }

    /* access modifiers changed from: protected */
    public void onSetInitialValue(boolean z, Object obj) {
        String str = (String) obj;
        if (!z && !TextUtils.isEmpty(str)) {
            onSaveRingtone(Uri.parse(str));
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
    }

    public boolean onActivityResult(int i, int i2, Intent intent) {
        if (intent == null) {
            return true;
        }
        Uri uri = (Uri) intent.getParcelableExtra("android.intent.extra.ringtone.PICKED_URI");
        if (!callChangeListener(uri != null ? uri.toString() : "")) {
            return true;
        }
        onSaveRingtone(uri);
        return true;
    }
}
