package com.ezrol.terry.minecraft.wasteland_meteors.gui;

import com.ezrol.terry.minecraft.wasteland_meteors.WastelandMeteors;
import com.ezrol.terry.minecraft.wasteland_meteors.blocks.TileMeteorChest;
import com.ezrol.terry.minecraft.wasteland_meteors.inventory.ContainerMeteorChest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MeteorChestGui extends GuiContainer
{
    /** The ResourceLocation containing the chest GUI texture. */
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation(WastelandMeteors.MODID, "textures/gui/meteorchestgui.png");
    private final IInventory meteorChest;
    private final IInventory playerInv;

    public MeteorChestGui(EntityPlayer player, World world, TileMeteorChest te)
    {

        super(new ContainerMeteorChest(player.inventory,te,player));
        meteorChest = te;
        playerInv=player.inventory;
        this.allowUserInput = false;
        this.ySize = 114 + 3 * 18;
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRendererObj.drawString(this.meteorChest.getDisplayName().getUnformattedText(), 8, 6, 4210752);
        this.fontRendererObj.drawString(this.playerInv.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, 3 * 18 + 17);
        this.drawTexturedModalRect(i, j + 3 * 18 + 17, 0, 126, this.xSize, 96);
    }
}