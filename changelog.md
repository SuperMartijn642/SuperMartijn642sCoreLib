### SuperMartijn642's Core Library 1.1.10
- `RegistrationHandler` will now register entries in the same order as they are submitted in

### SuperMartijn642's Core Library 1.1.9a
- Fixed crash with Emendatus Enigmatica

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
- Fix `RecipeGenerator`'s smelting smelting recipes not working for multiple smelting types
- Fix `ModelGenerator#itemHandheld` using wrong parent model

### SuperMartijn642's Core Library 1.1.6
- Fix `ModelGenerator#cube` methods ignoring parameters

### SuperMartijn642's Core Library 1.1.5
- Initial release for 1.19.4
