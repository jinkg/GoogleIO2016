package com.yalin.googleio2016.util;

import android.content.Context;

import com.google.common.base.Charsets;
import com.turbomanage.httpclient.BasicHttpClient;
import com.yalin.googleio2016.BuildConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * YaLin
 * 2016/12/7.
 * <p>
 * Utility methods and constants used for writing and reading to from streams and files.
 */
public class IOUtils {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final boolean AUTHORIZATION_TO_BACKEND_REQUIRED = BuildConfig.DEBUG;

    /**
     * Writes the given string to a {@link File}.
     *
     * @param data The data to be written to the File.
     * @param file The File to write to.
     * @throws IOException
     */
    public static void writeToFile(String data, File file) throws IOException {
        writeToFile(data.getBytes(Charsets.UTF_8), file);
    }

    /**
     * Write the given bytes to a {@link File}.
     *
     * @param data The bytes to be written to the File.
     * @param file The {@link File} to be used for writing the data.
     * @throws IOException
     */
    public static void writeToFile(byte[] data, File file) throws IOException {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(data);
            os.flush();
            // Perform an fsync on the FileOutputStream.
            os.getFD().sync();
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    /**
     * Reads a {@link File} as a String
     *
     * @param file The file to be read in.
     * @return Returns the contents of the File as a String.
     * @throws IOException
     */
    public static String readFileAsString(File file) throws IOException {
        return readAsString(new FileInputStream(file));
    }

    /**
     * Reads an {@link InputStream} into a String using the UTF-8 encoding.
     * Note that this method closes the InputStream passed to it.
     *
     * @param is The InputStream to be read.
     * @return The contents of the InputStream as a String.
     * @throws IOException
     */
    public static String readAsString(InputStream is) throws IOException {
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            String line;
            reader = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return sb.toString();
    }

    /**
     * If {@code AUTHORIZATION_TO_BACKEND_REQUIRED} is true add an authentication header to the
     * given request. The currently signed in user is used to retrieve the auth token.
     * Allows pre-release builds to use a Google Cloud storage bucket with non-public data.
     *
     * @param context         Context used to retrieve auth token from SharedPreferences.
     * @param basicHttpClient HTTP client to which the authorization header will be added.
     */
    public static void authorizeHttpClient(Context context, BasicHttpClient basicHttpClient) {
        if (basicHttpClient == null || AccountUtils.getAuthToken(context) == null) {
            return;
        }
        if (AUTHORIZATION_TO_BACKEND_REQUIRED) {
            basicHttpClient.addHeader(AUTHORIZATION_HEADER,
                    BEARER_PREFIX + AccountUtils.getAuthToken(context));
        }
    }
}
