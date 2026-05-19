# Namma-Nala – Infrastructure Monitoring Android App

## 📌 Project Overview
**Namma-Nala** is an Android application developed to help farmers and irrigation authorities monitor canal infrastructure issues such as:

- Water leakage
- Canal blockages
- Illegal water lifting
- Silt accumulation
- Water flow interruptions

The app allows users to report issues instantly using GPS location and image capture, helping authorities respond quickly and efficiently.

---

# 🚀 Features

## 👨‍🌾 Farmer Reporting System
- Report canal damage or blockage
- Upload photos of issues
- Automatic GPS location capture
- Real-time report submission

## 📍 Live Location Tracking
- Uses device GPS for accurate canal location mapping
- Helps engineers identify exact issue locations

## 📷 Image Upload
- Capture and upload issue photos directly from the app

## 🔥 Firebase Integration
- Firebase Authentication
- Firebase Firestore Database
- Firebase Storage for images
- Real-time data synchronization

## 📊 Report Management Workflow
### Report Status Flow:
1. **Pending**
2. **In Progress**
3. **Resolved**

### Workflow:
- User submits report
- Engineer/Admin clicks **Start**
- Report moves to **In Progress**
- Engineer uploads:
    - Repair image
    - Current GPS location
- Report marked as **Resolved**

## 🗺️ Google Maps Integration
- Displays reported locations on map
- Helps visualize canal infrastructure issues

## 📱 Modern UI
- Built completely using **Jetpack Compose**
- Clean and responsive design
- Material Design components

---

# 🛠️ Tech Stack

| Technology | Purpose |
|------------|----------|
| Kotlin | Main programming language |
| Jetpack Compose | Modern Android UI Toolkit |
| Firebase Authentication | User login & authentication |
| Firebase Firestore | Cloud database |
| Firebase Storage | Image storage |
| Google Maps API | Location & map integration |
| Fused Location Provider | GPS location services |
| MVVM Architecture | App architecture |
| Coroutines | Background operations |

---

# 📂 Project Structure

```bash
app/
├── ui/
│   ├── screens/
│   ├── components/
│   └── theme/
├── data/
│   ├── model/
│   ├── repository/
│   └── firebase/
├── viewmodel/
├── navigation/
└── utils/
```

# 🚀 How to Download and Run the Namma-Nala Project from GitHub

## 📥 Step 1: Install Required Software

Before running the project, install:

### ✅ Android Studio
Download:
https://developer.android.com/studio

### ✅ Git
Download:
https://git-scm.com/downloads

---

# 📂 Step 2: Download the Project from GitHub

## Method 1 — Using Git Clone (Recommended)

Open terminal or command prompt:

```bash
git clone https://github.com/your-username/namma-nala.git