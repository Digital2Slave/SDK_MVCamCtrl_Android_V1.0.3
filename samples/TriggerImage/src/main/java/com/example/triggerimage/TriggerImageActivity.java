package com.example.triggerimage;

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

public class TriggerImageActivity extends AppCompatActivity implements IMainView {

    private ArrayList<MvCameraControlDefines.MV_CC_DEVICE_INFO> deviceList = new ArrayList<>();
    private MaterialSpinner spinner;
    EditText logTv;
    int selectNum = 0;
    Button enumDevicesBt;
    Button closeDeviceBt;
    Button openDeviceBt;
    byte[] datas = null;
    MvCameraControlDefines.MV_FRAME_OUT_INFO info = new MvCameraControlDefines.MV_FRAME_OUT_INFO();
    GetOneFrameThread getOneFrameThread;
    private final int MV_TRIGGER_SOURCE_SOFTWARE = 7;
    MvCameraControlDefines.Handle handle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trigger_image);
        initView();
        initListener();
    }

    private void initView() {
        spinner = findViewById(R.id.spinner);
        logTv = findViewById(R.id.logTv);
        logTv.setMovementMethod(ScrollingMovementMethod.getInstance());
        openDeviceBt = findViewById(R.id.openDeviceBt);
        closeDeviceBt = findViewById(R.id.closeDeviceBt);
        enumDevicesBt = findViewById(R.id.enumDevicesBt);
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
                            } else if (entity.transportLayerType == MvCameraControlDefines.MV_USB_DEVICE) {
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

    private void openDevice() {
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
                        showLog("MV_CC_CloseDevice fail! nRet " + nRet);
                    }
                    // 销毁句柄
                    // destroy handle
                    nRet = MvCameraControl.MV_CC_DestroyHandle(handle);
                    if (MV_OK != nRet) {
                        showLog("MV_CC_DestroyHandle fail! nRet" + nRet);
                    }
                    showLog("关闭完毕");
                }
            });
            getOneFrameThread.start();
            getOneFrameThread.flag = false;
        } else {
            if (!getOneFrameThread.isAlive()) {
                getOneFrameThread.flag = false;
                getOneFrameThread.start();
            }else {
                showLog("请先关闭设备");
            }
        }
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
                Toast.makeText(TriggerImageActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void closeDevice() {

        if (getOneFrameThread != null) {
            getOneFrameThread.flag = true;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getOneFrameThread != null) {
            getOneFrameThread.flag = true;
        }
    }

    class GetOneFrameThread extends Thread {
        IExitGetOneFrame iExitGetOneFrame;

        boolean flag = false;

        public GetOneFrameThread(IExitGetOneFrame iExitGetOneFrame) {
            this.iExitGetOneFrame = iExitGetOneFrame;
        }

        @Override
        public void run() {
            super.run();
            try {
                setEnumBt(false);
                setCloseDevBt(false);
                showLog("正在打开...");
                if (deviceList != null && deviceList.size() > 0) {
                    int nRet = 0;
                    try {
                        handle = MvCameraControl.MV_CC_CreateHandle(deviceList.get(selectNum));
                    } catch (CameraControlException e) {
                        e.printStackTrace();
                    }
                    if (handle == null) {
                        showLog("CreateHandle fail! nRet");
                        return;
                    }
                    nRet = MvCameraControl.MV_CC_OpenDevice(handle);
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

//                    nRet = MvCameraControl.MV_CC_SetFloatValue(handle, "AcquisitionFrameRate", 2);
//                    if (MV_OK != nRet) {
//                        showLog("AcquisitionFrameRate fail! nRet " + Integer.toHexString(nRet));
//                        return;
//                    }
//                    nRet = MvCameraControl.MV_CC_SetBoolValue(handle, "AcquisitionFrameRateEnable", true);
//                    if (MV_OK != nRet) {
//                        showLog("AcquisitionFrameRateEnable fail! nRet " + Integer.toHexString(nRet));
//                        return;
//                    }

                    // 设置触发模式为on
                    // set trigger mode as on
                    nRet = MvCameraControl.MV_CC_SetEnumValue(handle, "TriggerMode", 1);
                    if (MV_OK != nRet) {
                        showLog("MV_CC_SetTriggerMode fail! nRet [%x]" + Integer.toHexString(nRet));
                        return;
                    }

                    // 设置触发源
                    // set trigger source
                    nRet = MvCameraControl.MV_CC_SetEnumValue(handle, "TriggerSource", MV_TRIGGER_SOURCE_SOFTWARE);
                    if (MV_OK != nRet) {
                        showLog("MV_CC_SetTriggerSource fail! nRet " + Integer.toHexString(nRet));
                        return;
                    }

                    Integer width = new Integer(0);
                    nRet = MvCameraControl.MV_CC_GetIntValue(handle, "Width", width);
                    if (nRet != MV_OK) {
                        showLog("GetWidth fail! nRet " + Integer.toHexString(nRet));
                        return;
                    }
                    Integer height = new Integer(0);
                    nRet = MvCameraControl.MV_CC_GetIntValue(handle, "Height", height);
                    if (nRet != MV_OK) {
                        showLog("GetHeight fail! nRet " + Integer.toHexString(nRet));
                        return;
                    }
                    datas = new byte[width * height * 3];


                    // 开始取流
                    // start grab image
                    nRet = MvCameraControl.MV_CC_StartGrabbing(handle);
                    if (MV_OK != nRet) {
                        showLog("MV_CC_StartGrabbing fail! nRet" + Integer.toHexString(nRet));
                        return;
                    }
                } else {
                    toast("请先枚举设备");
                    return;
                }
            } finally {
                setEnumBt(true);
                setCloseDevBt(true);
                showLog("打开完毕...");
            }


            while (true) {
                if (flag) {
                    if (iExitGetOneFrame != null) {
                        iExitGetOneFrame.exit();
                    }
                    return;
                } else {

                    int nRet1 = MvCameraControl.MV_CC_SetCommandValue(handle, "TriggerSoftware");
                    if (MV_OK != nRet1) {
                        showLog("failed in TriggerSoftware" + Integer.toHexString(nRet1));
                    }


                    nRet1 = MvCameraControl.MV_CC_GetOneFrameTimeout(handle, datas, info, 2000);
                    if (nRet1 == MV_OK) {
                        showLog("GetOneFrame, Width:" + info.width +
                                "Height:" + info.height +
                                "nFrameNum" + info.frameNum);
                    } else {
                        showLog("OneFrameTimeout fail：nRet = " + Integer.toHexString(nRet1));
                    }
                }
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
