package org.doxdevelop.ticketsystem;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.doxdevelop.ticketsystem.command.CommandHandler;
import org.doxdevelop.ticketsystem.database.MySQL;
import org.doxdevelop.ticketsystem.database.Queries;
import org.doxdevelop.ticketsystem.listeners.PlayerJoinListener;
import org.doxdevelop.ticketsystem.ticketsystem.TicketManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class TicketSystem extends JavaPlugin {

    public Map<String, String> messageData = new HashMap<>();
    private Connection conn = null;
    private Messages messages;
    private TicketManager manager;

    public void onEnable() {
        saveDefaultConfig();

        messages = new Messages(this);
        messageData = messages.getMessageData();

        String hostname = this.getConfig().getString("hostname");
        String port = this.getConfig().getString("port");
        String database = this.getConfig().getString("database");
        String username = this.getConfig().getString("username");
        String password = this.getConfig().getString("password");
        MySQL sql = new MySQL(hostname, port, database, username, password);
        try {
            conn = sql.openConnection();
            createMySQLTables();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        manager = new TicketManager(this, new Queries(conn), this.getConfig().getInt("maxtickets"));
        try {
            manager.loadInTickets();
        } catch (SQLException e) {
            getLogger().severe("ERROR!");
            e.printStackTrace();
        }
        this.getCommand("report").setExecutor(new CommandHandler(this, manager));
        this.getCommand("ticket").setExecutor(new CommandHandler(this, manager));
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new PlayerJoinListener(this, manager), this);
        manager.startTask();
    }

    @Override
    public void onDisable() {
        manager.onDisableUpdate();
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        getLogger().info("TicketSystem wurde deaktiviert.");
    }

    public void reloadMessages() {
        messages.loadMessages();
    }

    private void createMySQLTables() {
        Queries query = new Queries(conn);
        boolean created = query.createMySQLTable();
        if (!created) {
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

}
