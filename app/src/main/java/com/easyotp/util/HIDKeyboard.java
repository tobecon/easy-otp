package com.easyotp.util;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import java.util.ArrayList;

public class HIDKeyboard {
    
    // USB HID Keyboard usage codes
    private static final byte KEY_A = 0x04;
    private static final byte KEY_B = 0x05;
    private static final byte KEY_C = 0x06;
    private static final byte KEY_D = 0x07;
    private static final byte KEY_E = 0x08;
    private static final byte KEY_F = 0x09;
    private static final byte KEY_G = 0x0A;
    private static final byte KEY_H = 0x0B;
    private static final byte KEY_I = 0x0C;
    private static final byte KEY_J = 0x0D;
    private static final byte KEY_K = 0x0E;
    private static final byte KEY_L = 0x0F;
    private static final byte KEY_M = 0x10;
    private static final byte KEY_N = 0x11;
    private static final byte KEY_O = 0x12;
    private static final byte KEY_P = 0x13;
    private static final byte KEY_Q = 0x14;
    private static final byte KEY_R = 0x15;
    private static final byte KEY_S = 0x16;
    private static final byte KEY_T = 0x17;
    private static final byte KEY_U = 0x18;
    private static final byte KEY_V = 0x19;
    private static final byte KEY_W = 0x1A;
    private static final byte KEY_X = 0x1B;
    private static final byte KEY_Y = 0x1C;
    private static final byte KEY_Z = 0x1D;
    
    private static final byte KEY_1 = 0x1E;
    private static final byte KEY_2 = 0x1F;
    private static final byte KEY_3 = 0x20;
    private static final byte KEY_4 = 0x21;
    private static final byte KEY_5 = 0x22;
    private static final byte KEY_6 = 0x23;
    private static final byte KEY_7 = 0x24;
    private static final byte KEY_8 = 0x25;
    private static final byte KEY_9 = 0x26;
    private static final byte KEY_0 = 0x27;
    
    private static final byte KEY_ENTER = 0x28;
    private static final byte KEY_ESC = 0x29;
    private static final byte KEY_BACKSPACE = 0x2A;
    private static final byte KEY_TAB = 0x2B;
    private static final byte KEY_SPACE = 0x2C;
    
    private static final byte KEY_LEFT_SHIFT = 0xE1;
    
    public static boolean isBluetoothKeyboardConnected() {
        int[] deviceIds = InputDevice.getDeviceIds();
        for (int deviceId : deviceIds) {
            InputDevice device = InputDevice.getDevice(deviceId);
            if (device != null && 
                (device.getSources() & InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD) {
                String deviceName = device.getName();
                if (deviceName.toLowerCase().contains("bluetooth") || 
                    deviceName.toLowerCase().contains("keyboard")) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static void sendStringViaInput(String text) {
        try {
            long now = System.currentTimeMillis();
            ArrayList<KeyEvent> events = new ArrayList<>();
            
            KeyCharacterMap kcm = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
            KeyEvent[] keyEvents = kcm.getEvents(text.toCharArray());
            
            if (keyEvents != null) {
                for (KeyEvent event : keyEvents) {
                    event.setDownTime(now);
                    event.setEventTime(now);
                    
                    // Send key down
                    android.app.Instrumentation inst = new android.app.Instrumentation();
                    inst.sendKeyDownUpSync(event.getKeyCode());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
