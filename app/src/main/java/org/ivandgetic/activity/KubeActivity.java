package org.ivandgetic.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import org.ivandgetic.cube.R;
import org.ivandgetic.AppConfig;
import org.ivandgetic.view.MyGLSurfaceView;

import java.util.Timer;
import java.util.TimerTask;


public class KubeActivity extends Activity {

    Timer timer = new Timer();
    int timeshow = 0;
    private MyGLSurfaceView mGLSurfaceView;
    private Switch showCoordinateSystem;
    private TextView timeTV;
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            timeshow++;
            timeTV.post(new Runnable() {
                @Override
                public void run() {
                    timeTV.setText(timeshow + "秒");
                }
            });
        }
    };
    private Button daluan;
    private Button numberPickerButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kube);
        mGLSurfaceView = (MyGLSurfaceView) findViewById(R.id.myGLSurfaceView);
        daluan = (Button) findViewById(R.id.daluan);
        showCoordinateSystem = (Switch) findViewById(R.id.showCoordinateSystem);
        showCoordinateSystem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    AppConfig.showCoordinateSystem = true;
                } else {
                    AppConfig.showCoordinateSystem = false;
                }
            }
        });
        timeTV = (TextView) findViewById(R.id.timeTV);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }


    public void startGame(View view) {
        AppConfig.daluan=true;
        timer.schedule(timerTask, 1000, 1000);
        daluan.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_kube, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_show) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("警告");
            builder.setMessage("还有很多不完善的地方\n比如操作魔方会有bug");
            builder.setNegativeButton("明白了", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }

}