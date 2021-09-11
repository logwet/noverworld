# Noverworld

## About

When you create a new world, you will spawn in the nether at a portal.
Your inventory contains a set of standard nether entry items you would have in a usual hypermodern RSG situation where
you intend to do bastion trades. Beat the dragon and reach the credits as usual.

![inventory](assets/inventory.png)  
_The default inventrory_

You cannot customise the number, type or durability of the items in your inventory but you can adjust what slot they're in.
You do this through editing `config/noverworld.json`

```json
{
  "inventory": [
    {
      "name": "WOODEN_AXE",
      "slot": 1
    },
    ...
    {
      "name": "BREAD",
      "slot": 41
    }
  ]
}
```

![inventory_mapping](assets/inventory_mapping.png)

_The slots to inventory mapping. ie. if you want to put something in the hotbar, use slot 41._

Before you join a new world the mod saves your render distance and FOV, and resets to this after you leave the world.
_ie. this means you can configure the RD and FOV you will always spawn with by changing it in the main menu, and you
don't have to reset these settings to your preferred defaults in between runs._

Depending on the version you are using the f3 menu will be automatically opened for you.

When you spawn your yaw (facing angle) is randomised. The y height of the portal the mod attempts to target is also randomly determined using
the following distribution:
- 80% chance of between 7-13
- 5% chance of between 14-59
- 10% chance of between 60-75
- 5% chance of between 76-90
- The distribution was chosen in a fairly arbitrary manner. Make an issue if you want it changed.

All random values in the mod are deterministically derived from your world seed, meaning creating a world with the same
seed will yield the same yaw and targeted y height. Therefore, the mod is suitable for SSG

## Support

If you have a problem with the mod, a question or experience a crash follow these steps:
1. Check the README to see if there's any relevant info on there, particularly the FAQ section.
2. Check the GitHub Issues (including closed issues).
3. Check the GitHub Releases to see if a newer build has been released that addresses your problem.
4. If you're experiencing a crash, make a GitHub issue and include the crash report.
5. If none of the above steps are helpful, DM me on discord (I'm in HBG Hub, Javacord etc. under this name).

## FAQ

- How do I download the mod?
  - The [releases](https://github.com/logwet/noverworld/releases/) page on this GitHub repo
- How do I change the hotbar/inventory?
  - Using the config file at `config/noverworld.json`.
- How do I reset my hotbar to the default included in the mod?
  - Delete the user config file at `config/noverworld.json`. When you create a new world or reload the game the defaults will be applied.
- How do I set my FOV and render distance default?
  - Edit those values in the game's main menu (ie. outside of a world) and they'll be saved.
- I have a suggestion for improvement.
  - Make a GitHub issue and include as much information as you can.
  - Or message me on Discord.
- The mod is crashing while launching with `java.lang.reflect.InvocationTargetException`
  - Make sure you are running the release version of the mod ie. `noverworld-x.x.x.jar` instead of `noverworld-x-x-x-dev.jar`
- Is this incompatible with any mods:
  - This mod has been tested and confirmed to work with:
    - Sodium
    - Lithium
    - Starlight
    - FastReset (doesn't save world)
    - AutoReset (automatically reset worlds w/o macro) ___note: some users have reported problems with this mod___
    - LazyDFU
    - Moonlight (threaded stronghold gen)
    - Chunk mod (puts you in the world before spawn chunks have finished generating)
  - I will not support multi-threaded world gen such as C2ME. Perhaps sometime in the future as a separate branch.

Contributions are welcome. This mod is licensed under the GPL-3.0, meaning any forks/derivative works must also be open
source and licensed under the GPL-3.0. If you fork the mod and publish the distribution (without the intention of
merging your changes upstream) please change the name from Noverworld to a suitably distinct alternative to avoid confusion.
