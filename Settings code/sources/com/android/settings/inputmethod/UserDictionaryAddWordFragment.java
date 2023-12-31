package com.android.settings.inputmethod;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.android.settings.core.InstrumentedFragment;
import com.havoc.config.center.C1715R;

public class UserDictionaryAddWordFragment extends InstrumentedFragment {
    private UserDictionaryAddWordContents mContents;
    private boolean mIsDeleting = false;
    private View mRootView;

    public int getMetricsCategory() {
        return 62;
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.mRootView = layoutInflater.inflate(C1715R.layout.user_dictionary_add_word_fullscreen, (ViewGroup) null);
        this.mIsDeleting = false;
        UserDictionaryAddWordContents userDictionaryAddWordContents = this.mContents;
        if (userDictionaryAddWordContents == null) {
            this.mContents = new UserDictionaryAddWordContents(this.mRootView, getArguments());
        } else {
            this.mContents = new UserDictionaryAddWordContents(this.mRootView, userDictionaryAddWordContents);
        }
        getActivity().getActionBar().setSubtitle(UserDictionarySettingsUtils.getLocaleDisplayName(getActivity(), this.mContents.getCurrentUserDictionaryLocale()));
        return this.mRootView;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menu.add(0, 1, 0, C1715R.string.delete).setIcon(C1715R.C1717drawable.ic_delete).setShowAsAction(5);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 1) {
            return false;
        }
        this.mContents.delete(getActivity());
        this.mIsDeleting = true;
        getActivity().onBackPressed();
        return true;
    }

    public void onResume() {
        super.onResume();
        updateSpinner();
    }

    private void updateSpinner() {
        new ArrayAdapter(getActivity(), 17367048, this.mContents.getLocalesList(getActivity())).setDropDownViewResource(17367049);
    }

    public void onPause() {
        super.onPause();
        if (!this.mIsDeleting) {
            this.mContents.apply(getActivity(), (Bundle) null);
        }
    }
}
