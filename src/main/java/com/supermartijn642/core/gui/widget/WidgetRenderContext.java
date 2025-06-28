package com.supermartijn642.core.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Created 05/06/2023 by SuperMartijn642
 */
public interface WidgetRenderContext {

    float partialTicks();

    GuiGraphics guiGraphics();

    Font font();

    Minecraft minecraft();
}
