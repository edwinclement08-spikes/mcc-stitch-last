package com.edwinclement08.moodlev4.data;

import org.bson.Document;

import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.UUID;

public class Message {


    private UUID uuid;
    private Calendar time;

    private String author;
    private HashMap<String, Object> data;

    public Message(final Document document)  {
        data = new HashMap<>();

        uuid = UUID.fromString(document.getString("uuid"));

        author = document.getString("author");
        time = Calendar.getInstance(TimeZone.getTimeZone("India"));

        time.setTimeInMillis( Long.parseLong(document.getString("time")) * 1000L);

        Document dataDocument = document.get("data",Document.class);

        if(dataDocument.containsKey("text"))    {
            data.put("text",dataDocument.getString("text"));
        }
    }

    public Message()  {
        data = new HashMap<>();
    }


    public boolean hasMessageText()    {
        return data.containsKey("text");
    }

    public String getMessageText()   {
        return data.get("text").toString();
    }

    public void setMessageText(String text)    {
        data.put("text", text);
    }

    public Document getDocument()    {
        Document doc = new Document();


        String uuid_doc = uuid.toString();
        // author
        String time_doc = ((Long) (time.getTimeInMillis()/ 1000)).toString();

        Document data_doc = new Document();
        if(data.containsKey("text"))    {
            data_doc.put("text",data.get("text"));
        }
        doc.put("uuid", uuid_doc);
        doc.put("author", author);
        doc.put("time", time_doc);
        doc.put("data", data_doc);


        return doc;
    }

    public Calendar getTime()   {
        return time;
    }
    public String getAuthor() {
        return author;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setTime(Calendar time) {
        this.time = time;
    }

    public void setAuthor(String author) {
        this.author = author;
    }


}