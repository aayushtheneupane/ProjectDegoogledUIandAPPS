package com.google.android.material.shape;

import com.android.contacts.ContactPhotoManager;

public class CornerTreatment implements Cloneable {
    protected float cornerSize;

    public void getCornerPath(float f, float f2, ShapePath shapePath) {
    }

    public CornerTreatment() {
        this.cornerSize = ContactPhotoManager.OFFSET_DEFAULT;
    }

    protected CornerTreatment(float f) {
        this.cornerSize = f;
    }

    public float getCornerSize() {
        return this.cornerSize;
    }

    public void setCornerSize(float f) {
        this.cornerSize = f;
    }

    public CornerTreatment clone() {
        try {
            return (CornerTreatment) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
