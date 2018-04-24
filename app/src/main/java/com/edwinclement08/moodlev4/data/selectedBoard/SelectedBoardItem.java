package com.edwinclement08.moodlev4.data.selectedBoard;

import android.util.Log;

import com.edwinclement08.moodlev4.data.Message;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class SelectedBoardItem {

    private String TAG = "SelectedBoardItem";
    private Boolean DEBUG = false;
    private ObjectId objectId;

    private String owner_id;
    private String id;
    private Calendar last_update;
    public ArrayList<Message> messages;
    public ArrayList<String> admins;

    public String getId()   {return id;}
    public String getOwner_id() {return owner_id;}
    public Calendar getLast_update() {return last_update;}
    public ArrayList<String> getAdmins() {return admins;}
    public ArrayList<Message> getMessages() {return messages;}


    public SelectedBoardItem(final Document document) {

        messages = new ArrayList<>();
        admins = new ArrayList<>();
//        objectId = document.getObjectId("_id");
//        objectId = document.getObjectId("_id");
        owner_id = document.getString("owner_id");
        admins = document.get("admins", ArrayList.class);

        id = document.getString("id");


        last_update = Calendar.getInstance(TimeZone.getTimeZone("India"));

        String last_update = document.getString("last_update");
        if(last_update.equals(""))  {
            last_update = "0";
        }
        this.last_update.setTimeInMillis( Long.parseLong(last_update) * 1000L);


        final ArrayList<Document> documents  = document.get("messages", ArrayList.class);

        if(DEBUG) Log.i(TAG, "SelectedBoardItem: going to parse messages");
        ArrayList<Message> p =convertDocsToMessages(documents);
        if(DEBUG) Log.i(TAG, "SelectedBoardItem: parse complete");

        for( Message x : p) {
            if(DEBUG) Log.i(TAG, "SelectedBoardItem: tset loop");
            if(DEBUG) Log.i(TAG, "SelectedBoardItem: " + x.toString());
            messages.add(x);
            if(DEBUG) Log.i(TAG, "SelectedBoardItem: its the add");

        }
//        messages.addAll(p);
        if(DEBUG) Log.i(TAG, "SelectedBoardItem: created an object");

    }

    private ArrayList<Message> convertDocsToMessages(final List<Document> documents) {
        if(DEBUG) Log.i(TAG, "convertDocsToMessages: arrival Count = " + documents.toString() );
        final ArrayList<Message> items = new ArrayList<>(documents.size());
        for (final Document doc : documents) {
            if(DEBUG) Log.i(TAG, "convertDocsToMessages: " + documents.toString());
            items.add(new Message(doc));
        }
        if(DEBUG) Log.i(TAG, "convertDocsToMessages: Parsing of Object complete");
        if(DEBUG) Log.i(TAG, "convertDocsToMessages: " + items.toString());
        return items;
    }


}