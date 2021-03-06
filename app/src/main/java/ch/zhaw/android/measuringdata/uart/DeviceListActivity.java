
/*
 * Zuercher Hochschule fuer Angewandte Wissenschaften (ZHAW)
 * School of Engineering (SoE)
 * InES Institut of Embedded Systems
 *
 *   Bachelorarbeit BA19_gruj_10
 *   Projekt FMS
 *   Darius Eckhardt (eckhadar)
 *   Ibrahim Evren   (evrenibr)
 *
 *   07.06.2019, Winterthur Switzerland
 *
 *   This Software is based on the ble_app_uart, SDK Version 15.2.0
 *   It has been modified to fit the needs of the Project FMS
 *   For correct functionality this Software has to be placed into the same folder as the SDK.
 *
 *   Copyright (c) 2015, Nordic Semiconductor
 *   All rights reserved.
 *
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   * Neither the name of nRF UART nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *   AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *   DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 *   FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *   DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *   SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *   CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *   OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package ch.zhaw.android.measuringdata.uart;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.PaintDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import butterknife.OnClick;
import ch.zhaw.android.measuringdata.R;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import ch.zhaw.android.measuringdata.ui.UartActivity;
import ch.zhaw.android.measuringdata.utils.Utils;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;

/**
 * Class for Connecting Bluetooth LE into the connected mode. It scans the LE Enviroment and lists all the devices.
 */
public class DeviceListActivity extends Activity {
    final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
    private BluetoothAdapter mBluetoothAdapter;

    // private BluetoothAdapter mBtAdapter
    private TextView mEmptyList;
    private ProgressBar mScanningProgress;
    public static final String TAG = "DeviceListActivity";

    List<BluetoothDevice> deviceList;
    private DeviceAdapter deviceAdapter;
    Map<String, Integer> devRssiValues;
    public static final long SCAN_PERIOD = 20000; //scanning for 20 seconds
    private Handler mHandler;
    private boolean mScanning;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String DEVICE = "saved_device";
    private static final ParcelUuid FILTER_UUID = new ParcelUuid(BtService.UART_SERVICE_UUID);



