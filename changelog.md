### SuperMartijn642's Core Library 1.1.14
- Fixed error when a `BaseBlockEntity` returns null client data

### SuperMartijn642's Core Library 1.1.13
- Improved `LootTableGenerator.LootPoolBuilder` with additional helpers
- `BaseBlockEntity#dataChanged` will now be true initially to avoid issues with Create contraptions

### SuperMartijn642's Core Library 1.1.12d
- Prevent other mod's mixin errors from showing up in Core Lib's pre-launch entrypoint

### SuperMartijn642's Core Library 1.1.12c
- Added calls to Architectury's client events in `WidgetContainerScreen`

### SuperMartijn642's Core Library 1.1.12b
- Fixed crash caused by Fabric API 0.86.0

### SuperMartijn642's Core Library 1.1.12a
- Fixed wrong buffer source being used for `ScreenUtils#drawTooltip`

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

### SuperMartijn642's Core Library 1.1.9b
- Registry overrides now replace values in vanilla fields

### SuperMartijn642's Core Library 1.1.9a
- Improved registry overrides
- Fixed lighting for `ScreenUtils#drawItem`

### SuperMartijn642's Core Library 1.1.9
- Initial release of SuperMartijn642's Core Library for Minecraft 1.20
