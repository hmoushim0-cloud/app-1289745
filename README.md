# تطبيق إيوان العقارية - منصة المسح المكاني والعقارات الفاخرة (`iwan-estates-real-estate-1289745`)
> منظومة التقنية العقارية (PropTech) المتكاملة للمسح المكاني ثلاثي الأبعاد 360 LiDAR والنمذجة المعمارية الذكية (BIM)

- **Project ID**: `#1289745`
- **Commercial Archetype**: `real_estate`
- **Bound Hardware Peripheral**: `360 LiDAR Spatial Scanner + Matterport Pro3`
- **Package Name**: `sa.factory.iwanestatesrealest`
- **Zero-Emoji Invariant**: Strictly Enforced (100% Clean Vector/ASCII)

---

## [Sidekick Autonomous Update]

### 1. Summary of Completed Enhancements
- **Resolved Kotlin Template Hazards**: Extracted fallback peripheral strings in Compose screens to prevent Kotlin single-quote interpolation compiler failures.
- **Modernized Compose Components**: Upgraded deprecated dividers to `HorizontalDivider` and added missing Material3 text formatting imports (`FontWeight`, `sp`).
- **Clean Invariant Enforcement**: Stripped unicode checkmarks and non-standard symbols in compliance with the Zero-Emoji enterprise standard.
- **Relocated Test Suite**: Moved regression and engine tests into `app/src/test/java/sa/factory/iwanestatesrealest/` for full Gradle JUnit runner integration.
- **Embedded Gradle Wrapper**: Bundled official Gradle wrapper binaries and comprehensive `.gitignore` rules for autonomous CI/CD compatibility.

### 2. Architecture & Domain Engines
- **LidarSpatialMeshProtocolEngine (`sa.factory.iwanestatesrealest.engine.LidarSpatialMeshProtocolEngine`)**:
  - Implements 360-degree LiDAR spatial point cloud telemetry processing (Matterport Pro3 / Leica BLK360 format).
  - Converts raw spherical/cylindrical range measurements (azimuth, elevation, distance) to cartesian coordinates.
  - Automatically reconstructs room boundaries, computing Gross Floor Area (m2), ceiling height (m), perimeter, and volume (m3).
- **PropertyValuationBimEngine (`sa.factory.iwanestatesrealest.engine.PropertyValuationBimEngine`)**:
  - Luxury Saudi PropTech asset valuation engine covering prime districts (Al-Malqa, Hittin, Al-Nakheel, Al-Shati).
  - Calculates hedonic valuations integrating finish tier multipliers and ceiling height architectural premiums.
  - Generates annual rental estimations (Ejar) and gross rental yield analytics.
- **HardwareSensorDriver Telemetry Integration**:
  - Emits continuous real-time state frames combining active room telemetry, mapped spatial area, and total portfolio valuation.

### 3. Step-by-Step Build and Test Instructions
```bash
# Run comprehensive unit and regression test suite
./gradlew test

# Build debug APK for cloud test harness deployment
./gradlew assembleDebug
```
