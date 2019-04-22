package ch.zhaw.android.measuringdata.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import ch.zhaw.android.measuringdata.ActivityStore;
import ch.zhaw.android.measuringdata.MainActivity;
import ch.zhaw.android.measuringdata.R;
import ch.zhaw.android.measuringdata.engine.Engine;

import static java.util.Comparator.comparing;

public class ChartActivity extends AppCompatActivity {

    MainActivity main;
    Engine engine;
    Intent chartIntent;
    public Toolbar toolbar;
    boolean keep;
    boolean userWantCloseApp=false;
    boolean userWantExport=false;

    static Random nr = new Random();
    static String TAG="ChartActivity"+nr.nextInt(10);
    public static final int DISPLAY = 101;

    ArrayList<Entry> lastData;
    LineChart mpLineChart;
    @BindView(R.id.line_chart) View lineChartView;
    LinearLayout receivingContainer;
    YAxis rightYAxis;
    YAxis leftAxis;


    //Swipe-detection
    float x1,x2,y1,y2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chartIntent = this.getIntent();
        boolean keep = chartIntent.getExtras().getBoolean("keep");
        Log.d(TAG, "Created...,keep:"+keep);
        if(keep==true){
            if(ActivityStore.get("uart")==null){
                this.finish();
            }
            else if(main == null){
                main = (MainActivity) ActivityStore.get("main");
                Log.d(TAG,"main:"+main);
                engine = main.getEngine();
                Log.d(TAG,"engine:"+engine);
                if(engine == null){
                    this.finish();
                }
                else {
                    if (engine.getIsAppClosing()) {
                        this.finish();
                    }
                }
            }


            if (savedInstanceState==null) {//only the first time
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                setContentView(R.layout.activity_chart);
                toolbar = findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);

                ButterKnife.bind(this);
                FloatingActionButton fab = findViewById(R.id.fab);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                //Prograss-Cart
                receivingContainer = findViewById(R.id.receiving_container);

                //Line-CHART
                //mpLineChart = (LineChart) findViewById(R.id.line_chart);
                mpLineChart = (LineChart) lineChartView;
                mpLineChart.setBackgroundColor(getResources().getColor(R.color.transparentWhite));
                //Configure Axis
                rightYAxis = mpLineChart.getAxisRight();
                rightYAxis.setEnabled(false);
                leftAxis = mpLineChart.getAxisLeft();
                leftAxis.setAxisMinimum(0);
                leftAxis.setAxisMaximum(750);
                XAxis xAxis = mpLineChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            }

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
            ActivityStore.put("chart",this);
        }
        else if(keep==false)
        {
            this.finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        this.onBackPressed();
        return true;
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
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.popup_title)
                .setMessage(R.string.popup_message)
                .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userWantCloseApp=true;
                        finish();
                    }
                })
                .setNegativeButton(R.string.popup_no, null)
                .show();
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    public float getMax(LineData lineData){
        float maxYData = (int) (Math.ceil(lineData.getYMax()));
        return maxYData;
    }



    public void plot(ArrayList<Entry> data){
        if(mpLineChart.getLineData()!=null)
        {
            resetChart();
        }
        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
        float maxValue = 0;

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

        lineDataSet1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        //lineDataSet1.setCubicIntensity(0.5f);

        //get MAX
        maxValue =  getMax(lineData);
        LimitLine ll = new LimitLine(maxValue, "Max: "+maxValue);
        ll.setLineColor(Color.RED);
        ll.setLineWidth(4f);
        ll.setTextColor(Color.BLACK);
        ll.setTextSize(12f);
        leftAxis.addLimitLine(ll);



        mpLineChart.setScaleXEnabled(true);
        mpLineChart.setScaleYEnabled(false);
        mpLineChart.setData(lineData);
        mpLineChart.invalidate();

    }

    private void resetChart() {
        //mpLineChart.fitScreen();
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











}
