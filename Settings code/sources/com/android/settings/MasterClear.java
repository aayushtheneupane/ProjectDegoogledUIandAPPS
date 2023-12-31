package com.android.settings;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.sysprop.VoldProperties;
import android.telephony.euicc.EuiccManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.enterprise.ActionDisabledByAdminDialogHelper;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtilsInternal;
import com.google.android.setupcompat.template.FooterBarMixin;
import com.google.android.setupcompat.template.FooterButton;
import com.google.android.setupdesign.GlifLayout;
import com.havoc.config.center.C1715R;

public class MasterClear extends InstrumentedFragment implements ViewTreeObserver.OnGlobalLayoutListener {
    static final int CREDENTIAL_CONFIRM_REQUEST = 56;
    static final int KEYGUARD_REQUEST = 55;
    private View mContentView;
    CheckBox mEsimStorage;
    View mEsimStorageContainer;
    CheckBox mExternalStorage;
    private View mExternalStorageContainer;
    FooterButton mInitiateButton;
    protected final View.OnClickListener mInitiateListener = new View.OnClickListener() {
        public void onClick(View view) {
            Context context = view.getContext();
            if (Utils.isDemoUser(context)) {
                ComponentName deviceOwnerComponent = Utils.getDeviceOwnerComponent(context);
                if (deviceOwnerComponent != null) {
                    context.startActivity(new Intent().setPackage(deviceOwnerComponent.getPackageName()).setAction("android.intent.action.FACTORY_RESET"));
                }
            } else if (!MasterClear.this.runKeyguardConfirmation(55)) {
                Intent accountConfirmationIntent = MasterClear.this.getAccountConfirmationIntent();
                if (accountConfirmationIntent != null) {
                    MasterClear.this.showAccountCredentialConfirmation(accountConfirmationIntent);
                } else {
                    MasterClear.this.showFinalConfirmation();
                }
            }
        }
    };
    ScrollView mScrollView;

    public int getMetricsCategory() {
        return 66;
    }

    /* access modifiers changed from: package-private */
    public boolean isValidRequestCode(int i) {
        return i == 55 || i == 56;
    }

    public void onGlobalLayout() {
        this.mInitiateButton.setEnabled(hasReachedBottom(this.mScrollView));
    }

