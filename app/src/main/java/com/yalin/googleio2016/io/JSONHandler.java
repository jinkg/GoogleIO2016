package com.yalin.googleio2016.io;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.google.common.base.Charsets;
import com.google.gson.JsonElement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

/**
 * YaLin
 * 2016/11/28.
 */

public abstract class JSONHandler {

    protected static Context mContext;

    public JSONHandler(Context context) {
        mContext = context;
    }

    public abstract void makeContentProviderOperation(ArrayList<ContentProviderOperation> list);

    public abstract void process(JsonElement element);

    public static String parseResource(Context context, int resource) throws IOException {
        InputStream stream = null;
        String data;
        try {
            stream = context.getResources().openRawResource(resource);
            data = parseStream(stream);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // Ignore exceptions during stream close, other exceptions thrown earlier will
                    // be handled by the calling methods
                }
            }
        }
        return data;
    }

    private static String parseStream(final InputStream stream) throws IOException {
        Reader reader = null;
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];

        try {
            reader = new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // Ignore exceptions during stream close, other exceptions thrown earlier will
                    // be handled by the calling methods
                }
            }
        }
        return writer.toString();
    }
}
