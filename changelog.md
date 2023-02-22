### SuperMartijn642's Core Library 1.1.2
- Fix crash on dedicated servers when certain containers get forcibly closed
- Fix `CommonUtils#getServer` always returning `null`

### SuperMartijn642's Core Library 1.1.2
- Use the given block's namespace for blockstate files in `BlockStateGenerator`
- Fix crash when loading ore dictionary recipe ingredients

### SuperMartijn642's Core Library 1.1.1a
- Fix rare crash well loading recipe ingredients
- Fix crash when both Hammer Lib and ColorUtility are installed

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
- Added `RenderConfiguration` for setting up OpenGL properties
- Added `ResourceCondition` abstraction for use in recipes and advancements
- Added `RegistryEntryAcceptor` annotation for getting entries from registries
- Added interaction methods in `BaseBlock`, `BaseItem`, and `BaseBlockItem`
- Added methods to `EnergyFormat` to format text
- Added custom item and block entity renderers
- Added `BaseContainerType` to synchronize container data
- Added `TextureAtlases` to obtain locations of all default atlases
- Improved information in packet errors
- Renamed all 'TileEntity' classes to 'BlockEntity'
- Backported mining tags for use with `BaseBlock`
- Added `ResourceGenerator`s to generate all assets
- Added `BaseBlockEntityType`s to allow for multiple block entities from a single class
- Added json language files
