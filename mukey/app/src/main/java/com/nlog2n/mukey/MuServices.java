
package com.nlog2n.mukey;

import android.content.Context;

public class MuServices {

    private Context   mCtx;
    private MainActivity   mMainActivity;

    public MuServices(MainActivity mainAct, Context ctx)
    {
        mMainActivity = mainAct;
        mCtx = ctx;  // however, context can be derived from mainactivity.

        onCreateNative(mainAct);  // pass this object to native code

    }

    // initialization for JNI C module
    private native void onCreateNative(MainActivity mainAct);

    // password is required for modifying security policy(profile)
    public int register(String password, String policy)
    {
        return 0;
    }

    public int trustedStorage_create(int serviceNo, String key, String filebuffer)
    {
        return 0;
    }

    public int trustedStorage_get(int serviceNo, String filebuffer)
    {
        return 0;
    }

    public int trustedStorage_remove(int serviceNo)
    {
        return 0;
    }

    public int otpService_create(int serviceNo, String seed)
    {
        return 0;
    }

    public int otpService_get(int serviceNo)
    {
        return 0;
    }

    public int otpService_remove(int serviceNo)
    {
        return 0;
    }

    public native int GenOTP();


    public int aesService_create(int serviceNo, String key)
    {
        return 0;
    }

    public int aesService_get(int serviceNo, String buffer)
    {
        return 0;
    }

    public int aesService_remove(int serviceNo)
    {
        return 0;
    }

    public int hmacService_create(int serviceNo, String key)
    {
        return 0;
    }

    public int hmacService_get(int serviceNo, String buffer)
    {
        return 0;
    }

    public int hmacService_remove(int serviceNo)
    {
        return 0;
    }

    public void onDestroy() {
        // release my checking thread
        //onDestroy();   // this one got issue, crash when exiting

    }
}
