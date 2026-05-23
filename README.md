# 🩸 BloodBound — Android Application

> Native Android app for BloodBound — a localized blood donation coordination platform connecting verified donors with individuals in urgent need across Cebu City and surrounding areas.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Architecture](#architecture)
- [Features](#features)
- [Navigation](#navigation)
- [Screens & Fragments](#screens--fragments)
- [Core Layer](#core-layer)
- [Resource System](#resource-system)
- [Security & Session Management](#security--session-management)
- [Business Logic](#business-logic)
- [Getting Started](#getting-started)
- [Build & Run](#build--run)
- [Design System](#design-system)
- [Related Repositories](#related-repositories)

---

## Overview

BloodBound Android is a **native Kotlin application** that serves as the mobile client for the BloodBound platform. It connects to the Spring Boot backend via Retrofit and provides a full-featured blood donation coordination experience optimized for Android devices.

The full BloodBound system comprises:
- **This Repo** — Native Android App (Kotlin, Jetpack components)
- **Backend** — Spring Boot 3.x RESTful API ([bloodbound-backend.onrender.com](https://bloodbound-backend.onrender.com))
- **Web Frontend** — React/TypeScript ([bloodbound-webapp.vercel.app](https://bloodbound-webapp.vercel.app/))

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | XML Layouts + ViewBinding |
| Navigation | Jetpack Navigation Component |
| Dependency Injection | Hilt |
| HTTP Client | Retrofit 2 |
| JSON Parsing | Gson / Moshi |
| Session Storage | EncryptedSharedPreferences |
| Architecture | MVVM (ViewModel + Repository) |
| Build System | Gradle (Kotlin DSL) |
| Min SDK | Android 8.0 (API 26) |
| Target SDK | Android 14 (API 34) |

---

## Project Structure

```
app/
├── manifests/
│   └── AndroidManifest.xml
└── kotlin+java/
    └── com.bloodbound.app/
        ├── core/
        │   ├── network/
        │   │   ├── ApiResult.kt
        │   │   ├── AuthInterceptor.kt
        │   │   ├── NetworkModule.kt
        │   │   └── TokenManager.kt
        │   ├── ui/
        │   │   └── GradientTextView.kt
        │   └── util/
        │       ├── BloodTypeHelper.kt
        │       ├── EligibilityHelper.kt
        │       ├── FooterHelper.kt
        │       ├── StringHelper.kt
        │       └── TimeAgoHelper.kt
        ├── feature/
        │   ├── auth/
        │   │   ├── data/
        │   │   │   ├── AuthApi.kt
        │   │   │   ├── AuthModels.kt
        │   │   │   └── AuthRepository.kt
        │   │   └── ui/
        │   │       ├── AuthViewModel.kt
        │   │       ├── LoginFragment.kt
        │   │       ├── RegisterFragment.kt
        │   │       └── WelcomeFragment.kt
        │   ├── commitments/
        │   │   ├── data/
        │   │   │   ├── CommitmentModels.kt
        │   │   │   ├── CommitmentsApi.kt
        │   │   │   └── CommitmentsRepository.kt
        │   │   └── ui/
        │   │       ├── adapter/
        │   │       │   └── CommitmentAdapter.kt
        │   │       ├── CommitmentsViewModel.kt
        │   │       └── MyCommitmentsFragment.kt
        │   ├── dashboard/
        │   │   ├── data/
        │   │   │   ├── DashboardApi.kt
        │   │   │   └── DashboardRepository.kt
        │   │   └── ui/
        │   │       ├── DashboardFragment.kt
        │   │       ├── DashboardViewModel.kt
        │   │       └── RequestSummaryAdapter.kt
        │   ├── profile/
        │   │   ├── data/
        │   │   │   ├── ProfileApi.kt
        │   │   │   ├── ProfileModels.kt
        │   │   │   └── ProfileRepository.kt
        │   │   └── ui/
        │   │       ├── ProfileFragment.kt
        │   │       └── ProfileViewModel.kt
        │   └── requests/
        │       ├── data/
        │       │   ├── RequestModels.kt
        │       │   ├── RequestsApi.kt
        │       │   └── RequestsRepository.kt
        │       └── ui/
        │           ├── adapter/
        │           │   ├── DonorRequestAdapter.kt
        │           │   └── RequesterRequestAdapter.kt
        │           ├── ActiveRequestsFragment.kt
        │           ├── PostRequestDialog.kt
        │           ├── RequestHistoryFragment.kt
        │           └── RequestsViewModel.kt
        ├── BloodBoundApplication.kt
        └── MainActivity.kt

res/
├── anim/
│   ├── slide_in_right.xml
│   └── slide_out_left.xml
├── color/
├── drawable/                        # See Resource System section
├── layout/
│   ├── activity_main.xml
│   ├── dialog_post_request.xml
│   ├── footer_component.xml
│   ├── fragment_active_requests.xml
│   ├── fragment_dashboard.xml
│   ├── fragment_login.xml
│   ├── fragment_my_commitments.xml
│   ├── fragment_profile.xml
│   ├── fragment_register.xml
│   ├── fragment_request_history.xml
│   ├── fragment_welcome.xml
│   ├── item_donor_request.xml
│   ├── item_history_request.xml
│   ├── item_request_summary.xml
│   ├── item_requester_request.xml
│   └── item_ticket_card.xml
├── menu/
├── mipmap/                          # App launcher icons (all densities)
├── navigation/
│   └── nav_graph.xml
├── values/
│   ├── colors.xml
│   ├── dimens.xml
│   ├── strings.xml
│   └── themes.xml (light + night)
└── xml/
    ├── backup_rules.xml
    └── data_extraction_rules.xml

Gradle Scripts
├── build.gradle.kts        (Project: BloodBound)
├── build.gradle.kts        (Module :app)
├── libs.versions.toml      (Version Catalog)
├── gradle.properties       (Project + Global Properties)
├── gradle-wrapper.properties
├── proguard-rules.pro
├── settings.gradle.kts
└── local.properties        (SDK Location — git-ignored)
```

---

## Architecture

The app follows **MVVM (Model-View-ViewModel)** with a clean vertical-slice feature structure. Each feature owns its full stack — data layer (API + Repository) and UI layer (Fragment + ViewModel + Adapter).

```
Fragment  →  ViewModel  →  Repository  →  Api (Retrofit)
                                              ↓
                                       Spring Boot Backend
                                  (bloodbound-backend.onrender.com)
```

- **Fragments** observe `LiveData` / `StateFlow` from ViewModels and update UI reactively
- **ViewModels** hold UI state and call repository methods; survive config changes
- **Repositories** abstract the data source — Retrofit API calls wrapped in `ApiResult`
- **Hilt** manages dependency injection across all layers
- **AuthInterceptor** automatically attaches the JWT Bearer token to every outgoing request
- **TokenManager** stores session data securely in `EncryptedSharedPreferences`

---

## Features

### ✅ Must-Have (Implemented)

- **JWT Authentication** — Login, registration, and logout with full session management via `TokenManager` and `AuthInterceptor`
- **Role-Based UI** — Separate adapters and views for Donor (`DonorRequestAdapter`) and Requester (`RequesterRequestAdapter`) roles
- **Blood Request Management** — Browse active requests, post new requests via `PostRequestDialog`, view request history
- **Donor Commitment System** — Commit to donate with eligibility enforcement; view and manage commitments in `MyCommitmentsFragment`
- **56-Day Eligibility Tracker** — `EligibilityHelper` calculates countdown from `lastDonationDate`; visual status shown on dashboard
- **User Profile** — View and update profile info including blood type and donation stats
- **Dashboard** — Role-specific summary view with `RequestSummaryAdapter` for quick request overviews
- **Logout Flow** — Clears all session data, resets bottom nav state, and navigates back to `auth_graph` with full back-stack clearing

### 🔄 Should-Have

- Request filtering by blood type (horizontal scrollable chips)
- Pull-to-refresh on request lists
- `TimeAgoHelper` for human-readable relative timestamps
- Slide animations (`slide_in_right.xml`, `slide_out_left.xml`) between fragments
- Slide animations (`slide_in_right.xml`, `slide_out_left.xml`) between fragments

---

## Navigation

Navigation is handled by **Jetpack Navigation Component** via a single-activity architecture. `MainActivity` hosts the `NavHostFragment` and the bottom navigation bar.

### Navigation Graph (`nav_graph.xml`)

```
auth_graph (nested)
├── WelcomeFragment     (start)
├── LoginFragment
└── RegisterFragment

main_graph
├── DashboardFragment   (start)
├── ActiveRequestsFragment
├── RequestHistoryFragment
├── MyCommitmentsFragment
└── ProfileFragment
```

Icons defined in `res/drawable/`:
- `ic_nav_overview.xml` — Dashboard
- `ic_nav_requests.xml` — Requests
- `ic_nav_commitments.xml` — Commitments
- `ic_nav_history.xml` — History
- `ic_nav_profile.xml` — Profile

### Logout Behavior

`MainActivity.performLogout()` executes a 3-step logout:
1. `tokenManager.clearAll()` — wipes JWT and user data from `EncryptedSharedPreferences`
2. `binding.bottomNav.menu.clear()` — prevents stale nav state from persisting
3. `navController.navigate(R.id.auth_graph, null, navOptions)` — navigates to auth graph with `setPopUpTo(inclusive = true)` to clear the full back stack

---

## Screens & Fragments

### Auth Feature

| Fragment | Layout | Description |
|---|---|---|
| `WelcomeFragment` | `fragment_welcome.xml` | Landing/splash screen with role selection |
| `LoginFragment` | `fragment_login.xml` | Email + password login form |
| `RegisterFragment` | `fragment_register.xml` | Registration form with blood type, role, and optional last donation date |

`AuthViewModel` drives all auth state. `AuthRepository` calls `AuthApi` for `/auth/register`, `/auth/login`, and `/auth/me`.

### Dashboard Feature

| Fragment | Layout | Description |
|---|---|---|
| `DashboardFragment` | `fragment_dashboard.xml` | Role-specific home screen — eligibility card, nearby requests summary |

`RequestSummaryAdapter` powers the compact request list on the dashboard. `DashboardRepository` uses `DashboardApi` to fetch combined dashboard data.

### Requests Feature

| Fragment/Dialog | Layout | Description |
|---|---|---|
| `ActiveRequestsFragment` | `fragment_active_requests.xml` | Full list of active blood requests with filtering |
| `RequestHistoryFragment` | `fragment_request_history.xml` | Past requests (Requester) or donation history (Donor) |
| `PostRequestDialog` | `dialog_post_request.xml` | Bottom sheet / dialog for posting a new blood request (Requester only) |

Dual adapters serve both roles:
- `DonorRequestAdapter` — renders `item_donor_request.xml` with Commit button and eligibility state
- `RequesterRequestAdapter` — renders `item_requester_request.xml` with Fulfill button

`RequestsViewModel` drives all request state and filtering. `RequestsRepository` calls `RequestsApi`.

### Commitments Feature

| Fragment | Layout | Description |
|---|---|---|
| `MyCommitmentsFragment` | `fragment_my_commitments.xml` | Active and past donation commitments with ticket card view |

`CommitmentAdapter` renders `item_ticket_card.xml` — a ticket-style card showing hospital, date, time, case number, and QR code placeholder. `CommitmentsViewModel` manages commitment state; `CommitmentsRepository` calls `CommitmentsApi`.

### Profile Feature

| Fragment | Layout | Description |
|---|---|---|
| `ProfileFragment` | `fragment_profile.xml` | User profile — blood type badge, donation stats, eligibility status, edit options |

`ProfileViewModel` handles profile fetch and update. `ProfileRepository` calls `ProfileApi` for profile read and update endpoints.

---

## Core Layer

### `NetworkModule.kt`
Hilt module providing the Retrofit instance, OkHttpClient with `AuthInterceptor`, and all API service instances.

### `AuthInterceptor.kt`
OkHttp interceptor that retrieves the stored JWT from `TokenManager` and injects `Authorization: Bearer <token>` into every outgoing API request automatically.

### `TokenManager.kt`
Wraps `EncryptedSharedPreferences` to securely store and retrieve the JWT token and current user data. `clearAll()` is called on logout.

### `ApiResult.kt`
A sealed class wrapping API responses:
```kotlin
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}
```

### Utility Helpers

| File | Purpose |
|---|---|
| `EligibilityHelper.kt` | Calculates 56-day eligibility countdown from `lastDonationDate` |
| `BloodTypeHelper.kt` | Formats and validates blood type enums for display |
| `TimeAgoHelper.kt` | Converts ISO timestamps to relative strings (e.g. "2 hours ago") |
| `StringHelper.kt` | General string formatting utilities |
| `FooterHelper.kt` | Manages footer component visibility and content |
| `GradientTextView.kt` | Custom `TextView` subclass supporting gradient text color (used for branding) |

---

## Resource System

### Drawable Assets

The app uses an extensive set of XML vector drawables for consistent, scalable UI across all screen densities. Key groups:

| Prefix | Purpose |
|---|---|
| `bg_btn_*` | Button backgrounds (red ghost, red gradient, blue ghost, blue gradient, white outline, fulfill, login) |
| `bg_card_*` / `bg_card_section_*` | Card and section container backgrounds |
| `bg_blood_type_badge*` | Blood type indicator badges (default + blue variant) |
| `bg_chip_*` | Filter chip backgrounds for blood type and role |
| `bg_pill_*` | Pill-shaped status badges (blue, red, yellow, expiry, outline) |
| `bg_status_badge*` | Request status indicators |
| `bg_eligibility_icon*` | Eligibility status icons (standard + background variant) |
| `bg_circle_*` | Circular accent shapes (standard, small, overlay, red-light, blue-light) |
| `bg_input_*` | Input field states (default, focused) |
| `bg_filter_*` | Filter button active/inactive states |
| `bg_role_card_*` | Role selection cards (default, donor selected, requester selected, blue, red) |
| `bg_stat_chip*` | Dashboard stat chip backgrounds |
| `bg_profile_header*` | Profile screen header background |
| `bg_requester_card*` | Requester-specific card styles |
| `bg_toggle_*` / `bg_tab_*` | Tab and toggle container backgrounds |
| `bg_divider_gradient*` / `bg_dot_divider*` | Section dividers |
| `bg_avatar_ring*` | Profile avatar ring decoration |
| `bg_app_gradient*` | Full-screen app background gradient |
| `orb_blue.xml` / `orb_red.xml` | Decorative animated orb elements |
| `ring_blue_faint.xml` / `ring_red_faint.xml` | Decorative ring pulse elements |
| `ic_blood_drop.xml` | Blood drop icon |
| `ic_bloodbound_logo.png` | App logo |
| `ic_logout.xml` | Logout icon |
| `ic_nav_*.xml` | Bottom navigation icons (overview, requests, commitments, history, profile) |
| `bg_camera_badge*` | Profile photo upload badge |
| `bg_edit_photo_badge*` | Edit photo overlay badge |

### Layout Files

| Layout | Used By |
|---|---|
| `activity_main.xml` | `MainActivity` — hosts NavHostFragment + BottomNavigationView |
| `fragment_welcome.xml` | `WelcomeFragment` |
| `fragment_login.xml` | `LoginFragment` |
| `fragment_register.xml` | `RegisterFragment` |
| `fragment_dashboard.xml` | `DashboardFragment` |
| `fragment_active_requests.xml` | `ActiveRequestsFragment` |
| `fragment_request_history.xml` | `RequestHistoryFragment` |
| `fragment_my_commitments.xml` | `MyCommitmentsFragment` |
| `fragment_profile.xml` | `ProfileFragment` |
| `dialog_post_request.xml` | `PostRequestDialog` |
| `footer_component.xml` | `FooterHelper` — reusable footer |
| `item_donor_request.xml` | `DonorRequestAdapter` |
| `item_requester_request.xml` | `RequesterRequestAdapter` |
| `item_request_summary.xml` | `RequestSummaryAdapter` |
| `item_history_request.xml` | History list items |
| `item_ticket_card.xml` | `CommitmentAdapter` — donation ticket card |

### Navigation & Values

- `nav_graph.xml` — defines the full navigation graph with auth and main nested graphs
- `colors.xml` — centralized color definitions matching the design system
- `dimens.xml` — spacing and sizing constants
- `strings.xml` — all user-facing string resources
- `themes.xml` — app theme definitions

---

## Security & Session Management

- **JWT** stored in `EncryptedSharedPreferences` via `TokenManager`
- **`AuthInterceptor`** injects `Authorization: Bearer <token>` on every API call automatically — no manual header management in ViewModels or Repositories
- **Logout** clears the full session: token, user data, nav back stack, and bottom nav state
- **HTTPS** enforced for all API communication to `bloodbound-backend.onrender.com`
- **RBAC enforced on UI level** — role-aware adapters and fragment visibility ensure Donors and Requesters only see relevant actions

---

## Business Logic

### 56-Day Eligibility (`EligibilityHelper.kt`)

```kotlin
val daysElapsed = ChronoUnit.DAYS.between(lastDonationDate, LocalDate.now())
val isEligible  = daysElapsed >= 56
val daysLeft    = (56 - daysElapsed).coerceAtLeast(0)
```

- Eligible → green indicator, "Ready to Donate", Commit button **enabled**
- Ineligible → red indicator, "Eligible in X days", Commit button **disabled**
- `null` lastDonationDate (first-time donor) → immediately eligible

### Dual-Role Request Adapters

`ActiveRequestsFragment` uses two separate RecyclerView adapters based on the authenticated user's role:
- **`DonorRequestAdapter`** — shows blood type badge, urgency, hospital name, distance, and a "Commit to Donate" button with eligibility state enforcement
- **`RequesterRequestAdapter`** — shows commitment count, request status, and a "Mark as Fulfilled" action

### Commitment Ticket (`item_ticket_card.xml`)

After a successful commitment, the donor receives a ticket card in `MyCommitmentsFragment` showing: Hospital name, donation date, time, case reference number, and a QR code placeholder for on-site confirmation.

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- Android SDK (API 26–34)
- A running instance of the BloodBound backend ([bloodbound-backend.onrender.com](https://bloodbound-backend.onrender.com))

### Setup

```bash
# Clone the repository
git clone https://github.com/your-username/BloodBound-Android-New.git
cd BloodBound-Android-New
```

1. Open the project in **Android Studio**
2. Let Gradle sync complete
3. Set the backend base URL in `NetworkModule.kt` (or `local.properties`):

```kotlin
private const val BASE_URL = "https://bloodbound-backend.onrender.com/api/"
```

4. Run on a physical device or emulator (API 26+)

> ⚠️ `local.properties` contains your SDK path and is git-ignored. It is auto-generated by Android Studio.

---

## Build & Run

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (requires signing config)
./gradlew assembleRelease

# Run tests
./gradlew test

# Install directly on connected device
./gradlew installDebug
```

The version catalog (`libs.versions.toml`) manages all dependency versions centrally. ProGuard rules are configured in `proguard-rules.pro`.

---

## Design System

| Token | Value |
|---|---|
| Primary | `#D32F2F` (Emergency Red) |
| Secondary | `#F44336` (Soft Red) |
| Accent | `#DC2626` (Dialog buttons, branding) |
| Success | `#388E3C` (Eligible / Confirmed) |
| Warning | `#F57C00` (Urgency / Pending) |
| Font Family | Inter (Sans-serif) |
| Min Touch Target | 48dp |
| Grid | 8dp base spacing |
| Animations | Slide in/out (300ms) |
| Supported SDK | API 26 (Android 8.0) – API 34 (Android 14) |

> ⚠️ Dark mode is **not supported**. The app runs in light theme only.

---

## Related Repositories

| Component | Stack | Link |
|---|---|---|
| `bloodbound-backend` | Java 17, Spring Boot 3.x, PostgreSQL | [bloodbound-backend.onrender.com](https://bloodbound-backend.onrender.com) |
| `bloodbound-webapp` | React 18, TypeScript, Tailwind, Vercel | [bloodbound-webapp.vercel.app](https://bloodbound-webapp.vercel.app/) |

---

## Academic Context

This project is part of **IT342 — System Integration and Architecture (G7)** at **Cebu Institute of Technology — University (CIT-U)**.

- **Prepared by:** Michelle Marie Palacio Habon
- **SDD Version:** 2.0 (Final — 02/16/2026)
- **App Package:** `com.bloodbound.app`

---

*BloodBound — Connecting blood donors to those who need it most, across Cebu City.*
