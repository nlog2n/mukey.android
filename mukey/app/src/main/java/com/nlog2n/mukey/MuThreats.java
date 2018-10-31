package com.nlog2n.mukey;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import com.nlog2n.mukey.FindEmulator;
import com.nlog2n.mukey.FindTaint;
import com.nlog2n.mukey.DexOptCheck;

public class MuThreats {

    Context myContext = null;
    Activity myActivity = null;
    MuThreats(Activity activity, Context context)
    {
        this.myActivity = activity;
        this.myContext = context;
    }


    private native String printAppStatus();


    public String getStatus() {
        String status = "";

        // native 各项检测
        status +=  printAppStatus();

        try {

            // root 检测
            //if ( ShellUtils.checkRootPermission() ) {
            //    status +=  "root permission is allowed.\n";
            //}

            // 模拟器检测
            status += "qemu environment: " + isQEmuEnvDetected();  // is emulator or not
            status += "\nmonkey user: " + isUserAMonkey();
            status += "\ntaint droid: " + isTaintTrackingDetected();

            // APK dex 代码签名
            DexFileSignature dex = new DexFileSignature(myContext);
            dex.validateSignature();

            // APK DexOpt integrity 检查
            // solved:  native run dexopt还有问题,导致异常退出,可能需要放在forked process中运行!
            DexOptCheck  dexopt = new DexOptCheck(myContext);
            status += "\ndexopt check: " + dexopt.check();

            // Mock地理位置检测
            MockLocationCheck mMockLocationChecker = new MockLocationCheck(myContext);
            status += "\nmock location: " + mMockLocationChecker.isMockLocationEnabled();
            status += "\ngps enabled: " + mMockLocationChecker.isGPSEnabled(); // 需要权限 ACCESS_FINE_LOCATION
            // about to remove mock location provider
            // mMockLocationChecker.removeTestProvider(); // 需要权限 ACCESS_MOCK_LOCATION, only for debug


            // 禁止截屏
            AntiScreenShot mBlockScreenShot = new AntiScreenShot(myActivity);
            mBlockScreenShot.blockScreenShot();

        }catch (Exception ex)
        {}

        return status;
    }



    public boolean isQEmuEnvDetected() {
        log("Checking for QEmu env...");
        log("hasKnownDeviceId : " + FindEmulator.hasKnownDeviceId(myContext));
        log("hasKnownPhoneNumber : " + FindEmulator.hasKnownPhoneNumber(myContext));
        log("isOperatorNameAndroid : " + FindEmulator.isOperatorNameAndroid(myContext));
        log("hasKnownImsi : " + FindEmulator.hasKnownImsi(myContext));
        log("hasEmulatorBuild : " + FindEmulator.hasEmulatorBuild(myContext));
        log("hasPipes : " + FindEmulator.hasPipes());
        log("hasQEmuDriver : " + FindEmulator.hasQEmuDrivers());
        log("hasQEmuFiles : " + FindEmulator.hasQEmuFiles());
        log("hasGenyFiles : " + FindEmulator.hasGenyFiles());
        log("hitsQemuBreakpoint : " + FindEmulator.checkQemuBreakpoint());

        return (FindEmulator.hasKnownDeviceId(myContext)
                || FindEmulator.hasKnownImsi(myContext)
                || FindEmulator.hasEmulatorBuild(myContext)
                || FindEmulator.hasKnownPhoneNumber(myContext)
                || FindEmulator.hasPipes()
                || FindEmulator.hasQEmuDrivers()
                || FindEmulator.hasQEmuFiles()
                || FindEmulator.hasGenyFiles());
    }

    public boolean isTaintTrackingDetected() {
        log("Checking for Taint tracking...");
        log("hasAppAnalysisPackage : " + FindTaint.hasAppAnalysisPackage(myContext));
        log("hasTaintClass : " + FindTaint.hasTaintClass());
        log("hasTaintMemberVariables : " + FindTaint.hasTaintMemberVariables());

        return (FindTaint.hasAppAnalysisPackage(myContext)
                || FindTaint.hasTaintClass()
                || FindTaint.hasTaintMemberVariables());
    }


    public  boolean isUserAMonkey()
    {
        // check if it is for Monkey user
        return ActivityManager.isUserAMonkey();
    }


    public void log(String msg) {
        Log.v("MuThreats", msg);
    }
}
