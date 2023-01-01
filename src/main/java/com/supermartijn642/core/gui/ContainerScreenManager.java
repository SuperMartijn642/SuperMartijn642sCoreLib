package com.supermartijn642.core.gui;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created 05/08/2022 by SuperMartijn642
 */
public class ContainerScreenManager {

    private static final Map<BaseContainerType<?>,Function<Container,GuiContainer>> CONTAINER_SCREEN_MAP = new HashMap<>();

    public static <T extends Container> void registerContainerScreen(BaseContainerType<T> handler, Function<T,? extends GuiContainer> screenProvider){
        if(CONTAINER_SCREEN_MAP.containsKey(handler))
            throw new RuntimeException("Duplicate screen registration for container '" + Registries.MENU_TYPES.getIdentifier(handler) + "'!");

        //noinspection RedundantCast,unchecked
        CONTAINER_SCREEN_MAP.put(handler, (Function<Container,GuiContainer>)(Object)screenProvider);
    }

    public static <T extends Container> GuiContainer createScreen(BaseContainerType<T> handler, T container){
        if(!CONTAINER_SCREEN_MAP.containsKey(handler)){
            CoreLib.LOGGER.error("No screen registered for container handler '" + handler + "'!");
            return null;
        }

        return CONTAINER_SCREEN_MAP.get(handler).apply(container);
    }

    public static <T extends Container> GuiContainer displayContainer(BaseContainerType<T> handler, T container, int windowId){
        container.windowId = windowId;
        ClientUtils.getPlayer().openContainer = container;
        GuiContainer screen = createScreen(handler, container);
        if(screen != null)
            ClientUtils.displayScreen(screen);
        return screen;
    }
}
