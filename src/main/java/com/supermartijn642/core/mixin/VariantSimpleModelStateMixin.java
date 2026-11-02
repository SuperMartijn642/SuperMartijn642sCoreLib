package com.supermartijn642.core.mixin;

import com.mojang.math.OctahedralGroup;
import com.mojang.math.Quadrant;
import com.mojang.serialization.*;
import com.supermartijn642.core.util.ZBlockModelRotation;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.resources.model.ModelState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.stream.Stream;

@Mixin(Variant.SimpleModelState.class)
public class VariantSimpleModelStateMixin {

    @Final
    @Mutable
    @Shadow
    private static MapCodec<Variant.SimpleModelState> MAP_CODEC;
    @Final
    @Shadow
    private Quadrant x, y;
    @Final
    @Shadow
    private boolean uvLock;

    @Unique
    private Quadrant z;

    @Inject(
        method = "<clinit>",
        at = @At("TAIL")
    )
    private static void adjustCodec(CallbackInfo ci){
        MapCodec<Variant.SimpleModelState> original = MAP_CODEC;
        MapCodec<Optional<Quadrant>> zRotation = Quadrant.CODEC.optionalFieldOf("supermartijn642corelib:z");
        MAP_CODEC = new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops){
                return Stream.concat(original.keys(ops), zRotation.keys(ops));
            }

            @Override
            public <T> DataResult<Variant.SimpleModelState> decode(DynamicOps<T> ops, MapLike<T> input){
                return DataResult.unbox(DataResult.instance().apply2(
                    (state, rotation) -> {
                        //noinspection DataFlowIssue
                        ((VariantSimpleModelStateMixin)(Object)state).z = rotation.orElse(Quadrant.R0);
                        return state;
                    },
                    original.decode(ops, input),
                    zRotation.decode(ops, input)
                ));
            }

            @Override
            public <T> RecordBuilder<T> encode(Variant.SimpleModelState input, DynamicOps<T> ops, RecordBuilder<T> prefix){
                prefix = original.encode(input, ops, prefix);
                //noinspection DataFlowIssue
                return zRotation.encode(Optional.ofNullable(((VariantSimpleModelStateMixin)(Object)input).z), ops, prefix);
            }
        };
    }

    @Inject(
        method = "asModelState",
        at = @At("HEAD"),
        cancellable = true
    )
    private void createModelState(CallbackInfoReturnable<ModelState> ci){
        if(this.z == Quadrant.R0)
            return;
        OctahedralGroup zRotation = OctahedralGroup.IDENTITY;
        for(int i = 0; i < this.z.shift; i++)
            zRotation = zRotation.compose(OctahedralGroup.ROT_90_Z_NEG);
        OctahedralGroup rotation = zRotation.compose(OctahedralGroup.fromXYAngles(this.x, this.y));
        ci.setReturnValue(new ZBlockModelRotation(rotation, this.uvLock));
    }
}
