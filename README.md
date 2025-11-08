# ✨ SpaMute - Automatic Anti-Spam and Mute Plugin


[![GitHub release (latest by date)](https://img.shields.io/github/v/release/InySMP/SpaMute)](https://github.com/InySMP/SpaMute/releases)
[![GitHub license](https://img.shields.io/github/license/InySMP/SpaMute)](LICENSE)
[![Discord](https://img.shields.io/discord/1431154141677359142?label=Discord&color=5865F2&logo=discord&logoColor=white&style=flat&query=online)](https://discord.gg/kXXSQxmYnG)

**SpaMute** is a lightweight and efficient Minecraft server plugin specifically designed to **automatically detect and punish** chat spamming behavior. By issuing temporary mutes (TempMute) to offending players, it effectively maintains a clean and orderly chat environment, significantly reducing the burden on server administrators.

## ⚙️ Core Features

* **Automatic Spam Detection:** Real-time monitoring of message frequency sent by players within a short period.
* **Automatic Temporary Muting:** Upon detecting spam, the plugin automatically executes a mute for a configurable duration.
* **Persistent Mute Records:** Mute data is stored in `mutes.yml`, ensuring records persist even after server restarts.
* **Administrator Commands:** Provides commands for manually unmuting players and reloading configuration files.

---

## 📥 Installation and Configuration

### System Requirements

* **Minecraft Server:** Paper / Spigot (Paper recommended) **1.17.x - 1.21+**
* **Java Version:** Java 21 or higher

### How to Install

1.  Download the latest [![GitHub release (latest by date)](https://img.shields.io/github/v/release/InySMP/SpaMute)](https://github.com/InySMP/SpaMute/releases) file from [GitHub Releases](https://github.com/InySMP/SpaMute/releases).
2.  Place the file into your server's `plugins/` folder.
3.  **Restart** your server.
4.  The plugin will automatically generate `config.yml` and `message.yml` inside the `plugins/SpaMute/` folder.

### 📊 Initial Configuration (`config.yml` Defaults)

| Setting | Default Value | Description |
| :--- | :--- | :--- |
| `spam-max-count` | `3` | The number of messages a player can send within `spam-check-seconds` before being considered spamming. |
| `spam-check-seconds` | `3` | The time window (in seconds) used for checking spam behavior. |
| `mute-duration-seconds` | `300` | The duration (in seconds) the player will be muted for when spam is detected. **(300 seconds = 5 minutes)** |
| `mute-command` | `chat` | The type of permission revoked after muting (e.g., `chat` for standard chat restriction). |

---

## 💻 Commands and Permissions

| Command | Description | Permission Node |
| :--- | :--- | :--- |
| `/spamute help` | Displays the command list. | `spamute.admin` |
| `/spamute reload` | Reloads the plugin's main configurations (`config.yml` and `message.yml`). | `spamute.admin` |
| `/unmute <player>` | Manually un-mutes the specified player. | `spamute.admin` |

| Permission Node | Description | Default |
| :--- | :--- | :--- |
| `spamute.bypass` | Players with this permission are **exempt** from spam detection and will never be automatically muted by the plugin. | Op |
| `spamute.admin` | Grants access to administrative commands (e.g., `/spamute reload`, `/unmute`) and receives automatic mute notifications. | Op |

---

## 🔗 Contact and Support

If you encounter any issues, find bugs, or have feature suggestions, please feel free to contact us via the following channels:

* **[Discord Community](https://dsc.gg/InySMP)**
* **[Project Website](https://inysmp.github.io/InySMP/main.html)**

### 📝 Contribution

Contributions are welcome in any form! If you wish to submit bug fixes or new features, please do so via a Pull Request.
