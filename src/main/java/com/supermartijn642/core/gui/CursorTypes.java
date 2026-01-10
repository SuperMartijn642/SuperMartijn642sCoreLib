package com.supermartijn642.core.gui;

import com.mojang.blaze3d.platform.cursor.CursorType;

import static com.mojang.blaze3d.platform.cursor.CursorTypes.*;

/**
 * Created 10/01/2026 by SuperMartijn642
 */
public class CursorTypes {

    public static CursorType arrow(){
        return ARROW;
    }

    public static CursorType iBeam(){
        return IBEAM;
    }

    public static CursorType crosshair(){
        return CROSSHAIR;
    }

    public static CursorType pointingHand(){
        return POINTING_HAND;
    }

    public static CursorType resizeVertical(){
        return RESIZE_NS;
    }

    public static CursorType resizeHorizontal(){
        return RESIZE_EW;
    }

    public static CursorType resizeAll(){
        return RESIZE_ALL;
    }

    public static CursorType notAllowed(){
        return NOT_ALLOWED;
    }
}
