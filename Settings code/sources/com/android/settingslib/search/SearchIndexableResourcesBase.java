package com.android.settingslib.search;

import com.android.settings.DateTimeSettings;
import com.android.settings.LegalSettings;
import com.android.settings.TetherSettings;
import com.android.settings.accessibility.AccessibilityControlTimeoutPreferenceFragment;
import com.android.settings.accessibility.AccessibilitySettings;
import com.android.settings.accessibility.AccessibilityShortcutPreferenceFragment;
import com.android.settings.accessibility.MagnificationPreferenceFragment;
import com.android.settings.accessibility.ToggleAutoclickPreferenceFragment;
import com.android.settings.accessibility.ToggleDaltonizerPreferenceFragment;
import com.android.settings.accessibility.VibrationSettings;
import com.android.settings.accounts.AccountDashboardFragment;
import com.android.settings.accounts.ChooseAccountFragment;
import com.android.settings.accounts.ManagedProfileSettings;
import com.android.settings.applications.AppAndNotificationDashboardFragment;
import com.android.settings.applications.assist.ManageAssist;
import com.android.settings.applications.specialaccess.SpecialAccessSettings;
import com.android.settings.applications.specialaccess.deviceadmin.DeviceAdminSettings;
import com.android.settings.applications.specialaccess.pictureinpicture.PictureInPictureSettings;
import com.android.settings.applications.specialaccess.premiumsms.PremiumSmsAccess;
import com.android.settings.applications.specialaccess.vrlistener.VrListenerSettings;
import com.android.settings.backup.BackupSettingsFragment;
import com.android.settings.backup.PrivacySettings;
import com.android.settings.backup.UserBackupSettingsActivity;
import com.android.settings.biometrics.face.FaceSettings;
import com.android.settings.connecteddevice.BluetoothDashboardFragment;
import com.android.settings.datausage.BillingCycleSettings;
import com.android.settings.datausage.DataSaverSummary;
import com.android.settings.datausage.UnrestrictedDataAccess;
import com.android.settings.deletionhelper.AutomaticStorageManagerSettings;
import com.android.settings.development.featureflags.FeatureFlagsDashboard;
import com.android.settings.development.gamedriver.GameDriverDashboard;
import com.android.settings.development.qstile.DevelopmentTileConfigFragment;
import com.android.settings.deviceinfo.StorageDashboardFragment;
import com.android.settings.deviceinfo.StorageSettings;
import com.android.settings.deviceinfo.aboutphone.MyDeviceInfoFragment;
import com.android.settings.deviceinfo.firmwareversion.FirmwareVersionSettings;
import com.android.settings.deviceinfo.hardwareinfo.HardwareInfoFragment;
import com.android.settings.display.ColorModePreferenceFragment;
import com.android.settings.display.ToggleFontSizePreferenceFragment;
import com.android.settings.dream.DreamSettings;
import com.android.settings.enterprise.EnterprisePrivacySettings;
import com.android.settings.gestures.AssistGestureSettings;
import com.android.settings.gestures.DoubleTapPowerSettings;
import com.android.settings.gestures.DoubleTapScreenSettings;
import com.android.settings.gestures.DoubleTwistGestureSettings;
import com.android.settings.gestures.GestureSettings;
import com.android.settings.gestures.GlobalActionsPanelSettings;
import com.android.settings.gestures.PickupGestureSettings;
import com.android.settings.gestures.PreventRingingGestureSettings;
import com.android.settings.gestures.SwipeToNotificationSettings;
import com.android.settings.gestures.SystemNavigationGestureSettings;
import com.android.settings.gestures.TapScreenGestureSettings;
import com.android.settings.inputmethod.AvailableVirtualKeyboardFragment;
import com.android.settings.inputmethod.PhysicalKeyboardFragment;
import com.android.settings.inputmethod.UserDictionaryList;
import com.android.settings.inputmethod.VirtualKeyboardFragment;
import com.android.settings.language.LanguageAndInputSettings;
import com.android.settings.location.LocationSettings;
import com.android.settings.location.RecentLocationRequestSeeAllFragment;
import com.android.settings.network.NetworkDashboardFragment;
import com.android.settings.nfc.PaymentSettings;
import com.android.settings.notification.AppBubbleNotificationSettings;
import com.android.settings.notification.ConfigureNotificationSettings;
import com.android.settings.notification.NotificationAccessSettings;
import com.android.settings.notification.NotificationStation;
import com.android.settings.notification.SoundSettings;
import com.android.settings.notification.ZenAccessSettings;
import com.android.settings.notification.ZenModeAutomationSettings;
import com.android.settings.notification.ZenModeBlockedEffectsSettings;
import com.android.settings.notification.ZenModeBypassingAppsSettings;
import com.android.settings.notification.ZenModeCallsSettings;
import com.android.settings.notification.ZenModeMessagesSettings;
import com.android.settings.notification.ZenModeSettings;
import com.android.settings.notification.ZenModeSoundVibrationSettings;
import com.android.settings.print.PrintSettingsFragment;
import com.android.settings.privacy.PrivacyDashboardFragment;
import com.android.settings.security.EncryptionAndCredential;
import com.android.settings.security.LockscreenDashboardFragment;
import com.android.settings.security.ScreenPinningSettings;
import com.android.settings.security.SecuritySettings;
import com.android.settings.security.screenlock.ScreenLockSettings;
import com.android.settings.security.trustagent.TrustAgentSettings;
import com.android.settings.sim.SimSettings;
import com.android.settings.support.SupportDashboardActivity;
import com.android.settings.system.ResetDashboardFragment;
import com.android.settings.system.SystemDashboardFragment;
import com.android.settings.tts.TextToSpeechSettings;
import com.android.settings.tts.TtsEnginePreferenceFragment;
import com.android.settings.users.UserSettings;
import com.android.settings.wallpaper.WallpaperSuggestionActivity;
import com.android.settings.wifi.ConfigureWifiSettings;
import com.android.settings.wifi.WifiSettings;
import com.android.settings.wifi.tether.WifiTetherSettings;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SearchIndexableResourcesBase implements SearchIndexableResources {
    private final Set<Class> mProviders = new HashSet();

    public SearchIndexableResourcesBase() {
        addIndex(DateTimeSettings.class);
        addIndex(LegalSettings.class);
        addIndex(TetherSettings.class);
        addIndex(AccessibilityControlTimeoutPreferenceFragment.class);
        addIndex(AccessibilitySettings.class);
        addIndex(AccessibilityShortcutPreferenceFragment.class);
        addIndex(MagnificationPreferenceFragment.class);
        addIndex(ToggleAutoclickPreferenceFragment.class);
        addIndex(ToggleDaltonizerPreferenceFragment.class);
        addIndex(VibrationSettings.class);
        addIndex(AccountDashboardFragment.class);
        addIndex(ChooseAccountFragment.class);
        addIndex(ManagedProfileSettings.class);
        addIndex(AppAndNotificationDashboardFragment.class);
        addIndex(ManageAssist.class);
        addIndex(SpecialAccessSettings.class);
        addIndex(DeviceAdminSettings.class);
        addIndex(PictureInPictureSettings.class);
        addIndex(PremiumSmsAccess.class);
        addIndex(VrListenerSettings.class);
        addIndex(BackupSettingsFragment.class);
        addIndex(PrivacySettings.class);
        addIndex(UserBackupSettingsActivity.class);
        addIndex(FaceSettings.class);
        addIndex(BluetoothDashboardFragment.class);
        addIndex(BillingCycleSettings.class);
        addIndex(DataSaverSummary.class);
        addIndex(UnrestrictedDataAccess.class);
        addIndex(AutomaticStorageManagerSettings.class);
        addIndex(FeatureFlagsDashboard.class);
        addIndex(GameDriverDashboard.class);
        addIndex(DevelopmentTileConfigFragment.class);
        addIndex(StorageDashboardFragment.class);
        addIndex(StorageSettings.class);
        addIndex(MyDeviceInfoFragment.class);
        addIndex(FirmwareVersionSettings.class);
        addIndex(HardwareInfoFragment.class);
        addIndex(ColorModePreferenceFragment.class);
        addIndex(ToggleFontSizePreferenceFragment.class);
        addIndex(DreamSettings.class);
        addIndex(EnterprisePrivacySettings.class);
        addIndex(AssistGestureSettings.class);
        addIndex(DoubleTapPowerSettings.class);
        addIndex(DoubleTapScreenSettings.class);
        addIndex(DoubleTwistGestureSettings.class);
        addIndex(GestureSettings.class);
        addIndex(GlobalActionsPanelSettings.class);
        addIndex(PickupGestureSettings.class);
        addIndex(PreventRingingGestureSettings.class);
        addIndex(SwipeToNotificationSettings.class);
        addIndex(SystemNavigationGestureSettings.class);
        addIndex(TapScreenGestureSettings.class);
        addIndex(AvailableVirtualKeyboardFragment.class);
        addIndex(PhysicalKeyboardFragment.class);
        addIndex(UserDictionaryList.class);
        addIndex(VirtualKeyboardFragment.class);
        addIndex(LanguageAndInputSettings.class);
        addIndex(LocationSettings.class);
        addIndex(RecentLocationRequestSeeAllFragment.class);
        addIndex(NetworkDashboardFragment.class);
        addIndex(PaymentSettings.class);
        addIndex(AppBubbleNotificationSettings.class);
        addIndex(ConfigureNotificationSettings.class);
        addIndex(NotificationAccessSettings.class);
        addIndex(NotificationStation.class);
        addIndex(SoundSettings.class);
        addIndex(ZenAccessSettings.class);
        addIndex(ZenModeAutomationSettings.class);
        addIndex(ZenModeBlockedEffectsSettings.class);
        addIndex(ZenModeBypassingAppsSettings.class);
        addIndex(ZenModeCallsSettings.class);
        addIndex(ZenModeMessagesSettings.class);
        addIndex(ZenModeSettings.class);
        addIndex(ZenModeSoundVibrationSettings.class);
        addIndex(PrintSettingsFragment.class);
        addIndex(PrivacyDashboardFragment.class);
        addIndex(EncryptionAndCredential.class);
        addIndex(LockscreenDashboardFragment.class);
        addIndex(ScreenPinningSettings.class);
        addIndex(SecuritySettings.class);
        addIndex(ScreenLockSettings.class);
        addIndex(TrustAgentSettings.class);
        addIndex(SimSettings.class);
        addIndex(SupportDashboardActivity.class);
        addIndex(ResetDashboardFragment.class);
        addIndex(SystemDashboardFragment.class);
        addIndex(TextToSpeechSettings.class);
        addIndex(TtsEnginePreferenceFragment.class);
        addIndex(UserSettings.class);
        addIndex(WallpaperSuggestionActivity.class);
        addIndex(ConfigureWifiSettings.class);
        addIndex(WifiSettings.class);
        addIndex(WifiTetherSettings.class);
    }

    public void addIndex(Class cls) {
        this.mProviders.add(cls);
    }

    public Collection<Class> getProviderValues() {
        return this.mProviders;
    }
}
