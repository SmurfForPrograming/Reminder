package bajomoj.myapplication;

/**
 * Created by JohnDoe on 12.5.2015..
 */

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

//import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
public class InitLocation extends FragmentActivity implements
      GoogleMap.OnMapLongClickListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, GoogleMap.OnMapClickListener
        //GoogleMap.OnMarkerDragListener,
    // NumberPicker.OnValueChangeListener
{

    private static long INTERVAL = 1000 * 10; //10 sec
    private static long FASTEST_INTERVAL = 1000 * 10; // 10 sec

    private static final String TAG = "LocationActivity";

    // public static int INTERVAL = 1000 * 60;  //10 000 = 10 sec, 20 000 = 20 sec...
    //  public static int FASTEST_INTERVAL = 1000 * 60;
    //public int intervalcic = 10000;
    //public int fastest = 10000;

    Button btnFusedLocation;
    TextView tvLocation;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;
    GoogleMap googleMap;

    Circle mapCircle;

    static double latitude;
    static double longtitude;
    static double r;
    static String loc;


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(0);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //String poruka = "Dosao sam iz Add Activity-a";
        //Toast.makeText(this, poruka, Toast.LENGTH_SHORT).show();
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate ...............................");
        //prikazuje error ako GoolglePlayServices nisu dostupne
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        setContentView(R.layout.init_open_map);
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        googleMap = fm.getMap();
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        googleMap.setMyLocationEnabled(true);

        // dugi klik na mapu za brisanje svih postavljenih markera
        googleMap.setOnMapLongClickListener(this);

        // jedan klik na mapu za dodavanje markera
        googleMap.setOnMapClickListener(this);
        //kraj OnMapClick Listenera

        googleMap.setMyLocationEnabled(true);

    } // kraj ONCREATE



    @Override
    public void onMapClick(LatLng latLng) {
        // kreiranje markera
        MarkerOptions markerOptions = new MarkerOptions();
        // pozicioniranje markera
        markerOptions.position(latLng);
        // on klik markera, prikazuje se latitude/longitude
        markerOptions.title(latLng.latitude + " / " + latLng.longitude);
        // brisi prethodno stavljeni marker
        googleMap.clear();
        // stavi kameru na odabranu poziciju markera
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

        final Marker mapMarker = googleMap.addMarker(markerOptions);
        //googleMap.addMarker(mapMarker);
        //mapCircle.setCenter(new LatLng(latLng.latitude, latLng.longitude));
        mapCircle = googleMap.addCircle(new CircleOptions()
                        .center(new LatLng(latLng.latitude, latLng.longitude))
                        .radius(60)
        );
    } // kraj void onMapClick


    // ovo je metoda za pretvaranje upisane adrese u koordinate (latitude i longitude)
    public void geoLocate(View v) throws IOException {
        hideSoftKeyboard(v);

        googleMap.clear();

        EditText et = (EditText) findViewById(R.id.editText1);
        String location = et.getText().toString();

        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(location, 1);

        Toast.makeText(getApplicationContext(), "radi", Toast.LENGTH_SHORT).show();
        //ako nije pronasao adresu, odnosno unesenu lokaciju
        if (list != null && list.size() > 0) {
            Address add = list.get(0); //daj mi prvi i jedini item u listi
            String locality = add.getLocality();

            Toast.makeText(this, locality, Toast.LENGTH_SHORT).show();

            double lat = add.getLatitude();
            double lng = add.getLongitude();
            latitude = lat;
            longtitude = lng;
            r = 60;
            loc = location;


            //listData object = new listData()

            LatLng ll = new LatLng(lat, lng);  //dobivena lokacija iz adrese
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 18);
            googleMap.moveCamera(update);

            MarkerOptions options = new MarkerOptions();
            options.position(ll);
            options.title(String.valueOf(ll));
            //options.draggable(true);
            googleMap.addMarker(options);

            mapCircle = googleMap.addCircle(new CircleOptions()
                            .center(new LatLng(ll.latitude, ll.longitude))
                            .radius(60)
            );
        } else {
            Toast.makeText(this, "Lokacija nije pronadena.", Toast.LENGTH_SHORT).show();
        }
    }

    //nakon sto se unese adresa, tipkovnica se uklanja s ekrana...
    private void hideSoftKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    //pocisti sve markere s mape
    @Override
    public void onMapLongClick(LatLng arg0) {
        googleMap.clear();
        mapCircle = null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        //startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    public void onSpremiClick(View V) {
        Intent AddActivityIntent = new Intent(InitLocation.this, AddActivity.class);
        AddActivityIntent.putExtra("lat", latitude);
        AddActivityIntent.putExtra("lng", longtitude);
        AddActivityIntent.putExtra("radius", r);
        AddActivityIntent.putExtra("location", loc);
        setResult(RESULT_OK, AddActivityIntent);
        finish();
    }

}