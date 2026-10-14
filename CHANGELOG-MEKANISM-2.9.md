# Mekanism Version Locked 2.9

- Let Construction Sticks reach their item action on Mekanism and the related Version Locked ports, instead of opening the block GUI. Claim/item permissions remain authoritative.
- Respect includeData=false when picking structural glass, machine and storage block items. Stored energy, inventory and machine components are only copied for an explicit data pick.
- Add sulfur storage-block oxidizing at 900 mB sulfur dioxide and optional AllTheCompressed 1x oxidizing at 8100 mB, preserving nine dusts per normal block.
- Add lossless crusher decompression for all nine AllTheCompressed sulfur levels. Higher levels can be decompressed automatically instead of overflowing chemical recipe/tank integer limits.
- Preserve Core2.8 induction output fix and all prior fixes.
- Validate the unmodified Construction Sticks3.2.0 with the authorized blacklist-config adjustment, optional recipes loaded with original AllTheOres4.0.4/AllTheCompressed5.0.1, and all five final updated ports in an isolated dedicated server.

Original author credits/licenses retained. Changes prepared with Codex and verified through isolated NeoForge tests.
