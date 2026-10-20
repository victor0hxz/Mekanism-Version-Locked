# Mekanism Version Locked 2.11

For Minecraft 26.1.2 / NeoForge. Includes all fixes from 2.10.

- Fix loading with original XyCraft Machines 0.9.2: restore the canonical NeoForge ResourceHandler type for the shared chemical capability. Legacy Mekanism handler adapters remain available.
- Bridge XyCraft's invalid chemical-empty sentinel when constructing its chemical void handler. The bridge is optional and only targets that integration; NeoForge's resource validation remains intact.
- No changes to original ATM11 scripts, XyCraft JARs, Trash Cans, or Logistics Networks.

Validation: reproduced the original loading crash, then passed 33 functional checks with original XyCraft 0.9.2 and Trash Cans 1.1.0, including chemical insertion into XyCraft's void handler and transaction rollback. Also passed 30 checks without XyCraft installed.

Known limits: XyCraft 0.9.2 also ships legacy recipe JSON and references aluminum slurries that its chemical register does not register. Those recipe errors are separate from this loading fix. The reported 688 KubeJS errors have not been reproduced or verified as resolved; no player logs were available.
