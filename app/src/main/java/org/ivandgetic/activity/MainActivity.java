package org.ivandgetic.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import org.ivandgetic.cube.R;
import org.ivandgetic.AppConfig;

/**
 * Created by ivandgetic on 2015/6/12 0012.
 */
public class MainActivity extends Activity {
    Switch bgMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bgMusic = (Switch) findViewById(R.id.backgroundMusic);
        bgMusic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    AppConfig.playSound = true;
                } else {
                    AppConfig.playSound = false;
                }
            }
        });
    }

    public void enterGame(View view) {
        startActivity(new Intent(this, KubeActivity.class));
        AppConfig.daluan=false;
    }

    public void gameHelp(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("帮助");
        builder.setMessage("通过触摸非魔方处来旋转魔方\n\n通过滑动魔方才进行操作");
        builder.setNegativeButton("明白了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }
}
