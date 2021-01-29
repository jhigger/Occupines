package com.example.occupines;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import id.zelory.compressor.Compressor;

public class Utility {

    public static Uri compressImage(Context context, Uri imagePath) {
        //Getting imageUri and store in file. and compress to using compression library
        File filesDir = context.getFilesDir();
        File imageFile = new File(filesDir, "profile.jpg");

        File compressedImage = null;
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imagePath);
            OutputStream os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.WEBP, 100, os);
            os.flush();
            os.close();

            compressedImage = new Compressor(context)
                    .setMaxWidth(250)
                    .setMaxHeight(250)
                    .setQuality(75)
                    .setCompressFormat(Bitmap.CompressFormat.WEBP)
                    .compressToFile(imageFile, "compressedImage");
        } catch (Exception e) {
            Log.e(context.getClass().getSimpleName(), "Error writing bitmap", e);
        }

        return Uri.fromFile(compressedImage);
    }

    public static void removeBlinkOnTransition(Activity activity) {
        //Exclude things from transition animation
        Fade fade = new Fade();
        View decor = activity.getWindow().getDecorView();
        fade.excludeTarget(decor.findViewById(R.id.action_bar_container), true);
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);

        activity.getWindow().setEnterTransition(fade);
        activity.getWindow().setExitTransition(fade);
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message,
                Toast.LENGTH_SHORT).show();
    }

    public static void toggleButton(Button btn) {
        boolean toggle = btn.isEnabled();
        btn.setEnabled(!toggle);
    }

    public static boolean checkInputs(String email, String password) {
        return !email.isEmpty() && !password.isEmpty();
    }

    public static boolean checkInputs(String name, String email, String password, String rePassword) {
        return !name.isEmpty() && !email.isEmpty() && !password.isEmpty() && !rePassword.isEmpty();
    }

    public static boolean checkInputs(String price, String location, String info) {
        return !price.isEmpty() && !location.isEmpty() && !info.isEmpty();
    }

    public static boolean doesMatch(String password, String rePassword) {
        return password.equals(rePassword);
    }

}
