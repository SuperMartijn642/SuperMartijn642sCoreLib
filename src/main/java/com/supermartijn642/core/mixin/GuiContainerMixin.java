package com.supermartijn642.core.mixin;

import com.supermartijn642.core.gui.CustomSlot;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 08/01/2026 by SuperMartijn642
 */
@Mixin(GuiContainer.class)
public class GuiContainerMixin {

    @Inject(
        method = "isMouseOverSlot(Lnet/minecraft/inventory/Slot;II)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void isMouseOverSlot(Slot slot, int mouseX, int mouseY, CallbackInfoReturnable<Boolean> ci){
        if(slot instanceof CustomSlot){
            CustomSlot customSlot = (CustomSlot)slot;
            ci.setReturnValue(this.isPointInRegion(
                slot.xPos, slot.yPos,
                customSlot.getWidth() - 2, customSlot.getHeight() - 2,
                mouseX, mouseY
            ));
        }
    }

    @Shadow
    private boolean isPointInRegion(int x, int y, int width, int height, int mouseX, int mouseY){
        throw new AssertionError();
    }
}
