package com.edwinclement08.moodlev4;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class SelectedBoardFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    public String titleName = "Boards";             // TODO change this to the Board Name


    SelectedBoardDataRepository selectedBoardDataRepository;
    public String TAG = "SelectedBoardFragment";


    @NonNull
    @Override
    public LayoutInflater onGetLayoutInflater(@Nullable Bundle savedInstanceState) {
        return super.onGetLayoutInflater(savedInstanceState);
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
//        Task<Void> x = selectedBoardDataRepository.getRefreshTask();
//        x.addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                Log.i(TAG, "onSuccess: Trying to set visibility");
//                ArrayList<SelectedBoardItem.Message> p = selectedBoardDataRepository.getDataSet();
//                if (p.size() == 0) {
//                    getActivity().findViewById(R.id.boardEmptyMessageView).setVisibility(View.VISIBLE);
//                }
//
//
//            }
//        });

        mAdapter = new SelectedBoardItemAdapter(getContext(), selectedBoardDataRepository);
        mRecyclerView.setAdapter(mAdapter);


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
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
