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

### SuperMartijn642's Core Library 1.1.18b
- Fixed `Widget` screens using horizontal instead of vertical scrolling

### SuperMartijn642's Core Library 1.1.18a
- Fixed `BaseBlockItem#useOn` ignoring some parameters leading to issues when interacting with other mods
- Added ModMenu library badge integration

### SuperMartijn642's Core Library 1.1.18
- Fixed `TextFieldWidget` allowing one more character than the max length

### SuperMartijn642's Core Library 1.1.17c
- Added registry wrapper for criterion trigger types

### SuperMartijn642's Core Library 1.1.17b
- Fixed `LootTableGenerator` errors with custom enchant functions

### SuperMartijn642's Core Library 1.1.17a
- Fixed `CommonUtils#getRegistryAccess` returning nothing when in a multiplayer world

### SuperMartijn642's Core Library 1.1.17
- Initial release of SuperMartijn642's Core Library for Fabric 1.21
