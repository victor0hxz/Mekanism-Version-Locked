# Mekanism Version Locked 2.4

Minecraft 26.1.2 / NeoForge / ATM11 0.9.0.

- Fixed chemical ejection crashes when an addon exposes the legacy IChemicalHandler interface.
- The Mekanism-side legacy handler now participates in NeoForge resource transactions, including ordered root and nested rollback.
- Restored ChemicalStack compatibility methods used by compiled addon filters and display tanks.
- Modern chemical item containers also expose the legacy API; capability requests without ItemAccess receive a compatible access context.
- Verified chemical transfers, legacy storage rollback, filters and nuclear waste rejection using the ORIGINAL Trash Cans 1.1.0, without changing its JAR.
- Retained the 2.3 custom-ingredient cache fix and all earlier heater settings.

No modified Trash Cans JAR is required. Keep the original Trash Cans and update Mekanism on both clients and server.
Original Mekanism credits and MIT license preserved.
