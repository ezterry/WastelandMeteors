package com.ezrol.terry.minecraft.wasteland_meteors.blocks;

import com.ezrol.terry.minecraft.wasteland_meteors.WastelandMeteors;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;

/**
 * Created by ezterry on 12/1/16.
 */
public class MeteorStairs extends BlockStairs{
    public MeteorStairs(IBlockState parentblock) {
        super(parentblock);
        this.setHardness((float) 2.5);
        this.setSoundType(SoundType.STONE);
        this.setResistance(12.0F);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        this.setHarvestLevel("pickaxe", 2);
        this.setRegistryName("meteor_stairs");
        this.setUnlocalizedName(this.getRegistryName().toString());
    }
}
