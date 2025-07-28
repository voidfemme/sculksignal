# SculkSignal

A Minecraft fabricmod  that enables long-distance calibrated sculk sensor signal transmission through intelligent chunk loading.

## Features

- Manual registration of calibrated sculk sensors
- Automatic network topology calculation
- Just-in-time chunk loading for signal propagation

## Commands

- `/sculksignal register` - Register the calibrated sculk sensor you're looking at
- `/sculksignal unregister` - Unregister the sensor you're looking at  
- `/sculksignal list` - List all registered sensors
- `/sculksignal info` - Show info about the sensor you're looking at
- `/sculksignal reload` - Reload configuration (admin only)
- `/sculksignal ping all` - get a live update whenever a registered sculk sensor
  activates.

## Building

```bash
./gradlew build
```

## Note
This was going to be a paper plugin until I learned that paper messes with
vanilla redstone mechanics. I haven't updated any of the code to reflect that
change yet, but I'm migrating to the Fabric API for this project.

## License

CC0 1.0 Universal - Public Domain Dedication

This work has been released into the public domain.
