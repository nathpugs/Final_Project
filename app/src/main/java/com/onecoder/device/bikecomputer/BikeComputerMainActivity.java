package com.onecoder.device.bikecomputer;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.onecoder.device.R;
import com.onecoder.device.adpater.FileListAdapter;
import com.onecoder.device.base.BaseActivity;
import com.onecoder.devicelib.base.api.Manager;
import com.onecoder.devicelib.base.control.entity.BleDevice;
import com.onecoder.devicelib.base.control.interfaces.DeviceStateChangeCallback;
import com.onecoder.devicelib.base.entity.BaseDevice;
import com.onecoder.devicelib.bikecomputer.api.BikeComputerManager;
import com.onecoder.devicelib.bikecomputer.api.entity.DeleteFileSetting;
import com.onecoder.devicelib.bikecomputer.api.entity.FileInfo;
import com.onecoder.devicelib.bikecomputer.api.interfaces.GetFitFileDataCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/16.
 */
public class BikeComputerMainActivity extends BaseActivity implements View.OnClickListener, FileListAdapter.OnItemClickListener {
    public static final String KEY_DEVICE_VERSION = "deviceVersion";
    private static final String TAG = BikeComputerMainActivity.class.getSimpleName();

    private BaseDevice baseDevice;
    private BikeComputerManager bikeComputerManager;

    private ProgressDialog dialog;

    private LinearLayout layoutDeleteHistory;
    private LinearLayout layoutDeleteFile;
    private TextView tvHardwareVersion;
    private TextView textviewCurrentSelectedFileName;
    private CheckBox ckDeleteHistoryData;
    private CheckBox ckDeleteFile;

    private final String KEY_FILE_NAME = FileListAdapter.KEY_FILE_NAME;
    private final String KEY_FILE_SIZE = FileListAdapter.KEY_FILE_SIZE;
    private final String KEY_CHECK_DELETE_HISTORY_STATUS = FileListAdapter.KEY_CHECK_DELETE_HISTORY_STATUS;
    private final String KEY_CHECK_DELETE_FILE_STATUS = FileListAdapter.KEY_CHECK_DELETE_FILE_STATUS;
    private final String KEY_FILE_DOWNLOAD_STATUS = FileListAdapter.KEY_FILE_DOWNLOAD_STATUS;
    private final String KEY_CHECK_SELECT_STATUS = FileListAdapter.KEY_CHECK_SELECT_STATUS;
    private final String KEY_CHECK_HAS_FOCUS = FileListAdapter.KEY_CHECK_HAS_FOCUS;

    private final String VAL_BOOLEAN_FALSE = FileListAdapter.VAL_BOOLEAN_FALSE;
    private final String VAL_BOOLEAN_TRUE = FileListAdapter.VAL_BOOLEAN_TRUE;


    private String fileStorePath = BikeComputerManager.DEFAULT_FILE_DIRECTORY_PATH + "Test" + File.separator;

    private ListView listViewFileNameList;
    private FileListAdapter fileListAdapter;
    private List<Map<String, String>> mFileInfoList;

    private FileListAdapter.ViewHolder currentViewHolder;
    private String currentSelectedFileName;

    private boolean debugBatchDeleting = false;

    @Override
    protected Manager getManager(Bundle savedInstanceState) {
        if (bikeComputerManager != null) {
            return bikeComputerManager;
        }
        bikeComputerManager = BikeComputerManager.getInstance();
        //注册状态回调
        bikeComputerManager.registerStateChangeCallback(stateChangeCallback);
        bikeComputerManager.registerGetFitFileDataCallback(getFitFileDataCallback);
        return bikeComputerManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bike_computer_main_act);

        //requestPermisson();
        baseDevice = (BaseDevice) getIntent().getSerializableExtra(KEY_BASE_DEVICE);

        tvHardwareVersion = (TextView) findViewById(R.id.tv_hardware_version);
        layoutDeleteHistory = (LinearLayout) findViewById(R.id.layout_delete_history);
        layoutDeleteFile = (LinearLayout) findViewById(R.id.layout_delete_file);
        textviewCurrentSelectedFileName = (TextView) findViewById(R.id.text_view_current_selected_file_name);
        ckDeleteHistoryData = (CheckBox) findViewById(R.id.ck_delete_history_data);
        ckDeleteFile = (CheckBox) findViewById(R.id.ck_delete_file);
        listViewFileNameList = (ListView) findViewById(R.id.list_view_file_name_list);

        mFileInfoList = new ArrayList<Map<String, String>>();
        fileListAdapter = new FileListAdapter(this, mFileInfoList);
        fileListAdapter.setOnItemClickListener(this);
        fileListAdapter.setDebugBatchDeleting(debugBatchDeleting);

        listViewFileNameList.setAdapter(fileListAdapter);

        dialog = new ProgressDialog(this);
        tvHardwareVersion.setText("硬件版本:" + getIntent().getIntExtra(KEY_DEVICE_VERSION, 0));

