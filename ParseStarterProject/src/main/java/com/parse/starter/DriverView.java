package com.parse.starter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class DriverView extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng riderLocation;
    private LatLng userLocation;
    private String name;

    public void acceptRequest(View view) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
        query.whereEqualTo("name", name);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null && object != null) {
                    object.put("status", "Accepted");
                    object.put("driverUsername", ParseUser.getCurrentUser().getUsername());
                    object.saveInBackground();
                    Intent directionsIntent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?saddr=" + userLocation.latitude + "," + userLocation.longitude
                                    + "&daddr=" + riderLocation.latitude + "," + riderLocation.longitude));
                    startActivity(directionsIntent);
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_view);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        String riderLat = intent.getStringExtra("latitude");
        String riderLon = intent.getStringExtra("longitude");
        String driverLat = intent.getStringExtra("driverLatitude");
        String driverLon = intent.getStringExtra("driverLongitude");
        name = intent.getStringExtra("name");
        riderLocation = new LatLng(Double.parseDouble(riderLat), Double.parseDouble(riderLon));
        userLocation = new LatLng(Double.parseDouble(driverLat), Double.parseDouble(driverLon));
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
        mMap.addMarker(new MarkerOptions().position(riderLocation).title("Rider Location").icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(userLocation);
        builder.include(riderLocation);
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 100);
        googleMap.moveCamera(cu);
    }
}




