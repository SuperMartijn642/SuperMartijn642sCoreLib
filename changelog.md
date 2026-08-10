### SuperMartijn642's Core Library 1.1.23b
- Added workaround for guis not rendering correctly when ImmediatelyFast is present

### SuperMartijn642's Core Library 1.1.23a
- Fixed code for adding items to creative groups scaling with #total items * #total groups, thus being extremely slow when there's many mods

### SuperMartijn642's Core Library 1.1.23
- Added `#getWidget` method for `WidgetScreen` and `WidgetContainerScreen`
- Fixed multipart conditions not being flattened correctly in `BlockStateGenerator`

### SuperMartijn642's Core Library 1.1.22
- Fixed `InteractionFeedback#pass` using `SUCCESS` instead of `PASS`
- Fixed `ElementBuilder#shape` having one parameter as an int instead of float
- Fixed `BaseBlockItem#useOn` returning `CONSUME` instead of `FAIL` when a block cannot be placed

### SuperMartijn642's Core Library 1.1.21
- Fixed client not being updated when `BaseBlockEntity#writeClientData` returns empty tag
- Fixed datagen not working when mods have their own `datagen` entrypoint and use `GeneratorRegistrationHandler`

### SuperMartijn642's Core Library 1.1.20
- Added `Widget#cursor` to change the cursor when hovering a widget
- Added `ScrollbarWidget` for creating a configurable scrollbar
- Added `ScissorWidet` that restricts rendering and input handling of child widgets to its bounds
- Added `CustomSlot` for creating container slots
- Added `AbstractButtonWidget#isClickable` and `AbstractButtonWidget#setActive`
- Improved `BaseWidget` focus handling
- Fixed `GuiGraphicsHelper#submitCustomRendering` not respecting active scissor
- Fixed child widgets not getting unfocused when focused widget changes
- `RegistryEntryAcceptor` now only applies to mods that have core library as a dependency
- Fixed output from `BlockStateGenerator`, `ModelGenerator`, and `TagGenerator` not being consistent

### SuperMartijn642's Core Library 1.1.19
- Added additional properties to `BlockProperties` to match vanilla

### SuperMartijn642's Core Library 1.1.18a
- Fixed `BaseBlockItem#useOn` ignoring some parameters leading to issues when interacting with other mods
- Added ModMenu library badge integration

### SuperMartijn642's Core Library 1.1.18
- Fixed `TextFieldWidget` allowing one more character than the max length

### SuperMartijn642's Core Library 1.1.17a
- Added `CommonUtils#getRegistryAccess`
- Added `CodecHelper`

### SuperMartijn642's Core Library 1.1.17
- Added support for custom tag entry types
- Added a namespace tag entry type

### SuperMartijn642's Core Library 1.1.16
- Allow `ClientRegistrationHandler#registerAtlasSprite` to accept a different namespace
- Fixed `AtlasSourceGenerator` using the default resource generator name

### SuperMartijn642's Core Library 1.1.15
- Added `ConditionalRecipeSerializer#wrapRecipe` to serialize conditional recipes

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
