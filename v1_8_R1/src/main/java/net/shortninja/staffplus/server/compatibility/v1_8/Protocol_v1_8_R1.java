package net.shortninja.staffplus.server.compatibility.v1_8;

import net.minecraft.server.v1_8_R1.*;
import net.shortninja.staffplus.IStaffPlus;
import net.shortninja.staffplus.server.compatibility.AbstractProtocol;
import net.shortninja.staffplus.server.compatibility.IProtocol;
import net.shortninja.staffplus.util.lib.json.JsonMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.craftbukkit.v1_8_R1.CraftServer;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.util.Set;

public class Protocol_v1_8_R1 extends AbstractProtocol implements IProtocol
{
	public Protocol_v1_8_R1(IStaffPlus staffPlus) {
		super(staffPlus);
	}

	@SuppressWarnings("Duplicates")
	@Override
	public org.bukkit.inventory.ItemStack addNbtString(org.bukkit.inventory.ItemStack item, String value)
	{
		ItemStack craftItem = CraftItemStack.asNMSCopy(item);
		NBTTagCompound nbtCompound = craftItem.getTag() == null ? new NBTTagCompound() : craftItem.getTag();
		
		nbtCompound.setString(NBT_IDENTIFIER, value);
		craftItem.setTag(nbtCompound);
		
		return CraftItemStack.asCraftMirror(craftItem);
	}
	
	@Override
	public String getNbtString(org.bukkit.inventory.ItemStack item)
	{
		ItemStack craftItem = CraftItemStack.asNMSCopy(item);
		
		if(craftItem == null)
		{
			return "";
		}
		
		NBTTagCompound nbtCompound = craftItem.getTag() == null ? new NBTTagCompound() : craftItem.getTag();
		
		return nbtCompound.getString(NBT_IDENTIFIER);
	}
	
	@Override
	public void registerCommand(String match, Command command)
	{
		((CraftServer) Bukkit.getServer()).getCommandMap().register(match, command);
	}
	
	@Override
	public void listVanish(Player player, boolean shouldEnable)
	{
		PacketPlayOutPlayerInfo packet = null;
		
		if(shouldEnable)
		{
			packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, ((CraftPlayer) player).getHandle());
		}else packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, ((CraftPlayer) player).getHandle());
		
		sendGlobalPacket(packet);
	}
	
	@Override
	public void sendHoverableJsonMessage(Set<Player> players, String message, String hoverMessage)
	{
		JsonMessage json = new JsonMessage().append(message).setHoverAsTooltip(hoverMessage).save();
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a(json.getMessage()));
		
		for(Player player : players)
		{
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
		}
	}

	@Override
	public String getSound(Object object) {
		return object instanceof String ? (String) object : null;
	}
	
	private void sendGlobalPacket(Packet packet)
	{
		for(Player player : Bukkit.getOnlinePlayers())
		{
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
		}
	}
}