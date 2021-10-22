# Noverworld

[![Build and Release Artifacts](https://github.com/logwet/noverworld/actions/workflows/build.yml/badge.svg)](https://github.com/logwet/noverworld/actions/workflows/build.yml)

- [Noverworld](#noverworld)
  * [About](#about)
  * [Creating your own custom inventory](#creating-your-own-custom-inventory)
  * [Support](#support)
  * [FAQ](#faq)

## About

When you create a new world, you will spawn in the nether at a portal.
Your inventory contains a set of standard nether entry items you would have in a usual hypermodern RSG situation where
you intend to do bastion trades. Beat the dragon and reach the credits as usual.

![bastion inventory](assets/bastion_inventory.png)

_The bundled inventory for the `Bastion`/default category_

![monument inventory](assets/monument_inventory.png)

_The bundled inventory for the `Monument` category_

![classic inventory](assets/classic_inventory.png)

_The bundled inventory for the `Classic` category_

You cannot customise the number, type or durability of the items in your inventory but you can adjust what slot they're in.
You do this through editing `.minecraft/config/noverworld-X.X.X.json`.

_(At the moment the slot of non-unique items, eg. the beds in classic, are not directly configurable. They are 
automatically put into the first free slot, so you can move them around by editing the location of the unique items.)_

```json
{
  "f3Enabled": true,
  "recipeBookEnabled": true,
  "inventory": [
    {
      "name": "wooden_axe",
      "slot": 1
    },
    ...
    {
      "name": "bread",
      "slot": 41
    }
  ]
}
```

![inventory_mapping](assets/inventory_mapping.png)

_The slots to inventory mapping. ie. if you want to put something in the hotbar, use slot 41._

Every new release of noverworld will create a new config file, so if you want to use your slot config from the previous
version copy it over. This is to allow players to easily use different releases of the mod (eg. Bastion to Monument)
at the same time without having to delete their config file every time.

Before you join a new world the mod saves your render distance and FOV, and resets to this after you leave the world.
_ie. this means you can configure the RD and FOV you will always spawn with by changing it in the main menu, and you
don't have to reset these settings to your preferred defaults in between runs._

Depending on the version you are using the f3 menu will be automatically opened for you.

When you spawn your yaw (facing angle) is randomised. The y height of the portal the mod attempts to target is also
randomly determined using a different distribution per release. You can see this distribution in
[`fixed_config.json`](src/main/resources/fixed_config.json) _(Make sure to switch to the right branch)_

All random values in the mod are deterministically derived from your world seed, meaning creating a world with the same
seed will yield the same yaw and targeted y height. Therefore, the mod is suitable for SSG

## Creating your own custom inventory

```json
"spawnShiftRange": {
  "50-100": 60,
  "101-250": 40
}
```

This determines the distance the player's spawn will be shifted from the world spawn (radius). The key is a range in the format "min-max" and
the value is the relative weight that should be given to that range.

```json
"spawnYHeightDistribution": {
  "7-13": 80,
  "14-59": 5,
  "60-75": 10,
  "76-90": 5
}
```

This determines the player's targeted y height when generating a portal. The key is a range in the format "min-max" and
the value is the relative weight that should be given to that range.

```json
"playerAttributes": {
  "health": 17.0,
  "hunger": 15.0,
  "saturation": 3.0
}
```

This determines the player's health, hunger and saturation.

```json
{
  "name": "iron_boots",
  "tags": "{Enchantments:[{id:soul_speed,lvl:1}]}",
  "count": "0-4",
  "damage": 20,
  "slot": 36,
  "editable": true,
  "unique": true
}
```

| Key               	| Value                                                                                                                                                                                       	|
|-------------------	|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------	|
| name              	| The name of the item in the registry eg. `"iron_axe"`. If you want to add an item from outside of minecraft (eg. from a mod) you need to include the mod id like this: `"modid:moddeditem"` 	|
| tags (optional)   	| The nbt tags of the item, in JSON notation. Useful for things like enchantments. You shouldn't do damage/durability through here, do it using the `"damage"` key.                           	|
| count             	| A range in the format `"min-max"`. The number of items will be randomly chosen from that range using a uniform distribution.                                                                	|
| damage (optional) 	| Use this to change the durability of the item. Ie. a pickaxe which has mined 10 blocks will have 10 damage.                                                                                 	|
| slot              	| The slot the item occupies in the inventory. Note that while the user config starts counting from 1, `fixed_config` follows Java array conventions. Therefore, slot 1 of the hotbar = 0.    	|
| editable          	| Whether the slot of this item should be user configurable.                                                                                                                                  	|
| unique            	| Whether this item is unique or is one of many of the same type (eg. multiple beds). Note: you can make the first of a set of multiple items unique and thus user configurable.              	|

## Support

If you have a problem with the mod, a question or experience a crash follow these steps:
1. Check the README to see if there's any relevant info on there, particularly the FAQ section.
2. If you have a question, ask it on the GitHub Discussions Q&A section [here](https://github.com/logwet/noverworld/discussions/categories/q-a).
3. Check the GitHub Releases to see if a newer build has been released that addresses your problem.
4. If you're experiencing a crash, make a GitHub issue and include the crash report.
5. If none of the above steps are helpful, DM me on discord (I'm in HBG Hub, Javacord etc. under this name). I don't mind being pinged for help requests either.

## FAQ

- How do I download the mod?
  - The [releases](https://github.com/logwet/noverworld/releases/) page on this GitHub repo.
- What file do I download? There's a bunch.
  - There are three groups, one for each category (`BASTION`, `MONUMENT` and `CLASSIC`). Refer to the start of this README for the differences between each category.
  - Then download the release version of the mod ie. `noverworld-x.x.x.jar` instead of `noverworld-x-x-x-dev.jar` and put it in your mods folder.
- How do I change the hotbar/inventory?
  - Using the config file at `.minecraft/config/noverworld-X.X.X.json`.
- How do I change what items are in my hotbar/inventory?
  - You can't easily do so. The three Noverworld categories have fixed inventories, you can change the slots your items are in but not their type, durability or count.
  - If you __really__ want to change the items, you can do so by editing `fixed_config.json` and recompiling the mod, but beware that this will make your runs unverifiable for the leaderboards.
    - See the [Creating your own custom inventory](#creating-your-own-custom-inventory) section for more information.
    - If you don't have a java and fabric development environment, I believe you can still edit the file by renaming the `.jar` to a zip, editing the file then rezipping and renaming to `.jar`.
- How do I reset my hotbar to the default included in the mod?
  - Delete the config file at `.minecraft/config/noverworld-X.X.X.json`. When you create a new world or reload the game the defaults will be applied.
- I updated the mod and my inventory slot config has been reset, what gives?
  - Every new version of the mod writes a new file, just copy your settings over. Read above for an explanation of why.
- How do I set my FOV and render distance default?
  - Edit those values in the game's main menu (ie. outside of a world) and they'll be saved.
- I want to run noverworld without f3
  - Set `f3Enabled` to `false` in the config file. This won't automatically open f3 before you spawn.
- I don't book craft so I don't want the recipe book to be automatically opened by default.
  - Set `recipeBookEnabled` to `false` in the config file. This won't automatically open the recipe book pane before you spawn.
- I have a suggestion for improvement.
  - Put it on the Github Discussions Idea section [here](https://github.com/logwet/noverworld/discussions/categories/ideas)
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
    - Chunk Mod (delay spawn chunk gen until after spawning). Tested with noverworld 2.3.0, may be compatible with older versions. Depending on your system the performance improvement from this can be quite dramatic eg. 4x faster loads.
    - Moonlight (threaded stronghold gen)
  - I will not support multi-threaded world gen such as C2ME. Perhaps sometime in the future as a separate branch.

Contributions are welcome. This mod is licensed under the GPL-3.0, meaning any forks/derivative works must also be open
source and licensed under the GPL-3.0. If you fork the mod and publish the distribution (without the intention of
merging your changes upstream) please change the name from Noverworld to a suitably distinct alternative to avoid confusion.
