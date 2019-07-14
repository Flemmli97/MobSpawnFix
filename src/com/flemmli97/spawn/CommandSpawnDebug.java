package com.flemmli97.spawn;

import java.util.List;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.flemmli97.spawn.SpawningLogic.MobType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.md_5.bungee.api.chat.TextComponent;

public class CommandSpawnDebug implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
		if(sender.isOp())
		{
			List<String> list = Lists.newArrayList();
			Map<World,Integer> worldAmbientCap = Maps.newHashMap();
			Map<World,Integer> worldAnimalCap = Maps.newHashMap();
			Map<World,Integer> worldMobCap = Maps.newHashMap();
			Map<World,Integer> worldWaterCap = Maps.newHashMap();
			sender.getServer().getWorlds().forEach(world->{
				worldAmbientCap.put(world, Math.min(SpawnPlugin.playerAmbientCap, SpawningLogic.getActualCap(SpawnPlugin.ambientMax, world, world.getPlayers().size())));
				worldAnimalCap.put(world, Math.min(SpawnPlugin.playerAnimalCap, SpawningLogic.getActualCap(SpawnPlugin.animalMax, world, world.getPlayers().size())));
				worldMobCap.put(world, Math.min(SpawnPlugin.playerMobCap, SpawningLogic.getActualCap(SpawnPlugin.mobMax, world, world.getPlayers().size())));
				worldWaterCap.put(world, Math.min(SpawnPlugin.playerWaterCap, SpawningLogic.getActualCap(SpawnPlugin.waterMax, world, world.getPlayers().size())));
			});			
			for(Player player : sender.getServer().getOnlinePlayers())
			{
				int ambient = SpawningLogic.getEntityCount(player, MobType.AMBIENT);
				int animals = SpawningLogic.getEntityCount(player, MobType.ANIMAL);
				int mobs = SpawningLogic.getEntityCount(player, MobType.MOB);
				int water = SpawningLogic.getEntityCount(player, MobType.WATER);
				list.add(player.getName() + "("+player.getWorld().getName()+"): Ambient[" + ambient + "/" + worldAmbientCap.get(player.getWorld()) + "]; "
						+ "Animals[" + animals + "/" + worldAnimalCap.get(player.getWorld()) + "]; Mobs[" + mobs + "/" + worldMobCap.get(player.getWorld()) + 
						"]; Water[" + water + "/" + worldWaterCap.get(player.getWorld()) + "]");
			}
			for(String s : list)
				sender.spigot().sendMessage(new TextComponent(s));
			return true;
		}
		return false;
	}

}
