package com.bumptech.glide.request.target;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class DrawableImageViewTarget extends ImageViewTarget<Drawable> {
    public DrawableImageViewTarget(ImageView imageView) {
        super(imageView);
    }

    /* access modifiers changed from: protected */
    public void setResource(Object obj) {
        ((ImageView) this.view).setImageDrawable((Drawable) obj);
    }
}
