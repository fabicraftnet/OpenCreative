/*
 * OpenCreative+, Minecraft plugin.
 * (C) 2022-2026, McChicken Studio, mcchickenstudio@gmail.com
 *
 * OpenCreative+ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenCreative+ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ua.mcchickenstudio.opencreative.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ua.mcchickenstudio.opencreative.OpenCreative;
import ua.mcchickenstudio.opencreative.menus.world.browsers.OwnWorldsBrowserMenu;
import ua.mcchickenstudio.opencreative.utils.CooldownUtils;
import ua.mcchickenstudio.opencreative.utils.MessageUtils;

import java.util.List;

import static ua.mcchickenstudio.opencreative.utils.CooldownUtils.checkAndSetCooldownWithMessage;
import static ua.mcchickenstudio.opencreative.utils.MessageUtils.getLocaleMessageComponent;

/**
 * <h1>OwnMenuCommand</h1>
 * This command responsible for opening own worlds browser menu.
 * <p>
 * Available: For all players.
 */
public class OwnMenuCommand extends CommandHandler {

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length >= 1) {
            if (!sender.hasPermission("opencreative.own-menu.others")) {
                sender.sendMessage(MessageUtils.getLocaleMessageComponent("no-perms"));
                return;
            }
            Player player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                sender.sendMessage(MessageUtils.getLocaleMessageComponent("not-found-player"));
                return;
            }
            new OwnWorldsBrowserMenu(player).open(player);
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageUtils.getLocaleMessageComponent("only-players"));
            return;
        }
        if (OpenCreative.getSettings().isMaintenance() && !player.hasPermission("opencreative.maintenance.bypass")) {
            player.sendMessage(MessageUtils.getLocaleMessageComponent("maintenance"));
            return;
        }
        if (OpenCreative.getStability().isVeryBad() && !player.hasPermission("opencreative.stability.bypass")) {
            player.sendMessage(MessageUtils.getLocaleMessageComponent("creative.stability.cannot"));
            return;
        }
        if (!checkAndSetCooldownWithMessage(player, CooldownUtils.CooldownType.GENERIC_COMMAND)) return;
        new OwnWorldsBrowserMenu(player).open(player);
    }

    @Override
    public @Nullable List<String> onTab(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (sender.hasPermission("opencreative.own-menu.others")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return List.of();
    }
}
