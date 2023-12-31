package com.bumptech.glide.load.model.stream;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import java.io.InputStream;
import java.net.URL;

public class UrlLoader implements ModelLoader<URL, InputStream> {
    private final ModelLoader<GlideUrl, InputStream> glideUrlLoader;

    public static class StreamFactory implements ModelLoaderFactory<URL, InputStream> {
        public ModelLoader<URL, InputStream> build(MultiModelLoaderFactory multiModelLoaderFactory) {
            return new UrlLoader(multiModelLoaderFactory.build(GlideUrl.class, InputStream.class));
        }
    }

    public UrlLoader(ModelLoader<GlideUrl, InputStream> modelLoader) {
        this.glideUrlLoader = modelLoader;
    }

    public ModelLoader.LoadData buildLoadData(Object obj, int i, int i2, Options options) {
        return this.glideUrlLoader.buildLoadData(new GlideUrl((URL) obj), i, i2, options);
    }

    public boolean handles(Object obj) {
        URL url = (URL) obj;
        return true;
    }
}
