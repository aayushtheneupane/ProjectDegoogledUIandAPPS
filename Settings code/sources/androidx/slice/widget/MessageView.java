package androidx.slice.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.slice.SliceItem;
import androidx.slice.core.SliceQuery;
import androidx.slice.widget.SliceView;

public class MessageView extends SliceChildView {
    private TextView mDetails;
    private ImageView mIcon;
    private int mRowIndex;

    public int getMode() {
        return 2;
    }

    public void resetView() {
    }

    public MessageView(Context context) {
        super(context);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mDetails = (TextView) findViewById(16908304);
        this.mIcon = (ImageView) findViewById(16908294);
    }

    public void setSliceItem(SliceContent sliceContent, boolean z, int i, int i2, SliceView.OnSliceActionListener onSliceActionListener) {
        Drawable loadDrawable;
        SliceItem sliceItem = sliceContent.getSliceItem();
        setSliceActionListener(onSliceActionListener);
        this.mRowIndex = i;
        SliceItem findSubtype = SliceQuery.findSubtype(sliceItem, "image", "source");
        if (!(findSubtype == null || findSubtype.getIcon() == null || (loadDrawable = findSubtype.getIcon().loadDrawable(getContext())) == null)) {
            int applyDimension = (int) TypedValue.applyDimension(1, 24.0f, getContext().getResources().getDisplayMetrics());
            Bitmap createBitmap = Bitmap.createBitmap(applyDimension, applyDimension, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(createBitmap);
            loadDrawable.setBounds(0, 0, applyDimension, applyDimension);
            loadDrawable.draw(canvas);
            this.mIcon.setImageBitmap(SliceViewUtil.getCircularBitmap(createBitmap));
        }
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        for (SliceItem next : SliceQuery.findAll(sliceItem, "text")) {
            if (spannableStringBuilder.length() != 0) {
                spannableStringBuilder.append(10);
            }
            spannableStringBuilder.append(next.getSanitizedText());
        }
        this.mDetails.setText(spannableStringBuilder.toString());
    }
}
