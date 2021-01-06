package io.github.arnav03mehta.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemManager {

    public static ItemStack pancake;

    public static void init(){
        createItem();
    }

    private static void createItem() {
        ItemStack item = new ItemStack(Material.COOKIE);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(105527108);
        meta.setDisplayName(ChatColor.YELLOW + "Pancake");
        item.setItemMeta(meta);
        pancake = item;
    }
}
