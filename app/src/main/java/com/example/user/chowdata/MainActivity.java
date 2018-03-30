package com.example.user.chowdata;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
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
    @BindView(R.id.photo_library) ImageView photo_library;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int PICK_FROM_GALLERY = 2;
    private static final int REQUEST_EXTERNAL_STORAGE = 3;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
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
        verifyStoragePermissions(this);
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
                snackBarView.setBackgroundColor(getColor(R.color.success));
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

        photo_library.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent  = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, PICK_FROM_GALLERY);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        for(int i =0; i< menu.size(); i++){
            MenuItem item = menu.getItem(i);
            SpannableString spannableString = new SpannableString(menu.getItem(i).getTitle().toString());
            spannableString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, spannableString.length(), 0);
            item.setTitle(spannableString);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.about:
                new LibsBuilder()
                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                        .withAboutAppName(getString(R.string.app_name))
                        .withAboutIconShown(true)
                        .withAboutVersionShown(true)
                        .withAboutDescription(getString(R.string.description))
                        .withAutoDetect(true)
                        .start(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
            Snackbar snackbar = Snackbar.make(coordinatorLayout,
                    "Add food by choosing from the dropdown when you start typing the food",
                    Snackbar.LENGTH_LONG);
            View snackBarView = snackbar.getView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                snackBarView.setBackgroundColor(getColor(R.color.colorAccent));
            }
            snackbar.show();
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

    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
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
        else if (requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            Glide.with(getApplicationContext())
                    .load(selectedImage)
                    .centerCrop()
                    .into(foodImage);
            String tempPath = Util.getRealPath(this, selectedImage);
            currentPhotoPath = Util.compressImage(getApplicationContext(), tempPath);
            submitButton.setEnabled(true);
            cancelButton.setEnabled(true);
        }
    }
}
