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
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import ch.zhaw.android.measuringdata.ActivityStore;
import ch.zhaw.android.measuringdata.MainActivity;
import ch.zhaw.android.measuringdata.R;

import ch.zhaw.android.measuringdata.engine.Engine;
import ch.zhaw.android.measuringdata.utils.SwipeListener;

import static java.util.Comparator.comparing;

public class ChartActivity extends AppCompatActivity {

    public static final String SHARED_PREFS = "sharedPrefs";

    MainActivity main;
    Engine engine;
    Intent chartIntent;
    public Toolbar toolbar;
    boolean keep;
    boolean userWantCloseApp=false;
    boolean userWantGoBack = false;
    public boolean userWantExport=false;
    boolean displayHasRotated = false;

    static String TAG="ChartActivity";
    public static final int DISPLAY = 101;
    boolean isHomePressed = false;

    ArrayList<Entry> lastData;
    LineChart mpLineChart;
    @BindView(R.id.line_chart) View lineChartView;
    FloatingActionButton fab;
    LinearLayout receivingContainer;
    YAxis rightYAxis;
    YAxis leftAxis;

    SharedPreferences SP = null;

    String deviceName= "";
    Toolbar batteryLevelToolbar;
    int     yAxisMaxValue = 500;
    boolean xAxisDynamic = false;
    boolean yAxisDynamic = false;
    boolean maxLimitSetting =true;
    boolean preLimitSetting = true;


