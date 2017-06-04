package com.UTS.locaTO;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.net.Uri;
import android.content.Intent;

import com.UTS.locaTO.APIs.Eventbrite;
import com.UTS.locaTO.APIs.Reddit;
import com.UTS.locaTO.Adapters.EventsAdapter;

import java.util.ArrayList;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity implements EventsAdapter.IZoneClick {

    private Database database;
    private OkHttpClient client;
    private Reddit reddit;
    private Eventbrite eventbrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.database = new Database();
        this.client = new OkHttpClient();
        this.reddit = new Reddit(this);
        this.eventbrite = new Eventbrite(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                reddit.execute();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.reddit.execute();
        this.getLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public OkHttpClient getClient() {
        return this.client;
    }

    public void onNewEvents(ArrayList<Event> events) {
        if (events != null) {
            for (Event event : events) {
                this.database.addEvent(event);
            }
        }
    }

    //github.com/marceloneil/MinoTour
    public void zoneClick(Event model) {
        Uri location = Uri.parse("https://maps.google.com/maps?daddr=" + Uri.encode(model.getEventLocation()) + "(" + Uri.encode(model.getEventName()) + ")");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }

        /*Intent myIntent = new Intent(MainActivity.this, expand_card.class);
        myIntent.putExtra("query_name", model.name);
        myIntent.putExtra("query_address", model.vicinity);
        if (model.rating != null) {
            myIntent.putExtra("query_rating", model.rating.toString());
        } else {
            myIntent.putExtra("query_rating", "No Rating");
        }
        myIntent.putExtra("query_distance", model.distance.text);
        Integer price_int = model.price_level;
        String price_string;
        if (price_int == null) {
            price_string = "Not Available";
        } else if (price_int == 0) {
            price_string = "Free";
        } else if (price_int == 1) {
            price_string = "Inexpensive";
        } else if (price_int == 2) {
            price_string = "Moderate";
        } else if (price_int == 3) {
            price_string = "Expensive";
        } else {
            price_string = "Very Expensive";
        }
        myIntent.putExtra("query_price", price_string);
        if (model.photoUrl != null) {
            myIntent.putExtra("query_image", model.photoUrl.replaceAll("\\\\u0026", "&").replaceAll("\\\\u003d", "="));
        }
        MainActivity.this.startActivity(myIntent);*/
    }

    public void getLocation() {
        // Check for permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // Get the location manager
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Find best provider
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider == null) {
            provider = "gps";
        }

        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null) {
            Log.i("Location", "Provider " + provider + " has been selected.");
            this.onLocationChanged(location);
        } else {
            if (provider.equals("gps")) {
                provider = "network";
            } else {
                provider = "gps";
            }
            location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                this.onLocationChanged(location);
            } else {
                Log.i("Location", "Location not available");
            }
        }
    }

    public void onLocationChanged(Location location) {
        double lat = (location.getLatitude());
        double lng = (location.getLongitude());
        Log.i("Location", "Lat: " + lat + ", Lng: " + lng);
        this.eventbrite.execute(lat, lng);
    }

}
