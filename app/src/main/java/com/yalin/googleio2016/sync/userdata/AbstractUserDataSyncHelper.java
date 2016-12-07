package com.yalin.googleio2016.sync.userdata;

/**
 * YaLin
 * 2016/12/7.
 * <p>
 * Helper class that syncs user data in a Drive's AppData folder.
 * <p>
 * Protocode:
 * <p>
 * // when user clicks on "star":
 * session UI: run updateSession()
 * this.updateSession():
 * send addstar/removestar to contentProvider
 * send broadcast to update any dependent UI
 * save user actions as pending in shared settings_prefs
 * <p>
 * // on sync
 * syncadapter: call this.sync()
 * this.sync():
 * fetch remote content
 * if pending actions:
 * apply to content and update remote
 * if modified content != last synced content:
 * update contentProvider
 * send broadcast to update any dependent UI
 */
public abstract class AbstractUserDataSyncHelper {

    public boolean sync() {
        return false;
    }

    public int getIoExceptions() {
        return 0;
    }
}
