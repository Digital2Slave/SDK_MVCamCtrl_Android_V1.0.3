package com.example.setparam;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.hk.basemodule.IMainView;
import com.example.hk.basemodule.LogView;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.List;

import MvCameraControlWrapper.CameraControlException;
import MvCameraControlWrapper.MvCameraControl;
import MvCameraControlWrapper.MvCameraControlDefines;

import static MvCameraControlWrapper.MvCameraControlDefines.MV_OK;

public class SetParamActivity extends AppCompatActivity implements IMainView {


    private ArrayList<MvCameraControlDefines.MV_CC_DEVICE_INFO> deviceList = new ArrayList<>();
    private MaterialSpinner spinner;
    EditText logTv;
    int selectNum = 0;
    Button openDeviceBt;
    MvCameraControlDefines.Handle handle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_param_main);
        initView();
        initListener();
    }

    private void initView() {
        spinner = findViewById(R.id.spinner);
        logTv = findViewById(R.id.logTv);
        logTv.setMovementMethod(ScrollingMovementMethod.getInstance());
        openDeviceBt = findViewById(R.id.openDeviceBt);
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                selectNum = position;
            }
        });
    }

    private void initListener() {
        findViewById(R.id.enumDevicesBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectNum = 0;
                deviceList.clear();
                spinner.setItems(new ArrayList());
                try {
                    deviceList = MvCameraControl.MV_CC_EnumDevices(MvCameraControlDefines.MV_USB_DEVICE | MvCameraControlDefines.MV_GIGE_DEVICE);
                } catch (CameraControlException e) {
                    e.printStackTrace();
                }
                if (deviceList != null) {
                    int size = deviceList.size();
                    if (size > 0) {
                        List<String> deviceName = new ArrayList<>();
                        for (int i = 0; i < size; i++) {
                            MvCameraControlDefines.MV_CC_DEVICE_INFO entity = deviceList.get(i);
                            if (entity.transportLayerType == MvCameraControlDefines.MV_GIGE_DEVICE) {
                                String str = "[" + String.valueOf(i) + "]"
                                        + entity.gigEInfo.manufacturerName + "--"
                                        + entity.gigEInfo.serialNumber + "--"
                                        + entity.gigEInfo.deviceVersion + "--"
                                        + entity.gigEInfo.userDefinedName;
                                deviceName.add(str);
                            } else {
                                String str = "[" + String.valueOf(i) + "]"
                                        + entity.usb3VInfo.manufacturerName + "--"
                                        + entity.usb3VInfo.serialNumber + "--"
                                        + entity.usb3VInfo.deviceVersion + "--"
                                        + entity.usb3VInfo.userDefinedName;
                                deviceName.add(str);
                            }

                        }
                        spinner.setItems(deviceName);
                    } else {
                        spinner.setItems(new ArrayList());
                    }
                } else {
                    showLog("未枚举到设备");
                }
            }
        });

        findViewById(R.id.openDeviceBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDevice();
            }
        });

    }

    private void openDevice() {
        SetParamThread setParamThread = new SetParamThread();
        setParamThread.start();
    }

    @Override
    public void showLog(final String str) {
        LogView.showLog(this, logTv, str);

    }

    @Override
    public void updateButton(int state) {

    }

    @Override
    public void updateImage(byte[] data) {

    }

    public void toast(String string) {
        Toast.makeText(SetParamActivity.this, string, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class SetParamThread extends Thread {
        @Override
        public void run() {
            super.run();
            if (deviceList != null && deviceList.size() > 0) {
                try {
                    setOpenDeviceBt(false);

                    try {
                        handle = MvCameraControl.MV_CC_CreateHandle(deviceList.get(selectNum));
                    } catch (CameraControlException e) {
                        e.printStackTrace();
                    }
                    if (handle == null) {
                        showLog("CreateHandle fail! nRet");
                        return;
                    }
                    int nRet = MvCameraControl.MV_CC_OpenDevice(handle);
                    if (nRet != 0) {
                        showLog("OpenDevice fail! nRet " + Integer.toHexString(nRet));
                        if (handle != null) {
                            nRet = MvCameraControl.MV_CC_DestroyHandle(handle);
                        }
                        return;
                    }


                    int nHeightValue = 1024;
                    nRet = MvCameraControl.MV_CC_SetIntValue(handle, "Height", nHeightValue);
                    if (MV_OK == nRet) {
                        showLog("set height OK!");
                    } else {
                        showLog("set height failed! nRet " + nRet);
                    }


                    // 获取int型变量
                    // get IInteger variable
                    MvCameraControlDefines.MVCC_INTVALUE stHeight = new MvCameraControlDefines.MVCC_INTVALUE();
                    nRet = MvCameraControl.MV_CC_GetIntValue(handle, "Height", stHeight);
                    if (MV_OK == nRet) {
                        showLog("height current value: " + stHeight.curValue);
                        showLog("height max value: " + stHeight.max);
                        showLog("height min value: " + stHeight.min);
                        showLog("height increment value: " + stHeight.inc);
                    } else {
                        showLog("get height failed! nRet" + nRet);
                    }


                    //设置Float类型变量
                    //set IFloat variable
                    float gain = 5f;
                    nRet = MvCameraControl.MV_CC_SetFloatValue(handle, "Gain", gain);
                    if (MV_OK == nRet) {
                        showLog("set Gain OK!");
                    } else {
                        showLog("set Gain failed! nRet" + Integer.toHexString(nRet));
                    }


                    // 获取float型变量
                    // get IFloat variable
                    MvCameraControlDefines.MVCC_FLOATVALUE stExposureTime = new MvCameraControlDefines.MVCC_FLOATVALUE();
                    nRet = MvCameraControl.MV_CC_GetFloatValue(handle, "Gain", stExposureTime);
                    if (MV_OK == nRet) {
                        showLog("Gain time current value: " + stExposureTime.curValue);
                        showLog("Gain time max value:  " + stExposureTime.max);
                        showLog("Gain time min value: " + stExposureTime.min);
                    } else {
                        showLog("get Gain time failed! nRet " + Integer.toHexString(nRet));
                    }


                    // 获取enum型变量
                    // get IEnumeration variable
                    MvCameraControlDefines.MVCC_ENUMVALUE stTriggerMode = new MvCameraControlDefines.MVCC_ENUMVALUE();
                    nRet = MvCameraControl.MV_CC_GetEnumValue(handle, "TriggerMode", stTriggerMode);
                    if (MV_OK == nRet) {
                        showLog("TriggerMode current value: " + stTriggerMode.curValue);
                        for (int i = 0; i < stTriggerMode.supportValue.size(); i++) {
                            showLog("supported TriggerMode: " + stTriggerMode.supportValue.get(i).intValue() + "");
                        }
                    } else {
                        showLog("get TriggerMode failed! nRet" + Integer.toHexString(nRet));
                    }


                    int nTriggerMode = 0;
                    nRet = MvCameraControl.MV_CC_SetEnumValue(handle, "TriggerMode", nTriggerMode);
                    if (MV_OK == nRet) {
                        showLog("set TriggerMode OK!");
                    } else {
                        showLog("set TriggerMode failed! nRet " + nRet);
                    }


                    // 获取bool型变量
                    // get IBoolean variable
                    Boolean bGetBoolValue = false;
                    nRet = MvCameraControl.MV_CC_GetBoolValue(handle, "ReverseX", bGetBoolValue);
                    if (MV_OK == nRet) {

                        showLog("ReverseX current is: " + bGetBoolValue);

                    }

                    boolean nSetBoolValue = false;


                    nRet = MvCameraControl.MV_CC_SetBoolValue(handle, "ReverseX", nSetBoolValue);
                    if (MV_OK == nRet) {
                        showLog("Set ReverseX OK!");
                    } else {
                        showLog("Set ReverseX Failed! nRet" + Integer.toHexString(nRet));
                    }


                    // 获取string型变量
                    // get IString variable
                    MvCameraControlDefines.MVCC_STRINGVALUE stStringValue = new MvCameraControlDefines.MVCC_STRINGVALUE();
                    nRet = MvCameraControl.MV_CC_GetStringValue(handle, "DeviceUserID", stStringValue);
                    if (MV_OK == nRet) {
                        showLog("Get DeviceUserID: " + stStringValue.curValue);
                    } else {
                        showLog("Get DeviceUserID Failed! nRet" + Integer.toHexString(nRet));
                    }


                    nRet = MvCameraControl.MV_CC_SetStringValue(handle, "DeviceUserID", "hktestDev");
                    if (MV_OK == nRet) {
                        showLog("Set DeviceUserID OK!");
                    } else {
                        showLog("Set DeviceUserID Failed! nRet " + Integer.toHexString(nRet));
                    }

                    // 关闭设备
                    // close device
                    nRet = MvCameraControl.MV_CC_CloseDevice(handle);
                    if (MV_OK != nRet) {
                        showLog("MV_CC_CloseDevice fail! nRet" + Integer.toHexString(nRet));
                        return;
                    }

                    // 销毁句柄
                    // destroy handle
                    nRet = MvCameraControl.MV_CC_DestroyHandle(handle);
                    if (MV_OK != nRet) {
                        showLog("MV_CC_DestroyHandle fail! nRet" + Integer.toHexString(nRet));
                        return;
                    }
                } finally {
                    setOpenDeviceBt(true);
                }

            } else {
                toast("请先枚举设备");
                return;
            }
        }
    }

    public void setOpenDeviceBt(final boolean flag) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                openDeviceBt.setEnabled(flag);
            }
        });
    }

}
