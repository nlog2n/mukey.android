package com.nlog2n.mukey;

import android.content.Context;

import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import android.location.Location;
import android.location.LocationManager;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;

import android.util.Log;

import java.util.List;


// First we can check whether MockSetting option is turned ON   (warning)

// Second we can check whether are there other apps in the device ,
// which are using android.permission.ACCESS_MOCK_LOCATION . ( Location Spoofing Apps)
//  如果以上两条满足,则认为必然有location spoof发生.但第一条可能只作为warning

// Thirdly, We can remove the test provider before requesting the location updates
// from both the providers (Network and GPS)


public class MockLocationCheck {

    private  Context mCtx;

    public MockLocationCheck(Context ctx) {
        mCtx = ctx;
    }

    // check if the device enables Mock Locations.
    // 考虑将其转变成JNI function
    public boolean isMockLocationEnabled() {
        boolean isMockLocationEnabled = false;
        try {
            final int allowMockLocationSetting = Settings.Secure.getInt(mCtx.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION); 
            isMockLocationEnabled = (allowMockLocationSetting == 1);
        } catch (SettingNotFoundException e) {
            Log.e("mukey", "get mock location status failed.", e);
        }
        return isMockLocationEnabled;
    }

    // Since API 18, the object Location has the method .isFromMockProvider() so you can filter out fake locations.
    // but if you want to support versions before 18, checking setting permission above is the choice.

    // in Android 6.0 ALLOW_MOCK_LOCATION is deprecated. And actually there's no checkbox for
    // mock location as well. One can check if location is fake or not right from
    // location object: location.isFromMockProvider()
    public boolean isLocationFromMockProvider(Location loc)
    {
        boolean isMock = false;
        if (android.os.Build.VERSION.SDK_INT >= 18) {
            isMock = loc.isFromMockProvider();
        } else {
            isMock = Settings.Secure.getString(mCtx.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0");
        }

        return isMock;
    }


    public boolean areThereMockPermissionApps() {

        int count = 0;

        PackageManager pm = mCtx.getPackageManager();
        List<ApplicationInfo> packages =
                pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            try {
                PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName, PackageManager.GET_PERMISSIONS);

                // Get Permissions
                String[] requestedPermissions = packageInfo.requestedPermissions;

                if (requestedPermissions != null) {
                    for (int i = 0; i < requestedPermissions.length; i++) {
                        if (requestedPermissions[i]
                                .equals("android.permission.ACCESS_MOCK_LOCATION")
                                && !applicationInfo.packageName.equals(mCtx.getPackageName())) {
                            count++;
                        }
                    }
                }
            } catch (NameNotFoundException e) {
                Log.e("mukey", "check package info failed.",  e);
            }
        }

        if (count > 0)
            return true;
        return false;
    }




    public boolean isGPSEnabled()
    {
        boolean isGPSOn = false;
        try {
            LocationManager locationManager = (LocationManager) mCtx.getSystemService(Context.LOCATION_SERVICE);
            isGPSOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex){
            Log.e("mukey", "check GPS status failed.", ex);
        }

        return isGPSOn;
    }

    // TODO: as a service
    public String getRealLocation()
    {
        return null;
    }

    // Spoofing can be avoided by using Location Manager’s API and removeTestProvider() method
    // (Removes the mock location provider with the given name) as shown below.
    // Note:
    // must have android.permission.ACCESS_MOCK_LOCATION permission for removeTestProvider to work
    public void removeTestProvider()
    {
        try {
            LocationManager lm = (LocationManager) mCtx.getSystemService(mCtx.LOCATION_SERVICE);
            Log.d("mukey" ,"Removing Test providers");
            lm.removeTestProvider(LocationManager.GPS_PROVIDER);

            // You can remove the test provider before requesting the location updates from both providers: Network and GPS.
            // lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        } catch (IllegalArgumentException error) {
            Log.d("mukey","Got exception in removing test provider");
        }
    }
}

// http://blog.geomoby.com/2015/01/25/how-to-avoid-getting-your-location-based-app-spoofed/
// You can try to use a range of anti-spoofing measures using a list of the below measures:
// 1. Check the general location of cell towers (very low battery drain):
//   you could check to see if the current cell tower matches the location given and apply a margin error
// 2. Use speed of the device: maximum and minimum speed limits may apply.
// 3. Corroborating measures: cross referencing WI-FI SSID’s received with your location database
//   and validate IP address of your mobile users.
