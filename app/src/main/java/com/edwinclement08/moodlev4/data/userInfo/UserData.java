package com.edwinclement08.moodlev4.data.userInfo;

import android.support.annotation.NonNull;
import android.util.Log;

import com.edwinclement08.moodlev4.util.StitchClientListener;
import com.edwinclement08.moodlev4.util.StitchClientManager;
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
    public String TAG = "UserData";
    private Boolean DEBUG = true;

    private static UserData obj;

    private StitchClient _client;
    private MongoClient _mongoClient;

    private HashMap<String, HashMap<String,String>> idToUserMap;

    public HashMap<String,HashMap<String,String>> getIdToUserMap() {
        return idToUserMap;
    }


    private String picture;

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private String id;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    private ArrayList<String> boards;

    public ArrayList<String> getBoards() {
        return boards;
    }

    public void setBoards(ArrayList<String> boards) {
        this.boards = boards;
    }


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


    public MongoClient.Collection getUserDataCollection() {
        return _mongoClient.getDatabase("data").getCollection("userInfo");
    }


    @Override
    public void onReady(StitchClient stitchClient) {
        _client = stitchClient;
        id = _client.getUserId();
        _mongoClient = new MongoClient(_client, "mongodb-atlas");
        Log.d(TAG, "onReady: Got conn for UserData");
//        refreshTask = refresh();
        initializeIdToUseMap();

    }

    public Task<String> synchronize() {
        final Document doc = new Document();
        doc.put("owner_id", getId());

        return getUserDataCollection().find(doc, 10).continueWithTask(new Continuation<List<Document>, Task<String>>() {
            @Override
            public Task<String> then(@NonNull Task<List<Document>> task) throws Exception {
                if (task.isSuccessful()) {
                    final List<Document> documents = task.getResult();
                    if (documents.size() == 1) {
                        if (DEBUG)
                            Log.i(TAG, "then: There is a previously Existing user" + documents.get(0).toJson());
                        return Tasks.forResult("Success");
                    } else if (documents.size() > 1) {
                        Log.e(TAG, "then: WE HAVE TWO USERS WITH SAME ID");
                        return Tasks.forResult("Failure");
                    } else {
                        if (DEBUG) Log.i(TAG, "then: no user with that id exist yet.");
                        saveUserData();
                        return Tasks.forResult("Failure");
                    }

                } else {
                    Log.e(TAG, "Error refreshing list", task.getException());
                    return Tasks.forException(task.getException());
                }
            }
        });
    }


    public void saveUserData() {
        final Document doc = new Document();
        doc.put("owner_id", getId());
        doc.put("name", getName());
        doc.put("email", getEmail());
        doc.put("picture", getPicture());
        doc.put("boards", new ArrayList<String>());

        getUserDataCollection().insertOne(doc).addOnCompleteListener(new OnCompleteListener<Document>() {
            @Override
            public void onComplete(@NonNull final Task<Document> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "Saving User to DB Successful");
                } else {
                    Log.e(TAG, "Error adding item", task.getException());
                }
            }
        });

    }

    private void initializeIdToUseMap() {
        final Document select = new Document();
        final Document project = new Document();
        project.put("owner_id", 1);
        project.put("name", 1);
        project.put("email", 1);
        project.put("picture", 1);

        getUserDataCollection().find(select, project, 1000).continueWithTask(new Continuation<List<Document>, Task<String>>() {
            @Override
            public Task<String> then(@NonNull final Task<List<Document>> task) throws Exception {
                if (task.isSuccessful()) {
                    final List<Document> documents = task.getResult();
                    if (documents.size() > 0) {
                        if (DEBUG) Log.i(TAG, "then: got " + documents.size() + " user details");

                        for (Document d : documents) {
                            String id = d.getString("owner_id");
                            String name = d.getString("name");
                            String email = d.getString("email");
                            String picture = d.getString("picture");
                            HashMap<String, String> hmap = new HashMap<>();
                            hmap.put("owner_id", id);
                            hmap.put("name", name);
                            hmap.put("email", email);
                            hmap.put("picture", picture);
                            idToUserMap.put(id, hmap);
                        }

                        return Tasks.forResult("Success");
                    } else {
                        if (DEBUG) Log.i(TAG, "then: no users exist yet");
                        return Tasks.forResult("Failure");
                    }

                } else {
                    Log.e(TAG, "Error refreshing list", task.getException());
                    return Tasks.forException(task.getException());
                }
            }
        });
    }

//
//    public Task<String> checkUserData() {
//        final Document doc = new Document();
//        doc.put("owner_id", _client.getUserId());
//
//        return getUserDataCollection().find(doc, 1).continueWithTask(new Continuation<List<Document>, Task<String>>() {
//            @Override
//            public Task<String> then(@NonNull Task<List<Document>> task) throws Exception {
//                if (task.isSuccessful()) {
//                    final List<Document> documents = task.getResult();
//                    if(documents.size() > 0) {
//                        Log.d(TAG, "then: There is a previously Existing user" + documents.get(0).toJson());
//
//                        return Tasks.forResult("Success");
//                    } else {
//                        Log.i(TAG, "then: no user with that id exist yet, saving the user details-");
//                        saveUserData();
//                        return Tasks.forResult("Failure");
//                    }
//
//                } else {
//                    Log.e(TAG, "Error refreshing list", task.getException());
//                    return Tasks.forException(task.getException());
//                }
//            }
//        });
//
//    }


    public Task<String> getUserData() {
        MongoClient.Collection userInfo = getUserDataCollection();

        final UserData ref = this;


        Task<String> task = userInfo.find(new Document("owner_id", _client.getUserId()), 1).continueWithTask(new Continuation<List<Document>, Task<String>>() {
            @Override
            public Task<String> then(@NonNull final Task<List<Document>> task) throws Exception {
                if (task.isSuccessful()) {
                    final List<Document> documents = task.getResult();
                    if (documents.size() > 0) {
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


