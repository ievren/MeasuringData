package ch.zhaw.android.measuringdata;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.net.URLConnection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import ch.zhaw.android.measuringdata.data.Data;
import ch.zhaw.android.measuringdata.engine.Engine;
import ch.zhaw.android.measuringdata.ui.ChartActivity;
import ch.zhaw.android.measuringdata.ui.SettingsActivity;
import ch.zhaw.android.measuringdata.ui.UartActivity;

public class MainActivity extends AppCompatActivity {


    public static final String TAG = "MeasuringApp";
    public static MainActivity obj;

    public static  boolean isAppClosing = false;
    Intent chartIntent;
    Intent uartIntent;
    Intent settingsIntent;
    Intent mainIntent;

    Data data;
    Engine engine;
    // BtService btService; //Bluetooth Connection initialized by UARTActivity


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "created...");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dir = "/download";
                String fileName = "measuringData.tsv";
                //Snackbar.make(view, "Data seved to " + getPackageResourcePath() + "...", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                Context context = getApplicationContext();
                //////
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                /////

                /** Debug Test File Write
                data.exportData(data.getTestData(), dir, fileName);
                File listFile = new File(Environment.getExternalStorageDirectory() + "/" + dir,fileName);
                if(listFile.exists()) {
                    Intent intentShareFile = new Intent(Intent.ACTION_SEND);
                    Log.d(TAG,URLConnection.guessContentTypeFromName(listFile.getName()));
                    intentShareFile.setType("text/*");
                    intentShareFile.putExtra(Intent.EXTRA_STREAM,
                            Uri.parse("file://" + listFile.getAbsolutePath()));
                    startActivity(Intent.createChooser(intentShareFile, "Share File"));
                }
                else {
                    Toast.makeText(context, "Couldnt export data", Toast.LENGTH_SHORT).show();
                }
                 */



            }
        });

        //FIXME -> Doesent work nice... -> cant close sometimes..
        chartIntent = new Intent(MainActivity.this, ChartActivity.class);
        chartIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        chartIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        chartIntent.putExtra("keep", false);
        startActivity(chartIntent);

        uartIntent = new Intent(MainActivity.this, UartActivity.class);
        uartIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        uartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        uartIntent.putExtra("keep", false);
        startActivity(uartIntent);

        mainIntent = new Intent(MainActivity.this, MainActivity.class);
        settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        IntentStore.put("main",mainIntent);
        IntentStore.put("chart",chartIntent);
        IntentStore.put("uart",uartIntent);
        IntentStore.put("settings",settingsIntent);
        IntentStore.get("main");


        data = new Data();
        engine = new Engine(this);
        Log.d(TAG,"engine:"+engine);
        engine.setChart((ChartActivity) ActivityStore.get("chart"));
        engine.setData(data);
        engine.setRun(true);
        Log.d(TAG, "set Run true");
        engine.execute();


        //engine.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");


        ActivityStore.put("main", this);


    }

    public void closeApp() {
        isAppClosing = true;
        Log.d(TAG, "close App");
        engine.setRun(false);
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public boolean getIsAppClosing(){
        return isAppClosing;
    }

    public Engine getEngine() {
        return engine;
    }

    public void openSettingsActivity(View view){
        Intent intent = new Intent(this, SettingsActivity.class);
        ActivityStore.get("main").startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_in_right);

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

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
        if(getEngine()!=null) {
            getEngine().setRun(false);
        }
        finish();
    }
}
