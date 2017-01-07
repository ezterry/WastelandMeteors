package com.ezrol.terry.minecraft.wasteland_meteors;

import com.google.gson.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.InvalidBlockStateException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.server.CommandSetBlock;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by ezterry on 11/30/16.
 */
public class ConfigurationReader {
    private ArrayList<BlockEntry> surfaceBlocks = new ArrayList<>();
    private long totalSurfaceWeight = 0;
    private ArrayList<BlockEntry> undergroundBlocks = new ArrayList<>();
    private long totalUndergroundWeight = 0;

    public static class BlockEntry{
        private IBlockState blockstate;
        private NBTTagCompound nbt=null;
        private int weight;

        private BlockEntry(Block block, String meta, String nbt, int weight) throws NumberInvalidException, InvalidBlockStateException {
            if(! meta.equals("")) {
                blockstate = CommandSetBlock.convertArgToBlockState(block, meta);
            }
            else{
                blockstate = block.getDefaultState();
            }

            if(! nbt.equals("")){
                try
                {
                    this.nbt = JsonToNBT.getTagFromJson(nbt);
                }
                catch (NBTException nbtexception)
                {
                    FMLLog.log("wasteland_meteors", Level.WARN,
                            "Error reading NBT data: " + nbtexception.toString());
                }
            }
            this.weight = weight;
        }

        public IBlockState getBlockState(){
            return this.blockstate;
        }

        public NBTTagCompound getNbtData(){
            return this.nbt;
        }
    }
    public ConfigurationReader(File jsonpath){
        if(! jsonpath.exists()){
            FMLLog.log("wasteland_meteors", Level.WARN,
                    "JSON Block Configuration file missing, generating default");
            WriteDefaultJson(jsonpath);
        }

        //file exists or we just created it
        JsonObject data;
        try{
            FileReader f = new FileReader(jsonpath);
            data = new Gson().fromJson(f,JsonObject.class);
        }
        catch (IOException e){
            FMLLog.log("wasteland_meteors", Level.ERROR,
                    "Error reading the json file");
            FMLLog.log("wasteland_meteors", Level.ERROR,
                    e.toString());
            data = new JsonObject();
        }

        ReadJsonData(data);
    }

    private static JsonObject blockentry(String block, String meta, int weight){
        JsonObject r = new JsonObject();
        r.add("block", new JsonPrimitive(block));
        r.add("meta", new JsonPrimitive(meta));
        r.add("weight", new JsonPrimitive(weight));
        return(r);
    }
    private static JsonObject blockentry(String block, String meta,String nbt, int weight){
        JsonObject r = new JsonObject();
        r.add("block", new JsonPrimitive(block));
        r.add("meta", new JsonPrimitive(meta));
        r.add("nbt", new JsonPrimitive(nbt));
        r.add("weight", new JsonPrimitive(weight));
        return(r);
    }

