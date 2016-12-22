package com.ezrol.terry.minecraft.wasteland_meteors.blocks;

import com.ezrol.terry.minecraft.wasteland_meteors.WastelandMeteors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Created by ezterry on 12/21/16.
 */
public class MeteorChest extends BlockChest {
    public MeteorChest(){
        super(BlockChest.Type.BASIC);
        this.setRegistryName("meteor_chest");
        this.setUnlocalizedName(this.getRegistryName().toString());
        this.setResistance(12.0F);
        this.setHardness((float) 3.0);
        this.setSoundType(SoundType.STONE);
        this.setHarvestLevel("pickaxe", 2);
    }

    @Override
    @Nullable
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return NOT_CONNECTED_AABB;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        //nop
    }

    @Override
    public IBlockState checkForSurroundingChests(World worldIn, BlockPos pos, IBlockState state){
        return(state);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos){
        return(true);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos p_189540_5_){

    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing heldItem, float side, float hitX, float hitY)
    {
        if (worldIn.isRemote)
        {
            return true;
        }
        else
        {
            ILockableContainer ilockablecontainer = this.getLockableContainer(worldIn, pos);

            if (ilockablecontainer != null)
            {
                playerIn.openGui(WastelandMeteors.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
                playerIn.addStat(StatList.CHEST_OPENED);
            }

            return true;
        }
    }

    @Override
    @Nullable
    public ILockableContainer getContainer(World world, BlockPos chestPos, boolean p_189418_3_){
        TileEntity tileentity = world.getTileEntity(chestPos);

        if (!(tileentity instanceof TileMeteorChest))
        {
            return null;
        }
        if (!p_189418_3_ && this.isBlocked(world, chestPos)){
            return null;
        }
        return (TileMeteorChest)tileentity;
    }

    private boolean isBlocked(World worldIn, BlockPos pos)
    {
        if(worldIn.getBlockState(pos.up()).isSideSolid(worldIn, pos.up(), EnumFacing.DOWN)){
            return true;
        }
        for (Entity entity : worldIn.getEntitiesWithinAABB(EntityOcelot.class, new AxisAlignedBB((double)pos.getX(), (double)(pos.getY() + 1), (double)pos.getZ(), (double)(pos.getX() + 1), (double)(pos.getY() + 2), (double)(pos.getZ() + 1))))
        {
            EntityOcelot entityocelot = (EntityOcelot)entity;

            if (entityocelot.isSitting())
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileMeteorChest();
    }
}
