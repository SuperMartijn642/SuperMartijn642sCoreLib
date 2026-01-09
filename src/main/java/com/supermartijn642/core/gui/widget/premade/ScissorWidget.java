package com.supermartijn642.core.gui.widget.premade;

import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.BaseWidget;
import com.supermartijn642.core.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;

/**
 * A widgets that restricts the rendering and mouse input handling of its children to within its boundary.
 * <p>
 * Created 09/01/2026 by SuperMartijn642
 */
public class ScissorWidget extends BaseWidget {

    /**
     * @see ScissorWidget
     */
    public static ScissorWidget create(int x, int y, int width, int height, Widget... children){
        return new ScissorWidget(x, y, width, height, children);
    }

    private ScissorWidget(int x, int y, int width, int height, Widget... children){
        super(x, y, width, height);
        for(Widget child : children)
            this.addWidget(child);
    }

    @Override
    public ITextComponent getNarrationMessage(){
        return null;
    }

    @Override
    public <T extends Widget> T addWidget(T widget){
        return super.addWidget(widget);
    }

    @Override
    public boolean removeWidget(Widget widget){
        return super.removeWidget(widget);
    }

    private void renderScissored(int mouseX, int mouseY, RenderFunction renderFunction){
        if(mouseX < this.x || mouseX > this.x + this.width || mouseY < this.y || mouseY > this.y + this.height)
            mouseX = mouseY = -100;
        int finalMouseX = mouseX;
        int finalMouseY = mouseY;
        ScreenUtils.withScissor(this.x, this.y, this.width, this.height, () -> renderFunction.render(finalMouseX, finalMouseY));
    }

    private interface RenderFunction {
        void render(int mouseX, int mouseY);
    }

    @Override
    public void renderBackground(int mouseX, int mouseY){
        this.renderScissored(mouseX, mouseY, super::renderBackground);
    }

    @Override
    public void render(int mouseX, int mouseY){
        this.renderScissored(mouseX, mouseY, super::render);
    }

    @Override
    public void renderForeground(int mouseX, int mouseY){
        this.renderScissored(mouseX, mouseY, super::renderForeground);
    }

    @Override
    public void renderOverlay(int mouseX, int mouseY){
        this.renderScissored(mouseX, mouseY, super::renderOverlay);
    }

    @Override
    public void renderTooltips(int mouseX, int mouseY){
        if(mouseX < this.x || mouseX > this.x + this.width || mouseY < this.y || mouseY > this.y + this.height)
            mouseX = mouseY = -100;
        super.renderTooltips(mouseX, mouseY);
    }

    @Override
    public boolean mousePressed(int mouseX, int mouseY, int button, boolean hasBeenHandled){
        if(mouseX < this.x || mouseX > this.x + this.width || mouseY < this.y || mouseY > this.y + this.height)
            mouseX = mouseY = -100;
        return super.mousePressed(mouseX, mouseY, button, hasBeenHandled);
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int button, boolean hasBeenHandled){
        if(mouseX < this.x || mouseX > this.x + this.width || mouseY < this.y || mouseY > this.y + this.height)
            mouseX = mouseY = -100;
        return super.mouseReleased(mouseX, mouseY, button, hasBeenHandled);
    }

    @Override
    public boolean mouseScrolled(int mouseX, int mouseY, double scrollAmount, boolean hasBeenHandled){
        if(mouseX < this.x || mouseX > this.x + this.width || mouseY < this.y || mouseY > this.y + this.height)
            mouseX = mouseY = -100;
        return super.mouseScrolled(mouseX, mouseY, scrollAmount, hasBeenHandled);
    }
}
