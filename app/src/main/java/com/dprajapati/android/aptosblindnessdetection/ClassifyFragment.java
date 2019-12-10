package com.dprajapati.android.aptosblindnessdetection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;


import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.widget.Constraints.TAG;


public class ClassifyFragment extends Fragment {
    private static final int INPUT_SIZE = 224;
    private static final boolean QUANT = false;
    private static final String MODEL_PATH = "mobilenettranfer91.tflite";
    private static final String LABEL_PATH = "labels.txt";
    private static final int PICK_IMAGE = 1;
    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();
    private Uri imageUri;
    private Button btnDetectObject;
    private AppCompatImageView mImageView;
    private List<Bitmap> mImages;
    private CircleDisplay noDrCircleDisplay;
    private CircleDisplay mildCircleDisplay;
    private CircleDisplay moderateCircleDisplay;
    private CircleDisplay severeCircleDisplay;
    private CircleDisplay proliferativeCircleDisplay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_classify, container, false);
        //loadImages();
        windUpWidgets(rootView);
        btnDetectObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    InputStream imageStream = Objects.requireNonNull(getActivity()).getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                    bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
                    final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
                    noDrCircleDisplay.setAnimDuration(4000);
                    noDrCircleDisplay.setValueWidthPercent(55f);
                    noDrCircleDisplay.setFormatDigits(1);
                    noDrCircleDisplay.setDimAlpha(80);
                    noDrCircleDisplay.setTouchEnabled(true);
                    noDrCircleDisplay.setUnit("%");
                    noDrCircleDisplay.setColor(R.color.red);
                    noDrCircleDisplay.setTextSize(10);
                    noDrCircleDisplay.setStepSize(0.5f);
                    float resultdrFloat = Float.parseFloat(results.get(0).toString());
                    noDrCircleDisplay.showValue(resultdrFloat, 100f, true);

                    if (results.get(1) != null) {
                        mildCircleDisplay.setAnimDuration(4000);
                        mildCircleDisplay.setValueWidthPercent(55f);
                        mildCircleDisplay.setFormatDigits(1);
                        mildCircleDisplay.setDimAlpha(80);
                        mildCircleDisplay.setTouchEnabled(true);
                        mildCircleDisplay.setUnit("%");
                        mildCircleDisplay.setColor(R.color.red);
                        mildCircleDisplay.setTextSize(10);
                        mildCircleDisplay.setStepSize(0.5f);
                        float resultMildFloat = Float.parseFloat(results.get(1).toString());
                        mildCircleDisplay.showValue(resultMildFloat, 100f, true);
                    }
                    if (results.get(2) != null) {
                        moderateCircleDisplay.setAnimDuration(4000);
                        moderateCircleDisplay.setValueWidthPercent(55f);
                        moderateCircleDisplay.setFormatDigits(1);
                        moderateCircleDisplay.setDimAlpha(80);
                        moderateCircleDisplay.setTouchEnabled(true);
                        moderateCircleDisplay.setUnit("%");
                        moderateCircleDisplay.setColor(R.color.red);
                        moderateCircleDisplay.setTextSize(10);
                        moderateCircleDisplay.setStepSize(0.5f);
                        float resultModerateFloat = Float.parseFloat(results.get(2).toString());
                        moderateCircleDisplay.showValue(resultModerateFloat, 100f, true);
                    }
                    if (results.get(3) != null) {

                        severeCircleDisplay.setAnimDuration(4000);
                        severeCircleDisplay.setValueWidthPercent(55f);
                        severeCircleDisplay.setFormatDigits(1);
                        severeCircleDisplay.setDimAlpha(80);
                        severeCircleDisplay.setTouchEnabled(true);
                        severeCircleDisplay.setUnit("%");
                        severeCircleDisplay.setColor(R.color.red);
                        severeCircleDisplay.setTextSize(10);
                        severeCircleDisplay.setStepSize(0.5f);
                        float resultSevereFloat = Float.parseFloat(results.get(3).toString());
                        severeCircleDisplay.showValue(resultSevereFloat, 100f, true);
                    }
                    if (results.get(4) != null) {
                        proliferativeCircleDisplay.setAnimDuration(4000);
                        proliferativeCircleDisplay.setValueWidthPercent(55f);
                        proliferativeCircleDisplay.setFormatDigits(1);
                        proliferativeCircleDisplay.setDimAlpha(80);
                        proliferativeCircleDisplay.setTouchEnabled(true);
                        proliferativeCircleDisplay.setUnit("%");
                        proliferativeCircleDisplay.setColor(R.color.red);
                        proliferativeCircleDisplay.setTextSize(10);
                        proliferativeCircleDisplay.setStepSize(0.5f);
                        float resultProFloat = Float.parseFloat(results.get(4).toString());
                        proliferativeCircleDisplay.showValue(resultProFloat, 100f, true);
                    }

                    //textViewResult.setText(results.toString());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                //    Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
            }

        });

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFromGallery();
            }
        });
        initTensorFlowAndLoadModel();

        return rootView;
    }

    private void windUpWidgets(View view) {
        mImageView = view.findViewById(R.id.imageView);
        btnDetectObject = view.findViewById(R.id.button_detect);
        noDrCircleDisplay = view.findViewById(R.id.noDrCircleView);
        mildCircleDisplay = view.findViewById(R.id.mildCircleView);
        severeCircleDisplay = view.findViewById(R.id.severeCircleView);
        proliferativeCircleDisplay = view.findViewById(R.id.proliferativeCircleView);
        moderateCircleDisplay = view.findViewById(R.id.moderateCircleView);
    }

    private void pickFromGallery() {
        Intent gallery = new Intent();
        gallery.setType("image/*");
        gallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(gallery, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mImages = new ArrayList<>();
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
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
                            getActivity().getAssets(),
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
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnDetectObject.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
    }
}