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

package com.ezrol.terry.minecraft.wasteland_meteors.gen;

import com.ezrol.terry.minecraft.wasteland_meteors.ConfigurationReader;
import com.ezrol.terry.minecraft.wasteland_meteors.WastelandMeteors;
import com.ezrol.terry.minecraft.wastelands.api.IRegionElement;
import com.ezrol.terry.minecraft.wastelands.api.Param;
import com.ezrol.terry.minecraft.wastelands.api.RegionCore;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.*;

/**
 * Generate meteors on the surface of the wastelands
 * <p>
 * Created by ezterry on 12/1/16.
 */
public class SurfaceMeteors implements IRegionElement {
    private ConfigurationReader ModConfig;

    public SurfaceMeteors(ConfigurationReader c) {
        RegionCore.register(this, true);
        ModConfig = c;
    }

    static private int sq(int i) {
        return (i * i);
    }

    @Override
    public int addElementHeight(int currentoffset, int x, int z, RegionCore core, List<Object> elements) {
        int offset = currentoffset;
        int localoffset;
        float area_of_inf; //area of influence
        float distx;
        float distz;
        float dist;

        for (Object o : elements) {
            meteorLocation m = ((meteorLocation) o);
            area_of_inf = ((m.scale + 3) * 4);
            distx = x - m.x;
            distz = z - m.z;

            if (distx < 0) {
                switch (m.direction) {
                    case 1:
                    case 2:
                    case 5:
                        distx *= -1.25;
                        break;
                    case 3:
                    case 4:
                    case 6:
                        distx *= -0.75;
                        break;
                    default:
                        distx *= -1;
                        break;
                }
            } else {
                switch (m.direction) {
                    case 1:
                    case 2:
                    case 5:
                        distx *= 0.75;
                        break;
                    case 3:
                    case 4:
                    case 6:
                        distx *= 1.25;
                        break;
                    default:
                        distx *= 1;
                        break;
                }
            }
            if (distz < 0) {
                switch (m.direction) {
                    case 2:
                    case 3:
                    case 7:
                        distz *= -1.25;
                        break;
                    case 1:
                    case 4:
                    case 8:
                        distz *= -0.75;
                        break;
                    default:
                        distz *= -1;
                        break;
                }
            } else {
                switch (m.direction) {
                    case 2:
                    case 3:
                    case 7:
                        distz *= 0.75;
                        break;
                    case 1:
                    case 4:
                    case 8:
                        distz *= 1.25;
                        break;
                    default:
                        distz *= 1;
                        break;
                }
            }

            if (distx <= area_of_inf && distz <= area_of_inf) {
                //we need to consider this point
                dist = (float) Math.sqrt((distx * distx) + (distz * distz));
                dist = (float) pow((double) dist, (double) m.exponent);
                localoffset = currentoffset - ((int) ((m.scale + 3) * 1.75));
                localoffset += (int) dist;
                if (localoffset < offset) {
                    offset = localoffset;
                }
            }
        }
        return offset;
    }

    @Override
    public String getElementName() {
        return ("surfacemeteor");
    }

    @Override
    public List<Param> getParamTemplate() {
        List<Param> lst = new ArrayList<>();
        lst.add(new Param.BooleanParam("enable",
                "config.wasteland_meteors.surfacemeteor.enable.help",
                true));
        lst.add(new Param.FloatParam("frequency",
                "config.wasteland_meteors.surfacemeteor.frequency.help",
                0.8f, 0.0f, 2.0f));
        lst.add(new Param.FloatParam("exponent",
                "config.wasteland_meteors.surfacemeteor.exponent.help",
                0.90f, 0.7f, 1.5f));
        lst.add(new Param.IntegerParam("scale",
                "config.wasteland_meteors.surfacemeteor.scale.help",
                3, 0, 5));
        return lst;
    }

    @Override
    public List<Object> calcElements(Random random, int x, int z, RegionCore core){
        List<Object> lst = new ArrayList<>();
        if (((Param.BooleanParam) core.lookupParam(this, "enable")).get()) {
            //we have surface meteors enabled
            int count = 0;
            int scale = ((Param.IntegerParam) core.lookupParam(this, "scale")).get();
            float f = ((Param.FloatParam) core.lookupParam(this, "frequency")).get();
            float exponent = ((Param.FloatParam) core.lookupParam(this, "exponent")).get();

            int try1 = random.nextInt(3);
            int try2 = random.nextInt(3);
            float roll = random.nextFloat();

            if (f >= 1.0) {
                if (try1 == 1) {
                    count++;
                } else if (try1 > 1) {
                    count += 2;
                }
                f -= 1;
            }
            if (try2 == 1) {
                if (f > roll) {
                    count++;
                }
            } else if (try2 > 1) {
                if (f > roll) {
                    count += 2;
                }
            }
            //count is the number of meteors to generate

            for (int i = 0; i < count; i++) {
                meteorLocation obj = new meteorLocation();
                obj.x = random.nextInt(64) + (x << 6);
                obj.z = random.nextInt(64) + (z << 6);
                obj.scale = random.nextInt(scale) + 3;
                obj.exponent = exponent;
                obj.direction = (short) random.nextInt(9);
                lst.add(obj);
            }
        }
        return lst;
    }

