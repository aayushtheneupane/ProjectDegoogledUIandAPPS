package p000;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.apps.photosgo.R;

/* renamed from: cdr */
/* compiled from: PG */
public final class cdr extends cci implements hbf, ioe, hbd {

    /* renamed from: aa */
    private cds f4130aa;

    /* renamed from: ab */
    private Context f4131ab;

    /* renamed from: ac */
    private final hkx f4132ac = new hkx(this);

    /* renamed from: ad */
    private final C0002ab f4133ad = new C0002ab(this);

    /* renamed from: ae */
    private boolean f4134ae;

    /* renamed from: af */
    private boolean f4135af;

    @Deprecated
    public cdr() {
        fxk.m9830b();
    }

    /* renamed from: ad */
    public final C0600w mo5ad() {
        return this.f4133ad;
    }

    @Deprecated
    /* renamed from: P */
    public final Context mo2628P() {
        if (this.f4131ab == null) {
            this.f4131ab = new hcf((Context) this.f4053Z);
        }
        return this.f4131ab;
    }

    /* access modifiers changed from: protected */
    /* renamed from: Q */
    public final /* bridge */ /* synthetic */ ftr mo3032Q() {
        return hcl.m11211d(this);
    }

    /* renamed from: k */
    public final Context mo2634k() {
        if (this.f4053Z != null) {
            return mo2628P();
        }
        return null;
    }

    /* renamed from: d */
    public final void mo2667d(Bundle bundle) {
        hlq c = hnb.m11779c();
        try {
            super.mo2667d(bundle);
            if (c != null) {
                c.close();
                return;
            }
            return;
        } catch (Throwable th) {
            ifn.m12935a(th, th);
        }
        throw th;
    }

    /* renamed from: a */
    public final void mo2665a(int i, int i2, Intent intent) {
        hlq a = this.f4132ac.mo7537a();
        try {
            super.mo2665a(i, i2, intent);
            if (a != null) {
                a.close();
                return;
            }
            return;
        } catch (Throwable th) {
            ifn.m12935a(th, th);
        }
        throw th;
    }

    /* renamed from: a */
    public final void mo2631a(Activity activity) {
        hlq c = hnb.m11779c();
        try {
            super.mo2631a(activity);
            if (c != null) {
                c.close();
                return;
            }
            return;
        } catch (Throwable th) {
            ifn.m12935a(th, th);
        }
        throw th;
    }

    /* renamed from: a */
    public final void mo1832a(Context context) {
        hlq c = hnb.m11779c();
        try {
            if (!this.f4135af) {
                super.mo1832a(context);
                if (this.f4130aa == null) {
                    this.f4130aa = ((cdv) mo2453b()).mo2443u();
                    this.f9583V.mo64a(new hbu(this.f4133ad));
                }
                if (c != null) {
                    c.close();
                    return;
                }
                return;
            }
            throw new IllegalStateException("A Fragment cannot be attached more than once. Instead, create a new Fragment instance.");
        } catch (ClassCastException e) {
            throw new IllegalStateException("Missing entry point. If you're in a test with explicit entry points specified in your @TestRoot, check that you're not missing the one for this class.", e);
        } catch (Throwable th) {
            if (c != null) {
                try {
                    c.close();
                } catch (Throwable th2) {
                    ifn.m12935a(th, th2);
                }
            }
            throw th;
        }
    }

    public final void onCancel(DialogInterface dialogInterface) {
        ihg.m13040a((hoi) new cdq(), (C0140fa) mo2635n().f4136a);
    }

    /* renamed from: a */
    public final void mo165a(Bundle bundle) {
        hlq c = hnb.m11779c();
        try {
            super.mo165a(bundle);
            if (c != null) {
                c.close();
                return;
            }
            return;
        } catch (Throwable th) {
            ifn.m12935a(th, th);
        }
        throw th;
    }

    /* renamed from: c */
    public final Dialog mo193c(Bundle bundle) {
        super.mo193c(bundle);
        hoc hoc = mo2635n().f4137b;
        hoc.mo7625a(R.string.trimming_video);
        hoc.mo7624a();
        C0394oj b = hoc.mo7627b();
        b.setCanceledOnTouchOutside(false);
        return b;
    }

    /* renamed from: a */
    public final View mo2630a(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        hlq c = hnb.m11779c();
        try {
            View a = super.mo2630a(layoutInflater, viewGroup, bundle);
            this.f4134ae = false;
            if (c != null) {
                c.close();
            }
            return a;
        } catch (Throwable th) {
            ifn.m12935a(th, th);
        }
        throw th;
    }

