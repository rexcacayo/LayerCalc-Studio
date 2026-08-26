# Changelog

All notable changes to **LayerCalc Studio** will be documented in this file.

The project follows [Semantic Versioning](https://semver.org/) using the format:

`MAJOR.MINOR.PATCH`

## [1.0.0] - 2026-08-26

### Added

* Initial public release of LayerCalc Studio.
* FDM calibration dashboard built with Jetpack Compose and Material 3.
* Maximum print speed calculation based on volumetric flow.
* Flow Ratio calibration using four wall measurements.
* Pressure Advance calculation for Direct Drive and Bowden extrusion systems.
* Remaining filament estimation based on spool weight, tare and material density.
* Print cost calculation including material, electricity, machine wear and waste.
* Dynamic filament and printer profile naming.
* Printer manufacturer, model and nozzle configuration.
* Filament manufacturer, material, commercial name and variant fields.
* Local calibration profile persistence using Room and SQLite.
* Profile save, update, duplicate, load and delete operations.
* OrcaSlicer JSON profile export.
* Bambu Studio JSON profile export.
* Compatible JSON profile import.
* Native Android JSON save flow using the Storage Access Framework.
* JSON file sharing through Android `Intent.ACTION_SEND`.
* Human-readable calibration profile sharing.
* Secure file sharing using `FileProvider`.
* Animated application welcome experience.
* Unit tests for the main calculation engine.
* GitHub Actions CI workflow for automated tests and debug builds.
* Manual GitHub Actions release workflow.
* Secure Android release signing using GitHub Secrets.
* APK signature verification during the release pipeline.
* Signed APK artifact generation.
* First signed public APK release.

### Architecture

* Kotlin
* Jetpack Compose
* Material 3
* Room
* SQLite
* Repository / DAO persistence layer
* Domain calculation layer
* JSON interoperability layer
* Android Storage Access Framework
* FileProvider
* Gradle Kotlin DSL
* Version Catalogs
* KSP

### Distribution

* Git tag: `v1.0.0`
* Signed Android APK available through GitHub Releases.

### Notes

LayerCalc Studio `v1.0.0` establishes the initial architecture and feature set for future development.

Future versions may include expanded printer presets, filament libraries, profile comparison, additional slicer support, enhanced automated testing and optional profile synchronization.
