package com.supermartijn642.core.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Created 04/07/2026 by SuperMartijn642
 */
public interface LevelRenderStateExtension {

    List<BiConsumer<PoseStack,SubmitNodeCollector>> supermartijn642corelibGetSubmitters();

    void supermartijn642corelibSetSubmitters(List<BiConsumer<PoseStack,SubmitNodeCollector>> submitters);
}
