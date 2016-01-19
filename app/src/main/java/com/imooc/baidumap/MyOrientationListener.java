package com.imooc.baidumap;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by Wind on 2016/1/19.
 */
public class MyOrientationListener implements SensorEventListener{



    public SensorManager mSensorManager;
    public Context mContext;
    public Sensor mSensor;
    public float lastX;
    public onOrientationLoistner mOnOrientationListener;

    public  MyOrientationListener(Context context){
        this.mContext = context;
    }

    public void start(){
        mSensorManager = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
        if(mSensorManager!=null){
            //获得方向传感器
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }
        if(mSensor!=null){
            mSensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stop(){
        mSensorManager.unregisterListener(this);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    public void onSensorChanged(SensorEvent event){
        if(event.sensor.getType()==Sensor.TYPE_ORIENTATION){
            float x = event.values[SensorManager.DATA_X];
            if (Math.abs(x - lastX)>1.0) {
                if(mOnOrientationListener!=null){
                    mOnOrientationListener.onOrientationChanged(x);
                }
            }
            lastX = x;

        }
    }

    public interface  onOrientationLoistner{
        void onOrientationChanged(float x);
    }

    public void setmSensorManager(SensorManager mSensorManager) {
        this.mSensorManager = mSensorManager;
    }
    public void setmOnOrientationListener(onOrientationLoistner mOnOrientationListener) {
        this.mOnOrientationListener = mOnOrientationListener;
    }
}
