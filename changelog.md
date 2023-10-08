### SuperMartijn642's Core Library 1.1.15
- Added `ConditionalRecipeSerializer#wrapRecipe` to serialize conditional recipes

### SuperMartijn642's Core Library 1.1.14
- Fixed error when a `BaseBlockEntity` returns null client data

### SuperMartijn642's Core Library 1.1.13
- Improved `LootTableGenerator.LootPoolBuilder` with additional helpers
- `BaseBlockEntity#dataChanged` will now be true initially to avoid issues with Create contraptions

### SuperMartijn642's Core Library 1.1.12c
- Prevent other mod's mixin errors from showing up in Core Lib's pre-launch entrypoint

### SuperMartijn642's Core Library 1.1.12b
- Added calls to Architectury's client events in `WidgetContainerScreen`

### SuperMartijn642's Core Library 1.1.12a
- Fixed crash caused by Fabric API 0.86.0

### SuperMartijn642's Core Library 1.1.12
- Added `CommonUtils#getLogger`

### SuperMartijn642's Core Library 1.1.11
- Initialization of `TextureAtlases` will no longer load the `Sheets` class

### SuperMartijn642's Core Library 1.1.10c
- Added compatibility for Quilt loader

### SuperMartijn642's Core Library 1.1.10b
- Fixed crash when Plant In A Jar is installed

### SuperMartijn642's Core Library 1.1.10a
- Item registry overrides will now also override the block->item map

### SuperMartijn642's Core Library 1.1.10
- `RegistrationHandler` will now register entries in the same order as they are submitted in

### SuperMartijn642's Core Library 1.1.9c
- Registry overrides now replace values in vanilla fields

### SuperMartijn642's Core Library 1.1.9b
- Improved registry overrides

### SuperMartijn642's Core Library 1.1.9a
- Prevent crashes when scanning for `RegistryEntryAcceptor` entries

### SuperMartijn642's Core Library 1.1.9
- Fixed crash when `null` is passed into `BlockProperties#lootTableFrom`

### SuperMartijn642's Core Library 1.1.8
- Added `ResourceAggregator` to allow multiple data generators to write to the same file
- All data generators will now generate before anything gets saved
- Entries in json files from data generators will now always generate in the same order
- Fixed `ClientUtils#getPartialTicks` returning the wrong value when the game is paused
- Fixed `ItemProperties#toUnderlying` causing an exception when durability is set
- Fixed loot table handling in `BlockProperties`

### SuperMartijn642's Core Library 1.1.7
- Initial release of SuperMartijn642's Core Library for Fabric