    private void WriteDefaultJson(File jsonpath){
        Gson jsonbuilder = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        JsonObject root = new JsonObject();
        JsonArray  surface = new JsonArray();
        JsonArray  underground = new JsonArray();

        surface.add(blockentry("minecraft:cobblestone","",40));
        surface.add(blockentry("wasteland_meteors:meteor_block","",40));
        surface.add(blockentry("minecraft:stone","variant=andesite",30));
        surface.add(blockentry("minecraft:stone","variant=granite",30));
        surface.add(blockentry("minecraft:stone","variant=diorite",30));
        surface.add(blockentry("minecraft:coal_ore","",20));
        surface.add(blockentry("minecraft:iron_ore","",18));
        surface.add(blockentry("minecraft:gravel","",10));
        surface.add(blockentry("minecraft:sand","",10));
        surface.add(blockentry("minecraft:ice","",10));
        surface.add(blockentry("minecraft:gold_ore","",6));
        surface.add(blockentry("minecraft:lapis_ore","",5));
        surface.add(blockentry("minecraft:packed_ice","",3));
        surface.add(blockentry("minecraft:diamond_ore","",2));
        surface.add(blockentry("minecraft:emerald_ore","",1));
        surface.add(blockentry( "minecraft:chest","","{LootTable:\"minecraft:chests/simple_dungeon\"}",40));

        underground.add(blockentry("minecraft:coal_ore","",36));
        underground.add(blockentry("minecraft:iron_ore","",36));
        underground.add(blockentry("minecraft:dirt","",20));
        underground.add(blockentry("minecraft:cobblestone","",20));
        underground.add(blockentry("wasteland_meteors:meteor_block","",20));
        underground.add(blockentry("minecraft:gravel","",20));
        underground.add(blockentry("minecraft:sand","",20));
        underground.add(blockentry("minecraft:gold_ore","",12));
        underground.add(blockentry("minecraft:lapis_ore","",10));
        underground.add(blockentry("minecraft:obsidian","",9));
        underground.add(blockentry("minecraft:diamond_ore","",8));
        underground.add(blockentry("minecraft:emerald_ore","",4));

        root.add("surface",surface);
        root.add("underground",underground);

        FileWriter f=null;
        try {
            f = new FileWriter(jsonpath);

            f.write(jsonbuilder.toJson(root));
            f.close();
        }
        catch (IOException e){
            FMLLog.log("wasteland_meteors", Level.ERROR,
                    "Error writing to json file");
            FMLLog.log("wasteland_meteors", Level.ERROR,
                    e.toString());
        }
    }
    private BlockEntry ReadJsonEntry(JsonObject e){
        String block;
        String meta;
        String nbtjson;
        int weight;
        BlockEntry entry=null;

        if(e.has("block") && e.get("block").isJsonPrimitive()){
            block = e.getAsJsonPrimitive("block").getAsString();
        }
        else{
            FMLLog.log("wasteland_meteors", Level.ERROR,
                    "Invalid block object, requires \"block\" name: " + e.toString());
            return null;
        }

        if(e.has("meta")){
            if(e.get("meta").isJsonPrimitive()){
                meta=e.getAsJsonPrimitive("meta").getAsString();
            }
            else{
                FMLLog.log("wasteland_meteors", Level.ERROR,
                        "Invalid block object, \"meta\" included but not a string: " +
                        e.get("meta").toString());
                return null;
            }
        }
        else{
            meta = ""; //if meta is not provided assume empty string
        }

        if(e.has("nbt")){
            if(e.get("nbt").isJsonPrimitive()){
                nbtjson=e.getAsJsonPrimitive("nbt").getAsString();
            }
            else{
                FMLLog.log("wasteland_meteors", Level.ERROR,
                        "Invalid block object, \"nbt\" included but not a string: " +
                                e.get("nbt").toString());
                return null;
            }
        }
        else{
            nbtjson = ""; //if meta is not provided assume empty string
        }

        if(e.has("weight") && e.get("weight").isJsonPrimitive()){
            if(e.getAsJsonPrimitive("weight").isNumber()){
                weight = e.getAsJsonPrimitive("weight").getAsInt();
            }
            else{
                FMLLog.log("wasteland_meteors", Level.ERROR,
                        "wight needs to be an integer but got: " +
                        e.get("weight").toString());
                return null;
            }
        }
        else{
            FMLLog.log("wasteland_meteors", Level.ERROR,
                    "Invalid block object, requires \"weight\" name: " + e.toString());
            return null;
        }

        ResourceLocation key = new ResourceLocation(block);
        if(!ForgeRegistries.BLOCKS.containsKey(key)){
            FMLLog.log("wasteland_meteors", Level.ERROR,
                    "unable to find block ignoring: " + block);
            return null;
        }
        try{
            entry=new BlockEntry(ForgeRegistries.BLOCKS.getValue(key),meta,nbtjson,weight);
        }
        catch(NumberInvalidException nan){
            FMLLog.log("wasteland_meteors", Level.WARN,
                    "invalid metadata number provided, consider using block states: " + meta);
            FMLLog.log("wasteland_meteors", Level.WARN,
                    "using default block state for " + block);
        }
        catch(InvalidBlockStateException ivblk){
            FMLLog.log("wasteland_meteors", Level.WARN,
                    "invalid block state : " + meta);
            FMLLog.log("wasteland_meteors", Level.WARN,
                    "using default block state for " + block);
        }
        finally {
            if(entry == null){
                try{
                    entry=new BlockEntry(ForgeRegistries.BLOCKS.getValue(key),"",nbtjson,weight);
                }
                catch(Exception fatal){
                    FMLLog.log("wasteland_meteors", Level.ERROR,
                            "unable to fallback to block default state : " + fatal.toString());
                    return null;
                }
            }
        }
        return entry;
    }
    private void ReadJsonData(JsonObject data){
        JsonArray blockArray;
        BlockEntry current;

        //read surface blocks
        if(data.has("surface") && data.get("surface").isJsonArray()){
            //the surface object seems valid
            blockArray=data.getAsJsonArray("surface");
            for(JsonElement e : blockArray){
                if(! e.isJsonObject()){
                    FMLLog.log("wasteland_meteors", Level.ERROR,
                            "Expected block entry object in json got: " + e.toString());
                    continue;
                }
                current=ReadJsonEntry(e.getAsJsonObject());
                if(current != null){
                    totalSurfaceWeight += current.weight;
                    surfaceBlocks.add(current);
                }
            }
        }
        else{
            FMLLog.log("wasteland_meteors", Level.WARN,
                    "No surface meteor block data in JSON config");
        }

        //read underground blocks
        if(data.has("underground") && data.get("underground").isJsonArray()) {
            //the underground object seems valid
            blockArray=data.getAsJsonArray("underground");
            for(JsonElement e : blockArray){
                if(! e.isJsonObject()){
                    FMLLog.log("wasteland_meteors", Level.ERROR,
                            "Expected block entry object in json got: " + e.toString());
                    continue;
                }
                current=ReadJsonEntry(e.getAsJsonObject());
                if(current != null){
                    totalUndergroundWeight += current.weight;
                    undergroundBlocks.add(current);
                }
            }
        }
        else{
            FMLLog.log("wasteland_meteors", Level.WARN,
                    "No underground meteor block data in JSON config");
        }
    }

    public BlockEntry getSurfaceBlock(Random rng){
        long idex = rng.nextLong();
        idex = idex % totalSurfaceWeight;
        for(BlockEntry e : surfaceBlocks){
            if(idex < e.weight){
                return(e);
            }
            idex -= e.weight;
        }
        //no block found, return the meteorBlock
        try {
            return (new BlockEntry(WastelandMeteors.meteorBlock, "", "", 0));
        } catch(Exception e){
            throw(new RuntimeException("Can't make block entry for built in meteor block"));
        }
    }
    public BlockEntry getUndergroundBlock(Random rng){
        long idex = rng.nextLong();
        idex = idex % totalUndergroundWeight;
        for(BlockEntry e : undergroundBlocks){
            if(idex < e.weight){
                return(e);
            }
            idex -= e.weight;
        }
        //no block found, return the meteorBlock
        try {
            return (new BlockEntry(WastelandMeteors.meteorBlock, "", "", 0));
        } catch(Exception e){
            throw(new RuntimeException("Can't make block entry for built in meteor block"));
        }
    }
}
