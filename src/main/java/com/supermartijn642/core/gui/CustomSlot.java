package com.supermartijn642.core.gui;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.*;

/**
 * Created 29/01/2023 by SuperMartijn642
 */
@ApiStatus.NonExtendable
public interface CustomSlot {

    static Builder builder(){
        return CustomSlotImpl.builder();
    }

    Slot getVanillaSlot();

    /**
     * Moves this slot to the given position.
     */
    void move(int x, int y);

    /**
     * Whether this slot is rendered and interactable.
     */
    boolean isActive();

    /**
     * Sets whether this slot is rendered and interactable.
     */
    void setActive(boolean active);

    int getX();

    int getY();

    int getWidth();

    int getHeight();

    boolean scaleItemToSize();

    boolean showBackground();

    boolean showItem();

    boolean showHighlight();

    ItemStack getItem();

    interface Builder {

        Builder position(int x, int y);

        Builder size(int width, int height);

        Builder size(int size);

        /**
         * Used to query the item stack currently in the slot.
         */
        Builder getter(Supplier<ItemStack> getter);

        /**
         * Used to overwrite the item stack currently in the slot.
         */
        Builder setter(Consumer<ItemStack> getter);

        /**
         * Used to insert items into the slot. The given function should return the amount that was inserted.
         */
        Builder inserter(ToIntFunction<ItemStack> inserter);

        /**
         * Used to insert items into the slot. The given function should return the items that were extracted.
         */
        Builder extractor(Function<Integer,ItemStack> extractor);

        Builder capacity(ToIntFunction<ItemStack> capacity);

        /**
         * Filter for items that can be inserted into the slot.
         * <p>
         * Note that the setter is not affected by this filter.
         */
        Builder filter(Predicate<ItemStack> filter);

        Builder onInsert(SlotChangeListener onInsert);

        Builder onExtract(SlotChangeListener onExtract);

        Builder onChange(SlotChangeListener onChange);

        Builder vanillaContainer(int index, Container container);

        Builder playerInventory(int index, Inventory inventory);

        Builder itemStorage(Supplier<Storage<ItemVariant>> storageSupplier);

        Builder itemStorage(int index, Supplier<SlottedStorage<ItemVariant>> storageSupplier);

        /**
         * Note that the setter is not affected by this flag.
         */
        Builder canInsert(boolean canInsert);

        /**
         * Note that the setter is not affected by this flag.
         */
        Builder canExtract(boolean canExtract);

        /**
         * Note that the setter is not affected by this flag.
         */
        Builder canInsertExtract(boolean mutable);

        Builder scaleItemToSize(boolean scaleItemToSize);

        Builder scaleItemToSize();

        Builder showBackground(boolean showBackground);

        Builder noBackground();

        Builder showItem(boolean showItem);

        Builder showHighlight(boolean showHighlight);

        CustomSlot build();
    }

    interface SlotChangeListener {
        void onChange(ItemStack oldStack, ItemStack newStack);
    }
}
