package com.supermartijn642.core.gui.widget;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Consumer;

/**
 * Created 17/07/2022 by SuperMartijn642
 */
public abstract class ObjectBaseWidget<T> extends BaseWidget {

    protected T object;
    private final boolean alwaysRenewObject;

    public ObjectBaseWidget(int x, int y, int width, int height, boolean alwaysRenewObject){
        super(x, y, width, height);
        this.alwaysRenewObject = alwaysRenewObject;
    }

    public ObjectBaseWidget(int x, int y, int width, int height){
        this(x, y, width, height, false);
    }

    /**
     * Called to obtain object needed for this widget to remain active. May be called at any time.
     * @param oldObject the old object, will be {@code null} when the widget is first added
     * @return the object required for the container to remain open
     */
    protected abstract T getObject(T oldObject);

    /**
     * Validates the object obtained from {@link #getObject(Object)}.
     * The associated screen will be closed if {@code false} is returned.
     * @param object object to be validated, may be null
     * @return true if the object is valid
     */
    protected abstract boolean validateObject(T object);

    /**
     * Validates the object. If the object is not valid the screen will be closed.
     * @return true if the object is valid
     */
    protected boolean validateObjectOrClose(){
        if(this.alwaysRenewObject || !this.validateObject(this.object)){
            this.object = this.getObject(this.object);
            if(!this.validateObject(this.object)){
                ClientUtils.closeScreen();
                return false;
            }
        }
        return true;
    }

    @Override
    public final ITextComponent getNarrationMessage(){
        return this.validateObjectOrClose() ? this.getNarrationMessage(this.object) : null;
    }

    /**
     * @return the title to be read by the narrator when the widget is focused by the user
     */
    protected abstract ITextComponent getNarrationMessage(T object);

    @Override
    public final int width(){
        return this.validateObjectOrClose() ? this.width(this.object) : 0;
    }

    /**
     * @return the width of the widget
     */
    protected int width(T object){
        return super.width();
    }

    @Override
    public final int height(){
        return this.validateObjectOrClose() ? this.height(this.object) : 0;
    }

    /**
     * @return the height of the widget
     */
    protected int height(T object){
        return super.height();
    }

    @Override
    public final int left(){
        return this.validateObjectOrClose() ? this.width(this.object) : 0;
    }

    /**
     * @return the x-position of this widget
     */
    protected int left(T object){
        return super.left();
    }

    @Override
    public final int top(){
        return this.validateObjectOrClose() ? this.top(this.object) : 0;
    }

    /**
     * @return the y-position of this widget
     */
    protected int top(T object){
        return super.top();
    }

    @Override
    public final void initialize(){
        if(this.validateObjectOrClose())
            this.initialize(this.object);
    }

    /**
     * Called when the widget is added.
     */
    protected void initialize(T object){
        super.initialize();
    }

    @Override
    protected void addWidgets(){
        if(this.validateObjectOrClose())
            this.addWidgets(this.object);
    }

    /**
     * Adds widgets to the screen via {@link #addWidget(Widget)}.
     */
    protected void addWidgets(T object){
        super.addWidgets();
    }

    @Override
    public final void update(){
        if(this.validateObjectOrClose())
            this.update(this.object);
    }

    /**
     * Called once per tick when the widget is shown.
     */
    protected void update(T object){
        super.update();
    }

    @Override
    public final void renderBackground(int mouseX, int mouseY){
        if(this.validateObjectOrClose())
            this.renderBackground(mouseX, mouseY, this.object);
    }

    /**
     * Renders the widget's background. This will be called first in the render chain.
     */
    protected void renderBackground(int mouseX, int mouseY, T object){
        super.renderBackground(mouseX, mouseY);
    }

    @Override
    public final void render(int mouseX, int mouseY){
        if(this.validateObjectOrClose())
            this.render(mouseX, mouseY, this.object);
    }

    /**
     * Renders the widget's main features.
     * Called after the background and slots are drawn, but before items are drawn.
     */
    protected void render(int mouseX, int mouseY, T object){
        super.render(mouseX, mouseY);
    }

    @Override
    public final void renderForeground(int mouseX, int mouseY){
        if(this.validateObjectOrClose())
            this.renderForeground(mouseX, mouseY, this.object);
    }

    /**
     * Renders the widget's foreground.
     * Called after main features and items are drawn, but before cursor item and overlay are drawn.
     */
    protected void renderForeground(int mouseX, int mouseY, T object){
        super.renderForeground(mouseX, mouseY);
    }

    @Override
    public final void renderOverlay(int mouseX, int mouseY){
        if(this.validateObjectOrClose())
            this.renderOverlay(mouseX, mouseY, this.object);
    }

    /**
     * Called after foreground and cursor item are drawn, but before tooltips are drawn.
     */
    protected void renderOverlay(int mouseX, int mouseY, T object){
        super.renderOverlay(mouseX, mouseY);
    }

