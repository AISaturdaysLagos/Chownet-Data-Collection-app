package com.example.user.chowdata;

import android.app.Application;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.policy.GlobalUploadPolicy;
import com.cloudinary.android.policy.UploadPolicy;

/**
 * Created by Femi on 10/03/2018.
 */

public class MainApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        MediaManager.init(this);
        // configure global policy for cloudinary.
        MediaManager.get().setGlobalUploadPolicy(
                new GlobalUploadPolicy.Builder()
                        .maxConcurrentRequests(4)
                        .networkPolicy(UploadPolicy.NetworkType.ANY)
                        .build());

    }
}
