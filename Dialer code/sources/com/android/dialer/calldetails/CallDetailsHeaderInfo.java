package com.android.dialer.calldetails;

import com.android.dialer.DialerPhoneNumber;
import com.android.dialer.glidephotomanager.PhotoInfo;
import com.android.dialer.searchfragment.list.NewSearchFragment;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Parser;
import java.io.IOException;

public final class CallDetailsHeaderInfo extends GeneratedMessageLite<CallDetailsHeaderInfo, Builder> implements CallDetailsHeaderInfoOrBuilder {
    /* access modifiers changed from: private */
    public static final CallDetailsHeaderInfo DEFAULT_INSTANCE = new CallDetailsHeaderInfo();
    private static volatile Parser<CallDetailsHeaderInfo> PARSER;
    private int bitField0_;
    private DialerPhoneNumber dialerPhoneNumber_;
    private PhotoInfo photoInfo_;
    private String primaryText_ = "";
    private String secondaryText_ = "";

    /* renamed from: com.android.dialer.calldetails.CallDetailsHeaderInfo$1 */
    static /* synthetic */ class C04171 {

        /* renamed from: $SwitchMap$com$google$protobuf$GeneratedMessageLite$MethodToInvoke */
        static final /* synthetic */ int[] f15xa1df5c61 = new int[GeneratedMessageLite.MethodToInvoke.values().length];

        /* JADX WARNING: Can't wrap try/catch for region: R(16:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|(3:15|16|18)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(18:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|18) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x0040 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x004b */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0056 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0014 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001f */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x002a */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0035 */
        static {
            /*
                com.google.protobuf.GeneratedMessageLite$MethodToInvoke[] r0 = com.google.protobuf.GeneratedMessageLite.MethodToInvoke.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f15xa1df5c61 = r0
                int[] r0 = f15xa1df5c61     // Catch:{ NoSuchFieldError -> 0x0014 }
                com.google.protobuf.GeneratedMessageLite$MethodToInvoke r1 = com.google.protobuf.GeneratedMessageLite.MethodToInvoke.NEW_MUTABLE_INSTANCE     // Catch:{ NoSuchFieldError -> 0x0014 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0014 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0014 }
            L_0x0014:
                int[] r0 = f15xa1df5c61     // Catch:{ NoSuchFieldError -> 0x001f }
                com.google.protobuf.GeneratedMessageLite$MethodToInvoke r1 = com.google.protobuf.GeneratedMessageLite.MethodToInvoke.IS_INITIALIZED     // Catch:{ NoSuchFieldError -> 0x001f }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001f }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001f }
            L_0x001f:
                int[] r0 = f15xa1df5c61     // Catch:{ NoSuchFieldError -> 0x002a }
                com.google.protobuf.GeneratedMessageLite$MethodToInvoke r1 = com.google.protobuf.GeneratedMessageLite.MethodToInvoke.MAKE_IMMUTABLE     // Catch:{ NoSuchFieldError -> 0x002a }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x002a }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x002a }
            L_0x002a:
                int[] r0 = f15xa1df5c61     // Catch:{ NoSuchFieldError -> 0x0035 }
                com.google.protobuf.GeneratedMessageLite$MethodToInvoke r1 = com.google.protobuf.GeneratedMessageLite.MethodToInvoke.NEW_BUILDER     // Catch:{ NoSuchFieldError -> 0x0035 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0035 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0035 }
            L_0x0035:
                int[] r0 = f15xa1df5c61     // Catch:{ NoSuchFieldError -> 0x0040 }
                com.google.protobuf.GeneratedMessageLite$MethodToInvoke r1 = com.google.protobuf.GeneratedMessageLite.MethodToInvoke.VISIT     // Catch:{ NoSuchFieldError -> 0x0040 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0040 }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0040 }
            L_0x0040:
                int[] r0 = f15xa1df5c61     // Catch:{ NoSuchFieldError -> 0x004b }
                com.google.protobuf.GeneratedMessageLite$MethodToInvoke r1 = com.google.protobuf.GeneratedMessageLite.MethodToInvoke.MERGE_FROM_STREAM     // Catch:{ NoSuchFieldError -> 0x004b }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x004b }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x004b }
            L_0x004b:
                int[] r0 = f15xa1df5c61     // Catch:{ NoSuchFieldError -> 0x0056 }
                com.google.protobuf.GeneratedMessageLite$MethodToInvoke r1 = com.google.protobuf.GeneratedMessageLite.MethodToInvoke.GET_DEFAULT_INSTANCE     // Catch:{ NoSuchFieldError -> 0x0056 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0056 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0056 }
            L_0x0056:
                int[] r0 = f15xa1df5c61     // Catch:{ NoSuchFieldError -> 0x0062 }
                com.google.protobuf.GeneratedMessageLite$MethodToInvoke r1 = com.google.protobuf.GeneratedMessageLite.MethodToInvoke.GET_PARSER     // Catch:{ NoSuchFieldError -> 0x0062 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0062 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0062 }
            L_0x0062:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.dialer.calldetails.CallDetailsHeaderInfo.C04171.<clinit>():void");
        }
    }

    public static final class Builder extends GeneratedMessageLite.Builder<CallDetailsHeaderInfo, Builder> implements CallDetailsHeaderInfoOrBuilder {
        /* synthetic */ Builder(C04171 r1) {
            this();
        }

        public Builder setDialerPhoneNumber(DialerPhoneNumber dialerPhoneNumber) {
            copyOnWrite();
            CallDetailsHeaderInfo.access$100((CallDetailsHeaderInfo) this.instance, dialerPhoneNumber);
            return this;
        }

        public Builder setPhotoInfo(PhotoInfo.Builder builder) {
            copyOnWrite();
            ((CallDetailsHeaderInfo) this.instance).setPhotoInfo(builder);
            return this;
        }

        public Builder setPrimaryText(String str) {
            copyOnWrite();
            CallDetailsHeaderInfo.access$900((CallDetailsHeaderInfo) this.instance, str);
            return this;
        }

        public Builder setSecondaryText(String str) {
            copyOnWrite();
            CallDetailsHeaderInfo.access$1200((CallDetailsHeaderInfo) this.instance, str);
            return this;
        }

        private Builder() {
            super(CallDetailsHeaderInfo.DEFAULT_INSTANCE);
        }
    }

    static {
        DEFAULT_INSTANCE.makeImmutable();
    }

    private CallDetailsHeaderInfo() {
    }

    static /* synthetic */ void access$100(CallDetailsHeaderInfo callDetailsHeaderInfo, DialerPhoneNumber dialerPhoneNumber) {
        if (dialerPhoneNumber != null) {
            callDetailsHeaderInfo.dialerPhoneNumber_ = dialerPhoneNumber;
            callDetailsHeaderInfo.bitField0_ |= 1;
            return;
        }
        throw new NullPointerException();
    }

    static /* synthetic */ void access$1200(CallDetailsHeaderInfo callDetailsHeaderInfo, String str) {
        if (str != null) {
            callDetailsHeaderInfo.bitField0_ |= 8;
            callDetailsHeaderInfo.secondaryText_ = str;
            return;
        }
        throw new NullPointerException();
    }

    static /* synthetic */ void access$900(CallDetailsHeaderInfo callDetailsHeaderInfo, String str) {
        if (str != null) {
            callDetailsHeaderInfo.bitField0_ |= 4;
            callDetailsHeaderInfo.primaryText_ = str;
            return;
        }
        throw new NullPointerException();
    }

    public static CallDetailsHeaderInfo getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    public static Builder newBuilder() {
        return (Builder) DEFAULT_INSTANCE.toBuilder();
    }

    /* access modifiers changed from: private */
    public void setPhotoInfo(PhotoInfo.Builder builder) {
        this.photoInfo_ = (PhotoInfo) builder.build();
        this.bitField0_ |= 2;
    }

    /* access modifiers changed from: protected */
    public final Object dynamicMethod(GeneratedMessageLite.MethodToInvoke methodToInvoke, Object obj, Object obj2) {
        boolean z = true;
        boolean z2 = false;
        switch (methodToInvoke.ordinal()) {
            case 0:
                return DEFAULT_INSTANCE;
            case NewSearchFragment.READ_CONTACTS_PERMISSION_REQUEST_CODE /*1*/:
                GeneratedMessageLite.Visitor visitor = (GeneratedMessageLite.Visitor) obj;
                CallDetailsHeaderInfo callDetailsHeaderInfo = (CallDetailsHeaderInfo) obj2;
                this.dialerPhoneNumber_ = (DialerPhoneNumber) visitor.visitMessage(this.dialerPhoneNumber_, callDetailsHeaderInfo.dialerPhoneNumber_);
                this.photoInfo_ = (PhotoInfo) visitor.visitMessage(this.photoInfo_, callDetailsHeaderInfo.photoInfo_);
                this.primaryText_ = visitor.visitString((this.bitField0_ & 4) == 4, this.primaryText_, (callDetailsHeaderInfo.bitField0_ & 4) == 4, callDetailsHeaderInfo.primaryText_);
                boolean z3 = (this.bitField0_ & 8) == 8;
                String str = this.secondaryText_;
                if ((callDetailsHeaderInfo.bitField0_ & 8) != 8) {
                    z = false;
                }
                this.secondaryText_ = visitor.visitString(z3, str, z, callDetailsHeaderInfo.secondaryText_);
                if (visitor == GeneratedMessageLite.MergeFromVisitor.INSTANCE) {
                    this.bitField0_ |= callDetailsHeaderInfo.bitField0_;
                }
                return this;
            case 2:
                CodedInputStream codedInputStream = (CodedInputStream) obj;
                ExtensionRegistryLite extensionRegistryLite = (ExtensionRegistryLite) obj2;
                while (!z2) {
                    try {
                        int readTag = codedInputStream.readTag();
                        if (readTag != 0) {
                            if (readTag == 10) {
                                DialerPhoneNumber.Builder builder = (this.bitField0_ & 1) == 1 ? (DialerPhoneNumber.Builder) this.dialerPhoneNumber_.toBuilder() : null;
                                this.dialerPhoneNumber_ = (DialerPhoneNumber) codedInputStream.readMessage(DialerPhoneNumber.parser(), extensionRegistryLite);
                                if (builder != null) {
                                    builder.mergeFrom(this.dialerPhoneNumber_);
                                    this.dialerPhoneNumber_ = (DialerPhoneNumber) builder.buildPartial();
                                }
                                this.bitField0_ |= 1;
                            } else if (readTag == 18) {
                                PhotoInfo.Builder builder2 = (this.bitField0_ & 2) == 2 ? (PhotoInfo.Builder) this.photoInfo_.toBuilder() : null;
                                this.photoInfo_ = (PhotoInfo) codedInputStream.readMessage(PhotoInfo.parser(), extensionRegistryLite);
                                if (builder2 != null) {
                                    builder2.mergeFrom(this.photoInfo_);
                                    this.photoInfo_ = (PhotoInfo) builder2.buildPartial();
                                }
                                this.bitField0_ |= 2;
                            } else if (readTag == 26) {
                                String readString = codedInputStream.readString();
                                this.bitField0_ |= 4;
                                this.primaryText_ = readString;
                            } else if (readTag == 34) {
                                String readString2 = codedInputStream.readString();
                                this.bitField0_ |= 8;
                                this.secondaryText_ = readString2;
                            } else if (!parseUnknownField(readTag, codedInputStream)) {
                            }
                        }
                        z2 = true;
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException(e.setUnfinishedMessage(this));
                    } catch (IOException e2) {
                        throw new RuntimeException(new InvalidProtocolBufferException(e2.getMessage()).setUnfinishedMessage(this));
                    }
                }
                break;
            case 3:
                return null;
            case 4:
                return new CallDetailsHeaderInfo();
            case 5:
                return new Builder((C04171) null);
            case 6:
                break;
            case 7:
                if (PARSER == null) {
                    synchronized (CallDetailsHeaderInfo.class) {
                        if (PARSER == null) {
                            PARSER = new GeneratedMessageLite.DefaultInstanceBasedParser(DEFAULT_INSTANCE);
                        }
                    }
                }
                return PARSER;
            default:
                throw new UnsupportedOperationException();
        }
        return DEFAULT_INSTANCE;
    }

    public DialerPhoneNumber getDialerPhoneNumber() {
        DialerPhoneNumber dialerPhoneNumber = this.dialerPhoneNumber_;
        return dialerPhoneNumber == null ? DialerPhoneNumber.getDefaultInstance() : dialerPhoneNumber;
    }

    public PhotoInfo getPhotoInfo() {
        PhotoInfo photoInfo = this.photoInfo_;
        return photoInfo == null ? PhotoInfo.getDefaultInstance() : photoInfo;
    }

    public String getPrimaryText() {
        return this.primaryText_;
    }

    public String getSecondaryText() {
        return this.secondaryText_;
    }

    public int getSerializedSize() {
        int i = this.memoizedSerializedSize;
        if (i != -1) {
            return i;
        }
        int i2 = 0;
        if ((this.bitField0_ & 1) == 1) {
            i2 = 0 + CodedOutputStream.computeMessageSize(1, getDialerPhoneNumber());
        }
        if ((this.bitField0_ & 2) == 2) {
            i2 += CodedOutputStream.computeMessageSize(2, getPhotoInfo());
        }
        if ((this.bitField0_ & 4) == 4) {
            i2 += CodedOutputStream.computeStringSize(3, this.primaryText_);
        }
        if ((this.bitField0_ & 8) == 8) {
            i2 += CodedOutputStream.computeStringSize(4, this.secondaryText_);
        }
        int serializedSize = this.unknownFields.getSerializedSize() + i2;
        this.memoizedSerializedSize = serializedSize;
        return serializedSize;
    }

    public void writeTo(CodedOutputStream codedOutputStream) throws IOException {
        if ((this.bitField0_ & 1) == 1) {
            codedOutputStream.writeMessage(1, getDialerPhoneNumber());
        }
        if ((this.bitField0_ & 2) == 2) {
            codedOutputStream.writeMessage(2, getPhotoInfo());
        }
        if ((this.bitField0_ & 4) == 4) {
            codedOutputStream.writeString(3, this.primaryText_);
        }
        if ((this.bitField0_ & 8) == 8) {
            codedOutputStream.writeString(4, this.secondaryText_);
        }
        this.unknownFields.writeTo(codedOutputStream);
    }
}
