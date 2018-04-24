package com.edwinclement08.moodlev4.data.selectedBoard;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.edwinclement08.moodlev4.FileManager.ExternalStorageManager;
import com.edwinclement08.moodlev4.R;
import com.edwinclement08.moodlev4.data.Message;
import com.edwinclement08.moodlev4.data.MessageTransfer;
import com.edwinclement08.moodlev4.util.StitchClientListener;
import com.edwinclement08.moodlev4.util.StitchClientManager;
import com.edwinclement08.moodlev4.data.userInfo.UserData;
import com.edwinclement08.moodlev4.util.UIUpdater;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mongodb.stitch.android.StitchClient;
import com.mongodb.stitch.android.services.mongodb.MongoClient;


import org.bson.Document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class SelectedBoardFragment extends Fragment implements StitchClientListener {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    public String titleName = "Boards";             // TODO change this to the Board Name

    public int ACTIVITY_CHOOSE_FILE = 234;

    SelectedBoardDataRepository selectedBoardDataRepository;
    public String TAG = "SelectedBoardFragment";
    private Boolean DEBUG = false;

    StitchClient _client;
    MongoClient _mongoClient;

    UIUpdater uiUpdater;

    @NonNull
    @Override
    public LayoutInflater onGetLayoutInflater(@Nullable Bundle savedInstanceState) {
        return super.onGetLayoutInflater(savedInstanceState);
    }

    private int PICKFILE_REQUEST_CODE = 123;

    private View fragmentView;


    public void onBrowse(View view) {
        Intent chooseFile;
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("*/*");
        intent = Intent.createChooser(chooseFile, "Choose a file");

        Intent intent2 = new Intent(Intent.ACTION_GET_CONTENT);
        intent2.setType("*/*");
        startActivityForResult(intent2, PICKFILE_REQUEST_CODE);


//        startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    public void sendFileUpload(byte[] data) {


        MongoClient.Collection files = _mongoClient.getDatabase("data").getCollection("files");

        final Document doc = new Document();
        doc.put("owner_id", _client.getUserId());
        try {
            String encoded = Base64.encodeToString(data,Base64.DEFAULT);
            doc.put("bytes", encoded);

//            ArrayList<Byte> byteObjects = new ArrayList<>(data.length);
//
//            for(byte b: data)
//                byteObjects.add(b);  // Autoboxing.
//            doc.put("bytes", byteObjects);
        } catch (Exception e) {
            Log.e(TAG, "sendFileUpload: ", e);
        }

        files.insertOne(doc).addOnCompleteListener(new OnCompleteListener<Document>() {
            @Override
            public void onComplete(@NonNull final Task<Document> task) {
                if (task.isSuccessful()) {
                    if(DEBUG) Log.i(TAG, "onComplete: File Upload Successful");
                } else {
                    Log.e(TAG, "Error adding item", task.getException());
                }
            }
        });


    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        String path = "";
        if (requestCode == ACTIVITY_CHOOSE_FILE) {
            Uri uri = data.getData();

            String FilePath = getRealPathFromURI(uri); // should the path be here in this string
            if(DEBUG) Log.i(TAG, "onActivityResult: path" + uri);
            if(DEBUG) Log.i(TAG, "onActivityResult: path" + FilePath);
        } else if (requestCode == PICKFILE_REQUEST_CODE) {
            Uri uri = data.getData();

            if (uri != null) {
                if(DEBUG) Log.i(TAG, "onActivityResult: path" + uri);
                try {
                    InputStream pfd = getActivity().getContentResolver().openInputStream(uri);
                    if (pfd != null) {
                        if(DEBUG) Log.i(TAG, "onActivityResult: available " + pfd.available());
                        byte[] fileData = new byte[pfd.available()];


                        pfd.read(fileData, 0, pfd.available());
//                        if(DEBUG) Log.i(TAG, "onActivityResult: " + new String(fileData));
                        sendFileUpload(fileData);

                    }
//
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "onActivityResult: FILE NOT FOUND", e);
                } catch (IOException e) {
                    Log.e(TAG, "onActivityResult: FILE NOT IOException", e);
                }

//                try {
//                    File file = new File(uri.getPath());
//                    BufferedReader br = new BufferedReader(new FileReader(file));
//
//                    String st;
//                    while ((st = br.readLine()) != null)
//                        System.out.println(st);
//                }  catch (FileNotFoundException f)   {
//                    Log.e(TAG, "onActivityResult: URI does not point to a valid file", f );
//                } catch (IOException e) {
//                    Log.e(TAG, "onActivityResult: IOException", e);
//                }
            } else {
                if(DEBUG) Log.i(TAG, "onActivityResult: No Item Selected");
            }


//            String FilePath = getRealPathFromURI(uri); // should the path be here in this string
//            if(DEBUG) Log.i(TAG, "onActivityResult: path" + FilePath);
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    private String boardId;
    private String boardName;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.selected_board_page, container, false);
        StitchClientManager.registerListener(this);

        uiUpdater = new UIUpdater(new Runnable() {
            @Override
            public void run() {
                refreshItems();
            }
        });


        mRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.selected_board_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) fragmentView.findViewById(R.id.swipeRefreshLayout_selected_board);
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        try {
            Intent intent = getActivity().getIntent();
            boardId = intent.getStringExtra("id");
            boardName = intent.getStringExtra("name");
            getActivity().setTitle(boardName);

            if(DEBUG) Log.i(TAG, "onCreateView: Intent requests a page for ID = " + boardId);
            // specify an adapter (see also next example)

        } catch (NullPointerException e) {
            boardId = "";
            boardName = "";
            Log.e(TAG, "onCreateView: Not Called by Intent", e);
            getActivity().finish();
        }

        selectedBoardDataRepository = new SelectedBoardDataRepository(boardId, getActivity());

        Task<Void> x = selectedBoardDataRepository.getRefreshTask();
        x.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if(DEBUG) Log.i(TAG, "onSuccess: Trying to set visibility");
                ArrayList<Message> p = selectedBoardDataRepository.getDataSet();
                if (p.size() == 0) {
                    fragmentView.findViewById(R.id.boardEmptyMessageView).setVisibility(View.VISIBLE);
                }

                SelectedBoardItem metaDataSet = selectedBoardDataRepository.getMetaDataSet();
                String owner_id = metaDataSet.getOwner_id();

                // Check if current user is the owner
                UserData userData = UserData.getInstance();
                String userId = userData.getId();
                if(DEBUG) Log.i(TAG, "isUserTheOwner: UserID " + userId);
                if(DEBUG) Log.i(TAG, "isUserTheOwner:  Owner " + owner_id);
                if (userId.equals(owner_id)) {
                    if(DEBUG) Log.i(TAG, "isUserTheOwner:  True");

                    fragmentView.findViewById(R.id.message_enter_section).setVisibility(View.VISIBLE);
                 } else {
                    if(DEBUG) Log.i(TAG, "isUserTheOwner:  False");

                }

            }
        });


        mAdapter = new SelectedBoardItemAdapter(getContext(), selectedBoardDataRepository);
        mRecyclerView.setAdapter(mAdapter);


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });

        fragmentView.findViewById(R.id.attachFilesButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBrowse(v);
            }
        });

        fragmentView.findViewById(R.id.sendMessageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                onBrowse(v);
                MessageTransfer mt = MessageTransfer.getInstance();
                Message message = new Message();

                message.setUuid(UUID.randomUUID());
                message.setAuthor(UserData.getInstance().getId());
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("India"));
                message.setTime(cal);
                TextView editBox = (TextView) fragmentView.findViewById(R.id.message_edit_box);
                String text = editBox.getText().toString();
                message.setMessageText(text);


                mt.sendMessageToBoard(message, boardId);
                editBox.setText("");
            }
        });

        ExternalStorageManager esm = ExternalStorageManager.getManager();

        uiUpdater.startUpdates();
        return fragmentView;
    }

    void refreshItems() {
        ((SelectedBoardItemAdapter) mAdapter).updateDataset().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                onItemsLoadComplete();

                if(DEBUG) Log.i(TAG, "onSuccess: Trying to set visibility");
                ArrayList<Message> p = selectedBoardDataRepository.getDataSet();
                if (p.size() == 0) {
                    fragmentView.findViewById(R.id.boardEmptyMessageView).setVisibility(View.VISIBLE);
                }

            }
        });
    }

    void onItemsLoadComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
    }


    @Override
    public void onReady(StitchClient stitchClient) {
        this._client = stitchClient;

        _mongoClient = new MongoClient(_client, "mongodb-atlas");
        Log.d(TAG, "onReady: Got data for the selectedBoardFragment");
//        refreshTask = refresh();

    }


}
