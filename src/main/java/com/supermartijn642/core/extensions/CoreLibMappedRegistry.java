package com.supermartijn642.core.extensions;

import java.util.function.BiConsumer;

/**
 * Created 22/03/2023 by SuperMartijn642
 */
public interface CoreLibMappedRegistry {

    void supermartijn642corelibSetRegisterOverrides(boolean flag, BiConsumer<Object,Object> overrideConsumer);
}
