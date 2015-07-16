package de.fu_berlin.cdv.chasingpictures.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;

import de.fu_berlin.cdv.chasingpictures.LoginPage;
import de.fu_berlin.cdv.chasingpictures.Menu;
import de.fu_berlin.cdv.chasingpictures.R;
import de.fu_berlin.cdv.chasingpictures.security.Access;
import de.fu_berlin.cdv.chasingpictures.util.Utilities;

public class SplashScreen extends Activity {

    private static final long ANIMATION_DURATION = 3202;
    private final Thread splashThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                synchronized (splashThread) {
                    splashThread.wait(ANIMATION_DURATION);
                }
            } catch (InterruptedException ignored) {
            }

            if (!Access.hasAccess(getApplicationContext())) {
                Intent intent = new Intent(getApplicationContext(), LoginPage.class);
                startActivityForResult(intent, Menu.REQUEST_LOGIN_REGISTER);
            } else {
                onActivityResult(Menu.REQUEST_LOGIN_REGISTER, RESULT_OK, null);
            }
        }
    });
    private AnimationDrawable draw;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Menu.REQUEST_LOGIN_REGISTER && resultCode == RESULT_OK) {
            finish();
            Intent intent = new Intent(getApplicationContext(), Menu.class);
            startActivity(intent);
        } else {
            Utilities.showError(this, R.string.login_fail);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ImageView imageView = (ImageView) findViewById(R.id.splashScreenImageView);
        draw = (AnimationDrawable) imageView.getBackground();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            draw.start();
            splashThread.start();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            synchronized (splashThread) {
                splashThread.notifyAll();
            }
            return true;
        }
        return false;
    }
}