    String autoConnectDevice = "";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);
        setContentView(R.layout.device_list);
        WindowManager.LayoutParams layoutParams = this.getWindow().getAttributes();
        layoutParams.gravity= Gravity.TOP;
        layoutParams.y = 200;
        mHandler = new Handler();

        //SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        //autoConnectDevice = sharedPreferences.getString(DEVICE, "");
        Intent uartToDeviceIntent = getIntent();
        autoConnectDevice = uartToDeviceIntent.getStringExtra("STORED_DEVICE");

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        populateList();
        mEmptyList = findViewById(R.id.empty);
        mScanningProgress = findViewById(R.id.state_scanning);
        Button cancelButton = findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mScanning==false){
                    scanLeDevice(true, SCAN_PERIOD);
                }
                else {
                    finish();
                }
            }
        });

    }

    private void populateList() {
        /* Initialize device list container */
        Log.d(TAG, "populateList");
        deviceList = new ArrayList<BluetoothDevice>();
        deviceAdapter = new DeviceAdapter(this, deviceList);
        devRssiValues = new HashMap<String, Integer>();

        ListView newDevicesListView = findViewById(android.R.id.list);
        newDevicesListView.setAdapter(deviceAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        scanLeDevice(true, SCAN_PERIOD);

    }

    private void scanLeDevice(final boolean enable,final long t_scan_period) {
        final Button cancelButton = findViewById(R.id.btn_cancel);
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    scanner.stopScan(mLeScanCallback);
                    //mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    cancelButton.setText(R.string.scan);
                    mScanningProgress.setVisibility(View.GONE);
                    mEmptyList.setVisibility(View.GONE);

                }
            }, t_scan_period);
            mScanning = true;
            if(mScanningProgress!=null) {
                mScanningProgress.setVisibility(View.VISIBLE);
            }
            if(mEmptyList!=null){
                mEmptyList.setVisibility(View.VISIBLE);
            }
            scanner.startScan(mLeScanCallback);
            //TODO TEST with Filter
            //mBluetoothAdapter.startLeScan(mLeScanCallback);


            cancelButton.setText(R.string.cancel);
        }
        else {
            mScanning = false;
            scanner.stopScan(mLeScanCallback);
            //mBluetoothAdapter.stopLeScan(mLeScanCallback);
            cancelButton.setText(R.string.scan);
        }

    }


    /* Discovered Device */
    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, @NonNull no.nordicsemi.android.support.v18.scanner.ScanResult result) {
            runOnUiThread(new Runnable() {
                              @Override
                              public void run() {

                                  //Filter Devices before adding
                                  //Check UUID is UART so we can add to the List
                                  final ScanRecord record = result.getScanRecord();
                                  if (record == null) {
                                      Log.d(TAG,"ParcelUIIDS: null");
                                      return;
                                  }
                                  final List<ParcelUuid> uuids = record.getServiceUuids();
                                  if (uuids == null) {
                                      return;
                                  }
                                  if ( uuids.contains(FILTER_UUID) ) {
                                      //Log.d(TAG,"ParcelUIIDS:"+uuids.contains(FILTER_UUID)+" Device: "+result.getDevice().getName());
                                      //Log.d(TAG,"Adress:"+result.getDevice().getAddress());
                                      //Log.d(TAG,"autoConnectDevice:"+autoConnectDevice);
                                      //Log.d(TAG,"rssi:"+result.getRssi());
                                      //Log.d(TAG,"TxPower:"+result.getTxPower());
                                      ListView newDevicesListView = (ListView) findViewById(android.R.id.list);
                                      newDevicesListView.setDivider(getResources().getDrawable(R.drawable.divider));
                                      newDevicesListView.setDividerHeight(2);

                                      addDevice(result.getDevice(), result.getRssi());

                                  }

                                  if (result.getDevice().getAddress().equals(autoConnectDevice)) {
                                      Log.d(TAG,"Device already saved");
                                      //TODO Device is the Device from last time

                                      mScanning = false;
                                      scanner.stopScan(mLeScanCallback);
                                      Bundle b = new Bundle();
                                      b.putString(BluetoothDevice.EXTRA_DEVICE, autoConnectDevice);

                                      Intent result = new Intent();
                                      result.putExtras(b);
                                      setResult(Activity.RESULT_OK, result);
                                      finish();


                                  }
                              }
            });
        }
    };
        /*
        @Override

                //public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                public void onLeScan(final BluetoothDevice device, final int rssi, @NonNull final ScanResult result) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addDevice(device,rssi);
                        }
                    });
                }
            };
     */

    private void addDevice(BluetoothDevice device, int rssi) {
        boolean deviceFound = false;

        for (BluetoothDevice listDev : deviceList) {
            if (listDev.getAddress().equals(device.getAddress())) {
                deviceFound = true;
                break;
            }

        }
        devRssiValues.put(device.getAddress(), rssi);
        if (!deviceFound) {
            deviceList.add(device);
            mEmptyList.setVisibility(View.GONE);
            mScanningProgress.setVisibility(View.GONE);
            deviceAdapter.notifyDataSetChanged();

        }
    }




    @Override
    public void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    @Override
    public void onStop() {
        super.onStop();
        scanner.stopScan(mLeScanCallback);
        //TODO Cleanup
        //mBluetoothAdapter.stopLeScan(mLeScanCallback);
        finish();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanner.stopScan(mLeScanCallback);
        //mBluetoothAdapter.stopLeScan(mLeScanCallback);
        finish();

    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice device = deviceList.get(position);
            scanner.stopScan(mLeScanCallback);
            //mBluetoothAdapter.stopLeScan(mLeScanCallback);

            Bundle b = new Bundle();
            b.putString(BluetoothDevice.EXTRA_DEVICE, deviceList.get(position).getAddress());

            Intent result = new Intent();
            result.putExtras(b);
            setResult(Activity.RESULT_OK, result);
            finish();

        }
    };



    protected void onPause() {
        super.onPause();
        scanLeDevice(false, SCAN_PERIOD);
    }

    class DeviceAdapter extends BaseAdapter {
        Context context;
        List<BluetoothDevice> devices;
        LayoutInflater inflater;

        public DeviceAdapter(Context context, List<BluetoothDevice> devices) {
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.devices = devices;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup vg;

            if (convertView != null) {
                vg = (ViewGroup) convertView;
            } else {
                vg = (ViewGroup) inflater.inflate(R.layout.device_element, null);
            }

            BluetoothDevice device = devices.get(position);
            final TextView tvadd = vg.findViewById(R.id.address);
            final TextView tvname = vg.findViewById(R.id.name);
            final TextView tvpaired = vg.findViewById(R.id.paired);
            final TextView tvrssi = vg.findViewById(R.id.rssi);

            tvrssi.setVisibility(View.VISIBLE);
            byte rssival = (byte) devRssiValues.get(device.getAddress()).intValue();
            if (rssival != 0) {
                tvrssi.setText("Signal: " + String.valueOf((int) (100.0f * (127.0f + rssival) / (127.0f + 20.0f)) ) + " %");
            }

            tvname.setText(device.getName());
            tvadd.setText(device.getAddress());
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                Log.d(TAG, "device::"+device.getName() + ", uuids:" + device.getUuids());
                tvname.setTextColor(R.color.colorAccent);
                tvadd.setTextColor(R.color.colorAccent);
                tvpaired.setTextColor(Color.GRAY);
                tvpaired.setVisibility(View.VISIBLE);
                tvpaired.setText(R.string.paired);
                tvrssi.setVisibility(View.VISIBLE);
                tvrssi.setTextColor(R.color.colorAccent);

            } else {
                tvname.setTextColor(R.color.colorAccent);
                tvadd.setTextColor(R.color.colorAccent);
                tvpaired.setVisibility(View.GONE);
                tvrssi.setVisibility(View.VISIBLE);
                tvrssi.setTextColor(R.color.colorAccent);
            }
            return vg;
        }
    }
    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
