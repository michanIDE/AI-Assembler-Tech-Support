AI Assembler Tech Support
=======
Addon for [AI Assembler](https://github.com/michanIDE/AI-Assembler) that teaches it to read recipes from tech mods. Without it, machine recipes (Mekanism gas reactions, Thermal machines, Tinkers smeltery, ...) either don't expose their real inputs/outputs through the vanilla `Recipe` interface or don't exist as recipes at all — so AI Assembler's item vectorization and recommendations can't see them. This addon registers special recipe processors, special (non-item) ingredients, and synthetic recipes for hardcoded machine conversions.

Requires **AI Assembler 1.3.0+** on Minecraft **1.20.1 Forge**. Every supported tech mod is optional: an integration only activates when its mod is present, and its classes are never even classloaded otherwise.

Supported mods
--------

| Mod | Tested version | What the addon handles |
|-----|----------------|------------------------|
| **Mekanism** | 10.4.16.80 | All machine recipe types (24 types, gas/infusion/pigment/slurry amounts extracted correctly); all 4 chemical registries excluded from item vectorization; synthetic recipes for the fission reactor (fissile fuel → nuclear waste) and SPS (polonium → antimatter), which are multiblock logic with no recipe entries |
| **Thermal Series** | Foundation 11.0.6 + Expansion | All machine recipe types (15 types); output chances treated as expected yields |
| **Tinkers Construct** | 3.11.2.166 | Smeltery: melting (with byproducts), alloying, casting table/basin (casts counted only when consumed), molding |
| **Industrial Foregoing** | 3.5.22 | Dissolution chamber, fluid extractor (expected-value model), crusher, stonework (consumed fluids only); laser drill excluded (lens is never consumed) |
| **Applied Energistics 2** | 15.4.10 | Inscriber (press plates excluded in inscribe mode), charger, in-world transforms |
| **Draconic Evolution** | 3.1.2.621 | Fusion crafting (catalyst + injector ingredients) |
| **Integrated Dynamics** | 1.30.7 | Squeezer and drying basin, plain and mechanical (chanced outputs, fluid inputs/outputs); dynamic facade recipes excluded |
| **Re:Avaritia** | 1.4.0 | Neutron compressor input counts (up to 10000x); the extreme crafting table already works via the default path |
| **Flux Networks** | 7.2.1 | Synthetic recipe for flux dust (redstone crushed between bedrock and obsidian — hardcoded in-world logic, the root of every Flux recipe); its crafting recipes work via the default path |

Newer or older versions of these mods will usually work as long as their recipe APIs are unchanged — the versions above are the ones the extraction was verified against.

Required local setup (building from source)
--------
This project depends on the AI Assembler base mod jar, which is **not** committed to this repo. Before building:

1. Build the base mod (in the sibling `../base` project): `./gradlew build`
2. Copy the resulting jar into `libs/` here, e.g. `libs/ai_assembler-1.3.0.jar`
3. Extract the jarjar-nested `thermal_core` jar into `libs/` as well (needed at compile time;
   Gradle doesn't expand jarjar nesting): download thermal_foundation from CurseForge, then
   `unzip -j thermal_foundation-*.jar "META-INF/jarjar/thermal_core-*.jar" -d libs/`
4. If versions differ from the coordinates in `gradle.properties` (`thermal_core_version`, etc.), update those to match.

Then `./gradlew build` produces `build/libs/ai_assembler_tech-<version>.jar`. Only this jar and the base mod need to be installed; the tech mods and their libraries are never shipped.

Development
--------
- `./gradlew runServer` launches a dev server with **all** supported mods on the classpath.
- `./gradlew runServer -PminimalRun` launches with only the base mod — the classloading-safety check (every integration must log a skip, nothing may crash).
- With `debugMode = true` in `run/config/ai_assembler-common.toml`, the base mod writes `recipes_dump.json` / `vectorized_items.json` into `run/` at server start — the verification targets for every integration.
- See `dev/specification.md` and `dev/development_plan.md` for scope, per-mod findings, and progress.

This template uses [ModDevGradle Legacy](https://github.com/neoforged/ModDevGradle/blob/main/LEGACY.md).
