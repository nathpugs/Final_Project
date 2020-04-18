package com.onecoder.device.hubconfig;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.onecoder.device.Configs;
import com.onecoder.device.R;
import com.onecoder.device.base.BaseActivity;
import com.onecoder.devicelib.base.api.Manager;
import com.onecoder.devicelib.base.control.entity.BleDevice;
import com.onecoder.devicelib.base.control.interfaces.DeviceStateChangeCallback;
import com.onecoder.devicelib.base.entity.BaseDevice;
import com.onecoder.devicelib.base.protocol.entity.hubconfig.HubIpInfo;
import com.onecoder.devicelib.base.protocol.entity.hubconfig.InternalOrExternalNetMode;
import com.onecoder.devicelib.base.protocol.entity.hubconfig.LoginStatus;
import com.onecoder.devicelib.base.protocol.entity.hubconfig.ScannedWifiInfo;
import com.onecoder.devicelib.base.protocol.entity.hubconfig.WifiSocketInfo;
import com.onecoder.devicelib.base.protocol.entity.hubconfig.WifiStaInfo;
import com.onecoder.devicelib.base.protocol.entity.hubconfig.WifiStatus;
import com.onecoder.devicelib.base.protocol.entity.hubconfig.WifiWorkingMode;
import com.onecoder.devicelib.hubconfig.api.HubConfigManager;
import com.onecoder.devicelib.hubconfig.api.interfaces.RealTimeDataListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/9/16.
 */
public class HubConfigMainActivity extends BaseActivity implements View.OnClickListener, RealTimeDataListener {
    public static final String KEY_PROTOCOL_VERSION = "protocolVersion";
    public static final String KEY_DEVICE_VERSION = "deviceVersion";
    public static final String KEY_IS_FULL_FUNCTION = "isFullFunction";
    private static final String TAG = HubConfigMainActivity.class.getSimpleName();
    private static final int REQUEST_HUB_SCAN_WIFI = 0;

