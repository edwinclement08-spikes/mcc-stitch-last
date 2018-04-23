package com.edwinclement08.moodlev4;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.edwinclement08.moodlev4.data.selectedBoard.SelectedBoardDataRepository;
import com.edwinclement08.moodlev4.data.selectedBoard.SelectedBoardItem;
import com.edwinclement08.moodlev4.data.selectedBoard.SelectedBoardItemAdapter;
import com.edwinclement08.moodlev4.data.userInfo.UserData;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class SelectedBoardFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    public String titleName = "Boards";             // TODO change this to the Board Name

    public int ACTIVITY_CHOOSE_FILE = 234;

    SelectedBoardDataRepository selectedBoardDataRepository;
    public String TAG = "SelectedBoardFragment";


    @NonNull
    @Override
    public LayoutInflater onGetLayoutInflater(@Nullable Bundle savedInstanceState) {
        return super.onGetLayoutInflater(savedInstanceState);
    }



    public void onBrowse(View view) {
        Intent chooseFile;
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("*/ *");
        intent = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        String path     = "";
        if(requestCode == ACTIVITY_CHOOSE_FILE)
        {
            Uri uri = data.getData();

            String FilePath = getRealPathFromURI(uri); // should the path be here in this string
            Log.i(TAG, "onActivityResult: path" + uri);
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String [] proj      = {MediaStore.Images.Media.DATA};
        Cursor cursor       = getActivity().getContentResolver().query( contentUri, proj, null, null,null);
        if (cursor == null) return null;
        int column_index    = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.selected_board_page, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.selected_board_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout_selected_board);
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);


        try {
            Intent intent = getActivity().getIntent();
            String id = intent.getStringExtra("id");
            String name = intent.getStringExtra("name");
            getActivity().setTitle(name);

            Log.i(TAG, "onCreateView: Intent requests a page for ID = " + id);
            // specify an adapter (see also next example)
            selectedBoardDataRepository = new SelectedBoardDataRepository(id, getActivity());

        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: Not Called by Intent", e);
            selectedBoardDataRepository = new SelectedBoardDataRepository("", getActivity());

        }
        Task<Void> x = selectedBoardDataRepository.getRefreshTask();
        x.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "onSuccess: Trying to set visibility");
                ArrayList<SelectedBoardItem.Message> p = selectedBoardDataRepository.getDataSet();
                if (p.size() == 0) {
                    getActivity().findViewById(R.id.boardEmptyMessageView).setVisibility(View.VISIBLE);
                }

                SelectedBoardItem metaDataSet =  selectedBoardDataRepository.getMetaDataSet();
                String owner_id = metaDataSet.getOwner_id();

                // Check if current user is the owner
                UserData userData = UserData.getInstance();
                String userId = userData.getId();
                Log.i(TAG, "onSuccess: UserID " + userId );
                Log.i(TAG, "onSuccess: Owner " + owner_id);
                if(userId.equals(owner_id)) {
                    getActivity().findViewById(R.id.message_enter_section).setVisibility(View.VISIBLE);
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

        view.findViewById(R.id.attachFilesButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBrowse(v);
            }
        });



        return view;
    }

    void refreshItems() {

        ((SelectedBoardItemAdapter) mAdapter).updateDataset().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                onItemsLoadComplete();
            }
        });
    }

    void onItemsLoadComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
    }
//
//    @Override
//    public String getTitle() {
//        return "Boards";
//    }


}
