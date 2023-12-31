package com.android.systemui.fragments;

import android.app.Fragment;
import android.app.FragmentController;
import android.app.FragmentHostCallback;
import android.app.FragmentManager;
import android.app.FragmentManagerNonConfig;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import com.android.settingslib.applications.InterestingConfigChanges;
import com.android.systemui.Dependency;
import com.android.systemui.fragments.FragmentHostManager;
import com.android.systemui.plugins.Plugin;
import com.android.systemui.util.leak.LeakDetector;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

public class FragmentHostManager {
    private final InterestingConfigChanges mConfigChanges = new InterestingConfigChanges(-1073741052);
    /* access modifiers changed from: private */
    public final Context mContext;
    private FragmentController mFragments;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler(Looper.getMainLooper());
    private FragmentManager.FragmentLifecycleCallbacks mLifecycleCallbacks;
    private final HashMap<String, ArrayList<FragmentListener>> mListeners = new HashMap<>();
    /* access modifiers changed from: private */
    public final FragmentService mManager;
    /* access modifiers changed from: private */
    public final ExtensionFragmentManager mPlugins = new ExtensionFragmentManager();
    private final View mRootView;

    public interface FragmentListener {
        void onFragmentViewCreated(String str, Fragment fragment);

        void onFragmentViewDestroyed(String str, Fragment fragment) {
        }
    }

