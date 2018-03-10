package com.example.user.chowdata;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.policy.GlobalUploadPolicy;
import com.cloudinary.android.policy.UploadPolicy;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.ChipInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.chips_input) ChipsInput chipsInput;
    @BindView(R.id.food_image) ImageView foodImage;
    @BindView(R.id.submit) ImageView submitButton;
    @BindView(R.id.progressBarHolder) FrameLayout progressBarHolder;
    @BindView(R.id.uploading) TextView uploading;
    @BindView(R.id.cancel) ImageView cancelButton;
    @BindView(R.id.coordinator_layout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.parent) LinearLayout parentLayout;
    static final int REQUEST_TAKE_PHOTO = 1;
    ArrayList<String> chows;
    Uri photoPath;
    String currentPhotoPath;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Timber.plant(new Timber.DebugTree());
        submitButton.setEnabled(false);
        cancelButton.setEnabled(false);
        chows = new ArrayList<String>();
        try {
            readFile("Foods.txt");
        } catch (IOException e) {
            Timber.d("Error reading file");
            e.printStackTrace();
        }

        String message = getIntent().getStringExtra("message");
        if(message != null){
            Snackbar snackbar = Snackbar.make(coordinatorLayout, message,
                    Snackbar.LENGTH_SHORT);
            View snackBarView = snackbar.getView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                snackBarView.setBackgroundColor(getColor(R.color.colorAccent));
            }
            snackbar.show();
        }
        message = null;

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();
            }
        });

        foodImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(Util.takePicture(getApplicationContext()), REQUEST_TAKE_PHOTO);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelFile();
            }
        });


        List<FoodChip> chipList = new ArrayList<>();

        for (String chow: chows){
            chipList.add(new FoodChip(chow));
        }

        chipsInput.setFilterableList(chipList);

        chipsInput.addChipsListener(new ChipsInput.ChipsListener() {
            @Override
            public void onChipAdded(ChipInterface chipInterface, int i) {
            }

            @Override
            public void onChipRemoved(ChipInterface chipInterface, int i) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence) {

            }
        });
    }

    public void cancelFile(){
        if (currentPhotoPath != null) {
            File photofile = new File(currentPhotoPath);
            if(photofile.exists()){
                photofile.delete();
            }
        }
        foodImage.setImageResource(R.drawable.ic_camera_alt);
        overridePendingTransition(0, 0);
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.putExtra("message", message);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    public void uploadFile(){
        List<FoodChip> foodSelected = (List<FoodChip>) chipsInput.getSelectedChipList();
        if(foodSelected.isEmpty()){
            Snackbar.make(coordinatorLayout,
                    "Add food by choosing from the dropdown when you start typing the food",
                    Snackbar.LENGTH_LONG).show();
        } else {
            submitButton.setEnabled(false);
            inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            uploading.setText("Uploading foods.......");
            progressBarHolder.setAnimation(inAnimation);
            progressBarHolder.setVisibility(View.VISIBLE);
            parentLayout.setVisibility(View.GONE);
        }
        for(final FoodChip food: foodSelected){
            String requestId = MediaManager.get().upload(currentPhotoPath)
                    .unsigned("c1kmzm6x")
                    .option("folder", food.getLabel() + "/")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            submitButton.setEnabled(false);
                            inAnimation = new AlphaAnimation(0f, 1f);
                            inAnimation.setDuration(200);
                            uploading.setText("Uploading foods.......");
                            progressBarHolder.setAnimation(inAnimation);
                            progressBarHolder.setVisibility(View.VISIBLE);
                            parentLayout.setVisibility(View.GONE);
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {

                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            outAnimation = new AlphaAnimation(1f, 0f);
                            outAnimation.setDuration(200);
                            progressBarHolder.setAnimation(outAnimation);
                            progressBarHolder.setVisibility(View.GONE);
                            submitButton.setEnabled(false);
//                            Snackbar.make(coordinatorLayout, "Foods successfully uploaded",
//                                    Snackbar.LENGTH_SHORT).show();
                            message = "Foods successfully uploaded";
                            parentLayout.setVisibility(View.VISIBLE);
                            cancelFile();
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            outAnimation = new AlphaAnimation(1f, 0f);
                            outAnimation.setDuration(200);
                            progressBarHolder.setAnimation(outAnimation);
                            progressBarHolder.setVisibility(View.GONE);
                            submitButton.setEnabled(false);
                            parentLayout.setVisibility(View.VISIBLE);
                            cancelFile();
//                            Snackbar.make(coordinatorLayout, "You probably don't have internet connection",
//                                    Snackbar.LENGTH_SHORT).show();
                            message = "You probably don't have internet connection";
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {

                        }
                    })
                    .dispatch();
        }

    }

    private void readFile(String filename) throws IOException {
        InputStream inputStream = getAssets().open(filename);
        if (inputStream != null) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null){
                chows.add(line);
            }
        }

        inputStream.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            photoPath = Uri.parse(Util.getCurrentPhotoPath());
            String tempPhotoPath = Util.getCurrentPhotoPath();
            Glide.with(getApplicationContext())
                    .load(Util.getCurrentPhotoPath())
                    .centerCrop()
                    .into(foodImage);
            currentPhotoPath = Util.compressImage(getApplicationContext(), tempPhotoPath);
            if (tempPhotoPath != null) {
                File photofile = new File(tempPhotoPath);
                if(photofile.exists()){
                    photofile.delete();
                }
            }
            submitButton.setEnabled(true);
            cancelButton.setEnabled(true);
        }
    }
}
