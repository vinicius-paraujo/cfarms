package com.markineo.cfarms.util;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandManager {
    public static void registerCommands(JavaPlugin plugin, CommandExecutor... executors) {
        for (CommandExecutor executor : executors) {
            String commandName = executor.getClass().getSimpleName().replace("Command", "").toLowerCase();
            plugin.getCommand(commandName).setExecutor(executor);
        }
    }
}
