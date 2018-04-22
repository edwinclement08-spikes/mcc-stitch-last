package com.edwinclement08.moodlev4.data.userInfo;

import android.support.annotation.NonNull;
import android.util.Log;

import com.edwinclement08.moodlev4.StitchClientListener;
import com.edwinclement08.moodlev4.StitchClientManager;
import com.edwinclement08.moodlev4.data.boards.BoardsDataRepository;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.mongodb.stitch.android.StitchClient;
import com.mongodb.stitch.android.services.mongodb.MongoClient;

import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserData implements StitchClientListener {
    private static UserData obj;

    private StitchClient _client;
    private MongoClient _mongoClient;

    private HashMap<String, ArrayList<String>> idToUserMap;


    public String TAG = "UserData";

    // private constructor to force use of
    // getInstance() to create Singleton object
    private UserData() {
    }

    public static UserData getInstance() {
        if (obj == null) {
            obj = new UserData();
            obj.idToUserMap = new HashMap<>();

            StitchClientManager.registerListener(obj);
        }

        return obj;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ArrayList<String> getBoards() {
        return boards;
    }

    public void setBoards(ArrayList<String> boards) {
        this.boards = boards;
    }

    private String name;
    private String email;

    public String getId() {
        return id;
    }

    private String id;

    private ArrayList<String> boards;


    @Override
    public void onReady(StitchClient stitchClient) {
        _client = stitchClient;
        id = _client.getUserId();
        _mongoClient=new MongoClient(_client, "mongodb-atlas");
        Log.d(TAG, "onReady: Got conn for UserData");
//        refreshTask = refresh();
        initializeIdToUseMap();

    }

    public MongoClient.Collection getUserDataCollection()   {
        return  _mongoClient.getDatabase("data").getCollection("userInfo");
    }

    public void saveUserData()    {
        final Document doc = new Document();
        doc.put("owner_id", _client.getUserId());
        doc.put("name", name);
        doc.put("email", email);
        doc.put("boards", new ArrayList<String>());

        getUserDataCollection().insertOne(doc).addOnCompleteListener(new OnCompleteListener<Document>() {
            @Override
            public void onComplete(@NonNull final Task<Document> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "onComplete: User Addition Successful");
                } else {
                    Log.e(TAG, "Error adding item", task.getException());
                }
            }
        });

    }


    public HashMap<String, ArrayList<String>> getIdToUserMap() {
        return idToUserMap;
    }

    public void initializeIdToUseMap()  {
        final Document select = new Document();
        final Document project = new Document();
        project.put("owner_id",1 );
        project.put("name", 1);
        project.put("email", 1);

        Task<String> task  =  getUserDataCollection()
                .find(select, project,1).continueWithTask(new Continuation<List<Document>, Task<String>>() {
            @Override
            public Task<String> then(@NonNull final Task<List<Document>> task) throws Exception {
                if (task.isSuccessful()) {
                    final List<Document> documents = task.getResult();
                    if(documents.size() > 0) {
                        Log.d(TAG, "then: got Result" + documents.get(0).toJson());

                        for(Document d: documents)  {
                            String id = d.getString("owner_id");
                            String name = d.getString("name");
                            String email = d.getString("email");
                            ArrayList<String> arr = new ArrayList<>();
                            arr.add(name);
                            arr.add(email);
                            idToUserMap.put(id, arr);
                        }

                        return Tasks.forResult("Success");
                    } else {
                        Log.i(TAG, "then: no users exist yet");
                        return Tasks.forResult("Failure");
                    }

                } else {
                    Log.e(TAG, "Error refreshing list", task.getException());
                    return Tasks.forException(task.getException());
                }
            }
        });
    }





    public  Task<String> getUserData()   {
        MongoClient.Collection userInfo = getUserDataCollection();

        final UserData ref = this;


        Task<String> task  =  userInfo.find(new Document("owner_id", _client.getUserId()),1).continueWithTask(new Continuation<List<Document>, Task<String>>() {
            @Override
            public Task<String> then(@NonNull final Task<List<Document>> task) throws Exception {
                if (task.isSuccessful()) {
                    final List<Document> documents = task.getResult();
                    if(documents.size() > 0) {
                        Log.d(TAG, "then: got Result" + documents.get(0).toJson());
                        return Tasks.forResult("Success");

                    } else {
                        Log.i(TAG, "then: no userData yet");
                        return Tasks.forResult("Failure");
                    }

                } else {
                    Log.e(TAG, "Error refreshing list", task.getException());
                    return Tasks.forException(task.getException());
                }
            }
        });



        return task;

    }
}


