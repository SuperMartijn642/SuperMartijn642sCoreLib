package com.supermartijn642.core.extensions;

import com.supermartijn642.core.registry.GeneratorRegistrationHandler;

/**
 * Created 06/05/2023 by SuperMartijn642
 */
public interface CoreLibDataGenerator {

    void setGeneratorRegistrationHandler(GeneratorRegistrationHandler handler);

    GeneratorRegistrationHandler getGeneratorRegistrationHandler();
}
