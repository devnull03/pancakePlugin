package io.github.arnav03mehta.commands;

import io.github.arnav03mehta.items.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class PancakeCommands implements CommandExecutor {

    private static Connection connection = null;

    public static void connect() throws SQLException {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:bankDB.db");
        } catch (SQLException e) {
            try {
                if(connection != null)
                    connection.close();
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + e.getMessage());
                throw e;
            } catch(SQLException e2) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + e2.getMessage());
                throw e2;
            }
        }
    }
    public static void disconnect() throws SQLException {
        try {
            if(connection != null)
                connection.close();
        } catch(SQLException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Player only command");
            return false;
        }
        Player player = (Player) sender;
        World world = player.getWorld();
        UUID playerUUID = player.getUniqueId();

        try {
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            createTable(statement);

            switch (label) {
                case ("balance"), ("bal") -> {
                    if (args.length >= 1) {
                        Player newPlayer = Bukkit.getPlayer(args[0]);
                        if (newPlayer == null) {
                            player.sendMessage(ChatColor.RED + "invalid player");
                            return true;
                        }
                        playerUUID = newPlayer.getUniqueId();
                    }
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "Current balance = " + ChatColor.GREEN + balance(statement, playerUUID));
                    return true;
                }
                case ("wd"), ("withdraw") -> {
                    try {
                        if (args.length >= 1) {
                            int balance = balance(statement, playerUUID);
                            int amount = Integer.parseInt(args[0]);
                            if (amount > balance) {
                                player.sendMessage(String.format(ChatColor.RED + "Not enough pancakes, You need %s%s%s more", ChatColor.YELLOW, (amount - balance), ChatColor.RED));
                                return true;
                            }
                            int current = withdraw(statement, amount, playerUUID);
                            ItemStack pancakes = ItemManager.pancake;
                            pancakes.setAmount(amount);
                            world.dropItemNaturally(player.getLocation(), pancakes);
                            player.sendMessage(String.format(ChatColor.YELLOW+"Withdrew %s%s%s pancakes from your account", ChatColor.GREEN, args[0], ChatColor.YELLOW));
                            player.sendMessage(ChatColor.YELLOW + "New balance = " + ChatColor.GREEN + current);
                        } else {
                            player.sendMessage(ChatColor.RED + "Specify amount");
                        }
                    } catch (InvalidParameterException e) {
                        player.sendMessage(ChatColor.RED + "Invalid args");
                    }
                    return true;
                }
                case ("deposit"), ("dep") -> {
                    try {
                        if (args.length >= 1) {
                            Inventory inventory = player.getInventory();
                            ItemStack[] inventoryContents = inventory.getContents();
                            int amountOfPancakes = 0;
                            for (ItemStack stack : inventoryContents) {
                                if (stack != null) {
                                    if (stack.getItemMeta().equals(ItemManager.pancake.getItemMeta())) {
                                        amountOfPancakes += stack.getAmount();
                                    }
                                }
                            }
                            int amount;
                            ItemStack pancakes = ItemManager.pancake;
                            if (args[0].equalsIgnoreCase("all")) {
                                amount = amountOfPancakes;
                            } else {
                                amount = Integer.parseInt(args[0]);
                                if (amountOfPancakes < amount) {
                                    player.sendMessage(String.format(ChatColor.RED + "Not enough pancakes, You need %s%s%s more", ChatColor.YELLOW, (amount - amountOfPancakes), ChatColor.RED));
                                    return true;
                                }
                            }
                            pancakes.setAmount(amount);
                            inventory.removeItem(pancakes);
                            int current = deposit(statement, amount, playerUUID);
                            player.sendMessage(String.format(ChatColor.YELLOW + "deposited %s%s%s pancakes to your account",ChatColor.GREEN, amount, ChatColor.YELLOW));
                            player.sendMessage(ChatColor.YELLOW + "New balance = " + ChatColor.GREEN + current);
                        } else {
                            player.sendMessage(ChatColor.RED + "Specify amount");
                        }
                    } catch (InvalidParameterException e) {
                        player.sendMessage(ChatColor.RED + "Invalid args");
                    }
                    return true;
                }
                case ("newuser") -> {
                    if (!(player.isOp())) {
                        player.sendMessage(ChatColor.RED + "You don't have the required permission");
                        return true;
                    }
                    String discord;
                    int balance;
                    Player newPlayer;
                    try {
                        switch (args.length) {
                            case (3) -> {
                                discord = args[1];
                                balance = Integer.parseInt(args[2]);
                                newPlayer = Bukkit.getPlayer(args[0]);
                                if (newPlayer == null) {
                                    player.sendMessage(ChatColor.RED + "invalid player");
                                    return true;
                                }
                                newUser(statement, newPlayer.getUniqueId(), newPlayer.getName(), discord, balance);
                            }
                            case (2) -> {
                                discord = args[1];
                                balance = 500;
                                newPlayer = Bukkit.getPlayer(args[0]);
                                if (newPlayer == null) {
                                    player.sendMessage(ChatColor.RED + "invalid player");
                                    return true;
                                }
                                newUser(statement, playerUUID, player.getName(), discord, balance);
                            }
                            case (1) -> {
                                discord = null;
                                balance = 500;
                                newPlayer = Bukkit.getPlayer(args[0]);
                                if (newPlayer == null) {
                                    player.sendMessage(ChatColor.RED + "invalid player");
                                    return true;
                                }
                                newUser(statement, newPlayer.getUniqueId(), newPlayer.getName(), null, balance);
                            }
                            default -> {
                                player.sendMessage(ChatColor.RED + "Invalid amount of args");
                                return true;
                            }
                        }
                        player.sendMessage(ChatColor.GREEN + "New account created");
                        player.sendMessage(ChatColor.GREEN + String.format("UUID = %s \n", playerUUID) +
                                String.format("username = %s \n", player.getName()) +
                                String.format("discord username = %s \n", discord) +
                                String.format("balance = %s", balance));
                    } catch (InvalidParameterException e) {
                        player.sendMessage(ChatColor.RED + "Invalid args");
                    }
                    return true;
                }
                default -> {
                    return false;
                }
            }

        } catch(SQLException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + e.getMessage());
            return false;
        }
    }

    public static void createTable(Statement statement) throws SQLException {
        try {
            statement.executeUpdate("create table if not exists bank (" + "UUID char(36) primary key," + "username varchar,"
                    + "discordUsername varchar," + "pancakes int(15)" + ")");
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("[createTable] "+ChatColor.RED + e.getMessage());
            throw e;
        }
    }
    public static void newUser(Statement statement,
                               UUID uuid,
                               String username,
                               String discordUsername,
                               int initAmount) throws SQLException {
        try{
            statement.executeUpdate("insert into bank values(" +
                    String.format("\"%s\", ", uuid.toString()) +
                    String.format("\"%s\", ", username) +
                    String.format("\"%s\", ", discordUsername) +
                    String.format("%s)", initAmount));
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("[putValues] "+ChatColor.RED + e.getMessage());
            throw e;
        }
    }
    public static int balance(Statement statement, UUID uuid) throws SQLException {
        try {
            ResultSet result;
            if (uuid != null) {
                result = statement.executeQuery(
                        "select pancakes from bank where UUID = \"" + uuid.toString() + "\"");
            } else {
                result = statement.executeQuery("select pancakes from bank");
            }
            return result.getInt("pancakes");
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("[balance] "+ChatColor.RED + e.getMessage());
            throw e;
        }
    }
    public static int withdraw(Statement statement, int amount, UUID uuid) throws SQLException {
        try{
            ResultSet result;
            if (uuid != null) {
                statement.executeUpdate(
                        "update bank" +
                                " set pancakes=pancakes-" + amount + " where UUID=\"" + uuid.toString() + "\"");
            } else {
                statement.executeUpdate(
                        "update bank set pancakes=pancakes-" + amount);
            }
            result = statement.executeQuery("select pancakes from bank");
            return result.getInt("pancakes");
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("[withdraw] "+ChatColor.RED + e.getMessage());
            throw e;
        }
    }
    public static int deposit(Statement statement, int amount, UUID uuid) throws SQLException {
        try{
            ResultSet result;
            if (uuid != null) {
                statement.executeUpdate(
                        "update bank set pancakes=pancakes+" + amount + " where UUID=\"" + uuid.toString() + "\"");
            } else {
                statement.executeUpdate(
                        "update bank set pancakes=pancakes+" + amount);
            }
            result = statement.executeQuery("select pancakes from bank");
            return result.getInt("pancakes");
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("[deposit] "+ChatColor.RED + e.getMessage());
            throw e;
        }
    }
}
