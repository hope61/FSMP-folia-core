package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.hope.fSMPFoliaCore.Managers.ConfigManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VoteCommand implements CommandExecutor {
    private final LangManager lang;
    private final ConfigManager config;

    public VoteCommand(LangManager lang, ConfigManager config) {
        this.lang = lang;
        this.config = config;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("fsmp.vote")) {
            sender.sendMessage(Component.text(lang.getNoPermission(), NamedTextColor.DARK_PURPLE));
            return true;
        }

        List<String> links = config.getVoteLinks();
        sender.sendMessage(Component.text(lang.getVoteHeader(), NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD));
        if (links.isEmpty()) {
            sender.sendMessage(Component.text(lang.getVoteNoLinks(), NamedTextColor.DARK_PURPLE));
            return true;
        }
        for (int i = 0; i < links.size(); i++) {
            String link = links.get(i);
            Component btn = Component.text()
                    .append(Component.text("  ▶ ", NamedTextColor.DARK_PURPLE))
                    .append(Component.text(lang.getVoteSite(i + 1), NamedTextColor.LIGHT_PURPLE)
                            .decorate(TextDecoration.UNDERLINED)
                            .clickEvent(ClickEvent.openUrl(link))
                            .hoverEvent(HoverEvent.showText(Component.text(link, NamedTextColor.DARK_PURPLE))))
                    .build();
            sender.sendMessage(btn);
        }
        return true;
    }
}
