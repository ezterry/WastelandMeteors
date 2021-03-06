/*
 * Copyright (c) 2016-2017, Terrence Ezrol (ezterry)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.ezrol.terry.minecraft.wasteland_meteors.blocks;

import com.ezrol.terry.minecraft.wasteland_meteors.inventory.ContainerMeteorChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nonnull;

/**
 * Tile entity for the meteor chest
 * <p>
 * Created by ezterry on 12/12/16.
 */
public class TileMeteorChest extends TileEntityLockableLoot implements ITickable {

    public float lidAngle;
    public float lastLidAngle;
    private NonNullList<ItemStack> chestContents = NonNullList.withSize(18, ItemStack.EMPTY);
    /**
     * The number of players currently using this chest
     */
    private int numPlayersUsing;
    /**
     * Server sync counter (once per 20 ticks)
     */
    private int ticksSinceSync;

    @SuppressWarnings("WeakerAccess")
    public TileMeteorChest() {
        ticksSinceSync = 0;
    }

    @Override
    @Nonnull
    protected NonNullList<ItemStack> getItems() {
        return this.chestContents;
    }

    @Override
    public int getSizeInventory() {
        return 18;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.chestContents) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.chestContents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);

        if (!this.checkLootAndRead(compound)) {
            ItemStackHelper.loadAllItems(compound, this.chestContents);
        }

        if (compound.hasKey("CustomName", 8)) {
            this.customName = compound.getString("CustomName");
        }
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);

        if (!this.checkLootAndWrite(compound)) {
            ItemStackHelper.saveAllItems(compound, this.chestContents);
        }

        if (this.hasCustomName()) {
            compound.setString("CustomName", this.customName);
        }

        return compound;
    }

    /**
     * Maximum statck size per slot
     *
     * @return standard slots (stack to 64)
     */
    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    @Nonnull
    public Container createContainer(@Nonnull InventoryPlayer playerInventory, @Nonnull EntityPlayer playerIn) {
        this.fillWithLoot(playerIn);
        return new ContainerMeteorChest(playerInventory, this, playerIn);
    }

    @Override
    @Nonnull
    public String getGuiID() {
        return "wasteland_meteors:tile_meteorchest";
    }

    /**
     * the custom name of the chest/inventory
     *
     * @return custom name
     */
    @Override
    @Nonnull
    public String getName() {
        return this.hasCustomName() ? this.customName : "wasteland_meteors.container.meteorchest";
    }

    @Override
    public void update() {
        int posX = this.pos.getX();
        int posY = this.pos.getY();
        int posZ = this.pos.getZ();
        ++this.ticksSinceSync;

        if (!this.world.isRemote && this.numPlayersUsing != 0 && (this.ticksSinceSync + posX + posY + posZ) % 200 == 0) {
            this.numPlayersUsing = 0;
            for (EntityPlayer entityplayer : this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB((double) ((float) posX - 5.0F), (double) ((float) posY - 5.0F), (double) ((float) posZ - 5.0F), (double) ((float) (posX + 1) + 5.0F), (double) ((float) (posY + 1) + 5.0F), (double) ((float) (posZ + 1) + 5.0F)))) {
                if (entityplayer.openContainer instanceof ContainerMeteorChest) {
                    IInventory iinventory = ((ContainerMeteorChest) entityplayer.openContainer).getChestInventory();

                    if (iinventory == this) {
                        ++this.numPlayersUsing;
                    }
                }
            }
        }

        if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F) {
            this.world.playSound(null, (double) posX + 0.5D, (double) posY + 0.5D, (double) posZ + 0.5D,
                    SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F,
                    this.world.rand.nextFloat() * 0.1F + 0.9F);
        }

        this.lastLidAngle = this.lidAngle;
        if (this.numPlayersUsing == 0 && this.lidAngle > 0.0F || this.numPlayersUsing > 0 && this.lidAngle < 1.0F) {
            if (this.numPlayersUsing > 0) {
                this.lidAngle += 0.1F;
            } else {
                this.lidAngle -= 0.1F;
            }

            if (this.lidAngle > 1.0F) {
                this.lidAngle = 1.0F;
            }

            if (this.lidAngle < 0.5F && this.lastLidAngle >= 0.5F) {
                this.world.playSound(null, (double) posX + 0.5D, (double) posY + 0.5D, (double) posZ + 0.5D,
                        SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F,
                        this.world.rand.nextFloat() * 0.1F + 0.9F);
            }

            if (this.lidAngle < 0.0F) {
                this.lidAngle = 0.0F;
            }
        }
    }

    @Override
    public boolean receiveClientEvent(int id, int type) {
        if (id == 1) {
            this.numPlayersUsing = type;
            return true;
        } else {
            return super.receiveClientEvent(id, type);
        }
    }

    @Override
    public void openInventory(EntityPlayer player) {
        if (!player.isSpectator()) {
            if (this.numPlayersUsing < 0) {
                this.numPlayersUsing = 0;
            }

            this.numPlayersUsing++;
            this.world.addBlockEvent(this.pos, this.getBlockType(), 1, this.numPlayersUsing);
            this.world.notifyNeighborsOfStateChange(this.pos, this.getBlockType(), false);
        }
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        if (!player.isSpectator() && this.getBlockType() instanceof MeteorChest) {
            this.numPlayersUsing--;
            this.world.addBlockEvent(this.pos, this.getBlockType(), 1, this.numPlayersUsing);
            this.world.notifyNeighborsOfStateChange(this.pos, this.getBlockType(), false);
        }
    }
}
