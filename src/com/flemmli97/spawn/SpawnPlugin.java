package com.flemmli97.spawn;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Maps;

public class SpawnPlugin extends JavaPlugin{

	public final FileConfiguration config = this.getConfig();

	protected static int animalMax, mobMax, waterMax, ambientMax, mobSpawnRangeDef, playerMobCap, playerAnimalCap, playerAmbientCap, playerWaterCap;
	
	protected static boolean countNameTaged, countSpawner, disableSpawner,divideCap;
	private static Map<String,Integer> worldSpawnRange = Maps.newHashMap();
	
	@Override
	public void onEnable()
	{
		this.reloadConfig();
		countNameTaged=config.getBoolean("CountNameTaggedMobs", false);
		countSpawner=config.getBoolean("CountSpawnerMobs", true);
		divideCap=config.getBoolean("DivideMobCapWithPlayerCount", true);
		disableSpawner=config.getBoolean("DisableSpawner", true);
		playerMobCap=config.getInt("PlayerMobCap", 70);
		playerAnimalCap=config.getInt("PlayerAnimalCap", 10);
		playerAmbientCap=config.getInt("PlayerAmbientCap", 15);
		playerWaterCap=config.getInt("PlayerWaterCap", 15);

		this.saveDefaultConfig();
		
		this.getCommand("getGlobalEntities").setExecutor(new CommandSpawnDebug());
		
	    getServer().getPluginManager().registerEvents(new Events(), this);
	    
	    try 
	    {
		    YamlConfiguration spigot = new YamlConfiguration();
			spigot.load(new File("spigot.yml"));
			ConfigurationSection section = spigot.getConfigurationSection("world-settings");
            mobSpawnRangeDef = section.getConfigurationSection("default").getInt("mob-spawn-range");
            for (String world : section.getKeys(false))
                if (!world.equals("default")) 
                	worldSpawnRange.put(world, section.getConfigurationSection(world).getInt("mob-spawn-range"));
            
		} 
	    catch (IOException | InvalidConfigurationException e) 
	    {
			mobSpawnRangeDef=8;
			e.printStackTrace();
		}
	    
	    try 
	    {
		    YamlConfiguration bukkit = new YamlConfiguration();
			bukkit.load(new File("bukkit.yml"));
		    ConfigurationSection spawns = bukkit.getConfigurationSection("spawn-limits");
		    mobMax=spawns.getInt("monsters");
		    animalMax=spawns.getInt("animals");
		    waterMax=spawns.getInt("water-animals");
		    ambientMax=spawns.getInt("ambient");
		} 
	    catch (IOException | InvalidConfigurationException e) 
	    {
			mobMax=70;
		    animalMax=10;
		    waterMax=15;
		    ambientMax=15;
			e.printStackTrace();
		}
	} 
	
	@Override
	public void onDisable()
	{
		worldSpawnRange.clear();
	}
	
	public static int getSpawnRange(World world)
	{
		return worldSpawnRange.getOrDefault(world.getName(), mobSpawnRangeDef);
	}
	
	private static class Events implements Listener
	{
		
		@EventHandler
	    public void spawner(SpawnerSpawnEvent event)
	    {
			if(!SpawnPlugin.countSpawner)
			{
				event.getEntity().addScoreboardTag("SpawnPlugin:spawner");
			}
			if(SpawnPlugin.disableSpawner && !SpawningLogic.canSpawn(event.getEntityType(), event.getLocation()))
				event.setCancelled(true);
	    }
		
		@EventHandler
	    public void onSpawn(CreatureSpawnEvent event)
	    {
			if(event.getSpawnReason()!=SpawnReason.NATURAL)
				return;
			if(!SpawningLogic.canSpawn(event.getEntityType(), event.getLocation()))
				event.setCancelled(true);
	    }
	}
	
}
