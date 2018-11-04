package com.absinthe.chillweather.fragment;

import com.absinthe.chillweather.ChooseAreaActivity;
import com.absinthe.chillweather.R;
import com.absinthe.chillweather.WeatherActivity;
import com.absinthe.chillweather.adapter.CityAdapter;
import com.absinthe.chillweather.model.CityModel;
import com.absinthe.chillweather.model.CityItem;
import com.absinthe.chillweather.util.SharedPrefsStrListUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.thesurix.gesturerecycler.DefaultItemClickListener;
import com.thesurix.gesturerecycler.GestureAdapter;
import com.thesurix.gesturerecycler.GestureManager;
import com.thesurix.gesturerecycler.RecyclerItemTouchListener;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

public class CityManagerFragment extends BaseFragment {

    private CityAdapter mAdapter;
    private FloatingActionButton fab;
    public static int[] imgs =
            {R.drawable.january,
            R.drawable.february,
            R.drawable.march,
            R.drawable.april,
            R.drawable.may,
            R.drawable.june,
            R.drawable.july,
            R.drawable.august,
            R.drawable.september,
            R.drawable.october,
            R.drawable.november,
            R.drawable.december};

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_recycler, container, false);
        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        fab = rootView.findViewById(R.id.add_city);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final LinearLayoutManager manager = new LinearLayoutManager(getContext());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);

        mAdapter = new CityAdapter(getContext(), R.layout.linear_item_with_background);
        mAdapter.setData(getCities());
        mAdapter.setUndoSize(2);
        mAdapter.setDataChangeListener(new GestureAdapter.OnDataChangeListener<CityItem>() {
            @Override
            public void onItemRemoved(final CityItem item, final int position) {
                final Snackbar undoSnack = Snackbar.make(view, "城市已删除。", Snackbar.LENGTH_SHORT);
                undoSnack.setAction(R.string.undo_text, new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        mAdapter.undoLast();
                    }
                });
                undoSnack.show();
                SharedPrefsStrListUtil.removeStrListItem(getContext(), "city", item.getName());
            }

            @Override
            public void onItemReorder(final CityItem item, final int fromPos, final int toPos) {
                final Snackbar undoSnack = Snackbar.make(view, "移动成功", Snackbar.LENGTH_SHORT);
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

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChooseAreaActivity.class);
                startActivity(intent);
            }
        });

        mRecyclerView.addOnItemTouchListener(new RecyclerItemTouchListener<>(new DefaultItemClickListener<CityItem>() {
            @Override
            public boolean onItemClick(final CityItem item, final int position) {
                // return true if the event is consumed
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                editor.putString("weather_id", item.getWeatherId());
                editor.apply();
                Intent intent = new Intent(getActivity(), WeatherActivity.class);
                startActivity(intent);
                Objects.requireNonNull(getActivity()).finish();
                return true;
            }
        }));
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.setData(getCities());
    }

    @Override
    protected List<CityItem> getCities() {
        return SharedPrefsStrListUtil.getStrListValue(getContext(), "city");
    }
}
