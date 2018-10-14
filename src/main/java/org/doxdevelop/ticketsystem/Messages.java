package org.doxdevelop.ticketsystem;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

class Messages {
	
	private TicketSystem plugin;
	private Map<String, String> messageData;

	Messages(TicketSystem instance) {
		plugin = instance;
		messageData = new HashMap<>();
	}

	Map<String, String> getMessageData() {
		File f = new File(plugin.getDataFolder() + File.separator + "messages.yml");
		if (!f.exists()) {
			try {
				f.createNewFile();
				saveMessages();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return loadMessages();
	}
	
	Map<String, String> loadMessages() {
		File f = new File(plugin.getDataFolder() + File.separator + "messages.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(f);
		for (String message : config.getConfigurationSection("").getKeys(false)) {
			messageData.put(message, config.getString(message));
		}
		return messageData;
	}

	private void saveMessages(){
		setMessage("tag", "&5[&dTicketSystem&5] ");
		setMessage("createTicket", "&bTicket wurde erfolgreich für Sie mit der Ticket-ID &a%id&b erstellt. Verwenden Sie diese ID, wenn Sie Kommentare hinzufügen.");
		setMessage("createComment", "&bComment wurde erfolgreich zum Ticket hinzugefügt &a%id");
		setMessage("adminUpdate", "&bEin neuer Bericht wurde eingereicht! Um den Bericht anzuzeigen, machen Sie &c/ticket info %id");
		setMessage("reportHelp", "&bDie folgenden Befehle sind verfügbar;");
		setMessage("createHelp", "&b/Bericht <Grund> <Beschreibung> &aEinen Bericht öffnen");
		setMessage("ticketHelpTitle", "&bDie folgenden Befehle sind verfügbar");
		setMessage("ticketHelp1", "&b/ticket view <open|asigned|asclosed> <all> &aoffene Tickets zeigen");
		setMessage("ticketHelp2", "&b/ticket info <ticket id> &aZur Anzeige weiterer Informationen über einen Bericht");
		setMessage("ticketHelp3", "&b/ticket claim <ticket id> &aNimm ein Ticket an");
		setMessage("ticketHelp4", "&b/ticket close <ticket id> &aEin Ticket schließen, nachdem Sie den Bericht gelöst haben");
		setMessage("ticketHelp5", "&b/ticket teleport <ticket id> &aTeleport zum Ort des Berichts");
		setMessage("ticketHelp6", "&b/ticket stats &aTicket Stats anzeigen");
		setMessage("noId", "&4ERROR");
		setMessage("claimTicket", "&bSie haben dieses Ticket erfolgreich eingelöst");
		setMessage("alreadyClaimed", "&bDieses Ticket wurde bereits abgeholt");
		setMessage("alreadyClosed", "&bDieses Ticket wurde bereits geschlossen");
		setMessage("alreadyUnclaimed", "&bDieses Ticket kann nicht abgewickelt werden");
		setMessage("closeTicket", "&bSie haben dieses Ticket erfolgreich geschlossen");
		setMessage("unclaimTicket", "&bSie haben dieses Ticket erfolgreich abgeholt");
		setMessage("teleport", "&bSie wurden an den Ort des Berichts teleportiert");
		setMessage("closeNotice", "&bTicket &a%id&b wurde gerade vom &a%player geschlossen");
		setMessage("ticketTitle", "&b========================&4Information&b===================================");
		setMessage("ticketId", "&aTicket ID: &a");
		setMessage("ticketStatus", "&aStatus des Tickets: &a");
		setMessage("ticketAssignedTo", "&aZugewiesen zu: &a");
		setMessage("ticketClosedBy", "&aGeschlossen von: &a");
		setMessage("ticketClosedDate", "&aGeschlossen am: &a");
		setMessage("ticketReportingPlayer", "&aPlayer: &a");
		setMessage("ticketReason", "&aGrund: &a");
		setMessage("ticketDescription", "&aBeschreibung: &a");
		setMessage("ticketAmount", "&aAnzahl der Spieler online: &a");
		setMessage("ticketServer", "&aServer: &a");
		setMessage("ticketDateCreated", "&aDatum: &a");
		setMessage("ticketLocation", "&aOrt des Berichts: &a");
		setMessage("ticketComments", "&aComments: &a%amount comment(s)");
		setMessage("ticketFooter", "&b============================");
		setMessage("statsTitle", "&b==========&4Stats&b===========");
		setMessage("statsOpen", "&aOffen: &a");
		setMessage("statsAssigned", "&aZugewiesen: &a");
		setMessage("statsClosed", "&aGeschlossen: &a");
		setMessage("statsFooter", "&b============================");
		setMessage("openTitle", "&b==========&4Open Tickets&b==========");
		setMessage("openId", "&aTicket ID: &a");
		setMessage("openInfo", "&aDo /ticket info <number> Um mehr Informationen über einen Bericht anzuzeigen");
		setMessage("openClaim", "&aDo /ticket claim <number> So weisen Sie sich das Ticket zu");
		setMessage("openFooter", "&b===========================");
		setMessage("assignedTitle", "&b==========&4 Zugewiesene Tickets&b==========");
		setMessage("assignedId", "&aTicket ID: &a");
		setMessage("assignedInfo", "&aDo /ticket info <number> Um mehr Informationen über einen Bericht anzuzeigen");
		setMessage("assignedFooter", "&b============================");
		setMessage("allAssignedTitle", "&b==========&4All Assigned Tickets&b==========");
		setMessage("allAssignedId", "&aTicket ID: &a%id &ais assigned to &a%player");
		setMessage("allAssignedInfo", "&aDo /ticket info <number> Um mehr Informationen über einen Bericht anzuzeigen");
		setMessage("allAssignedFooter", "&b=========================");
		setMessage("closedTitle", "&b==========&4Geschlossene Tickets&b==========");
		setMessage("closedId", "&aTicket ID: &a");
		setMessage("closedInfo", "&aDo /ticket info <number> Um mehr Informationen über einen Bericht anzuzeigen");
		setMessage("closedFooter", "&b============================");
		setMessage("allClosedTitle", "&b=============&4Alle geschlossenen Tickets&b================");
		setMessage("allClosedId", "&aTicket ID: &a%id &awurde geschlossen durch &a%player");
		setMessage("allClosedInfo", "&aDo /ticket info <number> Um mehr Informationen über einen Bericht anzuzeigen");
		setMessage("allClosedFooter", "&b============================");
		setMessage("commentsTitle", "&b============================");
		setMessage("comment", "&a%player: &b%comment");
		setMessage("commentsFooter", "&b============================");
		setMessage("assignedNoTickets", "&aSie haben keine zugeordneten Tickets");
		setMessage("allAssignedNoTickets", "&aKeine zugeordneten Tickets");
		setMessage("closedNoTickets", "&aSie haben keine geschlossenen Tickets");
		setMessage("allClosedNoTickets", "&aKeine geschlossenen Tickets");
		setMessage("no comments", "&aKeine Kommentare!");
		setMessage("loginNotice", "&bEs gibt derzeit %Anzahl der offenen Tickets, die zugewiesen werden müssen!");
		setMessage("createFailed", "&bERROR!");
		setMessage("failClaimTicket", "&bERROR!");
		setMessage("failCloseTicket", "&bERROR!");
		setMessage("ticketInfoError", "&4ERROR! Sie müssen /ticket info <id> eingeben");
		setMessage("ticketClaimError", "&4ERROR! Sie müssen /ticket claim <id> eingeben");
		setMessage("ticketCloseError", "&4ERROR! Sie müssen /ticket close <id> eingeben");
		setMessage("ticketTeleportError", "&4ERROR! Sie müssen /ticket teleport <id> eingeben");
		setMessage("ticketReload", "&bSie haben die Konfigurationen erfolgreich neu geladen!");
		setMessage("noPermission", "&4Sie haben keine Berechtigung, diesen Befehl zu verwenden!");
		setMessage("mustBePlayer", "&4Sie müssen ein Spieler sein, um diesen Befehl zu verwenden");
		setMessage("maxTickets", "&cSie haben die maximale Anzahl von Tickets erreicht, die Sie in Anspruch nehmen können");
	}
	
	private void setMessage(String name, String message) {
		File f = new File(plugin.getDataFolder() + File.separator + "messages.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(f);
		if (!config.isSet(name)) {
			config.set(name, message);
			try {
				config.save(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
