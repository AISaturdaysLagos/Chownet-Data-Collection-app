package com.example.user.chowdata;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.policy.GlobalUploadPolicy;
import com.cloudinary.android.policy.UploadPolicy;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.ChipInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.chips_input) ChipsInput chipsInput;
    @BindView(R.id.food_image) ImageView foodImage;
    @BindView(R.id.submit) Button submitButton;
    static final int REQUEST_TAKE_PHOTO = 1;
    ArrayList<String> chows;
    Uri photoPath;
    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MediaManager.init(this);
        ButterKnife.bind(this);
        Timber.plant(new Timber.DebugTree());
        if(savedInstanceState != null && currentPhotoPath != null){
            Glide.with(getApplicationContext())
                    .load(currentPhotoPath)
                    .centerCrop()
                    .into(foodImage);
        }
        chows = new ArrayList<String>();
        try {
            readFile("Foods.txt");
        } catch (IOException e) {
            Timber.d("Error reading file");
            e.printStackTrace();
        }

        foodImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(Util.takePicture(getApplicationContext()), REQUEST_TAKE_PHOTO);
            }
        });



        // configure global policy for cloudinary.
        MediaManager.get().setGlobalUploadPolicy(
                new GlobalUploadPolicy.Builder()
                        .maxConcurrentRequests(4)
                        .networkPolicy(UploadPolicy.NetworkType.ANY)
                        .build());


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
            currentPhotoPath = Util.getCurrentPhotoPath();
            Glide.with(getApplicationContext())
                    .load(Util.getCurrentPhotoPath())
                    .centerCrop()
                    .into(foodImage);
        }
    }
}
