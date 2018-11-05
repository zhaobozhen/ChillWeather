package com.absinthe.chillweather.fragment;

import com.absinthe.chillweather.ChooseAreaActivity;
import com.absinthe.chillweather.R;
import com.absinthe.chillweather.WeatherActivity;
import com.absinthe.chillweather.adapter.CityAdapter;
import com.absinthe.chillweather.db.County;
import com.absinthe.chillweather.model.CityItem;
import com.absinthe.chillweather.util.SharedPrefsStrListUtil;
import com.google.android.material.snackbar.Snackbar;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.thesurix.gesturerecycler.DefaultItemClickListener;
import com.thesurix.gesturerecycler.GestureAdapter;
import com.thesurix.gesturerecycler.GestureManager;
import com.thesurix.gesturerecycler.RecyclerItemTouchListener;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

public class CityManagerFragment extends BaseFragment implements TencentLocationListener {

    private CityAdapter mAdapter;
    private FabSpeedDial fabSpeedDial;
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
    private TencentLocationManager mLocationManager;    //腾讯定位SDK
    private TencentLocationRequest request;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_recycler, container, false);
        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        fabSpeedDial = rootView.findViewById(R.id.add_fab);
        fabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                //TODO: Start some activity
                switch (menuItem.getItemId()) {
                    case R.id.action_locate:
                        Log.d("FAB_CLICK", "Located_Action");
                        cityLocated();
                        break;
                    case R.id.action_add_city:
                        Intent intent = new Intent(getActivity(), ChooseAreaActivity.class);
                        startActivity(intent);
                        break;
                    default:
                }
                return false;
            }
        });

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

    private void cityLocated() {
        LitePal.initialize(getContext());
        //运行时权限申请
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (!permissionList.isEmpty()) {
            Log.d("FAB_CLICK", "permissionList.isEmpty()");
            requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE}, 1);
        } else {
            showProgressDialog();
            mLocationManager = TencentLocationManager.getInstance(getContext());
            request = TencentLocationRequest.create().setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_ADMIN_AREA);
            mLocationManager.requestLocationUpdates(request, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(getContext(), getString(R.string.you_must_allow_all_permissions), Toast.LENGTH_SHORT).show();
                            Objects.requireNonNull(getActivity()).finish();
                            return;
                        }
                    }
                    Log.d("FAB_CLICK", "onRequestPermissionsResult");
                    showProgressDialog();
                    mLocationManager = TencentLocationManager.getInstance(getContext());
                    request = TencentLocationRequest.create().setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_ADMIN_AREA);
                    mLocationManager.requestLocationUpdates(request, this);
                } else {
                    Toast.makeText(getContext(), getString(R.string.unknown_errors), Toast.LENGTH_SHORT).show();
                    Objects.requireNonNull(getActivity()).finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
        if (i == TencentLocation.ERROR_OK) {
            // 定位成功
            //从返回的定位数据中截取城市名
            String str = tencentLocation.getDistrict();
            str = str.substring(0, str.length()-1);
            mLocationManager.removeUpdates(this);
            List<County> countyList = LitePal.where("countyName = ?", str).find(County.class);
            SharedPrefsStrListUtil.putStrValueInList(getContext(),
                    "city",
                    countyList.get(0).getCountyName(),
                    countyList.get(0).getWeatherId(),
                    CityManagerFragment.imgs[new Random().nextInt(12)]);
            closeProgressDialog();
            mAdapter.setData(getCities());
            Snackbar.make(mRecyclerView, getString(R.string.located_success_and_tap_to_change), Snackbar.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), getString(R.string.located_failed), Toast.LENGTH_SHORT).show();
            closeProgressDialog();
            Objects.requireNonNull(getActivity()).finish();
        }
    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {
        //ignore
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("定位中……");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