    private void setUpActionBarAndTitle() {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            Log.e("MasterClear", "No activity attached, skipping setUpActionBarAndTitle");
            return;
        }
        ActionBar actionBar = activity.getActionBar();
        if (actionBar == null) {
            Log.e("MasterClear", "No actionbar, skipping setUpActionBarAndTitle");
            return;
        }
        actionBar.hide();
        activity.getWindow().setStatusBarColor(0);
    }

    /* access modifiers changed from: private */
    public boolean runKeyguardConfirmation(int i) {
        return new ChooseLockSettingsHelper(getActivity(), this).launchConfirmationActivity(i, getActivity().getResources().getText(C1715R.string.master_clear_short_title));
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        onActivityResultInternal(i, i2, intent);
    }

    /* access modifiers changed from: package-private */
    public void onActivityResultInternal(int i, int i2, Intent intent) {
        Intent accountConfirmationIntent;
        if (isValidRequestCode(i)) {
            if (i2 != -1) {
                establishInitialState();
            } else if (56 == i || (accountConfirmationIntent = getAccountConfirmationIntent()) == null) {
                showFinalConfirmation();
            } else {
                showAccountCredentialConfirmation(accountConfirmationIntent);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void showFinalConfirmation() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("erase_sd", this.mExternalStorage.isChecked());
        bundle.putBoolean("erase_esim", this.mEsimStorage.isChecked());
        new SubSettingLauncher(getContext()).setDestination(MasterClearConfirm.class.getName()).setArguments(bundle).setTitleRes(C1715R.string.master_clear_confirm_title).setSourceMetricsCategory(getMetricsCategory()).launch();
    }

    /* access modifiers changed from: package-private */
    public void showAccountCredentialConfirmation(Intent intent) {
        startActivityForResult(intent, 56);
    }

    /* access modifiers changed from: package-private */
    public Intent getAccountConfirmationIntent() {
        ActivityInfo activityInfo;
        FragmentActivity activity = getActivity();
        String string = activity.getString(C1715R.string.account_type);
        String string2 = activity.getString(C1715R.string.account_confirmation_package);
        String string3 = activity.getString(C1715R.string.account_confirmation_class);
        if (TextUtils.isEmpty(string) || TextUtils.isEmpty(string2) || TextUtils.isEmpty(string3)) {
            Log.i("MasterClear", "Resources not set for account confirmation.");
            return null;
        }
        Account[] accountsByType = AccountManager.get(activity).getAccountsByType(string);
        if (accountsByType == null || accountsByType.length <= 0) {
            Log.d("MasterClear", "No " + string + " accounts installed!");
        } else {
            Intent component = new Intent().setPackage(string2).setComponent(new ComponentName(string2, string3));
            ResolveInfo resolveActivity = activity.getPackageManager().resolveActivity(component, 0);
            if (resolveActivity != null && (activityInfo = resolveActivity.activityInfo) != null && string2.equals(activityInfo.packageName)) {
                return component;
            }
            Log.i("MasterClear", "Unable to resolve Activity: " + string2 + "/" + string3);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void establishInitialState() {
        setUpActionBarAndTitle();
        setUpInitiateButton();
        this.mExternalStorageContainer = this.mContentView.findViewById(C1715R.C1718id.erase_external_container);
        this.mExternalStorage = (CheckBox) this.mContentView.findViewById(C1715R.C1718id.erase_external);
        this.mEsimStorageContainer = this.mContentView.findViewById(C1715R.C1718id.erase_esim_container);
        this.mEsimStorage = (CheckBox) this.mContentView.findViewById(C1715R.C1718id.erase_esim);
        ScrollView scrollView = this.mScrollView;
        if (scrollView != null) {
            scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
        this.mScrollView = (ScrollView) this.mContentView.findViewById(C1715R.C1718id.master_clear_scrollview);
        boolean isExternalStorageEmulated = Environment.isExternalStorageEmulated();
        if (isExternalStorageEmulated || (!Environment.isExternalStorageRemovable() && isExtStorageEncrypted())) {
            this.mExternalStorageContainer.setVisibility(8);
            this.mContentView.findViewById(C1715R.C1718id.erase_external_option_text).setVisibility(8);
            this.mContentView.findViewById(C1715R.C1718id.also_erases_external).setVisibility(0);
            this.mExternalStorage.setChecked(!isExternalStorageEmulated);
        } else {
            this.mExternalStorageContainer.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    MasterClear.this.mExternalStorage.toggle();
                }
            });
        }
        if (showWipeEuicc()) {
            if (showWipeEuiccCheckbox()) {
                this.mEsimStorageContainer.setVisibility(0);
                this.mEsimStorageContainer.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        MasterClear.this.mEsimStorage.toggle();
                    }
                });
            } else {
                this.mContentView.findViewById(C1715R.C1718id.also_erases_esim).setVisibility(0);
                this.mContentView.findViewById(C1715R.C1718id.no_cancel_mobile_plan).setVisibility(0);
                this.mEsimStorage.setChecked(true);
            }
        }
        loadAccountList((UserManager) getActivity().getSystemService("user"));
        StringBuffer stringBuffer = new StringBuffer();
        View findViewById = this.mContentView.findViewById(C1715R.C1718id.master_clear_container);
        getContentDescription(findViewById, stringBuffer);
        findViewById.setContentDescription(stringBuffer);
        this.mScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            public void onScrollChange(View view, int i, int i2, int i3, int i4) {
                if ((view instanceof ScrollView) && MasterClear.this.hasReachedBottom((ScrollView) view)) {
                    MasterClear.this.mInitiateButton.setEnabled(true);
                    MasterClear.this.mScrollView.setOnScrollChangeListener((View.OnScrollChangeListener) null);
                }
            }
        });
        this.mScrollView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    /* access modifiers changed from: package-private */
    public boolean showWipeEuicc() {
        Context context = getContext();
        if (!isEuiccEnabled(context)) {
            return false;
        }
        ContentResolver contentResolver = context.getContentResolver();
        if (Settings.Global.getInt(contentResolver, "euicc_provisioned", 0) == 0 && Settings.Global.getInt(contentResolver, "development_settings_enabled", 0) == 0) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean showWipeEuiccCheckbox() {
        return SystemProperties.getBoolean("masterclear.allow_retain_esim_profiles_after_fdr", false);
    }

    /* access modifiers changed from: protected */
    public boolean isEuiccEnabled(Context context) {
        return ((EuiccManager) context.getSystemService("euicc")).isEnabled();
    }

    /* access modifiers changed from: package-private */
    public boolean hasReachedBottom(ScrollView scrollView) {
        if (scrollView.getChildCount() < 1) {
            return true;
        }
        if (scrollView.getChildAt(0).getBottom() - (scrollView.getHeight() + scrollView.getScrollY()) <= 0) {
            return true;
        }
        return false;
    }

    private void setUpInitiateButton() {
        if (this.mInitiateButton == null) {
            FooterBarMixin footerBarMixin = (FooterBarMixin) ((GlifLayout) this.mContentView.findViewById(C1715R.C1718id.setup_wizard_layout)).getMixin(FooterBarMixin.class);
            FooterButton.Builder builder = new FooterButton.Builder(getActivity());
            builder.setText(C1715R.string.master_clear_button_text);
            builder.setListener(this.mInitiateListener);
            builder.setButtonType(0);
            builder.setTheme(2131952050);
            footerBarMixin.setPrimaryButton(builder.build());
            this.mInitiateButton = footerBarMixin.getPrimaryButton();
        }
    }

    private void getContentDescription(View view, StringBuffer stringBuffer) {
        if (view.getVisibility() == 0) {
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    getContentDescription(viewGroup.getChildAt(i), stringBuffer);
                }
            } else if (view instanceof TextView) {
                stringBuffer.append(((TextView) view).getText());
                stringBuffer.append(",");
            }
        }
    }

    private boolean isExtStorageEncrypted() {
        return !"".equals((String) VoldProperties.decrypt().orElse(""));
    }

    /* JADX WARNING: Removed duplicated region for block: B:50:0x0140  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadAccountList(android.os.UserManager r24) {
        /*
            r23 = this;
            r1 = r23
            android.view.View r0 = r1.mContentView
            r2 = 2131361839(0x7f0a002f, float:1.8343442E38)
            android.view.View r2 = r0.findViewById(r2)
            android.view.View r0 = r1.mContentView
            r3 = 2131361838(0x7f0a002e, float:1.834344E38)
            android.view.View r0 = r0.findViewById(r3)
            r3 = r0
            android.widget.LinearLayout r3 = (android.widget.LinearLayout) r3
            r3.removeAllViews()
            androidx.fragment.app.FragmentActivity r4 = r23.getActivity()
            int r0 = android.os.UserHandle.myUserId()
            r5 = r24
            java.util.List r6 = r5.getProfiles(r0)
            int r7 = r6.size()
            android.accounts.AccountManager r8 = android.accounts.AccountManager.get(r4)
            java.lang.String r0 = "layout_inflater"
            java.lang.Object r0 = r4.getSystemService(r0)
            r9 = r0
            android.view.LayoutInflater r9 = (android.view.LayoutInflater) r9
            r0 = 0
            r11 = 0
        L_0x003b:
            if (r11 >= r7) goto L_0x0189
            java.lang.Object r13 = r6.get(r11)
            android.content.pm.UserInfo r13 = (android.content.pm.UserInfo) r13
            int r14 = r13.id
            android.os.UserHandle r15 = new android.os.UserHandle
            r15.<init>(r14)
            android.accounts.Account[] r10 = r8.getAccountsAsUser(r14)
            int r12 = r10.length
            if (r12 != 0) goto L_0x0057
            r18 = r6
            r20 = r8
            goto L_0x017f
        L_0x0057:
            int r17 = r0 + r12
            android.accounts.AccountManager r0 = android.accounts.AccountManager.get(r4)
            android.accounts.AuthenticatorDescription[] r14 = r0.getAuthenticatorTypesAsUser(r14)
            int r5 = r14.length
            r18 = r6
            r6 = 16908310(0x1020016, float:2.387729E-38)
            r0 = 1
            if (r7 <= r0) goto L_0x0089
            android.view.View r0 = com.android.settings.Utils.inflateCategoryHeader(r9, r3)
            android.view.View r16 = r0.findViewById(r6)
            r6 = r16
            android.widget.TextView r6 = (android.widget.TextView) r6
            boolean r13 = r13.isManagedProfile()
            if (r13 == 0) goto L_0x0080
            r13 = 2131887176(0x7f120448, float:1.9408952E38)
            goto L_0x0083
        L_0x0080:
            r13 = 2131887175(0x7f120447, float:1.940895E38)
        L_0x0083:
            r6.setText(r13)
            r3.addView(r0)
        L_0x0089:
            r6 = 0
        L_0x008a:
            if (r6 >= r12) goto L_0x017b
            r13 = r10[r6]
            r0 = 0
        L_0x008f:
            r16 = 0
            r19 = r5
            if (r0 >= r5) goto L_0x00ae
            java.lang.String r5 = r13.type
            r20 = r8
            r8 = r14[r0]
            java.lang.String r8 = r8.type
            boolean r5 = r5.equals(r8)
            if (r5 == 0) goto L_0x00a7
            r0 = r14[r0]
            r5 = r0
            goto L_0x00b2
        L_0x00a7:
            int r0 = r0 + 1
            r5 = r19
            r8 = r20
            goto L_0x008f
        L_0x00ae:
            r20 = r8
            r5 = r16
        L_0x00b2:
            java.lang.String r8 = "MasterClear"
            if (r5 != 0) goto L_0x00df
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r5 = "No descriptor for account name="
            r0.append(r5)
            java.lang.String r5 = r13.name
            r0.append(r5)
            java.lang.String r5 = " type="
            r0.append(r5)
            java.lang.String r5 = r13.type
            r0.append(r5)
            java.lang.String r0 = r0.toString()
            android.util.Log.w(r8, r0)
            r21 = r10
            r22 = r12
            r8 = 16908310(0x1020016, float:2.387729E-38)
            goto L_0x016f
        L_0x00df:
            int r0 = r5.iconId     // Catch:{ NameNotFoundException -> 0x0124, NotFoundException -> 0x0108 }
            if (r0 == 0) goto L_0x0103
            java.lang.String r0 = r5.packageName     // Catch:{ NameNotFoundException -> 0x0124, NotFoundException -> 0x0108 }
            r21 = r10
            r10 = 0
            android.content.Context r0 = r4.createPackageContextAsUser(r0, r10, r15)     // Catch:{ NameNotFoundException -> 0x0126, NotFoundException -> 0x0101 }
            android.content.pm.PackageManager r10 = r4.getPackageManager()     // Catch:{ NameNotFoundException -> 0x0126, NotFoundException -> 0x0101 }
            r22 = r12
            int r12 = r5.iconId     // Catch:{ NameNotFoundException -> 0x0128, NotFoundException -> 0x00ff }
            android.graphics.drawable.Drawable r0 = r0.getDrawable(r12)     // Catch:{ NameNotFoundException -> 0x0128, NotFoundException -> 0x00ff }
            android.graphics.drawable.Drawable r0 = r10.getUserBadgedIcon(r0, r15)     // Catch:{ NameNotFoundException -> 0x0128, NotFoundException -> 0x00ff }
            r16 = r0
            goto L_0x013e
        L_0x00ff:
            r0 = move-exception
            goto L_0x010d
        L_0x0101:
            r0 = move-exception
            goto L_0x010b
        L_0x0103:
            r21 = r10
            r22 = r12
            goto L_0x013e
        L_0x0108:
            r0 = move-exception
            r21 = r10
        L_0x010b:
            r22 = r12
        L_0x010d:
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            java.lang.String r12 = "Invalid icon id for account type "
            r10.append(r12)
            java.lang.String r5 = r5.type
            r10.append(r5)
            java.lang.String r5 = r10.toString()
            android.util.Log.w(r8, r5, r0)
            goto L_0x013e
        L_0x0124:
            r21 = r10
        L_0x0126:
            r22 = r12
        L_0x0128:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r10 = "Bad package name for account type "
            r0.append(r10)
            java.lang.String r5 = r5.type
            r0.append(r5)
            java.lang.String r0 = r0.toString()
            android.util.Log.w(r8, r0)
        L_0x013e:
            if (r16 != 0) goto L_0x0148
            android.content.pm.PackageManager r0 = r4.getPackageManager()
            android.graphics.drawable.Drawable r16 = r0.getDefaultActivityIcon()
        L_0x0148:
            r0 = r16
            r5 = 2131558622(0x7f0d00de, float:1.8742565E38)
            r8 = 0
            android.view.View r5 = r9.inflate(r5, r3, r8)
            r8 = 16908294(0x1020006, float:2.3877246E-38)
            android.view.View r8 = r5.findViewById(r8)
            android.widget.ImageView r8 = (android.widget.ImageView) r8
            r8.setImageDrawable(r0)
            r8 = 16908310(0x1020016, float:2.387729E-38)
            android.view.View r0 = r5.findViewById(r8)
            android.widget.TextView r0 = (android.widget.TextView) r0
            java.lang.String r10 = r13.name
            r0.setText(r10)
            r3.addView(r5)
        L_0x016f:
            int r6 = r6 + 1
            r5 = r19
            r8 = r20
            r10 = r21
            r12 = r22
            goto L_0x008a
        L_0x017b:
            r20 = r8
            r0 = r17
        L_0x017f:
            int r11 = r11 + 1
            r5 = r24
            r6 = r18
            r8 = r20
            goto L_0x003b
        L_0x0189:
            r5 = 1
            r4 = 0
            if (r0 <= 0) goto L_0x0193
            r2.setVisibility(r4)
            r3.setVisibility(r4)
        L_0x0193:
            android.view.View r0 = r1.mContentView
            r1 = 2131362671(0x7f0a036f, float:1.834513E38)
            android.view.View r0 = r0.findViewById(r1)
            int r1 = r24.getUserCount()
            int r1 = r1 - r7
            if (r1 <= 0) goto L_0x01a4
            goto L_0x01a5
        L_0x01a4:
            r5 = r4
        L_0x01a5:
            if (r5 == 0) goto L_0x01a8
            goto L_0x01ab
        L_0x01a8:
            r10 = 8
            r4 = r10
        L_0x01ab:
            r0.setVisibility(r4)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.MasterClear.loadAccountList(android.os.UserManager):void");
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        Context context = getContext();
        RestrictedLockUtils.EnforcedAdmin checkIfRestrictionEnforced = RestrictedLockUtilsInternal.checkIfRestrictionEnforced(context, "no_factory_reset", UserHandle.myUserId());
        if ((!UserManager.get(context).isAdminUser() || RestrictedLockUtilsInternal.hasBaseUserRestriction(context, "no_factory_reset", UserHandle.myUserId())) && !Utils.isDemoUser(context)) {
            return layoutInflater.inflate(C1715R.layout.master_clear_disallowed_screen, (ViewGroup) null);
        }
        if (checkIfRestrictionEnforced != null) {
            AlertDialog.Builder prepareDialogBuilder = new ActionDisabledByAdminDialogHelper(getActivity()).prepareDialogBuilder("no_factory_reset", checkIfRestrictionEnforced);
            prepareDialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public final void onDismiss(DialogInterface dialogInterface) {
                    MasterClear.this.lambda$onCreateView$0$MasterClear(dialogInterface);
                }
            });
            prepareDialogBuilder.show();
            return new View(getContext());
        }
        this.mContentView = layoutInflater.inflate(C1715R.layout.master_clear, (ViewGroup) null);
        establishInitialState();
        return this.mContentView;
    }

    public /* synthetic */ void lambda$onCreateView$0$MasterClear(DialogInterface dialogInterface) {
        getActivity().finish();
    }
}
