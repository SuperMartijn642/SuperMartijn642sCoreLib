package com.supermartijn642.core.extensions;

import net.minecraftforge.data.event.GatherDataEvent;

/**
 * Created 16/10/2023 by SuperMartijn642
 */
public interface DataGeneratorExtension {

    void setDataGeneratorConfig(GatherDataEvent.DataGeneratorConfig config);
}
