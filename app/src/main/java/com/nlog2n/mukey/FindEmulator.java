package com.nlog2n.mukey;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.nlog2n.mukey.Property;
import com.nlog2n.mukey.Utilities;


/*
Android反调试之 AntiEmulator - 检测安卓模拟器
        多种方法检测安卓模拟器,在JAVA代码中检测当前程序是否运行在模拟器中。
        如果要更安全可以放在JNI C代码中实现.
*/

/**
 * Class used to determine functionality specific to the Android QEmu.
 *
 * @author fanghui
 */
public class FindEmulator
{
    /////////////////////////////////////////////////////////////////////////////////////

    // Method 1:  检测“/dev/socket/qemud”，“/dev/qemu_pipe”这两个pipe 文件是否存在。如存在则是在qemu模拟器中。
    private static String[] known_pipes = {"/dev/socket/qemud", "/dev/qemu_pipe"};
    /**
     * Check the existence of known pipes used by the Android QEmu environment.
     *
     * @return {@code true} if any pipes where found to exist or {@code false} if not.
     */
    public static boolean hasPipes()
    {
        //for (int i = 0; i < known_pipes.length; i++) {
        //    String pipe = known_pipes[i];

        for (String pipe : known_pipes)
        {
            File qemu_socket = new File(pipe);
            if (qemu_socket.exists())
            {
                return true;
            }
        }

        return false;
    }
    /////////////////////////////////////////////////////////////////////////////////////

    // Method 2: 检测Android QEMU模拟器上特有的几个文件
    // 注意: 第3个文件在真机samsung s6(rooted)上也有.
    private static String[] known_files = {
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace",
            "/system/bin/qemu-props"};
    /**
     * Check the existence of known files used by the Android QEmu environment.
     *
     * @return {@code true} if any files where found to exist or {@code false} if not.
     */
    public static boolean hasQEmuFiles() {
        for (String pipe : known_files) {
            File qemu_file = new File(pipe);
            if (qemu_file.exists()) {
                return true;
            }
        }

        return false;
    }


    /////////////////////////////////////////////////////////////////////////////////////

    // Method 3: 检测Genymotion模拟器特有的几个文件, 同方法2
    private static String[] known_geny_files = {"/dev/socket/genyd", "/dev/socket/baseband_genyd"};
    /**
     * Check the existence of known files used by the Genymotion environment.
     *
     * @return {@code true} if any files where found to exist or {@code false} if not.
     */
    public static boolean hasGenyFiles() {
        for (String file : known_geny_files) {
            File geny_file = new File(file);
            if (geny_file.exists()) {
                return true;
            }
        }

        return false;
    }

    /////////////////////////////////////////////////////////////////////////////////////

    // Method 4: 检测驱动文件内容，读取”/proc/tty/drivers“文件内容，然后检查已知QEmu的驱动程序的列表， 如"goldfish"

