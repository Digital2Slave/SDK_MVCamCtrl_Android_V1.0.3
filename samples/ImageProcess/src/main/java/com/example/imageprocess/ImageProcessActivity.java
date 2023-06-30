package com.example.imageprocess;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.hk.basemodule.FileUtils;
import com.example.hk.basemodule.IMainView;
import com.example.hk.basemodule.LogView;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.List;

import MvCameraControlWrapper.CameraControlException;
import MvCameraControlWrapper.MvCameraControl;
import MvCameraControlWrapper.MvCameraControlDefines;

public class ImageProcessActivity extends AppCompatActivity implements View.OnClickListener, IMainView {

    private ArrayList<MvCameraControlDefines.MV_CC_DEVICE_INFO> deviceList = new ArrayList<>();
    private MaterialSpinner spinner;
    int selectNum = 0;
    EditText logTv;
    ProcessThread processThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_process);
        findViewById(R.id.convertPixelTypeBt).setOnClickListener(this);
        initView();
        initListener();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.convertPixelTypeBt: {
                convertPixelType();
                break;
            }
        }
    }


    private void initView() {
        logTv = findViewById(R.id.logTv);
        logTv.setMovementMethod(ScrollingMovementMethod.getInstance());
        spinner = findViewById(R.id.spinner);
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

    }

    @Override
    public void showLog(final String str) {
        LogView.showLog(this,logTv,str);
    }

    @Override
    public void updateButton(int state) {

    }

    @Override
    public void updateImage(byte[] data) {

    }

    private void convertPixelType() {
        if (processThread == null) {
            processThread = new ProcessThread();
            processThread.start();
        } else {
            if (!processThread.isAlive()) {
                processThread.start();
            } else {
                showLog("请稍后再试");
            }
        }
    }


    class ProcessThread extends Thread {
        @Override
        public void run() {
            super.run();

            if (deviceList.size() == 0) {
                showLog("清先枚举设备");
                return;
            }
            MvCameraControlDefines.Handle handle = null;
            try {
                handle = MvCameraControl.MV_CC_CreateHandle(deviceList.get(selectNum));

            } catch (CameraControlException e) {
                e.printStackTrace();
                return;
            }
            if (handle == null) {
                return;
            }

            //bayerrg8
            MvCameraControlDefines.MV_CC_PIXEL_CONVERT_PARAM param = new MvCameraControlDefines.MV_CC_PIXEL_CONVERT_PARAM();
            param.width = 1024;
            param.height = 1024;
            param.srcPixelType = MvCameraControlDefines.MvGvspPixelType.PixelType_Gvsp_BayerRG8;
            param.dstPixelType = MvCameraControlDefines.MvGvspPixelType.PixelType_Gvsp_RGB8_Packed;

            param.srcData = FileUtils.getFromRaw(ImageProcessActivity.this, R.raw.bayerrg8); //mono

            param.srcDataLen = param.srcData.length;
            Log.e("param.nSrcDataLen", "param.nSrcDataLen" + param.srcDataLen);
            param.dstBuffer = new byte[1024 * 1024 * 4];
            param.dstLen = 0;
            param.dstBufferSize = 1024 * 1024 * 4;
            int nRet = MvCameraControl.MV_CC_ConvertPixelType(handle, param);
            if (nRet == 0) {
                Log.e("转换成功", "param.nDstLen" + param.dstLen);
            }
            showLog("ConvertPixelType:nRet" + Integer.toHexString(nRet));
            FileUtils.createFileWithByte(param.dstBuffer, "bayerrg8toRgb");


            //bayerrg10
            param = new MvCameraControlDefines.MV_CC_PIXEL_CONVERT_PARAM();
            param.width = 1024;
            param.height = 1024;
            param.srcPixelType = MvCameraControlDefines.MvGvspPixelType.PixelType_Gvsp_BayerRG10;
            param.dstPixelType = MvCameraControlDefines.MvGvspPixelType.PixelType_Gvsp_RGB8_Packed;
            //获取raw 文件夹中源数据
            param.srcData = FileUtils.getFromRaw(ImageProcessActivity.this, R.raw.bayerrg10); //mono

            param.srcDataLen = param.srcData.length;
            Log.e("param.nSrcDataLen", "param.nSrcDataLen" + param.srcDataLen);
            param.dstBuffer = new byte[1024 * 1024 * 4];
            param.dstLen = 0;
            param.dstBufferSize = 1024 * 1024 * 4;
            nRet = MvCameraControl.MV_CC_ConvertPixelType(handle, param);
            if (nRet == 0) {
                Log.e("转换成功", "param.nDstLen" + param.dstLen);
            }
            showLog("ConvertPixelType:nRet" + Integer.toHexString(nRet));
            FileUtils.createFileWithByte(param.dstBuffer, "BayerRG10toRgb");


            //bayerrg10packed
            param = new MvCameraControlDefines.MV_CC_PIXEL_CONVERT_PARAM();
            param.width = 1024;
            param.height = 1024;
            param.srcPixelType = MvCameraControlDefines.MvGvspPixelType.PixelType_Gvsp_BayerRG10_Packed;
            param.dstPixelType = MvCameraControlDefines.MvGvspPixelType.PixelType_Gvsp_RGB8_Packed;

            param.srcData = FileUtils.getFromRaw(ImageProcessActivity.this, R.raw.bayerrg10packed); //mono

            param.srcDataLen = param.srcData.length;
            Log.e("param.nSrcDataLen", "param.nSrcDataLen" + param.srcDataLen);
            param.dstBuffer = new byte[1024 * 1024 * 4];
            param.dstLen = 0;
            param.dstBufferSize = 1024 * 1024 * 4;
            nRet = MvCameraControl.MV_CC_ConvertPixelType(handle, param);
            if (nRet == 0) {
                Log.e("转换成功", "param.nDstLen" + param.dstLen);
            }
            showLog("ConvertPixelType:nRet" + Integer.toHexString(nRet));
            FileUtils.createFileWithByte(param.dstBuffer, "BayerRG10PackedtoRgb");


            //bayerrg12
            param = new MvCameraControlDefines.MV_CC_PIXEL_CONVERT_PARAM();
            param.width = 1024;
            param.height = 1024;
            param.srcPixelType = MvCameraControlDefines.MvGvspPixelType.PixelType_Gvsp_BayerRG12;
            param.dstPixelType = MvCameraControlDefines.MvGvspPixelType.PixelType_Gvsp_RGB8_Packed;

            param.srcData = FileUtils.getFromRaw(ImageProcessActivity.this, R.raw.bayerrg12); //mono

            param.srcDataLen = param.srcData.length;
            Log.e("param.nSrcDataLen", "param.nSrcDataLen" + param.srcDataLen);
            param.dstBuffer = new byte[1024 * 1024 * 4];
            param.dstLen = 0;
            param.dstBufferSize = 1024 * 1024 * 4;
            nRet = MvCameraControl.MV_CC_ConvertPixelType(handle, param);
            if (nRet == 0) {
                Log.e("转换成功", "param.nDstLen" + param.dstLen);
            }
            showLog("ConvertPixelType:nRet" + Integer.toHexString(nRet));
            FileUtils.createFileWithByte(param.dstBuffer, "BayerRG12toRgb");


            //bayerrg12
            param = new MvCameraControlDefines.MV_CC_PIXEL_CONVERT_PARAM();
            param.width = 1024;
            param.height = 1024;
            param.srcPixelType = MvCameraControlDefines.MvGvspPixelType.PixelType_Gvsp_BayerRG12_Packed;
            param.dstPixelType = MvCameraControlDefines.MvGvspPixelType.PixelType_Gvsp_RGB8_Packed;

            param.srcData = FileUtils.getFromRaw(ImageProcessActivity.this, R.raw.bayerrg12packed); //mono

            param.srcDataLen = param.srcData.length;
            Log.e("param.nSrcDataLen", "param.nSrcDataLen" + param.srcDataLen);
            param.dstBuffer = new byte[1024 * 1024 * 4];
            param.dstLen = 0;
            param.dstBufferSize = 1024 * 1024 * 4;
            nRet = MvCameraControl.MV_CC_ConvertPixelType(handle, param);
            if (nRet == 0) {
                Log.e("转换成功", "param.nDstLen" + param.dstLen);
            }
            showLog("ConvertPixelType:nRet" + Integer.toHexString(nRet));
            FileUtils.createFileWithByte(param.dstBuffer, "bayerrg12packedtoRgb");


            //bayerrg12
            param = new MvCameraControlDefines.MV_CC_PIXEL_CONVERT_PARAM();
            param.width = 1024;
            param.height = 1024;
            param.srcPixelType = MvCameraControlDefines.MvGvspPixelType.PixelType_Gvsp_Mono8;
            param.dstPixelType = MvCameraControlDefines.MvGvspPixelType.PixelType_Gvsp_RGB8_Packed;

            param.srcData = FileUtils.getFromRaw(ImageProcessActivity.this, R.raw.mono8); //mono

            param.srcDataLen = param.srcData.length;
            Log.e("param.nSrcDataLen", "param.nSrcDataLen" + param.srcDataLen);
            param.dstBuffer = new byte[1024 * 1024 * 4];
            param.dstLen = 0;
            param.dstBufferSize = 1024 * 1024 * 4;
            nRet = MvCameraControl.MV_CC_ConvertPixelType(handle, param);
            if (nRet == 0) {
                Log.e("转换成功", "param.nDstLen" + param.dstLen);
            }
            showLog("ConvertPixelType:nRet" + Integer.toHexString(nRet));
            FileUtils.createFileWithByte(param.dstBuffer, "mono8toRgb");


            param = new MvCameraControlDefines.MV_CC_PIXEL_CONVERT_PARAM();
            param.width = 1024;
            param.height = 1024;
            param.srcPixelType = MvCameraControlDefines.MvGvspPixelType.PixelType_Gvsp_Mono10;
            param.dstPixelType = MvCameraControlDefines.MvGvspPixelType.PixelType_Gvsp_RGB8_Packed;

            param.srcData = FileUtils.getFromRaw(ImageProcessActivity.this, R.raw.mono10); //mono

            param.srcDataLen = param.srcData.length;
            Log.e("param.nSrcDataLen", "param.nSrcDataLen" + param.srcDataLen);
            param.dstBuffer = new byte[1024 * 1024 * 4];
            param.dstLen = 0;
            param.dstBufferSize = 1024 * 1024 * 4;
            nRet = MvCameraControl.MV_CC_ConvertPixelType(handle, param);
            if (nRet == 0) {
                Log.e("转换成功", "param.nDstLen" + param.dstLen);
            }
            showLog("ConvertPixelType:nRet" + Integer.toHexString(nRet));
            FileUtils.createFileWithByte(param.dstBuffer, "mono10toRgb");


            param = new MvCameraControlDefines.MV_CC_PIXEL_CONVERT_PARAM();
            param.width = 1024;
            param.height = 1024;
            param.srcPixelType = MvCameraControlDefines.MvGvspPixelType.PixelType_Gvsp_Mono10_Packed;
            param.dstPixelType = MvCameraControlDefines.MvGvspPixelType.PixelType_Gvsp_RGB8_Packed;

            param.srcData = FileUtils.getFromRaw(ImageProcessActivity.this, R.raw.mono10packed); //mono

            param.srcDataLen = param.srcData.length;
            Log.e("param.nSrcDataLen", "param.nSrcDataLen" + param.srcDataLen);
            param.dstBuffer = new byte[1024 * 1024 * 4];
            param.dstLen = 0;
            param.dstBufferSize = 1024 * 1024 * 4;
            nRet = MvCameraControl.MV_CC_ConvertPixelType(handle, param);
            if (nRet == 0) {
                Log.e("转换成功", "param.nDstLen" + param.dstLen);
            }
            showLog("ConvertPixelType:nRet" + Integer.toHexString(nRet));
            FileUtils.createFileWithByte(param.dstBuffer, "mono10packedtoRgb");


            param = new MvCameraControlDefines.MV_CC_PIXEL_CONVERT_PARAM();
            param.width = 1024;
            param.height = 1024;
            param.srcPixelType = MvCameraControlDefines.MvGvspPixelType.PixelType_Gvsp_Mono12;
            param.dstPixelType = MvCameraControlDefines.MvGvspPixelType.PixelType_Gvsp_RGB8_Packed;

            param.srcData = FileUtils.getFromRaw(ImageProcessActivity.this, R.raw.mono12); //mono

            param.srcDataLen = param.srcData.length;
            Log.e("param.nSrcDataLen", "param.nSrcDataLen" + param.srcDataLen);
            param.dstBuffer = new byte[1024 * 1024 * 4];
            param.dstLen = 0;
            param.dstBufferSize = 1024 * 1024 * 4;
            nRet = MvCameraControl.MV_CC_ConvertPixelType(handle, param);
            if (nRet == 0) {
                Log.e("转换成功", "param.nDstLen" + param.dstLen);
            }
            showLog("ConvertPixelType:nRet" + Integer.toHexString(nRet));
            FileUtils.createFileWithByte(param.dstBuffer, "mono12toRgb");


            param = new MvCameraControlDefines.MV_CC_PIXEL_CONVERT_PARAM();
            param.width = 1024;
            param.height = 1024;
            param.srcPixelType = MvCameraControlDefines.MvGvspPixelType.PixelType_Gvsp_Mono12_Packed;
            param.dstPixelType = MvCameraControlDefines.MvGvspPixelType.PixelType_Gvsp_RGB8_Packed;

            param.srcData = FileUtils.getFromRaw(ImageProcessActivity.this, R.raw.mono12packed); //mono

            param.srcDataLen = param.srcData.length;
            Log.e("param.nSrcDataLen", "param.nSrcDataLen" + param.srcDataLen);
            param.dstBuffer = new byte[1024 * 1024 * 3];
            param.dstLen = 0;
            param.dstBufferSize = 1024 * 1024 * 3;
            nRet = MvCameraControl.MV_CC_ConvertPixelType(handle, param);
            if (nRet == 0) {
                Log.e("转换成功", "param.nDstLen" + param.dstLen);
            }
            showLog("ConvertPixelType:nRet" + Integer.toHexString(nRet));
            FileUtils.createFileWithByte(param.dstBuffer, "mono12packedtoRgb");

            MvCameraControl.MV_CC_DestroyHandle(handle);
        }
    }
}
