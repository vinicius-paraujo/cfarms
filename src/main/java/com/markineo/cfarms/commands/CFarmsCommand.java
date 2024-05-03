package com.markineo.cfarms.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.markineo.cfarms.util.FileManager;


public class CFarmsCommand implements CommandExecutor {
	private FileManager fManager;
	
	public CFarmsCommand(FileManager fManager) {
		this.fManager = fManager;
	}
	
	private Player player;
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			if (args.length < 1) { sender.sendMessage("§c/cfarms reload"); return true; }
			
			if (args[0].equals("reload")) {
				fManager.reloadConfigurations();
				
				sender.sendMessage("§aAs configurações foram recarregadas a partir dos arquivos locais.");
			}
			
			return true;
		}
		
		player = (Player) sender;
		
		if (!player.hasPermission("cfarms.admin")) { player.sendMessage("§cVocê não tem permissão para executar esse comando"); return true; }
		String sintaxe = "§ccFarms - AJUDA\n§c/cfarms reload";
		if (args.length < 1) { player.sendMessage(sintaxe); return true; }
		
		switch (args[0]) {
			case "reload":
				fManager.reloadConfigurations();
				
				player.sendMessage("§aAs configurações foram recarregadas a partir dos arquivos locais.");
				
			break;
			default:
				player.sendMessage(sintaxe);
			break;
		}
		
		return true;
	}
}
