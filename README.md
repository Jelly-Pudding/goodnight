# Goodnight Plugin
**Goodnight Plugin** is a simple Minecraft Paper 26.1.2 plugin that allows players to vote skip the night by typing /goodnight. Although it was custom built for [minecraftoffline.net](https://www.minecraftoffline.net), any server can use it.

## Installation
1. Download the latest release [here](https://github.com/Jelly-Pudding/goodnight/releases/latest).
2. Place the `.jar` file in your Minecraft server’s `plugins` folder.
3. Restart your server.

## Configuration
In `config.yml`, set the percentage of players required to skip the night (default is 66.6%):

```yaml
sleep-percentage: 66.6
```

## Commands
- `/goodnight [custom message]`: Announces that a player is ready to skip the night. A custom message can be optionally added.

## Permissions
- `goodnight.use`: Allows the player to use the goodnight command (default: true)

## Support Me
[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/K3K715TC1R)
