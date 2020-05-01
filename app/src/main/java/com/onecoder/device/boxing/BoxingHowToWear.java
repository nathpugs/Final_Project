package com.onecoder.device.boxing;

import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


import com.onecoder.device.R;
import com.onecoder.device.base.BaseActivity;


public class BoxingHowToWear extends AppCompatActivity {
    private Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.boxing_how_to_wear);


        ImageView firstImage = (ImageView) findViewById(R.id.firstImage);
        ImageView secondImage = (ImageView) findViewById(R.id.secondImage);
        ImageView thirdImage = (ImageView) findViewById(R.id.thirdImage);
        ImageView fourthImage = (ImageView) findViewById(R.id.fourthImage);


        firstImage.setImageResource(R.drawable.tutorial_1);
        secondImage.setImageResource(R.drawable.tutorial_2);
        thirdImage.setImageResource(R.drawable.tutorial_3);
        fourthImage.setImageResource(R.drawable.tutorial_4);

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
