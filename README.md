# 📍 Geofence Android App

An Android native application built using **Kotlin**, **Jetpack Compose**, and **Room Database** that implements **geofencing functionality**.  
The app allows users to define geofenced locations on a map, detects entry and exit events, calculates time spent inside the geofence, and stores visit history locally.

---

## 🚀 Features

- Live user location on Google Map
- Add geofences using long-press on the map
- Configurable geofence radius (10m – 50m)
- Detect entry and exit from geofenced areas
- Calculate duration spent inside a geofence
- Store geofence and visit data using Room DB
- View visit history with:
  - Location name  
  - Date  
  - Entry time  
  - Exit time  
  - Duration spent
- Notifications on geofence entry and exit
- Data persistence even after app restart or kill

---

## 🖥️ Screens

- **Screen 1:** Map View  
  - Displays Google Map  
  - Allows adding geofences with custom name and radius  
  - Shows all saved geofences on the map  

- **Screen 2:** Visit History  
  - Displays list of all visits  
  - Shows location name, date, entry time, exit time, and duration  

> Note: Geofence details (latitude, longitude, radius) are stored in the database and used internally, while visit history is presented as the primary user-facing list.

---

## 🛠️ Tech Stack

- **Language:** Kotlin  
- **UI:** Jetpack Compose  
- **Database:** Room DB  
- **Maps:** Google Maps SDK  
- **Location:** Fused Location Provider & Geofencing API  
- **Architecture:** MVVM (basic separation)  

---

## 📱 Android Compatibility

- Minimum Android Version: **Android 10 (API 29)**
- Tested on Android 10 and above

---

## 🔐 Permissions Used

- `ACCESS_FINE_LOCATION`
- `ACCESS_BACKGROUND_LOCATION` (for geofence monitoring)
- `INTERNET`

All required permissions are handled at runtime.

---

## 📦 Data Persistence

- Geofence and visit data are stored locally using **Room Database**
- Data remains available even after app is closed or restarted

---

## 🔔 Notifications

- User is notified when entering a geofenced area
- User is notified when exiting a geofenced area along with time spent

---
## 📦 APK Download

You can download the latest APK from GitHub Releases:  
👉 https://github.com/yadavpritam/GeoFenceApp/releases/tag/GeofenceApp

--

## 🎥 Demo Video

Demo video showcasing the complete app flow will be shared here:  
👉 **( Drive )**

---

## 📂 Project Setup

1. Clone the repository
   git clone https://github.com/yadavpritam/GeoFenceApp

##  Author
Pritam Yadav
Android Developer (Jetpack Compose)
