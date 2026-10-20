# Reproducing the 2.11 compatibility overlay

Base: released `Mekanism-Version-Locked-26.1.2-2.10.jar`, SHA-256 `81f6c4bc0b3c677b746157e1b52fb3f59c1d4f12d31f2b4ef9caa1a742b6a910`.

The source `Capabilities.CHEMICAL` already uses `ResourceHandler.asClass()`. The published 2.10 binary instead used `IChemicalHandler.class` as its shared capability type. The 2.11 overlay changes only that class literal in `Capabilities.<clinit>` to an interface static invocation of `ResourceHandler.asClass()`. All other provider adapters and bytecode remain unchanged.

Compile `XyCraftChemicalVoidMixin.java` against the base and NeoForge 26.1.2.109, add its class and `mekanism-xycraft.mixins.json`, and register the config in `META-INF/neoforge.mods.toml`. Update `Version-Locked-Release` in the manifest to 2.11. API version remains 10.8.0.

The artifact audit requires exactly three changed existing entries (Capabilities, manifest, mod TOML) and two new entries (optional mixin and config). Original XyCraft and Trash Cans JARs are private test dependencies, never included in the artifact.

Runtime evidence: baseline reproduces the capability type mismatch; changing only the type exposes XyCraft's invalid empty sentinel; the final bridge starts successfully. There are 33 functional assertions with XyCraft and 30 without it. Tests cover shared capability identity, empty sentinel, actual XyCraft void insertion, original Trash Cans legacy handlers, filtering and nested transaction rollback. Separate XyCraft recipe errors remain documented in the changelog.
