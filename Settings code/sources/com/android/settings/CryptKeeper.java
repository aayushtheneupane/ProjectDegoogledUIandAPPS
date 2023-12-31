package com.android.settings;

import android.app.Activity;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.storage.IStorageManager;
import android.provider.Settings;
import android.sysprop.VoldProperties;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.android.settings.widget.ImeAwareEditText;
import com.havoc.config.center.C1715R;
import java.util.ArrayList;
import java.util.List;

public class CryptKeeper extends Activity implements TextView.OnEditorActionListener, View.OnKeyListener, View.OnTouchListener, TextWatcher, View.OnClickListener {
    private static final int[] LOCK_BUTTON_IDS = {C1715R.C1718id.lock_pattern_size_3, C1715R.C1718id.lock_pattern_size_4, C1715R.C1718id.lock_pattern_size_5, C1715R.C1718id.lock_pattern_size_6};
    private AudioManager mAudioManager;
    protected LockPatternView.OnPatternListener mChooseNewLockPatternListener = new LockPatternView.OnPatternListener() {
        public void onPatternCellAdded(List<LockPatternView.Cell> list) {
        }

        public void onPatternStart() {
            CryptKeeper.this.setPatternButtonsEnabled(false);
            CryptKeeper.this.mLockPatternView.removeCallbacks(CryptKeeper.this.mClearPatternRunnable);
        }

        public void onPatternCleared() {
            CryptKeeper.this.setPatternButtonsEnabled(true);
        }

        public void onPatternDetected(List<LockPatternView.Cell> list) {
            CryptKeeper.this.mLockPatternView.setEnabled(false);
            if (list.size() >= 4) {
                byte lockPatternSize = CryptKeeper.this.mLockPatternView.getLockPatternSize();
                new DecryptTask().execute(new String[]{LockPatternUtils.patternToString(list, lockPatternSize)});
                return;
            }
            CryptKeeper cryptKeeper = CryptKeeper.this;
            cryptKeeper.fakeUnlockAttempt(cryptKeeper.mLockPatternView);
        }
    };
    /* access modifiers changed from: private */
    public final Runnable mClearPatternRunnable = new Runnable() {
        public void run() {
            CryptKeeper.this.mLockPatternView.clearPattern();
        }
    };
    /* access modifiers changed from: private */
    public boolean mCooldown = false;
    /* access modifiers changed from: private */
    public boolean mCorrupt;
    /* access modifiers changed from: private */
    public boolean mEncryptionGoneBad;
    /* access modifiers changed from: private */
    public final Runnable mFakeUnlockAttemptRunnable = new Runnable() {
        public void run() {
            CryptKeeper.this.handleBadAttempt(1);
        }
    };
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                CryptKeeper.this.updateProgress();
            } else if (i == 2) {
                CryptKeeper.this.notifyUser();
            }
        }
    };
    private List<Button> mLockPatternButtons = new ArrayList();
    /* access modifiers changed from: private */
    public LockPatternView mLockPatternView;
    private int mNotificationCountdown = 0;
    private ImeAwareEditText mPasswordEntry;
    private int mReleaseWakeLockCountdown = 0;
    private StatusBarManager mStatusBar;
    /* access modifiers changed from: private */
    public int mStatusString = C1715R.string.enter_password;
    /* access modifiers changed from: private */
    public TextView mStatusText;
    /* access modifiers changed from: private */
    public boolean mValidationComplete;
    private boolean mValidationRequested;
    PowerManager.WakeLock mWakeLock;

    public void afterTextChanged(Editable editable) {
    }

    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    public void onBackPressed() {
    }

    private static class NonConfigurationInstanceState {
        final PowerManager.WakeLock wakelock;

        NonConfigurationInstanceState(PowerManager.WakeLock wakeLock) {
            this.wakelock = wakeLock;
        }
    }

    private class DecryptTask extends AsyncTask<String, Void, Integer> {
        private DecryptTask() {
        }

        private void hide(int i) {
            View findViewById = CryptKeeper.this.findViewById(i);
            if (findViewById != null) {
                findViewById.setVisibility(8);
            }
        }

        /* access modifiers changed from: protected */
        public void onPreExecute() {
            super.onPreExecute();
            if (CryptKeeper.this.mLockPatternView != null) {
                CryptKeeper.this.mLockPatternView.removeCallbacks(CryptKeeper.this.mFakeUnlockAttemptRunnable);
            }
            CryptKeeper.this.beginAttempt();
        }

        /* access modifiers changed from: protected */
        public Integer doInBackground(String... strArr) {
            try {
                return Integer.valueOf(CryptKeeper.this.getStorageManager().decryptStorage(strArr[0]));
            } catch (Exception e) {
                Log.e("CryptKeeper", "Error while decrypting...", e);
                return -1;
            }
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Integer num) {
            if (num.intValue() == 0) {
                if (CryptKeeper.this.mLockPatternView != null) {
                    CryptKeeper.this.mLockPatternView.removeCallbacks(CryptKeeper.this.mClearPatternRunnable);
                    CryptKeeper.this.mLockPatternView.postDelayed(CryptKeeper.this.mClearPatternRunnable, 500);
                }
                CryptKeeper.this.mStatusText.setText(C1715R.string.starting_android);
                hide(C1715R.C1718id.passwordEntry);
                hide(C1715R.C1718id.switch_ime_button);
                hide(C1715R.C1718id.lockPattern);
                hide(C1715R.C1718id.owner_info);
                hide(C1715R.C1718id.emergencyCallButton);
                hide(C1715R.C1718id.pattern_sizes);
            } else if (num.intValue() == 30) {
                Intent intent = new Intent("android.intent.action.FACTORY_RESET");
                intent.setPackage("android");
                intent.addFlags(268435456);
                intent.putExtra("android.intent.extra.REASON", "CryptKeeper.MAX_FAILED_ATTEMPTS");
                CryptKeeper.this.sendBroadcast(intent);
            } else if (num.intValue() == -1) {
                CryptKeeper.this.setContentView(C1715R.layout.crypt_keeper_progress);
                CryptKeeper.this.showFactoryReset(true);
            } else {
                CryptKeeper.this.handleBadAttempt(num);
            }
        }
    }

    /* access modifiers changed from: private */
    public void beginAttempt() {
        this.mStatusText.setText(C1715R.string.checking_decryption);
    }

    /* access modifiers changed from: private */
    public void handleBadAttempt(Integer num) {
        LockPatternView lockPatternView = this.mLockPatternView;
        if (lockPatternView != null) {
            lockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
            this.mLockPatternView.removeCallbacks(this.mClearPatternRunnable);
            this.mLockPatternView.postDelayed(this.mClearPatternRunnable, 1500);
        }
        if (num.intValue() % 10 == 0) {
            this.mCooldown = true;
            cooldown();
            return;
        }
        int intValue = 30 - num.intValue();
        int i = 0;
        if (intValue < 10) {
            this.mStatusText.setText(TextUtils.expandTemplate(getText(C1715R.string.crypt_keeper_warn_wipe), new CharSequence[]{Integer.toString(intValue)}));
        } else {
            try {
                i = getStorageManager().getPasswordType();
            } catch (Exception e) {
                Log.e("CryptKeeper", "Error calling mount service " + e);
            }
            if (i == 3) {
                this.mStatusText.setText(C1715R.string.cryptkeeper_wrong_pin);
            } else if (i == 2) {
                this.mStatusText.setText(C1715R.string.cryptkeeper_wrong_pattern);
            } else {
                this.mStatusText.setText(C1715R.string.cryptkeeper_wrong_password);
            }
        }
        LockPatternView lockPatternView2 = this.mLockPatternView;
        if (lockPatternView2 != null) {
            lockPatternView2.setDisplayMode(LockPatternView.DisplayMode.Wrong);
            this.mLockPatternView.setEnabled(true);
            setPatternButtonsEnabled(true);
        }
        ImeAwareEditText imeAwareEditText = this.mPasswordEntry;
        if (imeAwareEditText != null) {
            imeAwareEditText.setEnabled(true);
            this.mPasswordEntry.scheduleShowSoftInput();
            setBackFunctionality(true);
        }
    }

    private class ValidationTask extends AsyncTask<Void, Void, Boolean> {
        int state;

        private ValidationTask() {
        }

        /* access modifiers changed from: protected */
        public Boolean doInBackground(Void... voidArr) {
            IStorageManager access$400 = CryptKeeper.this.getStorageManager();
            try {
                Log.d("CryptKeeper", "Validating encryption state.");
                this.state = access$400.getEncryptionState();
                if (this.state == 1) {
                    Log.w("CryptKeeper", "Unexpectedly in CryptKeeper even though there is no encryption.");
                    return true;
                }
                return Boolean.valueOf(this.state == 0);
            } catch (RemoteException unused) {
                Log.w("CryptKeeper", "Unable to get encryption state properly");
                return true;
            }
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Boolean bool) {
            boolean z = true;
            boolean unused = CryptKeeper.this.mValidationComplete = true;
            if (Boolean.FALSE.equals(bool)) {
                Log.w("CryptKeeper", "Incomplete, or corrupted encryption detected. Prompting user to wipe.");
                boolean unused2 = CryptKeeper.this.mEncryptionGoneBad = true;
                CryptKeeper cryptKeeper = CryptKeeper.this;
                if (this.state != -4) {
                    z = false;
                }
                boolean unused3 = cryptKeeper.mCorrupt = z;
            } else {
                Log.d("CryptKeeper", "Encryption state validated. Proceeding to configure UI");
            }
            CryptKeeper.this.setupUi();
        }
    }

    private boolean isDebugView() {
        return getIntent().hasExtra("com.android.settings.CryptKeeper.DEBUG_FORCE_VIEW");
    }

    private boolean isDebugView(String str) {
        return str.equals(getIntent().getStringExtra("com.android.settings.CryptKeeper.DEBUG_FORCE_VIEW"));
    }

    /* access modifiers changed from: private */
    public void notifyUser() {
        int i = this.mNotificationCountdown;
        if (i > 0) {
            this.mNotificationCountdown = i - 1;
        } else {
            AudioManager audioManager = this.mAudioManager;
            if (audioManager != null) {
                try {
                    audioManager.playSoundEffect(5, 100);
                } catch (Exception e) {
                    Log.w("CryptKeeper", "notifyUser: Exception while playing sound: " + e);
                }
            }
        }
        this.mHandler.removeMessages(2);
        this.mHandler.sendEmptyMessageDelayed(2, 5000);
        if (this.mWakeLock.isHeld()) {
            int i2 = this.mReleaseWakeLockCountdown;
            if (i2 > 0) {
                this.mReleaseWakeLockCountdown = i2 - 1;
            } else {
                this.mWakeLock.release();
            }
        }
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().getDecorView().setSystemUiVisibility(3334);
        String str = (String) VoldProperties.decrypt().orElse("");
        if (isDebugView() || (!"".equals(str) && !"trigger_restart_framework".equals(str))) {
            try {
                if (getResources().getBoolean(C1715R.bool.crypt_keeper_allow_rotation)) {
                    setRequestedOrientation(-1);
                }
            } catch (Resources.NotFoundException unused) {
            }
            this.mStatusBar = (StatusBarManager) getSystemService("statusbar");
            this.mStatusBar.disable(52887552);
            if (bundle != null) {
                this.mCooldown = bundle.getBoolean("cooldown");
            }
            setAirplaneModeIfNecessary();
            this.mAudioManager = (AudioManager) getSystemService("audio");
            Object lastNonConfigurationInstance = getLastNonConfigurationInstance();
            if (lastNonConfigurationInstance instanceof NonConfigurationInstanceState) {
                this.mWakeLock = ((NonConfigurationInstanceState) lastNonConfigurationInstance).wakelock;
                Log.d("CryptKeeper", "Restoring wakelock from NonConfigurationInstanceState");
                return;
            }
            return;
        }
        disableCryptKeeperComponent(this);
        finish();
    }

    public void onSaveInstanceState(Bundle bundle) {
        bundle.putBoolean("cooldown", this.mCooldown);
    }

    public void onStart() {
        super.onStart();
        setupUi();
    }

    /* access modifiers changed from: private */
    public void setupUi() {
        if (this.mEncryptionGoneBad || isDebugView("error")) {
            setContentView(C1715R.layout.crypt_keeper_progress);
            showFactoryReset(this.mCorrupt);
        } else if (!"".equals((String) VoldProperties.encrypt_progress().orElse("")) || isDebugView("progress")) {
            setContentView(C1715R.layout.crypt_keeper_progress);
            encryptionProgressInit();
        } else if (this.mValidationComplete || isDebugView("password")) {
            new AsyncTask<Void, Void, Void>() {
                String owner_info;
                int passwordType = 0;
                boolean password_visible;
                boolean pattern_visible;

                public Void doInBackground(Void... voidArr) {
                    try {
                        IStorageManager access$400 = CryptKeeper.this.getStorageManager();
                        this.passwordType = access$400.getPasswordType();
                        this.owner_info = access$400.getField("OwnerInfo");
                        boolean z = true;
                        this.pattern_visible = !"0".equals(access$400.getField("PatternVisible"));
                        if ("0".equals(access$400.getField("PasswordVisible"))) {
                            z = false;
                        }
                        this.password_visible = z;
                        return null;
                    } catch (Exception e) {
                        Log.e("CryptKeeper", "Error calling mount service " + e);
                        return null;
                    }
                }

                public void onPostExecute(Void voidR) {
                    Settings.System.putInt(CryptKeeper.this.getContentResolver(), "show_password", this.password_visible ? 1 : 0);
                    int i = this.passwordType;
                    if (i == 3) {
                        CryptKeeper.this.setContentView(C1715R.layout.crypt_keeper_pin_entry);
                        int unused = CryptKeeper.this.mStatusString = C1715R.string.enter_pin;
                    } else if (i == 2) {
                        CryptKeeper.this.setContentView(C1715R.layout.crypt_keeper_pattern_entry);
                        CryptKeeper.this.setBackFunctionality(false);
                        int unused2 = CryptKeeper.this.mStatusString = C1715R.string.enter_pattern;
                    } else {
                        CryptKeeper.this.setContentView(C1715R.layout.crypt_keeper_password_entry);
                        int unused3 = CryptKeeper.this.mStatusString = C1715R.string.enter_password;
                    }
                    CryptKeeper.this.mStatusText.setText(CryptKeeper.this.mStatusString);
                    TextView textView = (TextView) CryptKeeper.this.findViewById(C1715R.C1718id.owner_info);
                    textView.setText(this.owner_info);
                    textView.setSelected(true);
                    CryptKeeper.this.passwordEntryInit();
                    CryptKeeper.this.findViewById(16908290).setSystemUiVisibility(4194304);
                    if (CryptKeeper.this.mLockPatternView != null) {
                        CryptKeeper.this.mLockPatternView.setInStealthMode(true ^ this.pattern_visible);
                    }
                    if (CryptKeeper.this.mCooldown) {
                        CryptKeeper.this.setBackFunctionality(false);
                        CryptKeeper.this.cooldown();
                    }
                }
            }.execute(new Void[0]);
        } else if (!this.mValidationRequested) {
            new ValidationTask().execute((Object[]) null);
            this.mValidationRequested = true;
        }
    }

    public void onStop() {
        super.onStop();
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
    }

    public Object onRetainNonConfigurationInstance() {
        NonConfigurationInstanceState nonConfigurationInstanceState = new NonConfigurationInstanceState(this.mWakeLock);
        Log.d("CryptKeeper", "Handing wakelock off to NonConfigurationInstanceState");
        this.mWakeLock = null;
        return nonConfigurationInstanceState;
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mWakeLock != null) {
            Log.d("CryptKeeper", "Releasing and destroying wakelock");
            this.mWakeLock.release();
            this.mWakeLock = null;
        }
    }

    public void setContentView(int i) {
        super.setContentView(i);
        this.mStatusText = (TextView) findViewById(C1715R.C1718id.status);
    }

    private void encryptionProgressInit() {
        Log.d("CryptKeeper", "Encryption progress screen initializing.");
        if (this.mWakeLock == null) {
            Log.d("CryptKeeper", "Acquiring wakelock.");
            this.mWakeLock = ((PowerManager) getSystemService("power")).newWakeLock(26, "CryptKeeper");
            this.mWakeLock.acquire();
        }
        ((ProgressBar) findViewById(C1715R.C1718id.progress_bar)).setIndeterminate(true);
        setBackFunctionality(false);
        updateProgress();
    }

    /* access modifiers changed from: private */
    public void showFactoryReset(final boolean z) {
        findViewById(C1715R.C1718id.encroid).setVisibility(8);
        Button button = (Button) findViewById(C1715R.C1718id.factory_reset);
        button.setVisibility(0);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent("android.intent.action.FACTORY_RESET");
                intent.setPackage("android");
                intent.addFlags(268435456);
                intent.putExtra("android.intent.extra.REASON", "CryptKeeper.showFactoryReset() corrupt=" + z);
                CryptKeeper.this.sendBroadcast(intent);
            }
        });
        if (z) {
            ((TextView) findViewById(C1715R.C1718id.title)).setText(C1715R.string.crypt_keeper_data_corrupt_title);
            ((TextView) findViewById(C1715R.C1718id.status)).setText(C1715R.string.crypt_keeper_data_corrupt_summary);
        } else {
            ((TextView) findViewById(C1715R.C1718id.title)).setText(C1715R.string.crypt_keeper_failed_title);
            ((TextView) findViewById(C1715R.C1718id.status)).setText(C1715R.string.crypt_keeper_failed_summary);
        }
        View findViewById = findViewById(C1715R.C1718id.bottom_divider);
        if (findViewById != null) {
            findViewById.setVisibility(0);
        }
    }

    /* access modifiers changed from: private */
    public void updateProgress() {
        int i;
        String str = (String) VoldProperties.encrypt_progress().orElse("");
        if ("error_partially_encrypted".equals(str)) {
            showFactoryReset(false);
            return;
        }
        CharSequence text = getText(C1715R.string.crypt_keeper_setup_description);
        try {
            i = isDebugView() ? 50 : Integer.parseInt(str);
        } catch (Exception e) {
            Log.w("CryptKeeper", "Error parsing progress: " + e.toString());
            i = 0;
        }
        String num = Integer.toString(i);
        Log.v("CryptKeeper", "Encryption progress: " + num);
        try {
            int intValue = ((Integer) VoldProperties.encrypt_time_remaining().get()).intValue();
            if (intValue >= 0) {
                num = DateUtils.formatElapsedTime((long) (((intValue + 9) / 10) * 10));
                text = getText(C1715R.string.crypt_keeper_setup_time_remaining);
            }
        } catch (Exception unused) {
        }
        TextView textView = this.mStatusText;
        if (textView != null) {
            textView.setText(TextUtils.expandTemplate(text, new CharSequence[]{num}));
        }
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessageDelayed(1, 1000);
    }

    /* access modifiers changed from: private */
    public void cooldown() {
        ImeAwareEditText imeAwareEditText = this.mPasswordEntry;
        if (imeAwareEditText != null) {
            imeAwareEditText.setEnabled(false);
        }
        LockPatternView lockPatternView = this.mLockPatternView;
        if (lockPatternView != null) {
            lockPatternView.setEnabled(false);
            setPatternButtonsEnabled(false);
        }
        this.mStatusText.setText(C1715R.string.crypt_keeper_force_power_cycle);
    }

    /* access modifiers changed from: private */
    public final void setBackFunctionality(boolean z) {
        if (z) {
            this.mStatusBar.disable(52887552);
        } else {
            this.mStatusBar.disable(57081856);
        }
    }

    /* access modifiers changed from: private */
    public void fakeUnlockAttempt(View view) {
        beginAttempt();
        view.postDelayed(this.mFakeUnlockAttemptRunnable, 1000);
    }

    /* access modifiers changed from: private */
    public void passwordEntryInit() {
        View findViewById;
        this.mPasswordEntry = (ImeAwareEditText) findViewById(C1715R.C1718id.passwordEntry);
        ImeAwareEditText imeAwareEditText = this.mPasswordEntry;
        if (imeAwareEditText != null) {
            imeAwareEditText.setOnEditorActionListener(this);
            this.mPasswordEntry.requestFocus();
            this.mPasswordEntry.setOnKeyListener(this);
            this.mPasswordEntry.setOnTouchListener(this);
            this.mPasswordEntry.addTextChangedListener(this);
        }
        this.mLockPatternButtons.clear();
        this.mLockPatternView = findViewById(C1715R.C1718id.lockPattern);
        LockPatternView lockPatternView = this.mLockPatternView;
        if (lockPatternView != null) {
            lockPatternView.setOnPatternListener(this.mChooseNewLockPatternListener);
            for (int findViewById2 : LOCK_BUTTON_IDS) {
                Button button = (Button) findViewById(findViewById2);
                if (button != null) {
                    button.setOnClickListener(this);
                    this.mLockPatternButtons.add(button);
                }
            }
        }
        if (!getTelephonyManager().isVoiceCapable() && (findViewById = findViewById(C1715R.C1718id.emergencyCallButton)) != null) {
            Log.d("CryptKeeper", "Removing the emergency Call button");
            findViewById.setVisibility(8);
        }
        View findViewById3 = findViewById(C1715R.C1718id.switch_ime_button);
        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService("input_method");
        if (findViewById3 != null && hasMultipleEnabledIMEsOrSubtypes(inputMethodManager, false)) {
            findViewById3.setVisibility(0);
            findViewById3.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    inputMethodManager.showInputMethodPickerFromSystem(false, view.getDisplay().getDisplayId());
                }
            });
        }
        if (this.mWakeLock == null) {
            Log.d("CryptKeeper", "Acquiring wakelock.");
            PowerManager powerManager = (PowerManager) getSystemService("power");
            if (powerManager != null) {
                this.mWakeLock = powerManager.newWakeLock(26, "CryptKeeper");
                this.mWakeLock.acquire();
                this.mReleaseWakeLockCountdown = 96;
            }
        }
        if (this.mLockPatternView == null && !this.mCooldown) {
            getWindow().setSoftInputMode(5);
            ImeAwareEditText imeAwareEditText2 = this.mPasswordEntry;
            if (imeAwareEditText2 != null) {
                imeAwareEditText2.scheduleShowSoftInput();
            }
        }
        updateEmergencyCallButtonState();
        this.mHandler.removeMessages(2);
        this.mHandler.sendEmptyMessageDelayed(2, 120000);
        getWindow().addFlags(4718592);
    }

    private boolean hasMultipleEnabledIMEsOrSubtypes(InputMethodManager inputMethodManager, boolean z) {
        int i = 0;
        for (InputMethodInfo next : inputMethodManager.getEnabledInputMethodList()) {
            if (i > 1) {
                return true;
            }
            List<InputMethodSubtype> enabledInputMethodSubtypeList = inputMethodManager.getEnabledInputMethodSubtypeList(next, true);
            if (!enabledInputMethodSubtypeList.isEmpty()) {
                int i2 = 0;
                for (InputMethodSubtype isAuxiliary : enabledInputMethodSubtypeList) {
                    if (isAuxiliary.isAuxiliary()) {
                        i2++;
                    }
                }
                if (enabledInputMethodSubtypeList.size() - i2 <= 0) {
                    if (z) {
                        if (i2 <= 1) {
                        }
                    }
                }
            }
            i++;
        }
        if (i > 1 || inputMethodManager.getEnabledInputMethodSubtypeList((InputMethodInfo) null, false).size() > 1) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public IStorageManager getStorageManager() {
        IBinder service = ServiceManager.getService("mount");
        if (service != null) {
            return IStorageManager.Stub.asInterface(service);
        }
        return null;
    }

    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i != 0 && i != 6) {
            return false;
        }
        String charSequence = textView.getText().toString();
        if (TextUtils.isEmpty(charSequence)) {
            return true;
        }
        textView.setText((CharSequence) null);
        this.mPasswordEntry.setEnabled(false);
        setBackFunctionality(false);
        if (charSequence.length() >= 4) {
            new DecryptTask().execute(new String[]{charSequence});
        } else {
            fakeUnlockAttempt(this.mPasswordEntry);
        }
        return true;
    }

    private final void setAirplaneModeIfNecessary() {
        if (!(getTelephonyManager().getLteOnCdmaMode() == 1)) {
            Log.d("CryptKeeper", "Going into airplane mode.");
            Settings.Global.putInt(getContentResolver(), "airplane_mode_on", 1);
            Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
            intent.putExtra("state", true);
            sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    private void updateEmergencyCallButtonState() {
        Button button = (Button) findViewById(C1715R.C1718id.emergencyCallButton);
        if (button != null) {
            if (isEmergencyCallCapable()) {
                button.setVisibility(0);
                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        CryptKeeper.this.takeEmergencyCallAction();
                    }
                });
                button.setText(getTelecomManager().isInCall() ? C1715R.string.cryptkeeper_return_to_call : C1715R.string.cryptkeeper_emergency_call);
                return;
            }
            button.setVisibility(8);
        }
    }

    private boolean isEmergencyCallCapable() {
        return getResources().getBoolean(17891607);
    }

    /* access modifiers changed from: private */
    public void takeEmergencyCallAction() {
        TelecomManager telecomManager = getTelecomManager();
        if (telecomManager.isInCall()) {
            telecomManager.showInCallScreen(false);
        } else {
            launchEmergencyDialer();
        }
    }

    private void launchEmergencyDialer() {
        Intent intent = new Intent("com.android.phone.EmergencyDialer.DIAL");
        intent.setFlags(276824064);
        setBackFunctionality(true);
        startActivity(intent);
    }

    private TelephonyManager getTelephonyManager() {
        return (TelephonyManager) getSystemService("phone");
    }

    private TelecomManager getTelecomManager() {
        return (TelecomManager) getSystemService("telecom");
    }

    private void delayAudioNotification() {
        this.mNotificationCountdown = 20;
    }

    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        delayAudioNotification();
        return false;
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        delayAudioNotification();
        return false;
    }

    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        delayAudioNotification();
    }

    public void onClick(View view) {
        byte b;
        LockPatternView lockPatternView = this.mLockPatternView;
        if (lockPatternView != null && lockPatternView.isEnabled()) {
            switch (view.getId()) {
                case C1715R.C1718id.lock_pattern_size_4 /*2131362517*/:
                    b = 4;
                    break;
                case C1715R.C1718id.lock_pattern_size_5 /*2131362518*/:
                    b = 5;
                    break;
                case C1715R.C1718id.lock_pattern_size_6 /*2131362519*/:
                    b = 6;
                    break;
                default:
                    b = 3;
                    break;
            }
            setContentView(C1715R.layout.crypt_keeper_pattern_entry);
            passwordEntryInit();
            TextView textView = this.mStatusText;
            this.mStatusString = C1715R.string.enter_pattern;
            textView.setText(C1715R.string.enter_pattern);
            this.mLockPatternView.setLockPatternSize(b);
            this.mLockPatternView.postInvalidate();
        }
    }

    /* access modifiers changed from: private */
    public void setPatternButtonsEnabled(boolean z) {
        for (Button enabled : this.mLockPatternButtons) {
            enabled.setEnabled(z);
        }
    }

    private static void disableCryptKeeperComponent(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, CryptKeeper.class);
        Log.d("CryptKeeper", "Disabling component " + componentName);
        packageManager.setComponentEnabledSetting(componentName, 2, 1);
    }
}
