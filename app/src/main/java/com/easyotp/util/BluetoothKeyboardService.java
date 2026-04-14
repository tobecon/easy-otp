package com.easyotp.util;

import android.accessibilityservice.AccessibilityService;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class BluetoothKeyboardService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Not needed for keyboard input simulation
    }

    @Override
    public void onInterrupt() {
        // Handle interrupt if needed
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        // This method is called when the service intercepts key events
        return super.onKeyEvent(event);
    }

    public void sendString(String text) {
        // This will be implemented using InputManager for Bluetooth HID
        // For now, we'll use a simpler approach with clipboard or direct input
    }
}
