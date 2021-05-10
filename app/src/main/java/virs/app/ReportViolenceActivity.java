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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ReportViolenceActivity extends AppCompatActivity {

    ImageView imageViewReportAccidentGoBack;
    TextView textViewReportAccidentLocation, textViewReportAccidentDate, textViewReportAccidentTime;
    EditText editTextReportAccidentDetails;
    Button buttonReportAccidentTakePhoto;
    ImageView imageViewReportAccidentPhoto, imageViewReportAccidentOK;

    String accidentId="", accidentType="", accidentLocation, accidentDate, accidentTime, accidentDetails;

    StorageReference mStorageRef;
    boolean checkImage = false;
    Bitmap imageBitmap;

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference accidentsRef = database.getReference("violences");

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_violence);
        hideSystemUI();
        setViews();
        setListeners();
    }

    private void setViews() {
        imageViewReportAccidentGoBack = findViewById(R.id.imageViewReportViolenceGoBack);
        textViewReportAccidentLocation = findViewById(R.id.textViewReportViolenceLocation);
        textViewReportAccidentDate = findViewById(R.id.textViewReportViolenceDate);
        textViewReportAccidentTime = findViewById(R.id.textViewReportViolenceTime);
        editTextReportAccidentDetails = findViewById(R.id.editTextReportViolenceDetails);
        buttonReportAccidentTakePhoto = findViewById(R.id.buttonReportViolenceTakePhoto);
        imageViewReportAccidentPhoto = findViewById(R.id.imageViewReportViolencePhoto);
        imageViewReportAccidentOK = findViewById(R.id.imageViewReportViolenceOK);

        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Processing ...");
        mProgress.setMessage("Please wait...");
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);


        accidentLocation = HomeActivity.address;
        textViewReportAccidentLocation.setText(accidentLocation);

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        Date date = Calendar.getInstance().getTime();
        accidentDate = format.format(date);//31-12-9999
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

        accidentTime = hrs+":"+min+" "+AMPM;

        textViewReportAccidentDate.setText(accidentDate);
        textViewReportAccidentTime.setText(accidentTime);
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    private void setListeners() {
        imageViewReportAccidentGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        buttonReportAccidentTakePhoto.setOnClickListener(new View.OnClickListener() {
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

        imageViewReportAccidentOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(editTextReportAccidentDetails.getText())){
                    editTextReportAccidentDetails.setError("Enter Violence Details");
                } else {
                    accidentDetails = editTextReportAccidentDetails.getText().toString();
                    accidentId = accidentsRef.push().getKey();
                    HashMap<String, String> map = new HashMap<>();
                    map.put("violenceId", accidentId);
                    map.put("violenceLocation", accidentLocation);
                    map.put("violenceLongitude", String.valueOf(HomeActivity.currentLongitude));
                    map.put("violenceLatitude", String.valueOf(HomeActivity.currentLatitude));
                    map.put("violenceDate", accidentDate);
                    map.put("violenceTime", accidentTime);
                    map.put("violenceType", accidentType);
                    map.put("violenceDetails", accidentDetails);

                    accidentsRef.child(accidentId).setValue(map);
                    if(checkImage){
                        FileUpload();
                    } else {
                        Intent intent = new Intent(ReportViolenceActivity.this, ReportedSuccessfullyActivity.class);
                        intent.putExtra("message", "Violence Reported Successfully!");
                        intent.putExtra("date", accidentDate);
                        startActivity(intent);
                    }
                }
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
            imageViewReportAccidentPhoto.setImageBitmap(imageBitmap);
            checkImage = true;
        } else{
            Toast.makeText(ReportViolenceActivity.this, "Failure", Toast.LENGTH_SHORT).show();
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
            StorageReference Ref = mStorageRef.child("Violences/"+accidentId);
            byte[] bytes = stream.toByteArray();
            Ref.putBytes(bytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Intent intent = new Intent(ReportViolenceActivity.this, ReportedSuccessfullyActivity.class);
                    intent.putExtra("message", "Violence Reported Successfully!");
                    intent.putExtra("date", accidentDate);
                    startActivity(intent);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(ReportViolenceActivity.this, "Exception1: "+exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception exception) {
            Toast.makeText(ReportViolenceActivity.this, "Exception2: "+exception.getMessage(), Toast.LENGTH_SHORT).show();
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