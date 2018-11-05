package com.absinthe.chillweather;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.absinthe.chillweather.db.City;
import com.absinthe.chillweather.db.County;
import com.absinthe.chillweather.db.Province;
import com.absinthe.chillweather.fragment.CityManagerFragment;
import com.absinthe.chillweather.util.SharedPrefsStrListUtil;
import com.google.android.material.snackbar.Snackbar;
import com.wyt.searchbox.SearchFragment;
import com.wyt.searchbox.custom.IOnSearchClickListener;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ChooseAreaActivity extends AppCompatActivity {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

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

        countyList = LitePal.where("cityId = ?", String.valueOf(selectedCity.getId())).find(County.class);
        dataList.clear();
        for (County county : countyList) {
            dataList.add(county.getCountyName());
        }
        adapter.notifyDataSetChanged();
        listView.setSelection(0);
        currentLevel = LEVEL_COUNTY;
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