package com.supermartijn642.core.mixin;

import com.supermartijn642.core.item.BaseItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
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
@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;",
            ordinal = 0,
            shift = At.Shift.BEFORE
        ),
        method = "useItemOn",
        cancellable = true
    )
    public void useItemOn(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> ci){
        if(stack.getItem() instanceof BaseItem){
            BlockPos blockPos = hitResult.getBlockPos();
            BlockInWorld blockInWorld = new BlockInWorld(level, blockPos, false);
            if(player.getAbilities().mayBuild || stack.hasAdventureModePlaceTagForBlock(level.registryAccess().registryOrThrow(Registry.BLOCK_REGISTRY), blockInWorld)){
                BaseItem item = (BaseItem)stack.getItem();
                InteractionResult result = item.interactWithBlockFirst(stack, player, hand, level, hitResult.getBlockPos(), hitResult.getDirection(), hitResult.getLocation()).getUnderlying();
                if(result.shouldAwardStats())
                    player.awardStat(Stats.ITEM_USED.get(item));

                if(result != InteractionResult.PASS)
                    ci.setReturnValue(result);
            }
        }
    }
}
