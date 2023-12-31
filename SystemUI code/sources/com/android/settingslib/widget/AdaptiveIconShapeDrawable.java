package com.android.settingslib.widget;

import android.content.res.Resources;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.util.AttributeSet;
import android.util.PathParser;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AdaptiveIconShapeDrawable extends ShapeDrawable {
    public AdaptiveIconShapeDrawable(Resources resources) {
        init(resources);
    }

    public void inflate(Resources resources, XmlPullParser xmlPullParser, AttributeSet attributeSet, Resources.Theme theme) throws XmlPullParserException, IOException {
        super.inflate(resources, xmlPullParser, attributeSet, theme);
        init(resources);
    }

    private void init(Resources resources) {
        setShape(new PathShape(new Path(PathParser.createPathFromPathData(resources.getString(17039785))), 100.0f, 100.0f));
    }
}
