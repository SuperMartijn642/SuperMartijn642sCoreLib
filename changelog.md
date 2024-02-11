### SuperMartijn642's Core Library 1.1.17
- Added support for custom tag entry types
- Added a namespace tag entry type

### SuperMartijn642's Core Library 1.1.16
- Allow `ClientRegistrationHandler#registerAtlasSprite` to accept a different namespace

### SuperMartijn642's Core Library 1.1.15
- Added `ConditionalRecipeSerializer#wrapRecipe` to serialize conditional recipes

### SuperMartijn642's Core Library 1.1.14
- Fixed error when a `BaseBlockEntity` returns null client data

### SuperMartijn642's Core Library 1.1.13
- Improved `LootTableGenerator.LootPoolBuilder` with additional helpers
- `BaseBlockEntity#dataChanged` will now be true initially to avoid issues with Create contraptions

### SuperMartijn642's Core Library 1.1.12
- Added `CommonUtils#getLogger`

### SuperMartijn642's Core Library 1.1.11
- Initialization of `TextureAtlases` will no longer load the `Sheets` class

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
- Fixed static `CreativeItemGroup#get` methods always returning the decorations tab

### SuperMartijn642's Core Library 1.1.7
- Fix `RecipeGenerator`'s smelting smelting recipes not working for multiple smelting types
- Fix `ModelGenerator#itemHandheld` using wrong parent model

### SuperMartijn642's Core Library 1.1.6
- Fix `ModelGenerator#cube` methods ignoring parameters

### SuperMartijn642's Core Library 1.1.5
- Fix performance issues with model overwrites in ClientRegistrationHandler

### SuperMartijn642's Core Library 1.1.4a
- Fix `ConditionalRecipeSerializer` ignoring recipe conditions

### SuperMartijn642's Core Library 1.1.4
- Fix crash on startup

### SuperMartijn642's Core Library 1.1.3
- Fix crash on dedicated servers when certain containers get forcibly closed
- Fix `CommonUtils#getServer` always returning `null`

### SuperMartijn642's Core Library 1.1.2
- Use the given block's namespace for blockstate files in `BlockStateGenerator`

### SuperMartijn642's Core Library 1.1.1a
- Fix translations inside of `WidgetScreen` and `WidgetContainerScreen`

### SuperMartijn642's Core Library 1.1.1
- Fix crash when a recipe condition is registered as `RecipeConditionSerializerRegistry` gets initialized

### SuperMartijn642's Core Library 1.1.0
- All gui functionality has been extracted into `Widget`s
- Added `RegistrationHandler`, `ClientRegistrationHandler`, and `GeneratorRegistrationHandler` for registering everything
- Added `CreativeItemGroup` abstraction for dealing with creative tabs
- Added abstractions for opening `BaseContainer`s with `CommonUtils#openContainer`
- Improved caching for `Object` dependent containers and guis
- Added abstractions for registries in `Registries`
- Added `ResourceGenerator` abstraction for data providers
- Added `RenderConfiguration` for setting up OpenGL properties
- Added `ResourceCondition` abstraction for use in recipes and advancements
- Added `RegistryEntryAcceptor` annotation for getting entries from registries
- Added interaction methods in `BaseBlock`, `BaseItem`, and `BaseBlockItem`
- Added methods to `EnergyFormat` to format text
- Added custom item and block entity renderers
- Added `BaseBlockEntityType` and `BaseContainerType`
- Added `TextureAtlases` to obtain locations of all default atlases
- Improved information in packet errors
- Renamed all 'TileEntity' classes to 'BlockEntity'
