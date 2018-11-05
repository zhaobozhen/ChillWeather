package com.absinthe.chillweather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.absinthe.chillweather.db.City;
import com.absinthe.chillweather.db.County;
import com.absinthe.chillweather.db.Province;
import com.absinthe.chillweather.fragment.CityManagerFragment;
import com.absinthe.chillweather.model.CityItem;
import com.absinthe.chillweather.model.CityModel;
import com.absinthe.chillweather.util.SharedPrefsStrListUtil;
import com.google.android.material.snackbar.Snackbar;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.wyt.searchbox.SearchFragment;
import com.wyt.searchbox.custom.IOnSearchClickListener;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ChooseAreaActivity extends AppCompatActivity implements TencentLocationListener {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    public static boolean mAutoLocation;    //自动定位

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter adapter;
    private List<String> dataList = new ArrayList<>();

    private List<Province> provinceList;    //省列表
    private List<City> cityList;    //市列表
    private List<County> countyList;    //县列表

    private Province selectedProvince;  //选中的省份
    private City selectedCity;  //选中的城市
    private int currentLevel;   //当前选中的级别

    private TencentLocationManager mLocationManager;    //腾讯定位SDK
    private TencentLocationRequest request;
    private String str; //从返回的定位数据中截取城市名

    SearchFragment searchFragment;  //搜索框

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_area);
        titleText = findViewById(R.id.title_text);
        listView = findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, dataList);
        listView.setAdapter(adapter);
        Toolbar toolbar = findViewById(R.id.choose_area_toolbar);

        setSupportActionBar(toolbar);

        //城市搜索框
        searchFragment = SearchFragment.newInstance();
        searchFragment.setOnSearchClickListener(new IOnSearchClickListener() {
            @Override
            public void OnSearchClick(String keyword) {
                //这里处理逻辑
                countyList = LitePal.where("countyName = ?", keyword).find(County.class);
                if (countyList.size() != 0) {
                    dataList.clear();
                    dataList.add(countyList.get(0).getCountyName());
                    adapter.notifyDataSetChanged();
                    listView.setSelection(0);
                    currentLevel = LEVEL_COUNTY;
                } else {
                    Snackbar.make(listView, getString(R.string.not_searched_this_city), Snackbar.LENGTH_LONG).show();
                }
            }
        });

        LitePal.initialize(getApplicationContext());

        //运行时权限申请
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }

        mLocationManager = TencentLocationManager.getInstance(this);
        request = TencentLocationRequest.create().setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_ADMIN_AREA);

        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, permissions, 1);
        }

        SharedPreferences settings = getApplicationContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        mAutoLocation = settings.getBoolean("auto_locate_switch", true);
        if (mAutoLocation) {
            showProgressDialog();
            mLocationManager.requestLocationUpdates(request, this);
        }
        Log.d("AutoLocation", ""+mAutoLocation);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(i);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(i);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    SharedPrefsStrListUtil.putStrValueInList(getApplicationContext(),
                            "city",
                            countyList.get(i).getCountyName(),
                            countyList.get(i).getWeatherId(),
                            CityManagerFragment.imgs[new Random().nextInt(12)]);
                    finish();
                }
            }
        });
        queryProvinces();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationManager.removeUpdates(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, getString(R.string.you_must_allow_all_permissions), Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    mLocationManager.requestLocationUpdates(request, this);
                } else {
                    Toast.makeText(this, getString(R.string.unknown_errors), Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 查询全国所有的省，优先从数据库查询
     */
    private void queryProvinces() {
        titleText.setText(getString(R.string.choose_city));
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);

        provinceList = LitePal.findAll(Province.class);
        dataList.clear();
        for (Province province : provinceList) {
            dataList.add(province.getProvinceName());
        }
        adapter.notifyDataSetChanged();
        listView.setSelection(0);
        currentLevel = LEVEL_PROVINCE;
    }

    private void queryCities() {
        //titleText.setText(selectedProvince.getProvinceName());
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        cityList = LitePal.where("provinceId = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        dataList.clear();
        for (City city : cityList) {
            dataList.add(city.getCityName());
        }
        adapter.notifyDataSetChanged();
        listView.setSelection(0);
        currentLevel = LEVEL_CITY;
    }

    private void queryCounties() {
        //titleText.setText(selectedCity.getCityName());
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        countyList = LitePal.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        dataList.clear();
        for (County county : countyList) {
            dataList.add(county.getCountyName());
        }
        adapter.notifyDataSetChanged();
        listView.setSelection(0);
        currentLevel = LEVEL_COUNTY;
    }

    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
        if (i == TencentLocation.ERROR_OK) {
            // 定位成功
            str = tencentLocation.getDistrict();
            str = str.substring(0, str.length()-1);
            closeProgressDialog();
            Snackbar.make(listView, getString(R.string.located_success_and_tap_to_change), Snackbar.LENGTH_LONG).show();
            mLocationManager.removeUpdates(this);
        } else {
            Toast.makeText(this, getString(R.string.located_failed), Toast.LENGTH_SHORT).show();
            closeProgressDialog();
            finish();
        }
    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {
        //ignore
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                switch (currentLevel) {
                    case LEVEL_COUNTY:
                        queryCities();
                        break;
                    case LEVEL_CITY:
                        queryProvinces();
                        break;
                }
                break;
            case R.id.location:
                countyList = LitePal.where("countyName = ?", str).find(County.class);

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putString("weather_id", countyList.get(0).getWeatherId());
                editor.apply();
                Intent intent = new Intent(getApplicationContext(), WeatherActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.area_search:
                searchFragment.show(getSupportFragmentManager(),SearchFragment.TAG);
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY) {
            queryCities();
        } else if (currentLevel == LEVEL_CITY) {
            queryProvinces();
        } else if (currentLevel == LEVEL_PROVINCE) {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.choose_area_toolbar_menu, menu);
        return true;
    }
}