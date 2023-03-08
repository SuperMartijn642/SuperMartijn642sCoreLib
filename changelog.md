### SuperMartijn642's Core Library 1.1.4a
- Fix `ConditionalRecipeSerializer` ignoring recipe conditions

### SuperMartijn642's Core Library 1.1.4
- Fix crash on startup

### SuperMartijn642's Core Library 1.1.3
- Fix crash on dedicated servers when certain containers get forcibly closed
- Fix `CommonUtils#getServer` always returning `null`

### SuperMartijn642's Core Library 1.1.2
- Use the given block's namespace for blockstate files in `BlockStateGenerator`

### SuperMartijn642's Core Library 1.1.1
- Fix crash when a recipe condition is registered as `RecipeConditionSerializerRegistry` gets initialized

### SuperMartijn642's Core Library 1.1.0a
- Fix crash on dedicated servers in certain scenarios

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
- Backported mining tags for use with `BaseBlock`
