package de.nicolube.tablist;

import lombok.extern.java.Log;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.megavex.scoreboardlibrary.api.team.ScoreboardTeam;
import net.megavex.scoreboardlibrary.api.team.TeamDisplay;
import net.megavex.scoreboardlibrary.api.team.TeamManager;
import net.megavex.scoreboardlibrary.api.team.enums.CollisionRule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Log
public class TabListListener implements Listener {

    public TabListListener(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    private record PrefixMeta(UUID uuid, String username, int weight, Component prefix, Component suffix) {

    }

    private static final class TabListEntry {
        private final ScoreboardTeam team;
        private PrefixMeta meta;


        private TabListEntry(ScoreboardTeam team, PrefixMeta meta) {
            this.team = team;
            this.meta = meta;
        }

        void update(PrefixMeta meta) {
            this.meta = meta;
            final TeamDisplay display = team.defaultDisplay();
            if (display.entries().isEmpty()) display.addEntry(meta.username());
            display.prefix(meta.prefix());
            display.suffix(meta.suffix());
            display.playerColor(NamedTextColor.GRAY);
            display.collisionRule(CollisionRule.NEVER);
        }

        public ScoreboardTeam team() {
            return team;
        }

        public PrefixMeta meta() {
            return meta;
        }
    }

    private final TeamManager teamManager;
    private final ConcurrentHashMap<UUID, TabListEntry> teams = new ConcurrentHashMap<>();

    private static String calcGroup(UUID uuid, int weight) {
        return "%06d%x".formatted(999999 - weight, uuid.getLeastSignificantBits());
    }

    private TabListEntry getOrCreate(UUID uuid, String name) {
        return this.teams.getOrDefault(uuid, create(uuid, name));
    }

    private @NotNull TabListEntry create(UUID uuid, String name) {
        final PrefixMeta meta = new PrefixMeta(uuid, name, 0, Component.text("T"), Component.empty());
        String teamName = calcGroup(uuid, meta.weight());
        ScoreboardTeam team = teamManager.createIfAbsent(teamName);
        final TabListEntry tabListEntry = new TabListEntry(team, meta);
        tabListEntry.update(meta);
        return tabListEntry;
    }

    private void sendUpdate(@NotNull TabListEntry entry, @NotNull Player player) {
        final ScoreboardTeam team = entry.team();
        team.display(player);
    }

    private void sendUpdate(TabListEntry entry) {
        teamManager.players().forEach(player -> sendUpdate(entry, player));
        final UUID uuid = entry.meta().uuid();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    private void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        teamManager.addPlayer(player);
        for (TabListEntry entry : teams.values()) {
            sendUpdate(entry, player);
        }
        TabListEntry newEntry = getOrCreate(player.getUniqueId(), player.getName());
        this.teams.put(player.getUniqueId(), newEntry);
        sendUpdate(newEntry);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    private void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        if (teamManager.players().contains(player))
            teamManager.removePlayer(player);
        this.teams.remove(player.getUniqueId());
    }
}
