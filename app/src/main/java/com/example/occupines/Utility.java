package com.example.occupines;

import android.app.Activity;
import android.content.Context;
import android.transition.Fade;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Utility {

    public Utility() {
        //Empty constructor
    }

    public void removeBlinkOnTransition(Activity activity) {
        //Exclude things from transition animation
        Fade fade = new Fade();
        View decor = activity.getWindow().getDecorView();
        fade.excludeTarget(decor.findViewById(R.id.action_bar_container), true);
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);

        activity.getWindow().setEnterTransition(fade);
        activity.getWindow().setExitTransition(fade);
    }

    public void showToast(Context context, String message) {
        Toast.makeText(context, message,
                Toast.LENGTH_SHORT).show();
    }

    public void toggleButton(Button btn) {
        boolean toggle = btn.isEnabled();
        btn.setEnabled(!toggle);
    }

    public boolean checkInputs(String email, String password) {
        return !email.isEmpty() && !password.isEmpty();
    }

    public boolean checkInputs(String name, String email, String password, String rePassword) {
        return !name.isEmpty() && !email.isEmpty() && !password.isEmpty() && !rePassword.isEmpty();
    }

    public boolean checkInputs(String price, String location, String info) {
        return !price.isEmpty() && !location.isEmpty() && !info.isEmpty();
    }

    public boolean doesMatch(String password, String rePassword) {
        return password.equals(rePassword);
    }

}
