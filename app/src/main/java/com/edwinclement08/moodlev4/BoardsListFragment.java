package com.edwinclement08.moodlev4;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.edwinclement08.moodlev4.data.boards.BoardsDataRepository;
import com.edwinclement08.moodlev4.data.boards.BoardsItemAdapter;
import com.google.android.gms.tasks.OnSuccessListener;

public class BoardsListFragment extends Fragment implements NamedFragments{
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    public String titleName = "Boards";


    BoardsDataRepository boardsDataRepository;
    public String TAG = "BoardsListFragment";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.boards_page, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.boards_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        boardsDataRepository = new BoardsDataRepository();

        mAdapter = new BoardsItemAdapter(getContext(), boardsDataRepository);
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

        ((BoardsItemAdapter) mAdapter).updateDataset().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                onItemsLoadComplete();
            }
        });
    }

    void onItemsLoadComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public String getTitle() {
        return "Boards";
    }




}

