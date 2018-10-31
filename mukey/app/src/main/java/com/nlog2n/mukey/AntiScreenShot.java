package com.nlog2n.mukey;

import android.app.Activity;
import android.view.WindowManager;


public class AntiScreenShot
{
    private  Activity mActivity;

    public AntiScreenShot(Activity a) {
        mActivity = a;
    }

    public void blockScreenShot()
    {
        //该界面禁止截屏
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }

}
