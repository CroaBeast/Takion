<p align="center">
    <a href="https://discord.com/invite/gzzhVqgy3b" alt="Support Server">
        <img alt="Discord" src="https://img.shields.io/discord/826555143398752286?style=for-the-badge&logo=discord&label=Support%20Server&color=635aea">
    </a>
</p>

# ✨ Takion

Takion is an all-in-one toolkit for building premium chat and text experiences on top of the Spigot and Paper Minecraft APIs. From rich message formatting to per-channel moderation rules, Takion ships with a curated set of managers, adapters, and utilities so you can focus on gameplay instead of infrastructure.

> **TL;DR**: Drop Takion into your plugin to gain a scheduler-aware messaging layer, powerful placeholder resolution, flexible channel routing, and plenty of formatting sugar out-of-the-box. 🧙‍♂️

---

## 📚 Table of Contents

1. [Highlights](#-highlights)
2. [Module Overview](#-module-overview)
3. [Ecosystem & Bundled APIs](#-ecosystem--bundled-apis)
4. [Installation](#-installation)
5. [Quickstart](#-quickstart)
6. [Feature Tour](#-feature-tour)
7. [Building from Source](#-building-from-source)
8. [Contributing](#-contributing)
9. [License](#-license)

---

## 🚀 Highlights

- **Production-ready chat core** featuring color pipelines, rich components, and consistent alignment logic.
- **Channel, placeholder, and rule management** baked directly into the `TakionLib` entry point for one-line access in your plugin lifecycle.
- **Scheduler integration** via `GlobalScheduler`, making it trivial to run asynchronous or delayed messaging tasks without boilerplate.
- **Drop-in metrics** (bstats) and optional Vault/chat bridges when using the plugin distribution.
- **Ready for shading**—choose between the lightweight core API, the shaded binary with dependencies, or the demonstration plugin module.

---

## 🧩 Module Overview

Takion is a multi-module Gradle project. Pick the artifact that matches your distribution strategy:

| Module | Description | Ideal For |
| ------ | ----------- | --------- |
| `core` | The primary Takion API containing `TakionLib`, managers, and shared utilities. Optional when you only need to compile against the exposed API or validate the core sources. |
| `shaded` | Repackages `core` together with required libraries (PrismaticAPI, GlobalScheduler, YAML-API). | Shipping a single jar without configuring repositories in your consumer. |
| `plugin` | Example/production-ready plugin bundle that brings in optional adapters (Vault, bStats) and relocates packages using Shadow. | Deploying Takion directly on a server or as a base plugin for further customization. |

All modules target **Java 8** using Gradle toolchains, so you can build and run on modern JDKs while remaining compatible with legacy Minecraft hosts.

---

## 🌐 Ecosystem & Bundled APIs

Takion leans on several battle-tested libraries created by the same author:

- [**PrismaticAPI**](https://github.com/CroaBeast/PrismaticAPI) – color gradients, RGB conversion, and mini-message-style formatting.
- [**YAML-API**](https://github.com/CroaBeast/YAML-API) – lightweight YAML configuration helpers and file management.
- [**GlobalScheduler**](https://github.com/CroaBeast/GlobalScheduler) – abstraction for Paper/Spigot task scheduling.
- [**VaultAdapter**](https://github.com/CroaBeast/VaultAdapter) *(plugin module)* – bridges Vault chat/permissions into Takion placeholders.
- [**CommandFramework**](https://github.com/CroaBeast/CommandFramework) and [**AdvancementInfo**](https://github.com/CroaBeast/AdvancementInfo)** (via the shaded distribution) – extendable command and advancement utilities.
- [**bStats**](https://bstats.org/) *(plugin module)* – anonymous usage metrics (relocated to avoid conflicts).

Optional integrations such as **InteractiveChat** or **Vault** can be toggled inside the plugin module without affecting the core API.

---

## 📦 Installation

Add the public repository and choose the dependency that fits your workflow. As of **Takion 1.3**, artifacts live under the
`me.croabeast.takion` group and are published per-module (`core`, `shaded`, `plugin`).

> **Heads up:** You only need either the `shaded` or `plugin` artifact at runtime. The `core` artifact is optional—keep it as
> a `compileOnly` dependency when you want IDE access to the core sources or plan to shade Takion yourself, but it is not
> required on your production server.

### Gradle (Kotlin DSL)
```kotlin
repositories {
    maven("https://croabeast.github.io/repo/")
}

dependencies {
    // Optional: keep core on the compileOnly classpath for source access while shading
    compileOnly("me.croabeast.takion:core:1.3")
    // Choose exactly one runtime: shaded (self-contained) or plugin (ready-to-run)
    implementation("me.croabeast.takion:shaded:1.3")
    // implementation("me.croabeast.takion:plugin:1.3")
}
```

### Gradle (Groovy DSL)
```groovy
repositories {
    maven { url "https://croabeast.github.io/repo/" }
}

dependencies {
    // Optional: keep core on the compileOnly classpath for source access while shading
    compileOnly "me.croabeast.takion:core:1.3"
    // Choose exactly one runtime: shaded (self-contained) or plugin (ready-to-run)
    implementation "me.croabeast.takion:shaded:1.3"
    // implementation "me.croabeast.takion:plugin:1.3"
}
```

### Maven
```xml
<repositories>
    <repository>
        <id>croabeast-repo</id>
        <url>https://croabeast.github.io/repo/</url>
    </repository>
</repositories>

<dependencies>
    <!-- Optional: include core for compilation-time access to the API -->
    <dependency>
        <groupId>me.croabeast.takion</groupId>
        <artifactId>core</artifactId>
        <version>1.3</version>
        <scope>provided</scope>
    </dependency>
    <!-- Choose exactly one runtime: shaded (self-contained) or plugin (ready-to-run) -->
    <dependency>
        <groupId>me.croabeast.takion</groupId>
        <artifactId>shaded</artifactId>
        <version>1.3</version>
    </dependency>
    <!--
    <dependency>
        <groupId>me.croabeast.takion</groupId>
        <artifactId>plugin</artifactId>
        <version>1.3</version>
    </dependency>
    -->
</dependencies>
```

Once declared, reload your project and you are ready to import `TakionLib`.

---

## 🏃 Quickstart

1. **Initialize the library** during your plugin's `onEnable` hook:
    ```java
    public class MyPlugin extends JavaPlugin {
        private TakionLib takion;

        @Override
        public void onEnable() {
            takion = new TakionLib(this);
        }
    }
    ```
2. **Send rich messages** with placeholders and gradients:
    ```java
    takion.getLoadedSender()
          .addPlaceholder("{player}", player.getName())
          .send("<gradient:#8a4dff:#4dfcff>Hello, {player}!<reset>");
    ```
3. **Display titles** with animation timings:
    ```java
    takion.getTitleManager()
          .builder("&dWelcome", "&7Enjoy your stay!")
          .fadeIn(10).stay(60).fadeOut(10)
          .send(player);
    ```

Everything is exposed through `TakionLib`, so once you keep a reference, the rest of the managers are a method call away.

---

## 🧭 Feature Tour

| Capability | What it does |
| ---------- | ------------- |
| **PlaceholderManager** | Register, resolve, and chain placeholders with context-aware values, including Vault/chat integrations when present. |
| **MessageSender** | Compose reusable templates, apply gradients, center text, and deliver to players, console, or audiences. |
| **TitleManager** | Configure fade timings globally and build one-off titles through a fluent builder API. |
| **CharacterManager** | Normalize character widths, handle small capitals, and align text perfectly in chat or GUIs. |
| **ChannelManager** | Define named chat channels, route messages, and attach formatting or permission requirements. |
| **FormatManager & Rules** | Parse simple markup, enforce server-specific rules (`GameRuleManager`), and blend them into outbound messages. |
| **TakionLogger** | Structured logging with optional server/plugin separation, colorized output, and external API hooks. |
| **GlobalScheduler** | Unified async/sync task scheduling compatible with Bukkit, Paper, and Folia environments. |

All components are designed to be modular: you can use them individually or stitch them together for a full chat pipeline.

---

## 🛠️ Building from Source

1. **Clone the repository**:
   ```bash
   git clone https://github.com/CroaBeast/Takion.git
   cd Takion
   ```
2. **Build every module** (jars end up in `*/build/libs`):
   ```bash
   ./gradlew clean build
   ```
3. **Pick your artifact**:
    - `core/build/libs/Takion-<version>.jar` – API only.
    - `shaded/build/libs/Takion-<version>.jar` – shaded distribution.
    - `plugin/build/libs/Takion-<version>.jar` – ready-to-run plugin with relocated packages.

Gradle Wrapper handles dependency downloads, so no additional setup is required.

---

## 🤝 Contributing

We welcome contributions! Before opening a pull request:

1. Discuss major features in [GitHub issues](https://github.com/CroaBeast/Takion/issues) or in the Discord server.
2. Follow the existing code style and prefer Lombok annotations where the project already uses them.
3. Include tests or examples when adding new messaging features or integrations.
4. Run `./gradlew check` to ensure the build stays green.

---

## 📄 License

Takion is distributed under the **GNU General Public License v3.0**. See the [LICENSE](LICENSE) file for full terms. Using the shaded/plugin distributions on a server implies acceptance of the GPLv3 requirements.

Enjoy crafting delightful chat experiences! 💬