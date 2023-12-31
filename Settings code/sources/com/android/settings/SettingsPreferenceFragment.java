package com.android.settings;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.settings.CustomListPreference;
import com.android.settings.RestrictedListPreference;
import com.android.settings.core.InstrumentedPreferenceFragment;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.search.Indexable;
import com.android.settings.search.actionbar.SearchMenuController;
import com.android.settings.support.actionbar.HelpMenuController;
import com.android.settings.support.actionbar.HelpResourceProvider;
import com.android.settings.widget.HighlightablePreferenceGroupAdapter;
import com.android.settings.widget.LoadingViewController;
import com.android.settingslib.CustomDialogPreferenceCompat;
import com.android.settingslib.CustomEditTextPreferenceCompat;
import com.android.settingslib.core.lifecycle.ObservablePreferenceFragment;
import com.android.settingslib.widget.FooterPreferenceMixinCompat;
import com.android.settingslib.widget.LayoutPreference;
import com.havoc.config.center.C1715R;
import java.util.UUID;

public abstract class SettingsPreferenceFragment extends InstrumentedPreferenceFragment implements DialogCreatable, HelpResourceProvider, Indexable {
    public HighlightablePreferenceGroupAdapter mAdapter;
    private boolean mAnimationAllowed;
    private ViewGroup mButtonBar;
    private ContentResolver mContentResolver;
    private RecyclerView.Adapter mCurrentRootAdapter;
    private RecyclerView.AdapterDataObserver mDataSetObserver = new RecyclerView.AdapterDataObserver() {
        public void onChanged() {
            SettingsPreferenceFragment.this.onDataSetChanged();
        }

        public void onItemRangeChanged(int i, int i2) {
            SettingsPreferenceFragment.this.onDataSetChanged();
        }

        public void onItemRangeChanged(int i, int i2, Object obj) {
            SettingsPreferenceFragment.this.onDataSetChanged();
        }

        public void onItemRangeInserted(int i, int i2) {
            SettingsPreferenceFragment.this.onDataSetChanged();
        }

        public void onItemRangeRemoved(int i, int i2) {
            SettingsPreferenceFragment.this.onDataSetChanged();
        }

        public void onItemRangeMoved(int i, int i2, int i3) {
            SettingsPreferenceFragment.this.onDataSetChanged();
        }
    };
    /* access modifiers changed from: private */
    public SettingsDialogFragment mDialogFragment;
    private View mEmptyView;
    protected final FooterPreferenceMixinCompat mFooterPreferenceMixin = new FooterPreferenceMixinCompat(this, getSettingsLifecycle());
    private LayoutPreference mHeader;
    private boolean mIsDataSetObserverRegistered = false;
    private LinearLayoutManager mLayoutManager;
    private ViewGroup mPinnedHeaderFrameLayout;
    private ArrayMap<String, Preference> mPreferenceCache;
    public boolean mPreferenceHighlighted = false;

    public int getDialogMetricsCategory(int i) {
        return 0;
    }

    public int getInitialExpandedChildCount() {
        return Integer.MAX_VALUE;
    }

    public Dialog onCreateDialog(int i) {
        return null;
    }

    public void onDialogShowing() {
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        SearchMenuController.init((InstrumentedPreferenceFragment) this);
        HelpMenuController.init((ObservablePreferenceFragment) this);
        if (bundle != null) {
            this.mPreferenceHighlighted = bundle.getBoolean("android:preference_highlighted");
        }
        HighlightablePreferenceGroupAdapter.adjustInitialExpandedChildCount(this);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View onCreateView = super.onCreateView(layoutInflater, viewGroup, bundle);
        this.mPinnedHeaderFrameLayout = (ViewGroup) onCreateView.findViewById(C1715R.C1718id.pinned_header);
        this.mButtonBar = (ViewGroup) onCreateView.findViewById(C1715R.C1718id.button_bar);
        return onCreateView;
    }

