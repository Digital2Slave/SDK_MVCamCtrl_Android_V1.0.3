package com.example.reconnect;

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
import MvCameraControlWrapper.CameraExceptionCallBack;
import MvCameraControlWrapper.CameraImageCallBack;
import MvCameraControlWrapper.MvCameraControl;
import MvCameraControlWrapper.MvCameraControlDefines;

import static MvCameraControlWrapper.MvCameraControlDefines.MV_GIGE_DEVICE;
import static MvCameraControlWrapper.MvCameraControlDefines.MV_OK;
import static MvCameraControlWrapper.MvCameraControlDefines.MV_USB_DEVICE;

public class ReconnectActivity extends AppCompatActivity implements IMainView {

    private ArrayList<MvCameraControlDefines.MV_CC_DEVICE_INFO> deviceList = new ArrayList<>();
    private MaterialSpinner spinner;
    EditText logTv;
    int selectNum = 0;
    Button closeDeviceBt;
    Button openDeviceBt;
    Button enumDevicesBt;
    private boolean isConnect = false;
    private ReconnectThread reconnectThread;
    private String strSerialNumber = "";
    private CameraExceptionCallBack cameraExceptionCallBack = null;
    private CameraImageCallBack cameraImageCallBack = null;
    private OpenDeviceThread openDeviceThread = null;
    private CloseDeviceThread closeDeviceThread = null;
    MvCameraControlDefines.Handle handle = null;
    boolean isOpenFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reconnect_main);
        initView();
        initListener();
        cameraExceptionCallBack = new CameraExceptionCallBack() {
            @Override
            public int OnExceptionCallBack(int err) {
                showLog("cameraExceptionCallBack");
                isConnect = false;
                if (reconnectThread == null) {
                    reconnectThread = new ReconnectThread();
                    reconnectThread.start();
                } else {
                    reconnectThread.start();
                }

                return 0;
            }
        };
        cameraImageCallBack = new CameraImageCallBack() {
            @Override
            public int OnImageCallBack(byte[] bytes, MvCameraControlDefines.MV_FRAME_OUT_INFO mv_frame_out_info) {
                showLog("GetOneFrame, Width:" + mv_frame_out_info.width +
                        "Height:" + mv_frame_out_info.height +
                        "nFrameNum" + mv_frame_out_info.frameNum);
                return 0;
            }
        };

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

        enumDevicesBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectNum = 0;
                deviceList.clear();
                spinner.setItems(new ArrayList());
                try {
                    deviceList = MvCameraControl.MV_CC_EnumDevices(MvCameraControlDefines.MV_USB_DEVICE | MvCameraControlDefines.MV_GIGE_DEVICE);
                } catch (CameraControlException e) {
                    e.printStackTrace();
                    return;
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
                Toast.makeText(ReconnectActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openDevice() {
        if (isOpenFlag){
            showLog("请先关闭设备");
            return;
        }
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

    private void closeDevice() {
        isOpenFlag = false;
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    class OpenDeviceThread extends Thread {
        @Override
        public void run() {
            super.run();

            try {
                setEnumBt(false);
                setCloseBt(false);
                showLog("正在打开...");


                if (reconnectThread != null) {
                    if (reconnectThread.isAlive()) {
                        try {
                            reconnectThread.stop();
                        } catch (UnsupportedOperationException e) {
                            e.printStackTrace();
                        } finally {
                            reconnectThread = null;
                        }
                    }
                }

                if (deviceList != null && deviceList.size() > 0) {
                    MvCameraControlDefines.MV_CC_DEVICE_INFO info = deviceList.get(selectNum);
                    if (info.transportLayerType == MV_GIGE_DEVICE) {
                        strSerialNumber = info.gigEInfo.serialNumber;
                    } else if (info.transportLayerType == MV_USB_DEVICE) {
                        strSerialNumber = info.usb3VInfo.serialNumber;
                    }
                    try {
                        handle = MvCameraControl.MV_CC_CreateHandle(info);
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
                    }else{
                        isOpenFlag = true;
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
                    nRet = MvCameraControl.MV_CC_RegisterImageCallBack(handle, cameraImageCallBack);

                    if (MV_OK != nRet) {
                        showLog("MV_CC_RegisterImageCallBackEx fail! nRet" + nRet);
                        return;
                    }

                    nRet = MvCameraControl.MV_CC_RegisterExceptionCallBack(handle, cameraExceptionCallBack);
                    if (MV_OK != nRet) {
                        showLog("RegisterExceptionCallBackBt fail! nRet" + nRet);
                        return;
                    }
                    // 开始取流
                    // start grab image
                    nRet = MvCameraControl.MV_CC_StartGrabbing(handle);
                    if (MV_OK != nRet) {
                        showLog("MV_CC_StartGrabbing fail! nRet" + nRet);
                        return;
                    }
                    isConnect = true;

                } else {
                    toast("请先枚举设备");
                }
            } finally {
                setEnumBt(true);
                setCloseBt(true);
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
                return;
            }
            // 关闭设备
            // close device
            nRet = MvCameraControl.MV_CC_CloseDevice(handle);
            if (MV_OK != nRet) {
                showLog("MV_CC_CloseDevice fail! nRet " + nRet);
                return;
            }
            // 销毁句柄
            // destroy handle
            nRet = MvCameraControl.MV_CC_DestroyHandle(handle);
            if (MV_OK != nRet) {
                showLog("MV_CC_DestroyHandle fail! nRet" + nRet);
                return;
            }
            showLog("关闭完毕...");
            setEnumBt(true);
        }
    }

    class ReconnectThread extends Thread {

        private ArrayList<MvCameraControlDefines.MV_CC_DEVICE_INFO> reDeviceList = new ArrayList<>();
        private int reSelectNum = 0;

        @Override
        public void run() {
            super.run();
            for (int x = 0; x < 10; x++) {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isConnect) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                if (!isConnect) {
                    MvCameraControl.MV_CC_StopGrabbing(handle);
                    MvCameraControl.MV_CC_CloseDevice(handle);
                    MvCameraControl.MV_CC_DestroyHandle(handle);
                }
                reDeviceList.clear();
                try {
                    reDeviceList = MvCameraControl.MV_CC_EnumDevices(MvCameraControlDefines.MV_USB_DEVICE | MvCameraControlDefines.MV_GIGE_DEVICE);
                } catch (CameraControlException e) {
                    e.printStackTrace();
                }
                showLog("Reconnect enumDevices");
                boolean findDevice = false;
                for (int i = 0; i < reDeviceList.size(); i++) {
                    if (reDeviceList.get(i).transportLayerType == MV_GIGE_DEVICE) {
                        if (reDeviceList.get(i).gigEInfo.serialNumber.equals(strSerialNumber)) {
                            reSelectNum = i;
                            findDevice = true;
                            break;
                        }
                    } else if (reDeviceList.get(i).transportLayerType == MV_USB_DEVICE) {
                        if (reDeviceList.get(i).usb3VInfo.serialNumber.equals(strSerialNumber)) {
                            reSelectNum = i;
                            findDevice = true;
                            break;
                        }
                    }
                }
                if (findDevice) {
                    try {
                        handle = MvCameraControl.MV_CC_CreateHandle(reDeviceList.get(reSelectNum));
                    } catch (CameraControlException e) {
                        e.printStackTrace();
                    }
                    if (handle == null) {
                        showLog("CreateHandle fail! nRet");
                        continue;
                    } else {
                        showLog("Reconnect createHandle");
                    }
                    int nRet = MvCameraControl.MV_CC_OpenDevice(handle);
                    if (nRet != MV_OK) {
                        showLog("OpenDevice fail! nRet " + Integer.toHexString(nRet));
                        continue;
                    } else {
                        showLog("Reconnect openDevice");
                    }
                    nRet = MvCameraControl.MV_CC_RegisterImageCallBack(handle, cameraImageCallBack);
                    if (nRet != MV_OK) {
                        showLog("RegisterImageCallBackEx fail! nRet" + nRet);
                        continue;
                    } else {
                        showLog("Reconnect registerImageCallBack");
                    }
                    nRet = MvCameraControl.MV_CC_RegisterExceptionCallBack(handle, cameraExceptionCallBack);
                    if (nRet != MV_OK) {
                        showLog("RegisterExceptionCallBackBt fail! nRet" + nRet);
                        continue;
                    } else {
                        showLog("Reconnect RegisterExceptionCallBackBt");
                    }
                    nRet = MvCameraControl.MV_CC_StartGrabbing(handle);
                    if (nRet != MV_OK) {
                        showLog("StartGrabbing fail! nRet" + nRet);
                        continue;
                    } else {
                        showLog("Reconnect StartGrabbing");
                        isConnect = true;
                        break;
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

    public void setCloseBt(final boolean flag) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                closeDeviceBt.setEnabled(flag);
            }
        });
    }

}
