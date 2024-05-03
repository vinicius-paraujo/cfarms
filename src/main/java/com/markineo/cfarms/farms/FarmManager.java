package com.markineo.cfarms.farms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.Crops;
import org.bukkit.scheduler.BukkitTask;

import com.markineo.cfarms.Farms;
import com.markineo.cfarms.util.DatabaseManager;
import com.markineo.cfarms.util.FileManager;

public class FarmManager {
    private List<BlockFarm> allFarms;
    private List<BlockFarm> farmsActive;
    private List<BlockFarm> blocksToAdd;
    private List<BlockFarm> blocksToRemove;
    private HashMap<Block, BukkitTask> farmTasks;

    private Farms plugin;
    private double radius;

    private double defaultSpeed;
    private double growMultiplier;

    public FarmManager(Farms plugin, FileManager fManager) {
        DatabaseManager.createTable();
        this.farmsActive = new ArrayList<>();
        this.blocksToAdd = new ArrayList<>();
        this.blocksToRemove = new ArrayList<>();
        this.farmTasks = new HashMap<>();
        this.plugin = plugin;
        this.radius = fManager.getRadius();
        this.defaultSpeed = fManager.getDefaultSpeed();
        this.growMultiplier = fManager.getGrowMultiplier();
    }
    
    private boolean existTask(Block block) {
    	return farmTasks.containsKey(block);
    }
    
    private void setTask(BlockFarm block, BukkitTask task) {
    	farmTasks.put(block.getBlock(), task);
    }

    public void setupAllFarms() {
    	sendLog("As plantações foram carregadas do banco de dados.");
        allFarms = DatabaseManager.getAllFarms();
    }

    private void sendLog(String message) {
        Bukkit.getConsoleSender().sendMessage("§7[§cFarms§7] " + message);
    }

    public void updateDatabase() {
        DatabaseManager.updateDatabase(blocksToRemove, blocksToAdd);
    }

    public void updateFarmsActive() {
        List<BlockFarm> tempFarmsActive = new ArrayList<>();

        for (BlockFarm farm : allFarms) {
            if (isAnyPlayerNextToFarm(farm.getLocation())) {
            	if (!farmsActive.contains(farm)) {
            		setStageGrowTask(farm);
            	}
            	
                tempFarmsActive.add(farm);
            }
        }

        farmsActive = tempFarmsActive;
    }

    public void removePlayerFarm(Player player, Block block) {
        BlockFarm farmToRemove = null;

        for (BlockFarm farm : allFarms) {
            if (farm.getBlock().equals(block)) {
                farmToRemove = farm;
                break;
            }
        }

        if (farmToRemove != null) {
            allFarms.remove(farmToRemove);
            blocksToRemove.add(farmToRemove);
        }
    }

    public void addPlayerFarm(Player player, Block block) {
        BlockFarm farm = new BlockFarm(block);
        allFarms.add(farm);
        blocksToAdd.add(farm);

        setStageGrowTask(farm);
    }

    private void setStageGrowTask(BlockFarm block) {
        if (existTask(block.getBlock())) return;

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
        	farmTasks.remove(block.getBlock());

            if (block.isCropFarm() && !block.isCropMaxStage() && isAnyPlayerNextToFarm(block.getBlock().getLocation())) {
                block.nextCropStage();
                setStageGrowTask(block);
                
            } else if (block.isValidFarm() && isAnyPlayerNextToFarm(block.getBlock().getLocation())) {
                if (block.isMaxStage() && !block.isRepetableFarm()) return;
                
    			block.nextStage();
    			
    			if (!block.isMaxStage() || (block.isMaxStage() && block.isRepetableFarm())) setStageGrowTask(block);
            }
        }, calculateGrowthTicks(block));

        setTask(block, task);
    }

    private long calculateGrowthTicks(BlockFarm block) {
    	double growthTime = defaultSpeed * growMultiplier;
        double randomVariation = growthTime * 0.15;
        double finalGrowthTime = growthTime + (Math.random() * 2 * randomVariation) - randomVariation;


    	return (block.isRepetableFarm() && block.isMaxStage()) ? (long) (finalGrowthTime * 2400) : (long) (finalGrowthTime * 1200);
    }


    public List<BlockFarm> getFarmsWhereHasPlayerNext() {
        List<BlockFarm> blocks = new ArrayList<>();

        for (BlockFarm farm : farmsActive) {
            if (isAnyPlayerNextToFarm(farm.getLocation())) {
                blocks.add(farm);
            }
        }

        return blocks;
    }
    
    private boolean isAnyPlayerNextToFarm(Location location) {
        List<Player> nearbyPlayers = location.getWorld().getNearbyEntities(location, radius, radius, radius).stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .collect(Collectors.toList());

        return !nearbyPlayers.isEmpty();
    }
}