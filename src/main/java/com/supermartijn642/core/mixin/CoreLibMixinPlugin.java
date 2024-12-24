package com.supermartijn642.core.mixin;

import net.minecraftforge.fml.loading.FMLEnvironment;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Created 26/04/2023 by SuperMartijn642
 */
public class CoreLibMixinPlugin implements IMixinConfigPlugin {

    private static final boolean isDevEnvironment = !FMLEnvironment.production;
    private static final boolean isClient = FMLEnvironment.dist.isClient();

    private String mixinDevPackage;

    @Override
    public void onLoad(String mixinPackage){
        this.mixinDevPackage = mixinPackage + ".dev";
    }

    @Override
    public String getRefMapperConfig(){
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName){
        return (isDevEnvironment && isClient) || !mixinClassName.startsWith(this.mixinDevPackage);
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets){
    }

    @Override
    public List<String> getMixins(){
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo){
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo){
    }
}