    private Random fillRNG(int x, int z, long seed) {
        Random r;
        long localSeed;

        localSeed = ((long) x << 33) + (z * 31);
        localSeed = localSeed ^ seed;
        localSeed += 5147;

        r = new Random(localSeed);
        r.nextInt();
        r.nextInt();
        return (r);
    }

    @Override
    public void postFill(ChunkPrimer chunkprimer, int height, int x, int z, RegionCore core){
        int scale = ((Param.IntegerParam) core.lookupParam(this, "scale")).get();
        int distx;
        int disty;
        int distz;
        int m_y;
        float f;
        IBlockState meteor = WastelandMeteors.meteorBlock.getDefaultState();

        scale += 3;

        for (Object o : core.getRegionElements(x, z, this)) {
            meteorLocation m = ((meteorLocation) o);
            distx = abs(x - m.x);
            distz = abs(z - m.z);
            if (distx <= scale && distz <= scale) {
                m_y = core.addElementHeight(m.x, m.z);
                if (m_y == 0) {
                    //we didn't get the update
                    continue;
                }
                for (int y = m_y - scale; y < m_y + scale; y++) {
                    if (y >= 256 || y < 0) {
                        continue;
                    }
                    disty = abs(y - m_y);
                    f = (float) Math.sqrt((distx * distx) + (disty * disty) + (distz * distz));
                    if (f < m.scale + 0.5) {
                        //don't replace bedrock
                        if (chunkprimer.getBlockState(x & 0x0F, y, z & 0x0F).getBlock() != Blocks.BEDROCK) {
                            chunkprimer.setBlockState(x & 0x0F, y, z & 0x0F, meteor);
                        }
                    }
                }

            }
        }
    }

    @Override
    public void additionalTriggers(String event, IChunkGenerator iChunkGenerator, ChunkPos chunkPos,ChunkPrimer chunkPrimer, RegionCore core){
        if (event.equals("populate")) {
            int scale = ((Param.IntegerParam) core.lookupParam(this, "scale")).get();
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            Random rand = fillRNG(chunkPos.getXEnd() - 8, chunkPos.getZEnd() - 8, core.getWorld().getSeed());
            float f;

            scale += 3;

            for (Object o : core.getRegionElements(chunkPos.getXEnd() - 8, chunkPos.getZEnd() - 8, this)) {
                meteorLocation m = ((meteorLocation) o);

                int minX = max(m.x - scale, chunkPos.getXStart());
                int maxX = min(m.x + scale, chunkPos.getXEnd());

                int minZ = max(m.z - scale, chunkPos.getZStart());
                int maxZ = min(m.z + scale, chunkPos.getZEnd());

                if (minX > maxX || minZ > maxZ) {
                    continue;
                }
                //loop over the meteor bounding box
                Chunk currentChunk = core.getWorld().getChunkFromChunkCoords(chunkPos.x,chunkPos.z);
                int m_y = core.addElementHeight(m.x, m.z);
                for (int x = minX; x <= maxX; x++) {
                    for (int y = max(m_y - scale, 1); y < min(m_y + scale, 254); y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            f = (float) Math.sqrt(sq(x - m.x) + sq(y - m_y) + sq(z - m.z));
                            if (f < m.scale - 0.5) {
                                pos.setPos(x, y, z);
                                ConfigurationReader.BlockEntry block = ModConfig.getSurfaceBlock(rand);
                                if (currentChunk.getBlockState(pos).getBlock() != WastelandMeteors.meteorBlock) {
                                    continue;
                                }
                                currentChunk.setBlockState(pos.toImmutable(), block.getBlockState());
                                NBTTagCompound nbt = block.getNbtData();
                                if (nbt != null) {
                                    TileEntity te = currentChunk.getTileEntity(pos,Chunk.EnumCreateEntityType.IMMEDIATE);
                                    if (te != null) {
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
    public BlockPos getNearestStructure(String s, BlockPos blockPos, boolean b, RegionCore regionCore) {
        return null;
    }

    @Override
    public boolean isInsideStructure(String s, BlockPos blockPos, RegionCore regionCore) {
        return false;
    }

    @Override
    public List<Biome.SpawnListEntry> getSpawnable(List<Biome.SpawnListEntry> list, EnumCreatureType enumCreatureType, BlockPos blockPos, RegionCore regionCore) {
        return list;
    }

    private class meteorLocation {
        int x;
        int z;
        int scale;
        float exponent;
        short direction;
    }
}
