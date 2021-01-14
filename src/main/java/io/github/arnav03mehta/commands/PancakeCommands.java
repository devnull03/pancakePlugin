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
import java.text.NumberFormat;
import java.util.ArrayList;
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

        try {
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            createTable(statement);

            switch (label) {
                case ("balance"), ("bal") -> {
                    UUID playerUUID;
                    if (args.length >= 1) {
                        Player newPlayer = Bukkit.getPlayer(args[0]);
                        if (newPlayer == null) {
                            sender.sendMessage(ChatColor.RED + "invalid player");
                            return true;
                        }
                        playerUUID = newPlayer.getUniqueId();
                    } else if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Invalid amount of args");
                        return true;
                    } else {
                        playerUUID = ((Player) sender).getUniqueId();
                    }
                    NumberFormat format = NumberFormat.getCurrencyInstance();
                    sender.sendMessage(ChatColor.YELLOW + "Current balance = " + ChatColor.GREEN + format.format(balance(statement, playerUUID)));
                    return true;
                }
                case ("wd"), ("withdraw") -> {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Player only command");
                        return false;
                    }
                    Player player = (Player) sender;
                    World world = player.getWorld();
                    UUID playerUUID = player.getUniqueId();

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
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Player only command");
                        return false;
                    }
                    Player player = (Player) sender;
                    UUID playerUUID = player.getUniqueId();

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
                case ("addbalance") -> {
                    if (!(sender.isOp())) {
                        sender.sendMessage(ChatColor.RED + "You don't have the required permission");
                        return true;
                    }
                    switch (args.length) {
                        case (1) -> sender.sendMessage(ChatColor.RED + "Amount not specified");
                        case (2) -> {
                            int amount;
                            try {
                                amount = Integer.parseInt(args[1]);
                            } catch (NumberFormatException e) {
                                sender.sendMessage(ChatColor.RED + "Invalid amount");
                                return true;
                            }
                            Player selectedPlayer = Bukkit.getPlayer(args[0]);
                            if (selectedPlayer == null){
                                sender.sendMessage(ChatColor.RED + "Invalid player");
                                return true;
                            }
                            int current = deposit(statement, amount, selectedPlayer.getUniqueId());
                            sender.sendMessage(String.format(ChatColor.YELLOW + "deposited %s%s%s pancakes to %s's account",ChatColor.GREEN, amount, ChatColor.YELLOW, selectedPlayer.getName()));
                            sender.sendMessage(ChatColor.YELLOW + "New balance = " + ChatColor.GREEN + current);

                        }
                        default -> sender.sendMessage(ChatColor.RED + "Invalid amount of arguments");
                    }

                    return true;
                }
                case ("adduser") -> {
                    if (!(sender.isOp())) {
                        sender.sendMessage(ChatColor.RED + "You don't have the required permission");
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
                            }
                            case (2) -> {
                                discord = args[1];
                                balance = 500;
                            }
                            case (1) -> {
                                discord = null;
                                balance = 500;
                            }
                            default -> {
                                sender.sendMessage(ChatColor.RED + "Invalid amount of args");
                                return true;
                            }
                        }
                        newPlayer = Bukkit.getPlayer(args[0]);
                        if (newPlayer == null) {
                            sender.sendMessage(ChatColor.RED + "invalid player");
                            return true;
                        }
                        newUser(statement, newPlayer.getUniqueId(), newPlayer.getName(), discord, balance);

                        sender.sendMessage(ChatColor.GREEN + "New account created");
                        sender.sendMessage(ChatColor.GREEN + String.format("UUID = %s \n", newPlayer.getUniqueId()) +
                                String.format("username = %s \n", newPlayer.getName()) +
                                String.format("discord username = %s \n", discord) +
                                String.format("balance = %s", balance));
                    } catch (InvalidParameterException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid args");
                    }
                    return true;
                }
                case ("removeuser") -> {
                    if (!(sender.isOp())) {
                        sender.sendMessage(ChatColor.RED + "You don't have the required permission");
                        return true;
                    }
                    try {
                        if (!(args.length == 1)) {
                            sender.sendMessage(ChatColor.RED + "Invalid amount of args");
                            return true;
                        }
                        Player newPlayer = Bukkit.getPlayer(args[0]);
                        if (newPlayer == null) {
                            sender.sendMessage(ChatColor.RED + "invalid player");
                            return true;
                        }
                        removeUser(statement, newPlayer.getUniqueId());

                        sender.sendMessage(ChatColor.GREEN + "Account removed");

                    } catch (InvalidParameterException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid args");
                    }
                    return true;
                }

                case ("lb"), ("leaderboard") ->{
                    sender.sendMessage(leaderBoard(statement, sender));
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
    public static void removeUser(Statement statement, UUID uuid) throws SQLException {
        try{
            statement.executeUpdate("delete from bank where UUID = \"" + uuid.toString() + "\"");
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("[removeUser] "+ChatColor.RED + e.getMessage());
            throw e;
        }
    }

    public static int balance(Statement statement, UUID uuid) throws SQLException {
        try {
            ResultSet result = statement.executeQuery("select pancakes from bank where UUID = \"" + uuid.toString() + "\"");
            return result.getInt("pancakes");
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("[balance] "+ChatColor.RED + e.getMessage());
            throw e;
        }
    }
    public static int withdraw(Statement statement, int amount, UUID uuid) throws SQLException {
        try{
            statement.executeUpdate("update bank" +
                                " set pancakes=pancakes-" + amount + " where UUID=\"" + uuid.toString() + "\"");
            return balance(statement, uuid);
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("[withdraw] "+ChatColor.RED + e.getMessage());
            throw e;
        }
    }
    public static int deposit(Statement statement, int amount, UUID uuid) throws SQLException {
        try{
            statement.executeUpdate("update bank set pancakes=pancakes+" + amount + " where UUID=\"" + uuid.toString() + "\"");
            return balance(statement, uuid);
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("[deposit] "+ChatColor.RED + e.getMessage());
            throw e;
        }
    }
    public static String leaderBoard(Statement statement, CommandSender sender) throws SQLException {
        try {
            ResultSet result = statement.executeQuery("select username, discordUsername, pancakes from bank order by pancakes desc");
            int usernameLength = 0, discordLength = 0, pancakeLength = 0;
            ArrayList<String> usernameColumn = new ArrayList<>(), discordColumn = new ArrayList<>();
            ArrayList<Integer> pancakeColumn = new ArrayList<>();
            
            for (int i = 0; i < 10; i++) {
                if (!(result.next())) break;

                usernameColumn.add(result.getString("username"));
                if (usernameColumn.get(i).length() > usernameLength) {
                    usernameLength = usernameColumn.get(i).length();
                }
                discordColumn.add(result.getString("discordUsername"));
                if (discordColumn.get(i).length() > discordLength) {
                    discordLength = discordColumn.get(i).length();
                }
                pancakeColumn.add(result.getInt("pancakes"));
                if (Integer.toString(pancakeColumn.get(i)).length() > pancakeLength) {
                    pancakeLength = Integer.toString(pancakeColumn.get(i)).length();
                }
            }
            StringBuilder lb = new StringBuilder("Leaderboard: \n");
            if (sender instanceof Player) {
                lb.append(ChatColor.YELLOW).append("Username").append(" ".repeat(Math.max((usernameLength - 7), 0))).append(String.format("%s|%s Discord", ChatColor.WHITE, ChatColor.YELLOW)).append(" ".repeat(Math.max((discordLength - 5), 0))).append(String.format("%s|%s Pancakes", ChatColor.WHITE, ChatColor.YELLOW)).append(" ".repeat(Math.max((pancakeLength - 6), 0))).append("\n");
                lb.append(ChatColor.WHITE).append("-".repeat(
                        usernameLength + discordLength + pancakeLength + 6
                )).append(ChatColor.YELLOW).append("\n");
            } else {
                lb.append(ChatColor.YELLOW).append("Username").append(" ".repeat(Math.max((usernameLength - 7), 0))).append(String.format("%s|%s Discord", ChatColor.WHITE, ChatColor.YELLOW)).append(" ".repeat(Math.max((discordLength - 6), 0))).append(String.format("%s|%s Pancakes", ChatColor.WHITE, ChatColor.YELLOW)).append(" ".repeat(Math.max((pancakeLength - 7), 0))).append("\n");
                lb.append(ChatColor.WHITE).append("-".repeat(
                        usernameLength + discordLength + pancakeLength + 10
                )).append(ChatColor.YELLOW).append("\n");
            }

            for (int i = 0; i < usernameColumn.size(); i++) {
                lb.append("  ").append(usernameColumn.get(i)).append(" ".repeat(
                        usernameLength - usernameColumn.get(i).length()
                )).append(String.format("%s | %s", ChatColor.WHITE, ChatColor.YELLOW)).append(discordColumn.get(i)).append(" ".repeat(
                        discordLength - discordColumn.get(i).length()
                )).append(String.format("%s | %s", ChatColor.WHITE, ChatColor.YELLOW)).append(pancakeColumn.get(i)).append(" ".repeat(
                        pancakeLength - Integer.toString(pancakeColumn.get(i)).length()
                )).append("\n");
            }
            lb.append(" -");
            return lb.toString();
        } catch (SQLException e) {
            // System.err.println("[leaderboard] " + e.getMessage());
            Bukkit.getConsoleSender().sendMessage("[leaderboard] "+ChatColor.RED + e.getMessage());
            throw e;
        }
    }
}










