package com.edwinclement08.moodlev4.data.boards;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.edwinclement08.moodlev4.BoardActivity;
import com.edwinclement08.moodlev4.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class BoardsItemAdapter extends RecyclerView.Adapter<BoardsItemAdapter.ViewHolder> {
    private List<BoardsItem> mDataset;

    public String TAG = "BoardsItemAdapter";

    BoardsDataRepository dataRepository;

    private Context activityContext;

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


    // Provide a suitable constructor (depends on the kind of dataset)
    public BoardsItemAdapter(Context context, BoardsDataRepository dataRepository) {

        activityContext = context;
        this.dataRepository = dataRepository;


        updateDataset();
        mDataset = new ArrayList<BoardsItem>();
    }

    public Task<Void> updateDataset()  {
        final BoardsItemAdapter ref = this;

        return this.dataRepository.refresh().addOnSuccessListener(new OnSuccessListener<Void>() {
            public void onSuccess(Void x) {
                mDataset = ref.dataRepository.getDataSet();
                ref.notifyDataSetChanged();
            }
        });
    }


    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public BoardsItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
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
        final BoardsItem data = mDataset.get(position);
        View el = holder.boardItemView;
        ((TextView) el.findViewById(R.id.name)).setText(data.getName());
        ((TextView) el.findViewById(R.id.lastMessage)).setText(data.getTags().toString());
        el.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent()
                Log.i(TAG, "onClick: SelectedBoardItem Clicked with an ID of " + data.getId());
                Intent intent = new Intent(activityContext, BoardActivity.class);
                intent.setFlags(intent.getFlags()); // Adds the FLAG_ACTIVITY_NO_HISTORY flag
                intent.putExtra("id", data.getId());
                intent.putExtra("name", data.getName());
                activityContext.startActivity(intent);
            }
        });



    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}