package com.onecoder.device.boxing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.onecoder.device.R;


public class BoxingFeedback extends AppCompatActivity {
    private Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.boxing_feedback);

        button = (Button) findViewById(R.id.newRdBtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewRd();
            }
        });
    }

    public void openNewRd() {
        Intent intent = new Intent(this, BoxingMainActivity.class);
        startActivity(intent);
    }
}