    //Swipe-detection
    // This is the gesture detector compat instance.
    private GestureDetectorCompat gestureDetectorCompat = null;
    float x1,x2,y1,y2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chartIntent = this.getIntent();
        boolean keep = chartIntent.getExtras().getBoolean("keep");
        Log.d(TAG, "Created...,keep:"+keep);
        if(keep==true){
            if(ActivityStore.get("activity_uart")==null){
                this.finish();
            }
            else if(main == null){
                main = (MainActivity) ActivityStore.get("main");
                //Log.d(TAG,"main:"+main);
                engine = main.getEngine();
                //Log.d(TAG,"engine:"+engine);
                if(engine == null){
                    this.finish();
                }
                else {
                    if (engine.getIsAppClosing()) {
                        this.finish();
                    }
                }
            }
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            setContentView(R.layout.activity_chart);
            //Load settings
            loadSettings();

            batteryLevelToolbar = findViewById(R.id.batteryToolbar);
            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ButterKnife.bind(this);
            fab = findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(engine.getLastData()!=null){
                        Context context = getApplicationContext();
                        if(CheckStoragePermission(context)){
                            userWantExport=true;
                        }
                        else{
                            Snackbar.make(view, "No Data Write Permission granted", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                        //ActivityCompat.requestPermissions(ChartActivity.this,
                        //        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

                    }
                    else {
                        Snackbar.make(view, "Nothing to export", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }

                }
            });


            if (engine.getDisplayOrientation() != getResources().getConfiguration().orientation){
                Log.d(TAG, "!>engine display rotated:"+engine.getDisplayOrientation()+", new Orientation "+getResources().getConfiguration().orientation);
                displayHasRotated =true;
            }
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                engine.setDisplayOrientation(Configuration.ORIENTATION_LANDSCAPE); //2
                Log.d(TAG, "engine display:"+engine.getDisplayOrientation());
                getSupportActionBar().hide();
                fab.hide();
            }
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                engine.setDisplayOrientation(Configuration.ORIENTATION_PORTRAIT); //1
                Log.d(TAG, "engine display:"+engine.getDisplayOrientation());
            }


            // Create a common gesture listener object.
            SwipeListener gestureListener = new SwipeListener();
            // Create the gesture detector with the gesture listener.
            gestureDetectorCompat = new GestureDetectorCompat(this, gestureListener);

            //Prograss-Cart to show Measuring started..
            receivingContainer = findViewById(R.id.receiving_container);

            //Line-CHART
            lineChartConfig(xAxisDynamic, yAxisDynamic);


            ActivityStore.put("chart",this);

        }
        else if (keep==false){
            this.finish();
        }
    }




    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        keep = intent.getExtras().getBoolean("keep");
        Log.d(TAG, "On new Intent keep:"+keep);
        if(keep==true){
            //Line-CHART
            lineChartConfig(xAxisDynamic, yAxisDynamic);
            ActivityStore.put("chart",this);
        }
        else if(keep==false)
        {
            this.finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            //Todo testing Settings
            case  R.id.action_settings:
                Log.d(String.valueOf(this), "Menu Item clicked->Settings");
                Intent settings_intent = new Intent(ChartActivity.this,
                        SettingsActivity.class);
                startActivity(settings_intent);
                return false;
            case  R.id.action_about:
                Log.d(String.valueOf(this), "Menu Item clicked->About");
                Intent about_intent = new Intent(ChartActivity.this,
                        AboutActivity.class);
                startActivity(about_intent);
                return false;
            case android.R.id.home:
                this.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chart, menu);
        menu.findItem(R.id.action_settings);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if(ActivityStore.get("activity_uart")==null){
            this.finish();
        }
        else if(main == null){
            main = (MainActivity) ActivityStore.get("main");
            //Log.d(TAG,"main:"+main);
            engine = main.getEngine();
            //Log.d(TAG,"engine:"+engine);
            if(engine == null){
                this.finish();
            }
            else {
                if (engine.getIsAppClosing()) {
                    this.finish();
                }
            }
        }
        Log.d(TAG, "set user has rotated > engine:"+displayHasRotated);
        engine.setUserRotated(displayHasRotated);
        engine.setDisplayReady(true);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(MainActivity.TAG, "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode + " data:" + data.getExtras().getString("hello"));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        ActivityStore.put("chart",null);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG,"onBack pressed");
        userWantGoBack = true;
        isHomePressed = false;
    }


    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        //Todo when home Button pressed ->
        //if(getEngine()!=null) {
        //    getEngine().setRun(false);
        //}
        //finish();
        //userWantGoBack = true;
        //Toast.makeText(this, TAG+" User pressed Home Button", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        //Todo when home Button pressed ->
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            isHomePressed = true;
            Log.d(TAG, "Home Button pressed");
            //notifyBackground();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        userWantCloseApp=true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Pass activity on touch event to the gesture detector.
        gestureDetectorCompat.onTouchEvent(event);
        // Return true to tell android OS that event has been consumed, do not pass it to other event listeners.
        return true;
    }
    @TargetApi(Build.VERSION_CODES.M)
    public boolean CheckStoragePermission(@NonNull final Context context) {
        int permissionCheckRead = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionCheckWrite = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheckRead != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ChartActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(ChartActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(ChartActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }
            return false;
        }
        if (permissionCheckWrite != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ChartActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(ChartActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(ChartActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
            return false;
        }else
            return true;
    }

//    FIXME Battery
//    @Override
//    public void onBatteryLevelChanged(@NonNull final BluetoothDevice device, final int batteryLevel) {
//        runOnUiThread(() -> batteryLevelToolbar.setTitle(getString(R.string.battery, batteryLevel)));
//    }



    private void loadSettings() {
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        xAxisDynamic = SP.getBoolean("check_box_relative_x_axes", false);
        yAxisDynamic = SP.getBoolean("check_box_relative_y_axes", false);
        deviceName = SP.getString("edit_saved_device", "NA");
        maxLimitSetting = SP.getBoolean("limit_line_max", true);
        preLimitSetting = SP.getBoolean("limit_line_preForce",true);

    }

    private void lineChartConfig(boolean xAxisRelative, boolean yAxisRelative) {
        Log.d(TAG, "x:"+xAxisRelative+" y:"+yAxisRelative);
        //mpLineChart = (LineChart) findViewById(R.id.line_chart);
        mpLineChart = (LineChart) lineChartView;
        mpLineChart.setBackgroundColor(getResources().getColor(R.color.rotLogo_white));
        mpLineChart.setScaleEnabled(false);
        //Configure Axis
        rightYAxis = mpLineChart.getAxisRight();
        rightYAxis.setEnabled(false);
        leftAxis = mpLineChart.getAxisLeft();
        if(yAxisRelative){
            leftAxis.resetAxisMinimum();
            leftAxis.resetAxisMaximum();
        }else{
            leftAxis.setAxisMinimum(0);
            leftAxis.setAxisMaximum(yAxisMaxValue);
        }
        XAxis xAxis = mpLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        leftAxis.removeAllLimitLines();
    }

    public int getDetectedPeak(ArrayList<Entry> data) {
        int ret = 0;
        int threshold = 10;
        boolean isPeak = false;
        for (int n = 0; (n < data.size() - 10) && !isPeak; n++) {
            float value = data.get(n).getY();
            float delta = data.get(n + 10).getY() - data.get(n).getY();
            if (delta > 0 && delta > threshold) {
                System.out.println("Start Peak is "+data.get(n).getY()+" at:" + data.get(n).getX());
                isPeak = true;
                ret = n;
            }
        }
        return ret;
    }

    /**
     * Return the max Y-Value of LineData
     * @param lineData
     * @return maxYData
     */
    public int getMax(LineData lineData){
        int maxYData = (int) Math.ceil(lineData.getYMax());
        return maxYData;
    }



    /**
     * Calculates the pre Force of the Measurement
     * 1. First it has to detect the Peak using -> getDetectedPeak()
     * 2. Than we look at a window sized length before this peak -> window
     * 3. The average of the points in this window is the preForce.
     * @param data
     * @return index before the peek Starts
     */
    public int getPreForce(ArrayList<Entry> data){
        int ret = 0;
        int size = 4;
        int window = 50;
        int peakAt = getDetectedPeak(data);
        if(peakAt-(size*window) > 0) {
            Log.d(TAG,"peakAt:"+data.get(peakAt-(size*window)).getX()+","+data.get(peakAt-(size*window)).getY());
            for (int n = peakAt - (size * window); n < peakAt - window; n++) {
                //Log.d(TAG,"avg of:"+data.get(n).getX()+","+data.get(n).getY());
                ret += data.get(n).getY();

            }
            ret = ret / ((size-1)*window);
            //Log.d(TAG,"PreForce"+ret);
        }
        ret = (int) Math.ceil(ret);
        return ret;
    }





    public void plot(ArrayList<Entry> data){
        Log.d(TAG, "plotting...");
        if(mpLineChart.getLineData()!=null)
        {
            resetChart();
        }
        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
        int maxValue = 0;
        int preForce = 0;

        //MP-LineChart
        LineDataSet lineDataSet1 = new LineDataSet(data, "Messung von:   "+currentDateTimeString);
        lineDataSet1.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet1.setDrawCircles(false);
        lineDataSet1.setDrawValues(false);
        lineDataSet1.setLineWidth(2f);
        //lineDataSet1.
        mpLineChart.setDescription(null);
        ArrayList<ILineDataSet> dataSets = new ArrayList();
        dataSets.add(lineDataSet1);
        LineData lineData = new LineData(dataSets);
        lineDataSet1.notifyDataSetChanged();

        // Cubic Line
        lineDataSet1.setMode(LineDataSet.Mode.LINEAR);
        //lineDataSet1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        //lineDataSet1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        //lineDataSet1.setCubicIntensity(1f);

        //get MAX
        if(maxLimitSetting){
            maxValue =  getMax(lineData);
            LimitLine max_limit = new LimitLine(maxValue, "Max: "+maxValue);
            max_limit.setLineColor(Color.RED);
            max_limit.setLineWidth(4f);
            max_limit.setTextColor(Color.BLACK);
            max_limit.setTextSize(20f);
            leftAxis.addLimitLine(max_limit);

        }


        //lower Limit
        if(preLimitSetting){
            if(data!=null){
                if(data.size()>100) {
                    preForce = getPreForce(data);
                    if(preForce!=0){
                        LimitLine lower_limit = new LimitLine(preForce, "Pre Force: "+preForce);
                        lower_limit.setLineWidth(4f);
                        lower_limit.setLineColor(getResources().getColor(R.color.colorPrimaryDark));
                        lower_limit.enableDashedLine(10f, 10f, 0f);
                        lower_limit.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
                        lower_limit.setTextSize(12f);
                        leftAxis.addLimitLine(lower_limit);
                    }
                }
            }
        }





        mpLineChart.setScaleXEnabled(true);
        mpLineChart.setScaleYEnabled(false);
        mpLineChart.setData(lineData);
        mpLineChart.invalidate();

    }

    private void resetChart() {
        //mpLineChart.fitScreen();
        loadSettings();
        lineChartConfig(xAxisDynamic, yAxisDynamic);
        leftAxis.removeAllLimitLines();
        mpLineChart.getLineData().clearValues();
        mpLineChart.getXAxis().setValueFormatter(null);
        mpLineChart.notifyDataSetChanged();
        mpLineChart.clear();
        mpLineChart.invalidate();
        receivingContainer.setVisibility(View.GONE);
    }

    public boolean isUserWantCloseApp(){
        return  userWantCloseApp;
    }

    public void showStartReceived(boolean displayReceiving) {
        Log.d(TAG,"startReceived showing");
        if(displayReceiving ==true){
            receivingContainer.setVisibility(View.VISIBLE);
            receivingContainer.invalidate();
        }
        else{
            receivingContainer.setVisibility(View.GONE);
        }

    }

    public boolean isUserWantExport(){
        return userWantExport;
    }

    public boolean isUserWantGoBack() {
        return userWantGoBack;
    }

    public void resetUserHasRotated() {
        displayHasRotated = false;
    }

    public void showBatteryLevel(String batteryLevel) {
        batteryLevelToolbar = (Toolbar) findViewById(R.id.batteryToolbar);
        batteryLevelToolbar.setTitle(""+batteryLevel+"%");
    }

/*
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
    */











}
