# Mod Integration

**Applies to:** Minecraft 26.2 with NeoForge

World Cleanup automatically handles every modded item, even without dedicated integration.

## Default behavior

Items not assigned to a category use the following rules:

- Uncategorized items: 10 minutes
- Damageable or enchanted items: 20 minutes
- Player-thrown items: 3 minutes
- Mob drops: 5 minutes
- Items near a recent player death: protected for at least 15 minutes

The dense-farm timer only applies to items assigned to the farm-drop category.

## Adding item categories

Mod authors can categorize their items using standard item tags. No World Cleanup dependency or Java code is required.

Add the appropriate file to your mod:

```text
data/world_cleanup/tags/item/valuable.json
data/world_cleanup/tags/item/common.json
data/world_cleanup/tags/item/farm_drops.json
```

Example:

```json
{
  "replace": false,
  "values": [
    "examplemod:ruby",
    "examplemod:ruby_block",
    "#examplemod:rare_materials"
  ]
}
```

Use:

- `valuable` for rare or difficult-to-replace items
- `common` for plentiful blocks and disposable materials
- `farm_drops` for items commonly produced in automated farms

Always use `"replace": false` so your entries are added without replacing existing categories.

Server owners can add exact item IDs through World Cleanup's in-game configuration instead.
