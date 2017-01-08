package com.ezrol.terry.minecraft.wasteland_meteors.gen;

import com.ezrol.terry.minecraft.wasteland_meteors.ConfigurationReader;
import com.ezrol.terry.minecraft.wasteland_meteors.WastelandMeteors;
import com.ezrol.terry.minecraft.wastelands.api.IRegionElement;
import com.ezrol.terry.minecraft.wastelands.api.Param;
import com.ezrol.terry.minecraft.wastelands.api.RegionCore;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.*;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Generate meteors on the surface of the wastelands
 *
 * Created by ezterry on 12/1/16.
 */
public class UndergroundMeteors implements IRegionElement {
    private ConfigurationReader ModConfig;
    private World world=null;

    private class meteorLocation {
        int x;
        int z;
        int y;
        int scale;
    }


    public UndergroundMeteors(ConfigurationReader c) {
        RegionCore.register(this, false);
        ModConfig=c;
    }

    @Override
    public int addElementHeight(int currentoffset, int x, int z, RegionCore core, List<Object> elements){
        float amplitude;
        float dist;
        float xf;
        float zf;
        float adjust;

        for(Object o : elements){
            meteorLocation m = (meteorLocation)o;

            amplitude = (3f * (m.y/50f))+0.5f;
            xf = (float)m.x-x;
            zf = (float)m.z-z;
            dist = (float) Math.sqrt((double)(xf*xf + zf*zf));
            if(dist < 64) {
                adjust = (float) (Math.sin(dist / (((m.scale*2)/Math.PI))) * amplitude);
                adjust = ((dist - 64)/64) * adjust;
                currentoffset = currentoffset + (int) adjust;
            }
        }
        return currentoffset;
    }

    @Override
    public String getElementName() {
        return("undergroundmeteor");
    }

    @Override
    public List<Param> getParamTemplate() {
        List<Param> lst = new ArrayList<>();
        lst.add(new Param.BooleanParam("enable",
                "config.wasteland_meteors.undergroundmeteor.enable.help",
                true));
        lst.add(new Param.FloatParam("frequency",
                "config.wasteland_meteors.undergroundmeteor.frequency.help",
                0.6f,0.0f,2.0f));
        lst.add(new Param.IntegerParam("scale",
                "config.wasteland_meteors.undergroundmeteor.scale.help",
                5,0,8));
        return lst;
    }

    @Override
    public List<Object> calcElements(Random random, int x, int z, List<Param> list, RegionCore regionCore) {
        List<Object> lst = new ArrayList<>();
        if(((Param.BooleanParam)Param.lookUp(list, "enable")).get()){
            //we have surface meteors enabled
            int count=0;
            int scale = ((Param.IntegerParam)Param.lookUp(list, "scale")).get();
            float f = ((Param.FloatParam)Param.lookUp(list, "frequency")).get();

            int try1 = random.nextInt(3);
            int try2 = random.nextInt(3);
            float roll = random.nextFloat();

            if(f >= 1.0){
                if(try1 == 1) {
                    count++;
                } else if(try1 > 1){
                    count += 2;
                }
                f-=1;
            }
            if(try2 == 1){
                if(f > roll){
                    count++;
                }
            } else if(try2 > 1){
                if(f > roll){
                    count+=2;
                }
            }
            //count is the number of meteors to generate

            for(int i=0;i < count; i++){
                meteorLocation obj = new meteorLocation();
                obj.x = random.nextInt(64) + (x<<6);
                obj.z = random.nextInt(64) + (z<<6);
                obj.y = random.nextInt(52-scale);
                obj.scale = random.nextInt(scale) + 3;
                lst.add(obj);
            }
        }
        return lst;
    }

    private Random fillRNG(int x,int z,long seed){
        Random r;
        long localSeed;

        localSeed = ((long)z << 33) + (x * 31);
        localSeed = localSeed ^ seed;
        localSeed += 6247;

        r = new Random(localSeed);
        r.nextInt();
        r.nextInt();
        return (r);
    }

    private void findWorld(long seed){
        World check;
        if(world != null && world.getSeed() == seed){
            return;
        }
        check = DimensionManager.getWorld(0);
        if(check.getSeed() == seed){
            world = check;
        }
        if(world != null && world.getSeed() == seed){
            return;
        }
        for(int id : DimensionManager.getIDs()){
            check = DimensionManager.getWorld(id);
            if(check.getSeed() == seed){
                world = check;

            }
        }
    }

