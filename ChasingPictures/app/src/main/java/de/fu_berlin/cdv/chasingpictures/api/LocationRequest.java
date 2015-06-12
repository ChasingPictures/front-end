package de.fu_berlin.cdv.chasingpictures.api;

import android.content.Context;
import android.location.Location;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import de.fu_berlin.cdv.chasingpictures.R;
import de.fu_berlin.cdv.chasingpictures.security.Access;

/**
 * @author Simon Kalt
 */
public class LocationRequest extends ApiRequest<PlacesApiResult> {

    private final Location location;

    public LocationRequest(Context context, Location location) {
        super(context, R.string.api_path_location_request);
        this.location = location;
    }

    @Override
    public ResponseEntity<PlacesApiResult> send() {
        HttpHeaders headers = new HttpHeaders();
        Access.getAccess(context, headers);
        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);

        Map<String, Double> queryParameters = new HashMap<>(2);
        queryParameters.put("latitude", location.getLatitude());
        queryParameters.put("longitude", location.getLongitude());

        ResponseEntity<PlacesApiResult> responseEntity = restTemplate.exchange(
                apiUri,
                HttpMethod.GET,
                httpEntity,
                PlacesApiResult.class,
                queryParameters);
        Access.setAccess(context, responseEntity);
        return responseEntity;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }
}
