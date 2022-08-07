package com.supermartijn642.core.network;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.BaseContainerType;
import com.supermartijn642.core.gui.ContainerScreenManager;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

/**
 * Created 05/08/2022 by SuperMartijn642
 */
public final class OpenContainerPacket<T extends Container> implements BasePacket {

    private BaseContainerType<T> handler;
    private T container;

    public OpenContainerPacket(BaseContainerType<T> handler, T container){
        this.handler = handler;
        this.container = container;
    }

    public OpenContainerPacket(){
    }

    @Override
    public void write(PacketBuffer buffer){
        buffer.writeString(Registries.MENU_TYPES.getIdentifier(this.handler).toString());
        this.handler.writeContainer(this.container, buffer);
    }

    @Override
    public void read(PacketBuffer buffer){
        String identifier = buffer.readString(32768);
        if(!RegistryUtil.isValidIdentifier(identifier))
            throw new RuntimeException("Received invalid identifier!");
        //noinspection unchecked
        this.handler = (BaseContainerType<T>)Registries.MENU_TYPES.getValue(new ResourceLocation(identifier));
        if(this.handler == null)
            throw new RuntimeException("Received unknown menu type identifier!");
        this.container = this.handler.readContainer(buffer);
    }

    @Override
    public boolean verify(PacketContext context){
        return this.container != null;
    }

    @Override
    public void handle(PacketContext context){
        GuiScreen screen = ContainerScreenManager.createScreen(this.handler, this.container);
        if(screen != null)
            ClientUtils.displayScreen(screen);
    }
}
