# 🟣 Simple Hytale Twitch Integration
*Based on [hytale-template-plugin](https://github.com/realBritakee/hytale-template-plugin)*

Connect your stream directly to Hytale!

### ✨ Current Features

In-Game Messages:

*   Display Twitch messages highlighted via Channel Points rewards directly inside the game.

### 📂 Configuration

Run the server once. A HytaleTwitch folder should appear in your `/mods/` directory, containing this `config.json`:

```
{
  "accessToken": "",
  "rewardName": ""
}
```

*   **accessToken**: Input your token with the correct scopes ([generate one here](https://twitchtokengenerator.com/)).
*   **rewardName**: Must match your Twitch reward name **EXACTLY**.

⚠️ Don't forget to enable user input for your Twitch reward, even though it won't be able to display any message in the game (obviously). ⚠️

### 🚀 Roadmap

I plan to add many more interactions to spice up gameplay with chat.

## 💡 Suggestions?

Feel free to open an issue to ask for new features or suggest improvements!

# For devs

## Prerequisites

- **Java 25 JDK**
- **Gradle** (wrapper included)

## Build

```bash
# Windows
gradlew.bat shadowJar

# Linux/Mac
./gradlew shadowJar
```

The plugin JAR will be generated in `build/libs/SimpleTwitch-1.0.0.jar`

## Local Testing

```bash
# Windows
gradlew.bat runServer

# Linux/Mac
./gradlew runServer
```

## Contributing

Contributions are welcome! Feel free to open issues or submit pull requests.

## License

MIT License
