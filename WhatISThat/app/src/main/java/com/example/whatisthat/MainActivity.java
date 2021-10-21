package com.example.whatisthat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // frontend elements
    private Button takePictureBtn;
    private TextureView cameraView;
    private TextView inceptionTextResponse;

    Photography phy = new Photography(this, takePictureBtn, cameraView);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind frontend camera textureview holder
        cameraView = (TextureView) findViewById(R.id.camera_view);
        cameraView.setSurfaceTextureListener(phy.cameraListener);

        // Bind frontend inception text holder
        // inceptionTextResponse = (TextView) findViewById(R.id.inception_response);

        // Bind frontend takepicture Button
        takePictureBtn = (Button) findViewById(R.id.btn_takepicture);
        takePictureBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                phy.takePicture();
            }
        });
    }
}