    @BindView(R.id.tv_protocol_version)
    TextView tvProtocolVersion;
    @BindView(R.id.btn_setting_utc)
    Button btnSettingUtc;
    @BindView(R.id.tv_login_status)
    TextView tvLoginStatus;
    @BindView(R.id.edit_text_pwd)
    EditText editTextPwd;
    @BindView(R.id.btn_login_to_hub)
    Button btnLoginToHub;
    @BindView(R.id.ck_need_pwd)
    CheckBox ckNeedPwd;
    @BindView(R.id.btn_set_hub_pwd)
    Button btnSetHubPwd;
    @BindView(R.id.btn_get_hub_pwd)
    Button btnGetHubPwd;
    @BindView(R.id.tv_wifi_config_status)
    TextView tvWifiConfigStatus;
    @BindView(R.id.tv_wifi_connection_status)
    TextView tvWifiConnectionStatus;
    @BindView(R.id.btn_get_wifi_config_status)
    Button btnGetWifiConfigStatus;
    @BindView(R.id.spinner_wifi_working_mode)
    Spinner spinnerWifiWorkingMode;
    @BindView(R.id.btn_get_wifi_working_mode)
    Button btnGetWifiWorkingMode;
    @BindView(R.id.btn_set_wifi_working_mode)
    Button btnSetWifiWorkingMode;
    @BindView(R.id.edit_text_ssid)
    EditText editTextSsid;
    @BindView(R.id.edit_text_wifi_pwd)
    EditText editTextWifiPwd;
    @BindView(R.id.spinner_wifi_auth_mode)
    Spinner spinnerWifiAuthMode;
    @BindView(R.id.spinner_wifi_encryption_algorithm)
    Spinner spinnerWifiEncryptionAlgorithm;
    @BindView(R.id.btn_get_wifi_sta_info)
    Button btnGetWifiStaInfo;
    @BindView(R.id.btn_set_wifi_sta_info)
    Button btnSetWifiStaInfo;
    @BindView(R.id.spinner_switch_socket_a_or_socket_b)
    Spinner spinnerSwitchSocketAOrSocketB;
    @BindView(R.id.edit_text_ip_or_domain_name)
    EditText editTextIpOrDomainName;
    @BindView(R.id.edit_text_port)
    EditText editTextPort;
    @BindView(R.id.spinner_network_protocol)
    Spinner spinnerNetworkProtocol;
    @BindView(R.id.spinner_network_role)
    Spinner spinnerNetworkRole;
    @BindView(R.id.btn_get_wifi_socket_info)
    Button btnGetWifiSocketInfo;
    @BindView(R.id.btn_set_wifi_socket_info)
    Button btnSetWifiSocketInfo;
    @BindView(R.id.layout_internal_or_external_net_mode)
    LinearLayout layoutInternalOrExternalNetMode;
    @BindView(R.id.spinner_internal_or_external_net_mode)
    Spinner spinnerInternalOrExternalNetMode;
    @BindView(R.id.btn_get_internal_or_external_net_mode)
    Button btnGetInternalOrExternalNetMode;
    @BindView(R.id.btn_set_internal_or_external_net_mode)
    Button btnSetInternalOrExternalNetMode;
    @BindView(R.id.hub_remarks_info_layout)
    LinearLayout layoutHubRemarksInfo;
    @BindView(R.id.edit_text_hub_remarks_info)
    EditText editTextHubRemarksInfo;
    @BindView(R.id.btn_get_hub_remarks_info)
    Button btnGetHubRemarksInfo;
    @BindView(R.id.btn_set_hub_remarks_info)
    Button btnSetHubRemarksInfo;
    @BindView(R.id.tv_ip)
    TextView tvIp;
    @BindView(R.id.tv_sub_net_mask)
    TextView tvSubNetMask;
    @BindView(R.id.tv_gateway)
    TextView tvGateway;
    @BindView(R.id.btn_get_hub_ip)
    Button btnGetHubIp;
    @BindView(R.id.btn_reset_hub)
    Button btnResetHub;
    @BindView(R.id.btn_restore_hub_factory_settings)
    Button btnRestoreHubFactorySettings;
    @BindView(R.id.btn_set_hub_to_scan_wifi)
    Button btnSetHubToScanWifi;
    @BindView(R.id.login_layout)
    LinearLayout loginLayout;
    @BindView(R.id.wifi_state_layout)
    LinearLayout wifiStateLayout;
    @BindView(R.id.work_mode_layout)
    LinearLayout workModeLayout;
    @BindView(R.id.sta_info_layout)
    LinearLayout staInfoLayout;
    @BindView(R.id.socket_info_layout)
    LinearLayout socketInfoLayout;
    @BindView(R.id.ip_layout)
    LinearLayout ipLayout;
    @BindView(R.id.get_battery_level_btn)
    Button getPowerBtn;
    @BindView(R.id.tv_battery_level)
    TextView tvBatteryLevel;
    @BindView(R.id.tv_hardware_version)
    TextView tvHardwareVersion;
    @BindView(R.id.get_hardware_version_btn)
    Button getHardwareVersionBtn;
    @BindView(R.id.tv_firmware_version)
    TextView tvFirmwareVersion;
    @BindView(R.id.get_firmware_version_btn)
    Button getFirmwareVersionBtn;
    @BindView(R.id.tv_software_version)
    TextView tvSoftwareVersion;
    @BindView(R.id.get_software_version_btn)
    Button getSoftwareVersionBtn;

    private ArrayAdapter spinnerAdapterWifiWorkingMode;
    private ArrayAdapter spinnerAdapterWifiAuthMode;
    private ArrayAdapter spinnerAdapterWifiEncryptionAlgorithm;
    private ArrayAdapter spinnerAdaptersSwitchSocketAOrSocketB;
    private ArrayAdapter spinnerAdapterNetworkProtocol;
    private ArrayAdapter spinnerAdapterNetworkRole;
    private ArrayAdapter spinnerAdapterInternalOrExternalNetMode;

    private static final boolean INTER_EXT_NET_MODE_ENABLE =
            Configs.DeviceEnable.HubFunctionEnable.INTER_EXT_NET_MODE_ENABLE;
    private static final boolean REMARKS_INFO_ENABLE =
            Configs.DeviceEnable.HubFunctionEnable.REMARKS_INFO_ENABLE;

    private boolean isFullFunction;

    private BaseDevice baseDevice;

    private ProgressDialog dialog;

    private boolean selectSocketAOrSocketB;

