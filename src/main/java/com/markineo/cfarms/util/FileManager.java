package com.markineo.cfarms.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import com.markineo.cfarms.Farms;

public class FileManager {
	private final Farms plugin;
	private FileConfiguration messagesConfig;
	private FileConfiguration mainConfig;
	
	private boolean isLoaded;
	
	public FileManager(Farms plugin) {
		this.plugin = plugin;
	}
	
	public FileConfiguration getConfig(String fileName) {
	    File configFile = new File(plugin.getDataFolder(), fileName);

	    if (!configFile.exists()) {
	        try {
	            plugin.saveResource(fileName, false);
	        } catch (IllegalArgumentException e) {
	        	return null;
	        }
	    }

	    FileConfiguration config = new YamlConfiguration();
	    try {
	        config.load(configFile);
	    } catch (IOException | InvalidConfigurationException e) {
	        e.printStackTrace();
	    }

	    return config;
	}

	
	public void loadConfigurations() {
	    if (!isLoaded) {
	        isLoaded = true;

	        messagesConfig = getConfig("messages.yml");
	        mainConfig = getConfig("config.yml");
	    }
	}

	private void registerCommand(String name, CommandExecutor executor) {
        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

            Command command = new Command(name) {
                @Override
                public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                    return executor.onCommand(sender, this, commandLabel, args);
                }
            };

            commandMap.register(name, command);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
	
	public void setConfig(FileConfiguration file, String fileName, String config, Object value) {
        file.set(config, value);
        saveConfig(file, fileName);
    }

    private void saveConfig(FileConfiguration config, String fileName) {
        File configFile = new File(plugin.getDataFolder(), fileName);
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public ItemStack loadItem(FileConfiguration config, String path) {
    	if (!config.contains(path)) return null;
        
    	try {
    		return ItemStack.deserialize(config.getConfigurationSection(path).getValues(false));
    	} catch (Exception e) {
    		e.printStackTrace();
    		return null;
    	}
    }
    
    public Location loadLocation(FileConfiguration config, String path) {
        if (!config.contains(path)) return null;
        
    	try {
    		return Location.deserialize(config.getConfigurationSection(path).getValues(false));
    	} catch (Exception e) {
    		e.printStackTrace();
    		return null;
    	}
    }
	
	public void reloadConfigurations() {
		messagesConfig = null;
        mainConfig = null;
        
	    messagesConfig = getConfig("messages.yml");
        mainConfig = getConfig("config.yml");
	}

	public FileConfiguration getMessagesConfig() {
	    loadConfigurations();
	    return messagesConfig;
	}

	public FileConfiguration getMainConfig() {
	    loadConfigurations();
	    return mainConfig;
	}
	
	public String getMessage(String message) {
		if (messagesConfig == null) messagesConfig = getConfig("messages.yml");
		
		return messagesConfig.getString(message).replace("&","§").replace("{linha}", "§f§n                                                                            \n§f \n");
	}
	
	public double getDefaultSpeed() {
		return mainConfig.getDouble("growing.default_speed");
	}
	
	public double getGrowMultiplier() {
		return mainConfig.getDouble("growing.grow_multiplier");
	}
	
	public List<String> getFarmsWorlds() {
		return mainConfig.getStringList("protection.worlds");
	}
	
	public int getRadius() {
		return mainConfig.getInt("growing.radius");
	}
}
