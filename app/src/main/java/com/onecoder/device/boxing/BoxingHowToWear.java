package com.onecoder.device.boxing;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;


import com.onecoder.device.R;
import com.onecoder.device.base.BaseActivity;


public class BoxingHowToWear extends BaseActivity {
    private Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.boxing_how_to_wear);

        button = (Button) findViewById(R.id.howToBtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBoxingSelectExperience();
            }
        });
    }

    public void openBoxingSelectExperience() {
        Intent intent = new Intent(this, BoxingSelectExperience.class);
        startActivity(intent);
    }
}
