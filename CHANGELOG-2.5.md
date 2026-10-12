# Mekanism Version Locked — 2.5

- Fix `IncompatibleClassChangeError` when legacy chemical consumers drain a chemical container item.
- Add concrete transfer dispatch in `ComponentBackedResourceHandler` to resolve competing modern and legacy interface defaults.
- Preserve transactions, automatic transfer limits, and the `EXTERNAL` automation mode. All other JAR entries remain identical to Version Locked 2.4.
- Retain previous chemical compatibility, recipe cache fixes and heater configuration.

Validation: tested the original Trash Cans `drainGasFromItem` call, simulation without consumption, permitted extraction amounts, rollback and filters. The complete archived ATM11 server started and completed `save-all flush` with Mekanism 2.5 and Pipez Optimizer port3.

Update clients and server together. Remove the older Mekanism JAR from `mods`; do not install duplicate copies. Keep the original Trash Cans, Mekanism Extras, MoreMachine and other addons.

Community Version Locked build for Minecraft 26.1.2 / NeoForge / ATM11 0.9.0. Original Mekanism credits and MIT license preserved. No modified Trash Cans JAR is required.
