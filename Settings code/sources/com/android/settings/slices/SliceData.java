package com.android.settings.slices;

import android.net.Uri;
import android.text.TextUtils;

public class SliceData {
    private final String mFragmentClassName;
    private final int mIconResource;
    private final boolean mIsPlatformDefined;
    private final String mKey;
    private final String mKeywords;
    private final String mPreferenceController;
    private final CharSequence mScreenTitle;
    private final int mSliceType;
    private final String mSummary;
    private final String mTitle;
    private final String mUnavailableSliceSubtitle;
    private final Uri mUri;

    public String getKey() {
        return this.mKey;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public String getSummary() {
        return this.mSummary;
    }

    public CharSequence getScreenTitle() {
        return this.mScreenTitle;
    }

    public String getKeywords() {
        return this.mKeywords;
    }

    public int getIconResource() {
        return this.mIconResource;
    }

    public String getFragmentClassName() {
        return this.mFragmentClassName;
    }

    public Uri getUri() {
        return this.mUri;
    }

    public String getPreferenceController() {
        return this.mPreferenceController;
    }

    public int getSliceType() {
        return this.mSliceType;
    }

    public boolean isPlatformDefined() {
        return this.mIsPlatformDefined;
    }

    public String getUnavailableSliceSubtitle() {
        return this.mUnavailableSliceSubtitle;
    }

    private SliceData(Builder builder) {
        this.mKey = builder.mKey;
        this.mTitle = builder.mTitle;
        this.mSummary = builder.mSummary;
        this.mScreenTitle = builder.mScreenTitle;
        this.mKeywords = builder.mKeywords;
        this.mIconResource = builder.mIconResource;
        this.mFragmentClassName = builder.mFragmentClassName;
        this.mUri = builder.mUri;
        this.mPreferenceController = builder.mPrefControllerClassName;
        this.mSliceType = builder.mSliceType;
        this.mIsPlatformDefined = builder.mIsPlatformDefined;
        this.mUnavailableSliceSubtitle = builder.mUnavailableSliceSubtitle;
    }

    public int hashCode() {
        return this.mKey.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SliceData)) {
            return false;
        }
        return TextUtils.equals(this.mKey, ((SliceData) obj).mKey);
    }

    static class Builder {
        /* access modifiers changed from: private */
        public String mFragmentClassName;
        /* access modifiers changed from: private */
        public int mIconResource;
        /* access modifiers changed from: private */
        public boolean mIsPlatformDefined;
        /* access modifiers changed from: private */
        public String mKey;
        /* access modifiers changed from: private */
        public String mKeywords;
        /* access modifiers changed from: private */
        public String mPrefControllerClassName;
        /* access modifiers changed from: private */
        public CharSequence mScreenTitle;
        /* access modifiers changed from: private */
        public int mSliceType;
        /* access modifiers changed from: private */
        public String mSummary;
        /* access modifiers changed from: private */
        public String mTitle;
        /* access modifiers changed from: private */
        public String mUnavailableSliceSubtitle;
        /* access modifiers changed from: private */
        public Uri mUri;

        Builder() {
        }

        public Builder setKey(String str) {
            this.mKey = str;
            return this;
        }

        public Builder setTitle(String str) {
            this.mTitle = str;
            return this;
        }

        public Builder setSummary(String str) {
            this.mSummary = str;
            return this;
        }

        public Builder setScreenTitle(CharSequence charSequence) {
            this.mScreenTitle = charSequence;
            return this;
        }

        public Builder setKeywords(String str) {
            this.mKeywords = str;
            return this;
        }

        public Builder setIcon(int i) {
            this.mIconResource = i;
            return this;
        }

        public Builder setPreferenceControllerClassName(String str) {
            this.mPrefControllerClassName = str;
            return this;
        }

        public Builder setFragmentName(String str) {
            this.mFragmentClassName = str;
            return this;
        }

        public Builder setUri(Uri uri) {
            this.mUri = uri;
            return this;
        }

        public Builder setSliceType(int i) {
            this.mSliceType = i;
            return this;
        }

        public Builder setPlatformDefined(boolean z) {
            this.mIsPlatformDefined = z;
            return this;
        }

        public Builder setUnavailableSliceSubtitle(String str) {
            this.mUnavailableSliceSubtitle = str;
            return this;
        }

        public SliceData build() {
            if (TextUtils.isEmpty(this.mKey)) {
                throw new InvalidSliceDataException("Key cannot be empty");
            } else if (TextUtils.isEmpty(this.mTitle)) {
                throw new InvalidSliceDataException("Title cannot be empty");
            } else if (TextUtils.isEmpty(this.mFragmentClassName)) {
                throw new InvalidSliceDataException("Fragment Name cannot be empty");
            } else if (!TextUtils.isEmpty(this.mPrefControllerClassName)) {
                return new SliceData(this);
            } else {
                throw new InvalidSliceDataException("Preference Controller cannot be empty");
            }
        }
    }

    public static class InvalidSliceDataException extends RuntimeException {
        public InvalidSliceDataException(String str) {
            super(str);
        }
    }
}