    public void addPreferencesFromResource(int i) {
        super.addPreferencesFromResource(i);
        checkAvailablePrefs(getPreferenceScreen());
    }

    /* access modifiers changed from: package-private */
    public void checkAvailablePrefs(PreferenceGroup preferenceGroup) {
        if (preferenceGroup != null) {
            for (int i = 0; i < preferenceGroup.getPreferenceCount(); i++) {
                Preference preference = preferenceGroup.getPreference(i);
                if ((preference instanceof SelfAvailablePreference) && !((SelfAvailablePreference) preference).isAvailable(getContext())) {
                    preference.setVisible(false);
                } else if (preference instanceof PreferenceGroup) {
                    checkAvailablePrefs((PreferenceGroup) preference);
                }
            }
        }
    }

    public ViewGroup getButtonBar() {
        return this.mButtonBar;
    }

    public View setPinnedHeaderView(int i) {
        View inflate = getActivity().getLayoutInflater().inflate(i, this.mPinnedHeaderFrameLayout, false);
        setPinnedHeaderView(inflate);
        return inflate;
    }

    public void setPinnedHeaderView(View view) {
        this.mPinnedHeaderFrameLayout.addView(view);
        this.mPinnedHeaderFrameLayout.setVisibility(0);
    }

    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        HighlightablePreferenceGroupAdapter highlightablePreferenceGroupAdapter = this.mAdapter;
        if (highlightablePreferenceGroupAdapter != null) {
            bundle.putBoolean("android:preference_highlighted", highlightablePreferenceGroupAdapter.isHighlightRequested());
        }
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        setHasOptionsMenu(true);
    }

    public void onResume() {
        super.onResume();
        highlightPreferenceIfNeeded();
    }

    /* access modifiers changed from: protected */
    public void onBindPreferences() {
        registerObserverIfNeeded();
    }

    /* access modifiers changed from: protected */
    public void onUnbindPreferences() {
        unregisterObserverIfNeeded();
    }

    public void setLoading(boolean z, boolean z2) {
        LoadingViewController.handleLoadingContainer(getView().findViewById(C1715R.C1718id.loading_container), getListView(), !z, z2);
    }

    public void registerObserverIfNeeded() {
        if (!this.mIsDataSetObserverRegistered) {
            RecyclerView.Adapter adapter = this.mCurrentRootAdapter;
            if (adapter != null) {
                adapter.unregisterAdapterDataObserver(this.mDataSetObserver);
            }
            this.mCurrentRootAdapter = getListView().getAdapter();
            this.mCurrentRootAdapter.registerAdapterDataObserver(this.mDataSetObserver);
            this.mIsDataSetObserverRegistered = true;
            onDataSetChanged();
        }
    }

    public void unregisterObserverIfNeeded() {
        if (this.mIsDataSetObserverRegistered) {
            RecyclerView.Adapter adapter = this.mCurrentRootAdapter;
            if (adapter != null) {
                adapter.unregisterAdapterDataObserver(this.mDataSetObserver);
                this.mCurrentRootAdapter = null;
            }
            this.mIsDataSetObserverRegistered = false;
        }
    }

    public void highlightPreferenceIfNeeded() {
        HighlightablePreferenceGroupAdapter highlightablePreferenceGroupAdapter;
        if (isAdded() && (highlightablePreferenceGroupAdapter = this.mAdapter) != null) {
            highlightablePreferenceGroupAdapter.requestHighlight(getView(), getListView());
        }
    }

    /* access modifiers changed from: protected */
    public void onDataSetChanged() {
        highlightPreferenceIfNeeded();
        updateEmptyView();
    }

    public LayoutPreference getHeaderView() {
        return this.mHeader;
    }

    /* access modifiers changed from: protected */
    public void setHeaderView(int i) {
        this.mHeader = new LayoutPreference(getPrefContext(), i);
        addPreferenceToTop(this.mHeader);
    }

    private void addPreferenceToTop(LayoutPreference layoutPreference) {
        layoutPreference.setOrder(-1);
        if (getPreferenceScreen() != null) {
            getPreferenceScreen().addPreference(layoutPreference);
        }
    }

    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        LayoutPreference layoutPreference;
        if (preferenceScreen != null && !preferenceScreen.isAttached()) {
            preferenceScreen.setShouldUseGeneratedIds(this.mAnimationAllowed);
        }
        super.setPreferenceScreen(preferenceScreen);
        if (preferenceScreen != null && (layoutPreference = this.mHeader) != null) {
            preferenceScreen.addPreference(layoutPreference);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateEmptyView() {
        if (this.mEmptyView != null) {
            int i = 0;
            if (getPreferenceScreen() != null) {
                View findViewById = getActivity().findViewById(16908351);
                boolean z = true;
                if ((getPreferenceScreen().getPreferenceCount() - (this.mHeader != null ? 1 : 0)) - (this.mFooterPreferenceMixin.hasFooter() ? 1 : 0) > 0 && (findViewById == null || findViewById.getVisibility() == 0)) {
                    z = false;
                }
                View view = this.mEmptyView;
                if (!z) {
                    i = 8;
                }
                view.setVisibility(i);
                return;
            }
            this.mEmptyView.setVisibility(0);
        }
    }

    public void setEmptyView(View view) {
        View view2 = this.mEmptyView;
        if (view2 != null) {
            view2.setVisibility(8);
        }
        this.mEmptyView = view;
        updateEmptyView();
    }

    public View getEmptyView() {
        return this.mEmptyView;
    }

    public RecyclerView.LayoutManager onCreateLayoutManager() {
        this.mLayoutManager = new LinearLayoutManager(getContext());
        return this.mLayoutManager;
    }

    /* access modifiers changed from: protected */
    public RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        String str;
        Bundle arguments = getArguments();
        if (arguments == null) {
            str = null;
        } else {
            str = arguments.getString(":settings:fragment_args_key");
        }
        this.mAdapter = new HighlightablePreferenceGroupAdapter(preferenceScreen, str, this.mPreferenceHighlighted);
        return this.mAdapter;
    }

    /* access modifiers changed from: protected */
    public void setAnimationAllowed(boolean z) {
        this.mAnimationAllowed = z;
    }

    /* access modifiers changed from: protected */
    public void cacheRemoveAllPrefs(PreferenceGroup preferenceGroup) {
        this.mPreferenceCache = new ArrayMap<>();
        int preferenceCount = preferenceGroup.getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = preferenceGroup.getPreference(i);
            if (!TextUtils.isEmpty(preference.getKey())) {
                this.mPreferenceCache.put(preference.getKey(), preference);
            }
        }
    }

    /* access modifiers changed from: protected */
    public Preference getCachedPreference(String str) {
        ArrayMap<String, Preference> arrayMap = this.mPreferenceCache;
        if (arrayMap != null) {
            return arrayMap.remove(str);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void removeCachedPrefs(PreferenceGroup preferenceGroup) {
        for (Preference removePreference : this.mPreferenceCache.values()) {
            preferenceGroup.removePreference(removePreference);
        }
        this.mPreferenceCache = null;
    }

    public boolean removePreference(String str) {
        return removePreference(getPreferenceScreen(), str);
    }

    /* access modifiers changed from: package-private */
    public boolean removePreference(PreferenceGroup preferenceGroup, String str) {
        int preferenceCount = preferenceGroup.getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = preferenceGroup.getPreference(i);
            if (TextUtils.equals(preference.getKey(), str)) {
                return preferenceGroup.removePreference(preference);
            }
            if ((preference instanceof PreferenceGroup) && removePreference((PreferenceGroup) preference, str)) {
                return true;
            }
        }
        return false;
    }

    public final void finishFragment() {
        getActivity().onBackPressed();
    }

    /* access modifiers changed from: protected */
    public ContentResolver getContentResolver() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            this.mContentResolver = activity.getContentResolver();
        }
        return this.mContentResolver;
    }

    /* access modifiers changed from: protected */
    public Object getSystemService(String str) {
        return getActivity().getSystemService(str);
    }

    /* access modifiers changed from: protected */
    public PackageManager getPackageManager() {
        return getActivity().getPackageManager();
    }

    public void onDetach() {
        SettingsDialogFragment settingsDialogFragment;
        if (isRemoving() && (settingsDialogFragment = this.mDialogFragment) != null) {
            settingsDialogFragment.dismiss();
            this.mDialogFragment = null;
        }
        super.onDetach();
    }

    /* access modifiers changed from: protected */
    public void showDialog(int i) {
        if (this.mDialogFragment != null) {
            Log.e("SettingsPreference", "Old dialog fragment not null!");
        }
        this.mDialogFragment = SettingsDialogFragment.newInstance(this, i);
        this.mDialogFragment.show(getChildFragmentManager(), Integer.toString(i));
    }

    /* access modifiers changed from: protected */
    public void removeDialog(int i) {
        SettingsDialogFragment settingsDialogFragment = this.mDialogFragment;
        if (settingsDialogFragment != null && settingsDialogFragment.getDialogId() == i) {
            this.mDialogFragment.dismissAllowingStateLoss();
        }
        this.mDialogFragment = null;
    }

    /* access modifiers changed from: protected */
    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        SettingsDialogFragment settingsDialogFragment = this.mDialogFragment;
        if (settingsDialogFragment != null) {
            DialogInterface.OnDismissListener unused = settingsDialogFragment.mOnDismissListener = onDismissListener;
        }
    }

    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment;
        if (preference.getKey() == null) {
            preference.setKey(UUID.randomUUID().toString());
        }
        if (preference instanceof RestrictedListPreference) {
            dialogFragment = RestrictedListPreference.RestrictedListPreferenceDialogFragment.newInstance(preference.getKey());
        } else if (preference instanceof CustomListPreference) {
            dialogFragment = CustomListPreference.CustomListPreferenceDialogFragment.newInstance(preference.getKey());
        } else if (preference instanceof CustomDialogPreferenceCompat) {
            dialogFragment = CustomDialogPreferenceCompat.CustomPreferenceDialogFragment.newInstance(preference.getKey());
        } else if (preference instanceof CustomEditTextPreferenceCompat) {
            dialogFragment = CustomEditTextPreferenceCompat.CustomPreferenceDialogFragment.newInstance(preference.getKey());
        } else {
            super.onDisplayPreferenceDialog(preference);
            return;
        }
        dialogFragment.setTargetFragment(this, 0);
        dialogFragment.show(getFragmentManager(), "dialog_preference");
        onDialogShowing();
    }

    public static class SettingsDialogFragment extends InstrumentedDialogFragment {
        private DialogInterface.OnCancelListener mOnCancelListener;
        /* access modifiers changed from: private */
        public DialogInterface.OnDismissListener mOnDismissListener;
        private Fragment mParentFragment;

        public static SettingsDialogFragment newInstance(DialogCreatable dialogCreatable, int i) {
            if (dialogCreatable instanceof Fragment) {
                SettingsDialogFragment settingsDialogFragment = new SettingsDialogFragment();
                settingsDialogFragment.setParentFragment(dialogCreatable);
                settingsDialogFragment.setDialogId(i);
                return settingsDialogFragment;
            }
            throw new IllegalArgumentException("fragment argument must be an instance of " + Fragment.class.getName());
        }

        public int getMetricsCategory() {
            Fragment fragment = this.mParentFragment;
            if (fragment == null) {
                return 0;
            }
            int dialogMetricsCategory = ((DialogCreatable) fragment).getDialogMetricsCategory(this.mDialogId);
            if (dialogMetricsCategory > 0) {
                return dialogMetricsCategory;
            }
            throw new IllegalStateException("Dialog must provide a metrics category");
        }

        public void onSaveInstanceState(Bundle bundle) {
            super.onSaveInstanceState(bundle);
            if (this.mParentFragment != null) {
                bundle.putInt("key_dialog_id", this.mDialogId);
                bundle.putInt("key_parent_fragment_id", this.mParentFragment.getId());
            }
        }

        public void onStart() {
            super.onStart();
            Fragment fragment = this.mParentFragment;
            if (fragment != null && (fragment instanceof SettingsPreferenceFragment)) {
                ((SettingsPreferenceFragment) fragment).onDialogShowing();
            }
        }

        public Dialog onCreateDialog(Bundle bundle) {
            Object obj;
            if (bundle != null) {
                this.mDialogId = bundle.getInt("key_dialog_id", 0);
                this.mParentFragment = getParentFragment();
                int i = bundle.getInt("key_parent_fragment_id", -1);
                if (this.mParentFragment == null) {
                    this.mParentFragment = getFragmentManager().findFragmentById(i);
                }
                Fragment fragment = this.mParentFragment;
                if (!(fragment instanceof DialogCreatable)) {
                    StringBuilder sb = new StringBuilder();
                    Fragment fragment2 = this.mParentFragment;
                    if (fragment2 != null) {
                        obj = fragment2.getClass().getName();
                    } else {
                        obj = Integer.valueOf(i);
                    }
                    sb.append(obj);
                    sb.append(" must implement ");
                    sb.append(DialogCreatable.class.getName());
                    throw new IllegalArgumentException(sb.toString());
                } else if (fragment instanceof SettingsPreferenceFragment) {
                    SettingsDialogFragment unused = ((SettingsPreferenceFragment) fragment).mDialogFragment = this;
                }
            }
            return ((DialogCreatable) this.mParentFragment).onCreateDialog(this.mDialogId);
        }

        public void onCancel(DialogInterface dialogInterface) {
            super.onCancel(dialogInterface);
            DialogInterface.OnCancelListener onCancelListener = this.mOnCancelListener;
            if (onCancelListener != null) {
                onCancelListener.onCancel(dialogInterface);
            }
        }

        public void onDismiss(DialogInterface dialogInterface) {
            super.onDismiss(dialogInterface);
            DialogInterface.OnDismissListener onDismissListener = this.mOnDismissListener;
            if (onDismissListener != null) {
                onDismissListener.onDismiss(dialogInterface);
            }
        }

        public int getDialogId() {
            return this.mDialogId;
        }

        public void onDetach() {
            super.onDetach();
            Fragment fragment = this.mParentFragment;
            if ((fragment instanceof SettingsPreferenceFragment) && ((SettingsPreferenceFragment) fragment).mDialogFragment == this) {
                SettingsDialogFragment unused = ((SettingsPreferenceFragment) this.mParentFragment).mDialogFragment = null;
            }
        }

        private void setParentFragment(DialogCreatable dialogCreatable) {
            this.mParentFragment = (Fragment) dialogCreatable;
        }

        private void setDialogId(int i) {
            this.mDialogId = i;
        }
    }

    /* access modifiers changed from: protected */
    public boolean hasNextButton() {
        return ((ButtonBarHandler) getActivity()).hasNextButton();
    }

    /* access modifiers changed from: protected */
    public Button getNextButton() {
        return ((ButtonBarHandler) getActivity()).getNextButton();
    }

    public void finish() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
            } else {
                activity.finish();
            }
        }
    }

    /* access modifiers changed from: protected */
    public Intent getIntent() {
        if (getActivity() == null) {
            return null;
        }
        return getActivity().getIntent();
    }

    /* access modifiers changed from: protected */
    public void setResult(int i) {
        if (getActivity() != null) {
            getActivity().setResult(i);
        }
    }
}
