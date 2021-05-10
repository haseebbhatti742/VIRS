package virs.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PathActivity extends AppCompatActivity implements OnMapReadyCallback {

    ImageView imageViewPathGoBack;
    TextView textViewPathDistance, textViewPathCurrentAddress, textViewPathDestinationAddress;
    Context context;

    private static DecimalFormat decimalFormat = new DecimalFormat("#.##");

    //google map
    private static String API_KEY = "";
    public static GoogleMap map;
    public static MapView mapPath;
    private static final int REQUEST_CODE = 101;
    NetworkChangeReceiver networkChangeReceiver;

    public static double currentLongitude=0.0, currentLatitude=0.0;
    public static LocationListener locationListener;
    public static LocationManager locationManager;
    public static MarkerOptions markerOptions;
    public static Marker marker;
    public static BitmapDescriptor locationMarkerIcon;
    public static Geocoder geocoder;
    public static List<Address> addresses, addressesDestination;
    public static String address="", addressDestination="";
    Location locationCurrent, locationPrevious, locationDestination;
    private String destinationMarkerTitle, destinationMarkerType;
    private LatLng latLng, latLngDest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path);
        hideSystemUI();
        setViews();
        setListeners();
        locationPermission();
    }

    public void setViews(){
        imageViewPathGoBack = findViewById(R.id.imageViewPathGoBack);
        textViewPathDistance = findViewById(R.id.textViewPathDistance);
        textViewPathCurrentAddress = findViewById(R.id.textViewPathCurrentAddress);
        textViewPathDestinationAddress = findViewById(R.id.textViewPathDestinationAddress);

        locationDestination = new Location("");
        locationCurrent = new Location("");
        locationDestination.setLatitude(getIntent().getDoubleExtra("destinationLatitude", 0));
        locationDestination.setLongitude(getIntent().getDoubleExtra("destinationLongitude", 0));
        destinationMarkerTitle = getIntent().getStringExtra("destinationMarkerTitle");
        destinationMarkerType = getIntent().getStringExtra("destinationMarkerType");
        latLngDest = new LatLng(locationDestination.getLatitude(),locationDestination.getLongitude());
        context = getApplicationContext();
        geocoder = new Geocoder(this, Locale.getDefault());
    }

    public void setListeners(){
        imageViewPathGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void map() {
        MapsInitializer.initialize(context);
        networkChangeReceiver = new PathActivity.NetworkChangeReceiver();
        registerNetworkBroadcastForNougat();

        mapPath = (MapView) findViewById(R.id.mapPath);
        if(mapPath != null){
            mapPath.onCreate(null);
            mapPath.onResume();
            mapPath.getMapAsync(this);
        }

        API_KEY = getResources().getString(R.string.google_api);
        Places.initialize(PathActivity.this, API_KEY);
        PlacesClient placesClient = Places.createClient(PathActivity.this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap){
        MapsInitializer.initialize(this);

        map = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_CODE);
        }
        else{
            map.setMyLocationEnabled(true);
            startLocationListener();
            setDestinationMarker();
            setDestinationAddress();
        }
    }

    public void setDestinationMarker(){
        //String phoneNumber = hashMapList.get("phoneNumber");
        LatLng latLng = new LatLng(locationDestination.getLatitude(),locationDestination.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(destinationMarkerTitle);


        if(destinationMarkerType.equals("hospital")){
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else if(destinationMarkerType.equals("police")){
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        } else if(destinationMarkerType.equals("rescue ambulance")){
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
        //markerOptions.snippet(phoneNumber);
        Marker marker = map.addMarker(markerOptions);
        marker.showInfoWindow();
    }

    public void setDestinationAddress(){
        try {
            addressesDestination = geocoder.getFromLocation(locationDestination.getLatitude(), locationDestination.getLongitude(), 1);
            addressDestination = addressesDestination.get(0).getAddressLine(0);
            textViewPathDestinationAddress.setText(addressDestination);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void locationPermission(){
        if (ContextCompat.checkSelfPermission(PathActivity.this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(PathActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(PathActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            map();
        }
    }

    @SuppressLint("MissingPermission")
    public void startLocationListener(){
        locationListener = new LocationListener(){
            @Override
            public void onLocationChanged(Location location){
                if(SplashActivity.isNetworkConnected(context)) {
                    if (ViewReportedIncidentsActivity.marker != null) {
                        ViewReportedIncidentsActivity.marker.remove();
                    }
                    currentLongitude = location.getLongitude();
                    currentLatitude = location.getLatitude();
                    latLng = new LatLng(currentLatitude, currentLongitude);

                    locationCurrent = new Location("");
                    locationCurrent.setLatitude(currentLatitude);
                    locationCurrent.setLongitude(currentLongitude);

                    textViewPathDistance.setText(decimalFormat.format(locationCurrent.distanceTo(locationDestination)/1000) + " KM");

                    markerOptions = new MarkerOptions().position(latLng).title("You");
                    if(locationPrevious == null || (locationPrevious.distanceTo(locationCurrent))/1000 >1){
                        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13.0f));
                        locationPrevious = locationCurrent;
                        drawPath();
                    }

                    try {
                        addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);
                        address = addresses.get(0).getAddressLine(0);
                        textViewPathCurrentAddress.setText(address);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    stopLocationListener();
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle){

            }

            @Override
            public void onProviderEnabled(String s){

            }

            @Override
            public void onProviderDisabled(String s){
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);
    }

    public void drawPath(){
        map.clear();
        setDestinationMarker();

        String origin = latLng.latitude+","+latLng.longitude;
        String destination = latLngDest.latitude+","+latLngDest.longitude;
        List<LatLng> path = new ArrayList();
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(API_KEY)
                .build();
        DirectionsApiRequest req = DirectionsApi.getDirections(context, origin, destination);
        try {
            DirectionsResult res = req.await();

            //Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.length > 0) {
                DirectionsRoute route = res.routes[0];

                if (route.legs !=null) {
                    for(int i=0; i<route.legs.length; i++) {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null) {
                            for (int j=0; j<leg.steps.length;j++){
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length >0) {
                                    for (int k=0; k<step.steps.length;k++){
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;
                                        if (points1 != null) {
                                            //Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1) {
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                } else {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null) {
                                        //Decode polyline and add points to list of route coordinates
                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                        for (com.google.maps.model.LatLng coord : coords) {
                                            path.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch(Exception ex) {
            Log.e("TAG", ex.getLocalizedMessage());
        }

        //Draw the polyline
        if (path.size() > 0) {
            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(5);
            map.addPolyline(opts);
        }

        map.getUiSettings().setZoomControlsEnabled(true);
    }

    public void stopLocationListener(){
        if(locationManager != null){
            locationManager.removeUpdates(locationListener);
            locationManager = null;
            locationListener = null;
        }
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (SplashActivity.isNetworkConnected(context)) {
                startLocationListener();
            } else {
                stopLocationListener();
            }
        }
    }

    private void registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    public void hideSystemUI() {
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        int statusBarHeight = (int) dpToPx(-20);
        View view = new View(this);
        view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.getLayoutParams().height = statusBarHeight;
        ((ViewGroup) window.getDecorView()).addView(view);
        view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

    }

    public float dpToPx(float dp) {
        return (dp * Resources.getSystem().getDisplayMetrics().density);
    }

}