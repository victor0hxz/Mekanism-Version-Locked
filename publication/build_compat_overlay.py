#!/usr/bin/env python3
"""Build a safe candidate on top of the published Version Locked 2.9 JAR.

Only four classes are replaced, preserving all existing hotfixes/resources.
"""
import hashlib
import json
import sys
import zipfile
from pathlib import Path

BASE_SHA256 = "9aef31a2852cbedddaf52154fa9ba106931412d51423fd554566c37a445acafd"
REPLACEMENTS = [
    "mekanism/api/chemical/ChemicalBuilder.class",
    "mekanism/api/chemical/Chemical.class",
    "mekanism/client/recipe_viewer/RecipeViewerUtils.class",
    "mekanism/client/gui/element/custom/GuiQIOCrystallizerScreen.class",
]

def main():
    base, build, output = map(Path, sys.argv[1:4])
    sha = hashlib.sha256(base.read_bytes()).hexdigest()
    if sha != BASE_SHA256:
        raise RuntimeError(f"Refusing overlay: unexpected base JAR SHA256 {sha}")
    with zipfile.ZipFile(base) as old, zipfile.ZipFile(build) as new:
        old_names, new_names = set(old.namelist()), set(new.namelist())
        if not set(REPLACEMENTS) <= old_names & new_names:
            raise RuntimeError("An expected compiled class is missing")
        metadata = {
            "port_candidate": "2.10.0",
            "base_version": "2.9",
            "base_sha256": sha,
            "replaced_classes": REPLACEMENTS,
            "status": "Requires unmodified ATM11 0.12.0-beta integration test",
        }
        with zipfile.ZipFile(output, "w", allowZip64=True) as result:
            result.comment = old.comment
            for info in old.infolist():
                result.writestr(info, new.read(info.filename) if info.filename in REPLACEMENTS else old.read(info.filename))
            result.writestr(
                "META-INF/version-locked-compatibility-2.10.json",
                json.dumps(metadata, indent=2).encode("utf-8"),
                compress_type=zipfile.ZIP_DEFLATED,
            )
        with zipfile.ZipFile(output) as actual:
            if actual.testzip():
                raise RuntimeError("Generated JAR failed ZIP integrity check")
            assert old_names <= set(actual.namelist())
            assert all(actual.read(name) == old.read(name) for name in old_names - set(REPLACEMENTS))
            assert all(actual.read(name) == new.read(name) for name in REPLACEMENTS)
        output.with_suffix(output.suffix + ".sha256").write_text(
            f"{hashlib.sha256(output.read_bytes()).hexdigest()}  {output.name}\n", encoding="utf-8"
        )
    print(f"Validated compatibility overlay: {output}")

if __name__ == "__main__":
    if len(sys.argv) != 4:
        sys.exit("Usage: build_compat_overlay.py <base-v2.9.jar> <gradle-jar> <output.jar>")
    main()
