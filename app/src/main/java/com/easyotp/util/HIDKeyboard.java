package com.easyotp.util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothHidDeviceAppQosSettings;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class HIDKeyboard {
    private static final String TAG = "HIDKeyboard";

    private static BluetoothAdapter bluetoothAdapter;
    private static BluetoothHidDevice hidDevice;
    private static BluetoothDevice hostDevice;
    private static boolean connected = false;
    private static boolean registered = false;
    private static Context appContext;

    private static final byte[] HID_REPORT_DESCRIPTOR = new byte[] {
        (byte)0x05, (byte)0x01,
        (byte)0x09, (byte)0x06,
        (byte)0xA1, (byte)0x01,
        (byte)0x05, (byte)0x07,
        (byte)0x19, (byte)0xE0,
        (byte)0x29, (byte)0xE7,
        (byte)0x15, (byte)0x00,
        (byte)0x25, (byte)0x01,
        (byte)0x75, (byte)0x01,
        (byte)0x95, (byte)0x08,
        (byte)0x81, (byte)0x02,
        (byte)0x95, (byte)0x01,
        (byte)0x75, (byte)0x08,
        (byte)0x81, (byte)0x01,
        (byte)0x95, (byte)0x05,
        (byte)0x75, (byte)0x01,
        (byte)0x05, (byte)0x08,
        (byte)0x19, (byte)0x01,
        (byte)0x29, (byte)0x05,
        (byte)0x91, (byte)0x02,
        (byte)0x95, (byte)0x01,
        (byte)0x75, (byte)0x03,
        (byte)0x91, (byte)0x01,
        (byte)0x95, (byte)0x06,
        (byte)0x75, (byte)0x08,
        (byte)0x15, (byte)0x00,
        (byte)0x25, (byte)0x65,
        (byte)0x05, (byte)0x07,
        (byte)0x19, (byte)0x00,
        (byte)0x29, (byte)0x65,
        (byte)0x81, (byte)0x00,
        (byte)0xC0
    };

    private static final Map<Character, Byte> KEY_MAP = new HashMap<>();
    private static final byte MOD_SHIFT = 0x02;

    static {
        for (char c = 'a'; c <= 'z'; c++) {
            KEY_MAP.put(c, (byte)(0x04 + (c - 'a')));
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            KEY_MAP.put(c, (byte)(0x04 + (c - 'A')));
        }
        for (char c = '1'; c <= '9'; c++) {
            KEY_MAP.put(c, (byte)(0x1E + (c - '1')));
        }
        KEY_MAP.put('0', (byte)0x27);
        KEY_MAP.put(' ', (byte)0x2C);
        KEY_MAP.put('\n', (byte)0x28);
        KEY_MAP.put('\t', (byte)0x2B);
        KEY_MAP.put('-', (byte)0x2D);
        KEY_MAP.put('=', (byte)0x2E);
        KEY_MAP.put('[', (byte)0x2F);
        KEY_MAP.put(']', (byte)0x30);
        KEY_MAP.put('\\', (byte)0x31);
        KEY_MAP.put(';', (byte)0x33);
        KEY_MAP.put('\'', (byte)0x34);
        KEY_MAP.put('`', (byte)0x35);
        KEY_MAP.put(',', (byte)0x36);
        KEY_MAP.put('.', (byte)0x37);
        KEY_MAP.put('/', (byte)0x38);
    }

    private static final BluetoothProfile.ServiceListener SERVICE_LISTENER = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile != BluetoothProfile.HID_DEVICE) {
                return;
            }
            hidDevice = (BluetoothHidDevice) proxy;
            registerApp();
        }

        @Override
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = null;
                registered = false;
                connected = false;
                hostDevice = null;
            }
        }
    };

    private static final BluetoothHidDevice.Callback CALLBACK = new BluetoothHidDevice.Callback() {
        @Override
        public void onAppStatusChanged(BluetoothDevice pluggedDevice, boolean registered) {
            HIDKeyboard.registered = registered;
            Log.i(TAG, "HID app registered: " + registered);
        }

        @Override
        public void onConnectionStateChanged(BluetoothDevice device, int state) {
            connected = state == BluetoothProfile.STATE_CONNECTED;
            hostDevice = connected ? device : null;
            Log.i(TAG, "HID connection state=" + state + " device=" + device);
        }
    };

    @SuppressLint("MissingPermission")
    public static void initialize(Context context) {
        if (appContext != null) {
            return;
        }
        appContext = context.getApplicationContext();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.w(TAG, "Bluetooth not supported");
            return;
        }
        bluetoothAdapter.getProfileProxy(appContext, SERVICE_LISTENER, BluetoothProfile.HID_DEVICE);
    }

    public static boolean isBluetoothKeyboardConnected() {
        return connected && hostDevice != null;
    }

    @SuppressLint("MissingPermission")
    public static void sendStringViaInput(String text) {
        if (!isBluetoothKeyboardConnected() || hidDevice == null || hostDevice == null) {
            return;
        }
        for (char c : text.toCharArray()) {
            sendChar(c);
        }
        sendKey((byte)0, (byte)0); // release all after sending
    }

    @SuppressLint("MissingPermission")
    private static void sendChar(char c) {
        boolean shift = Character.isUpperCase(c);
        char lower = Character.toLowerCase(c);
        Byte keyCode = KEY_MAP.get(lower);
        if (keyCode == null) {
            return;
        }
        byte modifier = shift ? MOD_SHIFT : 0;
        sendKey(modifier, keyCode);
        sendKey((byte)0, (byte)0);
    }

    @SuppressLint("MissingPermission")
    private static void sendKey(byte modifier, byte keyCode) {
        if (hidDevice == null || hostDevice == null) {
            return;
        }
        byte[] report = new byte[8];
        report[0] = modifier;
        report[1] = 0;
        report[2] = keyCode;
        report[3] = 0;
        report[4] = 0;
        report[5] = 0;
        report[6] = 0;
        report[7] = 0;
        hidDevice.sendReport(hostDevice, 0, report);
    }

    private static void registerApp() {
        if (hidDevice == null || registered) {
            return;
        }
        BluetoothHidDeviceAppSdpSettings sdp = new BluetoothHidDeviceAppSdpSettings(
                "EasyOTP Keyboard",
                "Bluetooth keyboard for OTP input",
                "EasyOTP",
                BluetoothHidDevice.SUBCLASS1_COMBO,
                HID_REPORT_DESCRIPTOR
        );
        hidDevice.registerApp(sdp, null, null, appContext.getMainExecutor(), CALLBACK);
    }
}
