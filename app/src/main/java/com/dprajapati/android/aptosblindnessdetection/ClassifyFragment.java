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


public class ClassifyFragment extends Fragment {
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_classify,container,false);
        //loadImages();
        windUpWidgets(rootView);
            btnDetectObject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        InputStream imageStream = Objects.requireNonNull(getActivity()).getContentResolver().openInputStream(imageUri);
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
        imageViewResult = view.findViewById(R.id.imageViewResult);
        textViewResult = view.findViewById(R.id.textViewResult);
        btnDetectObject = view.findViewById(R.id.button_detect);
        mResultCardView = view.findViewById(R.id.resultCardView);

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