package com.example.grabmultiplecamera;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.List;

import MvCameraControlWrapper.CameraControlException;
import MvCameraControlWrapper.MvCameraControl;
import MvCameraControlWrapper.MvCameraControlDefines;

import static MvCameraControlWrapper.MvCameraControlDefines.MV_OK;

public class GrabMultipleCameraActivity extends AppCompatActivity {


    private ArrayList<MvCameraControlDefines.MV_CC_DEVICE_INFO> deviceList1 = new ArrayList<>();
    private MaterialSpinner spinner1;
    TextView logTv1;
    int selectNum1 = 0;
    Button closeDeviceBt1;
    Button openDeviceBt1;
    Button enumDevicesBt1;
    GetOneFrameThread getOneFrameThread1 = null;


    private ArrayList<MvCameraControlDefines.MV_CC_DEVICE_INFO> deviceList2 = new ArrayList<>();
    private MaterialSpinner spinner2;
    TextView logTv2;
    int selectNum2 = 0;
    Button closeDeviceBt2;
    Button openDeviceBt2;
    Button enumDevicesBt2;
    GetOneFrameThread getOneFrameThread2 = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grab_multiple_camera_main);
        initView();
        initListener();
    }

    private void initView() {
        spinner1 = findViewById(R.id.spinner1);
        logTv1 = findViewById(R.id.logTv1);
        openDeviceBt1 = findViewById(R.id.openDeviceBt1);
        closeDeviceBt1 = findViewById(R.id.closeDeviceBt1);
        enumDevicesBt1 = findViewById(R.id.enumDevicesBt1);
        spinner1.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                selectNum1 = position;
            }
        });


        spinner2 = findViewById(R.id.spinner2);
        logTv2 = findViewById(R.id.logTv2);
        openDeviceBt2 = findViewById(R.id.openDeviceBt2);
        closeDeviceBt2 = findViewById(R.id.closeDeviceBt2);
        enumDevicesBt2 = findViewById(R.id.enumDevicesBt2);
        spinner2.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                selectNum2 = position;
            }
        });
    }

    private void initListener() {
        findViewById(R.id.enumDevicesBt1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectNum1 = 0;
                deviceList1.clear();
                spinner1.setItems(new ArrayList());
                try {
                    deviceList1 = MvCameraControl.MV_CC_EnumDevices(MvCameraControlDefines.MV_USB_DEVICE | MvCameraControlDefines.MV_GIGE_DEVICE);
                } catch (CameraControlException e) {
                    e.printStackTrace();
                    return;
                }

                if (deviceList1 != null) {
                    int size = deviceList1.size();
                    if (size > 0) {
                        List<String> deviceName = new ArrayList<>();
                        for (int i = 0; i < size; i++) {
                            MvCameraControlDefines.MV_CC_DEVICE_INFO entity = deviceList1.get(i);
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
                        spinner1.setItems(deviceName);
                    } else {
                        spinner1.setItems(new ArrayList());
                    }
                }
            }
        });
        findViewById(R.id.enumDevicesBt2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectNum2 = 0;
                deviceList2.clear();
                spinner2.setItems(new ArrayList());
                try {
                    deviceList2 = MvCameraControl.MV_CC_EnumDevices(MvCameraControlDefines.MV_USB_DEVICE | MvCameraControlDefines.MV_GIGE_DEVICE);
                } catch (CameraControlException e) {
                    e.printStackTrace();
                    return;
                }

                if (deviceList2 != null) {
                    int size = deviceList2.size();
                    if (size > 0) {
                        List<String> deviceName = new ArrayList<>();
                        for (int i = 0; i < size; i++) {
                            MvCameraControlDefines.MV_CC_DEVICE_INFO entity = deviceList2.get(i);
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
                        spinner2.setItems(deviceName);
                    } else {
                        spinner2.setItems(new ArrayList());
                    }
                }
            }
        });

        findViewById(R.id.openDeviceBt1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDevice1();
            }
        });
        findViewById(R.id.closeDeviceBt1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDevice1();
            }
        });
        findViewById(R.id.openDeviceBt2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDevice2();
            }
        });
        findViewById(R.id.closeDeviceBt2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDevice2();
            }
        });
    }

    private void openDevice1() {
        if (getOneFrameThread1 == null) {
            getOneFrameThread1 = new GetOneFrameThread(1, logTv1);
            getOneFrameThread1.flag = false;
            getOneFrameThread1.start();
        } else {
            if (!getOneFrameThread1.isAlive()) {
                getOneFrameThread1.flag = false;
                getOneFrameThread1.start();
            }else {

            }
        }
    }

    private void openDevice2() {
        if (getOneFrameThread2 == null) {
            getOneFrameThread2 = new GetOneFrameThread(2, logTv2);
            getOneFrameThread2.flag = false;
            getOneFrameThread2.start();
        } else {
            if (!getOneFrameThread2.isAlive()) {
                getOneFrameThread2.flag = false;
                getOneFrameThread2.start();
            }
        }
    }

    private void closeDevice1() {
        if (getOneFrameThread1 != null) {
            getOneFrameThread1.flag = true;
        }
    }

    private void closeDevice2() {
        if (getOneFrameThread2 != null) {
            getOneFrameThread2.flag = true;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getOneFrameThread1 != null) {
            getOneFrameThread1.flag = true;
        }
        if (getOneFrameThread2 != null) {
            getOneFrameThread2.flag = true;
        }
    }

    class GetOneFrameThread extends Thread {
        byte[] datas = null;
        MvCameraControlDefines.Handle handle = null;
        TextView logView;
        boolean flag = false;
        int deviceNum = 0;
        MvCameraControlDefines.MV_FRAME_OUT_INFO frameInfo = new MvCameraControlDefines.MV_FRAME_OUT_INFO();

        public GetOneFrameThread(int deviceNum, TextView logView) {
            this.deviceNum = deviceNum;
            this.logView = logView;
        }

        void showLog(final String str) {
            if (logView != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (logView.getLineCount() > 150) {
                            logView.setText("");
                            return;
                        }
                        logView.append(str + "\n");
                        int offset = logView.getLineCount() * logView.getLineHeight();
                        if (offset > logView.getHeight()) {
                            logView.scrollTo(0, offset - logView.getHeight());
                        }
                    }
                });
            }
        }

        @Override
        public void run() {
            super.run();

            int nRet;
            try {
                if (deviceNum == 1) {
                    setEnumBt1(false);
                    setCloseDevBt1(false);
                    showLog("正在打开...");
                    if (deviceList1 != null && deviceList1.size() > 0) {
                        try {
                            handle = MvCameraControl.MV_CC_CreateHandle(deviceList1.get(selectNum1));
                        } catch (CameraControlException e) {
                            e.printStackTrace();
                        }
                        if (handle == null) {
                            showLog("CreateHandle fail! nRet");
                            return;
                        }
                    } else {
                        showLog("请先枚举设备");
                        return;
                    }
                } else if (deviceNum == 2) {
                    setEnumBt2(false);
                    setCloseDevBt2(false);
                    showLog("正在打开...");
                    if (deviceList2 != null && deviceList2.size() > 0) {
                        try {
                            handle = MvCameraControl.MV_CC_CreateHandle(deviceList2.get(selectNum2));
                        } catch (CameraControlException e) {
                            e.printStackTrace();
                        }
                        if (handle == null) {
                            showLog("CreateHandle fail! nRet");
                            return;
                        }
                    } else {
                        showLog("请先枚举设备");
                        return;
                    }
                } else {
                    showLog("未知错误");
                    return;
                }

                nRet = MvCameraControl.MV_CC_OpenDevice(handle);
                if (nRet != 0) {
                    showLog("OpenDevice fail! nRet " + Integer.toHexString(nRet));

                    return;
                }
                if (deviceList1.get(selectNum1).transportLayerType == MvCameraControlDefines.MV_GIGE_DEVICE) {
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
                nRet = MvCameraControl.MV_CC_SetFloatValue(handle,"AcquisitionFrameRate",2);
                if (MV_OK != nRet) {
                    showLog("AcquisitionFrameRate fail! nRet " + Integer.toHexString(nRet));
                    //return;
                }
                nRet = MvCameraControl.MV_CC_SetBoolValue(handle,"AcquisitionFrameRateEnable",true);
                if (MV_OK != nRet) {
                    showLog("AcquisitionFrameRateEnable fail! nRet " + Integer.toHexString(nRet));
                   // return;
                }


                nRet = MvCameraControl.MV_CC_SetEnumValue(handle, "TriggerMode", 0);
                if (MV_OK != nRet) {
                    showLog("MV_CC_SetTriggerMode fail! nRet " + Integer.toHexString(nRet));
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
                // start grab image
                nRet = MvCameraControl.MV_CC_StartGrabbing(handle);

                if (MV_OK != nRet) {
                    showLog("MV_CC_StartGrabbing fail! nRet" + nRet);
                    return;
                }
            } finally {
                if (deviceNum == 1){
                    setEnumBt1(true);
                    setCloseDevBt1(true);
                    showLog("打开完毕...");
                }else {
                    setEnumBt2(true);
                    setCloseDevBt2(true);
                    showLog("打开完毕...");
                }
            }

            while (true) {
                if (flag) {
                    showLog("正在关闭...");
                    // 停止取流
                    // end grab image
                    nRet = MvCameraControl.MV_CC_StopGrabbing(handle);
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
                    showLog("关闭完成...");
                    return;
                } else {
                    int nRet1 = MvCameraControl.MV_CC_GetOneFrameTimeout(handle, datas, frameInfo, 1000);
                    if (nRet1 == MV_OK) {
                        showLog("GetOneFrame, Width:" + frameInfo.width +
                                "Height:" + frameInfo.height +
                                "nFrameNum" + frameInfo.frameNum);
                    }
                }
            }
        }
    }



    public void setEnumBt1(final boolean flag) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                enumDevicesBt1.setEnabled(flag);
            }
        });
    }


    public void setEnumBt2(final boolean flag) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                enumDevicesBt2.setEnabled(flag);
            }
        });
    }

    public void setCloseDevBt1(final boolean flag) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                closeDeviceBt1.setEnabled(flag);
            }
        });
    }

    public void setCloseDevBt2(final boolean flag) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                closeDeviceBt2.setEnabled(flag);
            }
        });
    }

}
