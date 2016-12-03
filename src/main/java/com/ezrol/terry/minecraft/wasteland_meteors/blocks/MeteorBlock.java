package com.ezrol.terry.minecraft.wasteland_meteors.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class MeteorBlock extends Block {
    public MeteorBlock(Material material) {
        super(material);
        this.setHardness((float) 2.5);
        this.setSoundType(SoundType.STONE);
        this.setResistance(12.0F);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        this.setHarvestLevel("pickaxe", 2);
        this.setRegistryName("meteor_block");
        this.setUnlocalizedName(this.getRegistryName().toString());
    }
}

