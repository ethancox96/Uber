package com.parse.starter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class RequestsList extends AppCompatActivity {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private ListView requests;
    private ArrayList<String> requestList = new ArrayList<String>();
    private ArrayList<ParseGeoPoint> locations = new ArrayList<ParseGeoPoint>();
    private ArrayList<String> names = new ArrayList<String>();
    private ArrayAdapter<String> requestAdapter;
    private Location lastKnownLocation;
    private ParseGeoPoint userLocation;

    public void displayList(final Location location) {

        userLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        Log.i("User location", userLocation.toString());
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
        query.whereNear("location", userLocation);
        query.whereDoesNotExist("driverUsername");
        query.whereEqualTo("status", "Open");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects.size() > 0) {
                    requestList.clear();
                    locations.clear();
                    names.clear();
                    for (ParseObject object : objects) {
                        locations.add((ParseGeoPoint) object.get("location"));
                        names.add((String) object.get("name"));
                        Double distanceInMiles = userLocation.distanceInMilesTo((ParseGeoPoint) object.get("location"));
                        Double distanceOneDP = (double) Math.round((distanceInMiles * 10) / 10);
                        Log.i("Distance", distanceOneDP.toString() + " miles");
                        requestList.add(distanceOneDP.toString() + " miles");
                    }
                    requestAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests_list);

        setTitle("Nearby Requests");

        requests = (ListView) findViewById(R.id.requstList);
        requestAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, requestList);
        requestList.clear();
        requestList.add("Getting nearby requests");
        requests.setAdapter(requestAdapter);
        requests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), DriverView.class);
                String[] latlng = locations.get(i).toString().split(",");
                String latitude = latlng[0].split("\\[")[1];
                String longitude = latlng[1].split("\\]")[0];
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("name", names.get(i));
                intent.putExtra("driverLatitude", Double.toString(userLocation.getLatitude()));
                intent.putExtra("driverLongitude", Double.toString(userLocation.getLongitude()));
                startActivity(intent);
            }
        });

        // Get the user's location
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                ParseUser.getCurrentUser().put("location", new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
                ParseUser.getCurrentUser().saveInBackground();

                displayList(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        // if device is running SDK < 23
        if (Build.VERSION.SDK_INT < 23) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        } else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            } else {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                lastKnownLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
                ParseUser.getCurrentUser().put("location", new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
                ParseUser.getCurrentUser().saveInBackground();

                displayList(lastKnownLocation);
            }
        }

    }
}





