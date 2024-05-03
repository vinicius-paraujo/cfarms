package com.markineo.cfarms.events;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.material.Crops;

import com.markineo.cfarms.farms.FarmManager;

public class FarmsEvents implements Listener {
	private FarmManager fmManager;
	
	private List<Material> othersFarms = Arrays.asList(
        	Material.PUMPKIN_STEM,
        	Material.MELON_STEM,
        	Material.CACTUS,
        	Material.NETHER_WARTS,
        	Material.SUGAR_CANE,
        	Material.POTATO,
        	Material.CARROT
    );
	
	public FarmsEvents(FarmManager fmManager) {
		this.fmManager = fmManager;
	}
	
	private void sendLog(String message) {
		Bukkit.getConsoleSender().sendMessage("§7[§ccFarms§7]" + message);
	}
	
	@EventHandler
	public void onFarmPlace(BlockPlaceEvent event) {	
		if (event.getBlockPlaced().getState().getData() instanceof Crops || othersFarms.contains(event.getBlockPlaced().getType())) {
			
			if (event.getBlockPlaced().getType().equals(Material.CACTUS) && !(event.getBlockPlaced().getRelative(BlockFace.DOWN)).getType().equals(Material.SAND)) return; 
		
			fmManager.addPlayerFarm(event.getPlayer(), event.getBlockPlaced());
		}
		
	}
	
	@EventHandler
	public void onFarmBreak(BlockBreakEvent event) {
		if (event.getBlock().getState().getData() instanceof Crops || othersFarms.contains(event.getBlock().getType())) {
			fmManager.removePlayerFarm(event.getPlayer(), event.getBlock());
		}
	}
	
	@EventHandler
	public void defaultGrowEvent(BlockGrowEvent event) {
		if (event.getBlock().getState().getData() instanceof Crops || othersFarms.contains(event.getBlock().getType())) {
			event.setCancelled(true);
			return;
		}
	}
	
}
