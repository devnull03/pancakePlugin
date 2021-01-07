package io.github.arnav03mehta;

import io.github.arnav03mehta.commands.PancakeCommands;
import io.github.arnav03mehta.events.Events;
import io.github.arnav03mehta.items.ItemManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Objects;

public final class PancakePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        try {
            PancakeCommands.connect();
        } catch (SQLException e) {
            getServer().getConsoleSender().sendMessage(ChatColor.RED + e.getMessage());
        }

        PancakeCommands pancakeCommands = new PancakeCommands();
        ItemManager.init();
        getServer().getPluginManager().registerEvents(new Events(), this);
        Objects.requireNonNull(getCommand("withdraw")).setExecutor(pancakeCommands);
        Objects.requireNonNull(getCommand("deposit")).setExecutor(pancakeCommands);
        Objects.requireNonNull(getCommand("balance")).setExecutor(pancakeCommands);
        Objects.requireNonNull(getCommand("newuser")).setExecutor(pancakeCommands);
        Objects.requireNonNull(getCommand("addbalance")).setExecutor(pancakeCommands);
        Objects.requireNonNull(getCommand("test")).setExecutor(pancakeCommands);
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "plugin enabled");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        try {
            PancakeCommands.disconnect();
        } catch (SQLException e) {
            getServer().getConsoleSender().sendMessage(ChatColor.RED + e.getMessage());
        }
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "plugin Disabled");
    }
}
