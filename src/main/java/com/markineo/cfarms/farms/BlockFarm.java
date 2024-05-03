package com.markineo.cfarms.farms;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.CropState;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Crops;

public class BlockFarm {
	private Block block;
	private HashMap<CropState, CropState> nextStateMap;
	private HashMap<Material, Integer> maxState;
	
    public BlockFarm(Block block) {
        this.block = block;
        this.nextStateMap = new HashMap<>();
        this.maxState = new HashMap<>();
        initializeNextStateMap();
        initializeMaterials();
    }
	
	public Block getBlock() {
		return block;
	}
	
	public Location getLocation() {
		return block.getLocation();
	}
	
	public boolean isValidFarm() {
		return maxState.containsKey(block.getType());
	}
	
	public boolean isCropFarm() {
		return block.getState().getData() instanceof Crops;
	}
	
	public boolean isRepetableFarm() {
		return block.getType().equals(Material.MELON_STEM) || block.getType().equals(Material.PUMPKIN_STEM) || block.getType().equals(Material.CACTUS);
	}
	
	public boolean isMaxStage() {
		return block.getData() >= maxState.get(block.getType());
	}
	
	public boolean isCropMaxStage() {
		Crops crops = (Crops) block.getState().getData();
		return crops.getState().equals(CropState.RIPE);
	}
	
	public void nextStage() {
	    if (!maxState.containsKey(block.getType())) return;
	    
        byte bData = block.getData();
        byte nextState = (byte) (bData + 1);

        BlockFace[] directions = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
        
        if (block.getType().equals(Material.CACTUS)) {
        	handleCactusGrowing();
        	return;
        }
        
        if ((block.getType() == Material.MELON_STEM || block.getType() == Material.PUMPKIN_STEM) && nextState > maxState.get(block.getType())) {
            Material targetMaterial = (block.getType() == Material.MELON_STEM) ? Material.MELON_BLOCK : Material.PUMPKIN;
            boolean fruitCreated = false;

            for (BlockFace direction : directions) {
                boolean fruitAdjacent = Arrays.stream(directions).anyMatch(adjacentDirection -> block.getRelative(adjacentDirection).getType() == targetMaterial);

                if (!fruitAdjacent) {
                    Block fruitBlock = block.getRelative(direction);

                    if (fruitBlock.getType() == Material.AIR && fruitBlock.getRelative(BlockFace.DOWN).getType() != Material.AIR && !fruitCreated) {
                        fruitBlock.setType(targetMaterial);
                        fruitBlock.getWorld().playEffect(fruitBlock.getLocation(), Effect.HAPPY_VILLAGER, 2);
                        fruitCreated = true;
                    }
                }
            }
        }
        
        if (nextState <= maxState.get(block.getType())) {
        	block.setData(nextState);
        	Location animationLocation = new Location(block.getWorld(), block.getX() + 0.5, block.getY(), block.getZ() + 0.5);
	        block.getWorld().playEffect(animationLocation, Effect.HAPPY_VILLAGER, 2);
        }
        
        block.getState().update();
    }
	
	private void handleCactusGrowing() {
	    Location cactusLocation = block.getLocation();
	    int currentHeight = getCactusHeight(cactusLocation);
	    int maxHeight = 3;
	    
	    if (currentHeight >= maxHeight) {
	        return;
	    }
	    
	    Location blockAboveLocation = cactusLocation.clone().add(0, currentHeight, 0);
	    Block blockAbove = blockAboveLocation.getBlock();
	    
	    boolean isAdjacentOccupied = false;
	    BlockFace[] directions = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
	    for (BlockFace direction : directions) {
	        Block adjacentBlock = blockAbove.getRelative(direction);
	        if (!adjacentBlock.isEmpty()) {
	            isAdjacentOccupied = true;
	            break;
	        }
	    }
	    
	    if (isAdjacentOccupied) {
	        return;
	    }
	    
	    blockAbove.setType(Material.CACTUS);
	    block.getWorld().playEffect(blockAboveLocation, Effect.HAPPY_VILLAGER, 2);
	}
	
	private void sendLog(String message) {
		Bukkit.getConsoleSender().sendMessage("§7[§ccFarms§7] " + message);
	}
	
	private int getCactusHeight(Location cactusLocation) {
	    int height = 1;
	    
	    while (cactusLocation.clone().add(0, height, 0).getBlock().getType() == Material.CACTUS) {
	        height++;
	    }
	    
	    return height;
	}
	
	public void nextCropStage() {
        if (block.getState().getData() instanceof Crops) {
            Crops crops = (Crops) block.getState().getData();
            CropState currentState = crops.getState();  
            CropState nextState = nextStateMap.get(currentState);
            
            if (nextState != null) {
            	crops.setData(nextState.getData());
                block.setData(nextState.getData());;
                block.getState().update();
                
                Location animationLocation = new Location(block.getWorld(), block.getX() + .5, block.getY(), block.getZ() + .5);
                block.getWorld().playEffect(animationLocation, Effect.HAPPY_VILLAGER, 2);
            }
        }
    }
	
	private void initializeMaterials() {
		maxState.put(Material.NETHER_WARTS, 3);
		maxState.put(Material.PUMPKIN_STEM, 7);
		maxState.put(Material.CROPS, 7);
		maxState.put(Material.POTATO, 7);
		maxState.put(Material.CARROT, 7);
		maxState.put(Material.MELON_STEM, 7);
		maxState.put(Material.CACTUS, 0); //possui limite de 15, mas vai crescer no máximo 3 blocos
	}
	
	private void initializeNextStateMap() {
        nextStateMap.put(CropState.SEEDED, CropState.GERMINATED);
        nextStateMap.put(CropState.GERMINATED, CropState.VERY_SMALL);
        nextStateMap.put(CropState.VERY_SMALL, CropState.SMALL);
        nextStateMap.put(CropState.SMALL, CropState.MEDIUM);
        nextStateMap.put(CropState.MEDIUM, CropState.TALL);
        nextStateMap.put(CropState.TALL, CropState.VERY_TALL);
        nextStateMap.put(CropState.VERY_TALL, CropState.RIPE);
        nextStateMap.put(CropState.RIPE, CropState.RIPE);
    }
	
}
