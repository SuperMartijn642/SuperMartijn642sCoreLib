package com.supermartijn642.core.gui;

import org.lwjgl.glfw.GLFW;

/**
 * Created 15/01/2026 by SuperMartijn642
 */
public final class CursorType {

    private final int glfwCursorEnum;
    private long cursorHandle;
    private boolean created = false;

    CursorType(int glfwCursorEnum){
        this.glfwCursorEnum = glfwCursorEnum;
    }

    long getCursorHandle(){
        if(!this.created){
            this.cursorHandle = GLFW.glfwCreateStandardCursor(this.glfwCursorEnum);
            this.created = true;
        }
        return this.cursorHandle;
    }
}
