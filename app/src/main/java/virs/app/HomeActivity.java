package virs.app;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.drawerlayout.widget.DrawerLayout;

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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    DrawerLayout drawerLayout;
    View leftDrawerMenu;
    ImageView ivNavMenu;
    TextView textViewDrawerUserName, textViewDrawerHome, textViewDrawerReportIncident, textViewDrawerViewReportedIncidents,
            textViewDrawerGetHelpServiceLocation, textViewDrawerLogout, textViewHomeAddress;

    Button buttonHomeReportIncident, buttonHomeHelpServiceInformation, buttonHomeViewReportedIncidents;

    private static String API_KEY = "";
    public static GoogleMap map;
    public static MapView mapHome;
    private static final int REQUEST_CODE = 101;
    NetworkChangeReceiver networkChangeReceiver;

    Context context;

    public static double currentLongitude=0.0, currentLatitude=0.0;
    public static LocationListener locationListener;
    public static LocationManager locationManager;
    public static MarkerOptions markerOptions;
    public static Marker marker;
    public static BitmapDescriptor locationMarkerIcon;
    public static Geocoder geocoder;
    public static List<Address> addresses;
    public static String address="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        hideSystemUI();
        setViews();
        setListeners();
        locationPermission();
    }

    public void setViews(){
        context = getApplicationContext();
        leftDrawerMenu = findViewById(R.id.leftDrawerMenu);
        drawerLayout = findViewById(R.id.drawerLayout);
        ivNavMenu = findViewById(R.id.ivNavMenu);
        textViewDrawerUserName = findViewById(R.id.textViewDrawerUserName);
        textViewDrawerHome = findViewById(R.id.textViewDrawerHome);
        textViewDrawerReportIncident = findViewById(R.id.textViewDrawerReportIncident);
        textViewDrawerViewReportedIncidents = findViewById(R.id.textViewDrawerViewReportedIncidents);
        textViewDrawerGetHelpServiceLocation = findViewById(R.id.textViewDrawerGetHelpServiceLocation);
        textViewDrawerLogout = findViewById(R.id.textViewDrawerLogout);
        textViewHomeAddress = findViewById(R.id.textViewHomeAddress);

        buttonHomeReportIncident = findViewById(R.id.buttonHomeReportIncident);
        buttonHomeHelpServiceInformation = findViewById(R.id.buttonHomeHelpServiceInformation);
        buttonHomeViewReportedIncidents = findViewById(R.id.buttonHomeViewReportedIncidents);

        textViewDrawerUserName.setText(SplashActivity.session.getName());

        geocoder = new Geocoder(this, Locale.getDefault());
    }

    public void setListeners(){
        textViewDrawerHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Home Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        textViewDrawerReportIncident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(address.equals("")){
                    Toast.makeText(HomeActivity.this, "Getting Location", Toast.LENGTH_SHORT).show();
                } else{
                    startActivity(new Intent(HomeActivity.this, ReportIncidentActivity.class));
                }
            }
        });

        textViewDrawerViewReportedIncidents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, ViewReportedIncidentsActivity.class));
            }
        });

        textViewDrawerGetHelpServiceLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, HelpServiceActivity.class));
            }
        });

        textViewDrawerLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SplashActivity.session.setSession("false");
                SplashActivity.session.setCNIC("");
                SplashActivity.session.setName("");
                SplashActivity.session.setCity("");
                SplashActivity.session.setPhoneNumber("");
                Toast.makeText(HomeActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            }
        });

        ivNavMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLeftDrawer();
            }
        });

        buttonHomeReportIncident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(address.equals("")){
                    Toast.makeText(HomeActivity.this, "Getting Location", Toast.LENGTH_SHORT).show();
                } else{
                    startActivity(new Intent(HomeActivity.this, ReportIncidentActivity.class));
                }
            }
        });

        buttonHomeViewReportedIncidents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, ViewReportedIncidentsActivity.class));
            }
        });

        buttonHomeHelpServiceInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, HelpServiceActivity.class));
            }
        });
    }

    public void locationPermission(){
        if (ContextCompat.checkSelfPermission(HomeActivity.this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(HomeActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);

                // MY_PERMISSIONS_REQUEST_FINE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
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

    public void map(){
        MapsInitializer.initialize(context);
        networkChangeReceiver = new NetworkChangeReceiver();
        registerNetworkBroadcastForNougat();

        mapHome = (MapView) findViewById(R.id.mapHome);
        if(mapHome != null){
            mapHome.onCreate(null);
            mapHome.onResume();
            mapHome.getMapAsync(this);
        }

        API_KEY = getResources().getString(R.string.google_api);
        Places.initialize(HomeActivity.this, API_KEY);
        PlacesClient placesClient = Places.createClient(HomeActivity.this);
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
        //locationMarkerIcon = getBitmapFromVector(context, R.drawable.ic_location_marker, ContextCompat.getColor(context, R.color.marker_color_online));
        locationListener = new LocationListener(){
            @Override
            public void onLocationChanged(Location location){
                if(SplashActivity.isNetworkConnected(context)) {
                    if (HomeActivity.marker != null) {
                        HomeActivity.marker.remove();
                    }
                    currentLongitude = location.getLongitude();
                    currentLatitude = location.getLatitude();
                    LatLng latLng = new LatLng(currentLatitude, currentLongitude);
                    MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("You");
                    HomeActivity.map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    HomeActivity.map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.5f));
                    //HomeActivity.marker.showInfoWindow();
                    //HomeActivity.marker = HomeActivity.map.addMarker(markerOptions);
                    //HomeActivity.marker.setIcon(locationMarkerIcon);
                    //map.addMarker(new MarkerOptions().icon(locationMarkerIcon).position(latLng));
                    //HomeActivity.marker.setFlat(true);

                    try {
                        addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                        address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//                        String city = addresses.get(0).getLocality();
//                        String state = addresses.get(0).getAdminArea();
//                        String country = addresses.get(0).getCountryName();
//                        String postalCode = addresses.get(0).getPostalCode();
//                        String knownName = addresses.get(0).getFeatureName();

                        textViewHomeAddress.setText(address);
                    } catch (IOException e) {
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
//            locationMarkerIcon = getBitmapFromVector(context, R.drawable.ic_location_marker, ContextCompat.getColor(context, R.color.marker_color_offline));
//            HomeActivity.marker.setIcon(locationMarkerIcon);
            locationManager.removeUpdates(locationListener);
            locationManager = null;
            locationListener = null;
        }
    }

    public void toggleLeftDrawer() {
        if (drawerLayout.isDrawerOpen(leftDrawerMenu)) {
            drawerLayout.closeDrawer(leftDrawerMenu);
        } else {
            drawerLayout.openDrawer(leftDrawerMenu);
        }
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (SplashActivity.isNetworkConnected(context)) {
                //Snackbar.make(context, "Internet Connected", Snackbar.LENGTH_LONG).show();
                startLocationListener();
            } else {
                //Snackbar.make(context, "Internet Disconnected", Snackbar.LENGTH_LONG).show();
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

    protected void unregisterNetworkChanges() {
        try {
            context.unregisterReceiver(networkChangeReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
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

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
        // super.onBackPressed();
        // Not calling **super**, disables back button in current screen.
    }
}