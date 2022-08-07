package com.supermartijn642.core.mixin;

import com.supermartijn642.core.extensions.EntityExtension;
import com.supermartijn642.core.item.BaseItem;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 29/07/2022 by SuperMartijn642
 */
@Mixin(EntityItem.class)
public class ItemEntityMixin implements EntityExtension {

    @SuppressWarnings("ConstantConditions")
    @Inject(
        method = "attackEntityFrom",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hurt(DamageSource source, float damage, CallbackInfoReturnable<Boolean> ci){
        ItemStack stack = ((EntityItem)(Object)this).getItem();
        if(stack.getItem() instanceof BaseItem && !((BaseItem)stack.getItem()).canBeHurtBy(source))
            ci.setReturnValue(false);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean coreLibIsFireImmune(){
        ItemStack stack = ((EntityItem)(Object)this).getItem();
        return stack.getItem() instanceof BaseItem && ((BaseItem)stack.getItem()).isFireResistant();
    }
}
