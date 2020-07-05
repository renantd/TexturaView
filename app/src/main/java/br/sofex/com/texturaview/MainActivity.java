package br.sofex.com.texturaview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import br.sofex.com.texturaview.Camera.CameraTextureView;
import br.sofex.com.texturaview.Camera.CameraView;
import br.sofex.com.texturaview.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main = DataBindingUtil.setContentView(MainActivity.this,R.layout.activity_main);
        main.callTexture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CameraView.class);
                startActivity(intent);
            }
        });
    }
}