    private static String[] known_qemu_drivers = {"goldfish"};
    /**
     * Reads in the driver file, then checks a list for known QEmu drivers.
     *
     * @return {@code true} if any known drivers where found to exist or {@code false} if not.
     */
    public static boolean hasQEmuDrivers() {
        for (File drivers_file : new File[]{new File("/proc/tty/drivers"), new File("/proc/cpuinfo")})
        {
            if (drivers_file.exists() && drivers_file.canRead())
            {
                // We don't care to read much past things since info we care about should be inside here
                // The /proc filesystem is a special file system that maintains process state.
                // The contents don't exist on disk but rather in memory, and the
                // contents are highly dynamic. Because we are not opening a real file,
                // there is no easy way to determine the length, so Java returns 0 if we do
                //  byte[] data = new byte[(int) driver_file.length()];
                byte[] data = new byte[1024];

                try {
                    InputStream is = new FileInputStream(drivers_file);
                    is.read(data);
                    is.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                String driver_data = new String(data);
                for (String known_qemu_driver : FindEmulator.known_qemu_drivers) {
                    if (driver_data.indexOf(known_qemu_driver) != -1) {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    /////////////////////////////////////////////////////////////////////////////////////

    // Method 5: 检测模拟器默认的电话号码
    // Android emulator support up to 16 concurrent emulator
    // The console of the first emulator instance running on a given
    // machine uses console port 5554
    // Subsequent instances use port numbers increasing by two
    private static String[] known_numbers = {
            "15555215554", // Default emulator phone numbers + VirusTotal
            "15555215556", "15555215558", "15555215560", "15555215562", "15555215564", "15555215566",
            "15555215568", "15555215570", "15555215572", "15555215574", "15555215576", "15555215578",
            "15555215580", "15555215582", "15555215584",};

    public static boolean hasKnownPhoneNumber(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        String phoneNumber = telephonyManager.getLine1Number();

        for (String number : known_numbers) {
            if (number.equalsIgnoreCase(phoneNumber)) {
                return true;
            }

        }
        return false;
    }


    /////////////////////////////////////////////////////////////////////////////////////

    // Method 6: 检测device ID 是不是 “000000000000000”

    private static String[] known_device_ids = {
            "000000000000000", // Default emulator id
            "e21833235b6eef10", // VirusTotal id
            "012345678912345"};


    public static boolean hasKnownDeviceId(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        // 需要对exception处理,因为 Android 6.0上似乎对emulator已经禁止了获取phone state 权限.
        String deviceId = telephonyManager.getDeviceId();

        for (String known_deviceId : known_device_ids) {
            if (known_deviceId.equalsIgnoreCase(deviceId)) {
                return true;
            }

        }
        return false;
    }

    /////////////////////////////////////////////////////////////////////////////////////

    // Method 7: 检测imsi id是不是“310260000000000”

    private static String[] known_imsi_ids = {"310260000000000" // Default imsi id
    };

    public static boolean hasKnownImsi(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = telephonyManager.getSubscriberId();

        for (String known_imsi : known_imsi_ids) {
            if (known_imsi.equalsIgnoreCase(imsi)) {
                return true;
            }
        }
        return false;
    }


    /////////////////////////////////////////////////////////////////////////////////////

    // Method 8: 检测手机运营商家是否等于 "android"

    public static boolean isOperatorNameAndroid(Context paramContext) {
        String szOperatorName = ((TelephonyManager) paramContext.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkOperatorName();
        boolean isAndroid = szOperatorName.equalsIgnoreCase("android");
        return isAndroid;
    }


    /////////////////////////////////////////////////////////////////////////////////////

    // Method 9: Anti QEMU emulator by settting breakpoint, 其C代码只能运行在arm
    /*
    static {
        // This is only valid for arm
        System.loadLibrary("anti");
    }

    public native static int qemuBkpt();

    public static boolean checkQemuBreakpoint() {
        boolean hit_breakpoint = false;

        // Potentially you may want to see if this is a specific value
        int result = qemuBkpt();

        if (result > 0) {
            hit_breakpoint = true;   // emulator
        }

        return hit_breakpoint;
    }
    */
    public static boolean checkQemuBreakpoint() {
        return false;
    }





    /////////////////////////////////////////////////////////////////////////////////////

    // Method 11: 检测手机上的一些硬件属性信息

    public static boolean hasEmulatorBuild(Context context)
    {
        String BOARD = android.os.Build.BOARD; // The name of the underlying board, like "unknown".

        // This appears to occur often on real hardware... that's sad
        // String BOOTLOADER = android.os.Build.BOOTLOADER; // The system bootloader version number.

        String BRAND = android.os.Build.BRAND; // The brand (e.g., carrier) the software is customized for, if any.
        // "generic"

        String DEVICE = android.os.Build.DEVICE; // The name of the industrial design. "generic"

        String HARDWARE = android.os.Build.HARDWARE; // The name of the hardware (from the kernel command line or
        // /proc). "goldfish"

        String MODEL = android.os.Build.MODEL; // The end-user-visible name for the end product. "sdk"

        String PRODUCT = android.os.Build.PRODUCT; // The name of the overall product.

        if (       (BOARD.compareTo("unknown") == 0)
             /* || (BOOTLOADER.compareTo("unknown") == 0) */
                || (BRAND.compareTo("generic") == 0)
                || (DEVICE.compareTo("generic") == 0)
                || (MODEL.compareTo("sdk") == 0)
                || (PRODUCT.compareTo("sdk") == 0)
                || (HARDWARE.compareTo("goldfish") == 0))
        {
            return true;
        }
        return false;
    }


    /////////////////////////////////////////////////////////////////////////////////////

    // Method 12: 检测手机上的一些硬件属性信息


    /**
     * Known props, in the format of [property name, value to seek] if value to seek is null, then it is assumed that
     * the existence of this property (anything not null) indicates the QEmu environment.
     */
    private static Property[] known_props = {
            new Property("init.svc.qemud", null),
            new Property("init.svc.qemu-props", null),
            new Property("qemu.hw.mainkeys", null),
            new Property("qemu.sf.fake_camera", null),
            new Property("qemu.sf.lcd_density", null),
            new Property("ro.bootloader", "unknown"),
            new Property("ro.bootmode", "unknown"),
            new Property("ro.hardware", "goldfish"),
            new Property("ro.kernel.android.qemud", null),
            new Property("ro.kernel.qemu.gles", null),
            new Property("ro.kernel.qemu", "1"),
            new Property("ro.product.device", "generic"),
            new Property("ro.product.model", "sdk"),
            new Property("ro.product.name", "sdk"),
            // Need to double check that an "empty" string ("") returns null
            new Property("ro.serialno", null)};
    /**
     * The "known" props have the potential for false-positiving due to interesting (see: poorly) made Chinese
     * devices/odd ROMs. Keeping this threshold low will result in better QEmu detection with possible side affects.
     */
    private static int MIN_PROPERTIES_THRESHOLD = 0x5;

    /**
     * Will query specific system properties to try and fingerprint a QEmu environment. A minimum threshold must be met
     * in order to prevent false positives.
     *
     * @param context A {link Context} object for the Android application.
     * @return {@code true} if enough properties where found to exist or {@code false} if not.
     */
    public boolean hasQEmuProps(Context context) {
        int found_props = 0;

        for (Property property : known_props) {
            String property_value = Utilities.getProp(context, property.name);
            // See if we expected just a non-null
            if ((property.seek_value == null) && (property_value != null)) {
                found_props++;
            }
            // See if we expected a value to seek
            if ((property.seek_value != null) && (property_value.indexOf(property.seek_value) != -1)) {
                found_props++;
            }

        }

        if (found_props >= MIN_PROPERTIES_THRESHOLD) {
            return true;
        }

        return false;
    }
}
