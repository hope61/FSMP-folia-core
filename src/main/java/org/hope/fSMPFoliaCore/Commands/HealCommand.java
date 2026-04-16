package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.jetbrains.annotations.NotNull;

public class HealCommand implements CommandExecutor {
    private final LangManager lang;

    public HealCommand(LangManager lang) {
        this.lang = lang;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(lang.getHealPlayersOnly(), lang.secondary()));
            return true;
        }

        if (!player.hasPermission("fsmp.heal")) {
            player.sendMessage(Component.text(lang.getHealNoPermission(), lang.secondary()));
            return true;
        }

        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = maxHealthAttr != null ? maxHealthAttr.getValue() : 20.0;
        player.setHealth(maxHealth);
        player.setFireTicks(0);
        player.sendMessage(Component.text(lang.getHealSuccess(), lang.primary()));
        return true;
    }
}
