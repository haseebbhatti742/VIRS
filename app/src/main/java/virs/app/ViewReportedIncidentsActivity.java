package virs.app;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ViewReportedIncidentsActivity extends AppCompatActivity implements OnMapReadyCallback{

    ImageView imageViewViewIncidentsGoBack;
    TextView textViewViewIncidentsCurrentAddress;

    private static String API_KEY = "";
    public static GoogleMap map;
    public static MapView mapViewIncidents;
    private static final int REQUEST_CODE = 101;
    NetworkChangeReceiver networkChangeReceiver;

    Context context;

    public static double currentLongitude=0.0, currentLatitude=0.0;
    public static LocationListener locationListener;
    public static LocationManager locationManager;
    public static MarkerOptions markerOptions;
    public static Marker marker;
    public static Geocoder geocoder;
    public static List<Address> addresses;
    public static String address="";

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference crimesRef = database.getReference("crimes");
    DatabaseReference accidentsRef = database.getReference("accidents");
    DatabaseReference harassmentRef = database.getReference("harassments");
    DatabaseReference violenceRef = database.getReference("violences");

    ArrayList<IncidentMarker> incidentMarkerArrayList;

    Location locationCurrent, locationPrevious;
    public static final double RADIUS=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reported_inidents);
        hideSystemUI();
        setViews();
        setListeners();
        locationPermission();
    }

    private void setViews() {
        context = getApplicationContext();
        incidentMarkerArrayList = new ArrayList<>();
        imageViewViewIncidentsGoBack = findViewById(R.id.imageViewViewIncidentsGoBack);
        textViewViewIncidentsCurrentAddress = findViewById(R.id.textViewViewIncidentsCurrentAddress);

        geocoder = new Geocoder(this, Locale.getDefault());
    }

    private void setListeners() {
        imageViewViewIncidentsGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void locationPermission(){
        if (ContextCompat.checkSelfPermission(ViewReportedIncidentsActivity.this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ViewReportedIncidentsActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(ViewReportedIncidentsActivity.this,
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

    private void map() {
        MapsInitializer.initialize(context);
        networkChangeReceiver = new NetworkChangeReceiver();
        registerNetworkBroadcastForNougat();

        mapViewIncidents = (MapView) findViewById(R.id.mapViewIncidents);
        if(mapViewIncidents != null){
            mapViewIncidents.onCreate(null);
            mapViewIncidents.onResume();
            mapViewIncidents.getMapAsync(this);
        }

        API_KEY = getResources().getString(R.string.google_api);
        Places.initialize(ViewReportedIncidentsActivity.this, API_KEY);
        PlacesClient placesClient = Places.createClient(ViewReportedIncidentsActivity.this);
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
                    locationCurrent = new Location("");
                    locationCurrent.setLatitude(currentLatitude);
                    locationCurrent.setLongitude(currentLongitude);
                    LatLng latLng = new LatLng(currentLatitude, currentLongitude);
                    markerOptions = new MarkerOptions().position(latLng).title("You");
                    ViewReportedIncidentsActivity.map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    ViewReportedIncidentsActivity.map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.5f));

                    if(locationPrevious == null || (locationPrevious.distanceTo(locationCurrent))/1000 >1){
                        locationPrevious = locationCurrent;
                        map.clear();
                        getMarkers();
                    }

                    try {
                        addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);
                        address = addresses.get(0).getAddressLine(0);
                        textViewViewIncidentsCurrentAddress.setText(address);
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

    public BitmapDescriptor getBitmapFromVector(@NonNull Context context,
                                                @DrawableRes int vectorResourceId,
                                                @ColorInt int tintColor) {

        Drawable vectorDrawable = ResourcesCompat.getDrawable(
                getResources(), vectorResourceId, null);
        if (vectorDrawable == null) {
            Log.e("hey", "Requested vector resource was not found");
            return BitmapDescriptorFactory.defaultMarker();
        }
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, tintColor);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void getMarkers(){
        getCrimesMarkers();
        getAccidentMarkers();
        getHarassmentMarkers();
        getViolenceMarkers();
    }

    public void getCrimesMarkers(){
        crimesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    String crimeId = snapshot.getKey();
                    LatLng latLng = new LatLng(Double.parseDouble(snapshot.child("crimeLatitude").getValue().toString()),
                            Double.parseDouble(snapshot.child("crimeLongitude").getValue().toString()));

                    Location location = new Location("");
                    location.setLongitude(latLng.longitude);
                    location.setLatitude(latLng.latitude);

                    if((locationCurrent.distanceTo(location))/1000 <=RADIUS) {
                        String title = snapshot.child("crimeDate").getValue().toString();
                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title).snippet(snapshot.child("crimeType").getValue().toString());
                        Marker marker = ViewReportedIncidentsActivity.map.addMarker(markerOptions);
                        marker.setFlat(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getAccidentMarkers(){
        accidentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    LatLng latLng = new LatLng(Double.parseDouble(snapshot.child("accidentLatitude").getValue().toString()),
                            Double.parseDouble(snapshot.child("accidentLongitude").getValue().toString()));

                    Location location = new Location("");
                    location.setLongitude(latLng.longitude);
                    location.setLatitude(latLng.latitude);

                    if((locationCurrent.distanceTo(location))/1000 <=RADIUS) {
                        String title = snapshot.child("accidentDate").getValue().toString();
                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title).snippet("Accident");
                        Marker marker = ViewReportedIncidentsActivity.map.addMarker(markerOptions);
                        marker.setFlat(true);
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getHarassmentMarkers(){
        harassmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    LatLng latLng = new LatLng(Double.parseDouble(snapshot.child("harassmentLatitude").getValue().toString()),
                            Double.parseDouble(snapshot.child("harassmentLongitude").getValue().toString()));

                    Location location = new Location("");
                    location.setLongitude(latLng.longitude);
                    location.setLatitude(latLng.latitude);

                    if((locationCurrent.distanceTo(location))/1000 <=RADIUS) {
                        String title = snapshot.child("harassmentDate").getValue().toString();
                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title).snippet("Harassment");
                        Marker marker = ViewReportedIncidentsActivity.map.addMarker(markerOptions);
                        marker.setFlat(true);
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getViolenceMarkers(){
        violenceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    LatLng latLng = new LatLng(Double.parseDouble(snapshot.child("violenceLatitude").getValue().toString()),
                            Double.parseDouble(snapshot.child("violenceLongitude").getValue().toString()));

                    Location location = new Location("");
                    location.setLongitude(latLng.longitude);
                    location.setLatitude(latLng.latitude);

                    if((locationCurrent.distanceTo(location))/1000 <=RADIUS) {
                        String title = snapshot.child("violenceDate").getValue().toString();
                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title).snippet("Violence");
                        Marker marker = ViewReportedIncidentsActivity.map.addMarker(markerOptions);
                        marker.setFlat(true);
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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