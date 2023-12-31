package com.android.settings.tts;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TtsEngines;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.widget.GearPreference;
import com.android.settings.widget.SeekBarPreference;
import com.android.settingslib.widget.ActionButtonsPreference;
import com.havoc.config.center.C1715R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Set;

public class TextToSpeechSettings extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener, GearPreference.OnGearClickListener {
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean z) {
            SearchIndexableResource searchIndexableResource = new SearchIndexableResource(context);
            searchIndexableResource.xmlResId = C1715R.xml.tts_settings;
            return Arrays.asList(new SearchIndexableResource[]{searchIndexableResource});
        }
    };
    private ActionButtonsPreference mActionButtons;
    private List<String> mAvailableStrLocals;
    private Locale mCurrentDefaultLocale;
    private String mCurrentEngine;
    private int mDefaultPitch = 100;
    private SeekBarPreference mDefaultPitchPref;
    private int mDefaultRate = 100;
    private SeekBarPreference mDefaultRatePref;
    private TtsEngines mEnginesHelper = null;
    private final TextToSpeech.OnInitListener mInitListener = new TextToSpeech.OnInitListener() {
        public final void onInit(int i) {
            TextToSpeechSettings.this.onInitEngine(i);
        }
    };
    private ListPreference mLocalePreference;
    private String mSampleText = null;
    private int mSelectedLocaleIndex = -1;
    private TextToSpeech mTts = null;

    public int getMetricsCategory() {
        return 94;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(C1715R.xml.tts_settings);
        getActivity().setVolumeControlStream(3);
        this.mEnginesHelper = new TtsEngines(getActivity().getApplicationContext());
        this.mLocalePreference = (ListPreference) findPreference("tts_default_lang");
        this.mLocalePreference.setOnPreferenceChangeListener(this);
        this.mDefaultPitchPref = (SeekBarPreference) findPreference("tts_default_pitch");
        this.mDefaultRatePref = (SeekBarPreference) findPreference("tts_default_rate");
        boolean z = false;
        this.mActionButtons = ((ActionButtonsPreference) findPreference("action_buttons")).setButton1Text(C1715R.string.tts_play).setButton1OnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                TextToSpeechSettings.this.lambda$onCreate$0$TextToSpeechSettings(view);
            }
        }).setButton1Enabled(false).setButton2Text(C1715R.string.tts_reset).setButton2OnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                TextToSpeechSettings.this.lambda$onCreate$1$TextToSpeechSettings(view);
            }
        }).setButton1Enabled(true);
        if (bundle == null) {
            this.mLocalePreference.setEnabled(false);
            this.mLocalePreference.setEntries(new CharSequence[0]);
            this.mLocalePreference.setEntryValues(new CharSequence[0]);
        } else {
            CharSequence[] charSequenceArray = bundle.getCharSequenceArray("locale_entries");
            CharSequence[] charSequenceArray2 = bundle.getCharSequenceArray("locale_entry_values");
            CharSequence charSequence = bundle.getCharSequence("locale_value");
            this.mLocalePreference.setEntries(charSequenceArray);
            this.mLocalePreference.setEntryValues(charSequenceArray2);
            this.mLocalePreference.setValue(charSequence != null ? charSequence.toString() : null);
            ListPreference listPreference = this.mLocalePreference;
            if (charSequenceArray.length > 0) {
                z = true;
            }
            listPreference.setEnabled(z);
        }
        this.mTts = new TextToSpeech(getActivity().getApplicationContext(), this.mInitListener);
        setTtsUtteranceProgressListener();
        initSettings();
        setRetainInstance(true);
    }

    public /* synthetic */ void lambda$onCreate$0$TextToSpeechSettings(View view) {
        speakSampleText();
    }

    public /* synthetic */ void lambda$onCreate$1$TextToSpeechSettings(View view) {
        resetTts();
    }

    public void onResume() {
        super.onResume();
        getListView().getItemAnimator().setMoveDuration(0);
        TextToSpeech textToSpeech = this.mTts;
        if (textToSpeech != null && this.mCurrentDefaultLocale != null) {
            if (!textToSpeech.getDefaultEngine().equals(this.mTts.getCurrentEngine())) {
                try {
                    this.mTts.shutdown();
                    this.mTts = null;
                } catch (Exception e) {
                    Log.e("TextToSpeechSettings", "Error shutting down TTS engine" + e);
                }
                this.mTts = new TextToSpeech(getActivity().getApplicationContext(), this.mInitListener);
                setTtsUtteranceProgressListener();
                initSettings();
            } else {
                this.mTts.setPitch(((float) Settings.Secure.getInt(getContentResolver(), "tts_default_pitch", 100)) / 100.0f);
            }
            Locale defaultLanguage = this.mTts.getDefaultLanguage();
            Locale locale = this.mCurrentDefaultLocale;
            if (locale != null && !locale.equals(defaultLanguage)) {
                updateWidgetState(false);
                checkDefaultLocale();
            }
        }
    }

    private void setTtsUtteranceProgressListener() {
        TextToSpeech textToSpeech = this.mTts;
        if (textToSpeech != null) {
            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                public void onStart(String str) {
                    TextToSpeechSettings.this.updateWidgetState(false);
                }

                public void onDone(String str) {
                    TextToSpeechSettings.this.updateWidgetState(true);
                }

                public void onError(String str) {
                    Log.e("TextToSpeechSettings", "Error while trying to synthesize sample text");
                    TextToSpeechSettings.this.updateWidgetState(true);
                }
            });
        }
    }

    public void onDestroy() {
        super.onDestroy();
        TextToSpeech textToSpeech = this.mTts;
        if (textToSpeech != null) {
            textToSpeech.shutdown();
            this.mTts = null;
        }
    }

    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putCharSequenceArray("locale_entries", this.mLocalePreference.getEntries());
        bundle.putCharSequenceArray("locale_entry_values", this.mLocalePreference.getEntryValues());
        bundle.putCharSequence("locale_value", this.mLocalePreference.getValue());
    }

    private void initSettings() {
        ContentResolver contentResolver = getContentResolver();
        this.mDefaultRate = Settings.Secure.getInt(contentResolver, "tts_default_rate", 100);
        this.mDefaultPitch = Settings.Secure.getInt(contentResolver, "tts_default_pitch", 100);
        this.mDefaultRatePref.setProgress(getSeekBarProgressFromValue("tts_default_rate", this.mDefaultRate));
        this.mDefaultRatePref.setOnPreferenceChangeListener(this);
        this.mDefaultRatePref.setMax(getSeekBarProgressFromValue("tts_default_rate", 600));
        this.mDefaultPitchPref.setProgress(getSeekBarProgressFromValue("tts_default_pitch", this.mDefaultPitch));
        this.mDefaultPitchPref.setOnPreferenceChangeListener(this);
        this.mDefaultPitchPref.setMax(getSeekBarProgressFromValue("tts_default_pitch", 400));
        TextToSpeech textToSpeech = this.mTts;
        if (textToSpeech != null) {
            this.mCurrentEngine = textToSpeech.getCurrentEngine();
            this.mTts.setSpeechRate(((float) this.mDefaultRate) / 100.0f);
            this.mTts.setPitch(((float) this.mDefaultPitch) / 100.0f);
        }
        if (getActivity() instanceof SettingsActivity) {
            SettingsActivity settingsActivity = (SettingsActivity) getActivity();
            String str = this.mCurrentEngine;
            if (str != null) {
                TextToSpeech.EngineInfo engineInfo = this.mEnginesHelper.getEngineInfo(str);
                Preference findPreference = findPreference("tts_engine_preference");
                ((GearPreference) findPreference).setOnGearClickListener(this);
                findPreference.setSummary((CharSequence) engineInfo.label);
            }
            checkVoiceData(this.mCurrentEngine);
            return;
        }
        throw new IllegalStateException("TextToSpeechSettings used outside a Settings");
    }

    private int getValueFromSeekBarProgress(String str, int i) {
        if (str.equals("tts_default_rate")) {
            return i + 10;
        }
        return str.equals("tts_default_pitch") ? i + 25 : i;
    }

    private int getSeekBarProgressFromValue(String str, int i) {
        if (str.equals("tts_default_rate")) {
            return i - 10;
        }
        return str.equals("tts_default_pitch") ? i - 25 : i;
    }

    public void onInitEngine(int i) {
        if (i == 0) {
            checkDefaultLocale();
            getActivity().runOnUiThread(new Runnable() {
                public final void run() {
                    TextToSpeechSettings.this.lambda$onInitEngine$2$TextToSpeechSettings();
                }
            });
            return;
        }
        updateWidgetState(false);
    }

    public /* synthetic */ void lambda$onInitEngine$2$TextToSpeechSettings() {
        this.mLocalePreference.setEnabled(true);
    }

    private void checkDefaultLocale() {
        Locale defaultLanguage = this.mTts.getDefaultLanguage();
        if (defaultLanguage == null) {
            Log.e("TextToSpeechSettings", "Failed to get default language from engine " + this.mCurrentEngine);
            updateWidgetState(false);
            return;
        }
        Locale locale = this.mCurrentDefaultLocale;
        this.mCurrentDefaultLocale = this.mEnginesHelper.parseLocaleString(defaultLanguage.toString());
        if (!Objects.equals(locale, this.mCurrentDefaultLocale)) {
            this.mSampleText = null;
        }
        this.mTts.setLanguage(defaultLanguage);
        if (evaluateDefaultLocale() && this.mSampleText == null) {
            getSampleText();
        }
    }

    private boolean evaluateDefaultLocale() {
        boolean z;
        Locale locale = this.mCurrentDefaultLocale;
        if (!(locale == null || this.mAvailableStrLocals == null)) {
            try {
                String iSO3Language = locale.getISO3Language();
                if (!TextUtils.isEmpty(this.mCurrentDefaultLocale.getISO3Country())) {
                    iSO3Language = iSO3Language + "-" + this.mCurrentDefaultLocale.getISO3Country();
                }
                if (!TextUtils.isEmpty(this.mCurrentDefaultLocale.getVariant())) {
                    iSO3Language = iSO3Language + "-" + this.mCurrentDefaultLocale.getVariant();
                }
                Iterator<String> it = this.mAvailableStrLocals.iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (it.next().equalsIgnoreCase(iSO3Language)) {
                            z = false;
                            break;
                        }
                    } else {
                        z = true;
                        break;
                    }
                }
                int language = this.mTts.setLanguage(this.mCurrentDefaultLocale);
                if (language == -2 || language == -1 || z) {
                    updateWidgetState(false);
                    return false;
                }
                updateWidgetState(true);
                return true;
            } catch (MissingResourceException unused) {
                updateWidgetState(false);
            }
        }
        return false;
    }

    private void getSampleText() {
        String currentEngine = this.mTts.getCurrentEngine();
        if (TextUtils.isEmpty(currentEngine)) {
            currentEngine = this.mTts.getDefaultEngine();
        }
        Intent intent = new Intent("android.speech.tts.engine.GET_SAMPLE_TEXT");
        intent.putExtra("language", this.mCurrentDefaultLocale.getLanguage());
        intent.putExtra("country", this.mCurrentDefaultLocale.getCountry());
        intent.putExtra("variant", this.mCurrentDefaultLocale.getVariant());
        intent.setPackage(currentEngine);
        try {
            startActivityForResult(intent, 1983);
        } catch (ActivityNotFoundException unused) {
            Log.e("TextToSpeechSettings", "Failed to get sample text, no activity found for " + intent + ")");
        }
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == 1983) {
            onSampleTextReceived(i2, intent);
        } else if (i == 1977) {
            onVoiceDataIntegrityCheckDone(intent);
            if (i2 != 0) {
                updateDefaultLocalePref(intent);
            }
        }
    }

    private void updateDefaultLocalePref(Intent intent) {
        ArrayList<String> stringArrayListExtra = intent.getStringArrayListExtra("availableVoices");
        intent.getStringArrayListExtra("unavailableVoices");
        if (stringArrayListExtra == null || stringArrayListExtra.size() == 0) {
            this.mLocalePreference.setEnabled(false);
            return;
        }
        Locale locale = null;
        if (!this.mEnginesHelper.isLocaleSetToDefaultForEngine(this.mTts.getCurrentEngine())) {
            locale = this.mEnginesHelper.getLocalePrefForEngine(this.mTts.getCurrentEngine());
        }
        ArrayList arrayList = new ArrayList(stringArrayListExtra.size());
        for (int i = 0; i < stringArrayListExtra.size(); i++) {
            Locale parseLocaleString = this.mEnginesHelper.parseLocaleString(stringArrayListExtra.get(i));
            if (parseLocaleString != null) {
                arrayList.add(new Pair(parseLocaleString.getDisplayName(), parseLocaleString));
            }
        }
        Collections.sort(arrayList, $$Lambda$TextToSpeechSettings$D4bw4_ZUETt7NJagxWCp3GKL3oI.INSTANCE);
        this.mSelectedLocaleIndex = 0;
        CharSequence[] charSequenceArr = new CharSequence[(stringArrayListExtra.size() + 1)];
        CharSequence[] charSequenceArr2 = new CharSequence[(stringArrayListExtra.size() + 1)];
        charSequenceArr[0] = getActivity().getString(C1715R.string.tts_lang_use_system);
        charSequenceArr2[0] = "";
        Iterator it = arrayList.iterator();
        int i2 = 1;
        while (it.hasNext()) {
            Pair pair = (Pair) it.next();
            if (((Locale) pair.second).equals(locale)) {
                this.mSelectedLocaleIndex = i2;
            }
            charSequenceArr[i2] = (CharSequence) pair.first;
            charSequenceArr2[i2] = ((Locale) pair.second).toString();
            i2++;
        }
        this.mLocalePreference.setEntries(charSequenceArr);
        this.mLocalePreference.setEntryValues(charSequenceArr2);
        this.mLocalePreference.setEnabled(true);
        setLocalePreference(this.mSelectedLocaleIndex);
    }

    private void setLocalePreference(int i) {
        if (i < 0) {
            this.mLocalePreference.setValue("");
            this.mLocalePreference.setSummary((int) C1715R.string.tts_lang_not_selected);
            return;
        }
        this.mLocalePreference.setValueIndex(i);
        ListPreference listPreference = this.mLocalePreference;
        listPreference.setSummary(listPreference.getEntries()[i]);
    }

    private String getDefaultSampleString() {
        TextToSpeech textToSpeech = this.mTts;
        if (!(textToSpeech == null || textToSpeech.getLanguage() == null)) {
            try {
                String iSO3Language = this.mTts.getLanguage().getISO3Language();
                String[] stringArray = getActivity().getResources().getStringArray(C1715R.array.tts_demo_strings);
                String[] stringArray2 = getActivity().getResources().getStringArray(C1715R.array.tts_demo_string_langs);
                for (int i = 0; i < stringArray.length; i++) {
                    if (stringArray2[i].equals(iSO3Language)) {
                        return stringArray[i];
                    }
                }
            } catch (MissingResourceException unused) {
            }
        }
        return getString(C1715R.string.tts_default_sample_string);
    }

    private boolean isNetworkRequiredForSynthesis() {
        Set<String> features = this.mTts.getFeatures(this.mCurrentDefaultLocale);
        if (features != null && features.contains("networkTts") && !features.contains("embeddedTts")) {
            return true;
        }
        return false;
    }

    private void onSampleTextReceived(int i, Intent intent) {
        String defaultSampleString = getDefaultSampleString();
        if (!(i != 0 || intent == null || intent == null || intent.getStringExtra("sampleText") == null)) {
            defaultSampleString = intent.getStringExtra("sampleText");
        }
        this.mSampleText = defaultSampleString;
        if (this.mSampleText != null) {
            updateWidgetState(true);
        } else {
            Log.e("TextToSpeechSettings", "Did not have a sample string for the requested language. Using default");
        }
    }

    private void speakSampleText() {
        boolean isNetworkRequiredForSynthesis = isNetworkRequiredForSynthesis();
        if (!isNetworkRequiredForSynthesis || (isNetworkRequiredForSynthesis && this.mTts.isLanguageAvailable(this.mCurrentDefaultLocale) >= 0)) {
            HashMap hashMap = new HashMap();
            hashMap.put("utteranceId", "Sample");
            this.mTts.speak(this.mSampleText, 0, hashMap);
            return;
        }
        Log.w("TextToSpeechSettings", "Network required for sample synthesis for requested language");
        displayNetworkAlert();
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        if ("tts_default_rate".equals(preference.getKey())) {
            updateSpeechRate(((Integer) obj).intValue());
        } else if ("tts_default_pitch".equals(preference.getKey())) {
            updateSpeechPitchValue(((Integer) obj).intValue());
        } else if (preference == this.mLocalePreference) {
            String str = (String) obj;
            updateLanguageTo(!TextUtils.isEmpty(str) ? this.mEnginesHelper.parseLocaleString(str) : null);
            checkDefaultLocale();
        }
        return true;
    }

    private void updateLanguageTo(Locale locale) {
        String locale2 = locale != null ? locale.toString() : "";
        int i = 0;
        while (true) {
            if (i >= this.mLocalePreference.getEntryValues().length) {
                i = -1;
                break;
            } else if (locale2.equalsIgnoreCase(this.mLocalePreference.getEntryValues()[i].toString())) {
                break;
            } else {
                i++;
            }
        }
        if (i == -1) {
            Log.w("TextToSpeechSettings", "updateLanguageTo called with unknown locale argument");
            return;
        }
        ListPreference listPreference = this.mLocalePreference;
        listPreference.setSummary(listPreference.getEntries()[i]);
        this.mSelectedLocaleIndex = i;
        this.mEnginesHelper.updateLocalePrefForEngine(this.mTts.getCurrentEngine(), locale);
        TextToSpeech textToSpeech = this.mTts;
        if (locale == null) {
            locale = Locale.getDefault();
        }
        textToSpeech.setLanguage(locale);
    }

    private void resetTts() {
        int seekBarProgressFromValue = getSeekBarProgressFromValue("tts_default_rate", 100);
        this.mDefaultRatePref.setProgress(seekBarProgressFromValue);
        updateSpeechRate(seekBarProgressFromValue);
        int seekBarProgressFromValue2 = getSeekBarProgressFromValue("tts_default_pitch", 100);
        this.mDefaultPitchPref.setProgress(seekBarProgressFromValue2);
        updateSpeechPitchValue(seekBarProgressFromValue2);
    }

    private void updateSpeechRate(int i) {
        this.mDefaultRate = getValueFromSeekBarProgress("tts_default_rate", i);
        try {
            Settings.Secure.putInt(getContentResolver(), "tts_default_rate", this.mDefaultRate);
            if (this.mTts != null) {
                this.mTts.setSpeechRate(((float) this.mDefaultRate) / 100.0f);
            }
        } catch (NumberFormatException e) {
            Log.e("TextToSpeechSettings", "could not persist default TTS rate setting", e);
        }
    }

    private void updateSpeechPitchValue(int i) {
        this.mDefaultPitch = getValueFromSeekBarProgress("tts_default_pitch", i);
        try {
            Settings.Secure.putInt(getContentResolver(), "tts_default_pitch", this.mDefaultPitch);
            if (this.mTts != null) {
                this.mTts.setPitch(((float) this.mDefaultPitch) / 100.0f);
            }
        } catch (NumberFormatException e) {
            Log.e("TextToSpeechSettings", "could not persist default TTS pitch setting", e);
        }
    }

    /* access modifiers changed from: private */
    public void updateWidgetState(boolean z) {
        getActivity().runOnUiThread(new Runnable(z) {
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                TextToSpeechSettings.this.lambda$updateWidgetState$4$TextToSpeechSettings(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$updateWidgetState$4$TextToSpeechSettings(boolean z) {
        this.mActionButtons.setButton1Enabled(z);
        this.mDefaultRatePref.setEnabled(z);
        this.mDefaultPitchPref.setEnabled(z);
    }

    private void displayNetworkAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(17039380);
        builder.setMessage((CharSequence) getActivity().getString(C1715R.string.tts_engine_network_required));
        builder.setCancelable(false);
        builder.setPositiveButton(17039370, (DialogInterface.OnClickListener) null);
        builder.create().show();
    }

    private void checkVoiceData(String str) {
        Intent intent = new Intent("android.speech.tts.engine.CHECK_TTS_DATA");
        intent.setPackage(str);
        try {
            startActivityForResult(intent, 1977);
        } catch (ActivityNotFoundException unused) {
            Log.e("TextToSpeechSettings", "Failed to check TTS data, no activity found for " + intent + ")");
        }
    }

    private void onVoiceDataIntegrityCheckDone(Intent intent) {
        String currentEngine = this.mTts.getCurrentEngine();
        if (currentEngine == null) {
            Log.e("TextToSpeechSettings", "Voice data check complete, but no engine bound");
        } else if (intent == null) {
            Log.e("TextToSpeechSettings", "Engine failed voice data integrity check (null return)" + this.mTts.getCurrentEngine());
        } else {
            Settings.Secure.putString(getContentResolver(), "tts_default_synth", currentEngine);
            this.mAvailableStrLocals = intent.getStringArrayListExtra("availableVoices");
            if (this.mAvailableStrLocals == null) {
                Log.e("TextToSpeechSettings", "Voice data check complete, but no available voices found");
                this.mAvailableStrLocals = new ArrayList();
            }
            if (evaluateDefaultLocale()) {
                getSampleText();
            }
        }
    }

    public void onGearClick(GearPreference gearPreference) {
        if ("tts_engine_preference".equals(gearPreference.getKey())) {
            Intent settingsIntent = this.mEnginesHelper.getSettingsIntent(this.mEnginesHelper.getEngineInfo(this.mCurrentEngine).name);
            if (settingsIntent != null) {
                startActivity(settingsIntent);
            } else {
                Log.e("TextToSpeechSettings", "settingsIntent is null");
            }
        }
    }
}
