package com.ezrol.terry.minecraft.wasteland_meteors.blocks;

import java.util.Random;

import com.ezrol.terry.minecraft.wasteland_meteors.WastelandMeteors;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class MeteorBlockSlab extends BlockSlab
{
    public static final PropertyEnum<MeteorBlockSlab.Variant> VARIANT = PropertyEnum.create("variant", MeteorBlockSlab.Variant.class);

    public MeteorBlockSlab(Material m)
    {
        super(m);
        IBlockState iblockstate = this.blockState.getBaseState();

        if (!this.isDouble())
        {
            iblockstate = iblockstate.withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
            this.setRegistryName("meteor_slab");
        }
        else{
            this.setRegistryName("meteor_double_slab");
        }

        this.setHardness((float) 2.5);
        this.setSoundType(SoundType.STONE);
        this.setResistance(12.0F);
        this.setHarvestLevel("pickaxe", 2);
        this.setUnlocalizedName(this.getRegistryName().toString());
        this.setDefaultState(iblockstate.withProperty(VARIANT, MeteorBlockSlab.Variant.DEFAULT));
        if(!this.isDouble()) {
            this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        }
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return Item.getItemFromBlock(WastelandMeteors.meteorBlockHalf);
    }

    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state)
    {
        return new ItemStack(WastelandMeteors.meteorBlockHalf);
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta)
    {
        IBlockState iblockstate = this.getDefaultState().withProperty(VARIANT, MeteorBlockSlab.Variant.DEFAULT);

        if (!this.isDouble())
        {
            iblockstate = iblockstate.withProperty(HALF, (meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
        }

        return iblockstate;
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        int i = 0;

        if (!this.isDouble() && state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP)
        {
            i |= 8;
        }

        return i;
    }

    protected BlockStateContainer createBlockState()
    {
        return this.isDouble() ? new BlockStateContainer(this, VARIANT): new BlockStateContainer(this, HALF, VARIANT);
    }

    /**
     * Returns the slab block name with the type associated with it
     */
    public String getUnlocalizedName(int meta)
    {
        return super.getUnlocalizedName();
    }

    public IProperty<?> getVariantProperty()
    {
        return VARIANT;
    }

    public Comparable<?> getTypeForItem(ItemStack stack)
    {
        return MeteorBlockSlab.Variant.DEFAULT;
    }

    public static class Double extends MeteorBlockSlab
    {
        public Double(Material m){
            super(m);
        }
        public boolean isDouble()
        {
            return true;
        }
    }

    public static class Half extends MeteorBlockSlab
    {
        public Half(Material m){
            super(m);
        }
        public boolean isDouble()
        {
            return false;
        }
    }

    public enum Variant implements IStringSerializable
    {
        DEFAULT;

        public String getName()
        {
            return "default";
        }
    }
}