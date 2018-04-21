package com.edwinclement08.moodlev4;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.mongodb.stitch.android.StitchClient;
import com.mongodb.stitch.android.services.mongodb.MongoClient;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class BoardItemAdapter extends RecyclerView.Adapter<BoardItemAdapter.ViewHolder> implements  StitchClientListener{
    private List<BoardItem> mDataset;
    private StitchClient _client;
    private MongoClient _mongoClient;
    public String TAG = "BoardAdapter";

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View boardItemView;

        public ViewHolder(View v) {
            super(v);
            boardItemView = v;
        }
    }

    public class BoardItem {
        private ObjectId objectId;
        private String name;
        private List<String> tags;

        public BoardItem(final Document document) {
            objectId = document.getObjectId("_id");
            name = document.getString("name");
            tags = (List<String>)document.get("tags");
            Log.e(TAG, "BoardItem: " +  tags.toString() );
        }

        public String getName()    {
            return name;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public BoardItemAdapter() {
        StitchClientManager.registerListener(this);
        mDataset = new ArrayList<BoardItem>();
    }


    private List<BoardItem> convertDocsToBoards(final List<Document> documents) {
        final List<BoardItem> items = new ArrayList<>(documents.size());
        for (final Document doc : documents) {
            items.add(new BoardItem(doc));
        }
        return items;
    }

    @Override
    public void onReady(StitchClient stitchClient) {
        this._client = stitchClient;

        _mongoClient = new MongoClient(_client, "mongodb-atlas");
        Log.e(TAG, "onReady: Got data for the boards");

        MongoClient.Collection boards = _mongoClient.getDatabase("data").getCollection("boards");

        final BoardItemAdapter ref = this;


        boards.find(new Document(), 100).continueWithTask(new Continuation<List<Document>, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull final Task<List<Document>> task) throws Exception {
                if (task.isSuccessful()) {
                    final List<Document> documents = task.getResult();
                    mDataset.addAll(convertDocsToBoards(documents));
                    Log.e(TAG, "then: sfe" + mDataset.toString());
                    ref.notifyDataSetChanged();
                    return Tasks.forResult(null);
                } else {
                    Log.e(TAG, "Error refreshing list", task.getException());
                    return Tasks.forException(task.getException());
                }
            }
        });
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public BoardItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                          int viewType) {
        // create a new view
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.board_tab_item_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        BoardItem data = mDataset.get(position);
        View el = holder.boardItemView;
        ((TextView) el.findViewById(R.id.name)).setText(data.name);
        ((TextView) el.findViewById(R.id.lastMessage)).setText(data.tags.toString());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}