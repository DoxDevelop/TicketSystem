package org.doxdevelop.ticketsystem.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.doxdevelop.ticketsystem.TicketSystem;
import org.doxdevelop.ticketsystem.ticketsystem.TicketManager;

public class CommandHandler implements CommandExecutor {

    private final TicketSystem plugin;
    private final TicketManager manager;

    private String tag;
    private int id;

    public CommandHandler(TicketSystem instance, TicketManager manager) {
        plugin = instance;
        this.manager = manager;
        tag = ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("tag"));
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("report")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                    if (args.length > 0) {
                        StringBuilder description = new StringBuilder();
                        for (int i = 1; i < args.length; i++) {
                            description.append(" ").append(args[i]);
                        }
                        manager.createTicket(sender, player.getUniqueId().toString(), args[0], description.toString(), player.getLocation(), plugin.getServer().getServerName(), Bukkit.getOnlinePlayers().size());
                    } else {
                        sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("reportHelp"))));
                        sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("createHelp"))));
                    }
                return true;
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("mustBePlayer"))));
                return false;
            }
        } else if (cmd.getName().equalsIgnoreCase("ticket")) {
            if (sender instanceof Player) {
                return handleTicket(sender, args);
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("mustBePlayer"))));
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean handleTicket(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("view")) {
                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("open")) {
                        if (player.hasPermission("ticket.view.open")) {
                            manager.printOpenTickets(sender);
                        } else {
                            sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noPermission"))));
                        }
                    } else if (args[1].equalsIgnoreCase("assigned")) {
                        if (args.length > 2) {
                            if (args[2].equalsIgnoreCase("all")) {
                                if (player.hasPermission("ticket.view.assigned.all")) {
                                    manager.printAllAssignedTickets(sender);
                                } else {
                                    sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noPermission"))));
                                }
                            }
                        } else {
                            if (player.hasPermission("ticket.view.assigned")) {
                                manager.printAssignedTickets(sender, player.getUniqueId().toString());
                            } else {
                                sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noPermission"))));
                            }
                        }
                    } else if (args[1].equalsIgnoreCase("closed")) {
                        if (args.length > 2) {
                            if (args[2].equalsIgnoreCase("all")) {
                                if (player.hasPermission("ticket.view.closed.all")) {
                                    manager.printAllClosedTickets(sender);
                                } else {
                                    sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noPermission"))));
                                }
                            }
                        } else {
                            if (player.hasPermission("ticket.view.closed")) {
                                manager.printClosedTickets(sender, player.getUniqueId().toString());
                            } else {
                                sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noPermission"))));
                            }
                        }
                    }
                } else {
                    sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketHelp1"))));
                }
            } else if (args[0].equalsIgnoreCase("info")) {
                if (player.hasPermission("ticket.info")) {
                    if (args.length > 1) {
                        id = Integer.parseInt(args[1]);
                        manager.printTicketInfo(sender, id);
                    } else {
                        sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketInfoError"))));
                    }
                } else {
                    sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noPermission"))));
                }
            } else if (args[0].equalsIgnoreCase("comment")) {
                if (player.hasPermission("ticket.comments")) {
                    if (args.length > 1) {
                        if (args[1].equalsIgnoreCase("add")) {
                            id = Integer.parseInt(args[2]);
                            StringBuilder comment = new StringBuilder();
                            for (int i = 3; i < args.length; i++) {
                                comment.append(" ").append(args[i]);
                            }
                            manager.createComment(sender, player.getUniqueId().toString(), comment.toString(), id);
                        } else if (args[1].equalsIgnoreCase("view")) {
                            id = Integer.parseInt(args[2]);
                            manager.printComments(sender, id);
                        }
                    }
                }
            } else if (args[0].equalsIgnoreCase("claim")) {
                if (player.hasPermission("ticket.admin")) {
                    if (args.length > 1) {
                        id = Integer.parseInt(args[1]);
                        manager.setTicketAssigned(sender, player.getUniqueId().toString(), id);
                    } else {
                        sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketClaimError"))));
                    }
                } else {
                    sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noPermission"))));
                }
            } else if (args[0].equalsIgnoreCase("close")) {
                if (player.hasPermission("ticket.admin")) {
                    if (args.length > 1) {
                        id = Integer.parseInt(args[1]);
                        manager.setTicketClosed(sender, player.getUniqueId().toString(), id);
                    } else {
                        sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketCloseError"))));
                    }
                } else {
                    sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noPermission"))));
                }
            } else if (args[0].equalsIgnoreCase("teleport")) {
                if (player.hasPermission("ticket.teleport")) {
                    if (args.length > 1) {
                        id = Integer.parseInt(args[1]);
                        manager.teleportPlayer(sender, id);
                    } else {
                        sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketTeleportError"))));
                    }
                } else {
                    sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noPermission"))));
                }
            } else if (args[0].equalsIgnoreCase("stats")) {
                if (player.hasPermission("ticket.stats")) {
                    manager.printTicketStats(sender);
                } else {
                    sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noPermission"))));
                }
            } else if (args[0].equalsIgnoreCase("unclaim")) {
                if (player.hasPermission("ticket.admin")) {
                    manager.setUnclaimed(sender, id);
                }
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (player.hasPermission("ticket.reload")) {
                    plugin.reloadConfig();
                    plugin.reloadMessages();
                    sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("ticketReload")));
                } else {
                    sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noPermission"))));
                }
            }
        } else {
            sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketHelpTitle"))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketHelp1"))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketHelp2"))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketHelp3"))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketHelp4"))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketHelp5"))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketHelp6"))));
    }

    private String checkMessages(String message) {
        if (message.contains("%id")) {
            return message.replace("%id", "" + id);
        }
        return message;
    }

}
