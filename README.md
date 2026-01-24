# Fivemanage Logger - Hytale Server Logging & Analytics

The ultimate logging solution for Hytale dedicated servers. Track player activity, monitor server events, and send logs to Fivemanage, Grafana Loki, or local files.

## Why Use Fivemanage Logger?

- **Track Everything** — Player joins, disconnects, block breaks, and custom events
- **Multiple Backends** — Send logs to Fivemanage, Grafana Loki, or local files
- **Built for Performance** — Async batched logging with zero impact on server TPS
- **Easy Setup** — Drop-in JAR, simple JSON config, works out of the box
- **Open Source** — MIT licensed, customize to your needs

## Features

| Feature | Description |
|---------|-------------|
| **Multi-Provider Support** | Fivemanage, Grafana Loki, file-based logging |
| **Batched Logging** | Configurable buffer size and flush intervals |
| **Player Event Tracking** | Automatic logging of connect, disconnect, ready events |
| **Block Event Tracking** | Track when players break blocks |
| **Dual Logging** | Write to disk while sending to remote providers |
| **Rich Metadata** | Attach custom metadata to all log entries |

## Installation

1. Download the latest release from [Releases](../../releases)
2. Place the `.jar` file in your Hytale server's `mods` directory
3. Create a config file at `mods/Fivemanage_Logger/config.json`

## Quick Start

### Fivemanage Cloud

```json
{
  "LogProvider": {
    "Provider": "fivemanage",
    "ApiKey": "your-fivemanage-api-key",
    "WriteToDisk": false,
    "EnableBatching": true,
    "BufferSize": 10,
    "FlushIntervalMs": 5000
  },
  "PlayerEvents": {
    "Dataset": "player-events",
    "Enabled": true
  }
}
```

Get your API key from [fivemanage.com](https://fivemanage.com).

### Grafana Loki / Grafana Cloud

```json
{
  "LogProvider": {
    "Provider": "grafana-loki",
    "Endpoint": "https://logs-prod-us-central1.grafana.net",
    "Username": "your-grafana-username",
    "Password": "your-grafana-api-key",
    "WriteToDisk": false,
    "EnableBatching": true,
    "BufferSize": 10,
    "FlushIntervalMs": 5000
  },
  "PlayerEvents": {
    "Dataset": "player-events",
    "Enabled": true
  }
}
```

## Configuration Reference

### Log Provider Options

| Option | Type | Description |
|--------|------|-------------|
| `Provider` | string | `fivemanage`, `grafana-loki`, or `file` |
| `ApiKey` | string | API key for Fivemanage |
| `Endpoint` | string | Loki push endpoint URL |
| `Username` | string | Basic auth username (Loki) |
| `Password` | string | Basic auth password (Loki) |
| `WriteToDisk` | boolean | Also write logs to local files |
| `EnableBatching` | boolean | Buffer logs before sending |
| `BufferSize` | integer | Logs to buffer before flush (default: 10) |
| `FlushIntervalMs` | integer | Max time before flush in ms (default: 5000) |

### Player Events Options

| Option | Type | Description |
|--------|------|-------------|
| `Dataset` | string | Dataset/label name for player events |
| `Enabled` | boolean | Enable automatic player event logging |

## Usage in Your Hytale Mod

Use the `FivemanageLogger` API to send custom logs from your own Hytale mods:

```java
import org.fivemanage.FivemanageLogger;
import java.util.Map;

// Log with metadata
Map<String, Object> metadata = Map.of(
    "player", "Steve",
    "location", "x:100, y:64, z:200"
);

FivemanageLogger.info("my-dataset", "Player spawned", metadata);
FivemanageLogger.warn("my-dataset", "Low health warning", metadata);
FivemanageLogger.error("my-dataset", "Something went wrong", metadata);
FivemanageLogger.debug("my-dataset", "Debug info", metadata);
```

## Automatic Event Tracking

When `PlayerEvents.Enabled` is `true`, these events are logged automatically:

| Event | Description |
|-------|-------------|
| **Player Connect** | When a player connects to the server |
| **Player Ready** | When a player is fully loaded and ready |
| **Player Disconnect** | When a player leaves the server |

## Use Cases

- **Server Administration** — Monitor player activity and server health
- **Anti-Cheat Logging** — Track suspicious player behavior
- **Analytics Dashboard** — Build dashboards with Grafana
- **Audit Trails** — Keep records of all server events
- **Debugging** — Track down issues in your Hytale mods

## Related

- [Fivemanage Platform](https://fivemanage.com)
- [Fivemanage Lite](https://github.com/fivemanage/lite)
- [Grafana Loki](https://grafana.com/oss/loki/)

## Creds
- [Hytale WebServer Plugin](https://github.com/nitrado/hytale-plugin-webserver)
- [Hytale Modding Documentation](https://hytalemodding.dev)

## Support

- Website: [fivemanage.com](https://fivemanage.com)
- Email: support@fivemanage.com

## License

MIT
