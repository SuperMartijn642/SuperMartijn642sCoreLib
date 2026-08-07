### SuperMartijn642's Core Library 1.1.23
- Added `#getWidget` method for `WidgetScreen` and `WidgetContainerScreen`
- Fixed multipart conditions not being flattened correctly in `BlockStateGenerator`

### SuperMartijn642's Core Library 1.1.22
- Fixed `InteractionFeedback#pass` using `SUCCESS` instead of `PASS`
- Fixed `ElementBuilder#shape` having one parameter as an int instead of float
- Fixed `BaseBlockItem#useOn` returning `CONSUME` instead of `FAIL` when a block cannot be placed

### SuperMartijn642's Core Library 1.1.21
- Fixed client not being updated when `BaseBlockEntity#writeClientData` returns empty tag

### SuperMartijn642's Core Library 1.1.20
- Added `Widget#cursor` to change the cursor when hovering a widget
- Added `ScrollbarWidget` for creating a configurable scrollbar
- Added `ScissorWidet` that restricts rendering and input handling of child widgets to its bounds
- Added `CustomSlot` for creating container slots
- Added `AbstractButtonWidget#isClickable` and `AbstractButtonWidget#setActive`
- Improved `BaseWidget` focus handling
- Fixed `GuiGraphicsHelper#submitCustomRendering` not respecting active scissor
- Fixed child widgets not getting unfocused when focused widget changes
- Fixed output from `BlockStateGenerator`, `ModelGenerator`, and `TagGenerator` not being consistent

### SuperMartijn642's Core Library 1.1.19
- Added additional properties to `BlockProperties` to match vanilla

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
- Initial release for 1.19.3
