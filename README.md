# 📊 CommitCounter - IntelliJ Platform Plugin 🚀

[![Platform](https://img.shields.io/badge/Platform-IntelliJ_IDEA_/_Android_Studio-blue.svg?style=flat-square&logo=intellij-idea)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-orange.svg?style=flat-square&logo=kotlin)]()
[![Gradle](https://img.shields.io/badge/Gradle-8.x-green.svg?style=flat-square&logo=gradle)]()
[![Clean Architecture](https://img.shields.io/badge/Architecture-Clean_Architecture-lightgrey.svg?style=flat-square)]()

An elegant, robust, and highly functional **IntelliJ Platform Plugin** designed to keep developers motivated by displaying their real-time daily GitHub commit counts directly in the IDE's **Status Bar** (StatusBarWidget). Built using strict **Clean Architecture** principles, **SOLID design patterns**, and JetBrains' modern guidelines. 🌟

---

## 🎨 Visual Preview & User Interface States

The status bar widget dynamically changes its state and text depending on the authentication and network conditions:

| State | Status Bar Representation | Action on Click |
| :--- | :--- | :--- |
| **Logged Out** 🚪 | `GitHub: Click to Login` | Triggers OAuth Device Flow 🔑 |
| **Synchronizing** 🔄 | `GitHub: Fetching...` | Shows fetching indicator |
| **Authenticated** ✅ | `Commits Today: [Count]` | Opens Interactive Actions Menu ⚙️ |
| **Error / Offline** ⚠️ | `GitHub: Error` | Triggers re-authentication flow |

---

## 🚀 Key Features

* 📍 **Native Status Bar Integration:** Sits perfectly at the bottom-right corner of your IDE alongside other key indicators.
* 🔑 **Secure GitHub OAuth Device Flow:** Sign in securely without pasting raw passwords or personal access tokens. Authenticates seamlessly via browser with auto-copy code to clipboard.
* 🔒 **PasswordSafe Secure Storage:** Tokens are encrypted and stored safely using JetBrains' built-in `PasswordSafe` and `CredentialAttributes` APIs.
* 🕒 **Automatic Background Sync:** Checks for updates every 15 minutes utilizing `ScheduledExecutorService` so your count is always accurate.
* ⚡ **Zero-Lag UI Execution:** All network calls, JSON processing, and preference operations are offloaded to pool-threads, ensuring the IDE main thread remains smooth and responsive.
* 🛠️ **Quick Action Menu:** Direct refresh, logout, and cancellation controls available with a single click.

---

## 🏗️ Architectural Blueprint (Clean Architecture)

The plugin code is divided into decoupled, highly cohesive, and testable modules:

```
src/main/kotlin/com/vahitkeskin/commitcounter/
├── 📂 data/
│   └── 📂 repository/
│       ├── 📄 GitHubRepository.kt       # Communicates with GitHub API (Device Auth & Commit Search)
│       └── 📄 PasswordSafeStorage.kt    # Secure credential management using IDE APIs
├── 📂 domain/
│   ├── 📂 model/
│   │   └── 📄 CommitState.kt            # Sealed UI state representation
│   └── 📂 usecase/
│       └── 📄 CommitCounterService.kt   # Central business controller, Scheduler & State manager
└── 📂 presentation/
    └── 📂 widget/
        ├── 📄 CommitCounterWidget.kt    # Status Bar rendering & user click handling
        ├── 📄 CommitCounterWidgetFactory.kt # System registration for the StatusBarWidget
        └── 📄 VerificationDialog.kt     # Custom IDE window showing OAuth instructions
```

---

## 🛠️ Step-by-Step Installation & Setup

### 📋 Prerequisites
* **Java Development Kit (JDK):** Version 21 ☕
* **IntelliJ Platform Gradle Plugin:** Version 2.5.0 (Targeting IntelliJ IDEA 2025.1+) 🛠️

### 🏁 Running Locally
1. Clone the repository:
   ```bash
   git clone https://github.com/vahitkeskin/CommitCounter.git
   cd CommitCounter
   ```
2. Build the plugin and test it inside a sandboxed IDE instance:
   ```bash
   ./gradlew runIde
   ```

### 📦 Building the Plugin Package
To package the plugin as a distributable `.zip` archive for local manual installation:
```bash
./gradlew buildPlugin
```
The resulting archive will be created in the `build/distributions/` directory.

---

## 🔑 GitHub OAuth Configuration

The plugin uses **GitHub OAuth Device Flow**. To configure your own application client credentials:

1. Visit [GitHub Developer Settings](https://github.com/settings/developers) and click **New OAuth App**.
2. Set the application name and homepages. Ensure **Device Flow** authorization support is enabled.
3. Copy the generated **Client ID**.
4. Paste your client ID inside the `CLIENT_ID` constant within [GitHubRepository.kt](src/main/kotlin/com/vahitkeskin/commitcounter/data/repository/GitHubRepository.kt):
   ```kotlin
   private const val CLIENT_ID = "YOUR_CLIENT_ID"
   ```

---

## 💡 How It Works under the Hood

1. **Authentication Flow:**
   - The user clicks on the status bar.
   - The plugin sends a request to `https://github.com/login/device/code` to retrieve verification information.
   - The user code is automatically copied to the clipboard, the default browser opens `https://github.com/login/device`, and an instruction dialog is shown.
   - The plugin polls the authorization status endpoint in a background worker.
   - Once verified, the token is safely stored.
2. **Commit Searching:**
   - The plugin fetches commits utilizing the GitHub Search Commits API:
     `GET https://api.github.com/search/commits?q=author:{username}+committer-date:{today}`
   - The count is parsed from the JSON payload and the status bar is repainted.

---

## 📄 License & Contribution

- This project is open-source. Pull Requests and bug reports are highly welcome! 💖
- Feel free to open an issue if you encounter any bugs or would like to request new features. 🛠️