        layoutDeleteHistory.setVisibility(!debugBatchDeleting ? View.VISIBLE : View.GONE);
        layoutDeleteFile.setVisibility(!debugBatchDeleting ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isAllow = false;
        int count = 0;
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                count++;
            }
        }
        if (count != grantResults.length) {
            Log.e("mainactivity", "Some Permission is Denied");
        }

    }

    /**
     * if you want you receive sms , call remind,save sport data and bluetooth,you must check permission,and request permission below
     */
    public void requestPermisson() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean readCallLog = (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED);
            boolean writeCallLog = (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED);
            boolean phoneState = (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
            boolean readSms = (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED);
            boolean readCall = (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED);
            boolean storage = (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            boolean contact = (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED);
            boolean location = (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
            if (!readCall || !readSms || !storage || !contact || !location || !phoneState || !readCallLog || !writeCallLog) {
                requestPermissions(new String[]{Manifest.permission.READ_SMS, Manifest.permission.CALL_PHONE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS,
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG}, 1);
            }
        }
    }

    private void showDialog(String meassge, boolean isShow) {
        if (isShow && !dialog.isShowing()) {
            dialog.setMessage(meassge);
            dialog.show();
            return;
        }
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.main_menu_disconnect:
                if (bikeComputerManager.getConnectState()
                        >= BleDevice.STATE_CONNECTED) {
                    bikeComputerManager.disconnect(false);
                }
                break;

            case R.id.main_menu_connect:
                if (bikeComputerManager.getConnectState()
                        < BleDevice.STATE_CONNECTED) {
                    bikeComputerManager.connectDevice(baseDevice);
                }
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        boolean ret = false;

        switch (v.getId()) {
            case R.id.btn_get_fit_file_name_list:
                showDialog("获取文件名列表...", true);
                bikeComputerManager.getFileNameList();
                break;

            case R.id.btn_get_special_fit_file:
                showDialog("获取文件" + currentSelectedFileName + "...", true);
                ret = bikeComputerManager.getFile(fileStorePath, currentSelectedFileName);
                Toast.makeText(this, " ret:" + ret, Toast.LENGTH_LONG).show();
                Log.i(TAG, " ret:" + ret);
                break;

            case R.id.btn_delete_file:
                Map<String, String> map;
                boolean deleteHistory = false;
                boolean deleteFile = false;

                if (debugBatchDeleting) {
                    List<DeleteFileSetting> deleteFileSettingList = new ArrayList<DeleteFileSetting>();
                    for (int i = 0; i < mFileInfoList.size(); i++) {
                        map = mFileInfoList.get(i);
                        if (!VAL_BOOLEAN_TRUE.equals(map.get(KEY_CHECK_SELECT_STATUS))) {
                            continue;
                        }
                        if (VAL_BOOLEAN_TRUE.equals(map.get(KEY_CHECK_DELETE_FILE_STATUS))) {
                            deleteFile = true;
                            mFileInfoList.remove(i);
                        }
                        deleteFileSettingList.add(new DeleteFileSetting(map.get(KEY_FILE_NAME),
                                VAL_BOOLEAN_TRUE.equals(map.get(KEY_CHECK_DELETE_HISTORY_STATUS)),
                                VAL_BOOLEAN_TRUE.equals(map.get(KEY_CHECK_DELETE_FILE_STATUS))));
                    }
                    ret = bikeComputerManager.deleteFiles(deleteFileSettingList);
                    Log.i(TAG, "deleteFile ret:" + ret + " deleteFileSettingList:" + deleteFileSettingList);
                } else {
                    deleteHistory = ckDeleteHistoryData.isChecked();
                    deleteFile = ckDeleteFile.isChecked();
                    ret = bikeComputerManager.deleteFile(currentSelectedFileName, deleteHistory, deleteFile);
                    for (int i = 0; i < mFileInfoList.size(); i++) {
                        map = mFileInfoList.get(i);
                        if (!TextUtils.isEmpty(currentSelectedFileName) && currentSelectedFileName.equals(map.get(KEY_FILE_NAME)) && deleteFile) {
                            mFileInfoList.remove(i);
                            break;
                        }
                    }
                    Log.i(TAG, "deleteFile ret:" + ret + " currentSelectedFileName:" + currentSelectedFileName);
                }
                if (ret && deleteFile) {
                    if (currentViewHolder != null) {
                        currentViewHolder.root_layout.setBackgroundColor(Color.TRANSPARENT);
                    }
                    currentViewHolder = null;
                    currentSelectedFileName = null;
                    textviewCurrentSelectedFileName.setText("");
                    fileListAdapter.notifyDataSetChanged();
                }
                Toast.makeText(this, "deleteFile ret:" + ret, Toast.LENGTH_LONG).show();
                break;

            case R.id.btn_set_time_zone:
                bikeComputerManager.setTimeZone();
                break;

            case R.id.setting_utc:
                bikeComputerManager.setUTC();
                break;

            default:
                break;
        }
    }

    public void updateConnectStatus(int status) {

        int connectSate = R.string.un_stpes_walk;
        switch (status) {
            case BleDevice.STATE_DISCONNECTED:  //断开连接
                break;
            case BleDevice.STATE_CONNECTING:  //正在连接
                connectSate = R.string.device_connecting;
                break;
            case BleDevice.STATE_CONNECTED:  //已连接
                connectSate = R.string.stpes_walk;
                break;
            case BleDevice.STATE_SERVICES_DISCOVERED:  //发现服务
            case BleDevice.STATE_SERVICES_OPENCHANNELSUCCESS:  //打开通道
                connectSate = R.string.stpes_walk;
                break;
        }
        setTitle("当前设备状态： " + getString(connectSate));

    }


    /**
     * 设备的连接状态回调
     */
    private DeviceStateChangeCallback stateChangeCallback = new DeviceStateChangeCallback() {
        /**
         * 设备的连接状态变化回调
         * @param mac ：mac地址
         * @param status ：状态值
         */
        @Override
        public void onStateChange(String mac, int status) {
            Log.i(TAG, "DeviceStateChangeCallback onStateChange mac:" + mac + " status:" + status);
            updateConnectStatus(status);
        }

        /**
         * 设备可以下发数据的回调
         * @param mac
         * @param isNeedSetParam
         */
        @Override
        public void onEnableWriteToDevice(String mac, boolean isNeedSetParam) {
            Log.i(TAG, "DeviceStateChangeCallback onEnableWriteToDevice mac:"
                    + mac + " isNeedSetParam:" + isNeedSetParam);
        }
    };

    /**
     * 获取fit文件数据回调
     */
    private GetFitFileDataCallback getFitFileDataCallback = new GetFitFileDataCallback() {
        /**
         * 获取到了文件名列表
         *
         * @param fileInfoList 文件名列表
         */
        @Override
        public void onGotFileNameList(List<FileInfo> fileInfoList) {
            dealGotFileNameListEvent(fileInfoList);
        }

        /**
         * 获取到了指定文件
         *
         * @param fileName         文件名。与获取时参入的文件名一样
         * @param fileAbsolutePath 文件的绝对路径
         */
        @Override
        public void onGotSpecifiedFile(String fileName, String fileAbsolutePath) {
            dealGotSpecifiedFileEvent(fileName, fileAbsolutePath);
        }
    };

    private void dealGotFileNameListEvent(List<FileInfo> fileInfoList) {
        if (fileInfoList == null) {
            return;
        }

        mFileInfoList.clear();
        Map<String, String> map;
        for (FileInfo fileInfo : fileInfoList) {
            map = new HashMap<String, String>();
            map.put(KEY_FILE_NAME, fileInfo.getFileName());
            map.put(KEY_FILE_SIZE, String.valueOf(fileInfo.getFileSizeInKBytes()));
            map.put(KEY_CHECK_DELETE_HISTORY_STATUS, VAL_BOOLEAN_FALSE);
            map.put(KEY_CHECK_DELETE_FILE_STATUS, VAL_BOOLEAN_FALSE);
            map.put(KEY_FILE_DOWNLOAD_STATUS, VAL_BOOLEAN_FALSE);
            map.put(KEY_CHECK_SELECT_STATUS, VAL_BOOLEAN_FALSE);
            map.put(KEY_CHECK_HAS_FOCUS, VAL_BOOLEAN_FALSE);

            mFileInfoList.add(map);
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                fileListAdapter.notifyDataSetChanged();
                showDialog(null, false);
            }
        });
    }

    private void dealGotSpecifiedFileEvent(String fileName, String fileAbsolutePath) {
        if (mFileInfoList == null || mFileInfoList.size() == 0 || TextUtils.isEmpty(fileName)) {
            return;
        }

        boolean needRefreshUI = false;
        String tempFileName;
        Map<String, String> map;
        for (int i = 0; i < mFileInfoList.size(); i++) {
            map = mFileInfoList.get(i);
            if (map == null) {
                continue;
            }
            tempFileName = map.get(KEY_FILE_NAME);
            if (fileName.equals(tempFileName)) {
                map.put(KEY_FILE_DOWNLOAD_STATUS, VAL_BOOLEAN_TRUE);
                needRefreshUI = true;
                break;
            }
        }
        if (needRefreshUI) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    fileListAdapter.notifyDataSetChanged();
                    showDialog(null, false);
                }
            });
        }
    }

    @Override
    public void onItemClick(int position, FileListAdapter.ViewHolder viewHolder, Map<String, String> map,
                            boolean deleteHistory, boolean deleteFile, boolean selected) {
        if (map == null) {
            return;
        }
        fileListAdapter.focusOn(position);
        currentSelectedFileName = map.get(KEY_FILE_NAME);
        textviewCurrentSelectedFileName.setText(currentSelectedFileName);
    }

    @Override
    protected void onStop() {
        showDialog(null, false);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        setResult(RESULT_OK);
        //注销各种回调
        bikeComputerManager.unregistStateChangeCallback(stateChangeCallback);
        bikeComputerManager.unregisterGetFitFileDataCallback(getFitFileDataCallback);
        bikeComputerManager.disconnect(false);
        bikeComputerManager.closeDevice();
        super.onDestroy();
    }
}
