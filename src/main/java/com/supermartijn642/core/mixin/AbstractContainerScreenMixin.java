package com.supermartijn642.core.mixin;

import com.supermartijn642.core.gui.CustomSlot;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 08/01/2026 by SuperMartijn642
 */
@Mixin(ContainerScreen.class)
public class AbstractContainerScreenMixin {

    @Inject(
        method = "isHovering(Lnet/minecraft/inventory/container/Slot;DD)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void isHovering(Slot slot, double mouseX, double mouseY, CallbackInfoReturnable<Boolean> ci){
        if(slot instanceof CustomSlot){
            CustomSlot customSlot = (CustomSlot)slot;
            ci.setReturnValue(this.isHovering(
                slot.x, slot.y,
                customSlot.getWidth() - 2, customSlot.getHeight() - 2,
                mouseX, mouseY
            ));
        }
    }

    @Shadow
    private boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY){
        throw new AssertionError();
    }
}
