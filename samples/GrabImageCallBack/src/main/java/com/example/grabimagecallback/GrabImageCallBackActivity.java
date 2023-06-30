package com.example.grabimagecallback;

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
import MvCameraControlWrapper.CameraImageCallBack;
import MvCameraControlWrapper.MvCameraControl;
import MvCameraControlWrapper.MvCameraControlDefines;

import static MvCameraControlWrapper.MvCameraControlDefines.MV_OK;

public class GrabImageCallBackActivity extends AppCompatActivity implements IMainView {
    private ArrayList<MvCameraControlDefines.MV_CC_DEVICE_INFO> deviceList = new ArrayList<>();
    private MaterialSpinner spinner;
    EditText logTv;
    int selectNum = 0;
    Button enumDevicesBt;
    Button closeDeviceBt;
    Button openDeviceBt;
    OpenDeviceThread openDeviceThread = null;
    CloseDeviceThread closeDeviceThread = null;
    MvCameraControlDefines.Handle handle = null;
    boolean isOpenFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grabimagecallback_main);
        initView();
        initListener();
    }

    private void initView() {
        spinner = findViewById(R.id.spinner);
        logTv = findViewById(R.id.logTv);
        enumDevicesBt = findViewById(R.id.enumDevicesBt);
        logTv.setMovementMethod(ScrollingMovementMethod.getInstance());
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

    public void toast(final String string) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(GrabImageCallBackActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
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
                    showLog("请稍后再试");
                }
            }
        } else {
            showLog("请先关闭设备");
        }
    }

    private void closeDevice() {
        if (isOpenFlag) {
            if (closeDeviceThread == null) {
                closeDeviceThread = new CloseDeviceThread();
                closeDeviceThread.start();
            } else {
                if (!closeDeviceThread.isAlive()) {
                    closeDeviceThread.start();
                } else {
                    showLog("请稍后再试");
                }
            }
        }else {
            showLog("请先打开设备");
        }
    }

    class OpenDeviceThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                setEnumBt(false);
                setCloseDevBt(false);
                showLog("正在打开...");
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
                    nRet = MvCameraControl.MV_CC_SetEnumValue(handle, "TriggerMode", 0);
                    if (MV_OK != nRet) {
                        showLog("MV_CC_SetTriggerMode fail! nRet " + Integer.toHexString(nRet));
                        return;
                    }

                    // 注册抓图回调
                    // register image callback
                    nRet = MvCameraControl.MV_CC_RegisterImageCallBack(handle, new CameraImageCallBack() {
                        @Override
                        public int OnImageCallBack(byte[] bytes, final MvCameraControlDefines.MV_FRAME_OUT_INFO mv_frame_out_info) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showLog("GetOneFrame, Width:" + mv_frame_out_info.width +
                                            "Height:" + mv_frame_out_info.height +
                                            "nFrameNum" + mv_frame_out_info.frameNum);
                                }
                            });

                            return 0;
                        }
                    });
                    if (MV_OK != nRet) {
                        showLog("MV_CC_RegisterImageCallBackEx fail! nRet" + nRet);
                        return;
                    }
                    // 开始取流
                    // start grab image
                    nRet = MvCameraControl.MV_CC_StartGrabbing(handle);
                    if (MV_OK != nRet) {
                        showLog("MV_CC_StartGrabbing fail! nRet" + nRet);
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

    class CloseDeviceThread extends Thread {
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
                showLog("MV_CC_CloseDevice fail! nRet " + nRet);
            }
            // 销毁句柄
            // destroy handle
            nRet = MvCameraControl.MV_CC_DestroyHandle(handle);
            if (MV_OK != nRet) {
                showLog("MV_CC_DestroyHandle fail! nRet" + nRet);
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
