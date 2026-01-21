# HeyApps Manager

**The central hub and update manager for the ghostyApps ecosystem.**

HeyApps Manager is an open-source utility designed to simplify the installation and update process for all my Android applications. Instead of manually checking individual GitHub repositories for new releases, this app does it all for you in one place.

## 🚀 Why this app?

As an indie developer, I maintain multiple apps (HeyCam, HeyNotes, HeyBattery, etc.). Keeping them up-to-date shouldn't be a hassle for users.

**HeyApps Manager solves fragmentation.** It automatically scans the GitHub Releases of my projects, compares them with the versions installed on your device, and lets you update them with a single tap.

## 📥 Installation

You can download the latest version of HeyApps Manager from the Releases page:

[**👉 Download Latest APK**](https://github.com/ghostyapps/HeyAppsManager/releases)

## 🛠 How it Works

1.  The app uses the GitHub API to fetch the "Latest Release" tag for each repository.
2.  It strips non-numeric characters (like `v`, `HeyCam_`, etc.) to get a clean version number (e.g., `1.2.0`).
3.  It compares this with the `versionName` of the installed package on your phone.
4.  **Status Logic:**
    * **Download:** App is not installed.
    * **Update:** GitHub version > Local version.
    * **Open:** Local version == GitHub version.

## 🔒 Permissions

The app requires the following permissions to function:

* `INTERNET`: To check for updates on GitHub.
* `REQUEST_INSTALL_PACKAGES`: To install the downloaded APKs.
* `READ_EXTERNAL_STORAGE` (on older Androids): To save the APK file temporarily.

## 👨‍💻 Developer

With ❤️ by **ghostyApps**.

If you like my work, please consider starring ⭐ this repository!