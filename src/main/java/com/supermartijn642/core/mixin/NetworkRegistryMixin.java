package com.supermartijn642.core.mixin;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Created 12/02/2024 by SuperMartijn642
 */
@Mixin(value = NetworkRegistry.class, remap = false)
public class NetworkRegistryMixin {

    @Unique
    private static final Side[] SIDES = {Side.CLIENT, Side.SERVER};

    @Redirect(
        method = "newChannel(Ljava/lang/String;[Lio/netty/channel/ChannelHandler;)Ljava/util/EnumMap;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/fml/relauncher/Side;values()[Lnet/minecraftforge/fml/relauncher/Side;"
        ),
        require = 0
    )
    private Side[] replaceSides(){
        return SIDES;
    }

    @Redirect(
        method = "newChannel(Lnet/minecraftforge/fml/common/ModContainer;Ljava/lang/String;[Lio/netty/channel/ChannelHandler;)Ljava/util/EnumMap;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/fml/relauncher/Side;values()[Lnet/minecraftforge/fml/relauncher/Side;"
        ),
        require = 0
    )
    private Side[] replaceSides2(){
        return SIDES;
    }
}
