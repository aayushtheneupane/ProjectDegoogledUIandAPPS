package com.google.android.setupcompat.internal;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import com.google.android.setupcompat.ISetupCompatService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class SetupCompatServiceProvider {
    static final Intent COMPAT_SERVICE_INTENT = new Intent().setPackage("com.google.android.setupwizard").setAction("com.google.android.setupcompat.SetupCompatService.BIND");
    static boolean disableLooperCheckForTesting = false;
    @SuppressLint({"StaticFieldLeak"})
    private static volatile SetupCompatServiceProvider instance;
    private final AtomicReference<CountDownLatch> connectedConditionRef = new AtomicReference<>();
    private final Context context;
    final ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            State state = State.CONNECTED;
            if (iBinder == null) {
                state = State.DISCONNECTED;
                Log.w("SucServiceProvider", "Binder is null when onServiceConnected was called!");
            }
            SetupCompatServiceProvider.this.swapServiceContextAndNotify(new ServiceContext(state, ISetupCompatService.Stub.asInterface(iBinder)));
        }

        public void onServiceDisconnected(ComponentName componentName) {
            SetupCompatServiceProvider.this.swapServiceContextAndNotify(new ServiceContext(State.DISCONNECTED));
        }

        public void onBindingDied(ComponentName componentName) {
            SetupCompatServiceProvider.this.swapServiceContextAndNotify(new ServiceContext(State.REBIND_REQUIRED));
        }

        public void onNullBinding(ComponentName componentName) {
            SetupCompatServiceProvider.this.swapServiceContextAndNotify(new ServiceContext(State.SERVICE_NOT_USABLE));
        }
    };
    private volatile ServiceContext serviceContext = new ServiceContext(State.NOT_STARTED);

    enum State {
        NOT_STARTED,
        BIND_FAILED,
        BINDING,
        CONNECTED,
        DISCONNECTED,
        SERVICE_NOT_USABLE,
        REBIND_REQUIRED
    }

    public static ISetupCompatService get(Context context2, long j, TimeUnit timeUnit) throws TimeoutException, InterruptedException {
        return getInstance(context2).getService(j, timeUnit);
    }

    public ISetupCompatService getService(long j, TimeUnit timeUnit) throws TimeoutException, InterruptedException {
        Preconditions.checkState(disableLooperCheckForTesting || Looper.getMainLooper() != Looper.myLooper(), "getService blocks and should not be called from the main thread.");
        ServiceContext currentServiceState = getCurrentServiceState();
        switch (C15722.f71x90ac3479[currentServiceState.state.ordinal()]) {
            case 1:
                return currentServiceState.compatService;
            case 2:
            case 3:
                return null;
            case 4:
            case 5:
                return waitForConnection(j, timeUnit);
            case 6:
                requestServiceBind();
                return waitForConnection(j, timeUnit);
            case 7:
                throw new IllegalStateException("NOT_STARTED state only possible before instance is created.");
            default:
                throw new IllegalStateException("Unknown state = " + currentServiceState.state);
        }
    }

    /* renamed from: com.google.android.setupcompat.internal.SetupCompatServiceProvider$2 */
    static /* synthetic */ class C15722 {

        /* renamed from: $SwitchMap$com$google$android$setupcompat$internal$SetupCompatServiceProvider$State */
        static final /* synthetic */ int[] f71x90ac3479 = new int[State.values().length];

        /* JADX WARNING: Can't wrap try/catch for region: R(14:0|1|2|3|4|5|6|7|8|9|10|11|12|(3:13|14|16)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(16:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|16) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x0040 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x004b */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0014 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001f */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x002a */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0035 */
        static {
            /*
                com.google.android.setupcompat.internal.SetupCompatServiceProvider$State[] r0 = com.google.android.setupcompat.internal.SetupCompatServiceProvider.State.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f71x90ac3479 = r0
                int[] r0 = f71x90ac3479     // Catch:{ NoSuchFieldError -> 0x0014 }
                com.google.android.setupcompat.internal.SetupCompatServiceProvider$State r1 = com.google.android.setupcompat.internal.SetupCompatServiceProvider.State.CONNECTED     // Catch:{ NoSuchFieldError -> 0x0014 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0014 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0014 }
            L_0x0014:
                int[] r0 = f71x90ac3479     // Catch:{ NoSuchFieldError -> 0x001f }
                com.google.android.setupcompat.internal.SetupCompatServiceProvider$State r1 = com.google.android.setupcompat.internal.SetupCompatServiceProvider.State.SERVICE_NOT_USABLE     // Catch:{ NoSuchFieldError -> 0x001f }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001f }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001f }
            L_0x001f:
                int[] r0 = f71x90ac3479     // Catch:{ NoSuchFieldError -> 0x002a }
                com.google.android.setupcompat.internal.SetupCompatServiceProvider$State r1 = com.google.android.setupcompat.internal.SetupCompatServiceProvider.State.BIND_FAILED     // Catch:{ NoSuchFieldError -> 0x002a }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x002a }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x002a }
            L_0x002a:
                int[] r0 = f71x90ac3479     // Catch:{ NoSuchFieldError -> 0x0035 }
                com.google.android.setupcompat.internal.SetupCompatServiceProvider$State r1 = com.google.android.setupcompat.internal.SetupCompatServiceProvider.State.DISCONNECTED     // Catch:{ NoSuchFieldError -> 0x0035 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0035 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0035 }
            L_0x0035:
                int[] r0 = f71x90ac3479     // Catch:{ NoSuchFieldError -> 0x0040 }
                com.google.android.setupcompat.internal.SetupCompatServiceProvider$State r1 = com.google.android.setupcompat.internal.SetupCompatServiceProvider.State.BINDING     // Catch:{ NoSuchFieldError -> 0x0040 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0040 }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0040 }
            L_0x0040:
                int[] r0 = f71x90ac3479     // Catch:{ NoSuchFieldError -> 0x004b }
                com.google.android.setupcompat.internal.SetupCompatServiceProvider$State r1 = com.google.android.setupcompat.internal.SetupCompatServiceProvider.State.REBIND_REQUIRED     // Catch:{ NoSuchFieldError -> 0x004b }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x004b }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x004b }
            L_0x004b:
                int[] r0 = f71x90ac3479     // Catch:{ NoSuchFieldError -> 0x0056 }
                com.google.android.setupcompat.internal.SetupCompatServiceProvider$State r1 = com.google.android.setupcompat.internal.SetupCompatServiceProvider.State.NOT_STARTED     // Catch:{ NoSuchFieldError -> 0x0056 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0056 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0056 }
            L_0x0056:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.android.setupcompat.internal.SetupCompatServiceProvider.C15722.<clinit>():void");
        }
    }

    private ISetupCompatService waitForConnection(long j, TimeUnit timeUnit) throws TimeoutException, InterruptedException {
        ServiceContext currentServiceState = getCurrentServiceState();
        if (currentServiceState.state == State.CONNECTED) {
            return currentServiceState.compatService;
        }
        CountDownLatch connectedCondition = getConnectedCondition();
        Log.i("SucServiceProvider", "Waiting for service to get connected");
        if (connectedCondition.await(j, timeUnit)) {
            ServiceContext currentServiceState2 = getCurrentServiceState();
            if (Log.isLoggable("SucServiceProvider", 4)) {
                Log.i("SucServiceProvider", String.format("Finished waiting for service to get connected. Current state = %s", new Object[]{currentServiceState2.state}));
            }
            return currentServiceState2.compatService;
        }
        requestServiceBind();
        throw new TimeoutException(String.format("Failed to acquire connection after [%s %s]", new Object[]{Long.valueOf(j), timeUnit}));
    }

    /* access modifiers changed from: protected */
    public CountDownLatch createCountDownLatch() {
        return new CountDownLatch(1);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x006c, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void requestServiceBind() {
        /*
            r4 = this;
            monitor-enter(r4)
            com.google.android.setupcompat.internal.SetupCompatServiceProvider$ServiceContext r0 = r4.getCurrentServiceState()     // Catch:{ all -> 0x006d }
            com.google.android.setupcompat.internal.SetupCompatServiceProvider$State r1 = r0.state     // Catch:{ all -> 0x006d }
            com.google.android.setupcompat.internal.SetupCompatServiceProvider$State r2 = com.google.android.setupcompat.internal.SetupCompatServiceProvider.State.CONNECTED     // Catch:{ all -> 0x006d }
            if (r1 != r2) goto L_0x0014
            java.lang.String r0 = "SucServiceProvider"
            java.lang.String r1 = "Refusing to rebind since current state is already connected"
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x006d }
            monitor-exit(r4)
            return
        L_0x0014:
            com.google.android.setupcompat.internal.SetupCompatServiceProvider$State r0 = r0.state     // Catch:{ all -> 0x006d }
            com.google.android.setupcompat.internal.SetupCompatServiceProvider$State r1 = com.google.android.setupcompat.internal.SetupCompatServiceProvider.State.NOT_STARTED     // Catch:{ all -> 0x006d }
            if (r0 == r1) goto L_0x0028
            java.lang.String r0 = "SucServiceProvider"
            java.lang.String r1 = "Unbinding existing service connection."
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x006d }
            android.content.Context r0 = r4.context     // Catch:{ all -> 0x006d }
            android.content.ServiceConnection r1 = r4.serviceConnection     // Catch:{ all -> 0x006d }
            r0.unbindService(r1)     // Catch:{ all -> 0x006d }
        L_0x0028:
            android.content.Context r0 = r4.context     // Catch:{ SecurityException -> 0x0034 }
            android.content.Intent r1 = COMPAT_SERVICE_INTENT     // Catch:{ SecurityException -> 0x0034 }
            android.content.ServiceConnection r2 = r4.serviceConnection     // Catch:{ SecurityException -> 0x0034 }
            r3 = 1
            boolean r0 = r0.bindService(r1, r2, r3)     // Catch:{ SecurityException -> 0x0034 }
            goto L_0x003d
        L_0x0034:
            r0 = move-exception
            java.lang.String r1 = "SucServiceProvider"
            java.lang.String r2 = "Unable to bind to compat service"
            android.util.Log.e(r1, r2, r0)     // Catch:{ all -> 0x006d }
            r0 = 0
        L_0x003d:
            r1 = 0
            if (r0 == 0) goto L_0x005a
            com.google.android.setupcompat.internal.SetupCompatServiceProvider$State r0 = r4.getCurrentState()     // Catch:{ all -> 0x006d }
            com.google.android.setupcompat.internal.SetupCompatServiceProvider$State r2 = com.google.android.setupcompat.internal.SetupCompatServiceProvider.State.CONNECTED     // Catch:{ all -> 0x006d }
            if (r0 == r2) goto L_0x006b
            com.google.android.setupcompat.internal.SetupCompatServiceProvider$ServiceContext r0 = new com.google.android.setupcompat.internal.SetupCompatServiceProvider$ServiceContext     // Catch:{ all -> 0x006d }
            com.google.android.setupcompat.internal.SetupCompatServiceProvider$State r2 = com.google.android.setupcompat.internal.SetupCompatServiceProvider.State.BINDING     // Catch:{ all -> 0x006d }
            r0.<init>((com.google.android.setupcompat.internal.SetupCompatServiceProvider.State) r2)     // Catch:{ all -> 0x006d }
            r4.swapServiceContextAndNotify(r0)     // Catch:{ all -> 0x006d }
            java.lang.String r0 = "SucServiceProvider"
            java.lang.String r1 = "Context#bindService went through, now waiting for service connection"
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x006d }
            goto L_0x006b
        L_0x005a:
            com.google.android.setupcompat.internal.SetupCompatServiceProvider$ServiceContext r0 = new com.google.android.setupcompat.internal.SetupCompatServiceProvider$ServiceContext     // Catch:{ all -> 0x006d }
            com.google.android.setupcompat.internal.SetupCompatServiceProvider$State r2 = com.google.android.setupcompat.internal.SetupCompatServiceProvider.State.BIND_FAILED     // Catch:{ all -> 0x006d }
            r0.<init>((com.google.android.setupcompat.internal.SetupCompatServiceProvider.State) r2)     // Catch:{ all -> 0x006d }
            r4.swapServiceContextAndNotify(r0)     // Catch:{ all -> 0x006d }
            java.lang.String r0 = "SucServiceProvider"
            java.lang.String r1 = "Context#bindService did not succeed."
            android.util.Log.e(r0, r1)     // Catch:{ all -> 0x006d }
        L_0x006b:
            monitor-exit(r4)
            return
        L_0x006d:
            r0 = move-exception
            monitor-exit(r4)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.setupcompat.internal.SetupCompatServiceProvider.requestServiceBind():void");
    }

    /* access modifiers changed from: package-private */
    public State getCurrentState() {
        return this.serviceContext.state;
    }

    private ServiceContext getCurrentServiceState() {
        return this.serviceContext;
    }

    /* access modifiers changed from: private */
    public void swapServiceContextAndNotify(ServiceContext serviceContext2) {
        if (Log.isLoggable("SucServiceProvider", 4)) {
            Log.i("SucServiceProvider", String.format("State changed: %s -> %s", new Object[]{this.serviceContext.state, serviceContext2.state}));
        }
        this.serviceContext = serviceContext2;
        CountDownLatch andClearConnectedCondition = getAndClearConnectedCondition();
        if (andClearConnectedCondition != null) {
            andClearConnectedCondition.countDown();
        }
    }

    private CountDownLatch getAndClearConnectedCondition() {
        return this.connectedConditionRef.getAndSet((Object) null);
    }

    private CountDownLatch getConnectedCondition() {
        CountDownLatch createCountDownLatch;
        do {
            CountDownLatch countDownLatch = this.connectedConditionRef.get();
            if (countDownLatch != null) {
                return countDownLatch;
            }
            createCountDownLatch = createCountDownLatch();
        } while (!this.connectedConditionRef.compareAndSet((Object) null, createCountDownLatch));
        return createCountDownLatch;
    }

    SetupCompatServiceProvider(Context context2) {
        this.context = context2.getApplicationContext();
    }

    private static final class ServiceContext {
        final ISetupCompatService compatService;
        final State state;

        private ServiceContext(State state2, ISetupCompatService iSetupCompatService) {
            this.state = state2;
            this.compatService = iSetupCompatService;
            if (state2 == State.CONNECTED) {
                Preconditions.checkNotNull(iSetupCompatService, "CompatService cannot be null when state is connected");
            }
        }

        private ServiceContext(State state2) {
            this(state2, (ISetupCompatService) null);
        }
    }

    static SetupCompatServiceProvider getInstance(Context context2) {
        Preconditions.checkNotNull(context2, "Context object cannot be null.");
        SetupCompatServiceProvider setupCompatServiceProvider = instance;
        if (setupCompatServiceProvider == null) {
            synchronized (SetupCompatServiceProvider.class) {
                setupCompatServiceProvider = instance;
                if (setupCompatServiceProvider == null) {
                    setupCompatServiceProvider = new SetupCompatServiceProvider(context2.getApplicationContext());
                    instance = setupCompatServiceProvider;
                    instance.requestServiceBind();
                }
            }
        }
        return setupCompatServiceProvider;
    }

    public static void setInstanceForTesting(SetupCompatServiceProvider setupCompatServiceProvider) {
        instance = setupCompatServiceProvider;
    }
}
