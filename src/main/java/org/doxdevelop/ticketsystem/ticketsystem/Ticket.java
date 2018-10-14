package org.doxdevelop.ticketsystem.ticketsystem;

import com.google.common.collect.Maps;
import lombok.Data;
import org.doxdevelop.ticketsystem.enums.TicketStates;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
class Ticket {

    private int ticketId;
    private TicketStates state;
    private String assignedTo;
    private String closedBy;
    private String reportingPlayer;
    private String reason;
    private String description;
    private Date dateCreated;
    private Date closedDate;
    private String location;
    private String world;
    private String server;
    private int playerAmount;
    private int commentId;

    private Map<Integer, Map<String, String>> comments;


    Ticket(int ticketId, String reportingPlayer, String reason, String description, String location, String world, String server, int playerAmount) {
        this.ticketId = ticketId;
        this.state = TicketStates.OPEN;
        this.reportingPlayer = reportingPlayer;
        this.reason = reason;
        this.description = description;
        this.dateCreated = new Date();
        this.location = location;
        this.world = world;
        this.server = server;
        this.playerAmount = playerAmount;

        comments = Maps.newHashMap();
    }

    void addComment(String player, String comment) {
        Map<String, String> commentMap = Maps.newHashMap();
        commentMap.put(comment, player);
        comments.put(commentId, commentMap);
        commentId++;
    }

}
