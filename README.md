# Discord Music Bot

## Introduction
This project is a Discord music bot built using Java. It enables users to play music from YouTube within a Discord server. The bot is capable of playing a specific song or playlist based on a YouTube search, managing playback controls such as play, skip, list and shutdown, and integrating with Discord's voice channels for live music streaming.

## Features
- Play music via Discord bot from YouTube using URL or search term.
- Queue management with skip functionality.
- Automatic connection to voice channels for audio playback.
- Owner-only shutdown command for bot maintenance.

## Obtaining API Keys
### Getting a Discord Bot Token:
- Go to the Discord Developer Portal (https://discord.com/developers/applications).
- Create a new application and give it a name.
- In the application, navigate to the "Bot" tab and click "Add Bot".
- Under the bot settings, you'll find the token. Click "Copy" to save your bot token.
  
### Getting a Google YouTube API Key:
- Go to the Google Cloud Console (https://console.cloud.google.com/).
- Create a new project.
- Navigate to "APIs & Services" > "Dashboard" and click "ENABLE APIS AND SERVICES".
- Search for "YouTube Data API v3", select it, and click "Enable".
- Go to "Credentials", click "Create credentials", and choose "API key". Your new API key will appear.

## Installation
To install and run this bot, you will need:

- Java JDK 8 or higher
- Maven for dependency management
- A Discord account
- A Discord bot token
- A Google API key for YouTube Data API v3

### Steps:
- Clone the repository to your local machine.
- Navigate to the project directory and run mvn install to install dependencies.
- Adding your Discord userId with your Discord bot token and Google API key in the top of the main class.

Example `main.java`:
```java
public class Main extends ListenerAdapter {
    public static String botToken = "YOUR_DISCORD_BOT_TOKEN";
    public static String googleApi = "YOUR_GOOGLE_API_KEY";
    public static String ownerID = "YOUR_DISCORD_USER_ID";
}
```

## LavaPlayer Integration
This Discord music bot uses LavaPlayer, a versatile audio player library designed for Discord bots. It enables the bot to stream audio from various sources like YouTube and SoundCloud, and handle track loading and playlist management.

### Using LavaPlayer:
- Play audio from different sources including YouTube and SoundCloud.
- Efficient streaming and low-latency audio playback.
- Advanced audio processing and queue management.

## Usage
After setting up the bot in your Discord server, you can use the following commands:
- `$instruction` - Show all the commands and how to use.
- `$play [URL or search term]` - Play music from a given YouTube URL or search term.
- `$skip` - Skip to the next track in the queue.
- `$list` - List all the tracks in the queue.
- `$shutdown` - Shut down the bot (owner only).

## Contributing
If you'd like to contribute to this project, please feel free to fork the repository and submit a pull request with your changes.

