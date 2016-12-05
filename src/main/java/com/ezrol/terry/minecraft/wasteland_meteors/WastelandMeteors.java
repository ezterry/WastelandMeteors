package com.ezrol.terry.minecraft.wasteland_meteors;

import com.ezrol.terry.minecraft.wasteland_meteors.blocks.*;
import com.ezrol.terry.minecraft.wasteland_meteors.gen.SurfaceMeteors;
import com.ezrol.terry.minecraft.wasteland_meteors.gen.UndergroundMeteors;
import com.ezrol.terry.minecraft.wastelands.EzWastelands;
import com.ezrol.terry.minecraft.wastelands.api.RegionCore;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPurpurSlab;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Level;

import java.io.File;

@Mod(modid = WastelandMeteors.MODID,
        name = "Wasteland  Meteors",
        version = WastelandMeteors.VERSION,
        acceptedMinecraftVersions = "[1.11]",
        dependencies = "required-after:ezwastelands@[1.11-1.2,)")
public class WastelandMeteors
{
    public static final String MODID = "wasteland_meteors";
    public static final String VERSION = "${version}";
    public static Block meteorBlock;
    public static BlockSlab meteorBlockHalf;
    public static BlockSlab meteorBlockFull;
    public static BlockStairs meteorBlockStairs;
    private static File jsonconfig;

    /**
     * Transform the "recommended config" to a .json for our actual configuration
     *
     * @param config - input config from FMLPreInitializationEvent
     * @return - the json file we are working with
     */
    private static File TransformToJsonFile(File config){
        String path = config.getPath();

        if(path.endsWith(".cfg")){
            path=path.substring(0,path.length()-4);
        }
        return(new File(path + ".json"));
    }
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ItemSlab meteorSlabItem;
        ItemBlock meteorBlockItem;
        ItemBlock meteorStairsItem;

        //json config file
        jsonconfig = TransformToJsonFile(event.getSuggestedConfigurationFile());
        FMLLog.log("wasteland_meteors", Level.INFO,
                "Block Customization Json: " + jsonconfig.getPath());

        //register meteor_block
        meteorBlock = new MeteorBlock(Material.ROCK);
        GameRegistry.register(meteorBlock);
        meteorBlockItem = new ItemBlock(meteorBlock);
        meteorBlockItem.setRegistryName(meteorBlock.getRegistryName());
        meteorBlockItem.setUnlocalizedName(meteorBlockItem.getRegistryName().toString());
        //System.out.println(meteorBlockItem.getRegistryName().toString());
        GameRegistry.register(meteorBlockItem);

        //register meteor_slab
        meteorBlockHalf = new MeteorBlockSlab.Half(Material.ROCK);
        meteorBlockFull = new MeteorBlockSlab.Double(Material.ROCK);
        GameRegistry.register(meteorBlockHalf);
        GameRegistry.register(meteorBlockFull);
        meteorSlabItem = new ItemSlab(meteorBlockHalf,meteorBlockHalf,meteorBlockFull);
        meteorSlabItem.setRegistryName(meteorBlockHalf.getRegistryName());
        meteorSlabItem.setUnlocalizedName(meteorSlabItem.getRegistryName().toString());
        //System.out.println(meteorSlab.getRegistryName().toString());
        GameRegistry.register(meteorSlabItem);

        //register meteor_stairs
        meteorBlockStairs = new MeteorStairs(meteorBlock.getDefaultState());
        GameRegistry.register(meteorBlockStairs);
        meteorStairsItem = new ItemBlock(meteorBlockStairs);
        meteorStairsItem.setRegistryName(meteorBlockStairs.getRegistryName());
        meteorStairsItem.setUnlocalizedName(meteorStairsItem.getRegistryName().toString());
        //System.out.println(meteorStairsItem.getRegistryName().toString());
        GameRegistry.register(meteorStairsItem);
    }
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // For debugging print the ezwasteland version
        FMLLog.log("wasteland_meteors", Level.INFO,
                "Found EzWastelands Version: " + EzWastelands.VERSION);

        //Register icon renders
        if (event.getSide() == Side.CLIENT) {
            //set up item renderer
            net.minecraft.client.renderer.RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
            renderItem.getItemModelMesher().register(Item.getItemFromBlock(meteorBlockStairs), 0,
                    new ModelResourceLocation(MODID + ":" + "meteor_stairs","inventory"));
            renderItem.getItemModelMesher().register(Item.getItemFromBlock(meteorBlockHalf), 0,
                    new ModelResourceLocation(MODID + ":" + "meteor_slab","inventory"));
            renderItem.getItemModelMesher().register(Item.getItemFromBlock(meteorBlock), 0,
                    new ModelResourceLocation(MODID + ":" + "meteor_block","inventory"));

        }

        //Register slab recipe
        GameRegistry.addRecipe(new ItemStack(Item.getItemFromBlock(meteorBlockHalf),6),
                "bbb",'b',Item.getItemFromBlock(meteorBlock));
        //Register stair recipe
        GameRegistry.addRecipe(new ItemStack(Item.getItemFromBlock(meteorBlockStairs),4),
                "b  ","bb ","bbb",'b',Item.getItemFromBlock(meteorBlock));

        //process the configuration
        ConfigurationReader config = new ConfigurationReader(jsonconfig);

        //init/register terrain generators
        new SurfaceMeteors(config);
        new UndergroundMeteors(config);

        //register our custom presets
        RegionCore.registerPreset(new ResourceLocation(MODID, "presets/list.txt"));
    }
}
