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

import com.ezrol.terry.minecraft.wasteland_meteors.WastelandMeteors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The block entry for the meteor_chest
 * <p>
 * Created by ezterry on 12/21/16.
 */
public class MeteorChest extends BlockChest {
    public MeteorChest() {
        super(BlockChest.Type.BASIC);
        this.setRegistryName("meteor_chest");
        //noinspection ConstantConditions
        this.setUnlocalizedName(this.getRegistryName().toString());
        this.setResistance(12.0F);
        this.setHardness((float) 3.0);
        this.setSoundType(SoundType.STONE);
        this.setHarvestLevel("pickaxe", 2);
    }

    @Override
    @Nonnull
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return NOT_CONNECTED_AABB;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        //nop
    }

    @Override
    @Nullable
    @SuppressWarnings("NullableProblems")
    public IBlockState checkForSurroundingChests(World worldIn, BlockPos pos, IBlockState state) {
        return (state);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return (true);
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos p_189540_5_) {
        //nop
    }

    @Override
    public boolean onBlockActivated(World worldIn, @Nonnull BlockPos pos, IBlockState state, @Nonnull EntityPlayer playerIn, EnumHand hand, EnumFacing heldItem, float side, float hitX, float hitY) {
        if (worldIn.isRemote) {
            return true;
        } else {
            ILockableContainer ilockablecontainer = this.getLockableContainer(worldIn, pos);

            if (ilockablecontainer != null) {
                playerIn.openGui(WastelandMeteors.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
                playerIn.addStat(StatList.CHEST_OPENED);
            }

            return true;
        }
    }

    @Override
    @Nullable
    public ILockableContainer getContainer(World world, @Nonnull BlockPos chestPos, boolean p_189418_3_) {
        TileEntity tileentity = world.getTileEntity(chestPos);

        if (!(tileentity instanceof TileMeteorChest)) {
            return null;
        }
        if (!p_189418_3_ && this.isBlocked(world, chestPos)) {
            return null;
        }
        return (TileMeteorChest) tileentity;
    }

    private boolean isBlocked(World worldIn, @Nonnull BlockPos pos) {
        if (worldIn.getBlockState(pos.up()).isSideSolid(worldIn, pos.up(), EnumFacing.DOWN)) {
            return true;
        }
        for (Entity entity : worldIn.getEntitiesWithinAABB(EntityOcelot.class, new AxisAlignedBB((double) pos.getX(), (double) (pos.getY() + 1), (double) pos.getZ(), (double) (pos.getX() + 1), (double) (pos.getY() + 2), (double) (pos.getZ() + 1)))) {
            EntityOcelot entityocelot = (EntityOcelot) entity;

            if (entityocelot.isSitting()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileMeteorChest();
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        EnumFacing enumfacing = EnumFacing.getHorizontal(MathHelper.floor((double) (placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3).getOpposite();
        state = state.withProperty(FACING, enumfacing);

        worldIn.setBlockState(pos, state, 3);
    }
}
