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

package ua.mcchickenstudio.opencreative.planets;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ua.mcchickenstudio.opencreative.OpenCreative;
import ua.mcchickenstudio.opencreative.coding.blocks.events.player.world.QuitEvent;
import ua.mcchickenstudio.opencreative.settings.Sounds;

import java.util.*;

import static ua.mcchickenstudio.opencreative.utils.MessageUtils.getLocaleMessage;
import static ua.mcchickenstudio.opencreative.utils.PlayerUtils.*;

/**
 * <h1>PlanetPlayers</h1>
 * This class represents a planet players with
 * data and statuses, like building or development
 * permissions.
 */
public class PlanetPlayers {

    private final Planet planet;

    private final Set<PlanetPlayer> planetPlayers = new HashSet<>();

    private final Set<String> buildersTrusted = new HashSet<>();
    private final Set<String> buildersNotTrusted = new HashSet<>();

    private final Set<String> developersTrusted = new HashSet<>();
    private final Set<String> developersNotTrusted = new HashSet<>();
    private final Set<String> developersGuests = new HashSet<>();

    private final Set<String> bannedPlayers = new HashSet<>();
    private final Set<String> whitelistedPlayers = new HashSet<>();

    public PlanetPlayers(Planet planet) {
        this.planet = planet;
    }

    /**
     * Registers player to planet.
     *
     * @param player online player to register.
     */
    public void registerPlayer(@NotNull Player player) {
        planetPlayers.add(new PlanetPlayer(planet, player));
    }

    public void unregisterPlayer(Player player) {
        planetPlayers.removeIf(planetPlayer -> planetPlayer.getPlayer().equals(player));
        planet.getDevPlanet().getLastLocations().remove(player.getUniqueId());
        planet.getDevPlanet().clearMarkedExecutors(player);
        planet.getLimits().clearPlayerLimits(player);
        planet.getTerritory().getRecipes().clearForPlayer(player);
    }

    public @Nullable PlanetPlayer getPlanetPlayer(@NotNull Player player) {
        for (PlanetPlayer planetPlayer : planetPlayers) {
            if (planetPlayer.getPlayer().equals(player)) {
                return planetPlayer;
            }
        }
        return null;
    }

    public void clear() {
        buildersTrusted.clear();
        developersTrusted.clear();
        buildersNotTrusted.clear();
        developersNotTrusted.clear();
        developersGuests.clear();
        bannedPlayers.clear();
        whitelistedPlayers.clear();
    }

    public void loadPlayers() {
        clear();
        FileConfiguration config = planet.getConfiguration().getConfig();

        buildersTrusted.addAll(config.getStringList("players.builders.trusted"));
        developersTrusted.addAll(config.getStringList("players.developers.trusted"));

        buildersNotTrusted.addAll(config.getStringList("players.builders.not-trusted"));
        developersNotTrusted.addAll(config.getStringList("players.developers.not-trusted"));

        developersGuests.addAll(config.getStringList("players.developers.guests"));
        bannedPlayers.addAll(config.getStringList("players.blacklist"));
        whitelistedPlayers.addAll(config.getStringList("players.whitelist"));
    }

    public Set<String> getAllBuilders() {
        Set<String> builders = new HashSet<>(getBuildersTrusted());
        builders.addAll(getBuildersNotTrusted());
        return builders;
    }

    public Set<String> getAllDevelopers() {
        Set<String> developers = new HashSet<>(getDevelopersTrusted());
        developers.addAll(getDevelopersNotTrusted());
        developers.addAll(getDevelopersGuests());
        return developers;
    }

