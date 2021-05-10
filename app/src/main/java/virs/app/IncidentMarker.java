package virs.app;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class IncidentMarker {
    private MarkerOptions markerOptions;
    private Marker marker;
    LatLng latLng;
    private String date;
    private String time;
    private String incidentType;

    public IncidentMarker(MarkerOptions markerOptions, Marker marker, LatLng latLng, String date, String time, String incidentType) {
        this.markerOptions = markerOptions;
        this.marker = marker;
        this.latLng = latLng;
        this.date = date;
        this.time = time;
        this.incidentType = incidentType;
    }

    public MarkerOptions getMarkerOptions() {
        return markerOptions;
    }

    public void setMarkerOptions(MarkerOptions markerOptions) {
        this.markerOptions = markerOptions;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }
}
