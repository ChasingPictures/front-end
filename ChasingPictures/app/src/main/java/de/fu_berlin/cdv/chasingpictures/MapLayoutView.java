package de.fu_berlin.cdv.chasingpictures;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.GpsLocationProvider;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MapboxTileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.views.MapView;

import de.fu_berlin.cdv.chasingpictures.util.Utilities;

/**
 * @author Simon Kalt
 */
public class MapLayoutView {

    @NonNull
    private final Activity context;
    @NonNull
    private final MapView mapView;
    @NonNull
    private final String mapId;
    @Nullable
    private UserLocationOverlay locationOverlay;

    private boolean initDone;
    private boolean overlayConfigured;
    private boolean trackingStarted;

    public MapLayoutView(@NonNull Activity context, @NonNull MapView mapView, @NonNull String mapId, @Nullable UserLocationOverlay locationOverlay) {
        this.context = context;
        this.mapView = mapView;
        this.mapId = mapId;
        this.locationOverlay = locationOverlay;
    }

    public MapLayoutView(@NonNull Activity context, @NonNull MapView mapView, @NonNull String mapId, boolean useDefaultOverlay) {
        this.context = context;
        this.mapView = mapView;
        this.mapId = mapId;
        if (useDefaultOverlay) {
            this.locationOverlay = new MyUserLocationOverlay(mapView, context);
        }
    }

    /**
     * Init this view.
     *
     * @return This object for method chaining.
     */
    public MapLayoutView init() {
        replaceMapView(mapId);
        initDone = true;
        return this;
    }

    /**
     * Display Mapbox attribution links.
     * See the <a href="https://www.mapbox.com/help/attribution/">Mapbox help</a> for details.
     */
    private void displayAttribution() {
        displayAttribution(false);
    }

    /**
     * Display Mapbox attribution links.
     * See the <a href="https://www.mapbox.com/help/attribution/">Mapbox help</a> for details.
     *
     * @param attributionViewAdded Whether the view containing the attribution has already been added
     */
    private void displayAttribution(boolean attributionViewAdded) {
        TextView attribution;

        attribution = (TextView) context.findViewById(R.id.mapbox_attribution);

        // If the mapbox attribution text exists in the layout, use it
        if (attribution != null) {
            Utilities.setLinkifiedText(context, attribution, R.string.mapbox_attribution_links, false);
        } else {
            // Otherwise, try to add it
            //noinspection StatementWithEmptyBody
            if (!attributionViewAdded) {
                ViewGroup contentView = (ViewGroup) mapView.getParent();
                context.getLayoutInflater()
                        .inflate(R.layout.mapbox_attribution_layout, contentView, true);
                displayAttribution(true);
            } else {
                // Adding of attribution layout failed
            }
        }
    }

    /**
     * Configures the location overlay with the default options.
     * If the map view is not yet initialized, it will be done.
     *
     * @return This object for method chaining.
     */
    private MapLayoutView configureOverlay() {
        if (!initDone)
            init();

        if (locationOverlay != null) {
            locationOverlay.setTrackingMode(UserLocationOverlay.TrackingMode.FOLLOW);
            locationOverlay.setRequiredZoom(18);
            locationOverlay.enableMyLocation();
            locationOverlay.setDrawAccuracyEnabled(false);
        }

        overlayConfigured = true;

        return this;
    }

    /**
     * Starts tracking the user, if the overlay is not configured it will automatically be configured.
     *
     * @return This object for method chaining.
     */
    public MapLayoutView startTracking() {
        if (!overlayConfigured)
            configureOverlay();

        if (locationOverlay != null) {
            mapView.addOverlay(locationOverlay);
            locationOverlay.getMyLocationProvider().startLocationProvider(locationOverlay);
        }

        trackingStarted = true;

        return this;
    }


    protected void replaceMapView(String layer) {
        ITileLayer source;
        BoundingBox box;
        if (layer.equalsIgnoreCase("OpenStreetMap")) {
            source = new WebSourceTileLayer("openstreetmap",
                    "http://tile.openstreetmap.org/{z}/{x}/{y}.png").setName("OpenStreetMap")
                    .setAttribution("© OpenStreetMap Contributors")
                    .setMinimumZoomLevel(1)
                    .setMaximumZoomLevel(18);
        } else if (layer.equalsIgnoreCase("OpenSeaMap")) {
            source = new WebSourceTileLayer("openstreetmap",
                    "http://tile.openstreetmap.org/seamark/{z}/{x}/{y}.png").setName(
                    "OpenStreetMap")
                    .setAttribution("© OpenStreetMap Contributors")
                    .setMinimumZoomLevel(1)
                    .setMaximumZoomLevel(18);
        } else if (layer.equalsIgnoreCase("mapquest")) {
            source = new WebSourceTileLayer("mapquest",
                    "http://otile1.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png").setName(
                    "MapQuest Open Aerial")
                    .setAttribution(
                            "Tiles courtesy of MapQuest and OpenStreetMap contributors.")
                    .setMinimumZoomLevel(1)
                    .setMaximumZoomLevel(18);
        } else {
            source = new MapboxTileLayer(layer);
        }
        mapView.setTileSource(source);
        box = source.getBoundingBox();
        mapView.setScrollableAreaLimit(box);
        mapView.setMinZoomLevel(mapView.getTileProvider().getMinimumZoomLevel());
        mapView.setMaxZoomLevel(mapView.getTileProvider().getMaximumZoomLevel());
        mapView.setCenter(new LatLng(Menu.BERLIN));
        mapView.setZoom(10);
        displayAttribution();
    }

    public static class MyUserLocationOverlay extends UserLocationOverlay {

        public MyUserLocationOverlay(MapView mapView, GpsLocationProvider myLocationProvider, @DrawableRes int arrowId, @DrawableRes int personId) {
            super(myLocationProvider, mapView, arrowId, personId);
        }

        public MyUserLocationOverlay(MapView mapView, GpsLocationProvider myLocationProvider) {
            super(myLocationProvider, mapView, R.drawable.user_location_pointer, R.drawable.user_location);
        }

        public MyUserLocationOverlay(MapView mapView, Context context) {
            super(new GpsLocationProvider(context), mapView, R.drawable.user_location_pointer, R.drawable.user_location);
        }
    }
}
