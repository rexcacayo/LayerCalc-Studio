# LayerCalc Studio

**LayerCalc Studio** is an Android application for **FDM 3D printing calibration, filament profile management, and slicer interoperability**.

The project was designed as both a practical tool for 3D printing workflows and a technical showcase of modern Android development with Kotlin, Jetpack Compose, Material 3, Room, JSON interoperability, and native Android file handling.

## Overview

LayerCalc Studio helps users calibrate filament and printing parameters for a specific combination of:

* Filament
* Printer
* Nozzle diameter
* Extruder type

The application calculates and manages key FDM parameters such as:

* Maximum print speed based on volumetric flow
* Flow Ratio
* Pressure Advance
* Remaining filament
* Print cost estimation

Calibration profiles can be stored locally, exported to slicer-compatible JSON formats, imported again into LayerCalc, or shared as human-readable summaries.

---

## Main Features

### Filament and printer profiles

LayerCalc separates the physical filament from the printer configuration.

A calibration profile can include:

* Filament manufacturer
* Material type
* Commercial filament name
* Variant / color
* Printer manufacturer
* Printer model
* Nozzle diameter
* Extruder type

Profiles are named dynamically.

Example:

```text
eSun PLA+ Black @ Flashforge AD5M Pro 0.4 mm
```

---

## FDM Calibration Tools

### Maximum Speed

Calculates the maximum theoretical print speed according to volumetric flow:

```text
v_max = Q_max / (layer_height × line_width)
```

Where:

* `v_max` = maximum speed in mm/s
* `Q_max` = maximum volumetric flow in mm³/s
* `layer_height` = layer height in mm
* `line_width` = extrusion width in mm

---

### Flow Ratio

Calculates a corrected extrusion multiplier from measured wall thickness:

```text
new_flow =
current_flow ×
(theoretical_width / measured_average)
```

The application supports four wall measurements and calculates their average automatically.

---

### Pressure Advance

Pressure Advance is calculated using:

```text
PA = optimal_Z_height × step_factor
```

Supported extrusion systems include:

* Direct Drive
* Bowden

The step factor is determined by the selected extrusion system.

---

### Remaining Filament

LayerCalc estimates the amount of filament remaining on a spool using:

* Gross spool weight
* Empty spool tare
* Material density
* Filament diameter

Results are displayed as:

* Remaining grams
* Approximate remaining meters

---

### Print Cost

The cost calculator includes:

* Material cost
* Electricity cost
* Machine wear
* Waste / failed print percentage

The final result provides both the total cost and an individual breakdown.

---

## Calibration Profile Manager

LayerCalc includes a local profile library powered by **Room / SQLite**.

Users can:

* Save a calibration profile
* Update an existing profile
* Save a profile as a new variant
* Load previous profiles
* Duplicate calibrations
* Delete profiles with confirmation
* Reuse profiles across sessions

Room acts as the application's local source of truth.

```text
Dashboard
   │
   ▼
CalibrationProfileRepository
   │
   ▼
CalibrationProfileDao
   │
   ▼
Room / SQLite
```

JSON files are treated as interoperability formats rather than the application's internal database.

---

## OrcaSlicer and Bambu Studio

LayerCalc can generate filament profile JSON files for:

* **OrcaSlicer**
* **Bambu Studio**

The export flow allows users to:

```text
EXPORT PROFILE
      │
      ├── OrcaSlicer
      │
      └── Bambu Studio
              │
              ▼
      ┌─────────────────┐
      │ Save JSON       │
      │ Share JSON      │
      └─────────────────┘
```

Generated files can therefore be:

* Saved locally
* Sent through messaging applications
* Uploaded to cloud storage
* Sent by email
* Transferred directly to another device

LayerCalc also supports importing compatible JSON profiles.

---

## Human-readable Profile Sharing

Not every profile needs to be exchanged as JSON.

LayerCalc can also generate a readable calibration sheet containing information such as:

```text
LAYERCALC STUDIO - CALIBRATION PROFILE

Profile:
eSun PLA+ Black @ Flashforge AD5M Pro 0.4 mm

Material:
PLA

Density:
1.24 g/cm³

Flow Ratio:
0.938

Pressure Advance:
0.0250

Max Volumetric Flow:
18.0 mm³/s

Max Speed:
200.0 mm/s
```

The sheet can be shared using Android's native sharing system.

---

## Android Integration

The project uses native Android APIs for file handling and sharing.

### Saving files

JSON profiles are saved using:

```text
ACTION_CREATE_DOCUMENT
```

This allows users to choose the destination and filename using Android's document picker.

### Importing profiles

Profiles are loaded using:

```text
ACTION_OPEN_DOCUMENT
```

### Sharing JSON files

JSON files are shared as real attachments using:

```text
Intent.ACTION_SEND
FileProvider
```

