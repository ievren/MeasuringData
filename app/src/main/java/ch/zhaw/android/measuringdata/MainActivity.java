package ch.zhaw.android.measuringdata;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import ch.zhaw.android.measuringdata.ui.ChartActivity;
import ch.zhaw.android.measuringdata.data.Data;
import ch.zhaw.android.measuringdata.engine.Engine;
import ch.zhaw.android.measuringdata.uart.BtService;
import ch.zhaw.android.measuringdata.ui.UartActivity;
import ch.zhaw.android.measuringdata.utils.IntentStore;

public class MainActivity extends AppCompatActivity {


    public static final String TAG = "MeasuringApp";
    public static MainActivity obj;

    Intent chartIntent;
    Intent uartIntent;
    Intent mainIntent;

    Data data;
    Engine engine;
    // BtService btService; //Bluetooth Connection initialized by UARTActivity


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
                Snackbar.make(view, "Data seved to " + getPackageResourcePath() + "...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        chartIntent = new Intent(MainActivity.this, ChartActivity.class);
        startActivity(chartIntent);
        uartIntent = new Intent(MainActivity.this, UartActivity.class);
        startActivity(uartIntent);
        mainIntent = new Intent(MainActivity.this, MainActivity.class);
        IntentStore.put("main",mainIntent);
        IntentStore.put("chart",chartIntent);
        IntentStore.put("uart",uartIntent);
        IntentStore.get("main");


        data = new Data();
        engine = new Engine(this);
        engine.setChart((ChartActivity) ActivityStore.get("chart"));
        engine.setData(data);
        engine.setRun(true);
        engine.execute();
        //engine.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");


        ActivityStore.put("main", this);


    }

    public void closeApp(){
        Log.d(TAG,"close App");
        engine.cancel(true);
        finish();
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
        switch (item.getItemId()) {
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
