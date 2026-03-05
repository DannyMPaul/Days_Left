# Overlay Not Appearing - Debug Guide

## Issue
The overlay is not appearing when opening apps like YouTube, Chrome, etc.

## Most Likely Causes

### 1. Trigger Mode Set to "First App of Day"
**This is the default setting** and the most common reason.

- Overlay only shows on the **first app you open each day**
- If you already opened an app today, it won't show again until tomorrow
- Check your current setting in the app

### 2. How to Test

**Step 1: Change Trigger Mode**
1. Open Days Left app
2. Click Settings
3. Under "Trigger Mode", select **"Show on every app launch"**
4. Go back to main screen

**Step 2: Open Any App**
1. Open YouTube, Chrome, or any app
2. Overlay should appear immediately

**Step 3: Check Logcat (if using Android Studio)**
1. Connect phone via USB
2. Open Logcat in Android Studio
3. Filter by: `AccessibilityService`
4. Open an app
5. Look for these messages:
   - `"App launched: com.google.android.youtube"` ✓ Service is working
   - `"Showing normal overlay"` ✓ Overlay is being triggered
   - `"Trigger conditions not met"` ✗ Trigger mode blocking it
   - `"Overlay permission not granted"` ✗ Permission issue

### 3. Other Possible Issues

**App is Excluded:**
- Go to Settings → Manage Excluded Apps
- Make sure YouTube/Chrome are not in the list

**Accessibility Service Not Running:**
- Go to phone Settings → Accessibility
- Make sure "Days Left" service is ON
- Try toggling it OFF and ON again

**Overlay Permission Issue:**
- Go to phone Settings → Apps → Days Left → Display over other apps
- Make sure it's enabled

## Quick Fix

**Force trigger by changing mode:**
1. Settings → Trigger Mode → "Show on every app launch"
2. Open any app
3. Should work immediately

If still not working, check Logcat logs to see exact error.
