
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

package ch.zhaw.android.measuringdata.ui;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import ch.zhaw.android.measuringdata.ActivityStore;
import ch.zhaw.android.measuringdata.IntentStore;
import ch.zhaw.android.measuringdata.MainActivity;
import ch.zhaw.android.measuringdata.R;
import ch.zhaw.android.measuringdata.engine.Engine;
import ch.zhaw.android.measuringdata.uart.BtService;
import ch.zhaw.android.measuringdata.uart.DeviceListActivity;

import static java.lang.Integer.parseInt;
import static java.lang.Integer.parseUnsignedInt;

public class UartActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {
    public static final String TAG = UartActivity.class.getSimpleName();


    private static final int REQUEST_SELECT_DEVICE = 1;
    private final static int REQUEST_PERMISSION_REQ_CODE = 34; // any 8-bit number
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;

    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;

    private static final int STATE_OFF = 10;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String DEVICE = "saved_device_address";
    public static final String DEVICE_Name = "saved_device_name";

    public static int DATALENGTH=244;
    public static int TOTALPACKAGES=4;

    public static int TIMEOUT = 10*60; //After 5 min no Data Transfered Disconnect


    DeviceListActivity deviceActivity;

    MainActivity main;
    Engine engine;
    private int retryCount = 0;
    private int mState = UART_PROFILE_DISCONNECTED;
    private BtService mService = null;
    boolean btServiceBound = false;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    public Button btnConnectDisconnect;
    private Button btnSend;
    private EditText edtMessage;
    private Context permissonContext;
    public Toolbar toolbar;
    private static String saved_device;



    public static  boolean isConnectionLost = false;
    private boolean isConnect = false;
    public boolean isStartReceived = false;
    public boolean isBatteryLevelAvailable = false;
    public boolean isBackground = true;

    private boolean userWantCloseApp=false;
    private boolean isDataReady = false;
    private int packagecount=0;
    private byte[] rxValue;     //
    private byte[][]receivedData;   // [packagecount][received Data]
    public String batteryLevel = "n/a";

