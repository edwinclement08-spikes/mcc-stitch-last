package com.edwinclement08.moodlev4.data.selectedBoard;

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
import com.edwinclement08.moodlev4.data.userInfo.UserData;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SelectedBoardItemAdapter extends RecyclerView.Adapter<SelectedBoardItemAdapter.ViewHolder> {
    private ArrayList<SelectedBoardItem.Message> mDataset;

    public String TAG = "SelectedBoardItemAdapter";

    SelectedBoardDataRepository dataRepository;
    UserData userData;

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
    public SelectedBoardItemAdapter(Context context, SelectedBoardDataRepository dataRepository) {

        activityContext = context;
        this.dataRepository = dataRepository;
        userData = UserData.getInstance();

        mDataset = new ArrayList<SelectedBoardItem.Message>();

        updateDataset();
    }

    public Task<Void> updateDataset()  {
        final SelectedBoardItemAdapter ref = this;

        return this.dataRepository.refresh().addOnSuccessListener(new OnSuccessListener<Void>() {
            public void onSuccess(Void x) {
                mDataset = ref.dataRepository.getDataSet();
                Log.i(TAG, "onSuccess: data from repo received");
                ref.notifyDataSetChanged();
            }
        });
    }


    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public SelectedBoardItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                  int viewType) {
        // create a new view
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.board_message_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final SelectedBoardItem.Message data = mDataset.get(position);
        View el = holder.boardItemView;

        String authorName = userData.getIdToUserMap().get(data.getAuthor()).get(0);
        Log.i(TAG, "onBindViewHolder: " + authorName);
        ((TextView) el.findViewById(R.id.author)).setText(authorName);

        if(data.hasMessageText())  ((TextView) el.findViewById(R.id.message)).setText(data.getMessageText());

        Calendar time = data.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String timeShort = sdf.format(time.getTime());

        ((TextView) el.findViewById(R.id.time)).setText(timeShort);




    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}