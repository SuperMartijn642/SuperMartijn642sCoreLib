package com.supermartijn642.core.mixin;

import com.google.common.collect.Lists;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.util.List;
import java.util.Set;

/**
 * Created 20/08/2023 by SuperMartijn642
 */
public class CoreLibMixinPlugin implements IMixinConfigPlugin {

    private boolean isSpongeForgeLoaded;

    @Override
    public void onLoad(String mixinPackage){
        try{
            MixinService.getService().getBytecodeProvider().getClassNode("org.spongepowered.mod.SpongeMod");
            this.isSpongeForgeLoaded = true;
        }catch(Exception ignored){
            this.isSpongeForgeLoaded = false;
        }
    }

    @Override
    public String getRefMapperConfig(){
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName){
        return !(this.isSpongeForgeLoaded && mixinClassName.endsWith(".ForgeHooksMixin"));
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets){
    }

    @Override
    public List<String> getMixins(){
        return this.isSpongeForgeLoaded ?
            Lists.newArrayList("spongeforge.ForgeHooksMixinSpongeForge")
            : null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo){
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo){
    }
}
