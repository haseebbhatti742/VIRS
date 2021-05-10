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

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    EditText editTextLoginCNIC, editTextLoginPassword;
    Button buttonLogin;
    TextView textViewLinkSignup;
    private ProgressDialog mProgress;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference usersRef = database.getReference("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        hideSystemUI();
        setViews();
        setListeners();
    }

    public void setViews(){
        editTextLoginCNIC = findViewById(R.id.editTextLoginCNIC);
        editTextLoginPassword = findViewById(R.id.editTextLoginPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewLinkSignup = findViewById(R.id.textViewLinkSignup);

        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Processing ...");
        mProgress.setMessage("Please wait...");
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
    }

    public void setListeners(){
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(editTextLoginCNIC.getText().toString())){
                    editTextLoginCNIC.setError("Please Enter CNIC");
                } else if(TextUtils.isEmpty(editTextLoginPassword.getText().toString())){
                    editTextLoginPassword.setError("Please Enter Password");
                } else {
                    String cnic = editTextLoginCNIC.getText().toString();
                    String password = editTextLoginPassword.getText().toString();
                    LoginUser(cnic, password);
                }
            }
        });

        textViewLinkSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });
    }

    private void LoginUser(String cnic, String password) {
        mProgress.show();
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(cnic)){
                    if(snapshot.child(cnic).child("password").getValue().toString().equals(password)){
                        mProgress.hide();
                        SplashActivity.session.setCNIC(cnic);
                        SplashActivity.session.setName(snapshot.child(cnic).child("name").getValue().toString());
                        SplashActivity.session.setCity(snapshot.child(cnic).child("city").getValue().toString());
                        SplashActivity.session.setPhoneNumber(snapshot.child(cnic).child("phoneNumber").getValue().toString());
                        SplashActivity.session.setSession("true");
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    } else {
                        mProgress.hide();
                        Toast.makeText(LoginActivity.this, "Incorrect Password", Toast.LENGTH_LONG).show();
                    }
                } else {
                    mProgress.hide();
                    Toast.makeText(LoginActivity.this, "User Not Found", Toast.LENGTH_LONG).show();
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

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
        // super.onBackPressed();
        // Not calling **super**, disables back button in current screen.
    }

}