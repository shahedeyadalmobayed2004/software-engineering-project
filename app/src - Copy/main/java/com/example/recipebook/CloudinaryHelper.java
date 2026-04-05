package com.example.recipebook;

import android.content.Context;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryHelper {
    public static void init(Context context) {
        Map config = new HashMap();
        config.put("cloud_name", "drkzod8bq");
        config.put("api_key", "484757436838272");
        config.put("api_secret", "NlytYiWQMRWjM53KyJVVvI1a57s"); // For secure uploads only
        MediaManager.init(context, config);
    }
}
