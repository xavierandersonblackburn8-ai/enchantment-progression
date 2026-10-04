# Enchantment Progression — Minecraft 26.3 Fabric

Target: Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.160.7+26.3, Java 25.

## Intended behavior
- Inventory button opens Enchantment Progression.
- Screen reads the held item and lists compatible enchantments.
- Existing enchantments can progress beyond vanilla max levels.
- Missing compatible enchantments can begin at level I.
- Vanilla incompatibility rules remain enforced.
- Upgrade request is validated server-side.
- XP levels are deducted server-side only after validation.
- The real enchantment component on the held ItemStack is changed and synchronized.
- Gameplay calculations must receive the upgraded real level; tooltip-only upgrades are unacceptable.

## Current source checkpoint
Project/build metadata and independently testable XP-cost progression are implemented. Minecraft networking, inventory-screen injection, enchantment registry traversal, compatibility validation, ItemStack mutation, and effect verification are deliberately not marked complete until compiled against 26.3 APIs.

Do not distribute this checkpoint as a finished mod.

## Checkpoint 4 changes
- Added a server->client upgrade result packet and client pending-state to prevent accidental click-spam.
- The server is authoritative for held item, enchantment compatibility, current/target level, XP cost, and XP deduction.
- Successful upgrades write the real ItemEnchantments component via EnchantmentHelper.updateEnchantments and broadcast inventory changes.
- The GUI refreshes only after the server replies, and displays success/failure feedback.

## Checkpoint 7 hardening
- Client mixin config moved into the split client resource set used by Fabric 26.3 projects.
- Added fail-fast build verifier requiring Java 25.
- Added progression boundary self-tests (0->1, IV->V cost, V->VI cost, 254->255, hard cap).
- Server remains authoritative: request contains only enchantment identifier; target level and XP cost are recomputed server-side.
- The upgrade path writes the actual ItemStack enchantment component, not a cosmetic label.

### Still required before release
A Java 25 + Gradle/Fabric-connected build must compile this project, followed by in-game acceptance testing. Do not distribute a jar until those checks pass.


## Checkpoint 8 source audit
- Corrected 26.3 item rendering to public `GuiGraphicsExtractor.fakeItem`.
- Added same-tick server request throttling to prevent duplicate purchases.
- Revalidated 26.3 screen rendering/input and identifier buffer methods against current 26.3 API references.
- Still not a release jar until Java 25 Gradle compilation and in-game tests pass.

## Checkpoint 11 API corrections
- Removed the placeholder/missing ScreenAccessor mixin entirely.
- Inventory button insertion now follows Fabric Screen API 0.160.7+26.3's documented `Screens.getWidgets(screen).add(Button)` path.
- Removed redundant thread re-scheduling from Fabric networking handlers because 0.160.7 documents server handlers on the server thread and client handlers on the render thread.
- Verified project toolchain metadata against Fabric's official 26.3 example branch.


## Client preflight
The upgrade screen now prevents requests that are already known to be invalid (pending request, incompatible enchantment, configured cap, or insufficient survival XP). These checks are convenience only; the integrated/dedicated server independently validates every request before changing the held item or XP.
