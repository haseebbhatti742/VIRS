package virs.app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ReportCrimeActivity extends AppCompatActivity {

    ImageView imageViewReportCrimeGoBack;
    TextView textViewReportCrimeLocation, textViewReportCrimeDate, textViewReportCrimeTime;
    Spinner spinnerReportCrimeType;
    EditText editTextReportCrimeDetails;
    Button buttonReportCrimeTakePhoto;
    ImageView imageViewReportCrimePhoto, imageViewReportCrimeOK;

    String crimeId="", crimeType="", crimeLocation, crimeDate, crimeTime, crimeDetails;
    ArrayAdapter<String> adapterCrimeTypes;

    StorageReference mStorageRef;
    boolean checkImage = false;
    Bitmap imageBitmap;

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference crimesRef = database.getReference("crimes");

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_crime);
        hideSystemUI();
        setViews();
        setSpinnerReportCrimeType();
        setListeners();
    }

    private void setViews() {
        imageViewReportCrimeGoBack = findViewById(R.id.imageViewReportCrimeGoBack);
        textViewReportCrimeLocation = findViewById(R.id.textViewReportCrimeLocation);
        textViewReportCrimeDate = findViewById(R.id.textViewReportCrimeDate);
        textViewReportCrimeTime = findViewById(R.id.textViewReportCrimeTime);
        spinnerReportCrimeType = findViewById(R.id.spinnerReportCrimeType);
        editTextReportCrimeDetails = findViewById(R.id.editTextReportCrimeDetails);
        buttonReportCrimeTakePhoto = findViewById(R.id.buttonReportCrimeTakePhoto);
        imageViewReportCrimePhoto = findViewById(R.id.imageViewReportCrimePhoto);
        imageViewReportCrimeOK = findViewById(R.id.imageViewReportCrimeOK);

        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Processing ...");
        mProgress.setMessage("Please wait...");
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        List<String> listTypes = new ArrayList<>();
        listTypes.add("Select Crime Type");
        listTypes.add("Murder");
        listTypes.add("Robbery");
        listTypes.add("Kidnapping");
        listTypes.add("Car Theft");
        listTypes.add("Bombing");
        adapterCrimeTypes = new ArrayAdapter<String>(ReportCrimeActivity.this, android.R.layout.simple_spinner_dropdown_item, listTypes);
        adapterCrimeTypes.setDropDownViewResource(R.layout.spinner_list);
        spinnerReportCrimeType.setAdapter(adapterCrimeTypes);

        crimeLocation = HomeActivity.address;
        textViewReportCrimeLocation.setText(crimeLocation);

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        Date date = Calendar.getInstance().getTime();
        crimeDate = format.format(date);//31-12-9999
        int mYear = calendar.get(Calendar.YEAR);//9999
        int mMonth = calendar.get(Calendar.MONTH);
        mMonth = mMonth + 1;//12
        int hrs = calendar.get(Calendar.HOUR_OF_DAY);//24
        int min = calendar.get(Calendar.MINUTE);//59
        String AMPM;
        if (calendar.get(Calendar.AM_PM) == 0) {
            AMPM = "AM";
        } else {
            AMPM = "PM";
        }

        if(hrs>12){
            hrs = hrs-12;
        } else if(hrs==0){
            hrs = 12;
        }

        crimeTime = hrs+":"+min+" "+AMPM;

        textViewReportCrimeDate.setText(crimeDate);
        textViewReportCrimeTime.setText(crimeTime);
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    private void setListeners() {
        imageViewReportCrimeGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        buttonReportCrimeTakePhoto.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                else {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });

        imageViewReportCrimeOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(crimeType)) {
                    Toast.makeText(ReportCrimeActivity.this, "Select Crime Type", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(editTextReportCrimeDetails.getText())){
                    editTextReportCrimeDetails.setError("Enter Crime Details");
                } else {
                    crimeDetails = editTextReportCrimeDetails.getText().toString();
                    crimeId = crimesRef.push().getKey();
                    HashMap<String, String> map = new HashMap<>();
                    map.put("crimeId", crimeId);
                    map.put("crimeLocation", crimeLocation);
                    map.put("crimeLongitude", String.valueOf(HomeActivity.currentLongitude));
                    map.put("crimeLatitude", String.valueOf(HomeActivity.currentLatitude));
                    map.put("crimeDate", crimeDate);
                    map.put("crimeTime", crimeTime);
                    map.put("crimeType", crimeType);
                    map.put("crimeDetails", crimeDetails);

                    crimesRef.child(crimeId).setValue(map);
                    if(checkImage){
                        FileUpload();
                    } else {
                        Intent intent = new Intent(ReportCrimeActivity.this, ReportedSuccessfullyActivity.class);
                        intent.putExtra("message", "Crime Reported Successfully!");
                        intent.putExtra("date", crimeDate);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    public void setSpinnerReportCrimeType(){
        spinnerReportCrimeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0) {
                    crimeType = parent.getItemAtPosition(position).toString();
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                } else {
                    crimeType = "";
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.GRAY);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_LONG).show();
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, "Camera Exception: "+e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }


    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageViewReportCrimePhoto.setImageBitmap(imageBitmap);
            checkImage = true;
        } else{
            Toast.makeText(ReportCrimeActivity.this, "Failure", Toast.LENGTH_SHORT).show();
        }
    }

    private String getExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return  mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void FileUpload() {
        try{
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            StorageReference Ref = mStorageRef.child("Crimes/"+crimeId);
            byte[] bytes = stream.toByteArray();
            Ref.putBytes(bytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Intent intent = new Intent(ReportCrimeActivity.this, ReportedSuccessfullyActivity.class);
                    intent.putExtra("message", "Crime Reported Successfully!");
                    intent.putExtra("date", crimeDate);
                    startActivity(intent);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(ReportCrimeActivity.this, "Exception1: "+exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception exception) {
            Toast.makeText(ReportCrimeActivity.this, "Exception2: "+exception.getMessage(), Toast.LENGTH_SHORT).show();
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