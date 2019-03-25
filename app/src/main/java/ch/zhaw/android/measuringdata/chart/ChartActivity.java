package ch.zhaw.android.measuringdata.chart;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import ch.zhaw.android.measuringdata.ActivityStore;
import ch.zhaw.android.measuringdata.MainActivity;
import ch.zhaw.android.measuringdata.R;

public class ChartActivity extends AppCompatActivity {

    static String TAG="CharActivity";
    public static final int DISPLAY = 101;

    ArrayList<Entry> lastData;
    LineChart mpLineChart;
    boolean display;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mpLineChart = (LineChart) findViewById(R.id.line_chart);
        ActivityStore.put("chart",this);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(MainActivity.TAG, "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode + " data:" + data.getExtras().getString("hello"));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityStore.put("chart",null);
    }

    public void plot(ArrayList<Entry> data){
        //MP-LineChart
        LineDataSet lineDataSet1 = new LineDataSet(data, "TestPunkte");
        ArrayList<ILineDataSet> dataSets = new ArrayList();
        dataSets.add(lineDataSet1);

        LineData lineData = new LineData(dataSets);
        mpLineChart.setData(lineData);
        mpLineChart.invalidate();
    }

}
