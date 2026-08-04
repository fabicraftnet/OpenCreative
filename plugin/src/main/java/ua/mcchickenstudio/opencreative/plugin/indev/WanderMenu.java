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

package ua.mcchickenstudio.opencreative.plugin.indev;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import ua.mcchickenstudio.opencreative.plugin.OpenCreative;
import ua.mcchickenstudio.opencreative.plugin.coding.modules.ModulesBrowserMenu;
import ua.mcchickenstudio.opencreative.plugin.indev.messages.PlaceholderReplacer;
import ua.mcchickenstudio.opencreative.plugin.menus.AbstractMenu;
import ua.mcchickenstudio.opencreative.plugin.menus.world.browsers.WorldsBrowserMenu;
import ua.mcchickenstudio.opencreative.plugin.planets.Planet;
import ua.mcchickenstudio.opencreative.plugin.settings.Sounds;
import ua.mcchickenstudio.opencreative.plugin.utils.MessageUtils;
import ua.mcchickenstudio.opencreative.plugin.wanders.OfflineWander;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static ua.mcchickenstudio.opencreative.plugin.utils.ItemUtils.*;
import static ua.mcchickenstudio.opencreative.plugin.utils.MessageUtils.*;

public class WanderMenu extends AbstractMenu {

    private final String nickname;
    private final OfflineWander wander;

    private final ItemStack ABOUT_WANDER;
    private final ItemStack STATISTICS;
    private final ItemStack SOCIAL_LINKS;
    private final ItemStack WORLDS;
    private final ItemStack MODULES;
    private final ItemStack FAVORITE_WORLDS;

    public WanderMenu(@NotNull String nickname) {
        super(4, toComponent(substring(getPlayerLocaleMessageString("menus.player-profile.title",
                Bukkit.getOfflinePlayer(nickname)), 30)));
        this.nickname = nickname;
        this.wander = OpenCreative.getOfflineWander(Bukkit.getOfflinePlayer(nickname).getUniqueId());

        int worldsAmount = OpenCreative.getPlanetsManager().getPlanetsByOwner(nickname).size();
        int modulesAmount = OpenCreative.getModuleManager().getPlayerModules(wander.getUniqueId()).size();
        int favoritesAmount = OpenCreative.getPlanetsManager().getFavoritePlanets(wander).size();

        ABOUT_WANDER = getHead();
        STATISTICS = getStatistics(worldsAmount, modulesAmount);
        SOCIAL_LINKS = getSocialLinks();
        WORLDS = createItem(Material.GRASS_BLOCK, worldsAmount, "menus.player-profile.items.worlds", "worlds");
        MODULES = createItem(Material.CHEST, modulesAmount, "menus.player-profile.items.modules", "modules");
        FAVORITE_WORLDS = createItem(Material.END_CRYSTAL, favoritesAmount, "menus.player-profile.items.favorite-worlds", "favorites");
    }

    private ItemStack getStatistics(int worldsAmount, int modulesAmount) {
        ItemStack item = createItem(Material.KNOWLEDGE_BOOK, 1, "menus.player-profile.items.statistics");
        int time = wander.getOfflinePlayer().getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 * 1000;
        replacePlaceholdersInItem(item, new PlaceholderReplacer("worlds", worldsAmount,
                "modules", modulesAmount, "playtime", convertTime(time), "visits", wander.getVisits()));
        return item;
    }

    private ItemStack getSocialLinks() {
        ItemStack item = createItem(Material.NAME_TAG, 1, "menus.player-profile.items.social-links");
        List<String> socialSites = List.of("discord", "youtube", "telegram", "twitter");
        Object[] replacement = new Object[socialSites.size() * 2];
        int index = 0;
        for (String site : socialSites) {
            String link = wander.getLink(site);
            if (link == null) {
                link = getLocaleMessageString("menus.player-profile.items.social-links.unknown", false);
            }
            replacement[index++] = site; // discord
            replacement[index++] = link; // username
        }
        replacePlaceholdersInItem(item, new PlaceholderReplacer(replacement));
        return item;
    }

    private ItemStack getHead() {
        ItemStack item = createItem(Material.PLAYER_HEAD, 1, "menus.player-profile.items.player");
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        List<Component> lore = new ArrayList<>();
        OfflinePlayer offlinePlayer = wander.getOfflinePlayer();
        for (Component loreLine : MessageUtils.getLocaleItemDescription("menus.player-profile.items.player.lore")) {
            if (((TextComponent)loreLine).content().contains("%description%")) {
                String description = wander.getDescription() == null ? getLocaleMessageString("profiles.default-description") : wander.getDescription();
                String[] newLines = description.split("\\\\n");
                for (String newLine : newLines) {
                    lore.add(
                            loreLine.replaceText(TextReplacementConfig.builder()
                                    .match("%description%")
                                    .replacement(toComponent(newLine))
                                    .build()));
                }
            } else {
                lore.add(parsePAPI(offlinePlayer, loreLine));
            }
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        if (meta instanceof SkullMeta skullMeta) {
            PlayerProfile profile = Bukkit.createProfile(nickname);
            skullMeta.setPlayerProfile(profile);
            item.setItemMeta(skullMeta);
        }
        replacePlaceholdersInItem(item, new PlaceholderReplacer("player", nickname,
                "gender",  wander.getGender() == null ? OfflineWander.Gender.UNKNOWN.getLocaleName() : wander.getGender().getLocaleName()));
        return item;
    }

    @Override
    public void fillItems(Player player) {
        setItem(10, WORLDS);
        setItem(13, MODULES);
        setItem(16, FAVORITE_WORLDS);

        setItem(29, STATISTICS);
        setItem(31, ABOUT_WANDER);
        setItem(33, SOCIAL_LINKS);
        setItem(createItem(Material.CYAN_STAINED_GLASS_PANE, 1), 28, 34);
        setItem(DECORATION_PANE_ITEM, 27, 35);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        switch (getItemType(item)) {
            case "worlds" ->
                    new WorldsBrowserMenu(player, OpenCreative.getPlanetsManager().getPlanetsByOwner(nickname)).open(player);
            case "modules" ->
                    new ModulesBrowserMenu(player, new ArrayList<>(OpenCreative.getModuleManager().getPlayerModules(wander.getUniqueId()))).open(player);
            case "favorites" -> {
                Set<Planet> favorites = new LinkedHashSet<>();
                for (int id : wander.getFavoriteWorlds()) {
                    Planet planet = OpenCreative.getPlanetsManager().getPlanetById(String.valueOf(id));
                    if (planet != null) favorites.add(planet);
                }
                new WorldsBrowserMenu(player, favorites).open(player);
            }
        }
    }

    @Override
    public void onOpen(@NotNull InventoryOpenEvent event) {
        Sounds.MENU_OPEN_PROFILE.play(event.getPlayer());
    }
}
