package com.edwinclement08.moodlev4.data;

import android.util.Log;

import com.edwinclement08.moodlev4.data.boards.BoardsListFragment;
import com.edwinclement08.moodlev4.util.StitchClientListener;
import com.edwinclement08.moodlev4.util.StitchClientManager;
import com.google.android.gms.tasks.Task;
import com.mongodb.stitch.android.StitchClient;
import com.mongodb.stitch.android.services.mongodb.MongoClient;

import org.bson.Document;

public class MessageTransfer implements StitchClientListener {
    private static MessageTransfer _instance;
    public boolean DEBUG = true;
    public String TAG = "MessageTransfer";

    private StitchClient _client;
    private MongoClient _mongoClient;


    private MessageTransfer() {
        StitchClientManager.registerListener(this);
    }

    public static MessageTransfer getInstance() {
        if (_instance == null) {
            _instance = new MessageTransfer();
        }
        return _instance;
    }

    // Task<String>
    public void sendMessageToBoard(Message message, String boardId) {
        MongoClient.Collection boardData = getDatabase().getCollection("boardData");

        Document messageDoc = message.getDocument();

        Document select = new Document();
        select.put("id", boardId);

        Document update = new Document();
        update.put("$push", new Document("messages", messageDoc));
        update.put("$set", new Document("last_update", messageDoc.getString("time")));


        boardData.updateOne(select, update);

    }

    public MongoClient.Database getDatabase() {
        MongoClient.Database db = _mongoClient.getDatabase("data");
        return db;
    }

    @Override
    public void onReady(StitchClient stitchClient) {
        this._client = stitchClient;
        _mongoClient = new MongoClient(_client, "mongodb-atlas");
        if (DEBUG) Log.i(TAG, "onReady: StitchClient received in MessageTransfer");
    }
}