    float x1,x2,y1,y2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "created...");
        setContentView(R.layout.activity_uart);
        if(main == null) {
            main = (MainActivity) ActivityStore.get("main");
            Log.d(TAG,"main:"+main);
            if(main!=null) {
                engine = main.getEngine();
            }
            //Log.d(TAG,"engine:"+engine);
            if (engine == null) {
                //FIXME on Home Button pressed -> close Bluetooth connection
                Log.d(TAG, "Rerun App");
                reRunApp();
            } else {
                if (engine.getIsAppClosing()) {
                    this.finish();
                }
            }
        }

        //Added because Engine gets called from MainActivity -> After Home Button -> destroy this
        if (!isTaskRoot()) {
            final Intent intent = getIntent();
            final String intentAction = intent.getAction();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) &&
                    intentAction != null && intentAction.equals(Intent.ACTION_MAIN)) {
                Log.d(TAG, "!isTaskRoot -> finished");
                finish();
            }
        }


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("FMS");
        
        permissonContext = this;
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        messageListView = findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);
        btnConnectDisconnect= findViewById(R.id.btn_select);
        btnSend= findViewById(R.id.sendButton);
        edtMessage = findViewById(R.id.sendText);

        receivedData= new byte[TOTALPACKAGES][DATALENGTH];   // [packagecount][received Data]

        //service_init(); //not bounded -> FIXME put it to a bounded section
        Log.d(TAG, "onServiceConnected mService= " + mService);


        // Handle Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check Permissions
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) permissonContext, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    Log.i(TAG, "UART Activity started: Asked for permission");
                    return;
                }
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {
                    if (btnConnectDisconnect.getText().equals("Connect")){
                        isConnect = true;
                        Log.d(TAG,"onClick "+btnConnectDisconnect.getText() + ", isConnect: "+isConnect + ", mState="+mState);
                        //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
                        // Autoconnect to saved_device
                        /*try {
                            boolean connected = mService.connect(saved_device);
                            if(connected ==true){
                                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(saved_device);
                                mState = UART_PROFILE_CONNECTED;
                                isConnect = false;
                            }

                        }catch (Exception ignore) {
                            Log.e(TAG, ignore.toString());
                        }
                         */
                        if(mState !=UART_PROFILE_CONNECTED){
                            if(retryCount<3) {
                                Intent newIntent = new Intent(UartActivity.this, DeviceListActivity.class);
                                startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                                retryCount++;
                            }
                            isConnect = false;
                        }
                    } else {
                        //Disconnect button pressed
                        if (mDevice!=null)
                        {
                            isConnect = false;
                            clearDevice();
                            mService.disconnect();

                        }
                    }
                }
            }
        });

        // Handle Send button
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = findViewById(R.id.sendText);
                String message = editText.getText().toString();
                byte[] value;
                //send data to service
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    value = message.getBytes(StandardCharsets.UTF_8);
                    mService.writeRXCharacteristic(value);
                }
                //Update the log with time stamp
                String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                edtMessage.setText("");

            }
        });

        // load saved DEVICE Adress: -> String saved_device
        loadDevice();
        bindBtService();

        //connectDisconnect();
        ActivityStore.put("activity_uart",UartActivity.this);
    }

    public boolean onTouchEvent(MotionEvent touchEvent){
        switch(touchEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                y1 = touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                y2 = touchEvent.getY();
                if(x1 < x2){
                    //SwipeLeft detected
                    Log.d(TAG,"Swipe-LEFT");
                    Log.d(String.valueOf(this), "Menu swipe left->About");
                    Intent about_intent = new Intent(UartActivity.this,
                            AboutActivity.class);
                    startActivity(about_intent);
                    //Intent i = new Intent(MainActivity.this, SwipeLeft.class);
                    //startActivity(i);
                }else if(x1 > x2){
                    //SwipeLeft detected
                    Log.d(TAG,"Swipe-Right");
                    //Intent i = new Intent(MainActivity.this, SwipeRight.class);
                    //startActivity(i);
                }
                break;
        }
        return false;
    }


    public boolean isDataReady(){
        return isDataReady;
    }

    public boolean isConnectionLost(){
        return isConnectionLost;
    }

    public void setDataReady(boolean val) {
        isDataReady=val;
    }

    public void setConnect(boolean val) {
        Log.d(TAG, "setConnect called ->"+val);
        isConnect=val;
        if(isConnect ==true) {
            bindBtService();
            connectDisconnect();
        }

    }

    @NonNull
    public byte[][] getRecivedData(){
        return receivedData;
    }

    @NonNull
    public int getRecivedPackageNr(){
        return packagecount;
    }

    public int checkConnectionEstablished (){
        //Log.d(TAG, "checkConnectionEstablished"+mState);
        return mState;
    }

    //Fixme AutoConnect
    public void connectDisconnect (){
        //Check Permissions
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) permissonContext, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            Log.i(TAG, "UART Activity started: Asked for permission");
            return;
        }
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onClick - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else {
            if (btnConnectDisconnect.getText().equals("Connect")){
                if(mState !=UART_PROFILE_CONNECTED){
                    isConnect=true;
                    if (deviceActivity == null) {
                        Log.d(TAG, "connectDisscont called-> open DeviceList");
                        Intent newIntent = new Intent(UartActivity.this, DeviceListActivity.class);
                        //Using SharedPreferences for getting saved device
                        newIntent.putExtra("STORED_DEVICE", saved_device);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                    }
                }
            } else {
                //Disconnect button pressed
                if (mDevice!=null)
                {
                    Log.d(TAG, "connectDisconnect:"+isConnect);
                    isConnect=false;
                    mService.disconnect();
                }
            }
        }
    }

    public void Disconnect (){
        //Check Permissions
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) permissonContext, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            Log.i(TAG, "UART Activity started: Asked for permission");
            return;
        }
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onClick - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else {
            if(mState !=UART_PROFILE_CONNECTED){
                if (deviceActivity == null) {
                    try {
                        Log.d(TAG, "connectDisconnect:"+isConnect);
                        isConnect=false;
                        mService.disconnect();
                    }catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }

            } else {
                //Disconnect button pressed
                if (mDevice!=null)
                {
                    Log.d(TAG, "connectDisconnect:"+isConnect);
                    isConnect=false;
                    mService.disconnect();
                }
            }
        }
    }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            BtService.LocalBinder binder = (BtService.LocalBinder) rawBinder;
            //mService = ((BtService.LocalBinder) rawBinder).getService();
            mService = binder.getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            else {
                btServiceBound = true;

                Log.d(TAG,"btServiceBound: "+btServiceBound);
                broadcast_init();
            }

        }
        @Override
        public void onServiceDisconnected(ComponentName classname) {
            //mService.disconnect(mDevice);
            mService = null;
            btServiceBound =false;
        }
    };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            //*********************//
            if (action.equals(BtService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        btnConnectDisconnect.setText("Disconnect");
                        edtMessage.setEnabled(true);
                        btnSend.setEnabled(true);
                        ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - ready");
                        listAdapter.add("[" + currentDateTimeString + "] Connected to: " + mDevice.getName());
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        isConnectionLost = false;
                        mState = UART_PROFILE_CONNECTED;
                        retryCount = 0;
                    }
                });
            }

            //*********************//
            if (action.equals(BtService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        isConnectionLost = true;
                        btnConnectDisconnect.setText("Connect");
                        edtMessage.setEnabled(false);
                        btnSend.setEnabled(false);
                        ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                        listAdapter.add("[" + currentDateTimeString + "] Disconnected to: " + mDevice.getName());
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        //setUiState();
                    }
                });
            }


            //*********************//
            if (action.equals(BtService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
                mService.requestBatteryCharacteristics();
                // -> cant enable like this, using onDescriptorWrite after enable TXNotification()
                // mService.enableBATTERYNotification();
            }


            //********************//
            if (action.equals(BtService.ACTION_DATA_AVAILABLE)) {
                rxValue = intent.getByteArrayExtra(BtService.EXTRA_DATA);
                packagecount++;
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            //TODO Try to get 5 packets -> and save in receivedData[0-4]
                            //String text = new String(rxValue, "UTF-8");
                            //String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            //listAdapter.add("["+currentDateTimeString+"] RX: "+text);
                            //HEX ->
                            if (isDataReady) {
                                Log.d(TAG, "dataNotReset");
                                return;
                            }
                            //No Measuring Data -> commandData
                            if (rxValue.length < 244) {
                                //StartPacket
                                packagecount = 0;
                                if (rxValue[0] == (byte) 0x6) {
                                    Log.d(TAG, "Start Byte received");
                                    isStartReceived = true;
                                }

                            }

                            //FIXME if after Start Command no Data comes exit
                            //Measuring DATA -> 244 length
                            if (rxValue.length == 244) {

                                Log.d(TAG, "packagecount:" + packagecount);
                                //**** Check Data receiving is correct DEBUG ****
                                //StringBuilder sb = new StringBuilder();
                                //for (int i = 0; i < rxValue.length; i++) {
                                //    sb.append(String.format("%02X ", rxValue[i]));
                                //}
                                //Log.d(TAG, "receivedDataLength:" + rxValue.length + "receivedData:" + sb);
                                //**** END CHECK****

                                // Store the Data
                                receivedData[packagecount - 1] = rxValue;
                                if (packagecount == TOTALPACKAGES) {
                                    packagecount = 0;
                                    isDataReady = true;
                                }
                                else{
                                    isDataReady = false;
                                    //Log.e(TAG, "MORE PACKETS RECEIVED THAN DEFINED");
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }

            //*********************//
            if (action.equals(BtService.ACTION_BATTERY_AVAILABLE)) {
                batteryLevel =  intent.getStringExtra(BtService.BATTERY_LEVEL);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //Batterly Level
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            Log.d(TAG, ""+"["+currentDateTimeString+"] RX: "+ batteryLevel);
                            isBatteryLevelAvailable = true;
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }

            //*********************//
            if (action.equals(BtService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }


        }
    };



    private void broadcast_init() {
        if (btServiceBound){
            Log.d(TAG, "Service-INIT (BROADCAST MANAGER)");
            LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
        }
        else {
            Log.d(TAG, "Service-INIT not bounded to Service");
        }

    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BtService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BtService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BtService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BtService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BtService.ACTION_BATTERY_AVAILABLE);
        intentFilter.addAction(BtService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    public void clearDevice(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //sharedPreferences.edit().remove(DEVICE).commit();
        sharedPreferences.edit().clear().commit();
    }

    private void saveDevice(String deviceAddress) {
        //SP -> is editable in SettingsFrame
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //sharedPreferences.edit().remove(DEVICE).commit();
        sharedPreferences.edit().clear().commit();

        String deviceName = mDevice.getName();
        editor.putString(DEVICE, deviceAddress);
        editor.putString(DEVICE_Name, deviceName);
        editor.apply();
        SP.edit().putString("edit_saved_device", deviceName).apply();
        Toast.makeText(this, deviceName+" saved", Toast.LENGTH_SHORT).show();
    }

    public void loadDevice() {
        //FIXME Get Device Adress from String -> Settings Editable
        //SP -> is editable in SettingsFrame
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        saved_device = sharedPreferences.getString(DEVICE, "");
        Log.d(TAG, "Stored Device is:"+saved_device);
    }

    public final String getSavedDevice(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String saved_device_name = sharedPreferences.getString(DEVICE_Name, "");
        return saved_device_name;
    }


    public void bindBtService() {
        // Bind to LocalService
        Intent intent = new Intent(this, BtService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        return;
    }

    public boolean checkBtServiceBound(){
        return btServiceBound;
    }

    public boolean isUserWantCloseApp(){
        return  userWantCloseApp;
    }

    public static void forceRunApp(Context context, String packageApp){
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageApp);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launchIntent);
    }

    private void reRunApp() {
        Context context = getApplicationContext();
        Log.d(TAG, "Rerun App");
        forceRunApp(context, "ch.zhaw.android.measuringdata");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case  R.id.action_about:
                Log.d(String.valueOf(this), "Menu Item clicked->About");
                Intent about_intent = new Intent(UartActivity.this,
                        AboutActivity.class);
                startActivity(about_intent);
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chart, menu);
        menu.removeItem(R.id.action_settings);
        menu.findItem(R.id.action_about);
        return true;
    }

    /**
     * Activity Life-Cycle:
     */
    @Override
    public void onStart() {
        super.onStart();
        //bindBtService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        try {
            //Stop already unbind Service
            unbindService(mServiceConnection);
            mService.stopSelf();
            mService= null;
            btServiceBound = false;
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        //unbindService(mServiceConnection);
        //btServiceBound = false;
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        if(main == null) {
            main = (MainActivity) ActivityStore.get("main");
            //Log.d(TAG,"main:"+main);
            if(main!=null) {
                engine = main.getEngine();
            }
            //Log.d(TAG,"engine:"+engine);
            if (engine == null) {
                //FIXME on Home Button pressed -> close Bluetooth connection
                Log.d(TAG, "Rerun App");
                reRunApp();
            } else {
                if (engine.getIsAppClosing()) {
                    this.finish();
                }
            }
        }

    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        //Todo when home Button pressed -> Didnt work tested...
        //if(getEngine()!=null) {
        //    getEngine().setRun(false);
        //}
        //finish();
        //Toast.makeText(this, TAG+" User pressed Home Button", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        //Todo when home Button pressed ->
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            isBackground = true;
            Log.d(TAG, "Home Button pressed");
            userWantCloseApp = true;
            //notifyBackground();
            try {
                //Stop already unbind Service
                unbindService(mServiceConnection);
                mService.stopSelf();
                mService= null;
                btServiceBound = false;
                LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
            } catch (Exception ignore) {
                Log.e(TAG, ignore.toString());
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected saved_device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                    if(btServiceBound) {

                        saveDevice(deviceAddress);
                        mService.connect(deviceAddress);
                    }
                    else {
                        bindBtService();
                        mService.connect(deviceAddress);
                    }
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.popup_title)
                .setMessage(R.string.popup_message)
                .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mState == UART_PROFILE_CONNECTED) {
            /*Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain)
            showMessage("nRFUART's running in background.\n             Disconnect to exit");;*/
                            mService.disconnect();
                            //mService = null;
                            showMessage("Disconnected, Service closed");
                            userWantCloseApp = true;

                        }
                        else {
                            userWantCloseApp = true;
                            Log.d(TAG, "User want close app");

                        }
                        finish();
                    }
                })
                .setNegativeButton(R.string.popup_no, null)
                .show();
    }

    public String getBatteryLevel() {
        String ret ="";
        ret = batteryLevel;
        return ret;
    }
}
