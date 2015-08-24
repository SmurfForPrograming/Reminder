package bajomoj.myapplication;

/**
 * Created by JohnDoe on 12.5.2015..
 */

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import android.view.View.OnClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
//import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;


import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class MainMap extends FragmentActivity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapLongClickListener
        //GoogleMap.OnMarkerDragListener,
        // NumberPicker.OnValueChangeListener
{
    private static long INTERVAL = 1000 * 10; //10 sec
    private static long FASTEST_INTERVAL = 1000 * 10 ; // 10 sec

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
    int carMode = 1;  //prilikom pokretanja aplikacije CarMode je ukljucen, tj. cesto dobiva update lokacije

    static double sirina = 0;
    static double duljina = 0;



    protected void createLocationRequest() {
        //if (carMode != 0)
        //mLocationRequest.setSmallestDisplacement(10);
        // else
        // mLocationRequest.setSmallestDisplacement(10);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(0);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        setContentView(R.layout.main_map);
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        googleMap = fm.getMap();
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        googleMap.setMyLocationEnabled(true);

        // dugi klik na mapu za brisanje svih postavljenih markera
        googleMap.setOnMapLongClickListener(this);

        // jedan klik na mapu za dodavanje markera
        googleMap.setOnMapClickListener(new OnMapClickListener() {

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


        }); //kraj OnMapClick Listenera

        sirina = getIntent().getDoubleExtra("latitude", sirina);
        duljina = getIntent().getDoubleExtra("longitude",duljina);
        String msg = String.valueOf(sirina);
        Toast.makeText(getApplicationContext(), "Dobio sam " + sirina, Toast.LENGTH_SHORT).show();
    }

    // metoda za provjeru je li korisnik usao u krug
    public void checkIfHeCame (Location location) {
        if (mapCircle != null) {
            float[] distance = new float[2];

            Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                    mapCircle.getCenter().latitude, mapCircle.getCenter().longitude, distance);

            if (distance[0] > mapCircle.getRadius()) {
                //Log.i(TAG, "IZVAN SI");
                Toast.makeText(getBaseContext(), "Izvan kruga si, udaljenost od centra kruga: " + distance[0] + " radius: " + mapCircle.getRadius(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getBaseContext(), "Unutar kruga si, udaljenost od centra kruga: " + distance[0] + " radius: " + mapCircle.getRadius(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onStart () {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
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

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        addMarker();
        //checkIfHeCame(location);
    }


    private void addMarker() {   //kad te pozove metoda iznad, crtaj markere po mapi
        MarkerOptions options = new MarkerOptions();

        // 4 linije dolje zahtjevaju 'Google Maps Android API Utility Library'
        // https://developers.google.com/maps/documentation/android/utility/
        // koriste se za prikaz vremena kao title od markera
        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setStyle(IconGenerator.STYLE_PURPLE);
        options.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(mLastUpdateTime)));
        options.anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

        //LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        LatLng currentLatLng = new LatLng(sirina, duljina);
        Toast.makeText(getApplicationContext(), "Postavljen pin na " + sirina + "i" + duljina, Toast.LENGTH_SHORT).show();
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18));
        String poruka = "Lokacija: " + currentLatLng;
        //Toast.makeText(this, poruka, Toast.LENGTH_SHORT).show();
        Toast toast= Toast.makeText(getApplicationContext(),
                poruka , Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
        options.position(currentLatLng);
        Marker mapMarker = googleMap.addMarker(options);
        long atTime = mCurrentLocation.getTime();
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date(atTime));
        mapMarker.setTitle(mLastUpdateTime);
        Log.d(TAG, "Marker added.............................");
        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,13));
        Log.d(TAG, "Zoom done.............................");

    }

    public void On_ClickCarMode(View v) {
        long brojMetara = 0;
        //mLocationRequest = new LocationRequest();
        if (carMode == 0) {
            carMode = 1;
            brojMetara = 0;
            mLocationRequest.setSmallestDisplacement(brojMetara);
            startLocationUpdates();  //najbitnije ka!!! zapocni novi update sa promjenom
            Toast.makeText(this, "CarMode ukljucen", Toast.LENGTH_SHORT).show();

        } else if (carMode == 1) {
            carMode = 0;
            brojMetara = 20;
            mLocationRequest.setSmallestDisplacement(brojMetara);    //nemoj azurirati lokaciju za dok se korisnik ne pomakne za brojMetara
            startLocationUpdates();
            Toast.makeText(this, "CarMode iskljucen", Toast.LENGTH_SHORT).show();
        }
    }

    public void On_clickPovecaj(View v) {
        long interval = INTERVAL += 10000;
        mLocationRequest.setInterval(interval);
        startLocationUpdates();
        String porukaPovecaj = "INTERVAL povecan na: " + interval;
        Toast.makeText(this, porukaPovecaj, Toast.LENGTH_SHORT).show();     /* interval vs. fastest interval: ako nismo jedina app koja slusa za lokacije,
                                                                             mozemo dobiti lokaciju i prije nego zadani interval */
    }

    public void On_clickSmanji(View V) {
        if (INTERVAL == 10000) {
            Toast.makeText(this, "Ne mogu smanjiti INTERVAL", Toast.LENGTH_SHORT).show();
        } else {
            long interval = INTERVAL -= 10000;
            mLocationRequest.setInterval(interval);
            startLocationUpdates();
            String porukaSmanji = "INTERVAL smanjen na: " + interval;
            Toast.makeText(this, porukaSmanji, Toast.LENGTH_SHORT).show();
        }
    }

    public void On_clickPovecajFastest(View v) {
        long fastinterval = FASTEST_INTERVAL += 10000;
        mLocationRequest.setFastestInterval(fastinterval);
        startLocationUpdates();
        String porukaPovecajFastest = "FASTEST INTERVAL povecan na: " + fastinterval;
        Toast.makeText(this, porukaPovecajFastest, Toast.LENGTH_SHORT).show();
    }

    public void On_clickSmanjiFastest(View V) {
        if (FASTEST_INTERVAL == 10000) {
            Toast.makeText(this, "Ne mogu smanjiti FASTEST_INTERVAL", Toast.LENGTH_SHORT).show();
        } else {
            long fastinterval = FASTEST_INTERVAL -= 10000;
            mLocationRequest.setFastestInterval(fastinterval);
            startLocationUpdates();
            String porukaSmanjiFastest = "Smanjio FASTEST na:" + fastinterval;
            Toast.makeText(this, porukaSmanjiFastest, Toast.LENGTH_SHORT).show();
        }
    }

    // ovo je metoda za pretvaranje upisane adrese u koordinate (latitude i longitude)
    public void geoLocate(View v) throws IOException {
        hideSoftKeyboard(v);

        googleMap.clear();

        EditText et = (EditText) findViewById(R.id.editText1);
        String location = et.getText().toString();

        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(location, 1);

        //ako nije pronasao adresu, odnosno unesenu lokaciju
        if (list != null && list.size() > 0) {
            Address add = list.get(0); //daj mi prvi i jedini item u listi
            String locality = add.getLocality();

            Toast.makeText(this, locality, Toast.LENGTH_SHORT).show();

            double lat = add.getLatitude();
            double lng = add.getLongitude();

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


    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
    }

    //pocisti sve markere s mape
    @Override
    public void onMapLongClick(LatLng arg0) {
        googleMap.clear();
        mapCircle = null;
    }
}




