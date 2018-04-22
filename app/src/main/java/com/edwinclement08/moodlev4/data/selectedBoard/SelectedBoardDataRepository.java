package com.edwinclement08.moodlev4.data.selectedBoard;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.edwinclement08.moodlev4.R;
import com.edwinclement08.moodlev4.StitchClientListener;
import com.edwinclement08.moodlev4.StitchClientManager;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.mongodb.stitch.android.StitchClient;
import com.mongodb.stitch.android.services.mongodb.MongoClient;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class SelectedBoardDataRepository implements StitchClientListener {
    public String TAG = "SelectedBoardDataRepository";

    private StitchClient _client;
    private MongoClient _mongoClient;

    private String boardId;
    private Task<Void> refreshTask;

    private Activity parentActivity;


    public ArrayList<SelectedBoardItem> metaDataSet = null;
    public ArrayList<SelectedBoardItem.Message> dataSet = null;

    public SelectedBoardDataRepository(String boardId, Activity parentActivity) {
        this.boardId = boardId;
    this.parentActivity = parentActivity;
        dataSet = new ArrayList<SelectedBoardItem.Message>();
        metaDataSet  = new ArrayList<SelectedBoardItem>();

        StitchClientManager.registerListener(this);

    }

    private ArrayList<SelectedBoardItem> convertDocsToSelectedBoardItems(final List<Document> documents) {
        Log.i(TAG, "convertDocsToSelectedBoardItems: arrival Count = " + documents.toString());
        final ArrayList<SelectedBoardItem> items = new ArrayList<>(documents.size());
        Log.i(TAG, "convertDocsToSelectedBoardItems: Documents number " + documents.size());
        for (final Document doc : documents) {
            Log.i(TAG, "convertDocsToSelectedBoardItems: " + doc.toJson());
            SelectedBoardItem x = new SelectedBoardItem(doc);
            items.add(x);


        }
        Log.i(TAG, "convertDocsToSelectedBoardItems: Completed");

        Log.i(TAG, "convertDocsToSelectedBoardItems: "+ items.toString());
        return items;
    }

    public ArrayList<SelectedBoardItem.Message> getDataSet() {
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
                    dataSet.clear();
                    ArrayList<SelectedBoardItem> array = convertDocsToSelectedBoardItems(documents);
                    if(array.size() == 0)   {
                        parentActivity.findViewById(R.id.boardEmptyMessageView).setVisibility(View.VISIBLE);

                    } else {
                        SelectedBoardItem boardData = array.get(0);
                        Log.i(TAG, "then: return data" + boardData.toString());
                        dataSet.addAll(boardData.getMessages());
                        metaDataSet.add(boardData);
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

    public Task<Void> getRefreshTask() {
        return refreshTask;
    }



    @Override
    public void onReady(StitchClient stitchClient) {
        this._client = stitchClient;

        _mongoClient = new MongoClient(_client, "mongodb-atlas");
        Log.d(TAG, "onReady: Got data for the boards");
        refreshTask = refresh();

    }
}