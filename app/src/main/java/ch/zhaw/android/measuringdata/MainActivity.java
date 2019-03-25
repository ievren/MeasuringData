package ch.zhaw.android.measuringdata;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import ch.zhaw.android.measuringdata.data.Data;
import ch.zhaw.android.measuringdata.chart.ChartActivity;
import ch.zhaw.android.measuringdata.engine.Engine;
import ch.zhaw.android.measuringdata.uart.UartActivity;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {


    public static final String TAG = "MeasuringApp";
    public static MainActivity obj;

    Intent  chartIntent;
    Intent  uartIntent;
    Data    data;
    Engine  engine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Data seved to "+getPackageResourcePath()+"...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });



        chartIntent = new Intent(MainActivity.this, ChartActivity.class);
        uartIntent = new Intent(MainActivity.this, UartActivity.class);


        data=new Data();
        engine=new Engine();
        engine.setChart(null);
        engine.setData(data);
        engine.setRun(true);
        engine.execute();
        //engine.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");


        ActivityStore.put("main",this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_chart:
                Log.d(String.valueOf(this), "Menu Item clicked->action CHART");
                startActivity(chartIntent);
                return true;

            case R.id.action_uart:
                Log.d(String.valueOf(this), "Menu Item clicked->action UART");
                startActivity(uartIntent);
                return true;

            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}