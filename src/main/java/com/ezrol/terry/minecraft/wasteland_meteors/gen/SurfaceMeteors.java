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

/**
 * Generate meteors on the surface of the wastelands
 * <p>
 * Created by ezterry on 12/1/16.
 */
public class SurfaceMeteors implements IRegionElement {
    private ConfigurationReader ModConfig;
    private World world = null;

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
    public List<Object> calcElements(Random random, int x, int z, List<Param> list, RegionCore regionCore) {
        List<Object> lst = new ArrayList<>();
        if (((Param.BooleanParam) Param.lookUp(list, "enable")).get()) {
            //we have surface meteors enabled
            int count = 0;
            int scale = ((Param.IntegerParam) Param.lookUp(list, "scale")).get();
            float f = ((Param.FloatParam) Param.lookUp(list, "frequency")).get();
            float exponent = ((Param.FloatParam) Param.lookUp(list, "exponent")).get();

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

    private void findWorld(long seed) {
        World check;
        if (world != null && world.getSeed() == seed) {
            return;
        }
        check = DimensionManager.getWorld(0);
        if (check.getSeed() == seed) {
            world = check;
        }
        if (world != null && world.getSeed() == seed) {
            return;
        }
        for (int id : DimensionManager.getIDs()) {
            check = DimensionManager.getWorld(id);
            if (check.getSeed() == seed) {
                world = check;

            }
        }
    }

    @Override
    public void postFill(ChunkPrimer chunkprimer, int height, int x, int z, long worldSeed, List<Param> p, RegionCore core) {
        int scale = ((Param.IntegerParam) Param.lookUp(p, "scale")).get();
        int distx;
        int disty;
        int distz;
        int m_y;
        float f;
        IBlockState meteor = WastelandMeteors.meteorBlock.getDefaultState();

        scale += 3;
        findWorld(worldSeed);

        if (world == null) {
            return; //can't find world thus can't generate items
        }

        for (Object o : core.getRegionElements(x, z, this, world)) {
            meteorLocation m = ((meteorLocation) o);
            distx = abs(x - m.x);
            distz = abs(z - m.z);
            if (distx <= scale && distz <= scale) {
                m_y = core.addElementHeight(52, m.x, m.z, worldSeed);
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
    public void additionalTriggers(String event, IChunkGenerator iChunkGenerator, ChunkPos chunkPos, World world, boolean b, ChunkPrimer chunkPrimer, List<Param> p, RegionCore core) {
        if (event.equals("populate")) {
            int scale = ((Param.IntegerParam) Param.lookUp(p, "scale")).get();
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            Random rand = fillRNG(chunkPos.getXEnd() - 8, chunkPos.getZEnd() - 8, world.getSeed());
            float f;

            scale += 3;

            for (Object o : core.getRegionElements(chunkPos.getXEnd() - 8, chunkPos.getZEnd() - 8, this, world)) {
                meteorLocation m = ((meteorLocation) o);

                int minX = max(m.x - scale, chunkPos.getXStart());
                int maxX = min(m.x + scale, chunkPos.getXEnd());

                int minZ = max(m.z - scale, chunkPos.getZStart());
                int maxZ = min(m.z + scale, chunkPos.getZEnd());

                if (minX > maxX || minZ > maxZ) {
                    continue;
                }
                //loop over the meteor bounding box
                int m_y = core.addElementHeight(52, m.x, m.z, world.getSeed());
                for (int x = minX; x <= maxX; x++) {
                    for (int y = max(m_y - scale, 1); y < min(m_y + scale, 254); y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            f = (float) Math.sqrt(sq(x - m.x) + sq(y - m_y) + sq(z - m.z));
                            if (f < m.scale - 0.5) {
                                pos.setPos(x, y, z);
                                ConfigurationReader.BlockEntry block = ModConfig.getSurfaceBlock(rand);
                                if (world.getBlockState(pos).getBlock() != WastelandMeteors.meteorBlock) {
                                    continue;
                                }
                                world.setBlockState(pos.toImmutable(), block.getBlockState(), 2);
                                NBTTagCompound nbt = block.getNbtData();
                                if (nbt != null) {
                                    TileEntity te = world.getTileEntity(pos);
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
    public BlockPos getStrongholdGen(World world, boolean b, BlockPos blockPos, List<Param> list, RegionCore regionCore) {
        return null;
    }

    @Override
    public BlockPos getVillageGen(World world, boolean b, BlockPos blockPos, List<Param> list, RegionCore regionCore) {
        return null;
    }

    private class meteorLocation {
        int x;
        int z;
        int scale;
        float exponent;
        short direction;
    }
}
