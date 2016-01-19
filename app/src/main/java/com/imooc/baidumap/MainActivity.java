package com.imooc.baidumap;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public MapView mMapView;
    public BaiduMap baiduMap;

    public Context context;

    //定位相关
    public LocationClient mLocationClient;
    public myLocationListenter mLocationListenter;
    public boolean isFirst = true;
    public double mLatitude;
    public double mLongtitude;
    //自定义定位图标
    public BitmapDescriptor mIconLocation;
    public BitmapDescriptor bitmap;
    public BDLocation location;
    public MyOrientationListener myOrientationListener;
    public  float mCurrentX;
    public MyLocationConfiguration.LocationMode mLocationMode;

    //覆盖相关
    public BitmapDescriptor mMarket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法必须要在setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        this.context = this;
        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.maker);

        initView();

        //初始化定位
        initLocation();

        //初始化覆盖物
        initMarket();

        //点击确定弹出自己位置
        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //先清除图层
                baiduMap.clear();
                // 定义Maker坐标点
                LatLng point = new LatLng(mLatitude, mLongtitude);
                // 构建MarkerOption，用于在地图上添加Marker
                MarkerOptions options = new MarkerOptions().position(point)
                        .icon(bitmap);
                // 在地图上添加Marker，并显示
                baiduMap.addOverlay(options);
                Toast.makeText(context, "您当前的位置是:" + location.getAddrStr(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });

    }

    public void initMarket(){
        mMarket = BitmapDescriptorFactory.fromResource(R.drawable.maker);
    }

    private void initLocation(){
        mLocationMode = MyLocationConfiguration.LocationMode.NORMAL;
        mLocationClient = new LocationClient(this);
        mLocationListenter = new myLocationListenter();
        mLocationClient.registerLocationListener(mLocationListenter);

        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setScanSpan(1000);
        mLocationClient.setLocOption(option);
        //初始定位图标
        mIconLocation = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked);
        myOrientationListener = new MyOrientationListener(context);

        myOrientationListener.setmOnOrientationListener(new MyOrientationListener.onOrientationLoistner() {
            @Override
            public void onOrientationChanged(float x) {
                mCurrentX = x;
            }
        });
    }


    private void initView() {
        mMapView = (MapView)this.findViewById(R.id.id_bmapView);
        baiduMap = mMapView.getMap();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
        baiduMap.setMapStatus(msu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，实现地图生命周期管理
        mMapView.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //停止定位
        baiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
        //停止方向传感器
        myOrientationListener.stop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //开启定位
        baiduMap.setMyLocationEnabled(true);
        if(!mLocationClient.isStarted()){
            mLocationClient.start();
            //开启方向传感器
            myOrientationListener.start();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.id_map_commen:
                baiduMap.setMapType(baiduMap.MAP_TYPE_NORMAL);
                break;
            case R.id.id_map_site:
                baiduMap.setMapType(baiduMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.id_map_traffic:
                if(baiduMap.isTrafficEnabled()){
                    baiduMap.setTrafficEnabled(false);
                    item.setTitle("实时交通off");
                }else{
                    baiduMap.setTrafficEnabled(true);
                    item.setTitle("实时交通on");
                }
                break;
            case R.id.id_map_location:
                centerToMylocation(mLatitude, mLongtitude);
                break;
            case R.id.id_map_mode_normal:
                mLocationMode = MyLocationConfiguration.LocationMode.NORMAL;
                break;
            case R.id.id_map_mode_following:
                mLocationMode = MyLocationConfiguration.LocationMode.FOLLOWING;
                break;
            case R.id.id_map_mode_compass:
                mLocationMode = MyLocationConfiguration.LocationMode.COMPASS;
                break;
            case R.id.id_map_overlay:
                addOverlays(Info.infos);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }




    /*
    * 定位到我的位置
    *
    * */
    private void centerToMylocation(double mLatitude, double mLongtitude) {
        LatLng latLng = new LatLng(mLatitude, mLongtitude);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
        baiduMap.animateMapStatus(msu);
    }

    public class myLocationListenter implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            MyLocationData data = new MyLocationData.Builder()//
                    .direction(mCurrentX)
                    .accuracy(bdLocation.getRadius())//
                    .latitude(bdLocation.getLatitude())//
                    .longitude(bdLocation.getLongitude())//
                    .build();
            baiduMap.setMyLocationData(data);
            //设置定位图标
            MyLocationConfiguration config = new MyLocationConfiguration(
                    mLocationMode,true,mIconLocation);
            baiduMap.setMyLocationConfigeration(config);

            //更新经纬度
            mLatitude = bdLocation.getLatitude();
            mLongtitude = bdLocation.getLongitude();

            if(isFirst){
                //经纬度
                centerToMylocation(bdLocation.getLatitude(), bdLocation.getLongitude());

                isFirst = false;
                Toast.makeText(context,"定位完成:"+bdLocation.getAddrStr(),Toast.LENGTH_SHORT).show();
            }
            location = bdLocation;
        }
    }

    /*
    * 添加覆盖物
    *
    * */
    public void addOverlays(List<Info> infos) {
        baiduMap.clear();
        LatLng latLng = null;
        Marker marker = null;
        OverlayOptions options;
        for(Info info:infos){
            //经纬度
            latLng = new LatLng(info.getLatitude(),info.getLongitude());
            //图标
            options = new MarkerOptions().position(latLng).icon(mMarket).zIndex(5);
            marker = (Marker)baiduMap.addOverlay(options);
            Bundle arg0 = new Bundle();
            arg0.putSerializable("info", info);
            marker.setExtraInfo(arg0);
        }
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
        baiduMap.setMapStatus(msu);
    }
}
