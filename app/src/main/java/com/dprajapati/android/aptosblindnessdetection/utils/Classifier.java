package com.dprajapati.android.aptosblindnessdetection.utils;


import android.graphics.Bitmap;

import java.util.List;

public interface Classifier {
    class Recognition {
        /**
         * A unique identifier for what has been recognized. Specific to the class, not the instance of
         * the object.
         */
        private final String mId;

        /**
         * Display name for the recognition.
         */
        private final String mTitle;

        /**
         * Whether or not the model features quantized or float weights.
         */
        private final boolean mQuantization;

        /**
         * A sortable score for how good the recognition is relative to others. Higher should be better.
         */
        private final Float confidence;

        Recognition(
                final String id, final String title, final Float confidence, final boolean quantization) {
            this.mId = id;
            this.mTitle = title;
            this.confidence = confidence;
            this.mQuantization = quantization;
        }

        public String getTitle() {
            return mTitle;
        }

        public Float getConfidence() {
            return confidence;
        }

        @Override
        public String toString() {
            String resultString = "";
//            if (mTitle != null) {
//                resultString += mTitle + " ";
//            }

            if (confidence != null) {
                resultString += confidence * 100.0f;
            }

            return resultString;
        }

        public float toFloat() {
            float result = 0;
            if (confidence != null) {
                result = confidence * 100.0f;
            }
            return result;
        }

    }


    List<Recognition> recognizeImage(Bitmap bitmap);

    void close();
}
