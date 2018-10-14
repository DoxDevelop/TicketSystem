package org.doxdevelop.ticketsystem.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import org.doxdevelop.ticketsystem.ticketsystem.TicketManager;
import org.doxdevelop.ticketsystem.TicketSystem;

public class PlayerJoinListener implements Listener {

    private TicketSystem plugin;
    private TicketManager manager;

    private String tag;

    public PlayerJoinListener(TicketSystem instance, TicketManager manager) {
        plugin = instance;
        this.manager = manager;
        tag = ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("tag"));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (p.hasPermission("ticket.use")) {
            if (plugin.getConfig().getBoolean("adminmessages")) {
                int open = manager.getOpenTickets();
                if (open > 0) {
                    p.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', translateAmount(plugin.messageData.get("loginNotice"), open)));
                }
            }
        }
    }

    private String translateAmount(String message, int amount) {
        if ("%amount".contains(message)) {
            return message.replace("%amount", "" + amount);
        }
        return message;
    }

}
