package de.fu_berlin.cdv.chasingpictures.activity;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import de.fu_berlin.cdv.chasingpictures.api.Place;

/**
 * @author Simon Kalt
 */
public class SlideshowTest extends ActivityInstrumentationTestCase2<Slideshow> {

    public SlideshowTest() {
        super(Slideshow.class);
    }

    public void setUp() throws Exception {
        super.setUp();
        Place place = new Place();
        place.setId(6);

        // Set up activity intent
        Intent intent = Slideshow.createIntent(getInstrumentation().getContext(), place);
        setActivityIntent(intent);
    }
}
