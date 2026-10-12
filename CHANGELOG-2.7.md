# Mekanism Version Locked — 2.7

- Recalculate electrolytic separator speed, operation count and gas dumping rate before recalculating its energy buffer. Speed upgrades now increase sustained processing without the old unscaled dumping bottleneck.
- Clamp chemical dumping to stored contents, preventing negative amounts when the final chemical units are discarded.
- Retain the 2.6 stable separator capacity calculation and surplus repair, the 2.5 legacy chemical extraction fix and previous resistive-heater settings.
- Restore SPS lightning, central energy effect and orbit effects through the Minecraft 26.1 rendering submission API, using an emissive textured pipeline. Avoid loading the obsolete billboard class targeted by a legacy Iris integration. Iris itself is unchanged.
- Add connected structural glass models that remove internal borders between adjacent glass blocks without changing block IDs or world data.

Validation: dedicated-server tests verify actual separator energy consumption matches its usage reading, higher sustained processing with maximum upgrades, stable capacity and legacy chemical transfers. Client tests with Iris and Sodium verify custom glass models bake and change when connected, and that the SPS submits lightning and central-effect vertices. Visual appearance in an active multiplayer SPS still needs confirmation in the user's world.

Update clients and server together, back up and replace the previous Mekanism JAR. Install Generators 1.1 alongside core 2.7 for the turbine and reactor-glass fixes. Keep only one JAR per mod. This remains a community Version Locked build for Minecraft 26.1.2 / NeoForge / Java 25, with original credits and MIT license preserved.
