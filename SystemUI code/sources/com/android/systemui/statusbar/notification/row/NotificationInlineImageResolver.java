package com.android.systemui.statusbar.notification.row;

import android.app.ActivityManager;
import android.app.Notification;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import com.android.internal.widget.ImageResolver;
import com.android.internal.widget.LocalImageResolver;
import com.android.internal.widget.MessagingMessage;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class NotificationInlineImageResolver implements ImageResolver {
    private static final String TAG = "NotificationInlineImageResolver";
    private final Context mContext;
    private final ImageCache mImageCache;
    private Set<Uri> mWantedUriSet;

    interface ImageCache {
        Drawable get(Uri uri);

        boolean hasEntry(Uri uri);

        void preload(Uri uri);

        void purge();

        void setImageResolver(NotificationInlineImageResolver notificationInlineImageResolver);
    }

    public NotificationInlineImageResolver(Context context, ImageCache imageCache) {
        this.mContext = context.getApplicationContext();
        this.mImageCache = imageCache;
        ImageCache imageCache2 = this.mImageCache;
        if (imageCache2 != null) {
            imageCache2.setImageResolver(this);
        }
    }

    public boolean hasCache() {
        return this.mImageCache != null && !ActivityManager.isLowRamDeviceStatic();
    }

    /* access modifiers changed from: package-private */
    public Drawable resolveImage(Uri uri) throws IOException {
        return LocalImageResolver.resolveImage(uri, this.mContext);
    }

    public Drawable loadImage(Uri uri) {
        try {
            return hasCache() ? this.mImageCache.get(uri) : resolveImage(uri);
        } catch (IOException | SecurityException e) {
            String str = TAG;
            Log.d(str, "loadImage: Can't load image from " + uri, e);
            return null;
        }
    }

    public void preloadImages(Notification notification) {
        if (hasCache()) {
            retrieveWantedUriSet(notification);
            getWantedUriSet().forEach(new Consumer() {
                public final void accept(Object obj) {
                    NotificationInlineImageResolver.this.lambda$preloadImages$0$NotificationInlineImageResolver((Uri) obj);
                }
            });
        }
    }

    public /* synthetic */ void lambda$preloadImages$0$NotificationInlineImageResolver(Uri uri) {
        if (!this.mImageCache.hasEntry(uri)) {
            this.mImageCache.preload(uri);
        }
    }

    public void purgeCache() {
        if (hasCache()) {
            this.mImageCache.purge();
        }
    }

    private void retrieveWantedUriSet(Notification notification) {
        List<Notification.MessagingStyle.Message> list;
        HashSet hashSet = new HashSet();
        Bundle bundle = notification.extras;
        if (bundle != null) {
            Parcelable[] parcelableArray = bundle.getParcelableArray("android.messages");
            List<Notification.MessagingStyle.Message> list2 = null;
            if (parcelableArray == null) {
                list = null;
            } else {
                list = Notification.MessagingStyle.Message.getMessagesFromBundleArray(parcelableArray);
            }
            if (list != null) {
                for (Notification.MessagingStyle.Message message : list) {
                    if (MessagingMessage.hasImage(message)) {
                        hashSet.add(message.getDataUri());
                    }
                }
            }
            Parcelable[] parcelableArray2 = bundle.getParcelableArray("android.messages.historic");
            if (parcelableArray2 != null) {
                list2 = Notification.MessagingStyle.Message.getMessagesFromBundleArray(parcelableArray2);
            }
            if (list2 != null) {
                for (Notification.MessagingStyle.Message message2 : list2) {
                    if (MessagingMessage.hasImage(message2)) {
                        hashSet.add(message2.getDataUri());
                    }
                }
            }
            this.mWantedUriSet = hashSet;
        }
    }

    /* access modifiers changed from: package-private */
    public Set<Uri> getWantedUriSet() {
        return this.mWantedUriSet;
    }
}
