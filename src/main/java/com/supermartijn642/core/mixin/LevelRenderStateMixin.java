package com.supermartijn642.core.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.extensions.LevelRenderStateExtension;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Created 04/07/2026 by SuperMartijn642
 */
@Mixin(LevelRenderState.class)
public class LevelRenderStateMixin implements LevelRenderStateExtension {

    @Unique
    private List<BiConsumer<PoseStack,SubmitNodeCollector>> submitters = List.of();

    @Override
    public List<BiConsumer<PoseStack,SubmitNodeCollector>> supermartijn642corelibGetSubmitters(){
        return this.submitters;
    }

    @Override
    public void supermartijn642corelibSetSubmitters(List<BiConsumer<PoseStack,SubmitNodeCollector>> submitters){
        this.submitters = submitters;
    }
}
