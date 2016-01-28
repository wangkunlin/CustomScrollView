package com.wkl.scroll;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.wkl.scroll.view.Panel;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Panel panel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
         panel = (Panel) findViewById(R.id.panel);
        panel.close();

        findViewById(R.id.btn).setOnClickListener(this);
        findViewById(R.id.arrow).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (panel.isOpen()) {
            panel.close();
        } else {
            panel.open();
        }
    }
}