    /* access modifiers changed from: private */
    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
    }

    FragmentHostManager(FragmentService fragmentService, View view) {
        this.mContext = view.getContext();
        this.mManager = fragmentService;
        this.mRootView = view;
        this.mConfigChanges.applyNewConfig(this.mContext.getResources());
        createFragmentHost((Parcelable) null);
    }

    private void createFragmentHost(Parcelable parcelable) {
        this.mFragments = FragmentController.createController(new HostCallbacks());
        this.mFragments.attachHost((Fragment) null);
        this.mLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
            public void onFragmentViewCreated(FragmentManager fragmentManager, Fragment fragment, View view, Bundle bundle) {
                FragmentHostManager.this.onFragmentViewCreated(fragment);
            }

            public void onFragmentViewDestroyed(FragmentManager fragmentManager, Fragment fragment) {
                FragmentHostManager.this.onFragmentViewDestroyed(fragment);
            }

            public void onFragmentDestroyed(FragmentManager fragmentManager, Fragment fragment) {
                ((LeakDetector) Dependency.get(LeakDetector.class)).trackGarbage(fragment);
            }
        };
        this.mFragments.getFragmentManager().registerFragmentLifecycleCallbacks(this.mLifecycleCallbacks, true);
        if (parcelable != null) {
            this.mFragments.restoreAllState(parcelable, (FragmentManagerNonConfig) null);
        }
        this.mFragments.dispatchCreate();
        this.mFragments.dispatchStart();
        this.mFragments.dispatchResume();
    }

    private Parcelable destroyFragmentHost() {
        this.mFragments.dispatchPause();
        Parcelable saveAllState = this.mFragments.saveAllState();
        this.mFragments.dispatchStop();
        this.mFragments.dispatchDestroy();
        this.mFragments.getFragmentManager().unregisterFragmentLifecycleCallbacks(this.mLifecycleCallbacks);
        return saveAllState;
    }

    public FragmentHostManager addTagListener(String str, FragmentListener fragmentListener) {
        ArrayList arrayList = this.mListeners.get(str);
        if (arrayList == null) {
            arrayList = new ArrayList();
            this.mListeners.put(str, arrayList);
        }
        arrayList.add(fragmentListener);
        Fragment findFragmentByTag = getFragmentManager().findFragmentByTag(str);
        if (!(findFragmentByTag == null || findFragmentByTag.getView() == null)) {
            fragmentListener.onFragmentViewCreated(str, findFragmentByTag);
        }
        return this;
    }

    public void removeTagListener(String str, FragmentListener fragmentListener) {
        ArrayList arrayList = this.mListeners.get(str);
        if (arrayList != null && arrayList.remove(fragmentListener) && arrayList.size() == 0) {
            this.mListeners.remove(str);
        }
    }

    /* access modifiers changed from: private */
    public void onFragmentViewCreated(Fragment fragment) {
        String tag = fragment.getTag();
        ArrayList arrayList = this.mListeners.get(tag);
        if (arrayList != null) {
            arrayList.forEach(new Consumer(tag, fragment) {
                private final /* synthetic */ String f$0;
                private final /* synthetic */ Fragment f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final void accept(Object obj) {
                    ((FragmentHostManager.FragmentListener) obj).onFragmentViewCreated(this.f$0, this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void onFragmentViewDestroyed(Fragment fragment) {
        String tag = fragment.getTag();
        ArrayList arrayList = this.mListeners.get(tag);
        if (arrayList != null) {
            arrayList.forEach(new Consumer(tag, fragment) {
                private final /* synthetic */ String f$0;
                private final /* synthetic */ Fragment f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final void accept(Object obj) {
                    ((FragmentHostManager.FragmentListener) obj).onFragmentViewDestroyed(this.f$0, this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        if (this.mConfigChanges.applyNewConfig(this.mContext.getResources())) {
            reloadFragments();
        } else {
            this.mFragments.dispatchConfigurationChanged(configuration);
        }
    }

    /* access modifiers changed from: private */
    public <T extends View> T findViewById(int i) {
        return this.mRootView.findViewById(i);
    }

    public FragmentManager getFragmentManager() {
        return this.mFragments.getFragmentManager();
    }

    /* access modifiers changed from: package-private */
    public ExtensionFragmentManager getExtensionManager() {
        return this.mPlugins;
    }

    /* access modifiers changed from: package-private */
    public void destroy() {
        this.mFragments.dispatchDestroy();
    }

    public <T> T create(Class<T> cls) {
        return this.mPlugins.instantiate(this.mContext, cls.getName(), (Bundle) null);
    }

    public static FragmentHostManager get(View view) {
        try {
            return ((FragmentService) Dependency.get(FragmentService.class)).getFragmentHostManager(view);
        } catch (ClassCastException e) {
            throw e;
        }
    }

    public static void removeAndDestroy(View view) {
        ((FragmentService) Dependency.get(FragmentService.class)).removeAndDestroy(view);
    }

    public void reloadFragments() {
        createFragmentHost(destroyFragmentHost());
    }

    class HostCallbacks extends FragmentHostCallback<FragmentHostManager> {
        public void onAttachFragment(Fragment fragment) {
        }

        public int onGetWindowAnimations() {
            return 0;
        }

        public boolean onHasView() {
            return true;
        }

        public boolean onHasWindowAnimations() {
            return false;
        }

        public boolean onShouldSaveFragmentState(Fragment fragment) {
            return true;
        }

        public boolean onUseFragmentManagerInflaterFactory() {
            return true;
        }

        public HostCallbacks() {
            super(FragmentHostManager.this.mContext, FragmentHostManager.this.mHandler, 0);
        }

        public FragmentHostManager onGetHost() {
            return FragmentHostManager.this;
        }

        public void onDump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
            FragmentHostManager.this.dump(str, fileDescriptor, printWriter, strArr);
        }

        public Fragment instantiate(Context context, String str, Bundle bundle) {
            return FragmentHostManager.this.mPlugins.instantiate(context, str, bundle);
        }

        public LayoutInflater onGetLayoutInflater() {
            return LayoutInflater.from(FragmentHostManager.this.mContext);
        }

        public <T extends View> T onFindViewById(int i) {
            return FragmentHostManager.this.findViewById(i);
        }
    }

    class ExtensionFragmentManager {
        private final ArrayMap<String, Context> mExtensionLookup = new ArrayMap<>();

        ExtensionFragmentManager() {
        }

        public void setCurrentExtension(int i, String str, String str2, String str3, Context context) {
            if (str2 != null) {
                this.mExtensionLookup.remove(str2);
            }
            this.mExtensionLookup.put(str3, context);
            FragmentHostManager.this.getFragmentManager().beginTransaction().replace(i, instantiate(context, str3, (Bundle) null), str).commit();
            FragmentHostManager.this.reloadFragments();
        }

        /* access modifiers changed from: package-private */
        public Fragment instantiate(Context context, String str, Bundle bundle) {
            Context context2 = this.mExtensionLookup.get(str);
            if (context2 == null) {
                return instantiateWithInjections(context, str, bundle);
            }
            Fragment instantiateWithInjections = instantiateWithInjections(context2, str, bundle);
            if (instantiateWithInjections instanceof Plugin) {
                ((Plugin) instantiateWithInjections).onCreate(FragmentHostManager.this.mContext, context2);
            }
            return instantiateWithInjections;
        }

        private Fragment instantiateWithInjections(Context context, String str, Bundle bundle) {
            Method method = FragmentHostManager.this.mManager.getInjectionMap().get(str);
            if (method == null) {
                return Fragment.instantiate(context, str, bundle);
            }
            try {
                Fragment fragment = (Fragment) method.invoke(FragmentHostManager.this.mManager.getFragmentCreator(), new Object[0]);
                if (bundle != null) {
                    bundle.setClassLoader(fragment.getClass().getClassLoader());
                    fragment.setArguments(bundle);
                }
                return fragment;
            } catch (IllegalAccessException e) {
                throw new Fragment.InstantiationException("Unable to instantiate " + str, e);
            } catch (InvocationTargetException e2) {
                throw new Fragment.InstantiationException("Unable to instantiate " + str, e2);
            }
        }
    }
}
