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

package ua.mcchickenstudio.opencreative.commands.world.modes;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ua.mcchickenstudio.opencreative.OpenCreative;
import ua.mcchickenstudio.opencreative.coding.blocks.events.player.world.QuitEvent;
import ua.mcchickenstudio.opencreative.commands.CommandHandler;
import ua.mcchickenstudio.opencreative.planets.Planet;
import ua.mcchickenstudio.opencreative.utils.CooldownUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ua.mcchickenstudio.opencreative.utils.CooldownUtils.checkAndSetCooldownWithMessage;
import static ua.mcchickenstudio.opencreative.utils.MessageUtils.*;

/**
 * <h1>DevCommand</h1>
 * This command is responsible for connecting player to
 * developers world, where he can create a code with
 * coding blocks and items.
 * <p>
 * Available: For world developers.
 */
public class DevCommand extends CommandHandler {

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(getLocaleMessageComponent("only-players"));
            return;
        }

        if (!checkAndSetCooldownWithMessage(player, CooldownUtils.CooldownType.GENERIC_COMMAND)) return;

        if (player.isDead()) {
            sender.sendMessage(getLocaleMessageComponent("only-alive"));
            return;
        }

        Planet planet = OpenCreative.getPlanetsManager().getPlanetByPlayer(player);
        if (planet == null) {
            player.sendMessage(getLocaleMessageComponent("only-in-world"));
            return;
        }

        if (!OpenCreative.getSettings().getCodingSettings().isEnabled()) {
            player.sendMessage(getLocaleMessageComponent("world.dev-mode.disabled"));
            return;
        }

        if (args.length == 0 || args.length == 3) {
            if (planet.getWorldPlayers().canDevelop(player) || planet.getWorldPlayers().isDeveloperGuest(player)) {
                if (!planet.getWorldPlayers().isTrustedDeveloper(player)) {
                    Player planetOwner = Bukkit.getPlayer(planet.getOwner());
                    if (planetOwner == null) {
                        sender.sendMessage(getLocaleMessageComponent("world.dev-mode.cant-dev-when-offline"));
                        return;
                    }
                    Planet ownerPlanet = OpenCreative.getPlanetsManager().getPlanetByPlayer(planetOwner);
                    if (!planet.equals(ownerPlanet)) {
                        sender.sendMessage(getLocaleMessageComponent("world.dev-mode.cant-dev-when-offline"));
                        return;
                    }
                }
                new QuitEvent(player).callEvent();
                if (args.length == 3) {
                    try {
                        double x = Double.parseDouble(args[0]);
                        double y = Double.parseDouble(args[1]);
                        double z = Double.parseDouble(args[2]);
                        planet.getDevPlanet().connectPlayer(player, x, y, z);
                    } catch (Exception error) {
                        planet.getDevPlanet().connectPlayer(player);
                    }
                } else {
                    planet.getDevPlanet().connectPlayer(player);
                }
            } else {
                sender.sendMessage(getPlayerLocaleMessage("not-owner", player));
            }
        } else {
            if (!planet.isOwner(sender.getName())) {
                sender.sendMessage(getLocaleMessageComponent("not-owner"));
                return;
            }
            String nickname = args[0];
            UUID uuid = Bukkit.getOfflinePlayer(nickname).getUniqueId();
            Player onlinePlayer = Bukkit.getPlayer(nickname);
            if (!planet.getWorldPlayers().getAllDevelopers().contains(uuid)) {
                if (onlinePlayer != null) {
                    nickname = onlinePlayer.getName();
                }
            }
            if (planet.isOwner(uuid)) {
                sender.sendMessage(getLocaleMessageComponent("same-player"));
                return;
            }
            /*
             * Checks if player's uuid is in not trusted
             * or trusted developers.
             */
            if (planet.getWorldPlayers().getDevelopersNotTrusted().contains(uuid)) {
                planet.getWorldPlayers().addDeveloper(nickname, true);
                sender.sendMessage(toComponent(getLocaleMessageString("world.players.developers.trusted").replace("%player%", nickname)));
                return;
            }
            if (planet.getWorldPlayers().getDevelopersTrusted().contains(uuid)) {
                planet.getWorldPlayers().removeDeveloper(nickname);
                sender.sendMessage(toComponent(getLocaleMessageString("world.players.developers.removed").replace("%player%", nickname)));
                return;
            }
            /*
             * Adds online player as not trusted developers, if he's not
             * listed in developers.
             */
            int limit = planet.getLimits().getDevelopersLimit();
            if (planet.getWorldPlayers().getAllDevelopers().size() > limit) {
                sender.sendMessage(toComponent(getLocaleMessageString("world.players.developers.limit").replace("%limit%", String.valueOf(limit))));
                return;
            }
            if (onlinePlayer != null) {
                Planet playerPlanet = OpenCreative.getPlanetsManager().getPlanetByPlayer(onlinePlayer);
                if (planet.equals(playerPlanet)) {
                    sender.sendMessage(toComponent(getLocaleMessageString("world.players.developers.added").replace("%player%", onlinePlayer.getName())));
                    planet.getWorldPlayers().addDeveloper(onlinePlayer.getName(), false);
                } else {
                    sender.sendMessage(getLocaleMessageComponent("no-player-found"));
                }
            } else {
                sender.sendMessage(getLocaleMessageComponent("no-player-found"));
            }
        }
    }

    @Override
    public @Nullable List<String> onTab(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return null;
        Planet planet = OpenCreative.getPlanetsManager().getPlanetByPlayer(player);
        if (planet == null) return null;
        if (planet.isOwner(player)) {
            List<String> list = new ArrayList<>(planet.getWorldPlayers().getAllDevelopers().stream().map(uuid -> Bukkit.getOfflinePlayer(uuid).getName() ).toList());
            for (Player planetPlayer : planet.getPlayers()) {
                if (planet.isOwner(planetPlayer) || list.contains(planetPlayer.getName())) continue;
                list.add(planetPlayer.getName());
            }
            return list.subList(0, Math.min(10, list.size()));
        }
        return null;
    }
}
