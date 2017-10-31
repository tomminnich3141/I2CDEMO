package com.freedomse.i2cdemo;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG =  "MainActivity";
    private static final String TAGI2C = "I2C";
    private static final int INTERVAL_BETWEEN_I2C_MS = 2000;
    private static final String I2C_DEVICE_NAME = "I2C1";
    private static final int I2C_PCA9685 = 0x40 ;
    private static final int I2C_PCA9685_SUBADR1 = 0x2 ;
    private static final int I2C_PCA9685_SUBADR2 = 0x3 ;
    private static final int I2C_PCA9685_SUBADR3 = 0x4 ;
    private static final int I2C_PCA9685_MODE1 = 0x0 ;
    private static final int I2C_PCA9685_PRESCALE = 0xFE ;
    private static final int I2C_PCA9685_LED0_ON_L = 0x6 ;
    private static final int I2C_PCA9685_LED0_ON_H = 0x7 ;
    private static final int I2C_PCA9685_LED0_OFF_L = 0x8 ;
    private static final int I2C_PCA9685_LED0_OFF_H = 0x9 ;
    private static final int I2C_PCA9685_ALLLED_ON_L = 0xFA ;
    private static final int I2C_PCA9685_ALLLED_ON_H = 0xFB ;
    private static final int I2C_PCA9685_ALLLED_OFF_L = 0xFC ;
    private static final int I2C_PCA9685_ALLLED_OFF_H = 0xFD ;

    private Handler mHandler = new Handler();
    private I2cDevice mDevice;
    private int mState;
    private int pwm_dut1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PeripheralManagerService manager = new PeripheralManagerService();

        try {
            mDevice = manager.openI2cDevice(I2C_DEVICE_NAME,I2C_PCA9685);
        } catch (IOException e){
            Log.w(TAG, "Unable to access I2C device", e);
        }
        mState = 1;
        pwm_dut1 = 1;
        mHandler.post(mI2cRunnable);
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();

        if (mDevice != null){
            try {
                mDevice.close();
                mDevice = null;
                } catch (IOException e){
                    Log.w(TAG, "Unable to close I2C device", e);
                }
        }

    }

    private Runnable mI2cRunnable = new Runnable() {
        @Override
        public void run() {
            if(mDevice == null) return;

            try{
                if (mState==1){
                    // Do a reset
                    mDevice.writeRegByte( I2C_PCA9685_MODE1,(byte) 0);
                    Log.i(TAG, "I2C write the reset");
                }
                if (mState==2){
                    // Set PWM freq
                    mDevice.writeRegByte( I2C_PCA9685_MODE1,(byte) 0x10); //go to sleep
                    mDevice.writeRegByte( I2C_PCA9685_PRESCALE,(byte) 0x66); //set the prescaler
                    mDevice.writeRegByte( I2C_PCA9685_MODE1,(byte) 0);
                    Log.i(TAG, "I2C PWM freq STEP 1");

                }
                if (mState==3){// Second step PWM freq
                    // Set PWM freq
                    mDevice.writeRegByte( I2C_PCA9685_MODE1,(byte) 0xa1); //go to sleep
                    Log.i(TAG, "I2C PWM freq STEP 2");

                }
                if (mState==4){// Second step PWM freq
                    // Set PWM freq
                    mDevice.writeRegByte( I2C_PCA9685_ALLLED_ON_L,(byte) (pwm_dut1&0xff)); //go to sleep
                    mDevice.writeRegByte( I2C_PCA9685_ALLLED_ON_H,(byte) (pwm_dut1>>8)); //go to sleep
                    mDevice.writeRegByte( I2C_PCA9685_ALLLED_OFF_L,(byte) (4095&0xff)); //go to sleep
                    mDevice.writeRegByte( I2C_PCA9685_ALLLED_OFF_H,(byte) (4095>>8)); //go to sleep

                    Log.i(TAG, "I2C PWM duty cycle " + pwm_dut1);

                    pwm_dut1 += 400;
                    if(pwm_dut1 > 4095) pwm_dut1 = 1;
                }

                mState++;
                if(mState > 4) mState = 4;
                mHandler.postDelayed(mI2cRunnable,INTERVAL_BETWEEN_I2C_MS);
            }catch (IOException e){
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };
}
