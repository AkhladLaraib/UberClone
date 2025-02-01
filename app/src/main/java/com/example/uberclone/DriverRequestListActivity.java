package com.example.uberclone;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;

public class DriverRequestListActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnGetNearbyRequests;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private ListView listView;
    private ArrayList<String> nearByDriveRequests;
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request_list);

        Toolbar toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);

        btnGetNearbyRequests = findViewById(R.id.btnGetNearbyRequests);
        btnGetNearbyRequests.setOnClickListener(DriverRequestListActivity.this);

        listView = findViewById(R.id.requestListView);

        nearByDriveRequests = new ArrayList<>();
        adapter = new ArrayAdapter(DriverRequestListActivity.this,
                android.R.layout.simple_list_item_1, nearByDriveRequests);

        listView.setAdapter(adapter);

        nearByDriveRequests.clear();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        0, 0, locationListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_driver, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.driverLogoutItem) {

            ParseUser.logOutInBackground(e -> {

                if (e == null) {
                    finish();
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        locationListener = this::updateRequestListView;

        if (Build.VERSION.SDK_INT < 23){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, locationListener);

        } else if (Build.VERSION.SDK_INT >= 23) {

            if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(DriverRequestListActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
            } else {

//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                        0, 0, locationListener);

                Location currentDriverLocation =
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestListView(currentDriverLocation);
            }
        }

    }

    private void updateRequestListView(Location driverLocation) {

        if (driverLocation != null) {

            nearByDriveRequests.clear();

            final ParseGeoPoint driverCurrentLocation = new ParseGeoPoint(driverLocation.getLatitude(),
                    driverLocation.getLongitude());
            ParseQuery<ParseObject> requestCarQuery = ParseQuery.getQuery("RequestCar");
            requestCarQuery.whereNear("passengerLocation", driverCurrentLocation);
            requestCarQuery.findInBackground((objects, e) -> {


                if (e == null) {
                    if (!objects.isEmpty()) {

                        for (ParseObject nearObject : objects) {

                            Double milesDistanceToPassenger = driverCurrentLocation
                                    .distanceInMilesTo(nearObject.getParseGeoPoint("passengerLocation"));

                            float roundedDistanceValue = Math.round(milesDistanceToPassenger * 10) / 10;
                            nearByDriveRequests.add("There are " + roundedDistanceValue +
                                    " miles to " + nearObject.get("username"));

                        }

                    } else {

                        FancyToast.makeText(DriverRequestListActivity.this, "No requests found",
                                FancyToast.LENGTH_LONG, FancyToast.INFO, false).show();
                    }
                    adapter.notifyDataSetChanged();

                }
            });


        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        0, 0, locationListener);

//                Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                updateRequestListView(currentDriverLocation);
            }
        }
    }

}