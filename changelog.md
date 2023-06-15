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
