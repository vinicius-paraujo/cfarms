package com.markineo.cfarms;

import java.sql.Connection;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.markineo.cfarms.commands.CFarmsCommand;
import com.markineo.cfarms.events.FarmsEvents;
import com.markineo.cfarms.farms.FarmManager;
import com.markineo.cfarms.util.CommandManager;
import com.markineo.cfarms.util.DatabaseManager;
import com.markineo.cfarms.util.FileManager;

public class Farms extends JavaPlugin {
	private FileManager fManager;
	private FarmManager fmManager;
	
	private FileConfiguration sqlConfig;
	private Connection conn;
	
	@Override
	public void onEnable() {
		fManager = new FileManager(this);
		
		fManager.loadConfigurations();
		sqlConfig = fManager.getConfig("database.yml");
		
		if (!this.setupDatabase()) {
			Bukkit.getConsoleSender().sendMessage("§7[§ccFarms§7] §cFalha ao conectar ao MySQL. O plugin será desligado.");
            this.getServer().getPluginManager().disablePlugin(this);
            
            return;
        }
		
		fmManager = new FarmManager(this, fManager);
		
		CommandManager.registerCommands(this, new CFarmsCommand(fManager));
		getServer().getPluginManager().registerEvents(new FarmsEvents(fmManager), this);
		
		Bukkit.getConsoleSender().sendMessage("§7[§ccFarms§7] §fDesenvolvido por: Markineo.");

		setupScheduledTasks();
	}
	
	private boolean setupDatabase() {	
		String host = sqlConfig.getString("host");
		int port = sqlConfig.getInt("port");
		String user = sqlConfig.getString("user");
		String database = sqlConfig.getString("database");
		String password = sqlConfig.getString("password");
		String url = "jdbc:mysql://"+host+":"+port+"/"+database+"?characterEncoding=UTF-8";
        DatabaseManager.configureDataSource(url, user, password);
		
		try {
			conn = DatabaseManager.getConnection();
			if (conn != null) Bukkit.getConsoleSender().sendMessage("§7[§ccFarms§7] Conectado com sucesso ao MySQL.");
			
			return true;
		} catch (SQLException e) {
			Bukkit.getConsoleSender().sendMessage(e.getMessage());
			
			return false;
		} finally {
			try {
				if (conn != null && !conn.isClosed()) conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void setupScheduledTasks() {
		Bukkit.getScheduler().runTaskLater(this, () -> fmManager.setupAllFarms(), 250);
		
        Bukkit.getScheduler().runTaskLater(this, () -> {
            fmManager.updateFarmsActive();

            Bukkit.getScheduler().runTaskTimer(this, () -> fmManager.updateFarmsActive(), 0L, 100L);
        }, 300L);

        Bukkit.getScheduler().runTaskLater(this, () -> {
            fmManager.updateDatabase();

            Bukkit.getScheduler().runTaskTimer(this, () -> fmManager.updateDatabase(), 0L, 1200L);
        }, 1200L);
    }
}