    public boolean isTrustedDeveloper(Player player) {
        if (planet.isOwner(player)) {
            return true;
        }
        if (player.hasPermission("opencreative.world.dev.others")) {
            return true;
        }
        for (String uuid : getDevelopersTrusted()) {
            if (getUUIDFromText(uuid).equals(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public boolean isNotTrustedDeveloper(Player player) {
        for (String uuid : getDevelopersNotTrusted()) {
            if (getUUIDFromText(uuid).equals(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public boolean isNotTrustedBuilder(Player player) {
        for (String uuid : getBuildersNotTrusted()) {
            if (getUUIDFromText(uuid).equals(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public boolean isTrustedBuilder(Player player) {
        if (planet.isOwner(player)) {
            return true;
        }
        if (player.hasPermission("opencreative.world.build.others")) {
            return true;
        }
        for (String uuid : getBuildersTrusted()) {
            if (getUUIDFromText(uuid).equals(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public boolean isDeveloperGuest(Player player) {
        for (String uuid : getDevelopersGuests()) {
            if (getUUIDFromText(uuid).equals(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public boolean canDevelop(Player player) {
        if (planet.isOwner(player)) {
            return true;
        }
        if (player.hasPermission("opencreative.world.dev.others")) {
            return true;
        }
        for (String uuid : getDevelopersTrusted()) {
            if (getUUIDFromText(uuid).equals(player.getUniqueId())) {
                return true;
            }
        }
        Player owner = Bukkit.getPlayer(planet.getOwner());
        if (owner == null) {
            return false;
        }
        if (!planet.equals(OpenCreative.getPlanetsManager().getPlanetByPlayer(owner))) {
            return false;
        }
        for (String uuid : getDevelopersNotTrusted()) {
            if (getUUIDFromText(uuid).equals(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public boolean canBuild(Player player) {
        if (planet.isOwner(player)) {
            return true;
        }
        if (player.hasPermission("opencreative.world.build.others")) {
            return true;
        }
        for (String uuid : getBuildersTrusted()) {
            if (getUUIDFromText(uuid).equals(player.getUniqueId())) {
                return true;
            }
        }
        Player owner = Bukkit.getPlayer(planet.getOwner());
        if (owner == null) {
            return false;
        }
        if (!planet.equals(OpenCreative.getPlanetsManager().getPlanetByPlayer(owner))) {
            return false;
        }
        for (String uuid : getBuildersNotTrusted()) {
            if (getUUIDFromText(uuid).equals(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public void removeBuilder(String nickname) {
        Player player = Bukkit.getPlayer(nickname);
        String uuid = player.getUniqueId().toString();
        if (player != null) {
            Planet playerPlanet = OpenCreative.getPlanetsManager().getPlanetByPlayer(player);
            if (planet.equals(playerPlanet)) {
                if (player.getGameMode() == GameMode.CREATIVE) {
                    player.setGameMode(GameMode.ADVENTURE);
                    clearWorldModePermissions(player);
                    if (!canDevelop(player)) {
                        giveVisitorPermissions(player);
                    }
                }
            }
        }
        if (!planet.isLoaded()) loadPlayers();
        buildersNotTrusted.removeIf(builder -> builder.equalsIgnoreCase(uuid));
        buildersTrusted.removeIf(builder -> builder.equalsIgnoreCase(uuid));
        planet.getConfiguration().set("players.builders.not-trusted", buildersNotTrusted);
        planet.getConfiguration().set("players.builders.trusted", buildersTrusted);
    }

    public void removeDeveloper(String nickname) {
        Player player = Bukkit.getPlayer(nickname);
        String uuid = player.getUniqueId().toString();
        if (player != null) {
            Planet playerPlanet = OpenCreative.getPlanetsManager().getPlanetByPlayer(player);
            if (planet.equals(playerPlanet)) {
                if (player.getGameMode() == GameMode.CREATIVE) {
                    player.setGameMode(GameMode.ADVENTURE);
                }
                if (isEntityInDevPlanet(player)) {
                    clearPlayer(player);
                    player.teleport(planet.getTerritory().getSpawnLocation());
                }
                clearWorldModePermissions(player);
                if (!canBuild(player)) {
                    giveVisitorPermissions(player);
                }
            }
        }
        if (!planet.isLoaded()) loadPlayers();
        developersNotTrusted.removeIf(developer -> developer.equalsIgnoreCase(uuid));
        developersTrusted.removeIf(developer -> developer.equalsIgnoreCase(uuid));
        planet.getConfiguration().set("players.developers.not-trusted", developersNotTrusted);
        planet.getConfiguration().set("players.developers.trusted", developersTrusted);
    }

    public void addDeveloperGuest(String nickname) {
        if (getAllDevelopers().size() > planet.getLimits().getDevelopersLimit()) return;
        Player player = Bukkit.getPlayer(nickname);
        String uuid = player.getUniqueId().toString();
        if (player != null) {
            Planet playerPlanet = OpenCreative.getPlanetsManager().getPlanetByPlayer(player);
            if (planet.equals(playerPlanet)) {
                player.sendMessage(getLocaleMessage("world.players.developers.player-guest").replace("%player%", player.getName()));
                Sounds.WORLD_NOW_DEVELOPER_GUEST.play(player);
            }
        }
        if (!planet.isLoaded()) loadPlayers();
        developersGuests.add(uuid);
        developersNotTrusted.removeIf(developer -> developer.equalsIgnoreCase(uuid));
        developersTrusted.removeIf(developer -> developer.equalsIgnoreCase(uuid));
        planet.getConfiguration().set("players.developers.guests", developersGuests);
        planet.getConfiguration().set("players.developers.not-trusted", developersNotTrusted);
        planet.getConfiguration().set("players.developers.trusted", developersTrusted);
    }

    public void addDeveloper(String nickname, boolean trusted) {
        if (getAllDevelopers().size() > planet.getLimits().getDevelopersLimit()) return;
        Player player = Bukkit.getPlayer(nickname);
        String uuid = player.getUniqueId().toString();
        if (player != null) {
            Planet playerPlanet = OpenCreative.getPlanetsManager().getPlanetByPlayer(player);
            if (planet.equals(playerPlanet)) {
                if (!trusted) {
                    player.sendMessage(getLocaleMessage("world.players.developers.player").replace("%player%", player.getName()));
                    Sounds.WORLD_NOW_DEVELOPER.play(player);
                    if (OpenCreative.getPlanetsManager().getDevPlanet(player) != null) {
                        player.setGameMode(GameMode.CREATIVE);
                        clearWorldModePermissions(player);
                        giveDevPermissions(player);
                    }
                }
            }
        }
        if (!planet.isLoaded()) loadPlayers();
        if (trusted) {
            developersNotTrusted.removeIf(developer -> developer.equalsIgnoreCase(uuid));
            developersTrusted.add(uuid);
        } else {
            developersTrusted.removeIf(developer -> developer.equalsIgnoreCase(uuid));
            developersNotTrusted.add(uuid);
        }
        developersGuests.removeIf(developer -> developer.equalsIgnoreCase(uuid));
        planet.getConfiguration().set("players.developers.guests", developersGuests);
        planet.getConfiguration().set("players.developers.not-trusted", developersNotTrusted);
        planet.getConfiguration().set("players.developers.trusted", developersTrusted);
    }


    public void addBuilder(String nickname, boolean trusted) {
        if (getAllBuilders().size() > planet.getLimits().getBuildersLimit()) return;
        Player player = Bukkit.getPlayer(nickname);
        String uuid = player.getUniqueId().toString();
        if (player != null) {
            Planet playerPlanet = OpenCreative.getPlanetsManager().getPlanetByPlayer(player);
            if (planet.equals(playerPlanet)) {
                if (!trusted) {
                    player.sendMessage(getLocaleMessage("world.players.builders.player").replace("%player%", player.getName()));
                    Sounds.WORLD_NOW_BUILDER.play(player);
                    if (OpenCreative.getPlanetsManager().getDevPlanet(player) == null) {
                        player.setGameMode(GameMode.CREATIVE);
                        clearWorldModePermissions(player);
                        giveBuildPermissions(player);
                    }
                }
            }
        }
        if (!planet.isLoaded()) loadPlayers();
        if (trusted) {
            buildersNotTrusted.removeIf(builder -> builder.equalsIgnoreCase(uuid));
            buildersTrusted.add(uuid);
        } else {
            buildersTrusted.removeIf(builder -> builder.equalsIgnoreCase(uuid));
            buildersNotTrusted.add(uuid);
        }
        planet.getConfiguration().set("players.builders.not-trusted", buildersNotTrusted);
        planet.getConfiguration().set("players.builders.trusted", buildersTrusted);
        if (!planet.isLoaded()) clear();
    }

    public void unbanPlayer(String nickname) {
        if (!planet.isLoaded()) loadPlayers();
        String uuid = Bukkit.getOfflinePlayer(nickname).getUniqueId().toString();
        this.bannedPlayers.removeIf(ban -> ban.equalsIgnoreCase(uuid));
        planet.getConfiguration().set("players.blacklist", bannedPlayers);
        if (!planet.isLoaded()) clear();
    }

    public void removeFromWhitelist(String nickname) {
        if (!planet.isLoaded()) loadPlayers();
        String uuid = Bukkit.getOfflinePlayer(nickname).getUniqueId().toString();
        this.whitelistedPlayers.removeIf(whitelisted -> whitelisted.equalsIgnoreCase(uuid));
        planet.getConfiguration().set("players.whitelist", whitelistedPlayers);
        if (!planet.isLoaded()) clear();
    }

    public void banPlayer(String nickname) {
        if (planet.isOwner(Bukkit.getOfflinePlayer(nickname).getUniqueId())) return;
        if (getBannedPlayers().size() > planet.getLimits().getBlacklistedLimit()) return;
        Player player = Bukkit.getPlayer(nickname);
        String uuid = player.getUniqueId().toString();
        if (player != null && !player.hasPermission("opencreative.world.ban.bypass")) {
            Planet playerPlanet = OpenCreative.getPlanetsManager().getPlanetByPlayer(player);
            if (planet.equals(playerPlanet)) {
                teleportToLobby(player);
                player.sendMessage(getLocaleMessage("world.players.black-list.player").replace("%player%", player.getName()));
                Sounds.WORLD_BANNED.play(player);
            }
        }
        if (!planet.isLoaded()) loadPlayers();
        bannedPlayers.add(uuid);
        planet.getConfiguration().set("players.blacklist", bannedPlayers);
        if (!planet.isLoaded()) clear();
    }

    public void whitelistPlayer(String nickname) {
        if (planet.isOwner(Bukkit.getOfflinePlayer(nickname).getUniqueId())) return;
        if (getWhitelistedPlayers().size() > planet.getLimits().getWhitelistedLimit()) return;
        Player player = Bukkit.getPlayer(nickname);
        String uuid = player.getUniqueId().toString();
        if (player != null) {
            Planet playerPlanet = OpenCreative.getPlanetsManager().getPlanetByPlayer(player);
            if (planet.equals(playerPlanet)) {
                player.sendMessage(getLocaleMessage("world.players.white-list.player").replace("%player%", player.getName()));
                Sounds.WORLD_WHITELIST_ADDED.play(player);
            }
        }
        if (!planet.isLoaded()) loadPlayers();
        whitelistedPlayers.add(uuid);
        planet.getConfiguration().set("players.whitelist", whitelistedPlayers);
        if (!planet.isLoaded()) clear();
    }

    public void kickPlayer(Player player) {
        Planet playerPlanet = OpenCreative.getPlanetsManager().getPlanetByPlayer(player);
        if (planet.equals(playerPlanet) && !player.hasPermission("opencreative.world.kick.bypass")) {
            new QuitEvent(player).callEvent();
            removePassengers(player);
            teleportToLobby(player);
            player.sendMessage(getLocaleMessage("world.players.kick.player")
                    .replace("%player%", player.getName()));
            Sounds.WORLD_KICKED.play(player);
        }
    }

    public Set<String> getAllPlayersFromConfig() {
        Set<String> allPlayers = new HashSet<>();
        planet.getPlayers().forEach(player -> allPlayers.add(player.getUniqueId().toString()));
        allPlayers.addAll(getBuildersTrusted());
        allPlayers.addAll(getBuildersNotTrusted());
        allPlayers.addAll(getDevelopersTrusted());
        allPlayers.addAll(getDevelopersNotTrusted());
        allPlayers.addAll(getDevelopersGuests());
        allPlayers.addAll(getBannedPlayers());
        allPlayers.addAll(getWhitelistedPlayers());
        allPlayers.remove(planet.getOwner().toString());
        return allPlayers;
    }

    public Set<String> getBuildersTrusted() {
        if (!planet.isLoaded()) {
            return new HashSet<>(planet.getConfiguration().getConfig().getStringList("players.builders.trusted"));
        }
        return new HashSet<>(buildersTrusted);
    }

    public Set<String> getBuildersNotTrusted() {
        if (!planet.isLoaded()) {
            return new HashSet<>(planet.getConfiguration().getConfig().getStringList("players.builders.not-trusted"));
        }
        return new HashSet<>(buildersNotTrusted);
    }

    public Set<String> getDevelopersGuests() {
        if (!planet.isLoaded()) {
            return new HashSet<>(planet.getConfiguration().getConfig().getStringList("players.developers.guests"));
        }
        return new HashSet<>(developersGuests);
    }

    public Set<String> getDevelopersTrusted() {
        if (!planet.isLoaded()) {
            return new HashSet<>(planet.getConfiguration().getConfig().getStringList("players.developers.trusted"));
        }
        return new HashSet<>(developersTrusted);
    }

    public Set<String> getDevelopersNotTrusted() {
        if (!planet.isLoaded()) {
            return new HashSet<>(planet.getConfiguration().getConfig().getStringList("players.developers.not-trusted"));
        }
        return new HashSet<>(developersNotTrusted);
    }

    public String getBuilders() {
        return String.join(", ", planet.getWorldPlayers().getAllBuilders().stream().map(uuid -> Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName() ).toList());
    }

    public String getDevelopers() {
        return String.join(", ", planet.getWorldPlayers().getAllDevelopers().stream().map(uuid -> Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName() ).toList());
    }

    public boolean isBanned(String nickname) {
        String uuid = Bukkit.getOfflinePlayer(nickname).getUniqueId().toString();
        for (String banned : getBannedPlayers()) {
            if (banned.equalsIgnoreCase(uuid)) {
                return true;
            }
        }
        return false;
    }

    public boolean isWhitelisted(String nickname) {
        String uuid = Bukkit.getOfflinePlayer(nickname).getUniqueId().toString();
        for (String whitelisted : getWhitelistedPlayers()) {
            if (whitelisted.equalsIgnoreCase(uuid)) {
                return true;
            }
        }
        return false;
    }

    public Set<String> getBannedPlayers() {
        if (!planet.isLoaded()) {
            return new HashSet<>(planet.getConfiguration().getConfig().getStringList("players.blacklist"));
        }
        return bannedPlayers;
    }

    public Set<String> getWhitelistedPlayers() {
        if (!planet.isLoaded()) {
            return new HashSet<>(planet.getConfiguration().getConfig().getStringList("players.whitelist"));
        }
        return whitelistedPlayers;
    }

    public void purgeData() {
        List<String> empty = new ArrayList<>();
        clear();
        planet.getConfiguration().set("players.unique", empty);
        planet.getConfiguration().set("players.liked", empty);
        planet.getConfiguration().set("players.disliked", empty);
        planet.getConfiguration().set("players.blacklist", empty);
        planet.getConfiguration().set("players.whitelist", empty);
        planet.getConfiguration().set("players.developers.trusted", empty);
        planet.getConfiguration().set("players.developers.not-trusted", empty);
        planet.getConfiguration().set("players.developers.guests", empty);
        planet.getConfiguration().set("players.builders.trusted", empty);
        planet.getConfiguration().set("players.builders.not-trusted", empty);
    }
}
