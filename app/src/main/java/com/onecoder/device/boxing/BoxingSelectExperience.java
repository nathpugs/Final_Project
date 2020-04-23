package com.onecoder.device.boxing;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;


import com.onecoder.device.R;
import com.onecoder.device.base.BaseActivity;


public class BoxingSelectExperience extends BaseActivity {
    private Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.boxing_select_exp);

        button = (Button) findViewById(R.id.selectExpBtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBoxingMainActivity();
            }
        });
    }

    public void openBoxingMainActivity() {
        Intent intent = new Intent(this, BoxingMainActivity.class);
        startActivity(intent);
    }
}
