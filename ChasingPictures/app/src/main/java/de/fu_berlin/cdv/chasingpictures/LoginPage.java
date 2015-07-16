package de.fu_berlin.cdv.chasingpictures;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


/**
 * This activity presents a page where the user can choose to
 * either log in or register a new account.
 *
 * @author Simon Kalt
 */
public class LoginPage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
    }

    /**
     * On tapping the log in button, go to the log in form.
     */
    public void showLoginForm(View view) {
        Intent intent = new Intent(this, LoginForm.class);
        startActivityForResult(intent, Menu.REQUEST_LOGIN_REGISTER);
    }

    /**
     * On tapping the register button, go to the registration form.
     */
    public void showRegisterForm(View view) {
        Intent intent = new Intent(this, Register.class);
        startActivityForResult(intent, Menu.REQUEST_LOGIN_REGISTER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the sub-activity says that log in/registration succeeded, pass on that result
        if (resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }

        // Otherwise, stay here and let the user decide what to do
    }
}
