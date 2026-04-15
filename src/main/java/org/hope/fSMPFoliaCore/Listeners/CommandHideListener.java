package org.hope.fSMPFoliaCore.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.Map;

/**
 * Strips permission-gated commands from the client's tab-complete list
 * when the player lacks the required permission.
 *
 * PlayerCommandSendEvent fires when the server sends the command list to
 * the client — removing entries here means they never appear in tab-complete.
 */
public class CommandHideListener implements Listener {

    // command label (lowercase) → required permission
    private static final Map<String, String> GUARDED = Map.ofEntries(
        Map.entry("heal",        "fsmp.heal"),
        Map.entry("feed",        "fsmp.feed"),
        Map.entry("gamemode",    "fsmp.gamemode"),
        Map.entry("gm",          "fsmp.gamemode"),
        Map.entry("gms",         "fsmp.gamemode"),
        Map.entry("gmc",         "fsmp.gamemode"),
        Map.entry("gma",         "fsmp.gamemode"),
        Map.entry("gmsp",        "fsmp.gamemode"),
        Map.entry("vanish",      "fsmp.vanish"),
        Map.entry("v",           "fsmp.vanish"),
        Map.entry("bc",          "fsmp.broadcast"),
        Map.entry("setspawn",    "fsmp.setspawn"),
        Map.entry("removespawn", "fsmp.setspawn"),
        Map.entry("day",         "fsmp.time"),
        Map.entry("night",       "fsmp.time"),
        Map.entry("sun",         "fsmp.time"),
        Map.entry("tp",          "fsmp.tp.admin"),
        Map.entry("crates",      "fsmp.crates.admin"),
        Map.entry("resettime",   "fsmp.resettime"),
        Map.entry("xp",          "fsmp.xp"),
        Map.entry("fsmpcore",    "fsmp.admin"),
        Map.entry("fsmp",        "fsmp.reload")
    );

    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent event) {
        event.getCommands().removeIf(cmd -> {
            String perm = GUARDED.get(cmd.toLowerCase());
            return perm != null && !event.getPlayer().hasPermission(perm);
        });
    }
}
