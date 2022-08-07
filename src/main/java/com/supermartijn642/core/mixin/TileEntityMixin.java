package com.supermartijn642.core.mixin;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.core.block.BaseBlockEntityType;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.registry.RegistryUtil;
import com.supermartijn642.core.util.Pair;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 06/08/2022 by SuperMartijn642
 */
@Mixin(TileEntity.class)
public class TileEntityMixin {

    @Inject(
        method = "register",
        at = @At("HEAD")
    )
    private static void register(String id, Class<? extends TileEntity> clazz, CallbackInfo ci){
        if(Registries.BLOCK_ENTITY_TYPES.hasIdentifier(new ResourceLocation(id)))
            CoreLib.LOGGER.warn("Overlapping block entity class and block entity type registration for identifier '" + new ResourceLocation(id) + "'!");
    }

    @Inject(
        method = "getKey",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void getKey(Class<? extends TileEntity> clazz, CallbackInfoReturnable<ResourceLocation> ci){
        // This is very janky, but should work for most cases
        if(BaseBlockEntity.class.isAssignableFrom(clazz)){
            for(Pair<ResourceLocation,BaseBlockEntityType<?>> entry : Registries.BLOCK_ENTITY_TYPES.getEntries()){
                if(entry.right().containsClass(clazz)){
                    ci.setReturnValue(entry.left());
                    return;
                }
            }
            ci.setReturnValue(null);
        }
    }

    private static NBTTagCompound compound;

    @Inject(
        method = "create",
        at = @At("HEAD")
    )
    private static void create(World worldIn, NBTTagCompound compound, CallbackInfoReturnable<TileEntity> ci){
        TileEntityMixin.compound = compound;
    }

    @ModifyVariable(
        method = "create",
        at = @At("STORE"),
        ordinal = 0
    )
    private static TileEntity createModifyEntity(TileEntity entity){
        if(compound.hasKey("id", Constants.NBT.TAG_STRING)){
            String id = compound.getString("id");
            if(RegistryUtil.isValidIdentifier(id)){
                ResourceLocation identifier = new ResourceLocation(id);
                if(Registries.BLOCK_ENTITY_TYPES.hasIdentifier(identifier))
                    return Registries.BLOCK_ENTITY_TYPES.getValue(identifier).createBlockEntity();
            }
        }
        return entity;
    }

    @Redirect(
        method = "writeInternal",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/registry/RegistryNamespaced;getNameForObject(Ljava/lang/Object;)Ljava/lang/Object;"
        )
    )
    private Object writeInternalRedirect(RegistryNamespaced<Object,Object> registry, Object clazz){
        //noinspection ConstantConditions
        if((Object)this instanceof BaseBlockEntity)
            return Registries.BLOCK_ENTITY_TYPES.getIdentifier(((BaseBlockEntity)(Object)this).getType());
        return registry.getNameForObject(clazz);
    }
}
