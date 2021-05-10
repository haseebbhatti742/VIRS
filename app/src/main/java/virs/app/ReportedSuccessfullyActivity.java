package virs.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class ReportedSuccessfullyActivity extends AppCompatActivity {

    ImageView imageViewSuccessfullGoBack, imageViewSuccessfullHelp;
    TextView textViewSuccessfullDate, textViewSuccessfullMessage, textViewSuccessfullExit, textViewSuccessfullHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reported_successfully);
        hideSystemUI();
        setViews();
        setListeners();
    }

    private void setViews() {
        imageViewSuccessfullGoBack = findViewById(R.id.imageViewSuccessfullGoBack);
        imageViewSuccessfullHelp = findViewById(R.id.imageViewSuccessfullHelp);
        textViewSuccessfullDate = findViewById(R.id.textViewSuccessfullDate);
        textViewSuccessfullMessage = findViewById(R.id.textViewSuccessfullMessage);
        textViewSuccessfullExit = findViewById(R.id.textViewSuccessfullExit);
        textViewSuccessfullHelp = findViewById(R.id.textViewSuccessfullHelp);

        textViewSuccessfullDate.setText(getIntent().getStringExtra("date"));
        textViewSuccessfullMessage.setText(getIntent().getStringExtra("message"));
    }

    private void setListeners() {
        imageViewSuccessfullGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        textViewSuccessfullExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
                System.exit(0);
            }
        });

        textViewSuccessfullHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ReportedSuccessfullyActivity.this, HelpServiceActivity.class));
            }
        });

        imageViewSuccessfullHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ReportedSuccessfullyActivity.this, HelpServiceActivity.class));
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