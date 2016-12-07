package com.yalin.googleio2016.sync;

import android.annotation.SuppressLint;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.preference.PreferenceManager;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.yalin.googleio2016.io.BlocksHandler;
import com.yalin.googleio2016.io.CardHandler;
import com.yalin.googleio2016.io.HashtagsHandler;
import com.yalin.googleio2016.io.JSONHandler;
import com.yalin.googleio2016.io.MapPropertyHandler;
import com.yalin.googleio2016.io.RoomsHandler;
import com.yalin.googleio2016.io.SearchSuggestHandler;
import com.yalin.googleio2016.io.SessionsHandler;
import com.yalin.googleio2016.io.SpeakersHandler;
import com.yalin.googleio2016.io.TagsHandler;
import com.yalin.googleio2016.io.VideosHandler;
import com.yalin.googleio2016.io.map.model.Tile;
import com.yalin.googleio2016.provider.ScheduleContract;
import com.yalin.googleio2016.util.LogUtil;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * YaLin
 * 2016/11/29.
 * <p>
 * Helper class that parses conference data and imports them into the app's
 * Content Provider.
 */
public class ConferenceDataHandler {
    private static final String TAG = "ConferenceDataHandler";

    // Shared settings_prefs key under which we store the timestamp that corresponds to
    // the data we currently have in our content provider.
    private static final String SP_KEY_DATA_TIMESTAMP = "data_timestamp";

    // symbolic timestamp to use when we are missing timestamp data (which means our data is
    // really old or nonexistent)
    private static final String DEFAULT_TIMESTAMP = "Sat, 1 Jan 2000 00:00:00 GMT";

    private static final String DATA_KEY_BLOCKS = "blocks";
    private static final String DATA_KEY_TAGS = "tags";
    private static final String DATA_KEY_SPEAKERS = "speakers";
    private static final String DATA_KEY_SESSIONS = "sessions";

    private static final String[] DATA_KEYS_IN_ORDER = {
            DATA_KEY_TAGS,
            DATA_KEY_SPEAKERS,
            DATA_KEY_SESSIONS
    };

    Context mContext = null;

    BlocksHandler mBlocksHandler = null;
    TagsHandler mTagsHandler = null;
    SpeakersHandler mSpeakersHandler = null;
    SessionsHandler mSessionsHandler = null;

    // Convenience map that maps the key name to its corresponding handler (e.g.
    // "blocks" to mBlocksHandler (to avoid very tedious if-elses)
    HashMap<String, JSONHandler> mHandlerForKey = new HashMap<>();

    // Tally of total content provider operations we carried out (for statistical purposes)
    private int mContentProviderOperationDone = 0;

    public ConferenceDataHandler(Context ctx) {
        mContext = ctx;
    }

    /**
     * Parses the conference data in the given objects and imports the data into the
     * content provider. The format of the data is documented at https://code.google.com/p/iosched.
     *
     * @param dataBodies       The collection of JSON objects to parse and import.
     * @param dataTimestamp    The timestamp of the data. This should be in RFC1123 format.
     * @param downloadsAllowed Whether or not we are supposed to download data from the internet if needed.
     * @throws IOException If there is a problem parsing the data.
     */
    public void applyConferenceData(String[] dataBodies, String dataTimestamp,
                                    boolean downloadsAllowed) throws IOException {
        LogUtil.d(TAG, "Applying data from " + dataBodies.length +
                " files, timestamp " + dataTimestamp);

        mHandlerForKey.put(DATA_KEY_BLOCKS, mBlocksHandler = new BlocksHandler(mContext));
        mHandlerForKey.put(DATA_KEY_TAGS, mTagsHandler = new TagsHandler(mContext));
        mHandlerForKey.put(DATA_KEY_SPEAKERS, mSpeakersHandler = new SpeakersHandler(mContext));
        mHandlerForKey.put(DATA_KEY_SESSIONS, mSessionsHandler = new SessionsHandler(mContext));

        // process the jsons. This will call each of the handlers when appropriate to deal
        // with the objects we see in the data.
        LogUtil.d(TAG, "Processing " + dataBodies.length + " JSON objects.");
        for (int i = 0; i < dataBodies.length; i++) {
            LogUtil.d(TAG, "Processing json object #" + (i + 1) + " of " + dataBodies.length);
            processDataBody(dataBodies[i]);
        }

        // the sessions handler needs to know the tag and speaker maps to process sessions
        mSessionsHandler.setTagMap(mTagsHandler.getTagMap());
        mSessionsHandler.setSpeakerMap(mSpeakersHandler.getSpeakerMap());

        // produce the necessary content provider operations
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();
        for (String key : DATA_KEYS_IN_ORDER) {
            LogUtil.d(TAG, "Building content provider operations for: " + key);
            mHandlerForKey.get(key).makeContentProviderOperation(batch);
            LogUtil.d(TAG, "Content provider operations so far: " + batch.size());
        }
        LogUtil.d(TAG, "Total content provider operations: " + batch.size());

        // download or process local map tile overlay files (SVG files)
        LogUtil.d(TAG, "Processing map overlay files");
//        processMapOverlayFiles(mMapPropertyHandler.getTileOverlays(), downloadsAllowed);

        LogUtil.d(TAG, "Applying " + batch.size() + " content provider operations.");
        try {
            int operations = batch.size();
            if (operations > 0) {
                mContext.getContentResolver().applyBatch(ScheduleContract.CONTENT_AUTHORITY, batch);
            }
            LogUtil.d(TAG, "Successfully applied " + operations + " content provider operations.");
            mContentProviderOperationDone += operations;
        } catch (RemoteException ex) {
            LogUtil.e(TAG, "RemoteException while applying content provider operations.", ex);
            throw new RuntimeException("Error executing content provider batch operation.", ex);
        } catch (OperationApplicationException e) {
            LogUtil.e(TAG, "OperationApplicationException while applying content provider operations.", e);
            throw new RuntimeException("Error executing content provider batch operation", e);
        }

        LogUtil.d(TAG, "Notifying changes on all top-level paths on Content Resolver.");
        ContentResolver resolver = mContext.getContentResolver();
        for (String path : ScheduleContract.TOP_LEVEL_PATHS) {
            Uri uri = ScheduleContract.BASE_CONTENT_URI.buildUpon().appendPath(path).build();
            resolver.notifyChange(uri, null);
        }

        setDataTimestamp(dataTimestamp);
        LogUtil.d(TAG, "Done applying conference data.");
    }

    private void processDataBody(String dataBody) throws IOException {
        JsonReader reader = new JsonReader(new StringReader(dataBody));
        JsonParser parser = new JsonParser();
        try {
            reader.setLenient(true);

            // the whole file is a single JSON object
            reader.beginObject();

            while (reader.hasNext()) {
                String key = reader.nextName();
                if (mHandlerForKey.containsKey(key)) {
                    LogUtil.d(TAG, "Processing key in conference data json: " + key);
                    // pass the value to the corresponding handler
                    mHandlerForKey.get(key).process(parser.parse(reader));
                } else {
                    LogUtil.w(TAG, "Skipping unknown key in conference data json: " + key);
                    reader.skipValue();
                }
            }
            reader.endObject();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private void processMapOverlayFiles(Collection<Tile> collection, boolean downloadAllowed)
            throws IOException {

    }

    // Sets the timestamp of the data we have in the content provider.
    @SuppressLint("CommitPrefEdits")
    public void setDataTimestamp(String timestamp) {
        LogUtil.d(TAG, "Setting data timestamp to: " + timestamp);
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(
                SP_KEY_DATA_TIMESTAMP, timestamp).commit();
    }
}
