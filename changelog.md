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

### SuperMartijn642's Core Library 1.1.18d
- Fixed `Widget` screens using horizontal instead of vertical scrolling

### SuperMartijn642's Core Library 1.1.18c
- Fixed custom rendering in guis not showing when Iris is installed

### SuperMartijn642's Core Library 1.1.18b
- Fixed vanilla tooltips being offset in `ContainerWidgetScreen`

### SuperMartijn642's Core Library 1.1.18a
- Added picture in picture renderer registration through `ClientRegistrationHandler`
- Added picture in picture state submission in `GuiGraphicsHelper`
- Added method to submit arbitrary rendering to `GuiGraphicsHelper`
- Fixed `GuiGraphicsHelper#submitTooltipForTopStratum` not using matrix stack transformations

### SuperMartijn642's Core Library 1.1.18
- Initial release of SuperMartijn642's Core Library for Minecraft 1.21.6