    /* renamed from: x */
    public final void mo1836x() {
        hlq c = hnb.m11779c();
        try {
            super.mo1836x();
            if (c != null) {
                c.close();
                return;
            }
            return;
        } catch (Throwable th) {
            ifn.m12935a(th, th);
        }
        throw th;
    }

    /* renamed from: f */
    public final void mo212f() {
        hlq c = hnb.m11779c();
        try {
            super.mo212f();
            if (c != null) {
                c.close();
                return;
            }
            return;
        } catch (Throwable th) {
            ifn.m12935a(th, th);
        }
        throw th;
    }

    /* renamed from: c */
    public final void mo1834c() {
        hlq c = hnb.m11779c();
        try {
            super.mo1834c();
            this.f4135af = true;
            if (c != null) {
                c.close();
                return;
            }
            return;
        } catch (Throwable th) {
            ifn.m12935a(th, th);
        }
        throw th;
    }

    public final void onDismiss(DialogInterface dialogInterface) {
        hlq c = this.f4132ac.mo7539c();
        try {
            super.onDismiss(dialogInterface);
            c.close();
            return;
        } catch (Throwable th) {
            ifn.m12935a(th, th);
        }
        throw th;
    }

    /* renamed from: b */
    public final LayoutInflater mo2633b(Bundle bundle) {
        hlq c = hnb.m11779c();
        try {
            LayoutInflater from = LayoutInflater.from(new hcf(super.mo2633b(bundle)));
            if (c != null) {
                c.close();
            }
            return from;
        } catch (Throwable th) {
            ifn.m12935a(th, th);
        }
        throw th;
    }

    /* renamed from: a */
    public final boolean mo2666a(MenuItem menuItem) {
        hlq b = this.f4132ac.mo7538b();
        try {
            boolean a = super.mo2666a(menuItem);
            if (b != null) {
                b.close();
            }
            return a;
        } catch (Throwable th) {
            ifn.m12935a(th, th);
        }
        throw th;
    }

    /* renamed from: w */
    public final void mo2669w() {
        hlq c = hnb.m11779c();
        try {
            super.mo2669w();
            if (c != null) {
                c.close();
                return;
            }
            return;
        } catch (Throwable th) {
            ifn.m12935a(th, th);
        }
        throw th;
    }

    /* renamed from: v */
    public final void mo2668v() {
        hlq c = hnb.m11779c();
        try {
            super.mo2668v();
            if (c != null) {
                c.close();
                return;
            }
            return;
        } catch (Throwable th) {
            ifn.m12935a(th, th);
        }
        throw th;
    }

    /* renamed from: d */
    public final void mo210d() {
        hlq c = hnb.m11779c();
        try {
            super.mo210d();
            hok.m11841b((C0140fa) this);
            if (this.f9239c) {
                if (!this.f4134ae) {
                    hok.m11838a((Context) mo5653m()).f13171d = ihg.m13027a((C0140fa) this);
                    mo2635n();
                    cdu.m4146a((fwy) this);
                    this.f4134ae = true;
                }
                hok.m11839a((C0140fa) this);
            }
            if (c != null) {
                c.close();
                return;
            }
            return;
        } catch (Throwable th) {
            ifn.m12935a(th, th);
        }
        throw th;
    }

    /* renamed from: e */
    public final void mo211e() {
        hlq c = hnb.m11779c();
        try {
            super.mo211e();
            if (c != null) {
                c.close();
                return;
            }
            return;
        } catch (Throwable th) {
            ifn.m12935a(th, th);
        }
        throw th;
    }

    /* renamed from: a */
    public final void mo2632a(View view, Bundle bundle) {
        hlq c = hnb.m11779c();
        try {
            if (!this.f9239c && !this.f4134ae) {
                hok.m11838a((Context) mo5653m()).f13171d = view;
                mo2635n();
                cdu.m4146a((fwy) this);
                this.f4134ae = true;
            }
            super.mo2632a(view, bundle);
            if (c != null) {
                c.close();
                return;
            }
            return;
        } catch (Throwable th) {
            ifn.m12935a(th, th);
        }
        throw th;
    }

    /* access modifiers changed from: private */
    /* renamed from: R */
    public final cds mo2635n() {
        cds cds = this.f4130aa;
        if (cds == null) {
            throw new IllegalStateException("peer() called before initialized.");
        } else if (!this.f4135af) {
            return cds;
        } else {
            throw new IllegalStateException("peer() called after destroyed.");
        }
    }
}
