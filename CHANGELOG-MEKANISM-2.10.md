# Mekanism Version Locked 2.10 — ATM11 API compatibility (candidate)

## Fixed
- Restore the deprecated `ChemicalBuilder.ore(String)` API required by stock ATM11 KubeJS startup scripts.
- Accept item-tag IDs such as `c:ores/crimson_iron` and legacy `#c:ores/...` syntax.
- Preserve the slurry ore representation as a fallback in the Chemical Crystallizer GUI and recipe viewer when no chemical-solid-tag data map entry is present.
- Keep current NeoForge chemical-solid-tag data maps authoritative and compatible with reloads.

## Regression tests
- Test the legacy ore builder API and invalid tag handling.
- Full Gradle build and dedicated ATM11 integration test **not yet confirmed**.

## Known limits
- The KubeJS `redeclaration of var Chemical` JavaScript-scope error cannot be repaired by changing the Mekanism API.
- Missing `extradisks:infinite_chemical_storage_*` entries are owned by Extra Disks; Mekanism cannot safely recreate another mod's items.
- Keep the original ATM11 files unchanged; the above issues require separate compatibility investigation if still reproducible.

This is a pre-release source change; not a certified client/server build.
