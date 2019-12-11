package com.dprajapati.android.aptosblindnessdetection.classify;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;

import com.dprajapati.android.aptosblindnessdetection.R;
import com.dprajapati.android.aptosblindnessdetection.utils.Classifier;
import com.dprajapati.android.aptosblindnessdetection.utils.TensorFlowImageClassifier;
import com.google.android.material.button.MaterialButton;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.app.Activity.RESULT_OK;


public class ClassifyFragment extends Fragment {

    private static final int INPUT_SIZE = 224, PICK_IMAGE = 1;
    private static final boolean QUANTIZATION = false;
    private static final String MODEL_PATH = "mobile_net_v2.tflite", LABEL_PATH = "labels.txt";

    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();
    private Uri imageUri;
    private MaterialButton mClassifyButton, mChooseImageButton;
    private AppCompatImageView mImageView;
    private LinearLayoutCompat mProgressBarLayout;


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_classify, container, false);
        windUpWidgets(rootView);

        mClassifyButton.setOnClickListener(v -> {
            try {
                InputStream imageStream = Objects.requireNonNull(getActivity()).getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
                mProgressBarLayout.removeAllViews();
                for (Classifier.Recognition result : results) {
                    Log.d("Hello", result.getTitle() + ";" + result.getConfidence());
                    ProgressBar progressBar = new ProgressBar(this.getActivity(),
                            null, android.R.attr.progressBarStyleHorizontal);
                    AppCompatTextView textView = new AppCompatTextView(getActivity().getApplicationContext(),
                            null, android.R.attr.text);
                    textView.setTextColor(getResources().getColor(R.color.textColorPrimary));
                    textView.setText(String.format(Locale.getDefault(),
                            "%s : %f %s", result.getTitle(), result.getConfidence() * 100.0f, "%"));
                    progressBar.setIndeterminate(false);
                    progressBar.setMax(100);
                    progressBar.setProgress(Math.round(result.getConfidence() * 100));
                    mProgressBarLayout.addView(textView);
                    mProgressBarLayout.addView(progressBar);
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });

        mChooseImageButton.setOnClickListener(v -> pickFromGallery());

        initTensorFlowAndLoadModel();
        return rootView;
    }

    private void windUpWidgets(View view) {
        mImageView = view.findViewById(R.id.retina_image_view);
        mClassifyButton = view.findViewById(R.id.button_detect);
        mChooseImageButton = view.findViewById(R.id.file_button);
        mProgressBarLayout = view.findViewById(R.id.progress_bar_layout);
    }

    private void pickFromGallery() {
        Intent gallery = new Intent();
        gallery.setType("image/*");
        gallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(gallery, "Select Picture"), PICK_IMAGE);
        mProgressBarLayout.removeAllViews();
        mClassifyButton.setClickable(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            imageUri = Objects.requireNonNull(data).getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        Objects.requireNonNull(getActivity()).getContentResolver(), imageUri);
                mImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(() -> {
            try {
                classifier = TensorFlowImageClassifier.create(
                        Objects.requireNonNull(getActivity()).getAssets(),
                        MODEL_PATH,
                        LABEL_PATH,
                        INPUT_SIZE,
                        QUANTIZATION);
                makeButtonVisible();
            } catch (final Exception e) {
                throw new RuntimeException("Error initializing TensorFlow!", e);
            }
        });
    }

    private void makeButtonVisible() {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            mClassifyButton.setVisibility(View.VISIBLE);
            mClassifyButton.setClickable(false);
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.execute(() -> classifier.close());
    }
}