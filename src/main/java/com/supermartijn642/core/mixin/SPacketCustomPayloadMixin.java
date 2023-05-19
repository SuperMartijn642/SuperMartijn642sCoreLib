package com.supermartijn642.core.mixin;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Created 21/05/2023 by SuperMartijn642
 */
@Mixin(value = SPacketCustomPayload.class, priority = 900)
public class SPacketCustomPayloadMixin {

    @Redirect(
        method = "readPacketData",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/PacketBuffer;readString(I)Ljava/lang/String;"
        )
    )
    private String readPacketData(PacketBuffer buffer, int maxLength){
        // Increase the length limit for channel names to 30
        return buffer.readString(Math.max(maxLength, 30));
    }
}
