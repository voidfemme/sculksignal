# SculkSignal

A Minecraft Paper plugin that enables long-distance calibrated sculk sensor signal transmission through intelligent chunk loading.

## Features

- Manual registration of calibrated sculk sensors
- Automatic network topology calculation
- Just-in-time chunk loading for signal propagation
- Support for frequencies 9-13 (block events)
- Persistent network storage
- Administrative commands

## Commands

- `/sculksignal register` - Register the calibrated sculk sensor you're looking at
- `/sculksignal unregister` - Unregister the sensor you're looking at  
- `/sculksignal list` - List all registered sensors
- `/sculksignal info` - Show info about the sensor you're looking at
- `/sculksignal reload` - Reload configuration (admin only)

## Building

```bash
./gradlew build
```

## Installation

1. Build the plugin or download from releases
2. Place the JAR in your server's `plugins/` folder
3. Restart the server
4. Configure in `plugins/SculkSignal/config.yml` if needed

## License

CC0 1.0 Universal - Public Domain Dedication

This work has been released into the public domain.
