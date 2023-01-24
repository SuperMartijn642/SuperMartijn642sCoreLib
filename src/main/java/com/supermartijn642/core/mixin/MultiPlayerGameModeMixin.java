package com.supermartijn642.core.mixin;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.item.BaseBlockItem;
import com.supermartijn642.core.item.BaseItem;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 26/07/2022 by SuperMartijn642
 */
@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;",
            ordinal = 0,
            shift = At.Shift.BEFORE
        ),
        method = "performUseItemOn",
        cancellable = true
    )
    private void performUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> ci){
        Level level = ClientUtils.getWorld();
        ItemStack stack = player.getItemInHand(hand);
        if(stack.getItem() instanceof BaseItem){
            BlockPos blockPos = hitResult.getBlockPos();
            BlockInWorld blockInWorld = new BlockInWorld(level, blockPos, false);
            if(player.getAbilities().mayBuild || stack.hasAdventureModePlaceTagForBlock(level.registryAccess().registryOrThrow(Registries.BLOCK), blockInWorld)){
                BaseItem item = (BaseItem)stack.getItem();
                InteractionResult result = item.interactWithBlockFirst(stack, player, hand, level, hitResult.getBlockPos(), hitResult.getDirection(), hitResult.getLocation()).getUnderlying();
                if(result.shouldAwardStats())
                    player.awardStat(Stats.ITEM_USED.get(item));

                if(result != InteractionResult.PASS)
                    ci.setReturnValue(result);
            }
        }else if(stack.getItem() instanceof BaseBlockItem){
            BlockPos blockPos = hitResult.getBlockPos();
            BlockInWorld blockInWorld = new BlockInWorld(level, blockPos, false);
            if(player.getAbilities().mayBuild || stack.hasAdventureModePlaceTagForBlock(level.registryAccess().registryOrThrow(Registries.BLOCK), blockInWorld)){
                BaseBlockItem item = (BaseBlockItem)stack.getItem();
                InteractionResult result = item.interactWithBlockFirst(stack, player, hand, level, hitResult.getBlockPos(), hitResult.getDirection(), hitResult.getLocation()).getUnderlying();
                if(result.shouldAwardStats())
                    player.awardStat(Stats.ITEM_USED.get(item));

                if(result != InteractionResult.PASS)
                    ci.setReturnValue(result);
            }
        }
    }
}
