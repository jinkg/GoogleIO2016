package com.yalin.googleio2016.injection;

import android.app.LoaderManager;
import android.content.Context;
import android.net.Uri;

import com.yalin.googleio2016.explore.ExploreIOModel;
import com.yalin.googleio2016.model.ScheduleHelper;
import com.yalin.googleio2016.myschedule.MyScheduleModel;

/**
 * YaLin
 * 2016/11/30.
 * <p>
 * Provides a way to inject stub classes when running integration tests.
 */
public class ModelProvider {
    private static MyScheduleModel stubMyScheduleModel = null;

    private static ExploreIOModel stubExploreIOModel = null;

    public static ExploreIOModel provideExploreIOModel(Uri sessionsUri, Context context,
                                                       LoaderManager loaderManager) {
        if (stubExploreIOModel != null) {
            return stubExploreIOModel;
        } else {
            return new ExploreIOModel(context, sessionsUri, loaderManager);
        }
    }

    public static MyScheduleModel provideMyScheduleMode(ScheduleHelper scheduleHelper,
                                                        Context context) {
        if (stubMyScheduleModel != null) {
            return stubMyScheduleModel.initStaticDataAndObservers();
        } else {
            return new MyScheduleModel(scheduleHelper, context).initStaticDataAndObservers();
        }
    }
}
