package com.nlog2n.mukey;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.jar.JarFile;
import java.io.InputStream;
import java.io.IOException;

import android.content.Context;
import android.app.Activity;


/**
 * Created by fanghui on 25/9/15.
 * refer to: http://stackoverflow.com/questions/10122918/how-to-access-classes-dex-of-an-android-application
 *  http://androidcracking.blogspot.sg/2011/06/anti-tampering-with-crc-check.html
 *  http://developer.android.com/reference/dalvik/system/DexFile.html
 */


// 可参考: android apk 防止反编译技术第五篇-完整性校验
// https://m.oschina.net/blog/406860


// how to do dex file check
    //  1.  java reads CRC from a resource file
    //  2.  java computes CRC by unpacking apk file
    //  3.  build java apk
    //  4.  modify resource file

// secure way to do dex file check is like this:
    //  1. java code pass computed CRC to c function (thru jni)
    //  2. c function read another CRC from a resource file
    //  3. compare these two in C and set status


public class DexFileSignature
{
    Context myContext = null;

    DexFileSignature(Context context)
    {
        this.myContext = context;
    }

    // required dex crc value stored as a text string.
    // it could be any invisible layout element.
    // for example by modifying res/values/strings.xml file (using external tool!!)
    private long getStoredCrc()
    {
        //return 0;
        return Long.parseLong(myContext.getString(R.string.dex_crc));
    }

    // compute CRC by parsing "classes.dex" file
    private long computeCrc()
    {
        long result = 0;

        // Get the path to the apk container.
        //String apkPath = Main.MyContext.getApplicationInfo().sourceDir;
        String apkPath = myContext.getApplicationInfo().sourceDir;
        JarFile containerJar = null;

        System.out.println( "apk path: " + apkPath); // example: "/data/app/com.fanghui.antidebug-1.apk"

        try {

            // Open the apk container as a jar..
            containerJar = new JarFile(apkPath);

            // Look for the "classes.dex" entry inside the container.
            ZipEntry ze = containerJar.getEntry("classes.dex");

            // If this entry is present in the jar container
            if (ze != null) {

                // Get an Input Stream for the "classes.dex" entry
                InputStream in = containerJar.getInputStream(ze);

                // Perform read operations on the stream like in.read();
                // Notice that you reach this part of the code
                // only if the InputStream was properly created;
                // otherwise an IOException is raised

                //result = ze.getCrc();
                result = ze.hashCode();

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (containerJar != null)
                try {
                    containerJar.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        return result;
    }

    // two methods are equal
    private long computeCrc2()
    {
        long result = 0;

        //String path = Main.MyContext.getPackageCodePath();
        String path = myContext.getPackageCodePath();
        System.out.println( "package code path: " + path); // example: "/data/app/com.fanghui.antidebug-1.apk"

        try {
            ZipFile zf = new ZipFile(path);

            ZipEntry ze = zf.getEntry("classes.dex");

            //result =  ze.getCrc();
            result =  ze.hashCode();

        }catch (IOException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    public boolean validateSignature()
    {

        long storedDexCrc = getStoredCrc();
        long computedCrc = computeCrc();
        long computedCrc2 = computeCrc2();

        System.out.println( storedDexCrc + "<=>" + computedCrc + "<=>" +computedCrc2);

        // two should be equal, otherwise dex got modified
        return ( storedDexCrc == computedCrc);

    }
}
