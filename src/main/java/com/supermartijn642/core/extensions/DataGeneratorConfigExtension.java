package com.supermartijn642.core.extensions;

import net.minecraft.server.packs.resources.ResourceManager;

import java.nio.file.Path;
import java.util.List;

/**
 * Created 06/04/2025 by SuperMartijn642
 */
public interface DataGeneratorConfigExtension {

    List<Path> supermartijn642corelibGetExistingPaths();

    ResourceManager supermartijn642corelibGetClientResources();

    ResourceManager supermartijn642corelibGetServerResources();
}
