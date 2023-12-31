package com.bumptech.glide.load.model;

import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FileLoader<Data> implements ModelLoader<File, Data> {
    private final FileOpener<Data> fileOpener;

    public static class Factory<Data> implements ModelLoaderFactory<File, Data> {
        private final FileOpener<Data> opener;

        public Factory(FileOpener<Data> fileOpener) {
            this.opener = fileOpener;
        }

        public final ModelLoader<File, Data> build(MultiModelLoaderFactory multiModelLoaderFactory) {
            return new FileLoader(this.opener);
        }
    }

    public static class FileDescriptorFactory extends Factory<ParcelFileDescriptor> {
        public FileDescriptorFactory() {
            super(new FileOpener<ParcelFileDescriptor>() {
                public void close(Object obj) throws IOException {
                    ((ParcelFileDescriptor) obj).close();
                }

                public Class<ParcelFileDescriptor> getDataClass() {
                    return ParcelFileDescriptor.class;
                }

                public Object open(File file) throws FileNotFoundException {
                    return ParcelFileDescriptor.open(file, 268435456);
                }
            });
        }
    }

    private static final class FileFetcher<Data> implements DataFetcher<Data> {
        private Data data;
        private final File file;
        private final FileOpener<Data> opener;

        FileFetcher(File file2, FileOpener<Data> fileOpener) {
            this.file = file2;
            this.opener = fileOpener;
        }

        public void cancel() {
        }

        public void cleanup() {
            Data data2 = this.data;
            if (data2 != null) {
                try {
                    this.opener.close(data2);
                } catch (IOException unused) {
                }
            }
        }

        public Class<Data> getDataClass() {
            return this.opener.getDataClass();
        }

        public DataSource getDataSource() {
            return DataSource.LOCAL;
        }

        public void loadData(Priority priority, DataFetcher.DataCallback<? super Data> dataCallback) {
            try {
                this.data = this.opener.open(this.file);
                dataCallback.onDataReady(this.data);
            } catch (FileNotFoundException e) {
                if (Log.isLoggable("FileLoader", 3)) {
                    Log.d("FileLoader", "Failed to open file", e);
                }
                dataCallback.onLoadFailed(e);
            }
        }
    }

    public interface FileOpener<Data> {
        void close(Data data) throws IOException;

        Class<Data> getDataClass();

        Data open(File file) throws FileNotFoundException;
    }

    public static class StreamFactory extends Factory<InputStream> {
        public StreamFactory() {
            super(new FileOpener<InputStream>() {
                public void close(Object obj) throws IOException {
                    ((InputStream) obj).close();
                }

                public Class<InputStream> getDataClass() {
                    return InputStream.class;
                }

                public Object open(File file) throws FileNotFoundException {
                    return new FileInputStream(file);
                }
            });
        }
    }

    public FileLoader(FileOpener<Data> fileOpener2) {
        this.fileOpener = fileOpener2;
    }

    public ModelLoader.LoadData buildLoadData(Object obj, int i, int i2, Options options) {
        File file = (File) obj;
        return new ModelLoader.LoadData(new ObjectKey(file), new FileFetcher(file, this.fileOpener));
    }

    public boolean handles(Object obj) {
        File file = (File) obj;
        return true;
    }
}
