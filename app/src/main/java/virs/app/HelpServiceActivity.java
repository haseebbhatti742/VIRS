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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class HelpServiceActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    ImageView imageViewHelpServiceGoBack;
    TextView textViewHelpServiceCurrentAddress;
    LinearLayout linearLayoutHospitals, linearLayoutPoliceStations, linearLayoutRescue;
    Context context;
    String placesType, url, RADIUS="5000";

    //google map
    private static String API_KEY = "";
    public static GoogleMap map;
    public static MapView mapHelpService;
    private static final int REQUEST_CODE = 101;
    NetworkChangeReceiver networkChangeReceiver;

    public static double currentLongitude=0.0, currentLatitude=0.0;
    public static LocationListener locationListener;
    public static LocationManager locationManager;
    public static MarkerOptions markerOptions;
    public static Marker marker;
    public static BitmapDescriptor locationMarkerIcon;
    public static Geocoder geocoder;
    public static List<Address> addresses;
    public static String address="";
    Location locationCurrent, locationPrevious;
    ArrayList<Marker> markerArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_service);
        hideSystemUI();
        setViews();
        setListeners();
        locationPermission();
    }

    private void setViews() {
        context = getApplicationContext();
        geocoder = new Geocoder(this, Locale.getDefault());
        markerArrayList = new ArrayList<>();

        imageViewHelpServiceGoBack = findViewById(R.id.imageViewHelpServiceGoBack);
        textViewHelpServiceCurrentAddress = findViewById(R.id.textViewHelpServiceCurrentAddress);
        linearLayoutHospitals = findViewById(R.id.linearLayoutHospitals);
        linearLayoutPoliceStations = findViewById(R.id.linearLayoutPoliceStations);
        linearLayoutRescue = findViewById(R.id.linearLayoutRescue);
    }

    private void setListeners() {
        imageViewHelpServiceGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        linearLayoutHospitals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLatitude != 0.0 && currentLongitude != 0.0) {
                    placesType = "hospital";
                    getPlaces();
                    showAllMarkersInfo();
                } else {
                    Toast.makeText(HelpServiceActivity.this, "Getting Location", Toast.LENGTH_SHORT).show();
                }
            }
        });

        linearLayoutPoliceStations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLatitude != 0.0 && currentLongitude != 0.0) {
                    placesType = "police";
                    getPlaces();
                    showAllMarkersInfo();
                } else {
                    Toast.makeText(HelpServiceActivity.this, "Getting Location", Toast.LENGTH_SHORT).show();
                }
            }
        });

        linearLayoutRescue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLatitude != 0.0 && currentLongitude != 0.0) {
                    placesType = "rescue ambulance";
                    getPlaces();
                    showAllMarkersInfo();
                } else {
                    Toast.makeText(HelpServiceActivity.this, "Getting Location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void showAllMarkersInfo(){
        for(int i=0; i<markerArrayList.size(); i++){
            markerArrayList.get(i).showInfoWindow();
            markerArrayList.get(i).setFlat(true);
        }
    }

    private void map() {
        MapsInitializer.initialize(context);
        networkChangeReceiver = new HelpServiceActivity.NetworkChangeReceiver();
        registerNetworkBroadcastForNougat();

        mapHelpService = (MapView) findViewById(R.id.mapHelpService);
        if(mapHelpService != null){
            mapHelpService.onCreate(null);
            mapHelpService.onResume();
            mapHelpService.getMapAsync(this);
        }

        API_KEY = getResources().getString(R.string.google_api);
        Places.initialize(HelpServiceActivity.this, API_KEY);
        PlacesClient placesClient = Places.createClient(HelpServiceActivity.this);
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
            map.setOnMarkerClickListener(this);
            startLocationListener();
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        try {
            Intent intent = new Intent(HelpServiceActivity.this, PathActivity.class);
            intent.putExtra("destinationLatitude", marker.getPosition().latitude);
            intent.putExtra("destinationLongitude", marker.getPosition().longitude);
            intent.putExtra("destinationMarkerTitle", marker.getTitle());
            intent.putExtra("destinationMarkerType", placesType);
            startActivity(intent);
        } catch (Exception e) {
                    e.printStackTrace();
        }
        return true;
    }

    public void locationPermission(){
        if (ContextCompat.checkSelfPermission(HelpServiceActivity.this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(HelpServiceActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(HelpServiceActivity.this,
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
        //locationMarkerIcon = getBitmapFromVector(context, R.drawable.ic_location_marker, ContextCompat.getColor(context, R.color.marker_color_online));
        locationListener = new LocationListener(){
            @Override
            public void onLocationChanged(Location location){
                if(SplashActivity.isNetworkConnected(context)) {
                    if (ViewReportedIncidentsActivity.marker != null) {
                        ViewReportedIncidentsActivity.marker.remove();
                    }
                    currentLongitude = location.getLongitude();
                    currentLatitude = location.getLatitude();
                    LatLng latLng = new LatLng(currentLatitude, currentLongitude);

                    locationCurrent = new Location("");
                    locationCurrent.setLatitude(currentLatitude);
                    locationCurrent.setLongitude(currentLongitude);

                    markerOptions = new MarkerOptions().position(latLng).title("You");

                    if(locationPrevious == null || (locationPrevious.distanceTo(locationCurrent))/1000 >1){
                        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13.0f));
                        locationPrevious = locationCurrent;
                    }

                    try {
                        addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);
                        address = addresses.get(0).getAddressLine(0);
                        textViewHelpServiceCurrentAddress.setText(address);
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
//            locationMarkerIcon = getBitmapFromVector(context, R.drawable.ic_location_marker, ContextCompat.getColor(context, R.color.marker_color_offline));
//            ViewReportedInidentsActivity.marker.setIcon(locationMarkerIcon);
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

    public void getPlaces(){
       url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"+
               "?location="+currentLatitude+","+currentLongitude+
               "&radius="+RADIUS+
               "&keyword="+placesType+
               "&types="+placesType+
               "&sensor=true"+
               "&key="+API_KEY;

        new PlaceTask().execute(url);
    }

    public class PlaceTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... strings) {
            String data = null;
            try {
                data = downloadUrl(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            new ParserTask().execute(s);
        }
    }

    private String downloadUrl(String string) throws IOException {
        URL url = new URL(string);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        InputStream stream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line="";
        while((line = reader.readLine()) != null){
            builder.append(line);
        }
        String data = builder.toString();
        reader.close();
        return data;
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

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>>{

        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            MyJsonParser jsonParser = new MyJsonParser();
            List<HashMap<String,String>> mapList = null;
            JSONObject object = null;
            try {
                object = new JSONObject(strings[0]);
                mapList = jsonParser.parseResult(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return mapList;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> hashMaps) {
            map.clear();
            markerArrayList.clear();
            for(int i=0; i<hashMaps.size(); i++){
                HashMap<String, String> hashMapList = hashMaps.get(i);
                double lat = Double.parseDouble(hashMapList.get("lat"));
                double lng = Double.parseDouble(hashMapList.get("lng"));
                String name = hashMapList.get("name");
                //String phoneNumber = hashMapList.get("phoneNumber");
                LatLng latLng = new LatLng(lat,lng);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(name);

                if(placesType.equals("hospital")){
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                } else if(placesType.equals("police")){
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                } else if(placesType.equals("health")){
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
                //markerOptions.snippet(phoneNumber);

//                IconGenerator iconGenerator = new IconGenerator(context.getApplicationContext());
//                TextView textView = new TextView(context);
//                textView.setText(markerOptions.getTitle());
//                iconGenerator.setContentView(textView);
//                Bitmap icon = iconGenerator.makeIcon(markerOptions.getTitle());
//                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(markerOptions.getTitle());
                Marker marker = map.addMarker(markerOptions);
                //markerArrayList.add(marker);
            }

            //showAllMarkersInfo();
        }
    }
}