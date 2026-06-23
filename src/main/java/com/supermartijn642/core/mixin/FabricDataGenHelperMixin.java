package com.supermartijn642.core.mixin;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.extensions.CoreLibDataGenerator;
import com.supermartijn642.core.registry.GeneratorRegistrationHandler;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import org.jetbrains.annotations.Nullable;
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
            .forEach(entry -> {
                ModContainer container = FabricLoader.getInstance().getModContainer(entry.getKey()).get();

                // Create function to add providers
                GeneratorRegistrationHandler registrationHandler = entry.getValue();
                //noinspection DataFlowIssue
                DataGeneratorEntrypoint providers = dataGenerator -> ((CoreLibDataGenerator)(Object)dataGenerator).setGeneratorRegistrationHandler(registrationHandler);

                // Find an existing entrypoint for the mod
                for(int i = 0; i < newEntryPoints.size(); i++){
                    EntrypointContainer<DataGeneratorEntrypoint> entrypoint = newEntryPoints.get(i);
                    if(entrypoint.getProvider() == container){
                        newEntryPoints.set(i, new EntrypointContainer<>() {
                            @Override
                            public DataGeneratorEntrypoint getEntrypoint(){
                                DataGeneratorEntrypoint original = entrypoint.getEntrypoint();
                                return new DataGeneratorEntrypoint() {
                                    @Override
                                    public void onInitializeDataGenerator(FabricDataGenerator dataGenerator){
                                        original.onInitializeDataGenerator(dataGenerator);
                                        providers.onInitializeDataGenerator(dataGenerator);
                                    }

                                    @Override
                                    public @Nullable String getEffectiveModId(){
                                        return original.getEffectiveModId();
                                    }
                                };
                            }

                            @Override
                            public ModContainer getProvider(){
                                return container;
                            }
                        });
                        return;
                    }
                }

                // If there's no existing entrypoint add a new one
                newEntryPoints.add(new EntrypointContainer<>() {
                    @Override
                    public DataGeneratorEntrypoint getEntrypoint(){
                        return providers;
                    }

                    @Override
                    public ModContainer getProvider(){
                        return container;
                    }
                });
            });

        // Also just make sure resource packs are available
        ClientUtils.getMinecraft().resourcePackRepository.reload();

        return newEntryPoints;
    }
}
