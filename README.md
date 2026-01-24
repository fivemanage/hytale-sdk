# Fivemanage Logger for Hytale

A logging SDK for Hytale servers with support for multiple providers including Fivemanage, Grafana Loki, and local file logging.

## Features

- **Multiple Log Providers**: Fivemanage, Grafana Loki, and file-based logging
- **Batched Logging**: Efficient log batching with configurable buffer size and flush intervals
- **Player Event Tracking**: Automatic logging of player connect, disconnect, and ready events
- **Block Event Tracking**: Track block break events
- **Dual Logging**: Optionally write logs to disk while sending to remote providers
- **Metadata Support**: Attach custom metadata to all log entries

## Installation

1. Download the latest release from [Releases](../../releases)
2. Place the `.jar` file in your Hytale server's `mods` directory
3. Create a config file at `mods/Fivemanage_Logger/config.json`

## Configuration

### Fivemanage

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

### Grafana Loki

```json
{
  "LogProvider": {
    "Provider": "grafana-loki",
    "Endpoint": "https://your-loki-instance.com",
    "Username": "your-username",
    "Password": "your-password",
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

For Grafana Cloud, use your Grafana Cloud Loki URL as the endpoint and your Grafana Cloud credentials.

### Configuration Options

| Option | Type | Description |
|--------|------|-------------|
| `Provider` | string | `fivemanage` or `grafana-loki` |
| `ApiKey` | string | API key for Fivemanage |
| `Endpoint` | string | Loki push endpoint URL |
| `Username` | string | Basic auth username (Loki) |
| `Password` | string | Basic auth password (Loki) |
| `WriteToDisk` | boolean | Also write logs to local files |
| `EnableBatching` | boolean | Buffer logs before sending |
| `BufferSize` | integer | Logs to buffer before flush (default: 10) |
| `FlushIntervalMs` | integer | Max time before flush in ms (default: 5000) |

### Player Events

| Option | Type | Description |
|--------|------|-------------|
| `Dataset` | string | Dataset/label name for player events |
| `Enabled` | boolean | Enable automatic player event logging |

## Usage

### Logging API

```java


import java.util.Map;

// Log with metadata
Map<String, Object> metadata = Map.of(
        "player", "Steve",
        "location", "x:100, y:64, z:200"
);

FivemanageLogger.

        info("my-dataset","Player spawned",metadata);
FivemanageLogger.

        warn("my-dataset","Low health warning",metadata);
FivemanageLogger.

        error("my-dataset","Something went wrong",metadata);
FivemanageLogger.

        debug("my-dataset","Debug info",metadata);
```

### Automatic Event Tracking

When `PlayerEvents.Enabled` is `true`, these events are logged automatically:

- **Player Connect** — when a player connects
- **Player Ready** — when a player is fully loaded
- **Player Disconnect** — when a player leaves
- **Block Break** — when a player breaks a block

## Support

- Website: [fivemanage.com](https://fivemanage.com)
- Email: support@fivemanage.com

## License

MIT
