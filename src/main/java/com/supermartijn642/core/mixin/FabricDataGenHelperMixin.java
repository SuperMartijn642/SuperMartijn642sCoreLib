package com.supermartijn642.core.mixin;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.extensions.CoreLibDataGenerator;
import com.supermartijn642.core.registry.GeneratorRegistrationHandler;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 25/08/2022 by SuperMartijn642
 */
@Mixin(FabricDataGenHelper.class)
public class FabricDataGenHelperMixin {

    @ModifyVariable(
        method = "runInternal",
        at = @At("STORE"),
        ordinal = 0,
        remap = false
    )
    private static List<EntrypointContainer<DataGeneratorEntrypoint>> runInternalModifyEntryPoints(List<EntrypointContainer<DataGeneratorEntrypoint>> dataGeneratorInitializers){
        List<EntrypointContainer<DataGeneratorEntrypoint>> newEntryPoints = new ArrayList<>(dataGeneratorInitializers);
        GeneratorRegistrationHandler.getAllHandlers().entrySet()
            .stream()
            .filter(entry -> FabricLoader.getInstance().isModLoaded(entry.getKey()))
            .map(entry -> {
                ModContainer container = FabricLoader.getInstance().getModContainer(entry.getKey()).get();
                GeneratorRegistrationHandler registrationHandler = entry.getValue();
                return new EntrypointContainer<DataGeneratorEntrypoint>() {
                    @Override
                    public DataGeneratorEntrypoint getEntrypoint(){
                        //noinspection DataFlowIssue
                        return dataGenerator -> ((CoreLibDataGenerator)(Object)dataGenerator).setGeneratorRegistrationHandler(registrationHandler);
                    }

                    @Override
                    public ModContainer getProvider(){
                        return container;
                    }
                };
            })
            .forEach(newEntryPoints::add);

        // Also just make sure resource packs are available
        ClientUtils.getMinecraft().resourcePackRepository.reload();

        return newEntryPoints;
    }
}
