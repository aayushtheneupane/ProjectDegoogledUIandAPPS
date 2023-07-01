package com.google.internal.communications.voicemailtranscription.p008v1;

import com.android.dialer.searchfragment.list.NewSearchFragment;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Parser;
import java.io.IOException;

/* renamed from: com.google.internal.communications.voicemailtranscription.v1.GetTranscriptRequest */
public final class GetTranscriptRequest extends GeneratedMessageLite<GetTranscriptRequest, Builder> implements GetTranscriptRequestOrBuilder {
    /* access modifiers changed from: private */
    public static final GetTranscriptRequest DEFAULT_INSTANCE = new GetTranscriptRequest();
    private static volatile Parser<GetTranscriptRequest> PARSER;
    private int bitField0_;
    private String transcriptionId_ = "";

    /* renamed from: com.google.internal.communications.voicemailtranscription.v1.GetTranscriptRequest$1 */
    static /* synthetic */ class C09041 {

        /* renamed from: $SwitchMap$com$google$protobuf$GeneratedMessageLite$MethodToInvoke */
        static final /* synthetic */ int[] f67xa1df5c61 = new int[GeneratedMessageLite.MethodToInvoke.values().length];

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
                f67xa1df5c61 = r0
                int[] r0 = f67xa1df5c61     // Catch:{ NoSuchFieldError -> 0x0014 }
                com.google.protobuf.GeneratedMessageLite$MethodToInvoke r1 = com.google.protobuf.GeneratedMessageLite.MethodToInvoke.NEW_MUTABLE_INSTANCE     // Catch:{ NoSuchFieldError -> 0x0014 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0014 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0014 }
            L_0x0014:
                int[] r0 = f67xa1df5c61     // Catch:{ NoSuchFieldError -> 0x001f }
                com.google.protobuf.GeneratedMessageLite$MethodToInvoke r1 = com.google.protobuf.GeneratedMessageLite.MethodToInvoke.IS_INITIALIZED     // Catch:{ NoSuchFieldError -> 0x001f }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001f }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001f }
            L_0x001f:
                int[] r0 = f67xa1df5c61     // Catch:{ NoSuchFieldError -> 0x002a }
                com.google.protobuf.GeneratedMessageLite$MethodToInvoke r1 = com.google.protobuf.GeneratedMessageLite.MethodToInvoke.MAKE_IMMUTABLE     // Catch:{ NoSuchFieldError -> 0x002a }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x002a }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x002a }
            L_0x002a:
                int[] r0 = f67xa1df5c61     // Catch:{ NoSuchFieldError -> 0x0035 }
                com.google.protobuf.GeneratedMessageLite$MethodToInvoke r1 = com.google.protobuf.GeneratedMessageLite.MethodToInvoke.NEW_BUILDER     // Catch:{ NoSuchFieldError -> 0x0035 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0035 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0035 }
            L_0x0035:
                int[] r0 = f67xa1df5c61     // Catch:{ NoSuchFieldError -> 0x0040 }
                com.google.protobuf.GeneratedMessageLite$MethodToInvoke r1 = com.google.protobuf.GeneratedMessageLite.MethodToInvoke.VISIT     // Catch:{ NoSuchFieldError -> 0x0040 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0040 }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0040 }
            L_0x0040:
                int[] r0 = f67xa1df5c61     // Catch:{ NoSuchFieldError -> 0x004b }
                com.google.protobuf.GeneratedMessageLite$MethodToInvoke r1 = com.google.protobuf.GeneratedMessageLite.MethodToInvoke.MERGE_FROM_STREAM     // Catch:{ NoSuchFieldError -> 0x004b }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x004b }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x004b }
            L_0x004b:
                int[] r0 = f67xa1df5c61     // Catch:{ NoSuchFieldError -> 0x0056 }
                com.google.protobuf.GeneratedMessageLite$MethodToInvoke r1 = com.google.protobuf.GeneratedMessageLite.MethodToInvoke.GET_DEFAULT_INSTANCE     // Catch:{ NoSuchFieldError -> 0x0056 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0056 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0056 }
            L_0x0056:
                int[] r0 = f67xa1df5c61     // Catch:{ NoSuchFieldError -> 0x0062 }
                com.google.protobuf.GeneratedMessageLite$MethodToInvoke r1 = com.google.protobuf.GeneratedMessageLite.MethodToInvoke.GET_PARSER     // Catch:{ NoSuchFieldError -> 0x0062 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0062 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0062 }
            L_0x0062:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.internal.communications.voicemailtranscription.p008v1.GetTranscriptRequest.C09041.<clinit>():void");
        }
    }

    /* renamed from: com.google.internal.communications.voicemailtranscription.v1.GetTranscriptRequest$Builder */
    public static final class Builder extends GeneratedMessageLite.Builder<GetTranscriptRequest, Builder> implements GetTranscriptRequestOrBuilder {
        /* synthetic */ Builder(C09041 r1) {
            this();
        }

        public Builder setTranscriptionId(String str) {
            copyOnWrite();
            GetTranscriptRequest.access$100((GetTranscriptRequest) this.instance, str);
            return this;
        }

        private Builder() {
            super(GetTranscriptRequest.DEFAULT_INSTANCE);
        }
    }

    static {
        DEFAULT_INSTANCE.makeImmutable();
    }

    private GetTranscriptRequest() {
    }

    static /* synthetic */ void access$100(GetTranscriptRequest getTranscriptRequest, String str) {
        if (str != null) {
            getTranscriptRequest.bitField0_ |= 1;
            getTranscriptRequest.transcriptionId_ = str;
            return;
        }
        throw new NullPointerException();
    }

    public static GetTranscriptRequest getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    public static Builder newBuilder() {
        return (Builder) DEFAULT_INSTANCE.toBuilder();
    }

    /* access modifiers changed from: protected */
    public final Object dynamicMethod(GeneratedMessageLite.MethodToInvoke methodToInvoke, Object obj, Object obj2) {
        boolean z = false;
        switch (methodToInvoke.ordinal()) {
            case 0:
                return DEFAULT_INSTANCE;
            case NewSearchFragment.READ_CONTACTS_PERMISSION_REQUEST_CODE:
                GeneratedMessageLite.Visitor visitor = (GeneratedMessageLite.Visitor) obj;
                GetTranscriptRequest getTranscriptRequest = (GetTranscriptRequest) obj2;
                boolean z2 = (this.bitField0_ & 1) == 1;
                String str = this.transcriptionId_;
                if ((getTranscriptRequest.bitField0_ & 1) == 1) {
                    z = true;
                }
                this.transcriptionId_ = visitor.visitString(z2, str, z, getTranscriptRequest.transcriptionId_);
                if (visitor == GeneratedMessageLite.MergeFromVisitor.INSTANCE) {
                    this.bitField0_ |= getTranscriptRequest.bitField0_;
                }
                return this;
            case 2:
                CodedInputStream codedInputStream = (CodedInputStream) obj;
                ExtensionRegistryLite extensionRegistryLite = (ExtensionRegistryLite) obj2;
                while (!z) {
                    try {
                        int readTag = codedInputStream.readTag();
                        if (readTag != 0) {
                            if (readTag == 10) {
                                String readString = codedInputStream.readString();
                                this.bitField0_ |= 1;
                                this.transcriptionId_ = readString;
                            } else if (!parseUnknownField(readTag, codedInputStream)) {
                            }
                        }
                        z = true;
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
                return new GetTranscriptRequest();
            case 5:
                return new Builder((C09041) null);
            case 6:
                break;
            case 7:
                if (PARSER == null) {
                    synchronized (GetTranscriptRequest.class) {
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

    public int getSerializedSize() {
        int i = this.memoizedSerializedSize;
        if (i != -1) {
            return i;
        }
        int i2 = 0;
        if ((this.bitField0_ & 1) == 1) {
            i2 = 0 + CodedOutputStream.computeStringSize(1, this.transcriptionId_);
        }
        int serializedSize = this.unknownFields.getSerializedSize() + i2;
        this.memoizedSerializedSize = serializedSize;
        return serializedSize;
    }

    public void writeTo(CodedOutputStream codedOutputStream) throws IOException {
        if ((this.bitField0_ & 1) == 1) {
            codedOutputStream.writeString(1, this.transcriptionId_);
        }
        this.unknownFields.writeTo(codedOutputStream);
    }
}
