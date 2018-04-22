package com.edwinclement08.moodlev4.data.boards;

import android.support.annotation.NonNull;
import android.util.Log;

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

public class BoardsDataRepository implements StitchClientListener {
    public String TAG = "BoardsDataRepository";

    private StitchClient _client;
    private MongoClient _mongoClient;


    public ArrayList<BoardsItem> dataSet = null;

    public BoardsDataRepository()   {

        dataSet = new ArrayList<BoardsItem>();

        StitchClientManager.registerListener(this);

    }

    private List<BoardsItem> convertDocsToBoards(final List<Document> documents) {
        final List<BoardsItem> items = new ArrayList<>(documents.size());
        for (final Document doc : documents) {
            items.add(new BoardsItem(doc));
        }
        return items;
    }

    public ArrayList<BoardsItem> getDataSet() {
            return dataSet;
    }

    public Task<Void> refresh()    {
        MongoClient.Collection boards = _mongoClient.getDatabase("data").getCollection("boards");

        final BoardsDataRepository ref = this;

        return boards.find(new Document(),500).continueWithTask(new Continuation<List<Document>, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull final Task<List<Document>> task) throws Exception {
                if (task.isSuccessful()) {
                    final List<Document> documents = task.getResult();
                    dataSet.clear();
                    dataSet.addAll(convertDocsToBoards(documents));
                    Log.d(TAG, "then: sfe" + dataSet.toString());


                    return Tasks.forResult(null);
                } else {
                    Log.e(TAG, "Error refreshing list", task.getException());
                    return Tasks.forException(task.getException());
                }
            }
        });
    }

    @Override
    public void onReady(StitchClient stitchClient) {
        this._client = stitchClient;

        _mongoClient = new MongoClient(_client, "mongodb-atlas");
        Log.d(TAG, "onReady: Got data for the boards");
        refresh();
    }
}