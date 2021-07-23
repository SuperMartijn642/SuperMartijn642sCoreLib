package com.supermartijn642.core.gui.widget;

import net.minecraft.network.chat.Component;

/**
 * Created 10/29/2020 by SuperMartijn642
 */
public interface IHoverTextWidget {

    /**
     * @return the message which should be displayed as a tooltip when the user
     * hovers their cursor over the widget
     */
    Component getHoverText();
}
