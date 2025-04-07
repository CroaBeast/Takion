# Takion

Takion is a comprehensive Spigot library designed to streamline the development of text and chat-related plugins for Minecraft servers. It provides a robust set of tools and utilities to manage messaging, logging, placeholder replacement, and more, making it an essential core for enhancing chat interactions within Minecraft.

## Purpose

Takion aims to simplify the creation and management of chat-based functionalities in Minecraft plugins. It serves as a foundational library that developers can leverage to implement advanced text and chat features efficiently.

## Features

### Logging
- **TakionLogger**: Provides advanced logging capabilities with dynamic message formatting, colorization, and integration with external APIs (e.g., Paper and Prismatic).
- Supports both server-level and plugin-specific logs.

### Channel Management
- **ChannelManager**: Manages communication channels, defining and identifying different channels for player interactions.

### Title Management
- **TitleManager**: Configures and displays titles to players, with customizable fade-in, stay, and fade-out durations.

### Placeholder Management
- **PlaceholderManager**: Dynamically replaces tokens in messages with actual values, facilitating personalized and context-aware messaging.

### Character Management
- **CharacterManager**: Handles text alignment and formatting, including support for small capital conversions and custom character lengths.

### Messaging
- **MessageSender**: Sends formatted messages to players, with support for placeholders and advanced text processing.

### Text Processing
- Utilizes **PrismaticAPI**, **StringApplier**, and related utilities for colorization and string modifications, enabling rich text formatting.

## Installation

### Maven

To include Takion in your project, add the following repository and dependency to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>croabeast-repo</id>
        <url>https://croabeast.github.io/repo/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>me.croabeast</groupId>
        <artifactId>Takion</artifactId>
        <version>1.0</version>
    </dependency>
</dependencies>
```

### Gradle

To include Takion in your project, add the following to your `build.gradle`:

```groovy
repositories {
    maven {
        url 'https://croabeast.github.io/repo/'
    }
}

dependencies {
    implementation 'me.croabeast:Takion:1.0'
}
```

## Usage

### Initialization

Initialize TakionLib in your plugin's `onEnable` method:

```java
@Override
public void onEnable() {
    TakionLib lib = new TakionLib(this);
    // Additional initialization code...
}
```

### Sending Messages

Send a message to a player:

```java
lib.getLoadedSender().addPlaceholder("{player}", player.getName())
    .send("Hello, {player}! Welcome to our server.");
```

### Displaying Titles

Format a title and send it to a player:

```java
lib.getTitleManager().builder("Welcome", "Enjoy your stay!").send(player);
```

### Character Management

Use the `CharacterManager` to manage text alignment and formatting:

```java
String alignedText = lib.getCharacterManager().align("Centered Text");
player.sendMessage(alignedText);
```

### CollectionBuilder

Example of using `CollectionBuilder`:

```java
List<String> list = CollectionBuilder.of("Item1", "Item2", "Item3").toList();
player.sendMessage(String.join(", ", list));
```

## Dependencies

Takion includes several dependencies that are compiled and included within the library, so you don't need to add them separately. The primary dependencies are:

- [**PrismaticAPI**](https://github.com/CroaBeast/PrismaticAPI): For colorization and text modifications.
- [**CommandFramework**](https://github.com/CroaBeast/CommandFramework): For handling commands.
- [**YAML-API**](https://github.com/CroaBeast/YAML-API): For YAML configuration support.
- [**InventoryFramework**](https://github.com/stefvanschie/IF): For managing and interacting with Minecraft inventories.
> Note: UpdateChecker was originally created by Choco. For more details, see [this post](https://www.spigotmc.org/threads/an-actually-decent-plugin-update-checker.344327/).

## Optional Dependencies

While the following dependencies are optional and provided, they are not required for the core functionality of Takion:

- **VaultAPI**: For integrating with external permission and chat APIs.
- **InteractiveChat**: For enhancing chat interactions.

## Contributing

Contributions are welcome! Please fork the repository and submit pull requests with your changes. For major changes, please open an issue first to discuss what you would like to change.

## License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](https://www.gnu.org/licenses/gpl-3.0.html) file for details.

