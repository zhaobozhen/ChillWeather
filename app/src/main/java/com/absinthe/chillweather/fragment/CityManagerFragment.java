package com.absinthe.chillweather.fragment;

import com.absinthe.chillweather.ChooseAreaActivity;
import com.absinthe.chillweather.MainActivity;
import com.absinthe.chillweather.R;
import com.absinthe.chillweather.WeatherActivity;
import com.absinthe.chillweather.adapter.CityAdapter;
import com.absinthe.chillweather.db.County;
import com.absinthe.chillweather.model.CityItem;
import com.absinthe.chillweather.util.SharedPrefsStrListUtil;
import com.google.android.material.snackbar.Snackbar;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.thesurix.gesturerecycler.DefaultItemClickListener;
import com.thesurix.gesturerecycler.GestureAdapter;
import com.thesurix.gesturerecycler.GestureManager;
import com.thesurix.gesturerecycler.RecyclerItemTouchListener;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;


public class CityManagerFragment extends BaseFragment implements TencentLocationListener {

    private CityAdapter mAdapter;
    private ShortcutManager mShortcutManager;
    public static int[] imgs =
            {R.drawable.bg_january,
            R.drawable.bg_february,
            R.drawable.bg_march,
            R.drawable.bg_april,
            R.drawable.bg_may,
            R.drawable.bg_june,
            R.drawable.bg_july,
            R.drawable.bg_august,
            R.drawable.bg_september,
            R.drawable.bg_october,
            R.drawable.bg_november,
            R.drawable.bg_december};
    private TencentLocationManager mLocationManager;    //腾讯定位SDK
    private TencentLocationRequest request;
    private ProgressDialog progressDialog;
    private static final String TAG = "CityManagerFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_recycler, container, false);

        mRecyclerView = rootView.findViewById(R.id.rv_city_recycler_view);

        FabSpeedDial fabSpeedDial = rootView.findViewById(R.id.fab_add);
        fabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                //TODO: Start some activity
                switch (menuItem.getItemId()) {
                    case R.id.action_locate:
                        Log.d(TAG, "Located_Action");
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            mShortcutManager = Objects.requireNonNull(getActivity()).getSystemService(ShortcutManager.class);
        }
        final LinearLayoutManager manager = new LinearLayoutManager(getContext());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);

        mAdapter = new CityAdapter(getContext(), R.layout.linear_item_with_background);
        mAdapter.setData(getCities());
        mAdapter.setUndoSize(2);
        mAdapter.setDataChangeListener(new GestureAdapter.OnDataChangeListener<CityItem>() {
            @Override
            public void onItemRemoved(final CityItem item, final int position) {
                final Snackbar undoSnack = Snackbar.make(view, getString(R.string.city_removed), Snackbar.LENGTH_SHORT);
                undoSnack.setAction(R.string.undo_text, v -> mAdapter.undoLast());
                undoSnack.show();
                SharedPrefsStrListUtil.removeStrListItem(getContext(), "city", item.getName());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    mShortcutManager.removeDynamicShortcuts(Collections.singletonList(item.getWeatherId()));
                }
                if (mAdapter.getItemCount() == 0) {
                    SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                    prefs.remove("weather_id");
                    prefs.apply();
                }
            }

            @Override
            public void onItemReorder(final CityItem item, final int fromPos, final int toPos) {
                final Snackbar undoSnack = Snackbar.make(view, getString(R.string.move_success), Snackbar.LENGTH_SHORT);
                undoSnack.setAction(R.string.undo_text, v -> mAdapter.undoLast());
                undoSnack.show();
            }
        });

        final View emptyView = view.findViewById(R.id.rl_empty_root);
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
                WeatherActivity.isNeedRefresh = true;
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

    @SuppressLint("CheckResult")
    private void cityLocated() {
        LitePal.initialize(getContext());
        //运行时权限申请
        final RxPermissions rxPermissions = new RxPermissions(this);

        rxPermissions
                .request(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE)
                .subscribe(granted -> {
                    if (granted) {
                        // All requested permissions are granted
                        showProgressDialog();
                        mLocationManager = TencentLocationManager.getInstance(getContext());
                        request = TencentLocationRequest.create().setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_ADMIN_AREA);
                        mLocationManager.requestLocationUpdates(request, this);
                    } else {
                        // At least one permission is denied
                        Toast.makeText(getContext(), getString(R.string.you_must_allow_all_permissions), Toast.LENGTH_SHORT).show();
                    }
                });
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

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                ShortcutManager mShortcutManager = Objects.requireNonNull(getActivity()).getSystemService(ShortcutManager.class);
                List<ShortcutInfo> infos = new ArrayList<>();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.putExtra("weather_id", countyList.get(i).getWeatherId());
                ShortcutInfo info = new ShortcutInfo.Builder(getActivity(), countyList.get(0).getWeatherId())
                        .setShortLabel(countyList.get(i).getCountyName())
                        .setIcon(Icon.createWithResource(getContext(), R.drawable.ic_noti_logo_gray))
                        .setIntent(intent)
                        .build();
                infos.add(info);
                mShortcutManager.addDynamicShortcuts(infos);
            }
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
            progressDialog.setMessage(getString(R.string.on_locating));
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
