# INDICA for Minecraft 1.21.4

INDICA is a lightweight addon module for the [Meteor Client](https://meteorclient.com/), built specifically for Minecraft 1.21.4.

## 🔍 Features

- Ominous Vault ESP: Highlights Ominous Vaults in the world when nearby.
- Shulker-Frame ESP: Highlights (Glow-)Itemframes but only if they contain a Shulkerbox.
- Kill Effects: Displays visual and audio effects when selected entities die.
  - Entity Effects: Spawn lightning bolts at death locations
  - Particle Effects: Dynamic particle selection
  - Sound Effects: Entity sound selection with volume control
  - Entity Filter: Configure which entity types trigger effects
- Respawn Point Blocker: Attempts to block interactions with beds and/or respawn anchors.
- Map Copier: Automatically duplicates all maps in the players inventory.
- Inventory Notifs: Plays a Soundqueue on different events inside your inventory.

## 📦 Installation

  1. Make sure you have the Meteor Client set up and working.
  2. Make sure you have the latest [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api/files) installed - i recommend fabric-api-0.119.3+1.21.4.jar
  3. Clone this repository or download the ZIP.
  4. Unpack the .zip
  5. Run cmd in the main folder
  6. Build the module using Gradle:

    gradlew.bat build

  7. Grab the .jar from "build/libs" and place it into your Minecraft mods folder.
  8. done

## Known Issues

- none :)

## License

This project is licensed under the [GNU GPL v3.0](LICENSE).  
All previous commits and tags are retroactively covered by this license.