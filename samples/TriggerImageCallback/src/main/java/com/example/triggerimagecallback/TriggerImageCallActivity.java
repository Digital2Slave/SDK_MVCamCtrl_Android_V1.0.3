package com.example.triggerimagecallback;

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

public class TriggerImageCallActivity extends AppCompatActivity implements IMainView {

    private ArrayList<MvCameraControlDefines.MV_CC_DEVICE_INFO> deviceList = new ArrayList<>();
    private MaterialSpinner spinner;
    EditText logTv;
    int selectNum = 0;
    Button enumDevicesBt;
    Button closeDeviceBt;
    Button openDeviceBt;
    private final int MV_TRIGGER_SOURCE_SOFTWARE = 7;
    GetOneFrameThread getOneFrameThread;
    OpenDeviceThread openDeviceThread;
    boolean g_bIsGetImage = true;
    MvCameraControlDefines.Handle handle = null;
    boolean isOpenFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trigger_image_call);
        initView();
        initListener();
    }

    private void initView() {
        spinner = findViewById(R.id.spinner);
        logTv = findViewById(R.id.logTv);
        logTv.setMovementMethod(ScrollingMovementMethod.getInstance());
        openDeviceBt = findViewById(R.id.openDeviceBt);
        enumDevicesBt = findViewById(R.id.enumDevicesBt);
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
                Toast.makeText(TriggerImageCallActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openDevice() {
        if (getOneFrameThread == null || !getOneFrameThread.isAlive()) {
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
        }
    }

    private void closeDevice() {
        // 停止取流
        if (getOneFrameThread != null) {
            if (getOneFrameThread.isAlive()) {
                getOneFrameThread.falg = false;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isOpenFlag) {
            closeDevice();
        }
    }

    class GetOneFrameThread extends Thread {
        boolean falg = true;
        IExitGetOneFrame iExitGetOneFrame;

        public GetOneFrameThread(IExitGetOneFrame iExitGetOneFrame) {
            this.iExitGetOneFrame = iExitGetOneFrame;
        }

        @Override
        public void run() {
            super.run();
            while (true) {
                if (!falg) {
                    if (iExitGetOneFrame != null) {
                        iExitGetOneFrame.exit();
                    }
                    return;
                } else {

                    int nRet1 = MvCameraControl.MV_CC_SetCommandValue(handle, "TriggerSoftware");
                    if (MV_OK != nRet1) {
                        showLog("failed in TriggerSoftware" + Integer.toHexString(nRet1));
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
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


                    nRet = MvCameraControl.MV_CC_SetBoolValue(handle, "AcquisitionFrameRateEnable", false);
                    if (MV_OK != nRet) {
                        showLog("set AcquisitionFrameRateEnable fail! nRet " + Integer.toHexString(nRet));
                        return;
                    }

                    // 设置触发模式为on
                    // set trigger mode as on
                    nRet = MvCameraControl.MV_CC_SetEnumValue(handle, "TriggerMode", 1);
                    if (MV_OK != nRet) {
                        showLog("MV_CC_SetTriggerMode fail! nRet" + Integer.toHexString(nRet));
                        return;
                    }

                    // 设置触发源
                    // set trigger source
                    nRet = MvCameraControl.MV_CC_SetEnumValue(handle, "TriggerSource", MV_TRIGGER_SOURCE_SOFTWARE);
                    if (MV_OK != nRet) {
                        showLog("MV_CC_SetTriggerSource fail! nRet " + Integer.toHexString(nRet));
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
                                    // g_bIsGetImage = true;
                                }
                            });

                            return 0;
                        }
                    });
                    if (MV_OK != nRet) {
                        showLog("MV_CC_RegisterImageCallBackEx fail! nRet" + Integer.toHexString(nRet));
                        return;
                    }
                    // 开始取流
                    // start grab image
                    nRet = MvCameraControl.MV_CC_StartGrabbing(handle);
                    if (MV_OK != nRet) {
                        showLog("MV_CC_StartGrabbing fail! nRet" + Integer.toHexString(nRet));
                        return;
                    }


                    if (getOneFrameThread == null) {
                        getOneFrameThread = new GetOneFrameThread(new IExitGetOneFrame() {
                            @Override
                            public void exit() {
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
                                    showLog("MV_CC_DestroyHandle fail! nRet" +Integer.toHexString(nRet));
                                }
                                showLog("关闭完成...");
                                isOpenFlag = false;
                            }
                        });
                        getOneFrameThread.falg = true;
                        getOneFrameThread.start();

                    } else {
                        getOneFrameThread.falg = true;
                        getOneFrameThread.start();
                    }
                    g_bIsGetImage = true;

                } else {
                    toast("请先枚举设备");
                }
            } finally {
                setEnumBt(true);
                setCloseDevBt(true);
                isOpenFlag = true;
                showLog("打开完毕...");
            }
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
