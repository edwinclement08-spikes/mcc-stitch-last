package com.edwinclement08.moodlev4.data.boards;

import android.util.Log;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;

public class BoardsItem {
    private String TAG = "SelectedBoardItem";
    private ObjectId objectId;
    private String name;

    public List<String> getTags() {
        return tags;
    }

    private List<String> tags;
    private String id;

    public BoardsItem(final Document document) {
        objectId = document.getObjectId("_id");
        name = document.getString("name");
        tags = (List<String>)document.get("tags");
        id = document.getString("id");
        Log.d(TAG, "SelectedBoardItem: " +  tags.toString() );
    }

    public String getName()    {
        return name;
    }

    public String getId()   {return id;}
}