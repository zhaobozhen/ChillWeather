package com.absinthe.chillweather.fragment;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.absinthe.chillweather.model.CityItem;
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

    protected List<CityItem> getCities() {
        return new ArrayList<>();
    }

}
