package family_fun_pack.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

import family_fun_pack.gui.interfaces.PreviewGui;

/* Shulker/book peek, but for shulker/book dropped on the floor or hold by other entities */

@SideOnly(Side.CLIENT)
public class PeekCommand extends Command {

  private GuiScreen screen;

  public PeekCommand() {
    super("stare");
    this.screen = null;
  }

  public String usage() {
    return this.getName();
  }

  public String execute(String[] args) {
    Minecraft mc = Minecraft.getMinecraft();

    boolean book = (args.length > 1 && args[1].equals("book")) ? true : false;

    ItemStack stack = null;
    for(Entity entity : mc.world.<Entity>getEntitiesWithinAABB(Entity.class, mc.player.getEntityBoundingBox().grow(12.0D, 4.0D, 12.0D))) {

      if(entity == mc.player) continue;

      // Search through entities metadata
      for(EntityDataManager.DataEntry<?> entry : entity.getDataManager().getAll()) {
        if(entry.getValue() instanceof ItemStack && ((book && (((ItemStack) entry.getValue()).getItem() instanceof ItemWritableBook || ((ItemStack) entry.getValue()).getItem() instanceof ItemWrittenBook)) || (!book && ((ItemStack) entry.getValue()).getItem() instanceof ItemShulkerBox))) {
          stack = (ItemStack) entry.getValue();
          break;
        }
      }
      if(stack != null) break;

      // Search through entity equipment
      for(ItemStack item : entity.getEquipmentAndArmor()) {
        if((!book && item.getItem() instanceof ItemShulkerBox) || (book && (item.getItem() instanceof ItemWritableBook || item.getItem() instanceof ItemWrittenBook))) {
          stack = item;
          break;
        }
      }
      if(stack != null) break;
    }

    if(stack == null) return "No " + (book ? "book" : "shulker") + " item close to you";

    if(book) {

      if(stack.getItem() instanceof ItemWritableBook) {
        stack = stack.copy();
        stack.getTagCompound().setString("title", "Writable book");
        stack.getTagCompound().setString("author", "No author");
      }

      this.screen = new GuiScreenBook(mc.player, stack, false);
      MinecraftForge.EVENT_BUS.register(this);
    } else {
      NBTTagCompound tag = stack.getTagCompound();
      if(tag != null && tag.hasKey("BlockEntityTag") && tag.getTagId("BlockEntityTag") == 10) {
        NBTTagCompound blockTag = tag.getCompoundTag("BlockEntityTag");
        if(blockTag.hasKey("Items") && blockTag.getTagId("Items") == 9) {
          this.screen = new PreviewGui(blockTag.getTagList("Items", 10), false);
          MinecraftForge.EVENT_BUS.register(this);
          return null;
        }
      }

      if(tag == null) return "Shulker is empty, no tag";
      return "Shulker does not contain items, no BlockEntityTag tag, tag is " + tag.toString();
    }
    return null;
  }

  @SubscribeEvent
  public void onGuiOpened(GuiOpenEvent event) {
    if(event.getGui() == null) {
      event.setGui(this.screen);
      this.screen = null;
    }
    MinecraftForge.EVENT_BUS.unregister(this);
  }
}
