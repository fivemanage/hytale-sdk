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
| **Player Event Tracking** | Automatic logging of connect, disconnect, session durations |
| **Death Events** | Track when players are killed and kill other players |
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

## Session duration
The SDK will automatically store each player when they connect and record their session. Once they disconnect, we send the duration to your selected provider.

## Player Events

When `PlayerEvents.Enabled` is `true`, these events are logged automatically:

| Event | Description |
|-------|-------------|
| **Player Connect** | When a player connects to the server |
| **Player Ready** | When a player is fully loaded and ready |
| **Player Disconnect** | When a player leaves the server |


## Death Events
The Death System automatically logs detailed information when a player dies. These events can help track PvP deaths, NPC kills, causes of death, and more.

### Death Event Metadata

When a player dies, the following information is captured (when applicable):

| Field            | Type    | Description                                               |
|------------------|---------|-----------------------------------------------------------|
| `playerName`     | string  | Name of the player who died                               |
| `playerId`       | string  | UUID of the player who died                               |
| `killerName`     | string  | Name of the killer (if killed by another player)          |
| `killerId`       | string  | UUID of the killer (if killed by another player)          |
| `killerType`     | string  | "player" or "npc" depending on killer entity              |
| `killedBy`       | string  | NPC role or type name if killed by an NPC                 |
| `deathAmount`    | number  | Amount of damage taken before death                       |
| `deathCauseId`   | string  | Cause ID (e.g., "physical", "fire", etc.)                 |

#### Example Death Event

```json
{
  "playerName": "Steve",
  "playerId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "killerName": "Alex",
  "killerId": "yyyyyyyy-yyyy-yyyy-yyyy-yyyyyyyyyyyy",
  "killerType": "player",
  "deathAmount": 18,
  "deathCauseId": "Physical"
}
```

If killed by an NPC:

```json
{
  "playerName": "Steve",
  "playerId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "killedBy": "Skeleton_Incandescent_Fighter",
  "killerType": "npc",
  "deathAmount": 6,
  "deathCauseId": "Physical"
}
```

> **Note:** Some fields may be omitted depending on the specific death circumstances (e.g., no `killerName` if there was no killer).

These logs appear under the dataset and label you configure for player events (e.g., `player.died`). You can use this data for analytics, kill feeds, PvP stats, auditing, or integrations with your logging/monitoring stack.



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
