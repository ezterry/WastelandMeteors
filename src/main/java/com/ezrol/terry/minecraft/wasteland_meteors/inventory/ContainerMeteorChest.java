package com.ezrol.terry.minecraft.wasteland_meteors.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Created by ezterry on 12/12/16.
 */
public class ContainerMeteorChest extends Container {
    private final IInventory meteorInv;

    public ContainerMeteorChest(IInventory playerInventory, IInventory chestInventory, EntityPlayer player){
        this.meteorInv = chestInventory;
        chestInventory.openInventory(player);
        int i,j;

        for (i = 0; i < 3; ++i)
        {
            for (j= 0; j < 6; ++j)
            {
                this.addSlotToContainer(new Slot(chestInventory, (i * 6) + j, 35 + (j * 18), 18 + (i * 18)));
            }
        }

        for (i = 0; i < 3; ++i)
        {
            for (j = 0; j < 9; ++j)
            {
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 85 + i * 18));
            }
        }

        for (i = 0; i < 9; ++i)
        {
            this.addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18, 143));
        }
    }

    public IInventory getChestInventory(){
        return(this.meteorInv);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.meteorInv.isUsableByPlayer(playerIn);
    }

    /**
     * Take a stack from the specified inventory slot.
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack emptyStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack stack = slot.getStack();
            emptyStack = stack.copy();

            if (index < 3 * 6)
            {
                if (!this.mergeItemStack(stack, 3 * 6, this.inventorySlots.size(), true))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.mergeItemStack(stack, 0, 3 * 6, false))
            {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty())
            {
                slot.putStack(ItemStack.EMPTY);
            }
            else
            {
                slot.onSlotChanged();
            }
        }

        return emptyStack;
    }

    /**
     * Called when the container is closed.
     */
    @Override
    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);
        this.meteorInv.closeInventory(playerIn);
    }
}
