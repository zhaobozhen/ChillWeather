package com.absinthe.chillweather.fragment;

import com.absinthe.chillweather.R;
import com.absinthe.chillweather.adapter.MonthsAdapter;
import com.absinthe.chillweather.model.Month;
import com.absinthe.chillweather.model.MonthItem;
import com.google.android.material.snackbar.Snackbar;
import com.thesurix.gesturerecycler.GestureAdapter;
import com.thesurix.gesturerecycler.GestureManager;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.ArrayList;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

public class CityManagerFragment extends BaseFragment {

    private MonthsAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_recycler, container, false);
        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        return rootView;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final LinearLayoutManager manager = new LinearLayoutManager(getContext());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);

        mAdapter = new MonthsAdapter(getContext(), R.layout.linear_item_with_background);
        mAdapter.setData(getMonths());
        mAdapter.setUndoSize(2);
        mAdapter.setDataChangeListener(new GestureAdapter.OnDataChangeListener<MonthItem>() {
            @Override
            public void onItemRemoved(final MonthItem item, final int position) {
                final Snackbar undoSnack = Snackbar.make(view, "Month removed from position " + position, Snackbar.LENGTH_SHORT);
                undoSnack.setAction(R.string.undo_text, new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        mAdapter.undoLast();
                    }
                });
                undoSnack.show();
            }

            @Override
            public void onItemReorder(final MonthItem item, final int fromPos, final int toPos) {
                final Snackbar undoSnack = Snackbar.make(view, "Month moved from position " + fromPos + " to " + toPos, Snackbar.LENGTH_SHORT);
                undoSnack.setAction(R.string.undo_text, new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        mAdapter.undoLast();
                    }
                });
                undoSnack.show();
            }
        });

        final View emptyView = view.findViewById(R.id.empty_root);
        mAdapter.setEmptyView(emptyView);

        mRecyclerView.setAdapter(mAdapter);

        mGestureManager = new GestureManager.Builder(mRecyclerView)
                .setSwipeEnabled(true)
                .setSwipeFlags(ItemTouchHelper.LEFT)
                .setLongPressDragEnabled(true)
                .build();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.recycler_empty_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.recycler_undo_menu:
                mAdapter.undoLast();
                break;
            case R.id.recycler_clear_menu:
                mAdapter.clearData();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected List<MonthItem> getMonths() {
        final List<MonthItem> monthList = new ArrayList<>();
        monthList.add(new Month("JAN", R.drawable.january));
        monthList.add(new Month("FEB", R.drawable.february));
        monthList.add(new Month("MAR", R.drawable.march));
        monthList.add(new Month("APR", R.drawable.april));
        monthList.add(new Month("MAY", R.drawable.may));
        monthList.add(new Month("JUN", R.drawable.june));

        return monthList;
    }
}
