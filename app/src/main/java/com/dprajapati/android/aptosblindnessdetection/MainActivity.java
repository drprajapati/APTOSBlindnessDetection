package com.dprajapati.android.aptosblindnessdetection;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dprajapati.android.aptosblindnessdetection.ml.Classifier;
import com.dprajapati.android.aptosblindnessdetection.ml.TensorFlowImageClassifier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int INPUT_SIZE = 224;
    private static final boolean QUANT = false;
    private static final String MODEL_PATH = "mobilenettranfer91.tflite";
    private static final String LABEL_PATH = "labels.txt";
    private static final int PICK_IMAGE = 1;
    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();
    private TextView textViewResult;
    private Uri imageUri;
    private Button btnDetectObject;
    private ImageView imageViewResult;
    private AppCompatImageView mImageView;
    private List<Bitmap> mImages;
    private int mIndex;
    private CardView mResultCardView;
    private boolean clicked = false;
    public static Intent getIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //loadImages();
        windUpWidgets();
        if (mImageView.getDrawable() != null) {
            btnDetectObject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clicked = true;
                    try {
                        InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
                        imageViewResult.setImageBitmap(bitmap);
                        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
                        textViewResult.setText(results.toString());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    //    Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
                }

            });

        } else {
            Toast.makeText(getApplicationContext(), "Please Select Image", Toast.LENGTH_SHORT);
        }

        if(clicked){
            mResultCardView.setVisibility(View.VISIBLE);
        }else{
            mResultCardView.setVisibility(View.INVISIBLE);
        }
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFromGallery();
            }
        });
        initTensorFlowAndLoadModel();
    }

    private void pickFromGallery() {
        Intent gallery = new Intent();
        gallery.setType("image/*");
        gallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(gallery, "Select Picture"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mImages = new ArrayList<>();
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                mImageView.setImageBitmap(bitmap);
                mImages.add(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_PATH,
                            LABEL_PATH,
                            INPUT_SIZE,
                            QUANT);
                    makeButtonVisible();
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    private void makeButtonVisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnDetectObject.setVisibility(View.VISIBLE);
            }
        });
    }

    private void windUpWidgets() {
        mImageView = findViewById(R.id.imageView);
        imageViewResult = findViewById(R.id.imageViewResult);
        textViewResult = findViewById(R.id.textViewResult);
        textViewResult.setMovementMethod(new ScrollingMovementMethod());
        btnDetectObject = findViewById(R.id.button_detect);
        mResultCardView = findViewById(R.id.resultCardView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
    }

}