    @Override
    public final void renderTooltips(int mouseX, int mouseY){
        if(this.validateObjectOrClose())
            this.renderTooltips(mouseX, mouseY, this.object);
    }

    /**
     * Renders tooltips for the given {@code mouseX} and {@code mouseY}.
     * This will be called last in the render chain.
     */
    protected void renderTooltips(int mouseX, int mouseY, T object){
        super.renderTooltips(mouseX, mouseY);
    }

    @Override
    protected void getTooltips(Consumer<ITextComponent> tooltips){
        if(this.validateObjectOrClose())
            this.getTooltips(tooltips, this.object);
    }

    /**
     * Gathers the tooltips to be rendered in {@link #renderTooltips(int, int)}. Tooltips will only be shown when this widget is focused.
     * @param tooltips consumer for tooltips to be rendered
     */
    protected void getTooltips(Consumer<ITextComponent> tooltips, T object){
        super.getTooltips(tooltips);
    }

    @Override
    public final boolean mousePressed(int mouseX, int mouseY, int button, boolean hasBeenHandled){
        return this.validateObjectOrClose() && this.mousePressed(mouseX, mouseY, button, hasBeenHandled, this.object);
    }

    /**
     * Called when a mouse button is pressed down.
     * @param mouseX         x-position of the mouse
     * @param mouseY         y-position of the mouse
     * @param button         the button which is pressed down
     * @param hasBeenHandled whether the mouse press has already been handled
     * @return whether this widget has handled the mouse press
     */
    protected boolean mousePressed(int mouseX, int mouseY, int button, boolean hasBeenHandled, T object){
        return super.mousePressed(mouseX, mouseY, button, hasBeenHandled);
    }

    @Override
    public final boolean mouseReleased(int mouseX, int mouseY, int button, boolean hasBeenHandled){
        return this.validateObjectOrClose() && this.mouseReleased(mouseX, mouseY, button, hasBeenHandled, this.object);
    }

    /**
     * Called when a mouse button is released.
     * @param mouseX         x-position of the mouse
     * @param mouseY         y-position of the mouse
     * @param button         the button which is pressed down
     * @param hasBeenHandled whether the mouse press has already been handled
     * @return whether this widget has handled the mouse release
     */
    protected boolean mouseReleased(int mouseX, int mouseY, int button, boolean hasBeenHandled, T object){
        return super.mouseReleased(mouseX, mouseY, button, hasBeenHandled);
    }

    @Override
    public final boolean mouseScrolled(int mouseX, int mouseY, double scrollAmount, boolean hasBeenHandled){
        return this.validateObjectOrClose() && this.mouseScrolled(mouseX, mouseY, scrollAmount, hasBeenHandled, this.object);
    }

    /**
     * Called when the mouse wheel is scrolled.
     * @param mouseX         x-position of the mouse
     * @param mouseY         y-position of the mouse
     * @param scrollAmount   the amount the mouse wheel was scrolled by
     * @param hasBeenHandled whether the mouse press has already been handled
     * @return whether this widget has handled the mouse scroll
     */
    protected boolean mouseScrolled(int mouseX, int mouseY, double scrollAmount, boolean hasBeenHandled, T object){
        return super.mouseScrolled(mouseX, mouseY, scrollAmount, hasBeenHandled);
    }

    @Override
    public final boolean keyPressed(int keyCode, boolean hasBeenHandled){
        return this.validateObjectOrClose() && this.keyPressed(keyCode, hasBeenHandled, this.object);
    }

    /**
     * Called when a key is pressed down.
     * @param keyCode code of the key which was pressed
     * @return whether this widget has handled the key press
     */
    protected boolean keyPressed(int keyCode, boolean hasBeenHandled, T object){
        return super.keyPressed(keyCode, hasBeenHandled);
    }

    @Override
    public final boolean keyReleased(int keyCode, boolean hasBeenHandled){
        return this.validateObjectOrClose() && this.keyReleased(keyCode, hasBeenHandled, this.object);
    }

    /**
     * Called when a key is released.
     * @param keyCode code of the key which was released
     * @return whether this widget has handled the key release
     */
    protected boolean keyReleased(int keyCode, boolean hasBeenHandled, T object){
        return super.keyReleased(keyCode, hasBeenHandled);
    }

    @Override
    public final boolean charTyped(char character, boolean hasBeenHandled){
        return this.validateObjectOrClose() && this.charTyped(character, hasBeenHandled, this.object);
    }

    /**
     * Called when a character is typed. May be called in addition to {@link #keyPressed(int, boolean)}.
     * @param character the character which was typed
     * @return whether this widget has handled the character
     */
    protected boolean charTyped(char character, boolean hasBeenHandled, T object){
        return super.charTyped(character, hasBeenHandled);
    }
}
