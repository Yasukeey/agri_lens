package com.example.agrilens;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.agrilens.ml.DiseaseDetection; // Import the generated model class from TensorFlow Lite

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {

    private TextView result, demoText, classified, clickHere;
    private ImageView imageView;
    private Button cameraButton, galleryButton;

    private final int imageSize = 224; // Input size for the TensorFlow Lite model

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        result = findViewById(R.id.result);
        imageView = findViewById(R.id.imageView);
        cameraButton = findViewById(R.id.cameraButton);
        galleryButton = findViewById(R.id.galleryButton);
        demoText = findViewById(R.id.demoText);
        clickHere = findViewById(R.id.click_here);
        classified = findViewById(R.id.classified);

        // Set initial visibility
        demoText.setVisibility(View.VISIBLE);
        clickHere.setVisibility(View.GONE);
        classified.setVisibility(View.GONE);
        result.setVisibility(View.GONE);

        // Handle camera button click
        cameraButton.setOnClickListener(view -> {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, 1);
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
            }
        });

        // Handle gallery button click
        galleryButton.setOnClickListener(view -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, 2);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bitmap image = null;
            try {
                if (requestCode == 1 && data != null) { // Camera Result
                    image = (Bitmap) data.getExtras().get("data");
                } else if (requestCode == 2 && data != null) { // Gallery Result
                    Uri selectedImage = data.getData();
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                }

                if (image != null) {
                    processImage(image);
                } else {
                    Toast.makeText(this, "Error retrieving image", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void processImage(Bitmap image) {
        int dimension = Math.min(image.getWidth(), image.getHeight());
        image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);

        imageView.setImageBitmap(image);
        demoText.setVisibility(View.GONE);
        clickHere.setVisibility(View.VISIBLE);
        classified.setVisibility(View.VISIBLE);
        result.setVisibility(View.VISIBLE);

        image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
        classifyImage(image);
    }

    private void classifyImage(Bitmap image) {
        try {
            DiseaseDetection model = DiseaseDetection.newInstance(getApplicationContext());

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValue = new int[imageSize * imageSize];
            image.getPixels(intValue, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

            int pixel = 0;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValue[pixel++];
                    byteBuffer.putFloat(((val >> 16) & 0xFF) / 255.0f); // Red
                    byteBuffer.putFloat(((val >> 8) & 0xFF) / 255.0f);  // Green
                    byteBuffer.putFloat((val & 0xFF) / 255.0f);         // Blue
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            DiseaseDetection.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            float[] confidence = outputFeature0.getFloatArray();

            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidence.length; i++) {
                if (confidence[i] > maxConfidence) {
                    maxConfidence = confidence[i];
                    maxPos = i;
                }
            }

            String[] classes = {"Apple Black Rot", "Grape Black Rot", "Powdery Mildew", "Grey Mold", "Rust", "Gray Leaf Spot", "Common Smut", "Anthracnose", "Downy Mildew", "Black Spot"};
            result.setText(classes[maxPos]);
            result.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + result.getText()))));

            model.close();
        } catch (IOException e) {
            Toast.makeText(this, "Error during classification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
