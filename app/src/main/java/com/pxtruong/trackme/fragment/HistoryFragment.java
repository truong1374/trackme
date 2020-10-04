package com.pxtruong.trackme.fragment;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.pxtruong.trackme.MainActivity;
import com.pxtruong.trackme.R;
import com.pxtruong.trackme.data.Session;
import com.pxtruong.trackme.util.HistoryListAdapter;
import com.pxtruong.trackme.model.MainViewModel;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {
    private RecyclerView rvHistories;
    private ImageButton btnRecord;
    private Context mContext;
    private HistoryListAdapter mAdapter;
    private TextView mEmptyView;

    private MainViewModel mMainViewModel;

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEmptyView = view.findViewById(R.id.empty_view);
        rvHistories = view.findViewById(R.id.rvHistories);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        rvHistories.setLayoutManager(layoutManager);
        rvHistories.setHasFixedSize(true);
        DividerItemDecoration divider = new DividerItemDecoration(
                rvHistories.getContext(),
                DividerItemDecoration.VERTICAL
        );
        divider.setDrawable(ContextCompat.getDrawable(this.getContext(), R.drawable.vertical_divider));
        rvHistories.addItemDecoration(divider);

        mAdapter = new HistoryListAdapter(this.getContext());
        rvHistories.setAdapter(mAdapter);

        btnRecord = view.findViewById(R.id.btnRecord);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)mContext).transferToRecordScreen();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mMainViewModel.getAllSession().observe(this, new Observer<List<Session>>() {
            @Override
            public void onChanged(@Nullable final List<Session> sessions) {
                mAdapter.setSessionData(sessions);
                if(!sessions.isEmpty()) {
                    mEmptyView.setVisibility(View.GONE);
                    rvHistories.setVisibility(View.VISIBLE);
                }
            }
        });
    }

}