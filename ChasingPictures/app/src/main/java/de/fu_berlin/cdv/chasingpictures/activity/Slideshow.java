package de.fu_berlin.cdv.chasingpictures.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.io.Serializable;
import java.util.List;
import de.fu_berlin.cdv.chasingpictures.R;
import de.fu_berlin.cdv.chasingpictures.api.Picture;

public class Slideshow extends Activity {

    private static final String PICTURES_EXTRA = "de.fu_berlin.cdv.chasingpictures.EXTRA_PICTURES";

    public static Intent createIntent(Context context, List<Picture> pictures) {
        Intent intent = new Intent(context, Slideshow.class);
        intent.putExtra(PICTURES_EXTRA, (Serializable) pictures);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slideshow);
    }
}
