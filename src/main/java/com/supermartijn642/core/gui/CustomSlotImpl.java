package com.supermartijn642.core.gui;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.*;

/**
 * Created 08/01/2026 by SuperMartijn642
 */
public class CustomSlotImpl extends Slot implements CustomSlot {

    static Builder builder(){
        return new BuilderImpl();
    }

    private static final Container EMPTY_CONTAINER = new Container() {
        @Override
        public int getContainerSize(){
            return 0;
        }

        @Override
        public boolean isEmpty(){
            return true;
        }

        @Override
        public ItemStack getItem(int i){
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int i, int j){
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int i){
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int i, ItemStack itemStack){
        }

        @Override
        public void setChanged(){
        }

        @Override
        public boolean stillValid(Player player){
            return false;
        }

        @Override
        public void clearContent(){
        }
    };

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

    private CustomSlotImpl(Container vanillaContainer, int vanillaSlot, int x, int y, int width, int height, Supplier<ItemStack> getter, Consumer<ItemStack> setter, ToIntFunction<ItemStack> inserter, Function<Integer,ItemStack> extractor, ToIntFunction<ItemStack> capacity, Predicate<ItemStack> filter, SlotChangeListener onInsert, SlotChangeListener onExtract, boolean canInsert, boolean canExtract, boolean scaleItemToSize, boolean showBackground, boolean showItem, boolean showHighlight){
        super(vanillaContainer == null ? EMPTY_CONTAINER : vanillaContainer, vanillaSlot, x, y);
        this.width = width;
        this.height = height;
        this.getter = getter == null ? () -> ItemStack.EMPTY : getter;
        this.setter = setter == null ? stack -> {} : setter;
        this.inserter = inserter == null ? setter == null ? stack -> 0 : stack -> {
            if(stack.isEmpty())
                return 0;
            ItemStack currentStack = this.getter.get();
            if(!currentStack.isEmpty() && !ItemStack.isSameItemSameTags(stack, currentStack))
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
        return this;
    }

    @Override
    public void move(int x, int y){
        this.x = x;
        this.y = y;
    }

    @Override
    public void setActive(boolean active){
        this.active = active;
    }

    @Override
    public int getX(){
        return this.x;
    }

    @Override
    public int getY(){
        return this.y;
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
    public boolean mayPlace(ItemStack stack){
        return this.canInsert && this.filter.test(stack);
    }

    @Override
    public ItemStack getItem(){
        return this.getter.get();
    }

    @Override
    public void set(ItemStack stack){
        ItemStack original = this.getter.get();
        this.setter.accept(stack);
        ItemStack newStack = this.getter.get();
        if(!ItemStack.matches(original, newStack)){
            if(newStack.isEmpty())
                this.onExtract.onChange(original, newStack);
            else
                this.onInsert.onChange(original, newStack);
        }
    }

    @Override
    public int getMaxStackSize(){
        return this.capacity.applyAsInt(ItemStack.EMPTY);
    }

    @Override
    public int getMaxStackSize(ItemStack stack){
        return this.capacity.applyAsInt(stack);
    }

    @Override
    public ItemStack remove(int amount){
        return this.extractor.apply(amount);
    }

    @Override
    public boolean mayPickup(Player player){
        return this.canExtract;
    }

    @Override
    public boolean isActive(){
        return this.active;
    }

    @Override
    public Optional<ItemStack> tryRemove(int amount, int ignored, Player player){
        if(!this.mayPickup(player))
            return Optional.empty();
        ItemStack original = this.getter.get();
        ItemStack extracted = this.extractor.apply(amount);
        ItemStack newStack = this.getter.get();
        if(!ItemStack.matches(original, newStack))
            this.onInsert.onChange(original, newStack);
        return extracted.isEmpty() ? Optional.empty() : Optional.of(extracted);
    }

    @Override
    public ItemStack safeInsert(ItemStack stack, int amount){
        if(stack.isEmpty() || !this.mayPlace(stack))
            return stack;
        ItemStack original = this.getter.get();
        int inserted = this.inserter.applyAsInt(copyWithCount(stack, amount));
        stack.shrink(inserted);
        ItemStack newStack = this.getter.get();
        if(!ItemStack.matches(original, newStack))
            this.onExtract.onChange(original, newStack);
        return stack;
    }

    private static ItemStack copyWithCount(ItemStack stack, int count){
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }

    @SuppressWarnings("UnstableApiUsage")
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
        private Container vanillaContainer;
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
        public Builder vanillaContainer(int index, Container container){
            this.getter(() -> container.getItem(index));
            this.setter(stack -> {
                container.setItem(index, stack);
                container.setChanged();
            });
            this.inserter(stack -> {
                if(stack.isEmpty())
                    return 0;
                ItemStack currentStack = container.getItem(index);
                int amount = Math.min(stack.getCount(), container.getMaxStackSize() - currentStack.getCount());
                if(amount <= 0 || (!currentStack.isEmpty() && !ItemStack.isSameItemSameTags(stack, currentStack)))
                    return 0;
                container.setItem(index, copyWithCount(stack, currentStack.getCount() + amount));
                container.setChanged();
                return amount;
            });
            this.extractor(amount -> {
                ItemStack extracted = container.removeItem(index, amount);
                if(!extracted.isEmpty()){
                    if(container.getItem(index).isEmpty()) // For some reason vanilla explicitly sets the slot to empty
                        container.setItem(index, ItemStack.EMPTY);
                    container.setChanged();
                }
                return extracted;
            });
            this.capacity(stack -> container.getMaxStackSize());
            this.vanillaContainer = container;
            this.vanillaSlot = index;
            return this;
        }

        @Override
        public Builder playerInventory(int index, Inventory inventory){
            return this.vanillaContainer(index, inventory);
        }

        @Override
        public Builder itemStorage(Supplier<Storage<ItemVariant>> storageSupplier){
            this.getter(() -> {
                Storage<ItemVariant> storage = storageSupplier.get();
                if(storage instanceof SingleSlotStorage<ItemVariant> view)
                    return view.getResource().toStack((int)Math.min(Integer.MAX_VALUE, view.getAmount()));
                //noinspection deprecation,DataFlowIssue
                Transaction transaction = Transaction.isOpen() ?
                    Transaction.getCurrentUnsafe().getOpenTransaction(Transaction.getCurrentUnsafe().nestingDepth()).openNested() :
                    Transaction.openOuter();
                try(transaction){
                    Iterator<? extends StorageView<ItemVariant>> iterator = storage.iterator(transaction);
                    if(iterator.hasNext()){
                        StorageView<ItemVariant> view = iterator.next();
                        return view.getResource().toStack((int)Math.min(Integer.MAX_VALUE, view.getAmount()));
                    }
                }
                return ItemStack.EMPTY;
            });
            this.inserter(stack -> {
                Storage<ItemVariant> storage = storageSupplier.get();
                if(!storage.supportsInsertion())
                    return 0;
                //noinspection deprecation,DataFlowIssue
                Transaction transaction = Transaction.isOpen() ?
                    Transaction.getCurrentUnsafe().getOpenTransaction(Transaction.getCurrentUnsafe().nestingDepth()).openNested() :
                    Transaction.openOuter();
                try(transaction){
                    int inserted = (int)storage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
                    if(inserted < 0)
                        throw new IllegalStateException("Item storage of class '" + storage.getClass().getName() + "' returned negative amount for #insert!");
                    transaction.commit();
                    return inserted;
                }
            });
            this.extractor(amount -> {
                Storage<ItemVariant> storage = storageSupplier.get();
                if(!storage.supportsExtraction())
                    return ItemStack.EMPTY;
                //noinspection deprecation,DataFlowIssue
                Transaction transaction = Transaction.isOpen() ?
                    Transaction.getCurrentUnsafe().getOpenTransaction(Transaction.getCurrentUnsafe().nestingDepth()).openNested() :
                    Transaction.openOuter();
                if(storage instanceof SingleSlotStorage<ItemVariant> view){
                    try(transaction){
                        ItemVariant resource = view.getResource();
                        int extracted = (int)view.extract(resource, amount, transaction);
                        if(extracted < 0)
                            throw new IllegalStateException("Item storage of class '" + storage.getClass().getName() + "' returned negative amount for #extract from storage view!");
                        transaction.commit();
                        return resource.toStack(extracted);
                    }
                }
                try(transaction){
                    Iterator<? extends StorageView<ItemVariant>> iterator = storage.iterator(transaction);
                    if(iterator.hasNext()){
                        StorageView<ItemVariant> view = iterator.next();
                        ItemVariant resource = view.getResource();
                        int extracted = (int)view.extract(resource, amount, transaction);
                        if(extracted < 0)
                            throw new IllegalStateException("Item storage of class '" + storage.getClass().getName() + "' returned negative amount for #extract from storage view!");
                        transaction.commit();
                        return resource.toStack(extracted);
                    }
                }
                return ItemStack.EMPTY;
            });
            this.capacity(stack -> {
                Storage<ItemVariant> storage = storageSupplier.get();
                if(storage instanceof SingleSlotStorage<ItemVariant> view)
                    return (int)Math.min(Integer.MAX_VALUE, view.getCapacity());
                //noinspection deprecation,DataFlowIssue
                Transaction transaction = Transaction.isOpen() ?
                    Transaction.getCurrentUnsafe().getOpenTransaction(Transaction.getCurrentUnsafe().nestingDepth()).openNested() :
                    Transaction.openOuter();
                try(transaction){
                    Iterator<? extends StorageView<ItemVariant>> iterator = storage.iterator(transaction);
                    if(iterator.hasNext()){
                        StorageView<ItemVariant> view = iterator.next();
                        long capacity = view.getCapacity();
                        if(capacity < 0)
                            throw new IllegalStateException("Item storage of class '" + storage.getClass().getName() + "' returned negative amount for #capacity from storage view!");
                        return (int)Math.min(Integer.MAX_VALUE, capacity);
                    }
                }
                return stack.isEmpty() ? 99 : stack.getMaxStackSize();
            });
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
