package com.nlog2n.mukey;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.util.Log;


import java.io.File;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;


/*
AppOdexCheck

On Android, the bytecode of an application is optimized into an odex file specific 
to the platform it is running on. The optimization set speeds up the boot process, 
as it preloads part of an application. If an odex file exists for an application, 
it is loaded instead of the bytecode included in the application package. odex 
bytecode are not subject to Android signature mechanism, and attackers can abuse 
this mechanism to modify an application bytecode without altering the application 
package.

This detection mechanism verifies that the odex file that is stored on the disk 
has not been altered. To check if the dalvik-cache has not been altered, it 
generates temporarily an optimized version of the package application in 
files/opt.tmp by invoking /system/bin/dexopt in libcheck.so and compares the 
digest of output file with the one already stored in the dalvik-cache. If the 
digest is different, the optimized version of the application may have been modified.
*/


public class DexOptCheck{

    private Context mCtx;
    private static final String TAG = "DexCheck";

    private native int runDexOpt(String apkPath, String dexOptPath);

    public DexOptCheck(Context ctx)
    {
        mCtx = ctx;
    }


    public static char[] encodeHex(byte[] data) {
        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[data.length * 2];
        int v;
        for (int j = 0; j < data.length; j++) {
            v = data[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return hexChars;
    }


    public static String getSHA256Hash(File f) throws Exception {
        String hash = "";
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(f));

                final byte[] buffer = new byte[1024];
                for (int read = 0; (read = is.read(buffer)) != -1;) {
                    messageDigest.update(buffer, 0, read);
                }
            } finally {
                if (is != null) {
                    is.close();
                }
            }

            hash = new String(encodeHex(messageDigest.digest()));
        } catch (NoSuchAlgorithmException e) {
            throw new Exception(e);
        } catch (FileNotFoundException e) {
            throw new Exception(e);
        } catch (IOException e) {
            throw new Exception(e);
        }

        return hash;
    }


    public static String apphash(final String pkgSrcDir)
    {
        final File a = new File(pkgSrcDir);
        String hash = "";
        try {
            hash = getSHA256Hash(a);
        } catch (Exception e) {
            Log.e(TAG, TAG, e);
        }
        return hash;
    }

    public static boolean isARTRuntime() {
        String runtimeVersion = System.getProperty("java.vm.version");
        char first = runtimeVersion.charAt(0);
        return (Character.getNumericValue(first) >= 2 ? true : false);
    }

    public static boolean isSignedWithDebugCert(Context ctx) {
        boolean debuggable = false;

        X500Principal DEBUG_DN = new X500Principal("CN=Android Debug,O=Android,C=US");

        try {
            PackageInfo pinfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), PackageManager.GET_SIGNATURES);

            Signature signatures[] = pinfo.signatures;
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            for (int i = 0; i < signatures.length; i++) {
                ByteArrayInputStream stream = new ByteArrayInputStream(signatures[i].toByteArray());
                X509Certificate cert = (X509Certificate) cf.generateCertificate(stream);
                debuggable = DEBUG_DN.equals(cert.getSubjectX500Principal());
                if (debuggable)
                    break;
            }
        } catch (NameNotFoundException e) {
            // debuggable variable will remain false
            Log.e(TAG, TAG, e);
        } catch (CertificateException e) {
            // debuggable variable will remain false
            Log.e(TAG, TAG, e);
        }
        return debuggable;
    }


    public boolean check()
    {
        try {
            // 如果是ART模式则不做检查
            if (isARTRuntime()) {
                Log.i(TAG, "App Odex Check: in ART mode");
                return true;
            }

            // 如果是debugging模式也不做检查
            if (isSignedWithDebugCert(mCtx)) {
                Log.i(TAG, "App Odex Check: in dev mode");
                return true;
            }


            if (!isOdexVerified()) {
                return false; // dex integrity check failed
            }
        }
        catch (Exception e) {
            Log.e(TAG, TAG, e);
        }

        return true;
    }

    private boolean isOdexVerified() throws Exception {
        final PackageManager pkgMgr = mCtx.getPackageManager();
        final String pkgName = mCtx.getPackageName();
        final PackageInfo pkgInfo = pkgMgr.getPackageInfo(pkgName, 0);

        final String pkgDir = pkgInfo.applicationInfo.sourceDir;
        final String dataDir = pkgInfo.applicationInfo.dataDir;


        // 找是否odex file已安装到指定目录
        // 优化发生的时机有两个：
        // 对于预置应用，可以在系统编译后生成优化文件，以ODEX 结尾。这样在发布时除
        // APK 文件（不包含 DEX）以外，还有一个相应的 ODEX 文件；
        // 对于非预置应用，包含在 APK 文件里的 DEX 文件会在运行时通过dexopt进行优化，
        // 优化后的文件将被保存在缓存中(data/dalvik-cache)。
        String odexFilePath = null;
        {
            // apk pkg path: /data/app/com.nlog2n.mukey-1.apk
            String[] apkfile = pkgDir.split("/");
            String apkname = apkfile[3];  // TODO: instead use getPackageName()

            // check if any odex file in "/data/app" or "/data/dalvik-cache/"
            String odex_loc = pkgDir.substring(0, pkgDir.length() - 4) + ".odex";
            String odex_loc1 = "/data/dalvik-cache/data@app@" + apkname + "@classes.dex";
            File odex = new File(odex_loc);
            File odex1 = new File(odex_loc1);
            if (odex.exists()) {
                odexFilePath = odex_loc;
            } else if (odex1.exists()) {
                odexFilePath = odex_loc1;
            } else {
                // skip the check if can't find any Odex file
                // this is to cater conditions such as ART implementation, which yet to explore
                return true; // still regarded as OK
            }
        }

        // 利用dexopt生成自己的odex file
        //execl /system/bin/dexopt  zipfile=/data/app/com.nlog2n.mukey-1.apk odexfile=/data/data/com.nlog2n.mukey/files/opt.tmp
        // DexOpt: load 26ms, verify+opt 89ms, 2034252 bytes
        // 费时大约100毫秒
        String dexOptTempPath = dataDir + "/files/tmp.odex";
        File dexOptTempFile = new File(dexOptTempPath);
        deleteDexOptTemp(dexOptTempFile);
        if (runDexOpt(pkgDir, dexOptTempPath) != 0) // dexopt failed
        {
            // skip the check if failed to perform dexOpt
            // this is to cater conditions such as ART implementation, which yet to explore
            Log.e(TAG, "dexOpt process failed");
            return true;
        }

        // 比较两者是否一致
        String installedOdexSHA = null;
        String generatedOdexSHA = null;
        installedOdexSHA = apphash(odexFilePath);
        generatedOdexSHA = apphash(dexOptTempPath);
        if (installedOdexSHA != null && generatedOdexSHA != null) {
            Log.i(TAG, "Hash: cached odex=" + installedOdexSHA + ", generated odex=" + generatedOdexSHA);
            if (!installedOdexSHA.equals(generatedOdexSHA))
            {
                Log.e(TAG, "odex cache check failed.");
                return false; // integrity check failed
            }
        }

        return true;
    }
    
    private void deleteDexOptTemp(File f) {
        if (f.exists()) {
            Log.i(TAG, "deleting dex opt temp file");
            f.delete();
        }
    }
}
