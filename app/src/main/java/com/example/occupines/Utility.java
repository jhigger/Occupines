package com.example.occupines;

import android.content.Context;
import android.widget.Button;
import android.widget.Toast;

public class Utility {

    public Utility() {
        //Empty constructor
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

    public boolean matchPassword(String password, String rePassword) {
        return password.equals(rePassword);
    }

}
