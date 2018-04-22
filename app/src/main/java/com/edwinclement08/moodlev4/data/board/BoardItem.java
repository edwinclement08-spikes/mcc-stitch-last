package com.edwinclement08.moodlev4.data.board;

import android.util.Log;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;

public class BoardItem {
    private String TAG = "BoardItem";
    private ObjectId objectId;
    private String name;

    public List<String> getTags() {
        return tags;
    }

    private List<String> tags;
    private String id;

    public BoardItem(final Document document) {
        objectId = document.getObjectId("_id");
        name = document.getString("name");
        tags = (List<String>)document.get("tags");
        id = document.getString("id");
        Log.d(TAG, "BoardItem: " +  tags.toString() );
    }

    public String getName()    {
        return name;
    }

    public String getId()   {return id;}
}