# Building and validating the 2.10 compatibility changes

Use Java 25 and the existing Gradle wrapper. Supply the original KubeJS 26.1.2-8.0.6 JAR as a compile-only dependency, either under `libs/` with its original filename or through `-Pkubejs_compat_jar=...`. Do not redistribute that JAR inside Mekanism.

From the repository root:

```powershell
.\gradlew.bat '-Pkubejs_compat_jar=libs/kubejs-neoforge-26.1.2-8.0.6.jar' compileApiJava compileJava
.\gradlew.bat '-Pkubejs_compat_jar=libs/kubejs-neoforge-26.1.2-8.0.6.jar' test
```

The native source project declares NeoForge 26.1.2.74. Distribution compatibility was additionally exercised on an isolated NeoForge 26.1.2.109 client/server. Source compilation, unit tests and runtime compatibility are different checks; consult the release notes for actual results.

The published distribution preserves the complete 2.9 binary and replaces only `ChemicalBuilder`, `Chemical`, `ChemicalInstance` and `ChemicalResource`, plus manifest/mod metadata. It adds `ChemicalAliasCompatibility` (including its feature-probe nested class), the optional mixin plugin, the two classes under the `kubejs.mixin` subpackage and `mekanism-scriptcompat.mixins.json`. Helpers must remain outside the reserved mixin package.

The legacy `Action` source restores the public API of the unchanged enum already in the 2.9 binary. API version `10.8.0` remains unchanged for addon dependency compatibility; the distribution/release version is `2.10`.

Test dependencies remain original: KubeJS 8.0.6, Rhino 2101.2.8-build.91, JEI 29.47.0.109, ExtraDisks 26.1.1 and Better Advanced Tooltips 2601.1.0-build.11. The latter supplies `BATIcons`, used by KubeJS during registration. A test fixture must include it.

Never copy original ATM11 scripts into the public repository. Tests may use private fixture copies, with hashes compared against the originals. Exercise the actual script source name, both absent/present optional dependencies, invalid ingredient IDs and explicit chemical data-map precedence. The stock recipe-viewer script depends on additional pack mods; focused tests must use the real KubeJS/JEI event rather than fake ingredient conversion.
