# Days Left

A simple Android app that shows you how many days are left in the year. Every time you unlock your phone or open an app, it displays a grid of dots representing each day - helping you visualize your time and stay mindful of how you spend it.

## What it does

- Shows a 52x7 grid where each dot is one day of the year
- Current day is highlighted in blue
- You can mark past days as good (white) or bad (red)
- Appears automatically when you unlock your phone or open your first app
- Everything stays on your device - no internet, no tracking

## How to build

1. Open the project in Android Studio
2. Click Build → Build APK(s)
3. Find your APK in `app/build/outputs/apk/debug/`
4. Install it on your phone

## Permissions needed

- **Display over other apps** - to show the overlay
- **Accessibility service** - to detect when you open apps (only checks app names, nothing else)

Your data never leaves your phone. No internet permission, no tracking, completely private.

## Tech stack

- Kotlin
- Room database
- DataStore for preferences
- Custom views for the dot grid
- Accessibility service for app detection

That's it. Simple app to help you be more aware of your time.
