# Noverworld

When you create a new world your hotbar is replaced with the ninth saved creative hotbar (set with `c + 9`).
If you don't have a hotbar saved to that slot (ie. all 9 items empty) Noverworld will automatically save and use the following one:

![Default Hotbar 9](assets/default_hotbar_9.png)

You will spawn in the nether inside a portal with a random yaw (facing angle) and y height. _(These values are
deterministic based on your world seed, meaning creating a world with the same seed will yield the same yaw and targeted
y height. Therefore the mod is suitable for SSG)_

Depending on the version you are using
the f3 menu and pie chart will be automatically opened for you.

Before you join a new world the mod saves your render distance and FOV and resets to this after you leave the world.
_ie. this means you can configure the RD and FOV you will always spawn with by changing it in the main menu, and you
don't have to reset these settings to your preferred defaults in between runs._

The y height of the portal the mod attempts to target is determined using the following distribution:
- 80% chance of between 7-13
- 5% chance of between 14-59
- 10% chance of between 60-75
- 5% chance of between 76-90

The distribution was chosen in a fairly arbitrary manner. Make an issue if you want it changed.

This mod has been tested with:
- Sodium
- Lithium
- Starlight
- FastReset
- LazyDFU
- Moonlight (threaded stronghold gen)
- Chunk mod (puts you in the world before spawn chunks have finished generating)

I will not support multithreaded world gen such as C2ME.

Contributions are welcome.