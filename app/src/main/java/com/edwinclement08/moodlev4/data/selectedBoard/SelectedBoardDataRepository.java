package com.edwinclement08.moodlev4.data.selectedBoard;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.edwinclement08.moodlev4.data.Message;
import com.edwinclement08.moodlev4.util.StitchClientListener;
import com.edwinclement08.moodlev4.util.StitchClientManager;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.mongodb.stitch.android.StitchClient;
import com.mongodb.stitch.android.services.mongodb.MongoClient;

import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SelectedBoardDataRepository implements StitchClientListener {
    public String TAG = "SelectedBoardDataRepository";
    private Boolean DEBUG = false;

    private StitchClient _client;
    private MongoClient _mongoClient;

    private String boardId;
    private Task<Void> refreshTask;

    private Activity parentActivity;

    private boolean justUpdatedFlag_forScrollDown;


    public SelectedBoardItem getMetaDataSet() {
        return metaDataSet;
    }

    public SelectedBoardItem metaDataSet = null;
    public ArrayList<Message> dataSet = null;

    public SelectedBoardDataRepository(String boardId, Activity parentActivity) {
        this.boardId = boardId;
        this.parentActivity = parentActivity;
        dataSet = new ArrayList<Message>();
//        metaDataSet  = new ArrayList<SelectedBoardItem>();

        StitchClientManager.registerListener(this);

    }

    private ArrayList<SelectedBoardItem> convertDocsToSelectedBoardItems(final List<Document> documents) {
        if (DEBUG)
            Log.i(TAG, "convertDocsToSelectedBoardItems: arrival Count = " + documents.toString());
        final ArrayList<SelectedBoardItem> items = new ArrayList<>(documents.size());
        if (DEBUG)
            Log.i(TAG, "convertDocsToSelectedBoardItems: Documents number " + documents.size());
        for (final Document doc : documents) {
            if (DEBUG) Log.i(TAG, "convertDocsToSelectedBoardItems: " + doc.toJson());
            SelectedBoardItem x = new SelectedBoardItem(doc);
            items.add(x);


        }
        if (DEBUG) Log.i(TAG, "convertDocsToSelectedBoardItems: Completed");

        if (DEBUG) Log.i(TAG, "convertDocsToSelectedBoardItems: " + items.toString());
        return items;
    }

    public ArrayList<Message> getDataSet() {
        return dataSet;
    }

    public Task<Void> refresh() {
        MongoClient.Collection boards = _mongoClient.getDatabase("data").getCollection("boardData");
        final SelectedBoardDataRepository ref = this;

        return boards.find(new Document("id", boardId), 1).continueWithTask(new Continuation<List<Document>, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull final Task<List<Document>> task) throws Exception {
                if (task.isSuccessful()) {
                    final List<Document> documents = task.getResult();
                    ArrayList<SelectedBoardItem> array = convertDocsToSelectedBoardItems(documents);
                    if (array.size() == 0) {
//                        parentActivity.findViewById(R.id.boardEmptyMessageView).setVisibility(View.VISIBLE);
                        if (DEBUG) Log.i(TAG, "then: No Messages on the Board");
                    } else {

                        SelectedBoardItem boardData = array.get(0);

                        if (metaDataSet != null) {
                            Calendar oldCal = metaDataSet.getLast_update();
                            Calendar newCal = boardData.getLast_update();
                            if (checkIfBoardIsOutDated(newCal, oldCal)) {
                                if (DEBUG) Log.i(TAG, "then: Data is outdated, update");
                                dataSet.clear();
                                dataSet.addAll(boardData.getMessages());
                                metaDataSet = boardData;
                                justUpdatedFlag_forScrollDown = true;

                            } else {

                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                                String timeShort = sdf.format(newCal.getTime());
                                if (DEBUG) Log.i(TAG, "Data is unchanged, retain old data Last updated on " + timeShort);

                            }

                        } else {
                            dataSet.clear();
                            dataSet.addAll(boardData.getMessages());
                            metaDataSet = boardData;

                        }


                        Log.d(TAG, "then: sfe" + dataSet.toString());
                    }


                    return Tasks.forResult(null);
                } else {
                    Log.e(TAG, "Error refreshing list", task.getException());
                    return Tasks.forException(task.getException());
                }
            }
        });
    }

    public boolean isJustUpdated() {
        if(justUpdatedFlag_forScrollDown)   {
            justUpdatedFlag_forScrollDown = false;
            return true;
        }
        return false;
    }

    public Task<Void> getRefreshTask() {
        return refreshTask;
    }

    public boolean checkIfBoardIsOutDated(Calendar newCal, Calendar oldCal) {
        Long newTimeInSec = ((Long) (newCal.getTimeInMillis() / 1000));
        Long curTimeInSec = ((Long) (oldCal.getTimeInMillis() / 1000));

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String timeShort = sdf.format(newCal.getTime());
        if (DEBUG) Log.i(TAG, "then: (new) Last updated on " + timeShort);
        timeShort = sdf.format(oldCal.getTime());
        if (DEBUG) Log.i(TAG, "then: (old) Last updated on " + timeShort);

        return newTimeInSec > curTimeInSec;
    }


    @Override
    public void onReady(StitchClient stitchClient) {
        this._client = stitchClient;

        _mongoClient = new MongoClient(_client, "mongodb-atlas");
        Log.d(TAG, "onReady: Got data for the boards");
        refreshTask = refresh();

    }
}