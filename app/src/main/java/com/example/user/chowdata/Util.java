package com.example.user.chowdata;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Femi on 07/03/2018.
 */

public class Util {
    static String mCurrentPhotoPath;


    public static Intent takePicture(Context context){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null){
            File photoFile = null;
            try{
                photoFile = createImageFile(context);
            } catch (IOException e) {
                Toast.makeText(context, context.getString(R.string.file_not_created), Toast.LENGTH_SHORT).show();
            }

            if(photoFile != null ){
                Uri photoURI = FileProvider.getUriForFile(context,
                        "com.example.android.foodfileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

            }
        }
        return takePictureIntent;
    }

    public static String getCurrentPhotoPath(){
        return mCurrentPhotoPath;
    }

    private static File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
