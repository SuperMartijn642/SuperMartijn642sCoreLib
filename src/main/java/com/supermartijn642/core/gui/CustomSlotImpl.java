package com.supermartijn642.core.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandler;

import java.util.function.*;

/**
 * Created 08/01/2026 by SuperMartijn642
 */
public class CustomSlotImpl implements CustomSlot {

    static Builder builder(){
        return new BuilderImpl();
    }

    private static final IInventory EMPTY_CONTAINER = new IInventory() {
        @Override
        public String getName(){
            return "";
        }

        @Override
        public boolean hasCustomName(){
            return false;
        }

        @Override
        public ITextComponent getDisplayName(){
            return null;
        }

        @Override
        public int getSizeInventory(){
            return 0;
        }

        @Override
        public boolean isEmpty(){
            return true;
        }

        @Override
        public ItemStack getStackInSlot(int i){
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack decrStackSize(int i, int j){
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeStackFromSlot(int i){
            return ItemStack.EMPTY;
        }

        @Override
        public void setInventorySlotContents(int i, ItemStack itemStack){
        }

        @Override
        public int getInventoryStackLimit(){
            return 0;
        }

        @Override
        public void markDirty(){
        }

        @Override
        public boolean isUsableByPlayer(EntityPlayer player){
            return false;
        }

        @Override
        public void openInventory(EntityPlayer player){
        }

        @Override
        public void closeInventory(EntityPlayer player){

        }

        @Override
        public boolean isItemValidForSlot(int i, ItemStack stack){
            return false;
        }

        @Override
        public int getField(int i){
            return 0;
        }

        @Override
        public void setField(int i, int j){
        }

        @Override
        public int getFieldCount(){
            return 0;
        }

        @Override
        public void clear(){
        }
    };

    /*
     * We have to separate the vanilla Slot implementation from the CustomSlot implementation
     * Otherwise, ForgeGradle will obfuscate CustomSlot#isActive to Slot#isActive eventhough the interface method has nothing to do with Slot
     * https://github.com/SuperMartijn642/TrashCans/issues/56
     */

    private final VanillaSlot vanillaSlot;
    private final int width, height;
    private final Supplier<ItemStack> getter;
    private final Consumer<ItemStack> setter;
    private final ToIntFunction<ItemStack> inserter;
    private final Function<Integer,ItemStack> extractor;
    private final ToIntFunction<ItemStack> capacity;
    private final Predicate<ItemStack> filter;
    private final SlotChangeListener onInsert, onExtract;
    private final boolean canInsert, canExtract;
    private final boolean scaleItemToSize;
    private final boolean showBackground, showItem, showHighlight;

    private boolean active = true;

    private CustomSlotImpl(IInventory vanillaContainer, int vanillaSlot, int x, int y, int width, int height, Supplier<ItemStack> getter, Consumer<ItemStack> setter, ToIntFunction<ItemStack> inserter, Function<Integer,ItemStack> extractor, ToIntFunction<ItemStack> capacity, Predicate<ItemStack> filter, SlotChangeListener onInsert, SlotChangeListener onExtract, boolean canInsert, boolean canExtract, boolean scaleItemToSize, boolean showBackground, boolean showItem, boolean showHighlight){
        this.vanillaSlot = new VanillaSlot(vanillaContainer, vanillaSlot, x, y);
        this.width = width;
        this.height = height;
        this.getter = getter == null ? () -> ItemStack.EMPTY : getter;
        this.setter = setter == null ? stack -> {} : setter;
        this.inserter = inserter == null ? setter == null ? stack -> 0 : stack -> {
            if(stack.isEmpty())
                return 0;
            ItemStack currentStack = this.getter.get();
            if(!currentStack.isEmpty() && (!ItemStack.areItemsEqual(stack, currentStack) || !ItemStack.areItemStackTagsEqual(stack, currentStack)))
                return 0;
            int inserted = Math.min(stack.getCount(), stack.getMaxStackSize() - currentStack.getCount());
            if(capacity != null)
                inserted = Math.min(inserted, capacity.applyAsInt(stack));
            setter.accept(copyWithCount(stack, currentStack.getCount() + inserted));
            return inserted;
        } : inserter;
        this.extractor = extractor == null ? setter == null ? amount -> ItemStack.EMPTY : amount -> {
            if(amount <= 0)
                return ItemStack.EMPTY;
            ItemStack currentStack = this.getter.get();
            if(currentStack.isEmpty())
                return ItemStack.EMPTY;
            int extracted = Math.min(amount, currentStack.getCount());
            ItemStack extractedStack = copyWithCount(currentStack, extracted);
            setter.accept(copyWithCount(currentStack, currentStack.getCount() - extracted));
            return extractedStack;
        } : extractor;
        this.capacity = capacity == null ? stack -> stack.isEmpty() ? 64 : stack.getMaxStackSize() : capacity;
        this.filter = filter == null ? stack -> true : filter;
        this.onInsert = onInsert == null ? (oldStack, newStack) -> {} : onInsert;
        this.onExtract = onExtract == null ? (oldStack, newStack) -> {} : onExtract;
        this.canInsert = canInsert;
        this.canExtract = canExtract;
        this.scaleItemToSize = scaleItemToSize;
        this.showBackground = showBackground;
        this.showItem = showItem;
        this.showHighlight = showHighlight;
    }

    @Override
    public Slot getVanillaSlot(){
        return this.vanillaSlot;
    }

    @Override
    public void move(int x, int y){
        this.vanillaSlot.xPos = x;
        this.vanillaSlot.yPos = y;
    }

    @Override
    public boolean isActive(){
        return this.active;
    }

    @Override
    public void setActive(boolean active){
        this.active = active;
    }

    @Override
    public int getX(){
        return this.vanillaSlot.xPos;
    }

    @Override
    public int getY(){
        return this.vanillaSlot.yPos;
    }

    @Override
    public int getWidth(){
        return this.width;
    }

    @Override
    public int getHeight(){
        return this.height;
    }

    @Override
    public boolean scaleItemToSize(){
        return this.scaleItemToSize;
    }

    @Override
    public boolean showBackground(){
        return this.showBackground;
    }

    @Override
    public boolean showItem(){
        return this.showItem;
    }

    @Override
    public boolean showHighlight(){
        return this.showHighlight;
    }

    @Override
    public ItemStack getItem(){
        return this.vanillaSlot.getStack();
    }

    private class VanillaSlot extends Slot {

        /*
         * Minecraft (,1.16.5] just modify the slots returned stack rather than updating it explicitly.
         * As returned stacks may be arbitrary instances, we need to keep track and update the stack manually.
         */
        private ItemStack lastReturnedStack;
        private int lastReturnedStackCount = 0;

        public VanillaSlot(IInventory vanillaContainer, int vanillaSlot, int x, int y){
            super(vanillaContainer == null ? EMPTY_CONTAINER : vanillaContainer, vanillaSlot, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack){
            return CustomSlotImpl.this.canInsert && CustomSlotImpl.this.filter.test(stack);
        }

        @Override
        public ItemStack getStack(){
            ItemStack stack = CustomSlotImpl.this.getter.get();
            this.lastReturnedStack = stack;
            this.lastReturnedStackCount = stack.getCount();
            return stack;
        }

        @Override
        public void putStack(ItemStack stack){
            ItemStack original = CustomSlotImpl.this.getter.get();
            CustomSlotImpl.this.setter.accept(stack);
            ItemStack newStack = CustomSlotImpl.this.getter.get();
            if(!ItemStack.areItemStacksEqual(original, newStack)){
                if(newStack.isEmpty())
                    CustomSlotImpl.this.onExtract.onChange(original, newStack);
                else
                    CustomSlotImpl.this.onInsert.onChange(original, newStack);
            }
            this.lastReturnedStack = stack;
            this.lastReturnedStackCount = stack.getCount();
        }

        @Override
        public int getSlotStackLimit(){
            return CustomSlotImpl.this.capacity.applyAsInt(ItemStack.EMPTY);
        }

        @Override
        public int getItemStackLimit(ItemStack stack){
            return CustomSlotImpl.this.capacity.applyAsInt(stack);
        }

        @Override
        public ItemStack decrStackSize(int amount){
            ItemStack original = CustomSlotImpl.this.getter.get();
            ItemStack extracted = CustomSlotImpl.this.extractor.apply(amount);
            ItemStack newStack = CustomSlotImpl.this.getter.get();
            if(!ItemStack.areItemStacksEqual(original, newStack))
                CustomSlotImpl.this.onExtract.onChange(original, newStack);
            this.lastReturnedStack = null;
            return extracted;
        }

        @Override
        public boolean canTakeStack(EntityPlayer player){
            return CustomSlotImpl.this.canExtract;
        }

        @Override
        public boolean isEnabled(){
            return CustomSlotImpl.this.active;
        }

        @Override
        public void onSlotChanged(){
            // Check if the stack size of the last given stack was modified
            if(this.lastReturnedStack != null && this.lastReturnedStack.getCount() != this.lastReturnedStackCount)
                this.putStack(this.lastReturnedStack);
            super.onSlotChanged();
        }
    }

    private static ItemStack copyWithCount(ItemStack stack, int count){
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }

    private static class BuilderImpl implements Builder {

        private int x, y;
        private int width = 18, height = 18;
        private Supplier<ItemStack> getter;
        private Consumer<ItemStack> setter;
        private ToIntFunction<ItemStack> inserter;
        private Function<Integer,ItemStack> extractor;
        private ToIntFunction<ItemStack> capacity;
        private Predicate<ItemStack> filter;
        private SlotChangeListener onInsert, onExtract;
        private boolean canInsert = true, canExtract = true;
        private boolean scaleItemToSize = false;
        private boolean showBackground = true, showItem = true, showHighlight = true;

        /**
         * Set the original container field for the slot in case mods try to access it directly.
         */
        private IInventory vanillaContainer;
        private int vanillaSlot;

        private BuilderImpl(){
        }

        @Override
        public Builder position(int x, int y){
            this.x = x;
            this.y = y;
            return this;
        }

        @Override
        public Builder size(int width, int height){
            this.width = width;
            this.height = height;
            return this;
        }

        @Override
        public Builder size(int size){
            return this.size(size, size);
        }

        @Override
        public Builder getter(Supplier<ItemStack> getter){
            this.getter = getter;
            return this;
        }

        @Override
        public Builder setter(Consumer<ItemStack> getter){
            this.setter = getter;
            return this;
        }

        @Override
        public Builder inserter(ToIntFunction<ItemStack> inserter){
            this.inserter = inserter;
            return this;
        }

        @Override
        public Builder extractor(Function<Integer,ItemStack> extractor){
            this.extractor = extractor;
            return this;
        }

        @Override
        public Builder capacity(ToIntFunction<ItemStack> capacity){
            this.capacity = capacity;
            return this;
        }

        @Override
        public Builder filter(Predicate<ItemStack> filter){
            this.filter = filter;
            return this;
        }

        @Override
        public Builder onInsert(SlotChangeListener onInsert){
            this.onInsert = onInsert;
            return this;
        }

        @Override
        public Builder onExtract(SlotChangeListener onExtract){
            this.onExtract = onExtract;
            return this;
        }

        @Override
        public Builder onChange(SlotChangeListener onChange){
            return this.onInsert(onChange).onExtract(onChange);
        }

        @Override
        public Builder vanillaContainer(int index, IInventory container){
            this.getter(() -> container.getStackInSlot(index));
            this.setter(stack -> {
                container.setInventorySlotContents(index, stack);
                container.markDirty();
            });
            this.inserter(stack -> {
                if(stack.isEmpty())
                    return 0;
                ItemStack currentStack = container.getStackInSlot(index);
                int amount = Math.min(stack.getCount(), container.getInventoryStackLimit() - currentStack.getCount());
                if(amount <= 0 || (!currentStack.isEmpty() && (!ItemStack.areItemsEqual(stack, currentStack) || !ItemStack.areItemStackTagsEqual(stack, currentStack))))
                    return 0;
                container.setInventorySlotContents(index, copyWithCount(stack, currentStack.getCount() + amount));
                container.markDirty();
                return amount;
            });
            this.extractor(amount -> {
                ItemStack extracted = container.decrStackSize(index, amount);
                if(!extracted.isEmpty()){
                    if(container.getStackInSlot(index).isEmpty()) // For some reason vanilla explicitly sets the slot to empty
                        container.setInventorySlotContents(index, ItemStack.EMPTY);
                    container.markDirty();
                }
                return extracted;
            });
            this.capacity(stack -> container.getInventoryStackLimit());
            this.vanillaContainer = container;
            this.vanillaSlot = index;
            return this;
        }

        @Override
        public Builder playerInventory(int index, InventoryPlayer inventory){
            return this.vanillaContainer(index, inventory);
        }

        @Override
        public Builder itemHandler(int index, Supplier<IItemHandler> handlerSupplier){
            this.getter(() -> handlerSupplier.get().getStackInSlot(0));
            this.inserter(stack -> {
                if(stack.isEmpty())
                    return 0;
                IItemHandler handler = handlerSupplier.get();
                int initialCount = stack.getCount();
                ItemStack leftover = handler.insertItem(0, stack, false);
                if(stack.getCount() != initialCount)
                    throw new RuntimeException("Item handler of class '" + handler.getClass() + "' modified input stack to #insertItem!");
                return initialCount - leftover.getCount();
            });
            this.extractor(amount -> {
                if(amount <= 0)
                    return ItemStack.EMPTY;
                IItemHandler handler = handlerSupplier.get();
                return handler.extractItem(index, amount, false);
            });
            this.capacity(stack -> handlerSupplier.get().getSlotLimit(0));
            return this;
        }

        @Override
        public Builder canInsert(boolean canInsert){
            this.canInsert = canInsert;
            return this;
        }

        @Override
        public Builder canExtract(boolean canExtract){
            this.canExtract = canExtract;
            return this;
        }

        @Override
        public Builder canInsertExtract(boolean mutable){
            return this.canExtract(mutable).canInsert(mutable);
        }

        @Override
        public Builder scaleItemToSize(boolean scaleItemToSize){
            this.scaleItemToSize = scaleItemToSize;
            return this;
        }

        @Override
        public Builder scaleItemToSize(){
            return this.scaleItemToSize(true);
        }

        @Override
        public Builder showBackground(boolean showBackground){
            this.showBackground = showBackground;
            return this;
        }

        @Override
        public Builder noBackground(){
            this.showBackground = false;
            return this;
        }

        @Override
        public Builder showItem(boolean showItem){
            this.showItem = showItem;
            return this;
        }

        @Override
        public Builder showHighlight(boolean showHighlight){
            this.showHighlight = showHighlight;
            return this;
        }

        @Override
        public CustomSlot build(){
            return new CustomSlotImpl(
                this.vanillaContainer, this.vanillaSlot,
                this.x, this.y,
                this.width, this.height,
                this.getter, this.setter,
                this.inserter, this.extractor,
                this.capacity,
                this.filter,
                this.onInsert, this.onExtract,
                this.canInsert, this.canExtract,
                this.scaleItemToSize,
                this.showBackground, this.showItem, this.showHighlight
            );
        }
    }
}
