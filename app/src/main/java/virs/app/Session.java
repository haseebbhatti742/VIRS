package virs.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Session {

    private SharedPreferences prefs;

    public Session(Context cntx) {
        prefs = PreferenceManager.getDefaultSharedPreferences(cntx);
    }

    public void setSession(String session) {
        prefs.edit().putString("session", session).commit();
    }

    public String getSession() {
        return prefs.getString("session","");
    }

    public void setCNIC(String cnic) {
        prefs.edit().putString("cnic", cnic).commit();
    }

    public String getCNIC() {
        return prefs.getString("cnic","");
    }

    public void setName(String name) {
        prefs.edit().putString("name", name).commit();
    }

    public String getName() {
        return prefs.getString("name","");
    }

    public void setCity(String city) {
        prefs.edit().putString("city", city).commit();
    }

    public String getCity() {
        return prefs.getString("city","");
    }

    public void setPhoneNumber(String phone) {
        prefs.edit().putString("phone", phone).commit();
    }

    public String getPhoneNumber() {
        return prefs.getString("phone","");
    }

}
