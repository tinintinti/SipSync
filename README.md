# SipSync

**Offline beverage inventory management for stores.**

SipSync lets a store owner and an employee track beverage stock on their own Android phones, with no internet or cloud. The two phones are designed to stay in sync by exchanging records directly over **Wi-Fi Direct**.

Built for a Mobile Computing course project.

> **Project status:** UI prototype. All screens work with in-memory sample data. The local database and Wi-Fi Direct sync are the next milestones (see [Roadmap](#roadmap)).

---

## Features

### Implemented (UI prototype)

- Animated **S&S splash screen** with reduced-motion support
- **Login screen** with show/hide password and prototype account cards (Owner / Employee)
- **Role-based interface**: Owner and Employee see different screens and permissions
- **Home**: inventory overview, low-stock and out-of-stock counts, quick actions, recent transactions
- **Inventory**: search, category filters, stock status badges, and Owner-only Add and Delete
- **Add Beverage** form (Owner only) with validation
- **History**: Stock In / Stock Out filters (Owners see all devices, Employees see their own activity)
- **Sync**: Wi-Fi Direct status screen with a simulated sync button
- **Profile**: account details, **Switch prototype role**, and Log out
- Custom app icon and shared SipSync logo

### Planned

- Local database (Room / SQLite)
- Stock In and Stock Out forms, Edit Beverage
- Real login with hashed passwords
- Wi-Fi Direct device discovery and TCP socket sync

---

## Roles

| | Owner | Employee |
|---|---|---|
| View inventory | Yes | Yes (view-only) |
| Add, edit, delete beverages | Yes | No |
| Stock In / Stock Out | Yes | Yes |
| History | All devices | Own activity only |
| Weekly stock in/out summary | Yes | No |

## Prototype accounts

| Role | Username | Password |
|---|---|---|
| Owner | `owner` | `owner123` |
| Employee | `employee` | `staff123` |

Tapping an account card on the login screen fills these in. Login currently accepts any non-empty username and password. Real authentication comes with the database.

---

## Design

SipSync has a clean, modern, minimal look built around deep teal and a warm cream background, with rounded corners everywhere.

### Color palette

| Color | Hex | Used for |
|---|---|---|
| Deep Teal | `#073B3A` | Headers, banners, titles, primary text |
| Primary Teal | `#087F70` | Buttons, active tab, accents, Stock In |
| Cream | `#F8F7F2` | Screen background |
| White | `#FFFFFF` | Cards, input fields, top bars |
| Light Mint | `#E7F4EF` | Selected cards, icon tiles, info boxes |
| Bright Mint | `#5FE3C0` | Highlights on dark teal, status dots |
| Red | `#D64545` | Stock Out, out-of-stock, log out |
| Amber | `#E0922B` | Low stock, warnings, Synchronize |
| Blue | `#5B6BD6` | Manage / View Inventory accent |
| Splash Gold | `#E3B04B` | The ampersand on the splash screen |

### Style guidelines

- **Layout:** dark teal headers with white cards that overlap or sit below them, on a cream background
- **Shapes:** rounded corners on cards (18 to 26 dp), buttons (16 dp), input fields (14 to 16 dp) and chips (12 dp)
- **Icons:** Material outlined icons, shown in tinted rounded tiles
- **Status colors:** teal means in stock or stock in, amber means low stock, red means out of stock or stock out
- **Navigation:** a bottom bar with Home, Inventory, History, Sync and Profile
- **Responsive:** screens scroll on small phones, and the login footer pins to the bottom on tall ones
- **Motion:** only the splash screen is animated, and it respects the system reduced-motion setting
- **Logo:** a bottle outline with a small teal "sync" badge, shown in the login screen, screen headers and the launcher icon

### Screenshots

Add screenshots to `docs/screenshots/` and they will show here.

| Splash | Login | Home |
|---|---|---|
| ![Splash](docs/screenshots/splash.png) | ![Login](docs/screenshots/login.png) | ![Home](docs/screenshots/home.png) |

| Inventory | History | Sync |
|---|---|---|
| ![Inventory](docs/screenshots/inventory.png) | ![History](docs/screenshots/history.png) | ![Sync](docs/screenshots/sync.png) |

| Add Beverage | Profile |
|---|---|
| ![Add Beverage](docs/screenshots/add-beverage.png) | ![Profile](docs/screenshots/profile.png) |

---

## Tech stack

| | |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Build | Gradle (Kotlin DSL) |
| Android SDK | compileSdk / targetSdk 35, minSdk 23 |
| JDK | 17 |
| Icons | `androidx.compose.material:material-icons-extended` |
| Planned | Room (SQLite), `WifiP2pManager`, TCP sockets |

---

## Project structure

```
SipSync/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/                          # app icon resources
│       └── java/com/example/sipsync/
│           ├── MainActivity.kt           # splash -> login -> main
│           └── screens/
│               ├── SplashScreen.kt
│               ├── LoginScreen.kt
│               ├── MainScreen.kt         # bottom nav, shared UI parts, sample data
│               ├── HomeScreen.kt
│               ├── InventoryScreen.kt
│               ├── AddBeverageScreen.kt
│               ├── HistoryScreen.kt
│               ├── SyncScreen.kt
│               └── ProfileScreen.kt
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── gradlew.bat
```

Shared colors, cards, chips, and the logo live in `MainScreen.kt`. The sample `Beverage` and `Txn` data and `transactionsFor()` also live there, and will be replaced by the database.

---

## Getting started

### Requirements

- JDK 17
- Android SDK with API 35 and platform-tools (`adb`)
- A phone with **USB debugging** enabled, or an emulator
- VS Code or any editor. Android Studio is not required.

### Setup

```powershell
git clone https://github.com/tinintinti/SipSync.git
cd SipSync
```

Create a `local.properties` file in the project root pointing to your own SDK (this file is git-ignored):

```properties
sdk.dir=C:/Android/Sdk
```

### Build

```powershell
.\gradlew.bat assembleDebug
```

The APK is created at `app\build\outputs\apk\debug\app-debug.apk`.

### Install and run on a phone

```powershell
adb devices                                   # the phone should show as "device"
.\gradlew.bat installDebug
adb shell am start -n com.example.sipsync/.MainActivity
```

If `adb` is not recognized, add the platform-tools folder to your PATH for the session:

```powershell
$env:Path += ";C:\Android\Sdk\platform-tools"
```

---

## Roadmap

1. **Room database** with `users`, `beverages` and `transactions` tables, replacing the sample data
2. **Stock In / Stock Out** forms and **Edit Beverage**
3. **Real login** with seeded Owner and Employee accounts and hashed passwords
4. **Wi-Fi Direct** discovery and connection (`WifiP2pManager`)
5. **TCP socket sync** that exchanges JSON transactions between the two phones
6. Testing with two real phones, including changes made while disconnected

### Design decisions for sync

- Stock is **computed** from the sum of transaction rows, not stored as an editable number. Stock In and Stock Out only add rows.
- All ids are **UUIDs**, so two phones never create clashing ids.
- The **Owner** is the source of truth for the beverage catalog. **Transactions** flow both ways.
- Duplicate transactions are ignored, so syncing twice is harmless.

> Wi-Fi Direct does not work on the Android emulator. Testing it needs two real phones.

---

## Team workflow

- Work on a branch, for example `git checkout -b feature/database`.
- Push the branch and open a pull request before merging into `main`.
- Confirm `.\gradlew.bat installDebug` ends with `BUILD SUCCESSFUL` before pushing.
- Do not commit `local.properties`, `build/` folders, or `*.log` files (they are in `.gitignore`).

## License

Course project. Add a license here if the project is shared publicly.