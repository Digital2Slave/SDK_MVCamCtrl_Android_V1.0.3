package com.example.setIp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.hk.basemodule.IMainView;
import com.example.hk.basemodule.IpUtil;
import com.example.hk.basemodule.LogView;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.List;

import MvCameraControlWrapper.CameraControlException;
import MvCameraControlWrapper.MvCameraControl;
import MvCameraControlWrapper.MvCameraControlDefines;

public class SetIpActivity extends AppCompatActivity implements IMainView {
    private ArrayList<MvCameraControlDefines.MV_CC_DEVICE_INFO> deviceList = new ArrayList<>();
    private MaterialSpinner spinner;
    EditText logTv;
    int selectNum = 0;
    Button openDeviceBt;
    private EditText ipEdit;
    private EditText subNetMaskEdit;
    private EditText defaultGateWayEdit;
    MvCameraControlDefines.Handle handle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_ip);
        initView();
        initListener();
    }

    private void initView() {
        ipEdit = findViewById(R.id.ipEdit);
        subNetMaskEdit = findViewById(R.id.subNetMaskEdit);
        defaultGateWayEdit = findViewById(R.id.defaultGateWayEdit);

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


    }




    private void openDevice() {
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


            try {
                String ip = ipEdit.getText().toString();
                if (ip.length() == 0) {
                    toast("ip 不能为空");
                    return;
                }
                if (!IpUtil.isboolIp(ip)) {
                    toast("请输入正确的ip");
                    return;
                }


                String subNetMask = subNetMaskEdit.getText().toString();
                if (subNetMask.length() == 0) {
                    toast("subNetMask 不能为空");
                    return;
                }
                if (!IpUtil.isboolIp(subNetMask)) {
                    toast("请输入正确的 subNetMask");
                    return;
                }

                String defaultGateWay = defaultGateWayEdit.getText().toString();
                if (defaultGateWay.length() == 0) {
                    toast("defaultGateWay 不能为空");
                    return;
                }
                if (!IpUtil.isboolIp(defaultGateWay)) {
                    toast("请输入正确的 defaultGateWay");
                    return;
                }


                int nRet = MvCameraControl.MV_GIGE_ForceIp(handle, ip, subNetMask, defaultGateWay);
                if (nRet != MvCameraControlDefines.MV_OK) {
                    showLog("MV_GIGE_ForceIpEx fail! nRet " + Integer.toHexString(nRet));
                } else {
                    showLog("修改相机ip 成功");
                }
            } finally {
                if (handle != null){
                    MvCameraControl.MV_CC_DestroyHandle(handle);
                    handle = null;
                }
            }

        } else {
            toast("请先枚举设备");
            return;
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


    public void toast(String string) {
        Toast.makeText(SetIpActivity.this, string, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

}
