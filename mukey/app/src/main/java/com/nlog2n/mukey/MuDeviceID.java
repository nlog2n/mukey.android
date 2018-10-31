package com.nlog2n.mukey;

import android.content.Context;

import android.provider.Settings;

import android.telephony.TelephonyManager;

import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;

//import java.net.NetworkInterface;
//import java.net.InetAddress;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.bluetooth.BluetoothAdapter;

// Android设备ID目前没有native code可以得到，但可以通过java method取到.
//   1. to get IMEI
//   2. to get device unique ID
//   3. to get wifi mac address
//   4. to get bluetooth address
// refer to:
// http://stackoverflow.com/questions/1972381/how-to-get-the-devices-imei-esn-programmatically-in-android
// http://android-developers.blogspot.sg/2011/03/identifying-app-installations.html
// http://programminglinuxblog.blogspot.pt/2011/06/mac-address-string-to-byte.html


// output for Samsung galaxy S6:
//  GSM Imei = 359878063862284
//  device id = a5ccfb97d3c1296d
//  wifi mac = E8:50:8B:B5:0A:56


public class MuDeviceID
{
    Context myContext = null;

    MuDeviceID(Context context)
    {
        this.myContext = context;
    }


    public String  getAndroidDeviceId()
    {
        String ssss = "";
        ssss += getImei();
        ssss += "\nDeviceId=" + getDeviceId();
        ssss += "\nWifMacAddr=" + getMacAddress();
        ssss += "\nBluetoothAddr=" + getBluetoothAddr();

        //ssss += "\n" + getMacAddress2().toString();

        ssss += "\n\nMD5="  + getMd5(ssss);

        return ssss;
    }

    // The result has 32 hex digits and it looks like this:
    // 9DDDF85AFF0A87974CE4541BD94D5F55
    private String getMd5(String ss)  {
        // compute md5
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        }catch (Exception ex)
        {
            return "";
        }
        m.update(ss.getBytes(),0,ss.length());
        // get md5 bytes
        byte p_md5Data[] = m.digest();
        // create a hex string
        String m_szUniqueID = new String();
        for (int i=0;i<p_md5Data.length;i++) {
            int b =  (0xFF & p_md5Data[i]);
            // if it is a single digit, make sure it have 0 in front (proper padding)
            if (b <= 0xF) m_szUniqueID+="0";
            // add number to string
            m_szUniqueID+=Integer.toHexString(b);
        }
        // hex string to uppercase
        m_szUniqueID = m_szUniqueID.toUpperCase();
        return m_szUniqueID;
    }


    //  assume we already got context as this
    private  String getImei()
    {
// method 1: 从 TelephonyManager class 的 String getDeviceId() 方法得到.
// it returns (IMEI on GSM, or MEID/ESN for CDMA).
// 读手机设备的IMEI号码（15位char string）
// 但该方法需要权限 READ_PHONE_STATE
// You'll need the following permission in your AndroidManifest.xml:
//    <uses-permission android:name="android.permission.READ_PHONE_STATE" />

        String phoneType = "";
        String imei = "";
        try {
            TelephonyManager telephony = (TelephonyManager) (myContext.getSystemService(Context.TELEPHONY_SERVICE));
            if (telephony != null) {
                imei = telephony.getDeviceId();
            }

            if (imei == null) {
                imei = "";
            }

            // further get phone type
            int ptype = telephony.getPhoneType();
            switch (ptype) {
                case TelephonyManager.PHONE_TYPE_NONE:
                    phoneType = "NONE: ";
                    break;

                case TelephonyManager.PHONE_TYPE_GSM:
                    phoneType = "GSM IMEI:";
                    break;

                case TelephonyManager.PHONE_TYPE_CDMA:
                    phoneType = "CDMA MEID/ESN:";
                    break;

    /*
     *  for API Level 11 or above
     *  case TelephonyManager.PHONE_TYPE_SIP:
     *   return "SIP";
     */
                default:
                    phoneType = "UNKNOWN:";
                    break;
            }
        }
        catch (Exception ex)
        {

        }

        return  phoneType + imei;
    }

    private  String getDeviceId() {
        String identifier = "";
        // 转用第2种方法: unique device id, 8 byte shown as hex string
        // 该方法不需要权限， 该值可能雷同，或被其他应用改变
        try {
            identifier = Settings.Secure.getString(myContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        catch (Exception ex)
        {

        }
        return identifier;   // identifier.getBytes();    // return byte[]
    }

    // 该方法不需要Wifi打开,但需要权限: android.permission.ACCESS_WIFI_STATE
    private  String getMacAddress() {
        String macAddr = "";
        try {
            WifiManager manager = (WifiManager) myContext.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = manager.getConnectionInfo();
            macAddr = info.getMacAddress();   // .replace(":", "");
            //macAddr.getBytes();    // getBytes("UTF8");
        }
        catch (Exception ex)
        {
        }
        return macAddr;
    }

    /*
    private byte[] getMacAddress2()
    {
        byte[] macAddr = null;
        try {
            InetAddress inet = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(inet);
            macAddr = ni.getHardwareAddress();
        }
        catch (Exception ex)
        {}
        return macAddr;
    }
    */

    // The BT MAC Address string, available on Android devices with Bluetooth,
    // can be read if your project has the android.permission.BLUETOOTH permission.
    // 不需要蓝牙打开,但需要权限
    private String getBluetoothAddr()
    {
        String szBTMAC = "";
        try {
            BluetoothAdapter m_BluetoothAdapter = null; // Local Bluetooth adapter
            m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            szBTMAC = m_BluetoothAdapter.getAddress();
        }catch (Exception ex)
        {}

        return szBTMAC;
    }

    public static byte[] parseMacAddress(String macAddress) {
        String[] bytes = macAddress.split(":");
        byte[] parsed = new byte[bytes.length];

        for (int x = 0; x < bytes.length; x++) {
            BigInteger temp = new BigInteger(bytes[x], 16);
            byte[] raw = temp.toByteArray();
            parsed[x] = raw[raw.length - 1];
        }
        return parsed;
    }

}
