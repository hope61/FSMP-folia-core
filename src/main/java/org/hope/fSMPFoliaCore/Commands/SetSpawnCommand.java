package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Managers.SpawnManager;
import org.jetbrains.annotations.NotNull;

public class SetSpawnCommand implements CommandExecutor {
    private final LangManager lang;
    private final SpawnManager spawnManager;

    public SetSpawnCommand(LangManager lang, SpawnManager spawnManager) {
        this.lang = lang;
        this.spawnManager = spawnManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(lang.getSpawnPlayersOnly(), lang.secondary()));
            return true;
        }

        if (!player.hasPermission("fsmp.setspawn")) {
            player.sendMessage(Component.text(lang.getSpawnNoPermission(), lang.secondary()));
            return true;
        }

        spawnManager.setSpawn(player.getLocation());
        player.sendMessage(Component.text(lang.getSpawnSet(), lang.primary()));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
        return true;
    }
}