    private LoginStatus loginStatus;
    private String loginPwd;
    private WifiWorkingMode wifiWorkingMode;
    private WifiStaInfo wifiStaInfo;
    private WifiSocketInfo wifiSocketInfo;
    private InternalOrExternalNetMode internalOrExternalNetMode;
    private byte[] hubRemarksInfo;
    private HubIpInfo hubIpInfo;
    private WifiStatus wifiStatus;

    private HubConfigManager hubConfigManager;

    // HUB配置的通道是否已经打开
    private boolean isChannelOpened;

    @Override
    protected Manager getManager(Bundle savedInstanceState) {
        if (hubConfigManager != null) {
            return hubConfigManager;
        }
        hubConfigManager = HubConfigManager.getInstance();
        //注册状态回调
        hubConfigManager.registerStateChangeCallback(stateChangeCallback);
        // 注册实时数据回调
        hubConfigManager.registerRealTimeDataListner(this);
        isChannelOpened = hubConfigManager.isChannelOpened();
        return hubConfigManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hub_config_main_act);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    private void initData() {
        baseDevice = (BaseDevice) getIntent().getSerializableExtra(KEY_BASE_DEVICE);

        selectSocketAOrSocketB = true;
        loginStatus = null;
        loginPwd = null;
        wifiWorkingMode = WifiWorkingMode.getInstance(0);
        wifiStaInfo = new WifiStaInfo();
        wifiSocketInfo = new WifiSocketInfo();
        internalOrExternalNetMode = InternalOrExternalNetMode.getInstance(0);
        hubRemarksInfo = null;
        hubIpInfo = new HubIpInfo(null, null, null);
        wifiStatus = null;
    }

