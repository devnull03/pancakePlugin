package io.github.arnav03mehta.events;

import io.github.arnav03mehta.items.ItemManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class Events implements Listener {

    @EventHandler
    public static void onPlayerItemConsume(PlayerItemConsumeEvent event){
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        if (item.getItemMeta().equals(ItemManager.pancake.getItemMeta())){
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1));
        }
    }

    @EventHandler
    public static void onJoin(PlayerJoinEvent event) {
        // TODO: Fix crash caused due to applying the resource pack
        event.getPlayer().setResourcePack("https://www.dropbox.com/s/fmku6wej3re6qf2/pancake%20texture%20pack.zip?dl=1");
    }
}
