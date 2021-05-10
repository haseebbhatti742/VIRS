package virs.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ReportIncidentActivity extends AppCompatActivity {

    ImageView imageViewReportIncidentGoBack;
    RelativeLayout relativeLayoutCrime, relativeLayoutAccident, relativeLayoutHarassment, relativeLayoutViolence, relativeLayoutOther;
    Button buttonReportIncidentCrime, buttonReportIncidentAccident, buttonReportIncidentHarassment, buttonReportIncidentViolence, buttonReportIncidentOther;
    ImageView imageViewAddCrime, imageViewAddAccident, imageViewAddHarassment, imageViewAddViolence, imageViewAddOther;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_incident);
        hideSystemUI();
        setViews();
        setListeners();
    }

    private void setViews() {
        imageViewReportIncidentGoBack = findViewById(R.id.imageViewReportIncidentGoBack);
        //layouts
        relativeLayoutCrime = findViewById(R.id.relativeLayoutCrime);
        relativeLayoutAccident = findViewById(R.id.relativeLayoutAccident);
        relativeLayoutHarassment = findViewById(R.id.relativeLayoutHarassment);
        relativeLayoutViolence = findViewById(R.id.relativeLayoutViolence);
        relativeLayoutOther = findViewById(R.id.relativeLayoutOther);
        //buttons
        buttonReportIncidentCrime = findViewById(R.id.buttonReportIncidentCrime);
        buttonReportIncidentAccident = findViewById(R.id.buttonReportIncidentAccident);
        buttonReportIncidentHarassment = findViewById(R.id.buttonReportIncidentHarassment);
        buttonReportIncidentViolence = findViewById(R.id.buttonReportIncidentViolence);
        buttonReportIncidentOther = findViewById(R.id.buttonReportIncidentOther);
        //imageViews
        imageViewAddCrime = findViewById(R.id.imageViewAddCrime);
        imageViewAddAccident = findViewById(R.id.imageViewAddAccident);
        imageViewAddHarassment = findViewById(R.id.imageViewAddHarassment);
        imageViewAddViolence = findViewById(R.id.imageViewAddViolence);
        imageViewAddOther = findViewById(R.id.imageViewAddOther);
    }

    private void setListeners() {
        imageViewReportIncidentGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //layouts
        relativeLayoutCrime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ReportIncidentActivity.this, ReportCrimeActivity.class));
            }
        });

        relativeLayoutAccident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ReportIncidentActivity.this, ReportAccidentActivity.class));
            }
        });

        relativeLayoutHarassment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ReportIncidentActivity.this, ReportHarassmentActivity.class));
            }
        });

        relativeLayoutViolence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ReportIncidentActivity.this, ReportViolenceActivity.class));
            }
        });

        relativeLayoutOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        //buttons
        buttonReportIncidentCrime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ReportIncidentActivity.this, ReportCrimeActivity.class));
            }
        });

        buttonReportIncidentAccident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ReportIncidentActivity.this, ReportAccidentActivity.class));
            }
        });

        buttonReportIncidentHarassment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ReportIncidentActivity.this, ReportHarassmentActivity.class));
            }
        });

        buttonReportIncidentViolence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ReportIncidentActivity.this, ReportViolenceActivity.class));
            }
        });

        buttonReportIncidentOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        //imageViews
        imageViewAddCrime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ReportIncidentActivity.this, ReportCrimeActivity.class));
            }
        });

        imageViewAddAccident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ReportIncidentActivity.this, ReportAccidentActivity.class));
            }
        });

        imageViewAddHarassment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ReportIncidentActivity.this, ReportHarassmentActivity.class));
            }
        });

        imageViewAddViolence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ReportIncidentActivity.this, ReportViolenceActivity.class));
            }
        });

        imageViewAddOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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