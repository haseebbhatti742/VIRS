package virs.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.installations.Utils;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    EditText editTextSignupName, editTextSignupCNIC, editTextSignupCity, editTextSignupPhoneNumber, editTextSignupPasword;
    Button buttonSignUp;
    TextView textViewLinkLogin;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference usersRef = database.getReference("users");

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        hideSystemUI();
        setViews();
        setListeners();
    }

    public void setViews(){
        editTextSignupName = findViewById(R.id.editTextSignupName);
        editTextSignupCNIC = findViewById(R.id.editTextSignupCNIC);
        editTextSignupCity = findViewById(R.id.editTextSignupCity);
        editTextSignupPhoneNumber = findViewById(R.id.editTextSignupPhoneNumber);
        editTextSignupPasword = findViewById(R.id.editTextSignupPasword);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        textViewLinkLogin = findViewById(R.id.textViewLinkLogin);

        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Processing ...");
        mProgress.setMessage("Please wait...");
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
    }

    public void setListeners(){
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(editTextSignupName.getText().toString())){
                    editTextSignupName.setError("Please Enter Name");
                } else if(TextUtils.isEmpty(editTextSignupCNIC.getText().toString())){
                    editTextSignupCNIC.setError("Please Enter CNIC");
                } else if(TextUtils.isEmpty(editTextSignupCity.getText().toString())){
                    editTextSignupCity.setError("Please Enter City");
                } else if(TextUtils.isEmpty(editTextSignupPhoneNumber.getText().toString())){
                    editTextSignupPhoneNumber.setError("Please Enter Phone Number");
                } else if(TextUtils.isEmpty(editTextSignupPasword.getText().toString())){
                    editTextSignupPasword.setError("Please Enter Password");
                } else {
                    String name = editTextSignupName.getText().toString();
                    String cnic = editTextSignupCNIC.getText().toString();
                    String city = editTextSignupCity.getText().toString();
                    String phoneNumber = editTextSignupPhoneNumber.getText().toString();
                    String password = editTextSignupPasword.getText().toString();
                    signUpUser(cnic, name, city, phoneNumber, password);
                }
            }
        });

        textViewLinkLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            }
        });
    }

    private void signUpUser(String cnic, String name, String city, String phoneNumber, String password) {
        mProgress.show();
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(cnic)){
                    mProgress.hide();
                    Toast.makeText(SignupActivity.this, "CNIC Already Registered", Toast.LENGTH_LONG).show();
                    Log.d("virs123", "check true");
                } else {
                    Log.d("virs123", "check false");
                    Log.d("virs123", "inside signUpUser");
                    HashMap<String, String> map = new HashMap<>();
                    map.put("cnic", cnic);
                    map.put("name", name);
                    map.put("city", city);
                    map.put("phoneNumber", phoneNumber);
                    map.put("password", password);

                    usersRef.child(cnic).setValue(map);
                    mProgress.hide();
                    SplashActivity.session.setCNIC(cnic);
                    SplashActivity.session.setName(name);
                    SplashActivity.session.setCity(city);
                    SplashActivity.session.setPhoneNumber(phoneNumber);
                    SplashActivity.session.setSession("true");
                    Toast.makeText(SignupActivity.this, "Signup Successful", Toast.LENGTH_LONG).show();
                    Log.d("virs123", "User Registered");
                    startActivity(new Intent(SignupActivity.this, HomeActivity.class));
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