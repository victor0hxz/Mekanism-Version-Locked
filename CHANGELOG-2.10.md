# Mekanism Version Locked 2.10

- Restore legacy `ChemicalBuilder.ore(...)` compatibility for pack scripts that register ore-processing slurries. Modern chemical solid-tag data maps remain authoritative.
- Accept legacy 24-bit chemical tint values consistently in development and production.
- Work around the installed Rhino block-scoped `const` regression for the exact `Chemical` and `ChemicalBuilder` Java aliases in Mekanism startup scripts. The workaround runs in memory, preserves diagnostic positions and disables itself when the Rhino feature probe succeeds.
- Let older recipe-viewer scripts hide the three retired ExtraDisks infinite chemical-storage items when those items are absent. Other invalid ingredients still report errors; no replacement items are registered.
- Guard optional KubeJS/JEI integrations against incompatible API changes. Mekanism remains usable without these optional mods.
- Restore the missing source of the legacy `Action` enum already present in earlier distribution JARs, preserving its existing binary contract.
- Preserve the original ATM11 script files and all previous Mekanism Version Locked 2.9 fixes. KubeJS, Rhino, ExtraDisks and Better Advanced Tooltips remain original dependencies and are not bundled or modified.

Original Mekanism authors, credits and license are preserved. This is an unofficial compatibility release for Minecraft 26.1.2 and Java 25.
