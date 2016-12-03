package com.yalin.googleio2016.myschedule;

import com.yalin.googleio2016.archframework.QueryEnum;
import com.yalin.googleio2016.archframework.UserActionEnum;

/**
 * YaLin
 * 2016/12/3.
 */

public class MyScheduleModel {

    public enum MyScheduleQueryEnum implements QueryEnum {
        SCHEDULE(0, null);

        private int id;

        private String[] projection;

        MyScheduleQueryEnum(int id, String[] projection) {
            this.id = id;
            this.projection = projection;
        }


        @Override
        public int getId() {
            return id;
        }

        @Override
        public String[] getProjection() {
            return projection;
        }
    }

    public enum MyScheduleUserActionEnum implements UserActionEnum {
        RELOAD_DATA(1),
        // Click on a row in the schedule, it opens the session or a list of available sessions
        SESSION_SLOT(2),
        FEEDBACK(3),
        REDRAW_UI(4);

        private int id;

        MyScheduleUserActionEnum(int id) {
            this.id = id;
        }


        @Override
        public int getId() {
            return id;
        }
    }
}