    private void initView() {
        isFullFunction = getIntent().getBooleanExtra(KEY_IS_FULL_FUNCTION, false);

        layoutInternalOrExternalNetMode.setVisibility(isChannelOpened ? View.VISIBLE : View.GONE);
        layoutHubRemarksInfo.setVisibility(isChannelOpened ? View.VISIBLE : View.GONE);
        wifiStateLayout.setVisibility(isChannelOpened ? View.VISIBLE : View.GONE);
        staInfoLayout.setVisibility(isChannelOpened ? View.VISIBLE : View.GONE);
        btnResetHub.setVisibility(isChannelOpened ? View.VISIBLE : View.GONE);
        btnRestoreHubFactorySettings.setVisibility(isChannelOpened ? View.VISIBLE : View.GONE);
        btnSettingUtc.setVisibility(isChannelOpened ? View.VISIBLE : View.GONE);

        loginLayout.setVisibility(isChannelOpened && isFullFunction ? View.VISIBLE : View.GONE);
        workModeLayout.setVisibility(isChannelOpened && isFullFunction ? View.VISIBLE : View.GONE);
        socketInfoLayout.setVisibility(isChannelOpened && isFullFunction ? View.VISIBLE : View.GONE);
        ipLayout.setVisibility(isChannelOpened && isFullFunction ? View.VISIBLE : View.GONE);

        if (isChannelOpened) {
            tvProtocolVersion.setText("协议版本:" + getIntent().getIntExtra(KEY_PROTOCOL_VERSION, 0));
        } else {
            tvProtocolVersion.setVisibility(View.GONE);
        }

        dialog = new ProgressDialog(this);

        if (!isChannelOpened) {
            return;
        }

        List<String> stringList = new ArrayList<String>();
        stringList.add("AP");
        stringList.add("STA");
        stringList.add("AP + STA");
        spinnerAdapterWifiWorkingMode = new ArrayAdapter(this, R.layout.android_msg_type_item_layout, stringList);
        spinnerWifiWorkingMode.setAdapter(spinnerAdapterWifiWorkingMode);

        stringList = new ArrayList<String>();
        stringList.add("INVALID");
        stringList.add("OPEN");
        stringList.add("SHARED");
        stringList.add("WPAPSK");
        stringList.add("WAP2PSK");
        spinnerAdapterWifiAuthMode = new ArrayAdapter(this, R.layout.android_msg_type_item_layout, stringList);
        spinnerWifiAuthMode.setAdapter(spinnerAdapterWifiAuthMode);

        stringList = new ArrayList<String>();
        stringList.add("INVALID");
        stringList.add("NONE");
        stringList.add("WEP-H");
        stringList.add("WEP-A");
        stringList.add("TKIP");
        stringList.add("AES");
        spinnerAdapterWifiEncryptionAlgorithm = new ArrayAdapter(this, R.layout.android_msg_type_item_layout, stringList);
        spinnerWifiEncryptionAlgorithm.setAdapter(spinnerAdapterWifiEncryptionAlgorithm);

        stringList = new ArrayList<String>();
        stringList.add("Socket A");
        stringList.add("Socket B");
        spinnerAdaptersSwitchSocketAOrSocketB = new ArrayAdapter(this, R.layout.android_msg_type_item_layout, stringList);
        spinnerSwitchSocketAOrSocketB.setAdapter(spinnerAdaptersSwitchSocketAOrSocketB);

        stringList = new ArrayList<String>();
        stringList.add("TCP");
        stringList.add("UDP");
        spinnerAdapterNetworkProtocol = new ArrayAdapter(this, R.layout.android_msg_type_item_layout, stringList);
        spinnerNetworkProtocol.setAdapter(spinnerAdapterNetworkProtocol);

        stringList = new ArrayList<String>();
        stringList.add("SERVER");
        stringList.add("CLIENT");
        spinnerAdapterNetworkRole = new ArrayAdapter(this, R.layout.android_msg_type_item_layout, stringList);
        spinnerNetworkRole.setAdapter(spinnerAdapterNetworkRole);

        layoutInternalOrExternalNetMode.setVisibility(INTER_EXT_NET_MODE_ENABLE ? View.VISIBLE : View.GONE);
        layoutHubRemarksInfo.setVisibility(REMARKS_INFO_ENABLE ? View.VISIBLE : View.GONE);
        if (INTER_EXT_NET_MODE_ENABLE) {
            stringList = new ArrayList<String>();
            stringList.add("External");
            stringList.add("Internal");
            spinnerAdapterInternalOrExternalNetMode = new ArrayAdapter(this, R.layout.android_msg_type_item_layout, stringList);
            spinnerInternalOrExternalNetMode.setAdapter(spinnerAdapterInternalOrExternalNetMode);
        }

        AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemSelected parent:" + parent + " position:" + position);
                switch (parent.getId()) {
                    case R.id.spinner_wifi_working_mode:
                        wifiWorkingMode = WifiWorkingMode.getInstance(position);
                        break;

                    case R.id.spinner_wifi_auth_mode:
                        if (wifiStaInfo == null) {
                            wifiStaInfo = new WifiStaInfo();
                        }
                        wifiStaInfo.setAuthMode(WifiStaInfo.AuthMode.getInstance(position));
                        break;

                    case R.id.spinner_wifi_encryption_algorithm:
                        if (wifiStaInfo == null) {
                            wifiStaInfo = new WifiStaInfo();
                        }
                        wifiStaInfo.setEncryptionAlgorithm(WifiStaInfo.EncryptionAlgorithm.getInstance(position));
                        break;

                    case R.id.spinner_switch_socket_a_or_socket_b:
                        selectSocketAOrSocketB = position == 0;
                        if (!selectSocketAOrSocketB) {
                            int targetSelectPosition = WifiSocketInfo.NetworkProtocol.TCP.getVal();
                            if (spinnerNetworkProtocol.getSelectedItemPosition() != targetSelectPosition) {
                                spinnerNetworkProtocol.setSelection(targetSelectPosition, true);
                            }
                            targetSelectPosition = WifiSocketInfo.NetworkRole.CLIENT.getVal();
                            if (spinnerNetworkRole.getSelectedItemPosition() != targetSelectPosition) {
                                spinnerNetworkRole.setSelection(targetSelectPosition, true);
                            }
                        }
                        spinnerNetworkProtocol.setEnabled(selectSocketAOrSocketB);
                        spinnerNetworkRole.setEnabled(selectSocketAOrSocketB);
                        break;

                    case R.id.spinner_network_protocol:
                        if (wifiSocketInfo == null) {
                            wifiSocketInfo = new WifiSocketInfo();
                        }
                        wifiSocketInfo.setNetworkProtocol(WifiSocketInfo.NetworkProtocol.getInstance(position));
                        break;

                    case R.id.spinner_network_role:
                        if (wifiSocketInfo == null) {
                            wifiSocketInfo = new WifiSocketInfo();
                        }
                        wifiSocketInfo.setNetworkRole(WifiSocketInfo.NetworkRole.getInstance(position));
                        break;

                    case R.id.spinner_internal_or_external_net_mode:
                        internalOrExternalNetMode = InternalOrExternalNetMode.getInstance(position);
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };

        spinnerWifiWorkingMode.setOnItemSelectedListener(onItemSelectedListener);
        spinnerWifiAuthMode.setOnItemSelectedListener(onItemSelectedListener);
        spinnerWifiEncryptionAlgorithm.setOnItemSelectedListener(onItemSelectedListener);
        spinnerSwitchSocketAOrSocketB.setOnItemSelectedListener(onItemSelectedListener);
        spinnerNetworkProtocol.setOnItemSelectedListener(onItemSelectedListener);
        spinnerNetworkRole.setOnItemSelectedListener(onItemSelectedListener);
        spinnerInternalOrExternalNetMode.setOnItemSelectedListener(onItemSelectedListener);
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
                if (hubConfigManager.getConnectState()
                        >= BleDevice.STATE_CONNECTED) {
                    hubConfigManager.disconnect(false);
                }
                break;

            case R.id.main_menu_connect:
                if (hubConfigManager.getConnectState()
                        < BleDevice.STATE_CONNECTED) {
                    hubConfigManager.connectDevice(baseDevice);
                }
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        boolean getOrSet;
        boolean ret = false;
        int vId = v.getId();

        switch (vId) {
            case R.id.get_battery_level_btn:
                ret = hubConfigManager.getBatteryLevel();
                break;

            case R.id.get_hardware_version_btn:
                ret = hubConfigManager.getHardwareVersion();
                break;

            case R.id.get_firmware_version_btn:
                ret = hubConfigManager.getFirmwareVersion();
                break;

            case R.id.get_software_version_btn:
                ret = hubConfigManager.getSoftwareVersion();
                break;

            case R.id.btn_setting_utc:
                ret = hubConfigManager.setUTC();
                break;

            case R.id.btn_login_to_hub:
                loginPwd = editTextPwd.getText().toString();
                ret = hubConfigManager.loginToHub(loginPwd);
                break;

            case R.id.btn_set_hub_pwd:
            case R.id.btn_get_hub_pwd:
                getOrSet = vId == R.id.btn_get_hub_pwd;
                loginPwd = editTextPwd.getText().toString();
                if (!getOrSet && ckNeedPwd.isChecked() && !HubConfigManager.isValidHubPwd(loginPwd)) {
                    Toast.makeText(this, "Please input a valid hub password.", Toast.LENGTH_LONG).show();
                    return;
                }
                ret = hubConfigManager.getOrSetHubPwd(getOrSet, ckNeedPwd.isChecked(), loginPwd);
                break;

            case R.id.btn_get_wifi_config_status:
                ret = hubConfigManager.getHubWifiStatus();
                break;

            case R.id.btn_get_wifi_working_mode:
            case R.id.btn_set_wifi_working_mode:
                getOrSet = vId == R.id.btn_get_wifi_working_mode;
                wifiWorkingMode = WifiWorkingMode.getInstance(spinnerWifiWorkingMode.getSelectedItemPosition());
                ret = hubConfigManager.getOrSetWifiWorkingMode(getOrSet, wifiWorkingMode);
                break;

            case R.id.btn_get_wifi_sta_info:
            case R.id.btn_set_wifi_sta_info:
                getOrSet = vId == R.id.btn_get_wifi_sta_info;
                if (!getOrSet) {
                    if (wifiStaInfo == null) {
                        wifiStaInfo = new WifiStaInfo();
                    }
                    wifiStaInfo.setSsid(editTextSsid.getText().toString());
                    wifiStaInfo.setPwd(editTextWifiPwd.getText().toString());
                    if (!WifiStaInfo.isValidSSID(wifiStaInfo.getSsid())) {
                        Toast.makeText(this, "Please input a valid ssid.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (!WifiStaInfo.isValidWifiPwd(wifiStaInfo.getPwd())) {
                        Toast.makeText(this, "Please input a valid wifi password.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    wifiStaInfo.setAuthMode(WifiStaInfo.AuthMode.getInstance(spinnerWifiAuthMode.getSelectedItemPosition()));
                    wifiStaInfo.setEncryptionAlgorithm(
                            WifiStaInfo.EncryptionAlgorithm.getInstance(spinnerWifiEncryptionAlgorithm.getSelectedItemPosition()));
                }
                ret = hubConfigManager.getOrSetWifiStaInfo(getOrSet, wifiStaInfo);
                break;

            case R.id.btn_get_wifi_socket_info:
            case R.id.btn_set_wifi_socket_info:
                getOrSet = vId == R.id.btn_get_wifi_socket_info;
                if (!getOrSet) {
                    if (wifiSocketInfo == null) {
                        wifiSocketInfo = new WifiSocketInfo();
                    }
                    selectSocketAOrSocketB = spinnerSwitchSocketAOrSocketB.getSelectedItemPosition() == 0;
                    String ipOrDomainName = editTextIpOrDomainName.getText().toString();
                    String portStr = editTextPort.getText().toString();
                    if (!WifiSocketInfo.isValidIpOrDomainName(ipOrDomainName)) {
                        //Please input a valid wifi password.
                        Toast.makeText(this, "Please input a valid IP or domain name.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (!WifiSocketInfo.isValidPort(portStr)) {
                        Toast.makeText(this, "" + portStr + " is not a valid port string.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    wifiSocketInfo.setIpOrDomainName(ipOrDomainName);
                    wifiSocketInfo.setPort(Integer.valueOf(portStr));
                    wifiSocketInfo.setNetworkProtocol(
                            WifiSocketInfo.NetworkProtocol.getInstance(spinnerNetworkProtocol.getSelectedItemPosition()));
                    wifiSocketInfo.setNetworkRole(
                            WifiSocketInfo.NetworkRole.getInstance(spinnerNetworkRole.getSelectedItemPosition()));
                }
                ret = hubConfigManager.getOrSetWifiSocketInfo(getOrSet, selectSocketAOrSocketB, wifiSocketInfo);
                break;

            case R.id.btn_get_internal_or_external_net_mode:
            case R.id.btn_set_internal_or_external_net_mode:
                getOrSet = vId == R.id.btn_get_internal_or_external_net_mode;
                internalOrExternalNetMode = InternalOrExternalNetMode.getInstance(spinnerInternalOrExternalNetMode.getSelectedItemPosition());
                ret = hubConfigManager.getOrSetInternalOrExternalNetMode(getOrSet, internalOrExternalNetMode);
                break;

            case R.id.btn_get_hub_remarks_info:
            case R.id.btn_set_hub_remarks_info:
                getOrSet = vId == R.id.btn_get_hub_remarks_info;
                String hubRemarks = editTextHubRemarksInfo.getText().toString();
                if (!getOrSet && !HubConfigManager.isValidHubRemarks(hubRemarks)) {
                    Toast.makeText(this, "Please input hub remarks", Toast.LENGTH_LONG).show();
                    return;
                }
                hubRemarksInfo = hubRemarks.getBytes();
                ret = hubConfigManager.getOrSetHubRemarks(getOrSet, hubRemarksInfo);
                break;

            case R.id.btn_get_hub_ip:
                ret = hubConfigManager.getHubIp();
                break;

            case R.id.btn_reset_hub:
                ret = hubConfigManager.resetRestartHub();
                break;

            case R.id.btn_restore_hub_factory_settings:
                ret = hubConfigManager.restoreHubFactorySettings();
                break;

            case R.id.btn_set_hub_to_scan_wifi:
                ret = hubConfigManager.setHubToScanWifi();
                if (ret) {
                    Intent intent = new Intent(this, WifiListActivity.class);
                    startActivityForResult(intent, REQUEST_HUB_SCAN_WIFI);
                }
                break;

            default:
                break;
        }
        Toast.makeText(this, "ret:" + ret, Toast.LENGTH_LONG).show();
        Log.i(TAG, "ret:" + ret);
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

    @Override
    public void onGotHardwareVersion(String hardwareVersion) {
        tvHardwareVersion.setText("硬件版本号:" + hardwareVersion);
    }

    @Override
    public void onGotFirmwareVersion(String firmwareVersion) {
        tvFirmwareVersion.setText("固件版本号:" + firmwareVersion);
    }

    @Override
    public void onGotSoftwareVersion(String softwareVersion) {
        tvSoftwareVersion.setText("软件版本号:" + softwareVersion);
    }

    /**
     * 获取到电池电量
     *
     * @param batteryLevel 电池电量百分比。取值范围0-100
     */
    @Override
    public void onGotBatteryLevel(int batteryLevel) {
        tvBatteryLevel.setText("" + batteryLevel);
    }

    @Override
    public void onGotLoginStatus(LoginStatus loginStatus) {
        this.loginStatus = loginStatus;
        if (loginStatus == null) {
            Toast.makeText(this, "onGotLoginStatus loginStatus:" + loginStatus, Toast.LENGTH_LONG).show();
            return;
        }
        ckNeedPwd.setChecked(loginStatus != LoginStatus.NotNeedPwd);
        tvLoginStatus.setText(loginStatus.name());
        Toast.makeText(this, "onGotLoginStatus loginStatus:" + loginStatus.name(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGotLoginPwd(boolean needPwd, String loginPwd) {
        this.loginPwd = loginPwd;
        ckNeedPwd.setChecked(needPwd);
        editTextPwd.setText(loginPwd);
        Toast.makeText(this, "onGotLoginPwd needPwd:" + needPwd + " loginPwd:" + loginPwd, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGotWifiWorkingMode(WifiWorkingMode wifiWorkingMode) {
        this.wifiWorkingMode = wifiWorkingMode;
        if (wifiWorkingMode != null) {
            spinnerWifiWorkingMode.setSelection(wifiWorkingMode.getVal(), true);
        }
        Toast.makeText(this, "onGotWifiWorkingMode wifiWorkingMode:" + wifiWorkingMode, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGotWifiStaInfo(WifiStaInfo wifiStaInfo) {
        Toast.makeText(this, "onGotWifiStaInfo wifiStaInfo:" + wifiStaInfo, Toast.LENGTH_LONG).show();
        this.wifiStaInfo = wifiStaInfo;
        if (wifiStaInfo == null) {
            return;
        }
        editTextSsid.setText(wifiStaInfo.getSsid());
        editTextWifiPwd.setText(wifiStaInfo.getPwd());
        if (wifiStaInfo.getAuthMode() != null) {
            spinnerWifiAuthMode.setSelection(wifiStaInfo.getAuthMode().getVal(), true);
        }
        if (wifiStaInfo.getEncryptionAlgorithm() != null) {
            spinnerWifiEncryptionAlgorithm.setSelection(wifiStaInfo.getEncryptionAlgorithm().getVal(), true);
        }
    }

    @Override
    public void onGotWifiSocketInfo(boolean socketAOrSocketB, WifiSocketInfo wifiSocketInfo) {
        Toast.makeText(this, "onGotWifiSocketInfo wifiSocketInfo:" + wifiSocketInfo, Toast.LENGTH_LONG).show();
        selectSocketAOrSocketB = socketAOrSocketB;
        spinnerSwitchSocketAOrSocketB.setSelection(selectSocketAOrSocketB ? 0 : 1, true);
        this.wifiSocketInfo = wifiSocketInfo;
        if (wifiSocketInfo == null) {
            return;
        }
        editTextIpOrDomainName.setText(wifiSocketInfo.getIpOrDomainName());
        editTextPort.setText(String.valueOf(wifiSocketInfo.getPort()));
        if (wifiSocketInfo.getNetworkProtocol() != null) {
            spinnerNetworkProtocol.setSelection(wifiSocketInfo.getNetworkProtocol().getVal(), true);
        }
        if (wifiSocketInfo.getNetworkRole() != null) {
            spinnerNetworkRole.setSelection(wifiSocketInfo.getNetworkRole().getVal(), true);
        }
    }

//    @Override
//    public void onGotInternalOrExternalNetMode(InternalOrExternalNetMode internalOrExternalNetMode) {
//        Toast.makeText(this, "onGotInternalOrExternalNetMode internalOrExternalNetMode:"
//                + internalOrExternalNetMode, Toast.LENGTH_SHORT).show();
//        this.internalOrExternalNetMode = internalOrExternalNetMode;
//        if (internalOrExternalNetMode == null) {
//            return;
//        }
//        spinnerInternalOrExternalNetMode.setSelection(internalOrExternalNetMode.getVal(), true);
//    }
//
//    @Override
//    public void onGotHubRemarksInfo(byte[] hubRemarksInfo) {
//        this.hubRemarksInfo = hubRemarksInfo;
//        if (hubRemarksInfo == null) {
//            Toast.makeText(this, "onGotHubRemarksInfo hubRemarksInfo:" + hubRemarksInfo, Toast.LENGTH_SHORT).show();
//            return;
//        }
//        editTextHubRemarksInfo.setText(new String(hubRemarksInfo));
//        Toast.makeText(this, "onGotHubRemarksInfo hubRemarksInfo:" + editTextHubRemarksInfo.getText(), Toast.LENGTH_SHORT).show();
//    }

    @Override
    public void onGotHubIpInfo(HubIpInfo hubIpInfo) {
        Toast.makeText(this, "onGotHubIpInfo hubIpInfo:" + hubIpInfo, Toast.LENGTH_SHORT).show();
        this.hubIpInfo = hubIpInfo;
        if (hubIpInfo == null) {
            return;
        }
        tvIp.setText(hubIpInfo.getIP());
        tvSubNetMask.setText(hubIpInfo.getSubnetMask());
        tvGateway.setText(hubIpInfo.getGateway());
    }

    @Override
    public void onGotWifiList(List<ScannedWifiInfo> wifiInfoList) {
        //Toast.makeText(this, "onGotWifiList wifiInfoList:" + wifiInfoList, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "onGotWifiList wifiInfoList.size():" + wifiInfoList.size(), Toast.LENGTH_SHORT).show();
        if (wifiInfoList == null) {
            return;
        }
        ArrayList<Parcelable> list = new ArrayList();
        for (ScannedWifiInfo scannedWifiInfo : wifiInfoList) {
            list.add(scannedWifiInfo);
        }
        Intent intent = new Intent(WifiListActivity.ACTION_SCANNED_WIFI_INFO_LIST);
        intent.putParcelableArrayListExtra(WifiListActivity.KEY_SCANNED_WIFI_INFO_LIST, list);
        sendBroadcast(intent);
    }

    @Override
    public void onGotWifiStatus(WifiStatus wifiStatus) {
        this.wifiStatus = wifiStatus;
        if (this.wifiStatus == null) {
            Toast.makeText(this, "onGotWifiStatus wifiStatus:" + wifiStatus, Toast.LENGTH_SHORT).show();
            return;
        }
        if (wifiStatus.getWifiConfigStatus() != null) {
            tvWifiConfigStatus.setText(wifiStatus.getWifiConfigStatus().name());
        }
        if (wifiStatus.getWifiConnectionStatus() != null) {
            tvWifiConnectionStatus.setText(wifiStatus.getWifiConnectionStatus().name());
        }
        Toast.makeText(this, "onGotWifiStatus wifiStatus:" + wifiStatus, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Toast.makeText(this, "onActivityResult requestCode:"
                + requestCode + " resultCode:" + resultCode + " data:" + data, Toast.LENGTH_SHORT).show();

        if (REQUEST_HUB_SCAN_WIFI == requestCode && RESULT_OK == resultCode && data != null) {
            Parcelable parcelable = data.getParcelableExtra(WifiListActivity.KEY_SELECTED_SCANNED_WIFI_INFO);
            if (parcelable instanceof ScannedWifiInfo) {
                ScannedWifiInfo scannedWifiInfo = (ScannedWifiInfo) parcelable;
                editTextSsid.setText(scannedWifiInfo.getSSID());
                editTextWifiPwd.setText("");

                WifiStaInfo.AuthMode authMode = scannedWifiInfo.getAuthMode();
                WifiStaInfo.EncryptionAlgorithm encryptionAlgorithm = scannedWifiInfo.getEncryptionAlgorithm();
                if (authMode != null) {
                    spinnerWifiAuthMode.setSelection(authMode.getVal(), true);
                }
                if (encryptionAlgorithm != null) {
                    spinnerWifiEncryptionAlgorithm.setSelection(encryptionAlgorithm.getVal(), true);
                }
                if (wifiStaInfo == null) {
                    wifiStaInfo = new WifiStaInfo();
                }
                wifiStaInfo.setSsid(scannedWifiInfo.getSSID());
                wifiStaInfo.setAuthMode(authMode);
                wifiStaInfo.setEncryptionAlgorithm(encryptionAlgorithm);

                Toast.makeText(this, "onActivityResult selected scannedWifiInfo:" + scannedWifiInfo, Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        hubConfigManager.unregistStateChangeCallback(stateChangeCallback);
        hubConfigManager.unregisterRealTimeDataListner(this);
        hubConfigManager.disconnect(false);
        hubConfigManager.closeDevice();
        super.onDestroy();
    }
}