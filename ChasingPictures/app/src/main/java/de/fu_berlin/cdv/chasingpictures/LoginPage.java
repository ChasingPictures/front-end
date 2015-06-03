package de.fu_berlin.cdv.chasingpictures;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import de.fu_berlin.cdv.chasingpictures.api.UserData;


public class LoginPage extends Activity {

    public static final int LOGIN = 1;
    public static final int REGISTER = 2;
    public static final String TAG = "LoginPage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login_page, menu);
        return true;
    }

    public void showLoginForm(View view) {
        Intent intent = new Intent(this, LoginForm.class);
        startActivityForResult(intent, LOGIN);
    }

    public void showRegisterForm(View view) {
        Intent intent = new Intent(this, Register.class);
        startActivityForResult(intent, REGISTER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // TODO: Depending on whether the user logged in or registered, do something with that information...
            switch (requestCode) {
                case LOGIN:
                    UserData userData = data.getParcelableExtra(LoginForm.RETURN_USER_DATA);
                    Log.d(TAG, "Logged in successfully with following user data:");
                    Log.d(TAG, userData.toString());
                    break;
                case REGISTER:
                    Log.d(TAG, "Registered successfully!");
                    break;
            }

            // TODO: Continue to logged-in status
        } else {
            Log.d(TAG, "Log in/Register result not OK!");
            // Do nothing
        }
    }
}