    @Override
    public void postFill(ChunkPrimer chunkprimer, int height, int x, int z, long worldSeed, List<Param> p, RegionCore core) {
        int scale = ((Param.IntegerParam)Param.lookUp(p, "scale")).get();
        int distx;
        int disty;
        int distz;
        float f;
        IBlockState meteor = WastelandMeteors.meteorBlock.getDefaultState();

        scale+=3;
        findWorld(worldSeed);

        if(world == null){
            return; //can't find world thus can't generate items
        }

        for(Object o : core.getRegionElements(x,z,this, world)){
            meteorLocation m = ((meteorLocation)o);
            distx=abs(x-m.x);
            distz=abs(z-m.z);
            if(distx <= scale && distz <= scale){
                int h=core.addElementHeight(52,m.x,m.z,worldSeed);
                if((m.y+(m.scale*2)) > h){
                    m.y= h - (m.scale*2);
                }
                if(m.y==0){
                    //we didn't get the update
                    continue;
                }
                for(int y=m.y-scale;y<m.y+scale;y++){
                    if(y>=256 || y<0){
                        continue;
                    }
                    disty=abs(y-m.y);
                    f=(float) Math.sqrt((distx * distx) + (disty * disty) + (distz * distz));
                    if(f < m.scale+0.5){
                        //don't replace bedrock
                        if(chunkprimer.getBlockState(x & 0x0F, y, z & 0x0F).getBlock() != Blocks.BEDROCK) {
                            chunkprimer.setBlockState(x & 0x0F, y, z & 0x0F, meteor);
                        }
                    }
                }

            }
        }
    }

    static private int sq(int i){
        return(i*i);
    }

    @Override
    public void additionalTriggers(String event, IChunkGenerator iChunkGenerator, ChunkPos chunkPos, World world, boolean b, ChunkPrimer chunkPrimer, List<Param> p, RegionCore core) {
        if(event.equals("populate")){
            int scale = ((Param.IntegerParam)Param.lookUp(p, "scale")).get();
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            Random rand=fillRNG(chunkPos.getXEnd()-8,chunkPos.getZEnd()-8,world.getSeed());
            float f;

            scale+=3;

            for(Object o : core.getRegionElements(chunkPos.getXEnd()-8,chunkPos.getZEnd()-8,this, world)) {
                UndergroundMeteors.meteorLocation m = ((UndergroundMeteors.meteorLocation) o);

                int minX = max(m.x-scale,chunkPos.getXStart());
                int maxX = min(m.x+scale,chunkPos.getXEnd());

                int minZ = max(m.z-scale,chunkPos.getZStart());
                int maxZ = min(m.z+scale,chunkPos.getZEnd());

                if(minX > maxX || minZ > maxZ){
                    continue;
                }
                //loop over the meteor bounding box
                int h=core.addElementHeight(52,m.x,m.z,world.getSeed());
                if((m.y+(m.scale*2)) > h){
                    m.y= h - (m.scale*2);
                }
                if(m.y==0){
                    //we didn't get the update
                    continue;
                }
                for(int x=minX;x<=maxX;x++){
                    for(int y=max(m.y-scale,1);y<min(m.y+scale,254);y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            f=(float) Math.sqrt(sq(x-m.x) + sq(y-m.y) + sq(z-m.z));
                            if(f < m.scale-0.5){
                                pos.setPos(x,y,z);
                                ConfigurationReader.BlockEntry block = ModConfig.getUndergroundBlock(rand);
                                if(world.getBlockState(pos).getBlock() != WastelandMeteors.meteorBlock){
                                    continue;
                                }
                                world.setBlockState(pos.toImmutable(),block.getBlockState(),2);
                                NBTTagCompound nbt = block.getNbtData();
                                if(nbt != null){
                                    TileEntity te = world.getTileEntity(pos);
                                    if(te != null) {
                                        nbt.setInteger("x", pos.getX());
                                        nbt.setInteger("y", pos.getY());
                                        nbt.setInteger("z", pos.getZ());
                                        te.readFromNBT(nbt);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public BlockPos getStrongholdGen(World world, boolean b, BlockPos blockPos, List<Param> list, RegionCore regionCore) {
        return null;
    }

    @Override
    public BlockPos getVillageGen(World world, boolean b, BlockPos blockPos, List<Param> list, RegionCore regionCore) {
        return null;
    }
}
