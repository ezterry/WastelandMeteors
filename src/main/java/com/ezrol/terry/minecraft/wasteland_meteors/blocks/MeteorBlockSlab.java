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
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Random;

public abstract class MeteorBlockSlab extends BlockSlab {
    @SuppressWarnings("WeakerAccess")
    public static final PropertyEnum<MeteorBlockSlab.Variant> VARIANT = PropertyEnum.create("variant", MeteorBlockSlab.Variant.class);

    @SuppressWarnings("WeakerAccess")
    public MeteorBlockSlab(Material m) {
        super(m);
        IBlockState iblockstate = this.blockState.getBaseState();

        if (!this.isDouble()) {
            iblockstate = iblockstate.withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
            this.setRegistryName("meteor_slab");
        } else {
            this.setRegistryName("meteor_double_slab");
        }

        this.setHardness((float) 2.5);
        this.setSoundType(SoundType.STONE);
        this.setResistance(12.0F);
        this.setHarvestLevel("pickaxe", 2);
        this.setUnlocalizedName(this.getRegistryName().toString());
        this.setDefaultState(iblockstate.withProperty(VARIANT, MeteorBlockSlab.Variant.DEFAULT));
        if (!this.isDouble()) {
            this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        }
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    @SuppressWarnings("NullableProblems")
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(WastelandMeteors.meteorBlockHalf);
    }

    @SuppressWarnings({"deprecation", "NullableProblems"})
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(WastelandMeteors.meteorBlockHalf);
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    @SuppressWarnings("deprecation")
    @Nonnull
    public IBlockState getStateFromMeta(int meta) {
        IBlockState iblockstate = this.getDefaultState().withProperty(VARIANT, MeteorBlockSlab.Variant.DEFAULT);

        if (!this.isDouble()) {
            iblockstate = iblockstate.withProperty(HALF, (meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
        }

        return iblockstate;
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state) {
        int i = 0;

        if (!this.isDouble() && state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP) {
            i |= 8;
        }

        return i;
    }

    @Nonnull
    protected BlockStateContainer createBlockState() {
        return this.isDouble() ? new BlockStateContainer(this, VARIANT) : new BlockStateContainer(this, HALF, VARIANT);
    }

    /**
     * Returns the slab block name with the type associated with it
     */
    @Nonnull
    public String getUnlocalizedName(int meta) {
        return super.getUnlocalizedName();
    }

    @Nonnull
    public IProperty<?> getVariantProperty() {
        return VARIANT;
    }

    @Nonnull
    public Comparable<?> getTypeForItem(@SuppressWarnings("NullableProblems") ItemStack stack) {
        return MeteorBlockSlab.Variant.DEFAULT;
    }

    public enum Variant implements IStringSerializable {
        DEFAULT;

        public String getName() {
            return "default";
        }
    }

    public static class Double extends MeteorBlockSlab {
        public Double(Material m) {
            super(m);
        }

        public boolean isDouble() {
            return true;
        }
    }

    public static class Half extends MeteorBlockSlab {
        public Half(Material m) {
            super(m);
        }

        public boolean isDouble() {
            return false;
        }
    }
}