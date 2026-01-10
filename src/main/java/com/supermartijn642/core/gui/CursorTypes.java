package com.supermartijn642.core.gui;


import com.supermartijn642.core.ClientUtils;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.glfw.GLFW;

/**
 * Created 10/01/2026 by SuperMartijn642
 */
public class CursorTypes {

    static CursorType pendingCursor;
    private static long lastCursorHandle;

    @ApiStatus.Internal
    public static void applyPending(){
        long handle = pendingCursor == null ? 0 : pendingCursor.getCursorHandle();
        if(handle != lastCursorHandle){
            GLFW.glfwSetCursor(ClientUtils.getMinecraft().getWindow().getWindow(), handle);
            lastCursorHandle = handle;
        }
        pendingCursor = null;
    }

    private static final CursorType ARROW = new CursorType(GLFW.GLFW_ARROW_CURSOR);
    private static final CursorType IBEAM = new CursorType(GLFW.GLFW_IBEAM_CURSOR);
    private static final CursorType CROSSHAIR = new CursorType(GLFW.GLFW_CROSSHAIR_CURSOR);
    private static final CursorType POINTING_HAND = new CursorType(GLFW.GLFW_HAND_CURSOR);
    private static final CursorType RESIZE_VERTICAL = new CursorType(GLFW.GLFW_VRESIZE_CURSOR);
    private static final CursorType RESIZE_HORIZONTAL = new CursorType(GLFW.GLFW_HRESIZE_CURSOR);

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
        return RESIZE_VERTICAL;
    }

    public static CursorType resizeHorizontal(){
        return RESIZE_HORIZONTAL;
    }
}