Temporary read permissions are granted through Android's URI permission system.

No storage permission is required.

---

## Architecture

The project follows a lightweight layered architecture:

```text
com.lugaresi.layercalc
│
├── data
│   └── local
│       ├── CalibrationProfileEntity
│       ├── CalibrationProfileDao
│       ├── CalibrationProfileRepository
│       └── LayerCalcDatabase
│
├── domain
│   ├── CalculatorLogic
│   ├── Models
│   └── ProfileJsonCodec
│
├── ui
│   ├── components
│   ├── screens
│   └── theme
│
└── MainActivity
```

Responsibilities are separated between:

**Domain**

Contains the mathematical logic and profile conversion logic.

**Data**

Handles local persistence using Room.

**UI**

Contains Jetpack Compose screens and reusable components.

---

## Technology Stack

* Kotlin
* Android SDK
* Jetpack Compose
* Material 3
* Room
* SQLite
* Kotlin Symbol Processing — KSP
* Gradle Kotlin DSL
* Version Catalogs
* Android Storage Access Framework
* FileProvider
* JSON profile serialization
* JUnit

---

## UI

LayerCalc uses a custom technical visual identity based on a **Bento-style dashboard**.

The interface focuses on:

* Large numeric metrics
* Clear technical grouping
* Dark theme
* Real-time calculations
* Mobile-first interaction
* Minimal navigation overhead

The application also includes an animated welcome experience before entering the main dashboard.

---

## Reactive Calculations

Most calculations are performed directly from Compose state.

Changes to input values immediately update:

* Flow telemetry
* Maximum safe speed
* Flow Ratio
* Pressure Advance
* Remaining filament
* Print cost
* Generated profile name

This provides immediate feedback while calibrating a filament.

---

## Example Workflow

A typical calibration workflow is:

```text
1. Select filament
       ↓
2. Select printer / nozzle
       ↓
3. Enter calibration measurements
       ↓
4. LayerCalc calculates the parameters
       ↓
5. Save calibration locally
       ↓
6. Export to OrcaSlicer or Bambu Studio
```

A saved profile can later be loaded, edited, duplicated or exported again.

---

## Building the Project

Requirements:

* Android Studio
* Android SDK
* JDK compatible with the configured Android Gradle Plugin

Clone the repository:

```bash
git clone https://github.com/rexcacayo/LayerCalc-Studio.git
```

Enter the project:

```bash
cd LayerCalc-Studio
```

Build using the Gradle wrapper:

### Linux / macOS

```bash
./gradlew build
```

### Windows

```powershell
.\gradlew.bat build
```

The Gradle wrapper downloads the required dependencies automatically.

No manual Room or KSP installation is required.

---

## Testing

The calculation engine includes unit tests for the primary formulas.

Examples include:

* Volumetric speed calculation
* Flow Ratio correction
* Pressure Advance
* Remaining filament
* Cost calculation

Run tests with:

```bash
./gradlew test
```

---

## Data Persistence

Calibration profiles are stored locally using Room.

Application data remains on the device unless the user explicitly exports or shares a profile.

Future database changes are intended to use explicit Room migrations to preserve existing user profiles.

---

## Project Goals

LayerCalc Studio was created to explore the intersection between:

* Android application architecture
* Practical engineering tools
* FDM 3D printing
* Technical calculation engines
* Local-first data management
* Interoperability between applications
* Mobile UX for engineering workflows

The project is intended to remain useful as a real application while also demonstrating production-oriented Android development practices.

---

## Roadmap

Planned improvements include:

* Expanded printer presets
* Filament manufacturer presets
* Better profile search and filtering
* Profile version history
* Calibration comparison
* Additional slicer compatibility
* Improved automated testing
* UI and instrumentation tests
* Optional cloud backup
* Optional profile synchronization
* Public profile sharing
* Release builds and distribution

---

## Repository

GitHub:

```text
https://github.com/rexcacayo/LayerCalc-Studio
```

---

## License

This project is licensed under the **MIT License**.

Copyright © 2026 Ricardo Lugaresi

See the `LICENSE` file for details.

---

## Disclaimer

LayerCalc Studio provides engineering estimates and calibration assistance for FDM 3D printing.

Results depend on printer hardware, filament characteristics, slicer settings, environmental conditions and measurement accuracy.

Always validate calibration parameters on the target printer before production use.

Download

The latest signed Android release is available from GitHub Releases.

Current release

LayerCalc Studio v1.0.0

Download the signed APK from:

https://github.com/rexcacayo/LayerCalc-Studio/releases/latest

The release APK is built, signed and verified through the project's GitHub Actions release pipeline.

Android may request permission to install applications from outside Google Play when installing the APK manually.

For developers who prefer to build from source, clone the repository and use the included Gradle wrapper.