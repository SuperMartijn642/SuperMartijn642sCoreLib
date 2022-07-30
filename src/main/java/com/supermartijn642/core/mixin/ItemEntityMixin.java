package com.supermartijn642.core.mixin;

import com.supermartijn642.core.item.BaseItem;
import com.supermartijn642.core.mixin.extensions.EntityExtension;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 29/07/2022 by SuperMartijn642
 */
@Mixin(ItemEntity.class)
public class ItemEntityMixin implements EntityExtension {

    @SuppressWarnings("ConstantConditions")
    @Inject(
        method = "hurt",
        at = @At("HEAD"),
        cancellable = true
    )
    public void hurt(DamageSource source, float damage, CallbackInfoReturnable<Boolean> ci){
        ItemStack stack = ((ItemEntity)(Object)this).getItem();
        if(stack.getItem() instanceof BaseItem && !((BaseItem)stack.getItem()).canBeHurtBy(source))
            ci.setReturnValue(false);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean coreLibIsFireImmune(){
        ItemStack stack = ((ItemEntity)(Object)this).getItem();
        return stack.getItem() instanceof BaseItem && ((BaseItem)stack.getItem()).isFireResistant();
    }
}
