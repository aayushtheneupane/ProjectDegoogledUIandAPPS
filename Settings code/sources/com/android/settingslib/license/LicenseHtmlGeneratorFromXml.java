package com.android.settingslib.license;

import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class LicenseHtmlGeneratorFromXml {
    private final Map<String, String> mContentIdToFileContentMap = new HashMap();
    private final Map<String, String> mFileNameToContentIdMap = new HashMap();
    private final List<File> mXmlFiles;

    static class ContentIdAndFileNames {
        final String mContentId;
        final List<String> mFileNameList = new ArrayList();

        ContentIdAndFileNames(String str) {
            this.mContentId = str;
        }
    }

    private LicenseHtmlGeneratorFromXml(List<File> list) {
        this.mXmlFiles = list;
    }

    public static boolean generateHtml(List<File> list, File file, String str) {
        return new LicenseHtmlGeneratorFromXml(list).generateHtml(file, str);
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0059  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean generateHtml(java.io.File r4, java.lang.String r5) {
        /*
            r3 = this;
            java.util.List<java.io.File> r0 = r3.mXmlFiles
            java.util.Iterator r0 = r0.iterator()
        L_0x0006:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x0016
            java.lang.Object r1 = r0.next()
            java.io.File r1 = (java.io.File) r1
            r3.parse(r1)
            goto L_0x0006
        L_0x0016:
            java.util.Map<java.lang.String, java.lang.String> r0 = r3.mFileNameToContentIdMap
            boolean r0 = r0.isEmpty()
            r1 = 0
            if (r0 != 0) goto L_0x005c
            java.util.Map<java.lang.String, java.lang.String> r0 = r3.mContentIdToFileContentMap
            boolean r0 = r0.isEmpty()
            if (r0 == 0) goto L_0x0028
            goto L_0x005c
        L_0x0028:
            r0 = 0
            java.io.PrintWriter r2 = new java.io.PrintWriter     // Catch:{ FileNotFoundException | SecurityException -> 0x003f }
            r2.<init>(r4)     // Catch:{ FileNotFoundException | SecurityException -> 0x003f }
            java.util.Map<java.lang.String, java.lang.String> r0 = r3.mFileNameToContentIdMap     // Catch:{ FileNotFoundException | SecurityException -> 0x003d }
            java.util.Map<java.lang.String, java.lang.String> r3 = r3.mContentIdToFileContentMap     // Catch:{ FileNotFoundException | SecurityException -> 0x003d }
            generateHtml(r0, r3, r2, r5)     // Catch:{ FileNotFoundException | SecurityException -> 0x003d }
            r2.flush()     // Catch:{ FileNotFoundException | SecurityException -> 0x003d }
            r2.close()     // Catch:{ FileNotFoundException | SecurityException -> 0x003d }
            r3 = 1
            return r3
        L_0x003d:
            r3 = move-exception
            goto L_0x0041
        L_0x003f:
            r3 = move-exception
            r2 = r0
        L_0x0041:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r0 = "Failed to generate "
            r5.append(r0)
            r5.append(r4)
            java.lang.String r4 = r5.toString()
            java.lang.String r5 = "LicenseGeneratorFromXml"
            android.util.Log.e(r5, r4, r3)
            if (r2 == 0) goto L_0x005c
            r2.close()
        L_0x005c:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.license.LicenseHtmlGeneratorFromXml.generateHtml(java.io.File, java.lang.String):boolean");
    }

    private void parse(File file) {
        InputStreamReader inputStreamReader;
        if (file != null && file.exists() && file.length() != 0) {
            InputStreamReader inputStreamReader2 = null;
            try {
                if (file.getName().endsWith(".gz")) {
                    inputStreamReader = new InputStreamReader(new GZIPInputStream(new FileInputStream(file)));
                } else {
                    inputStreamReader = new FileReader(file);
                }
                InputStreamReader inputStreamReader3 = inputStreamReader;
                parse(inputStreamReader3, this.mFileNameToContentIdMap, this.mContentIdToFileContentMap);
                inputStreamReader3.close();
            } catch (IOException | XmlPullParserException e) {
                Log.e("LicenseGeneratorFromXml", "Failed to parse " + file, e);
                if (inputStreamReader2 != null) {
                    try {
                        inputStreamReader2.close();
                    } catch (IOException unused) {
                        Log.w("LicenseGeneratorFromXml", "Failed to close " + file);
                    }
                }
            }
        }
    }

    static void parse(InputStreamReader inputStreamReader, Map<String, String> map, Map<String, String> map2) throws XmlPullParserException, IOException {
        HashMap hashMap = new HashMap();
        HashMap hashMap2 = new HashMap();
        XmlPullParser newPullParser = Xml.newPullParser();
        newPullParser.setInput(inputStreamReader);
        newPullParser.nextTag();
        newPullParser.require(2, "", "licenses");
        for (int eventType = newPullParser.getEventType(); eventType != 1; eventType = newPullParser.next()) {
            if (eventType == 2) {
                if ("file-name".equals(newPullParser.getName())) {
                    String attributeValue = newPullParser.getAttributeValue("", "contentId");
                    if (!TextUtils.isEmpty(attributeValue)) {
                        String trim = readText(newPullParser).trim();
                        if (!TextUtils.isEmpty(trim)) {
                            hashMap.put(trim, attributeValue);
                        }
                    }
                } else if ("file-content".equals(newPullParser.getName())) {
                    String attributeValue2 = newPullParser.getAttributeValue("", "contentId");
                    if (!TextUtils.isEmpty(attributeValue2) && !map2.containsKey(attributeValue2) && !hashMap2.containsKey(attributeValue2)) {
                        String readText = readText(newPullParser);
                        if (!TextUtils.isEmpty(readText)) {
                            hashMap2.put(attributeValue2, readText);
                        }
                    }
                }
            }
        }
        map.putAll(hashMap);
        map2.putAll(hashMap2);
    }

    private static String readText(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        StringBuffer stringBuffer = new StringBuffer();
        int next = xmlPullParser.next();
        while (next == 4) {
            stringBuffer.append(xmlPullParser.getText());
            next = xmlPullParser.next();
        }
        return stringBuffer.toString();
    }

    static void generateHtml(Map<String, String> map, Map<String, String> map2, PrintWriter printWriter, String str) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.addAll(map.keySet());
        Collections.sort(arrayList);
        printWriter.println("<html><head>\n<style type=\"text/css\">\nbody { padding: 0; font-family: sans-serif; }\n.same-license { background-color: #eeeeee;\n                border-top: 20px solid white;\n                padding: 10px; }\n.label { font-weight: bold; }\n.file-list { margin-left: 1em; color: blue; }\n</style>\n</head><body topmargin=\"0\" leftmargin=\"0\" rightmargin=\"0\" bottommargin=\"0\">\n<div class=\"toc\">\n<ul>");
        if (!TextUtils.isEmpty(str)) {
            printWriter.println(str);
        }
        HashMap hashMap = new HashMap();
        ArrayList<ContentIdAndFileNames> arrayList2 = new ArrayList<>();
        int i = 0;
        for (String str2 : arrayList) {
            String str3 = map.get(str2);
            if (!hashMap.containsKey(str3)) {
                hashMap.put(str3, Integer.valueOf(i));
                arrayList2.add(new ContentIdAndFileNames(str3));
                i++;
            }
            int intValue = ((Integer) hashMap.get(str3)).intValue();
            ((ContentIdAndFileNames) arrayList2.get(intValue)).mFileNameList.add(str2);
            printWriter.format("<li><a href=\"#id%d\">%s</a></li>\n", new Object[]{Integer.valueOf(intValue), str2});
        }
        printWriter.println("</ul>\n</div><!-- table of contents -->\n<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
        int i2 = 0;
        for (ContentIdAndFileNames contentIdAndFileNames : arrayList2) {
            printWriter.format("<tr id=\"id%d\"><td class=\"same-license\">\n", new Object[]{Integer.valueOf(i2)});
            printWriter.println("<div class=\"label\">Notices for file(s):</div>");
            printWriter.println("<div class=\"file-list\">");
            for (String str4 : contentIdAndFileNames.mFileNameList) {
                printWriter.format("%s <br/>\n", new Object[]{str4});
            }
            printWriter.println("</div><!-- file-list -->");
            printWriter.println("<pre class=\"license-text\">");
            printWriter.println(map2.get(contentIdAndFileNames.mContentId));
            printWriter.println("</pre><!-- license-text -->");
            printWriter.println("</td></tr><!-- same-license -->");
            i2++;
        }
        printWriter.println("</table></body></html>");
    }
}
