package com.onecoder.device.boxing;

import android.content.Intent;
import android.os.Bundle;

import android.support.v7.widget.AppCompatImageButton;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


import com.onecoder.device.R;
import com.onecoder.device.base.BaseActivity;


public class BoxingSelectExperience extends BaseActivity {
    private AppCompatImageButton button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.boxing_select_exp);

        ImageView beginnerImage = (ImageView) findViewById(R.id.beginnerBtn);
        ImageView coachImage = (ImageView) findViewById(R.id.coachBtn);

        beginnerImage.setImageResource(R.drawable.beginner);
        coachImage.setImageResource(R.drawable.coach);



        button = (AppCompatImageButton) findViewById(R.id.beginnerBtn);
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

