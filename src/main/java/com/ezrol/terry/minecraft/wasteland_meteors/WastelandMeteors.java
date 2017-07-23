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

package com.ezrol.terry.minecraft.wasteland_meteors;

import com.ezrol.terry.minecraft.wasteland_meteors.blocks.*;
import com.ezrol.terry.minecraft.wasteland_meteors.gen.SurfaceMeteors;
import com.ezrol.terry.minecraft.wasteland_meteors.gen.UndergroundMeteors;
import com.ezrol.terry.minecraft.wasteland_meteors.gui.MeteorChestGui;
import com.ezrol.terry.minecraft.wasteland_meteors.gui.TileMeteorChestRenderer;
import com.ezrol.terry.minecraft.wasteland_meteors.inventory.ContainerMeteorChest;
import com.ezrol.terry.minecraft.wastelands.api.RegionCore;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSlab;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * The wasteland meteors mod (to be used with ezwastelands 1.11-1.2 and above)
 */
@Mod(modid = WastelandMeteors.MODID,
        name = "Wasteland  Meteors",
        version = WastelandMeteors.VERSION,
        acceptedMinecraftVersions = "[1.12]",
        dependencies = "required-after:ezwastelands@[1.12-1.3,)")
@SuppressWarnings({"WeakerAccess", "unused"})
public class WastelandMeteors implements IGuiHandler {
    public static final String MODID = "wasteland_meteors";
    public static final String VERSION = "${version}";
    public static Block meteorBlock;
    public static BlockSlab meteorBlockHalf;
    public static BlockSlab meteorBlockFull;
    public static BlockStairs meteorBlockStairs;
    public static BlockChest meteorChest;
    public static WastelandMeteors instance;
    private static File jsonconfig;
    private boolean isServer;
    public static Logger modLog;

    /**
     * Transform the "recommended config" to a .json for our actual configuration
     *
     * @param config - input config from FMLPreInitializationEvent
     * @return - the json file we are working with
     */
    private static File TransformToJsonFile(File config) {
        String path = config.getPath();

        if (path.endsWith(".cfg")) {
            path = path.substring(0, path.length() - 4);
        }
        return (new File(path + ".json"));
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ItemSlab meteorSlabItem;
        ItemBlock meteorBlockItem;
        ItemBlock meteorStairsItem;
        ItemBlock meteorChestItem;

        modLog=event.getModLog();

        WastelandMeteors.instance = this;
        //json config file
        jsonconfig = TransformToJsonFile(event.getSuggestedConfigurationFile());
        modLog.log(Level.INFO,"Block Customization Json: " + jsonconfig.getPath());

        //register meteor_block
        meteorBlock = new MeteorBlock(Material.ROCK);
        ForgeRegistries.BLOCKS.register(meteorBlock);
        meteorBlockItem = new ItemBlock(meteorBlock);
        //noinspection ConstantConditions
        meteorBlockItem.setRegistryName(meteorBlock.getRegistryName());
        //noinspection ConstantConditions
        meteorBlockItem.setUnlocalizedName(meteorBlockItem.getRegistryName().toString());
        //System.out.println(meteorBlockItem.getRegistryName().toString());
        ForgeRegistries.ITEMS.register(meteorBlockItem);

        //register meteor_slab
        meteorBlockHalf = new MeteorBlockSlab.Half(Material.ROCK);
        meteorBlockFull = new MeteorBlockSlab.Double(Material.ROCK);
        ForgeRegistries.BLOCKS.register(meteorBlockHalf);
        ForgeRegistries.BLOCKS.register(meteorBlockFull);
        meteorSlabItem = new ItemSlab(meteorBlockHalf, meteorBlockHalf, meteorBlockFull);
        //noinspection ConstantConditions
        meteorSlabItem.setRegistryName(meteorBlockHalf.getRegistryName());
        //noinspection ConstantConditions
        meteorSlabItem.setUnlocalizedName(meteorSlabItem.getRegistryName().toString());
        //System.out.println(meteorSlab.getRegistryName().toString());
        ForgeRegistries.ITEMS.register(meteorSlabItem);

        //register meteor_stairs
        meteorBlockStairs = new MeteorStairs(meteorBlock.getDefaultState());
        ForgeRegistries.BLOCKS.register(meteorBlockStairs);
        meteorStairsItem = new ItemBlock(meteorBlockStairs);
        //noinspection ConstantConditions
        meteorStairsItem.setRegistryName(meteorBlockStairs.getRegistryName());
        //noinspection ConstantConditions
        meteorStairsItem.setUnlocalizedName(meteorStairsItem.getRegistryName().toString());
        //System.out.println(meteorStairsItem.getRegistryName().toString());
        ForgeRegistries.ITEMS.register(meteorStairsItem);

        //register meteor_chest
        meteorChest = new MeteorChest();
        ForgeRegistries.BLOCKS.register(meteorChest);
        meteorChestItem = new ItemBlock(meteorChest);
        //noinspection ConstantConditions
        meteorChestItem.setRegistryName(meteorChest.getRegistryName());
        //noinspection ConstantConditions
        meteorChestItem.setUnlocalizedName(meteorChestItem.getRegistryName().toString());
        //System.out.println(meteorChestItem.getRegistryName().toString());
        ForgeRegistries.ITEMS.register(meteorChestItem);
        GameRegistry.registerTileEntity(TileMeteorChest.class, "tile_meteor_chest");

        //register gui handler
        if (event.getSide() == Side.CLIENT) {
            this.isServer = false;
            this.registerRenderer();
        } else {
            this.isServer = true;
        }
        NetworkRegistry.INSTANCE.registerGuiHandler(MODID, this);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        //Register icon renders
        if (event.getSide() == Side.CLIENT) {
            //set up item renderer
            net.minecraft.client.renderer.RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
            renderItem.getItemModelMesher().register(Item.getItemFromBlock(meteorBlockStairs), 0,
                    new ModelResourceLocation(MODID + ":" + "meteor_stairs", "inventory"));
            renderItem.getItemModelMesher().register(Item.getItemFromBlock(meteorBlockHalf), 0,
                    new ModelResourceLocation(MODID + ":" + "meteor_slab", "inventory"));
            renderItem.getItemModelMesher().register(Item.getItemFromBlock(meteorBlock), 0,
                    new ModelResourceLocation(MODID + ":" + "meteor_block", "inventory"));
            renderItem.getItemModelMesher().register(Item.getItemFromBlock(meteorChest), 0,
                    new ModelResourceLocation(MODID + ":" + "meteor_chest", "inventory"));
        }

        //register our custom presets
        RegionCore.registerPreset(new ResourceLocation(MODID, "presets/list.txt"));
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        //process the configuration
        ConfigurationReader config = new ConfigurationReader(jsonconfig);

        //init/register terrain generators
        //these auto register with ezwastelands
        new SurfaceMeteors(config);
        new UndergroundMeteors(config);
    }

    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        if (te != null && ID == 0 && te instanceof TileMeteorChest) {
            return new ContainerMeteorChest(player.inventory, (TileMeteorChest) te, player);
        } else {
            return null;
        }
    }

    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        if (!isServer && te != null && ID == 0 && te instanceof TileMeteorChest) {
            return new MeteorChestGui(player, (TileMeteorChest) te);
        } else {
            return null;
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerRenderer() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileMeteorChest.class, new TileMeteorChestRenderer());
    }
}
