package com.yalin.googleio2016.io;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.yalin.googleio2016.io.model.Block;
import com.yalin.googleio2016.provider.ScheduleContract;
import com.yalin.googleio2016.provider.ScheduleContractHelper;
import com.yalin.googleio2016.util.LogUtil;
import com.yalin.googleio2016.util.ParserUtils;

import java.util.ArrayList;

/**
 * YaLin
 * 2016/11/29.
 */

public class BlocksHandler extends JSONHandler {
    private static final String TAG = "BlocksHandler";

    private ArrayList<Block> mBlocks = new ArrayList<>();

    public BlocksHandler(Context context) {
        super(context);
    }

    @Override
    public void makeContentProviderOperation(ArrayList<ContentProviderOperation> list) {
        Uri uri = ScheduleContractHelper.setUriAsCalledFromSyncAdapter(
                ScheduleContract.Blocks.CONTENT_URI);
        list.add(ContentProviderOperation.newDelete(uri).build());
        for (Block block : mBlocks) {
            outputBlock(block, list);
        }
    }

    @Override
    public void process(JsonElement element) {
        for (Block block : new Gson().fromJson(element, Block[].class)) {
            mBlocks.add(block);
        }
    }

    private static void outputBlock(Block block, ArrayList<ContentProviderOperation> list) {
        Uri uri = ScheduleContractHelper.setUriAsCalledFromSyncAdapter(
                ScheduleContract.Blocks.CONTENT_URI);
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri);
        String title = block.title != null ? block.title : "";
        String meta = block.subtitle != null ? block.subtitle : "";

        String type = block.type;
        if (!ScheduleContract.Blocks.isValidBlockType(type)) {
            LogUtil.w(TAG, "block from " + block.start + " to " + block.end + " has unrecognized type ("
                    + type + "). Using " + ScheduleContract.Blocks.BLOCK_TYPE_BREAK + " instead.");
            type = ScheduleContract.Blocks.BLOCK_TYPE_BREAK;
        }

        long startTimeL = ParserUtils.parseTime(block.start);
        long endTimeL = ParserUtils.parseTime(block.end);
        final String blockId = ScheduleContract.Blocks.generateBlockId(startTimeL, endTimeL);
        builder.withValue(ScheduleContract.Blocks.BLOCK_ID, blockId);
        builder.withValue(ScheduleContract.Blocks.BLOCK_TITLE, title);
        builder.withValue(ScheduleContract.Blocks.BLOCK_START, startTimeL);
        builder.withValue(ScheduleContract.Blocks.BLOCK_END, endTimeL);
        builder.withValue(ScheduleContract.Blocks.BLOCK_TYPE, type);
        builder.withValue(ScheduleContract.Blocks.BLOCK_SUBTITLE, meta);
        list.add(builder.build());
    }
}
