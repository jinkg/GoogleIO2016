package com.yalin.googleio2016.util;

import android.os.Environment;
import android.os.Process;
import android.util.Log;

import com.yalin.googleio2016.BuildConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Locale;

/**
 * YaLin
 * 2016/11/23.
 */

public class LogUtil {
    private static final String FILE_NAME = "GoogleIO2016/stat_log.txt";

    public static synchronized boolean isExternalLogEnabled() {
        //noinspection RedundantIfStatement
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return false;
        }
        return BuildConfig.ENABLE_EXTERNAL_LOG;
    }

    public static boolean isLoggable() {
        return BuildConfig.ENABLE_EXTERNAL_LOG;
    }

    public static void d(String tag, String msg) {
        String procInfo = getProcessInfo();
        Log.d(tag, procInfo + msg);
    }

    public static void f(String tag, String msg) {
        String procInfo = getProcessInfo();

        Log.d(tag, procInfo + msg);

        if (!isExternalLogEnabled()) {
            return;
        }
        tag = tag + ":debug";
        writeLog(tag, procInfo + msg);
    }

    public static void w(String tag, String msg) {
        String procInfo = getProcessInfo();
        Log.w(tag, procInfo + msg);
    }

    public static void e(String tag, String msg, Throwable throwable) {
        String stackTraces = Log.getStackTraceString(throwable);
        if (msg == null) {
            msg = "";
        }
        e(tag, msg + " :\n " + stackTraces);
    }

    public static void e(String tag, String msg) {
        String procInfo = getProcessInfo();

        Log.e(tag, procInfo + msg);

        if (!isExternalLogEnabled()) {
            return;
        }
        writeLog(tag, procInfo + msg);
    }

    private static String getProcessInfo() {
        return "Process id: " + Process.myPid()
                + " Thread id: " + Thread.currentThread().getId() + " ";
    }

    private static String formatStackTrance(StackTraceElement[] stackTraceElements) {
        StringBuilder stackTraces = new StringBuilder();
        for (StackTraceElement stackTrace : stackTraceElements) {
            stackTraces.append(stackTrace.toString())
                    .append("\n");
        }
        return stackTraces.toString();
    }

    private static synchronized void writeLog(String tag, String msg) {
        internalWriteLog(FILE_NAME, tag, msg);
    }

    private static synchronized void internalWriteLog(String filename, String tag, String msg) {
        try {
            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                return;
            }

            File file = new File(Environment.getExternalStorageDirectory(), filename);
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));

            String time = getCurrentTime();
            bw.write(time + " " + tag + " \t" + msg + "\r\n");

            bw.close();
        } catch (Exception e) {
            // ignore
        }
    }

    private static String getCurrentTime() {
        Calendar c = Calendar.getInstance();
        return String.format(Locale.getDefault(), "%d-%02d-%02d %02d:%02d:%02d.%03d",
                c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND),
                c.get(Calendar.MILLISECOND));
    }
}
