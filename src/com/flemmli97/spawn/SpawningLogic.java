package com.flemmli97.spawn;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.WaterMob;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class SpawningLogic {

	private static final Set<EntityType> mobs;
	private static final Set<EntityType> animals;
	private static final Set<EntityType> ambient;
	private static final Set<EntityType> water;

	static
	{
		mobs = Sets.newHashSet();
		animals = Sets.newHashSet();
		ambient = Sets.newHashSet();
		water = Sets.newHashSet();
		for (EntityType type : EntityType.values()) 
		{
            Class<?> c = type.getEntityClass();
            if(c==null)
            	continue;
            if (Monster.class.isAssignableFrom(c) || Slime.class.isAssignableFrom(c) || Ghast.class.isAssignableFrom(c))
                mobs.add(type); 
            if (Animals.class.isAssignableFrom(c))
            	animals.add(type);
            if(Ambient.class.isAssignableFrom(c))
            	ambient.add(type);
            if(WaterMob.class.isAssignableFrom(c))
				water.add(type);
		}
	}
	
	@SuppressWarnings("incomplete-switch")
	protected static boolean canSpawn(EntityType entity, Location loc)
	{
		MobType type = getType(entity);
		if(type==MobType.UNDEFINED)
			return true;
		int range = SpawnPlugin.getSpawnRange(loc.getWorld());
		List<Player> inRange = Lists.newArrayList();
		List<Player> players = loc.getWorld().getPlayers();
		int cap = 0;
		switch(type)
		{
			case AMBIENT: cap = SpawnPlugin.divideCap?(int)(SpawnPlugin.ambientMax/(float)players.size()):SpawnPlugin.ambientMax;
				break;
			case ANIMAL: cap = SpawnPlugin.divideCap?(int)(SpawnPlugin.animalMax/(float)players.size()):SpawnPlugin.animalMax;
				break;
			case MOB: cap = SpawnPlugin.divideCap?(int)(SpawnPlugin.mobMax/(float)players.size()):SpawnPlugin.mobMax;
				break;
			case WATER: cap = SpawnPlugin.divideCap?(int)(SpawnPlugin.waterMax/(float)players.size()):SpawnPlugin.waterMax;
				break;
		}
		loc.getWorld().getPlayers().forEach(player->{if(!player.getGameMode().equals(GameMode.SPECTATOR) && player.getLocation().distanceSquared(loc)< (range+1)*16*(range+1)*16)inRange.add(player);});
		for(Player player : inRange)
		{
			if(getEntityCount(player, type)<cap)
				return true;
		}
		return false;
	}
	
	private static final Predicate<Entity> pred = entity->(!SpawnPlugin.countNameTaged||entity.getCustomName()==null)
			&&(SpawnPlugin.countSpawner||!entity.getScoreboardTags().contains("SpawnPlugin:spawner"));
	private static final Predicate<Entity> mobsPred = entity->mobs.contains(entity.getType());
	private static final Predicate<Entity> animalsPred = entity->animals.contains(entity.getType());
	private static final Predicate<Entity> waterPred = entity->water.contains(entity.getType());
	private static final Predicate<Entity> ambientPred = entity->ambient.contains(entity.getType());

	@SuppressWarnings("incomplete-switch")
	public static int getEntityCount(Player player, MobType type)
	{
		//Not Needed
		if(type==MobType.UNDEFINED)
			return 0;
		Predicate<Entity> typePred = mobsPred;
		switch(type)
		{
			case AMBIENT: typePred = ambientPred;
				break;
			case ANIMAL: typePred = animalsPred;
				break;
			case WATER: typePred = waterPred;
				break;
		}
		typePred = pred.and(typePred);
		int mobCount = 0;
        int playerX = player.getLocation().getBlockX()>>4;
        int playerZ = player.getLocation().getBlockZ()>>4;
        int radius = SpawnPlugin.getSpawnRange(player.getWorld());
        for (int x = -radius; x <= radius; x++)
            for (int z = -radius; z <= radius; z++)
            {
                int chunkX = x + playerX;
                int chunkZ = z + playerZ;
                if (player.getWorld().isChunkLoaded(chunkX, chunkZ))
                	for(Entity e : player.getWorld().getChunkAt(chunkX, chunkZ).getEntities())
                	{
                		if(typePred.test(e))
                			mobCount++;
                	}
            }     
		return mobCount;
	}
	
	private static MobType getType(EntityType type)
	{
		if(mobs.contains(type))
			return MobType.MOB;
		if(animals.contains(type))
			return MobType.ANIMAL;
		if(water.contains(type))
			return MobType.WATER;
		if(ambient.contains(type))
			return MobType.AMBIENT;
		return MobType.UNDEFINED;
	}
	
	public static enum MobType
	{
		MOB,
		ANIMAL,
		WATER,
		AMBIENT,
		UNDEFINED;
	}
}
