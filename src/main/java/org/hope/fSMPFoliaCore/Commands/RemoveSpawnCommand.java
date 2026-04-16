package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Managers.SpawnManager;
import org.jetbrains.annotations.NotNull;

public class RemoveSpawnCommand implements CommandExecutor {
    private final LangManager lang;
    private final SpawnManager spawnManager;

    public RemoveSpawnCommand(LangManager lang, SpawnManager spawnManager) {
        this.lang = lang;
        this.spawnManager = spawnManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("fsmp.setspawn")) {
            sender.sendMessage(Component.text(lang.getSpawnNoPermission(), lang.secondary()));
            return true;
        }
        spawnManager.removeSpawn();
        sender.sendMessage(Component.text(lang.getSpawnRemoved(), lang.secondary()));
        return true;
    }
}
