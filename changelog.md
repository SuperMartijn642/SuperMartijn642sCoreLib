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

### SuperMartijn642's Core Library 1.1.18b
- Fixed `Widget` screens using horizontal instead of vertical scrolling

### SuperMartijn642's Core Library 1.1.18a
- Fixed `BaseBlockItem#useOn` ignoring some parameters leading to issues when interacting with other mods

### SuperMartijn642's Core Library 1.1.18
- Fixed `TextFieldWidget` allowing one more character than the max length

### SuperMartijn642's Core Library 1.1.17h
- Added registry wrapper for criterion trigger types

### SuperMartijn642's Core Library 1.1.17g
- Fixed tag-dependent recipe conditions not working

### SuperMartijn642's Core Library 1.1.17f
- Fixed `RecipeGenerator` not applying resource conditions

### SuperMartijn642's Core Library 1.1.17e
- Fixed registry overrides not working

### SuperMartijn642's Core Library 1.1.17d
- Fixed `LootTableGenerator` errors with custom enchant functions

### SuperMartijn642's Core Library 1.1.17c
- Fixed items not appearing in creative menu search tab

### SuperMartijn642's Core Library 1.1.17b
- Fixed `CommonUtils#getRegistryAccess` returning nothing when in a multiplayer world

### SuperMartijn642's Core Library 1.1.17a
- Fixed items not appearing in creative menu search tab

### SuperMartijn642's Core Library 1.1.17
- Initial release of SuperMartijn642's Core Library for NeoForge 1.20.6
