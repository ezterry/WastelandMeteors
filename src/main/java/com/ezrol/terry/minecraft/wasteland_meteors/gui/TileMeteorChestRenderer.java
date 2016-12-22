package com.ezrol.terry.minecraft.wasteland_meteors.gui;

import com.ezrol.terry.minecraft.wasteland_meteors.WastelandMeteors;
import com.ezrol.terry.minecraft.wasteland_meteors.blocks.TileMeteorChest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.model.ModelLargeChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Calendar;

@SideOnly(Side.CLIENT)
public class TileMeteorChestRenderer extends TileEntitySpecialRenderer<TileMeteorChest>
{
    private static final ResourceLocation TEXTURE_METEOR_CHEST = new ResourceLocation(WastelandMeteors.MODID, "textures/tile/meteor_chest_normal.png");
    private final ModelChest simpleChest = new ModelChest();

    public TileMeteorChestRenderer()
    {
    }

    public void renderTileEntityAt(TileMeteorChest te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        ModelChest modelchest;
        GlStateManager.enableDepth();
        GlStateManager.depthFunc(515);
        GlStateManager.depthMask(true);
        int meta;
        int rotation = 0;

        if (te.hasWorld())
        {
            Block block = te.getBlockType();
            meta = te.getBlockMetadata();

            if (block instanceof BlockChest && meta == 0)
            {
                ((BlockChest)block).checkForSurroundingChests(te.getWorld(), te.getPos(), te.getWorld().getBlockState(te.getPos()));
                meta = te.getBlockMetadata();
            }
        }
        else
        {
            meta = 0;
        }

        modelchest = this.simpleChest;

        if (destroyStage >= 0)
        {
            this.bindTexture(DESTROY_STAGES[destroyStage]);
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 4.0F, 1.0F);
            GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(5888);
        }
        this.bindTexture(TEXTURE_METEOR_CHEST);

        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();

        if (destroyStage < 0)
        {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        GlStateManager.translate((float)x, (float)y + 1.0F, (float)z + 1.0F);
        GlStateManager.scale(1.0F, -1.0F, -1.0F);
        GlStateManager.translate(0.5F, 0.5F, 0.5F);


        if (meta == 2)
        {
            rotation = 180;
        }

        if (meta == 3)
        {
            rotation = 0;
        }

        if (meta == 4)
        {
            rotation = 90;
        }

        if (meta == 5)
        {
            rotation = -90;
        }


        GlStateManager.rotate((float)rotation, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-0.5F, -0.5F, -0.5F);

        float lidAngle = te.lastLidAngle + (te.lidAngle - te.lastLidAngle) * partialTicks;
        lidAngle = 1.0F - lidAngle;
        lidAngle = 1.0F - lidAngle * lidAngle * lidAngle;

        modelchest.chestLid.rotateAngleX = -(lidAngle * ((float)Math.PI / 2F));
        modelchest.renderAll();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        if (destroyStage >= 0)
        {
            GlStateManager.matrixMode(5890);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }
    }
}