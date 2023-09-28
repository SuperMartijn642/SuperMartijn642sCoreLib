### SuperMartijn642's Core Library 1.1.13
- Improved `LootTableGenerator.LootPoolBuilder` with additional helpers
- `BaseBlockEntity#dataChanged` will now be true initially to avoid issues with Create contraptions

### SuperMartijn642's Core Library 1.1.12b
- Prevent other mod's mixin errors from showing up in Core Lib's pre-launch entrypoint

### SuperMartijn642's Core Library 1.1.12a
- Added calls to Architectury's client events in `WidgetContainerScreen`

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
- Fixed static `CreativeItemGroup#get` methods always returning the decorations tab

### SuperMartijn642's Core Library 1.1.7
- Initial release of SuperMartijn642's Core Library for Fabric
