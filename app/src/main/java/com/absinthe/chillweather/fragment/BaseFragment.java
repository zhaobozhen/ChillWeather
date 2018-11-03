package com.absinthe.chillweather.fragment;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.absinthe.chillweather.R;
import com.absinthe.chillweather.model.Month;
import com.absinthe.chillweather.model.MonthHeader;
import com.absinthe.chillweather.model.MonthItem;
import com.thesurix.gesturerecycler.GestureManager;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class BaseFragment extends Fragment {

    protected RecyclerView mRecyclerView;
    protected GestureManager mGestureManager;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        mRecyclerView = new RecyclerView(getActivity());
        return mRecyclerView;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.recycler_menu, menu);
    }

    protected List<MonthItem> getMonths() {
        final List<MonthItem> monthList = new ArrayList<>();
        monthList.add(new MonthHeader("First quarter"));
        monthList.add(new Month("JAN", R.drawable.january));
        monthList.add(new Month("FEB", R.drawable.february));
        monthList.add(new Month("MAR", R.drawable.march));
        monthList.add(new MonthHeader("Second quarter"));
        monthList.add(new Month("APR", R.drawable.april));
        monthList.add(new Month("MAY", R.drawable.may));
        monthList.add(new Month("JUN", R.drawable.june));
        monthList.add(new MonthHeader("Third quarter"));
        monthList.add(new Month("JUL", R.drawable.july));
        monthList.add(new Month("AUG", R.drawable.august));
        monthList.add(new Month("SEP", R.drawable.september));
        monthList.add(new MonthHeader("Fourth quarter"));
        monthList.add(new Month("OCT", R.drawable.october));
        monthList.add(new Month("NOV", R.drawable.november));
        monthList.add(new Month("DEC", R.drawable.december));

        return monthList;
    }

}
