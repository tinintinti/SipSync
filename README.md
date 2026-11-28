# 🥤 SipSync

### Offline-First Beverage Inventory Management and Device Synchronization System

**SipSync** is an offline-first Android mobile application designed to simplify beverage inventory management for small stores and convenience stores. It allows store owners and employees to manage beverage products, record stock movements, monitor inventory levels, and synchronize transactions between two Android devices using **Wi-Fi Direct**, without requiring an internet connection.

---

## 📌 Project Overview

Managing beverage inventory can be challenging, especially when stock records are not properly monitored or internet connectivity is unreliable.

SipSync addresses these challenges by providing an offline inventory management solution. All inventory records are stored locally on each device, allowing users to continue managing their stock even without internet access.

Through Wi-Fi Direct and TCP socket communication, two authorized Android devices can exchange pending inventory transactions and maintain synchronized records.

## ✨ Features

- **User Authentication** – Secure login system with Owner and Employee roles.
- **Beverage Inventory Management** – Add, edit, delete, and monitor beverage products.
- **Stock In and Stock Out** – Record incoming and outgoing beverage products with automatic stock updates.
- **Transaction History** – View previously recorded inventory transactions.
- **Employee Account Management** – Allow the Owner to create employee accounts.
- **Offline-First Functionality** – Access essential inventory features without an internet connection.
- **Device Synchronization** – Synchronize inventory transactions between two Android devices using Wi-Fi Direct.
- **Low-Stock Monitoring** – Monitor beverage quantities and identify low-stock or out-of-stock products.

## 👥 User Roles

| User | Description |
|---|---|
| **Store Owner** | Manages beverage products, creates employee accounts, monitors inventory, and reviews transaction history. |
| **Store Employee** | Records Stock In and Stock Out transactions and checks available stock and transaction history. |

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Kotlin 2.0.21 | Primary programming language |
| Jetpack Compose | User interface development |
| Material 3 | UI components and design |
| Room Database | Local database management |
| SQLite | Local data storage |
| Wi-Fi Direct | Direct device-to-device communication |
| TCP Sockets | Data transmission between devices |
| JSON | Data exchange format |
| AndroidX ViewModel | Application state management |
| Kotlin Coroutines & StateFlow | Asynchronous operations and reactive data handling |
| Gradle 8.9 | Build automation |
| Android Gradle Plugin 8.7.3 | Android project configuration |
| JDK 17 | Java development environment |

## 📱 Application Screenshots

The application includes the following main screens:

| Screen | Description |
|---|---|
| Login Screen | User authentication |
| Home Screen | Inventory overview and quick actions |
| Inventory Screen | Beverage product management |
| Transaction History | Recorded stock movements |
| Device Sync | Connect and synchronize two Android devices |
| Profile Screen | User account information and settings |

> Screenshots of the application can be added here.

## ⚙️ System Requirements

Before running SipSync, make sure you have the following:

- Windows, macOS, or Linux computer
- JDK 17
- Android SDK
- Android SDK Platform 34
- Android SDK Build Tools 34.0.0
- Android device running Android 6.0 (Marshmallow) or later
- USB cable for device installation (optional if installing the APK manually)

**For device synchronization:**

- Two physical Android devices
- Wi-Fi Direct support on both devices
- Wi-Fi enabled on both devices
- No internet connection or Wi-Fi router required

## 🚀 Installation and Setup

### 1. Clone the Repository

Open your terminal or VS Code terminal and execute:

```bash
git clone https://github.com/tinintinti/SipSync.git
```

### 2. Open the Project

Navigate to the project directory:

```bash
cd SipSync
```

Open the folder in Visual Studio Code.

### 3. Configure Android SDK

Create a `local.properties` file in the root directory of the project.

Add your Android SDK location:

```properties
sdk.dir=C\:\\Android
```

**Note:** Change the path according to your actual Android SDK installation directory.

### 4. Build the Application

Run the following command in the project terminal:

```bash
.\gradlew assembleDebug
```

After a successful build, the APK will be generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### 5. Install the Application

Connect your Android device through USB with USB Debugging enabled.

Run:

```bash
.\gradlew installDebug
```

Alternatively, transfer the generated APK to your Android device and install it manually.

## 📖 How to Use

1. Open SipSync on your Android device.
2. Create the Owner account during the first launch.
3. Log in using your Owner credentials.
4. Navigate to **Profile → Add Employee** to create an employee account.
5. Log in using the appropriate Owner or Employee account.
6. Add and manage beverage products (Owner only).
7. Record Stock In and Stock Out transactions.
8. Check the Home screen for updated inventory quantities.
9. Open Transaction History to review recorded stock movements.
10. To synchronize inventory, open the Sync screen on both devices, establish a Wi-Fi Direct connection, and initiate synchronization.

## 🔄 How Device Synchronization Works

SipSync uses Wi-Fi Direct to establish a direct connection between two Android devices.

The synchronization process works as follows:

1. Both devices discover and connect to each other through Wi-Fi Direct.
2. A TCP socket connection is established.
3. Each device exchanges its existing transaction IDs.
4. Only transactions missing from the receiving device are transferred.
5. Unique transaction IDs prevent duplicate records.
6. Successfully acknowledged transactions are marked as synchronized.

All inventory records remain stored locally, even when the connection is interrupted.

## ⚠️ Scope and Limitations

- Designed exclusively for Android devices.
- Supports synchronization between two devices only: one Owner device and one Employee device.
- Both devices must support Wi-Fi Direct.
- Stock In and Stock Out transactions must be entered manually.
- Limited to beverage inventory management.
- Does not include cloud storage or online synchronization.
- Does not include payment processing, POS functionality, or advanced accounting features.
- Encrypted data transfer and pairing PIN are planned improvements.

## 🎓 Project Information

| Information | Details |
|---|---|
| Institution | Batangas State University – The National Engineering University |
| Campus | Alangilan Campus |
| College | College of Informatics and Computing Sciences |
| Course and Section | CS-3104 |
| Group Number | 13 |
| Project Title | SipSync – Offline-First Beverage Inventory Management and Device Synchronization System |

### 👨‍💻 Developers

- **Christine Nicole P. Miranda** – Group Leader
- **Dhanreigh I. Atienza** – Member
- **Philip Joshua D. Vinluan** – Member

---

**SipSync** – *Manage Your Beverage Inventory Anytime, Anywhere, Even Offline.* 🥤
