package com.ezrol.terry.minecraft.wasteland_meteors.gen;

import com.ezrol.terry.minecraft.wasteland_meteors.ConfigurationReader;
import com.ezrol.terry.minecraft.wasteland_meteors.WastelandMeteors;
import com.ezrol.terry.minecraft.wasteland_meteors.WeakCache;
import com.ezrol.terry.minecraft.wastelands.api.IRegionElement;
import com.ezrol.terry.minecraft.wastelands.api.Param;
import com.ezrol.terry.minecraft.wastelands.api.RegionCore;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.WeakHashMap;

import static java.lang.Math.abs;
import static java.lang.Math.pow;

/**
 * Created by ezterry on 12/1/16.
 */
public class SurfaceMeteors implements IRegionElement {
    WeakCache cache;
    ConfigurationReader ModConfig;


    private class meteorLocation {
        int x;
        int y;
        int z;
        int scale;
        float exponent;
    }


    public SurfaceMeteors(ConfigurationReader c) {
        RegionCore.register(this, true);
        cache = new WeakCache(25*8);
        ModConfig=c;
    }

    @Override
    public int addElementHeight(int currentoffset, int x, int z, RegionCore core, List<Object> elements){
        int offset=currentoffset;
        int localoffset;
        int area_of_inf; //area of influence
        int distx;
        int distz;
        float dist;

        for(Object o : elements ){
            meteorLocation m = ((meteorLocation)o);
            area_of_inf = ((m.scale+3)*4);
            distx=abs(x-m.x);
            distz=abs(z-m.z);
            if(distx<= area_of_inf && distz<=area_of_inf){
                //we need to consider this point
                dist = (float) Math.sqrt((distx * distx) + (distz * distz));
                dist = (float) pow((double)dist,(double) m.exponent);
                localoffset = currentoffset - ((int)((m.scale+3)*1.75));
                localoffset += (int)dist;
                if(localoffset < offset){
                    offset=localoffset;
                }
                if(m.x == x && m.z == z){
                    m.y=localoffset;
                }
            }
        }
        return offset;
    }

    @Override
    public String getElementName() {
        return("surfacemeteor");
    }

    @Override
    public List<Param> getParamTemplate() {
        List<Param> lst = new ArrayList<>();
        lst.add(new Param.BooleanParam("enable",
                "config.wasteland_meteors.surfacemeteor.enable.help",
                true));
        lst.add(new Param.FloatParam("frequency",
                "config.wasteland_meteors.surfacemeteor.frequency.help",
                0.8f,0.0f,2.0f));
        lst.add(new Param.FloatParam("exponent",
                "config.wasteland_meteors.surfacemeteor.exponent.help",
                0.90f,0.7f,1.5f));
        lst.add(new Param.IntegerParam("scale",
                "config.wasteland_meteors.surfacemeteor.scale.help",
                1,0,4));
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
            float exponent = ((Param.FloatParam)Param.lookUp(list, "exponent")).get();

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
                obj.y = 0;
                obj.scale = random.nextInt(scale) + 3;
                obj.exponent = exponent;
                lst.add(obj);
                cache.addObject(obj);
            }
        }
        return lst;
    }

    private Random fillRNG(int x,int z,long seed){
        Random r;
        long localSeed;

        localSeed = (x << 33) + (z * 31);
        localSeed = localSeed ^ seed;
        localSeed += 5147;

        r = new Random(localSeed);
        r.nextInt();
        r.nextInt();
        return (r);
    }
    @Override
    public void postFill(ChunkPrimer chunkprimer, int height, int x, int z, long worldSeed, List<Param> p, RegionCore core) {
        int scale = ((Param.IntegerParam)Param.lookUp(p, "scale")).get();
        int distx;
        int disty;
        int distz;
        float f;
        Random rand;
        IBlockState meteor = WastelandMeteors.meteorBlock.getDefaultState();
        IBlockState curblock;

        scale+=3;
        for(Object o : cache.currentList()){
            meteorLocation m = ((meteorLocation)o);
            distx=abs(x-m.x);
            distz=abs(z-m.z);
            if(distx <= scale && distz <= scale){
                rand=fillRNG(x,z,worldSeed);
                if(m.y==0){
                    //force the height lookup (again?)
                    core.addElementHeight(52,m.x,m.z,worldSeed);
                }
                for(int y=m.y-scale;y<m.y+scale;y++){
                    if(y>=256 || y<0){
                        continue;
                    }
                    disty=abs(y-m.y);
                    f=(float) Math.sqrt((distx * distx) + (disty * disty) + (distz * distz));
                    if(f < m.scale+0.5){
                        curblock=meteor;
                        if(f <( m.scale-1.0)){
                            curblock = ModConfig.getSurfaceBlock(rand);
                        }

                        chunkprimer.setBlockState(x & 0x0F, y, z & 0x0F, curblock);
                    }
                }

            }
        }

    }

    @Override
    public void additionalTriggers(String s, IChunkGenerator iChunkGenerator, ChunkPos chunkPos, World world, boolean b, ChunkPrimer chunkPrimer, List<Param> list, RegionCore regionCore) {

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
