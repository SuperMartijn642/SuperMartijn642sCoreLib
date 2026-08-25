### SuperMartijn642's Core Library 1.1.24a
- Fixed `tag_populated` resource condition always failing on first resource reload

### SuperMartijn642's Core Library 1.1.24
- Fixed `CustomSlot` hover check area being 2 pixels too large

### SuperMartijn642's Core Library 1.1.23b
- Added workaround for guis not rendering correctly when ImmediatelyFast is present

### SuperMartijn642's Core Library 1.1.23a
- Fixed code for adding items to creative groups scaling with #total items * #total groups, thus being extremely slow when there's many mods

### SuperMartijn642's Core Library 1.1.23
- Added `#getWidget` method for `WidgetScreen` and `WidgetContainerScreen`
- Fixed multipart conditions not being flattened correctly in `BlockStateGenerator`

### SuperMartijn642's Core Library 1.1.22
- Fixed `ElementBuilder#shape` having one parameter as an int instead of float

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

### SuperMartijn642's Core Library 1.1.18b
- Fixed `Widget` screens using horizontal instead of vertical scrolling

### SuperMartijn642's Core Library 1.1.18a
- Fixed `BaseBlockItem#useOn` ignoring some parameters leading to issues when interacting with other mods

### SuperMartijn642's Core Library 1.1.18
- Fixed `TextFieldWidget` allowing one more character than the max length

### SuperMartijn642's Core Library 1.1.17b
- Fixed `BaseBlock` not dropping anything when using default drops

### SuperMartijn642's Core Library 1.1.17a
- Fixed crash on dedicated server

### SuperMartijn642's Core Library 1.1.17
- Initial release of SuperMartijn642's Core Library for Minecraft 1.21.2 & 1.21.3
