package com.supermartijn642.core.mixin.spongeforge;

import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.registry.RegistrationHandler;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.registry.RegistryEntryAcceptor;
import net.minecraft.advancements.Advancement;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.Map;

/**
 * Created 29/07/2022 by SuperMartijn642
 */
@Mixin(ForgeHooks.class)
public class ForgeHooksMixinSpongeForge {

    @Inject(
        method = "canHarvestBlock",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private static void canHarvestBlock(Block block, EntityPlayer player, IBlockAccess world, BlockPos pos, CallbackInfoReturnable<Boolean> ci){
        if(!(block instanceof BaseBlock))
            return;

        // Got to copy all this, because SpongeForge decides to overwrite the entire method for no reason
        // ----- SpongeForge start -----

        // ----- core lib injected part start -----
        if(!((BaseBlock)block).requiresCorrectToolForDrops())
            ci.setReturnValue(ForgeEventFactory.doPlayerHarvestCheck(player, world.getBlockState(pos), true));
        // ----- core lib injected part end -----

        IBlockState state = world.getBlockState(pos);
        state = state.getBlock().getActualState(state, world, pos);
        if(state.getMaterial().isToolNotRequired()){
            ci.setReturnValue(true);
            return;
        }

        final ItemStack stack = player.getHeldItemMainhand();
        final String tool = block.getHarvestTool(state);
        if(stack.isEmpty() || tool == null){
            ci.setReturnValue(player.canHarvestBlock(state));
            return;
        }

        final PhaseContext<?> context = PhaseTracker.getInstance().getCurrentContext();
        final IPhaseState<?> phaseState = context.state;
        // Many mods use this hook with fake players so we need to avoid passing them when possible
        // and instead pass the true source which is usually a TileEntity
        final Object source = context.getSource() == null ? player : context.getSource();
        if(ShouldFire.CHANGE_BLOCK_EVENT_PRE && !phaseState.isInteraction()){
            // Sponge Start - Add the changeblockevent.pre check here before we bother with item stacks.
            if(world instanceof WorldBridge && !((WorldBridge)world).bridge$isFake() && SpongeImplHooks.isMainThread()){
                try(final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()){
                    // Might as well provide the active item in use.
                    frame.addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(player.getActiveItemStack()));
                    if(SpongeCommonEventFactory.callChangeBlockEventPre((WorldServerBridge)world, pos, source).isCancelled()){
                        // Since a plugin cancelled it, go ahead and cancel it.
                        ci.setReturnValue(false);
                        return;
                    }
                }
            }
            // Sponge End
        }

        // ----- core lib injected part start -----
        int toolLevel = -1;
        Item item = stack.getItem();
        for(String toolType : item.getToolClasses(stack)){
            if(state.getBlock().isToolEffective(toolType, state)){
                int harvestLevel = item.getHarvestLevel(stack, toolType, player, state);
                if(harvestLevel > toolLevel)
                    toolLevel = harvestLevel;
            }
        }
        if(toolLevel == -1)
            toolLevel = item.getHarvestLevel(stack, tool, player, state);
        // ----- core lib injected part end -----
        if(toolLevel < 0){
            ci.setReturnValue(player.canHarvestBlock(state));
            return;
        }

        ci.setReturnValue(toolLevel >= block.getHarvestLevel(state));
        // ----- SpongeForge end -----
    }

    @Inject(
        method = "canToolHarvestBlock",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private static void canToolHarvestBlock(IBlockAccess world, BlockPos pos, ItemStack stack, CallbackInfoReturnable<Boolean> ci){
        if(stack.isEmpty())
            return;
        IBlockState state = world.getBlockState(pos);
        if(state.getBlock() instanceof BaseBlock){
            int harvestLevel = state.getBlock().getHarvestLevel(state);
            for(String toolType : stack.getItem().getToolClasses(stack)){
                if(state.getBlock().isToolEffective(toolType, state) && stack.getItem().getHarvestLevel(stack, toolType, null, state) >= harvestLevel){
                    ci.setReturnValue(true);
                    return;
                }
            }
            ci.setReturnValue(false);
        }
    }

    @Inject(
        method = "loadAdvancements",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/common/crafting/CraftingHelper;init()V",
            shift = At.Shift.AFTER
        ),
        remap = false
    )
    private static void loadAdvancements(Map<ResourceLocation,Advancement.Builder> map, CallbackInfoReturnable<Boolean> ci){
        RegistrationHandler.handleResourceConditionSerializerRegistryEvent();
        RegistryEntryAcceptor.Handler.onRegisterEvent(Registries.RECIPE_CONDITION_SERIALIZERS);
    }
}
