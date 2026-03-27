# Days Left: A Mindful Year

## What is this?

Days Left is a simple Android application designed to give you a constant, subtle awareness of the passage of time. It visualizes the year as a grid of 52 by 7 dots, where each dot represents a day. The goal isn't to make you anxious about time running out, but to help you be more mindful of how you spend your days.

Every time you unlock your phone or open an app, it briefly shows you the grid. It's a quiet reminder to make the most of your time.

## Core Features

- **Year-at-a-Glance:** A clean 52x7 grid visualizes all the days in the year.
- **Daily Progress:** The current day is highlighted, showing you where you are in the year.
- **Reflect on Your Days:** You can tap on past days to mark them. The app doesn't define what "good" or "bad" means - that's up to you. It's a simple tool for personal reflection.
- **Seamless Integration:** The overlay appears for a moment when you unlock your device or switch to a new app, then fades away. It's designed to be a fleeting, non-intrusive reminder.
- **Fully Offline and Private:** The app has no internet permission. All your data and reflections are stored only on your device and are never sent anywhere.

## How It Works

The app uses a combination of Android features to deliver its experience:

1.  **The Overlay:** It uses the `SYSTEM_ALERT_WINDOW` permission to draw the grid of dots over other applications. This is the core of the "show on unlock" feature.
2.  **App-Open Detection:** An `AccessibilityService` is used to detect when a new application window is opened. This is how it knows when to trigger the overlay. The service only looks at the event type and does not read any content from your screen or track your usage patterns.
3.  **Data Persistence:** Your reflections (the marked days) are saved on your device using `androidx.datastore:datastore-preferences`, a modern and efficient way to store small amounts of data.

## Tech Stack and Architecture

This project is a native Android application written entirely in **Kotlin**. It follows modern Android development practices.

- **UI:** The UI is built using Android's traditional View system with `ViewBinding` to avoid `findViewById` boilerplate. The dot grid itself is a custom view.
- **Architecture:** The app uses a basic MVVM (Model-View-ViewModel) like architecture.
  - `Activity`: Manages the UI and observes data from the `ViewModel`.
  - `ViewModel`: Holds and processes the data for the UI, surviving configuration changes.
  - `DataStore`: Provides the data persistence layer.
- **Concurrency:** Kotlin Coroutines are used for background tasks, ensuring the main thread is never blocked.
- **Key Android Components:**
  - `androidx.appcompat` & `com.google.android.material`: For core UI components and Material Design.
  - `androidx.lifecycle`: For `ViewModel` and `Lifecycle` awareness.
  - `androidx.datastore`: For storing user preferences and reflections.

## Building from Source

To build the app, you'll need Android Studio.

1.  Clone this repository.
2.  Open the project in Android Studio.
3.  Let Gradle sync the dependencies.
4.  You can then build the project using `Build > Make Project` or run it on an emulator or a physical device.
5.  To generate a release APK, you can use the `Build > Build Bundle(s) / APK(s) > Build APK(s)` option.

## A Note on Privacy

Privacy was a primary consideration when building this app. Here’s the commitment to you:

- **No Internet Access:** The app does not request internet permission. It cannot and will not send any data off your device.
- **No Tracking or Analytics:** There are no third-party libraries for tracking or analytics.
- **Minimal Permissions:** The app only asks for the permissions it absolutely needs to function:
  - `Display over other apps`: To show the overlay.
  - `Accessibility service`: To know when to show the overlay. This service does not log or store information about the apps you use.

## Contributing

This is a small, personal project, but feel free to open an issue if you find a bug or have a suggestion.

---

_This README was last updated on March 26, 2026._
