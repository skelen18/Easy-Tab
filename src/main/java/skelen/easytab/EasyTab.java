package skelen.easytab;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.stream.Collectors;

public final class EasyTab extends JavaPlugin {

    private Team aliveTeam;
    private Team deadTeam;
    private Team opTeam;

    @Override
    public void onEnable() {
        createTeams();
        startTabUpdater();
    }

    @Override
    public void onDisable() {
    }

    private void createTeams() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        opTeam = scoreboard.getTeam("op");
        if (opTeam == null) {
            opTeam = scoreboard.registerNewTeam("op");
        }
        opTeam.setColor(ChatColor.RED);
        opTeam.setPrefix(ChatColor.RED.toString());

        aliveTeam = scoreboard.getTeam("alive");
        if (aliveTeam == null) {
            aliveTeam = scoreboard.registerNewTeam("alive");
        }
        aliveTeam.setColor(ChatColor.GREEN);
        aliveTeam.setPrefix(ChatColor.GREEN.toString());

        deadTeam = scoreboard.getTeam("dead");
        if (deadTeam == null) {
            deadTeam = scoreboard.registerNewTeam("dead");
        }
        deadTeam.setColor(ChatColor.GRAY);
        deadTeam.setPrefix(ChatColor.GRAY.toString());
    }

    private void startTabUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateTabList();
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    private void updateTabList() {
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int alivePlayers = aliveTeam.getSize();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("admin")) {
                if (!opTeam.hasEntry(player.getName())) {
                    opTeam.addEntry(player.getName());
                }
                aliveTeam.removeEntry(player.getName());
                deadTeam.removeEntry(player.getName());
            } else if (aliveTeam.hasEntry(player.getName()) || deadTeam.hasEntry(player.getName())) {
            } else {
                if (!aliveTeam.hasEntry(player.getName())) {
                    aliveTeam.addEntry(player.getName());
                }
            }
        }

        String header = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "RILY EVENT";
        String footer =
                ChatColor.GRAY + "Hráči naživu: " + ChatColor.WHITE + alivePlayers + "\n" +
                        ChatColor.GRAY + "Online hráči: " + ChatColor.WHITE + onlinePlayers + "\n\n" +
                        ChatColor.AQUA + "mc.rilyevent.eu";

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setPlayerListHeader(header);
            player.setPlayerListFooter(footer);
        }

        List<Player> sortedPlayers = Bukkit.getOnlinePlayers().stream()
                .sorted((p1, p2) -> Integer.compare(getTeamPriority(p1), getTeamPriority(p2)))
                .collect(Collectors.toList());

        for (Player player : sortedPlayers) {
            updatePlayerListName(player);
        }
    }

    private void updatePlayerListName(Player player) {
        if (opTeam.hasEntry(player.getName())) {
            player.setPlayerListName(ChatColor.RED + player.getName());
        } else if (aliveTeam.hasEntry(player.getName())) {
            player.setPlayerListName(ChatColor.GREEN + player.getName());
        } else if (deadTeam.hasEntry(player.getName())) {
            player.setPlayerListName(ChatColor.GRAY + player.getName());
        } else {
            player.setPlayerListName(player.getName());
        }
    }

    private int getTeamPriority(Player player) {
        if (opTeam.hasEntry(player.getName())) {
            return 1;
        } else if (aliveTeam.hasEntry(player.getName())) {
            return 2;
        } else if (deadTeam.hasEntry(player.getName())) {
            return 3;
        } else {
            return 4;
        }
    }
}
