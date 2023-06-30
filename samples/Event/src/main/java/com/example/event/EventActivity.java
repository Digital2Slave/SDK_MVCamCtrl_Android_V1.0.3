package com.example.event;

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
import MvCameraControlWrapper.CameraEventCallBack;
import MvCameraControlWrapper.MvCameraControl;
import MvCameraControlWrapper.MvCameraControlDefines;

import static MvCameraControlWrapper.MvCameraControlDefines.MV_OK;

public class EventActivity extends AppCompatActivity implements IMainView {

    private ArrayList<MvCameraControlDefines.MV_CC_DEVICE_INFO> deviceList = new ArrayList<>();
    private MaterialSpinner spinner;
    EditText logTv;
    int selectNum = 0;
    Button enumDevicesBt;
    Button closeDeviceBt;
    Button openDeviceBt;
    final int MV_TRIGGER_MODE_OFF = 0;           // ch:关闭 | en:Off
    final int MV_TRIGGER_MODE_ON = 1;            // ch:打开 | en:ON
    OpenDeviceThread openDeviceThread = null;
    CloseDeviceThread closeDeviceThread = null;
    MvCameraControlDefines.Handle handle = null;
    boolean isOpenFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        initView();
        initListener();
    }

    private void initView() {
        spinner = findViewById(R.id.spinner);
        logTv = findViewById(R.id.logTv);
        logTv.setMovementMethod(ScrollingMovementMethod.getInstance());
        enumDevicesBt = findViewById(R.id.enumDevicesBt);
        openDeviceBt = findViewById(R.id.openDeviceBt);
        closeDeviceBt = findViewById(R.id.closeDeviceBt);
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
                    deviceList = MvCameraControl.MV_CC_EnumDevices(MvCameraControlDefines.MV_USB_DEVICE | MvCameraControlDefines.MV_GIGE_DEVICE);//cameraManager.enumDevice();
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

        findViewById(R.id.closeDeviceBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDevice();
            }
        });
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
        Toast.makeText(EventActivity.this, string, Toast.LENGTH_SHORT).show();
    }

    private void openDevice() {
        if (!isOpenFlag) {
            if (openDeviceThread == null) {
                openDeviceThread = new OpenDeviceThread();
                openDeviceThread.start();
            } else {
                if (!openDeviceThread.isAlive()) {
                    openDeviceThread.start();
                } else {
                    showLog("请稍后尝试");
                }

            }
        } else {
            showLog("请先关闭设备");
        }
    }

    private void closeDevice() {
        if (closeDeviceThread == null) {
            closeDeviceThread = new CloseDeviceThread();
            closeDeviceThread.start();
        } else {
            if (!closeDeviceThread.isAlive()) {
                closeDeviceThread.start();
            } else {
                showLog("请稍后尝试");
            }
        }
    }


    public class OpenDeviceThread extends Thread {
        @Override
        public void run() {

            super.run();
            setEnumBt(false);
            setCloseDevBt(false);
            showLog("正在打开...");
            try {
                if (deviceList != null && deviceList.size() > 0) {

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
                        return;
                    }
                    if (deviceList.get(selectNum).transportLayerType == MvCameraControlDefines.MV_GIGE_DEVICE) {
                        int nPacketSize = MvCameraControl.MV_CC_GetOptimalPacketSize(handle);
                        if (nPacketSize > 0) {
                            nRet = MvCameraControl.MV_CC_SetIntValue(handle, "GevSCPSPacketSize", nPacketSize);
                            if (nRet != MV_OK) {
                                showLog("Warning: Set Packet Size fail nRet" + Integer.toHexString(nRet));
                                return;
                            }
                        } else {
                            showLog("Warning: Get Packet Size fail nRet" + Integer.toHexString(nPacketSize));
                        }
                    }

                    nRet = MvCameraControl.MV_CC_SetFloatValue(handle, "AcquisitionFrameRate", 2);
                    if (MV_OK != nRet) {
                        showLog("AcquisitionFrameRate fail! nRet " + Integer.toHexString(nRet));
                    }
                    nRet = MvCameraControl.MV_CC_SetBoolValue(handle, "AcquisitionFrameRateEnable", true);
                    if (MV_OK != nRet) {
                        showLog("AcquisitionFrameRateEnable fail! nRet " + Integer.toHexString(nRet));
                    }

                    nRet = MvCameraControl.MV_CC_SetEnumValue(handle, "TriggerMode", MV_TRIGGER_MODE_OFF);
                    if (MV_OK != nRet) {
                        showLog("Set Trigger Mode fail! nRet" + Integer.toHexString(nRet));
                        return;
                    }
                    // ch:开启Event | en:Set Event of ExposureEnd On
                    nRet = MvCameraControl.MV_CC_SetEnumValueByString(handle, "EventSelector", "ExposureEnd");
                    if (MV_OK != nRet) {
                        showLog("Set Event Selector fail! nRet" + Integer.toHexString(nRet));
                        return;
                    }
                    nRet = MvCameraControl.MV_CC_SetEnumValueByString(handle, "EventNotification", "On");
                    if (MV_OK != nRet) {
                        showLog("Set Event Notification fail! nRet " + Integer.toHexString(nRet));
                        return;
                    }
                    // ch:注册事件回调 | en:Register event callback
                    nRet = MvCameraControl.MV_CC_RegisterEventCallBack(handle, "ExposureEnd", new CameraEventCallBack() {
                        @Override
                        public int OnEventCallBack(MvCameraControlDefines.MV_EVENT_OUT_INFO info) {
                            showLog("eventCallBack:" + info.eventName);
                            return 0;
                        }
                    });
                    if (MV_OK != nRet) {
                        showLog("Register Event CallBack fail! nRet " + Integer.toHexString(nRet));
                        return;
                    }
                    // 开始取流
                    // start grab image
                    nRet = MvCameraControl.MV_CC_StartGrabbing(handle);
                    if (MV_OK != nRet) {
                        showLog("MV_CC_StartGrabbing fail! nRet" + Integer.toHexString(nRet));
                        return;
                    }

                } else {
                    toast("请先枚举设备");
                }
            } finally {
                isOpenFlag = true;
                setEnumBt(true);
                setCloseDevBt(true);
                showLog("打开完毕...");
            }
        }
    }

    public class CloseDeviceThread extends Thread {
        @Override
        public void run() {
            super.run();
            showLog("正在关闭...");
            // 停止取流
            // end grab image
            int nRet = MvCameraControl.MV_CC_StopGrabbing(handle);

            if (MV_OK != nRet) {
                showLog("MV_CC_StopGrabbing fail! nRet" + Integer.toHexString(nRet));
            }
            // 关闭设备
            // close device
            nRet = MvCameraControl.MV_CC_CloseDevice(handle);
            if (MV_OK != nRet) {
                showLog("MV_CC_CloseDevice fail! nRet " + Integer.toHexString(nRet));
            }
            // 销毁句柄
            // destroy handle
            nRet = MvCameraControl.MV_CC_DestroyHandle(handle);
            if (MV_OK != nRet) {
                showLog("MV_CC_DestroyHandle fail! nRet" +Integer.toHexString( nRet));
            }
            showLog("关闭完毕...");
            isOpenFlag = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isOpenFlag) {
            closeDevice();
        }

    }

    public void setEnumBt(final boolean flag) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                enumDevicesBt.setEnabled(flag);
            }
        });
    }

    public void setCloseDevBt(final boolean flag) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                closeDeviceBt.setEnabled(flag);
            }
        });
    }

}
