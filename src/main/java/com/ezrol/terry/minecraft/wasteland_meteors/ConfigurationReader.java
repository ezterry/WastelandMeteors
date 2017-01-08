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

import com.google.gson.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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
 * Class to read in the json configuration, and if it is missing to generate it
 * Created by ezterry on 11/30/16.
 */
public class ConfigurationReader {
    @SuppressWarnings("CanBeFinal")
    private ArrayList<BlockEntry> surfaceBlocks = new ArrayList<>();
    private long totalSurfaceWeight = 0;
    @SuppressWarnings("CanBeFinal")
    private ArrayList<BlockEntry> undergroundBlocks = new ArrayList<>();
    private long totalUndergroundWeight = 0;

    /**
     * BlockEntry class for storing the current block state/nbt combo
     */
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

    /**
     * Configuration Reader Constructor for loading a configuration json
     * @param jsonpath path to the configuration json
     */
    @SuppressWarnings("WeakerAccess")
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

    /**
     * Helper funciton to create block entry (without nbt data)
     *
     * @param block name of the block
     * @param meta meta data (numeric or block state string)
     * @param weight odds this block will be picked
     * @return json object of the block entry
     */
    private static JsonObject blockentry(String block, String meta, int weight){
        JsonObject r = new JsonObject();
        r.add("block", new JsonPrimitive(block));
        r.add("meta", new JsonPrimitive(meta));
        r.add("weight", new JsonPrimitive(weight));
        return(r);
    }

    /**
     * Helper funciton to create block entry (with nbt data)
     *
     * @param block name of the block
     * @param meta meta data (numeric or block state string)
     * @param nbt a string of the nbt data to be included with the tile entity
     * @param weight odds this block will be picked
     * @return json object of the block entry
     */
    private static JsonObject blockentry(String block, String meta,String nbt, int weight){
        JsonObject r = new JsonObject();
        r.add("block", new JsonPrimitive(block));
        r.add("meta", new JsonPrimitive(meta));
        r.add("nbt", new JsonPrimitive(nbt));
        r.add("weight", new JsonPrimitive(weight));
        return(r);
    }

    /**
     * Create the default json file
     *
     * @param jsonpath path to the json config file
     */
    private void WriteDefaultJson(File jsonpath){
        Gson jsonbuilder = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        JsonObject root = new JsonObject();
        JsonArray  surface = new JsonArray();
        JsonArray  underground = new JsonArray();

        surface.add(blockentry("minecraft:cobblestone","",80));
        surface.add(blockentry("wasteland_meteors:meteor_block","",80));
        surface.add(blockentry("minecraft:stone","variant=andesite",60));
        surface.add(blockentry("minecraft:stone","variant=granite",60));
        surface.add(blockentry("minecraft:stone","variant=diorite",60));
        surface.add(blockentry("minecraft:coal_ore","",40));
        surface.add(blockentry("minecraft:iron_ore","",36));
        surface.add(blockentry("minecraft:gravel","",20));
        surface.add(blockentry("minecraft:sand","",20));
        surface.add(blockentry("minecraft:ice","",20));
        surface.add(blockentry("minecraft:gold_ore","",12));
        surface.add(blockentry("minecraft:lapis_ore","",10));
        surface.add(blockentry("minecraft:packed_ice","",6));
        surface.add(blockentry("minecraft:diamond_ore","",4));
        surface.add(blockentry("minecraft:emerald_ore","",2));
        surface.add(blockentry("wasteland_meteors:meteor_chest","facing=east",
                "{LootTable:\"wasteland_meteors:chests/minecraft_plants\"}",1));
        surface.add(blockentry("wasteland_meteors:meteor_chest","facing=west",
                "{LootTable:\"wasteland_meteors:chests/minecraft_plants\"}",1));
        surface.add(blockentry("wasteland_meteors:meteor_chest","facing=north",
                "{LootTable:\"wasteland_meteors:chests/minecraft_plants\"}",1));
        surface.add(blockentry("wasteland_meteors:meteor_chest","facing=south",
                "{LootTable:\"wasteland_meteors:chests/minecraft_plants\"}",1));

        underground.add(blockentry("minecraft:coal_ore","",1440));
        underground.add(blockentry("minecraft:iron_ore","",1440));
        underground.add(blockentry("minecraft:dirt","",800));
        underground.add(blockentry("minecraft:cobblestone","",1000));
        underground.add(blockentry("wasteland_meteors:meteor_block","",1000));
        underground.add(blockentry("minecraft:gravel","",800));
        underground.add(blockentry("minecraft:sand","",800));
        underground.add(blockentry("minecraft:gold_ore","",480));
        underground.add(blockentry("minecraft:lapis_ore","",400));
        underground.add(blockentry("minecraft:obsidian","",360));
        underground.add(blockentry("minecraft:diamond_ore","",320));
        underground.add(blockentry("minecraft:emerald_ore","",160));
        underground.add(blockentry("wasteland_meteors:meteor_chest","facing=east",
                "{LootTable:\"wasteland_meteors:chests/minecraft_plants\"}",8));
        underground.add(blockentry("wasteland_meteors:meteor_chest","facing=west",
                "{LootTable:\"wasteland_meteors:chests/minecraft_plants\"}",8));
        underground.add(blockentry("wasteland_meteors:meteor_chest","facing=north",
                "{LootTable:\"wasteland_meteors:chests/minecraft_plants\"}",8));
        underground.add(blockentry("wasteland_meteors:meteor_chest","facing=south",
                "{LootTable:\"wasteland_meteors:chests/minecraft_plants\"}",8));
        underground.add(blockentry("minecraft:mob_spawner","",
                "{SpawnData:{id:\"minecraft:guardian\"}}",2));
        underground.add(blockentry("minecraft:mob_spawner","",
                "{SpawnData:{id:\"minecraft:skeleton\"}}",2));
        underground.add(blockentry("minecraft:mob_spawner","",
                "{SpawnData:{id:\"minecraft:zombie\"}}",4));
        underground.add(blockentry("minecraft:mob_spawner","",
                "{SpawnData:{id:\"minecraft:cave_spider\"}}",4));
        underground.add(blockentry("minecraft:mob_spawner","",
                "{SpawnData:{id:\"minecraft:witch\"}}",2));


        root.add("surface",surface);
        root.add("underground",underground);

        try {
            FileWriter f = new FileWriter(jsonpath);

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
                    entry=null;
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
