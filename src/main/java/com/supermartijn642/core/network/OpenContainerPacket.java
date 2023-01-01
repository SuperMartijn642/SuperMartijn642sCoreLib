package com.supermartijn642.core.network;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.BaseContainer;
import com.supermartijn642.core.gui.BaseContainerType;
import com.supermartijn642.core.gui.ContainerScreenManager;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

/**
 * Created 05/08/2022 by SuperMartijn642
 */
public final class OpenContainerPacket<T extends BaseContainer> implements BasePacket {

    private BaseContainerType<T> handler;
    private T container;
    private int windowId;

    public OpenContainerPacket(T container, int windowId){
        //noinspection unchecked
        this.handler = (BaseContainerType<T>)container.getContainerType();
        this.container = container;
        this.windowId = windowId;
    }

    public OpenContainerPacket(){
    }

    @Override
    public void write(PacketBuffer buffer){
        buffer.writeString(Registries.MENU_TYPES.getIdentifier(this.handler).toString());
        buffer.writeInt(this.windowId);
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
            throw new RuntimeException("Received unknown menu type identifier '" + identifier + "'!");
        this.windowId = buffer.readInt();
        this.container = this.handler.readContainer(ClientUtils.getPlayer(), buffer);
    }

    @Override
    public boolean verify(PacketContext context){
        return this.container != null && this.windowId >= 0;
    }

    @Override
    public void handle(PacketContext context){
        ContainerScreenManager.displayContainer(this.handler, this.container, this.windowId);
    }
}
