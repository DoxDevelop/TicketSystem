package org.doxdevelop.ticketsystem.ticketsystem;

import static java.lang.Math.round;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import org.doxdevelop.ticketsystem.TicketSystem;
import org.doxdevelop.ticketsystem.database.Queries;
import org.doxdevelop.ticketsystem.enums.TicketStates;

public class TicketManager {

    private TicketSystem plugin;
    private Queries query;

    private int currentTicketId = 1;
    private String tag;

    private int ticketId;
    private int maxTickets;

    private ArrayList<Ticket> allTickets;
    private ArrayList<Ticket> ticketsToUpdate;
    private ArrayList<Ticket> commentsToUpdate;

    private HashMap<String, Integer> claimedAmount;

    public TicketManager(TicketSystem plugin, Queries query, int maxTickets) {
        this.plugin = plugin;
        this.query = query;
        this.maxTickets = maxTickets;
        allTickets = new ArrayList<>();
        ticketsToUpdate = new ArrayList<>();
        commentsToUpdate = new ArrayList<>();
        claimedAmount = new HashMap<>();

        tag = ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("tag"));
    }

    public void createTicket(CommandSender sender, String player, String reason, String description, Location l, String server, int playerAmount) {
        String world = l.getWorld().toString();
        String[] tempWorld = world.split("=");
        world = tempWorld[1].substring(0, tempWorld[1].length() - 1);

        long x = round(l.getX());
        long y = round(l.getY());
        long z = round(l.getZ());
        String location = "X:" + x + " Y:" + y + " Z:" + z;

        Ticket newTicket = new Ticket(currentTicketId, player, reason, description, location, world, server, playerAmount);
        ticketId = newTicket.getTicketId();

        allTickets.add(newTicket);
        ticketsToUpdate.add(newTicket);
        currentTicketId++;
        sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("createTicket"))));

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("ticket.use")) {
                p.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("adminUpdate"))));
            }
        }
    }

    private Ticket createTicket(String player, TicketStates ticketState, String reason, String description, String location, Date createdDate, String world, String server, int playerAmount) {
        Ticket newTicket = new Ticket(currentTicketId, player, reason, description, location, world, server, playerAmount);
        newTicket.setState(ticketState);
        newTicket.setDateCreated(createdDate);

        allTickets.add(newTicket);
        currentTicketId++;
        return newTicket;
    }

    public void createComment(CommandSender sender, String player, String comment, int id) {
        Ticket ticket = getTicket(id);
        if (ticket != null) {
            ticket.addComment(player, comment);
            commentsToUpdate.add(ticket);
            ticketId = ticket.getTicketId();
            sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("createComment"))));
        }
    }

    @SuppressWarnings("deprecation")
    public void startTask() {
        long delay = plugin.getConfig().getLong("updateTime") * 20;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new BukkitRunnable() {
            public void run() {
                boolean successful = false;
                int noneUpdated = 0;
                if (!ticketsToUpdate.isEmpty()) {
                    Iterator<Ticket> it = ticketsToUpdate.iterator();
                    while (it.hasNext()) {
                        Ticket ticket = it.next();
                        if (ticket.getState().equals(TicketStates.OPEN)) {
                            try {
                                if (!query.checkIfTicketExists(ticket.getTicketId())) {
                                    successful = query.insertTicket(ticket.getReportingPlayer(), ticket.getReason(), ticket.getDescription(), ticket.getPlayerAmount(), ticket.getWorld(), ticket.getDateCreated().getTime(), ticket.getLocation(), ticket.getServer());
                                } else {
                                    successful = query.setUnclaimed(ticket.getTicketId());
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        } else if (ticket.getState().equals(TicketStates.ASSIGNED)) {
                            successful = query.setAssigned(ticket.getAssignedTo(), ticket.getTicketId());
                        } else if (ticket.getState().equals(TicketStates.CLOSED)) {
                            successful = query.setClosed(ticket.getClosedBy(), ticket.getAssignedTo(), ticket.getTicketId(), ticket.getClosedDate().getTime());
                        }
                        if (!successful) {
                            Bukkit.getLogger().severe("ERROR");
                        }
                        it.remove();
                        noneUpdated = 1;
                    }
                }
                if (!commentsToUpdate.isEmpty()) {
                    Iterator<Ticket> it = commentsToUpdate.iterator();
                    while (it.hasNext()) {
                        Ticket ticket = it.next();
                        Map<Integer, Map<String, String>> commentsMap = ticket.getComments();
                        for (Entry<Integer, Map<String, String>> entryComments : commentsMap.entrySet()) {
                            Map<String, String> comments = entryComments.getValue();
                            for (Entry<String, String> entry : comments.entrySet()) {
                                try {
                                    if (!query.checkIfCommentExists(entry.getValue(), entry.getKey(), ticket.getTicketId())) {
                                        query.insertComment(entry.getValue(), entry.getKey(), ticket.getTicketId());
                                        noneUpdated = 1;
                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        it.remove();
                    }
                }
                int currentTickets = allTickets.size();
                allTickets.clear();
                try {
                    loadInTickets();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                int newTickets = allTickets.size();
                if (newTickets > currentTickets) {
                    noneUpdated = 2;
                }
                if (noneUpdated == 0) {
                    query.keepConnectionAlive();
                }
            }
        }, 0L, delay);

    }

    public void setTicketAssigned(CommandSender sender, String player, int id) {
        Ticket ticket = getTicket(id);
        ticketId = id;
        int amount = 0;
        if (claimedAmount.containsKey(player)) {
            amount = claimedAmount.get(player);
        }
        if (ticket != null) {
            if (ticket.getState().equals(TicketStates.OPEN)) {
                if (amount < maxTickets) {
                    ticket.setAssignedTo(player);
                    ticket.setState(TicketStates.ASSIGNED);
                    ticketsToUpdate.add(ticket);
                    increaseClaimedAmount(player);
                    sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("claimTicket"))));
                } else {
                    sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("maxTickets"))));
                }
            } else {
                sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("alreadyClaimed"))));
            }
        } else {
            sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noId"))));
        }
    }

    public void setTicketClosed(CommandSender sender, String player, int id) {
        Ticket ticket = getTicket(id);
        ticketId = id;
        if (ticket != null) {
            if (ticket.getState().equals(TicketStates.ASSIGNED)) {
                boolean ableToClose = false;
                if (!plugin.getConfig().getBoolean("allowotherstoclose")) {
                    if (ticket.getAssignedTo().equals(player)) {
                        ableToClose = true;
                    }
                }
                if (ableToClose) {
                    ticket.setClosedBy(player);
                    ticket.setClosedDate(new Date());
                    ticket.setState(TicketStates.CLOSED);
                    ticketsToUpdate.add(ticket);
                    decreaseClaimedAmount(ticket.getAssignedTo());
                    sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("closeTicket"))));
                    if (plugin.getConfig().getBoolean("closenotice")) {
                        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                            if (p.hasPermission("ticket.admin")) {
                                p.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkPlayer(checkMessages(plugin.messageData.get("closeNotice")), player)));
                            }
                        }
                    }
                }
            } else {
                sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("alreadyClosed"))));
            }
        } else {
            sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noId"))));
        }
    }

    public void setUnclaimed(CommandSender sender, int id) {
        Ticket ticket = getTicket(id);
        ticketId = id;
        if (ticket != null) {
            if (ticket.getState().equals(TicketStates.ASSIGNED)) {
                decreaseClaimedAmount(ticket.getAssignedTo());
                ticket.setAssignedTo(null);
                ticket.setState(TicketStates.OPEN);
                ticketsToUpdate.add(ticket);
                sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("unclaimTicket"))));
            } else {
                sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("alreadyUnclaimed"))));
            }
        } else {
            sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noId"))));
        }
    }

    public void printTicketInfo(CommandSender sender, int id) {
        Ticket ticket = getTicket(id);
        ticketId = id;
        if (ticket != null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketTitle"))));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketId"))) + ticket.getTicketId());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketStatus"))) + ticket.getState().toString());

            if (ticket.getState().equals(TicketStates.ASSIGNED)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketAssignedTo"))) + getName(ticket.getAssignedTo()));
            } else if (ticket.getState().equals(TicketStates.CLOSED)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketAssignedTo"))) + getName(ticket.getAssignedTo()));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketClosedBy"))) + getName(ticket.getClosedBy()));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketClosedDate"))) + ticket.getClosedDate().toString());
            }
            sender.sendMessage("");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketReportingPlayer"))) + getName(ticket.getReportingPlayer()));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketReason"))) + ticket.getReason());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketDescription"))) + ticket.getDescription());
            if (plugin.getConfig().getBoolean("players")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketAmount"))) + ticket.getPlayerAmount());
            }
            if (plugin.getConfig().getBoolean("bungeecord")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketServer"))) + ticket.getServer());
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketDateCreated"))) + ticket.getDateCreated().toString());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketLocation"))) + ticket.getLocation() + " World: " + ticket.getWorld());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkSize(checkMessages(plugin.messageData.get("ticketComments")), ticket.getComments().size())));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketFooter"))));
        } else {
            sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noId"))));
        }
    }

    public void teleportPlayer(CommandSender sender, int id) {
        Ticket ticket = getTicket(id);
        if (ticket != null) {
            Player p = (Player) sender;
            String location = ticket.getLocation();
            String[] temp = location.split(" ");
            String[] tempx = temp[0].split(":");
            String[] tempy = temp[1].split(":");
            String[] tempz = temp[2].split(":");
            Double x = Double.parseDouble(tempx[1].trim());
            Double y = Double.parseDouble(tempy[1].trim());
            Double z = Double.parseDouble(tempz[1].trim());
            Location l = new Location(Bukkit.getWorld(ticket.getWorld()), x, y, z);
            p.teleport(l);
            p.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("teleport"))));
        }
    }

    public void printTicketStats(CommandSender sender) {
        int open = 0;
        int assigned = 0;
        int closed = 0;
        for (Ticket ticket : allTickets) {
            if (ticket.getState().equals(TicketStates.OPEN)) {
                open++;
            } else if (ticket.getState().equals(TicketStates.ASSIGNED)) {
                assigned++;
            } else if (ticket.getState().equals(TicketStates.CLOSED)) {
                closed++;
            }
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("statsTitle"))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("statsOpen"))) + open);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("statsAssigned"))) + assigned);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("statsClosed"))) + closed);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("statsFooter"))));
    }

    public void printOpenTickets(CommandSender sender) {
        ArrayList<Ticket> tickets = getTicketsOpen();
        StringBuilder view = new StringBuilder();
        if (!tickets.isEmpty()) {
            for (Ticket ticket : tickets) {
                view.append(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("openId")))).append(ticket.getTicketId()).append("\n");
            }
        } else {
            view = new StringBuilder(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("openNoTickets"))));
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("openTitle"))));
        sender.sendMessage(view.toString());
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("openInfo"))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("openClaim"))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("openFooter"))));
    }

    public void printAssignedTickets(CommandSender sender, String player) {
        ArrayList<Ticket> tickets = getTicketsAssignedForPlayer(player);
        StringBuilder view = new StringBuilder();
        if (!tickets.isEmpty()) {
            for (Ticket ticket : tickets) {
                view.append(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("assignedId")))).append(ticket.getTicketId()).append("\n");
            }
        } else {
            view = new StringBuilder(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("assignedNoTickets"))));
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("assignedTitle"))));
        sender.sendMessage(view.toString());
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("assignedInfo"))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("assignedFooter"))));
    }

    public void printAllAssignedTickets(CommandSender sender) {
        ArrayList<Ticket> tickets = getTicketsAssignedForAllPlayers();
        StringBuilder view = new StringBuilder();
        if (!tickets.isEmpty()) {
            for (Ticket ticket : tickets) {
                ticketId = ticket.getTicketId();
                view.append(ChatColor.translateAlternateColorCodes('&', checkPlayer(checkMessages(plugin.messageData.get("allAssignedId")), ticket.getAssignedTo()))).append("\n");
            }
        } else {
            view = new StringBuilder(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("allAssignedNoTickets"))));
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("allAssignedTitle"))));
        sender.sendMessage(view.toString());
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("allAssignedInfo"))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("allAssignedFooter"))));
    }

    public void printClosedTickets(CommandSender sender, String player) {
        ArrayList<Ticket> tickets = getTicketsClosedForPlayer(player);
        StringBuilder view = new StringBuilder();
        if (!tickets.isEmpty()) {
            for (Ticket ticket : tickets) {
                view.append(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("closedId")))).append(ticket.getTicketId()).append("\n");
            }
        } else {
            view = new StringBuilder(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("closedNoTickets"))));
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("closedTitle"))));
        sender.sendMessage(view.toString());
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("closedInfo"))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("closedFooter"))));
    }

    public void printAllClosedTickets(CommandSender sender) {
        ArrayList<Ticket> tickets = getTicketsClosedForAllPlayers();
        StringBuilder view = new StringBuilder();
        if (!tickets.isEmpty()) {
            for (Ticket ticket : tickets) {
                ticketId = ticket.getTicketId();
                view.append(ChatColor.translateAlternateColorCodes('&', checkPlayer(checkMessages(plugin.messageData.get("allClosedId")), ticket.getClosedBy()))).append("\n");
            }
        } else {
            view = new StringBuilder(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("allClosedNoTickets"))));
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("allClosedTitle"))));
        sender.sendMessage(view.toString());
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("allClosedInfo"))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("allClosedFooter"))));
    }

    public void printComments(CommandSender sender, int id) {
        Ticket ticket = getTicket(id);
        if (ticket != null) {
            StringBuilder commentsList = new StringBuilder();
            ticketId = ticket.getTicketId();
            Map<Integer, Map<String, String>> commentsMap = ticket.getComments();
            if (!commentsMap.isEmpty()) {
                for (int i = 0; i < commentsMap.size(); i++) {
                    Map<String, String> comments = commentsMap.get(i);
                    for (Entry<String, String> entry : comments.entrySet()) {
                        String uuid = entry.getValue();
                        String comment = entry.getKey();
                        commentsList.append(ChatColor.translateAlternateColorCodes('&', checkComment(checkPlayer(checkMessages(plugin.messageData.get("comment")), uuid), comment))).append("\n");
                    }
                }
            } else {
                commentsList = new StringBuilder(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noComments"))));
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("commentsTitle"))));
            sender.sendMessage(commentsList.toString());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("commentsFooter"))));
        } else {
            sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noId"))));
        }
    }

    public int getOpenTickets() {
        return getTicketsOpen().size();
    }

    public void loadInTickets() throws SQLException {
        ResultSet rs = query.loadAllTickets();
        if (rs != null) {
            while (rs.next()) {
                String id = rs.getString("ticket_id");
                String player = rs.getString("uuid");
                TicketStates ticketState = TicketStates.valueOf(rs.getString("status"));
                String reason = rs.getString("reason");
                String description = rs.getString("description");
                Date createdDate = new Date(rs.getLong("created_date"));
                String location = rs.getString("location");
                String world = rs.getString("world");
                String server = rs.getString("server_name");
                int playerAmount = rs.getInt("player_amount");

                currentTicketId = Integer.parseInt(id);

                Ticket ticket = createTicket(player, ticketState, reason, description, location, createdDate, world, server, playerAmount);

                ResultSet set = query.loadAllComments(ticket.getTicketId());
                if (set != null) {
                    while (set.next()) {
                        String uuid = set.getString("uuid");
                        String comment = set.getString("comment");
                        ticket.addComment(uuid, comment);
                    }
                }

                if (ticketState.equals(TicketStates.ASSIGNED)) {
                    String assignedTo = rs.getString("assigned_to");
                    ticket.setAssignedTo(assignedTo);
                    increaseClaimedAmount(assignedTo);
                } else if (ticketState.equals(TicketStates.CLOSED)) {
                    String closedBy = rs.getString("closed_by");
                    Date closedDate = rs.getDate("closed_date");
                    ticket.setClosedBy(closedBy);
                    ticket.setClosedDate(closedDate);
                }
            }
        }
    }

    public void onDisableUpdate() {
        if (ticketsToUpdate.size() > 0) {
            for (Ticket ticket : ticketsToUpdate) {
                if (ticket.getState().equals(TicketStates.OPEN)) {
                    try {
                        if (query.checkIfTicketExists(ticket.getTicketId())) {
                            query.insertTicket(ticket.getReportingPlayer(), ticket.getReason(), ticket.getDescription(), ticket.getPlayerAmount(), ticket.getWorld(), ticket.getDateCreated().getTime(), ticket.getLocation(), ticket.getServer());
                        } else {
                            query.setUnclaimed(ticket.getTicketId());
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        Bukkit.getLogger().severe("There was a problem with updating 1 ticket to the database");
                    }
                } else if (ticket.getState().equals(TicketStates.ASSIGNED)) {
                    query.setAssigned(ticket.getAssignedTo(), ticket.getTicketId());
                } else if (ticket.getState().equals(TicketStates.CLOSED)) {
                    query.setClosed(ticket.getClosedBy(), ticket.getAssignedTo(), ticket.getTicketId(), ticket.getClosedDate().getTime());
                }
            }
        }
        if (!commentsToUpdate.isEmpty()) {
            Iterator<Ticket> it = commentsToUpdate.iterator();
            while (it.hasNext()) {
                Ticket ticket = it.next();
                Map<Integer, Map<String, String>> commentsMap = ticket.getComments();
                for (Entry<Integer, Map<String, String>> entryComments : commentsMap.entrySet()) {
                    Map<String, String> comments = entryComments.getValue();
                    for (Entry<String, String> entry : comments.entrySet()) {
                        try {
                            if (!query.checkIfCommentExists(entry.getValue(), entry.getKey(), ticket.getTicketId())) {
                                query.insertComment(entry.getValue(), entry.getKey(), ticket.getTicketId());
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
                it.remove();
            }
        }
    }

    private Ticket getTicket(int id) {
        Ticket selectedTicket = null;
        for (Ticket ticket : allTickets) {
            if (ticket.getTicketId() == id) {
                selectedTicket = ticket;
                break;
            }
        }
        return selectedTicket;
    }

    private ArrayList<Ticket> getTicketsOpen() {
        ArrayList<Ticket> tickets = new ArrayList<>();
        for (Ticket ticket : allTickets) {
            if (ticket.getState().equals(TicketStates.OPEN)) {
                tickets.add(ticket);
            }
        }
        return tickets;
    }

    private ArrayList<Ticket> getTicketsAssignedForPlayer(String player) {
        ArrayList<Ticket> tickets = new ArrayList<>();
        for (Ticket ticket : allTickets) {
            if (ticket.getState().equals(TicketStates.ASSIGNED)) {
                if (ticket.getAssignedTo().equals(player)) {
                    tickets.add(ticket);
                }
            }
        }
        return tickets;
    }

    private ArrayList<Ticket> getTicketsAssignedForAllPlayers() {
        ArrayList<Ticket> tickets = new ArrayList<>();
        for (Ticket ticket : allTickets) {
            if (ticket.getState().equals(TicketStates.ASSIGNED)) {
                tickets.add(ticket);
            }
        }
        return tickets;
    }

    private ArrayList<Ticket> getTicketsClosedForPlayer(String player) {
        ArrayList<Ticket> tickets = new ArrayList<>();
        for (Ticket ticket : allTickets) {
            if (ticket.getState().equals(TicketStates.CLOSED)) {
                if (ticket.getClosedBy().equals(player)) {
                    tickets.add(ticket);
                }
            }
        }
        return tickets;
    }

    private ArrayList<Ticket> getTicketsClosedForAllPlayers() {
        ArrayList<Ticket> tickets = new ArrayList<>();
        for (Ticket ticket : allTickets) {
            if (ticket.getState().equals(TicketStates.CLOSED)) {
                tickets.add(ticket);
            }
        }
        return tickets;
    }

    private String checkMessages(String message) {
        if (message.contains("%id")) {
            return message.replace("%id", "" + ticketId);
        } else {
            return message;
        }
    }

    private String checkSize(String message, int size) {
        if (message.contains("%amount")) {
            return message.replace("%amount", "" + size);
        } else {
            return message;
        }
    }

    private String checkPlayer(String message, String player) {
        if (message.contains("%player")) {
            return message.replace("%player", getName(player));
        } else {
            return message;
        }
    }

    private String checkComment(String message, String comment) {
        if (message.contains("%comment")) {
            return message.replace("%comment", comment);
        } else {
            return message;
        }
    }

    private void increaseClaimedAmount(String uuid) {
        if (claimedAmount.containsKey(uuid)) {
            Integer amount = claimedAmount.get(uuid);
            amount++;
            claimedAmount.put(uuid, amount);
        } else {
            claimedAmount.put(uuid, 1);
        }
    }

    private void decreaseClaimedAmount(String uuid) {
        if (claimedAmount.containsKey(uuid)) {
            Integer amount = claimedAmount.get(uuid);
            amount--;
            claimedAmount.put(uuid, amount);
        }
    }

    private String getName(String uuid) {
        return Bukkit.getServer().getOfflinePlayer(UUID.fromString(uuid)).getName();
    }

}
 