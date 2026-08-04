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

package ua.mcchickenstudio.opencreative.plugin.planets;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ua.mcchickenstudio.opencreative.plugin.OpenCreative;
import ua.mcchickenstudio.opencreative.plugin.utils.ItemUtils;

import java.util.ArrayList;
import java.util.List;

import static ua.mcchickenstudio.opencreative.plugin.planets.Planet.Sharing.PUBLIC;
import static ua.mcchickenstudio.opencreative.plugin.utils.FileUtils.getPlanetConfig;
import static ua.mcchickenstudio.opencreative.plugin.utils.FileUtils.getPlayersFromPlanetList;
import static ua.mcchickenstudio.opencreative.plugin.utils.ItemUtils.*;
import static ua.mcchickenstudio.opencreative.plugin.utils.MessageUtils.*;

public class PlanetInfo implements ua.mcchickenstudio.opencreative.api.planet.PlanetInfo {

    private final Planet planet;

    private String displayName;
    private String description;
    private String customID;

    private int uniques;
    private int reputation;
    private Category category;
    private ItemStack icon;
    private boolean downloadable;
    private int online;

    public PlanetInfo(Planet planet) {
        this.planet = planet;
        loadInformation();
    }

    public PlanetInfo(@NotNull Planet planet, @Nullable String displayName,
                      @Nullable String description, @Nullable String customID,
                      @Nullable String icon, @Nullable Category category,
                      int uniques, int reputation) {
        this.planet = planet;
        this.displayName = displayName == null ? "Unknown name" : displayName;
        this.description = description == null ? "World data is corrupted,\\nplease report server admin\\nabout this world." : description;
        this.customID = customID == null ? String.valueOf(planet.getId()) : customID;
        this.category = category == null ? Category.SANDBOX : category;
        if (icon != null) {
            Material material = Material.matchMaterial(icon);
            if (material != null && material.isItem()) {
                this.icon = new ItemStack(material, 1);
            } else {
                this.icon = ItemUtils.loadItemFromByteArray(icon);
            }
        } else {
            this.icon = new ItemStack(Material.REDSTONE);
        }
        this.uniques = uniques;
        this.reputation = reputation;
    }

    @Override
    public void loadInformation() {
        FileConfiguration config = getPlanetConfig(planet);
        String name = "Unknown name";
        String description = "World data is corrupted,\\nplease report server admin\\nabout this world.";
        String customID = String.valueOf(planet.getId());
        Category category = Category.SANDBOX;
        ItemStack icon = new ItemStack(Material.REDSTONE);
        boolean downloadable = false;
        reputation = getPlayersFromPlanetList(planet, Planet.PlayersType.LIKED).size() - getPlayersFromPlanetList(planet, Planet.PlayersType.DISLIKED).size();
        uniques = getPlayersFromPlanetList(planet, Planet.PlayersType.UNIQUE).size();
        if (config.getString("name") != null) {
            name = config.getString("name");
        }
        if (config.getString("description") != null) {
            description = config.getString("description");
        }
        if (config.getString("customID") != null) {
            customID = config.getString("customID");
        }
        if (config.getString("category") != null) {
            try {
                category = Category.valueOf(config.getString("category"));
            } catch (Exception error) {
                category = Category.SANDBOX;
            }
        }
        if (config.get("icon") != null) {
            try {
                if (config.isString("icon")) {
                    Material material = Material.matchMaterial(config.getString("icon", ""));
                    if (material != null && material.isItem()) {
                        icon = new ItemStack(material, 1);
                    } else {
                        icon = ItemUtils.loadItemFromByteArray(config.getString("icon", ""));
                    }
                } else if (config.isConfigurationSection("icon")) {
                    ConfigurationSection section = config.getConfigurationSection("icon");
                    if (section != null) {
                        icon = ItemStack.deserialize(section.getValues(true));
                    }
                }
            } catch (Exception ignored) {
                icon = new ItemStack(Material.REDSTONE, 1);
            }
            if (icon.isEmpty()) icon = new ItemStack(Material.REDSTONE, 1);
        }
        if (config.getString("downloadable") != null) {
            downloadable = config.getBoolean("downloadable");
        }
        this.displayName = name;
        this.description = description;
        this.category = category;
        this.customID = customID;
        this.downloadable = downloadable;
        this.icon = icon;
    }

    @Override
    public void updateIconAsync() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateIcon();
            }
        }.runTaskAsynchronously(OpenCreative.getPlugin());
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!planet.isLoaded()) {
                    online = 0;
                    return;
                }
                online = planet.getPlayers().size();
            }
        }.runTask(OpenCreative.getPlugin());
    }

    @Override
    public void updateIcon() {
        ItemStack item = icon.clone();
        ItemMeta meta = item.getItemMeta();
        meta.displayName(
                getLocaleComponent("menus.all-worlds.items.world.name")
                        .replaceText(TextReplacementConfig.builder()
                                .match("%planetName%")
                                .replacement(displayName()).build()));
        List<Component> lore = new ArrayList<>();
        for (Component loreLine : getLocaleItemDescription("menus.all-worlds.items.world.lore")) {
            if (((TextComponent) loreLine).content().contains("%planetDescription%")) {
                String[] newLines = this.description.split("\\\\n");
                for (String newLine : newLines) {
                    lore.add(
                            loreLine.replaceText(TextReplacementConfig.builder()
                                    .match("%planetDescription%")
                                    .replacement(toComponent(newLine))
                                    .build()));
                }
            } else {
                lore.add(parsePlanetLines(this.planet, loreLine));
            }
        }
        item.setAmount((Math.max(planet.getOnline(), 1)));
        meta.lore(lore);
        item.setItemMeta(meta);
        clearItemFlags(item);
        setPersistentData(item, getItemIdKey(), customID);
        icon = item;
    }

    @Override
    public void resetCustomID() {
        this.customID = String.valueOf(planet.getId());
        planet.getConfiguration().remove("customID");
    }

    @Override
    public Component displayName() {
        return LegacyComponentSerializer.legacySection().deserialize(displayName).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    @Override
    public Component description() {
        return LegacyComponentSerializer.legacySection().deserialize(description);
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String name) {
        this.displayName = name;
        planet.getConfiguration().set("name", name);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
        planet.getConfiguration().set("description", description);
    }

    /**
     * Returns category of planet.
     *
     * @return category of planet.
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Sets category of planet, that will be displayed
     * in worlds browser menu.
     *
     * @param category new category.
     */
    public void setCategory(Category category) {
        this.category = category;
        planet.getConfiguration().set("category", category.toString());
    }

    @Override
    public String getCustomID() {
        return customID;
    }

    @Override
    public void setCustomID(@NotNull String customID) {
        this.customID = customID;
        planet.getConfiguration().set("customID", customID);
    }

    @Override
    public ItemStack getIcon() {
        if (planet.getSharing() == PUBLIC) return icon;
        else {
            return icon.clone().withType(Material.BARRIER);
        }
    }

    @Override
    public void setIcon(ItemStack itemStack) {
        ItemStack newIcon = clearItemMeta(itemStack.clone());
        newIcon.setAmount(1);
        if (ItemUtils.doesItemRequireSpecialData(newIcon)) {
            planet.getConfiguration().set("icon", ItemUtils.saveItemAsByteArray(newIcon));
        } else {
            planet.getConfiguration().set("icon", newIcon.getType().name());
        }
        this.icon = newIcon;
        updateIcon();
    }

    @Override
    public int getUniques() {
        return uniques;
    }

    @Override
    public void setUniques(int uniques) {
        this.uniques = uniques;
    }

    @Override
    public int getReputation() {
        return reputation;
    }

    @Override
    public void setPlanetReputation(int reputation) {
        this.reputation = reputation;
    }

    @Override
    public boolean isDownloadable() {
        return downloadable;
    }

    @Override
    public void setDownloadable(boolean downloadable) {
        this.downloadable = downloadable;
        planet.getConfiguration().set("downloadable", downloadable);
    }

    @Override
    public int getAsyncOnline() {
        return online;
    }

    @Override
    public @NotNull Planet getPlanet() {
        return planet;
    }

    public enum Category {

        SANDBOX(Material.HORN_CORAL),
        ADVENTURE(Material.FIRE_CHARGE),
        STRATEGY(Material.ZOMBIE_HEAD),
        ARCADE(Material.HEART_OF_THE_SEA),
        ROLEPLAY(Material.CHERRY_SAPLING),
        STORY(Material.ENCHANTED_BOOK),
        SIMULATOR(Material.PUFFERFISH),
        EXPERIMENT(Material.NETHER_WART);

        private final Material material;

        Category(Material material) {
            this.material = material;
        }

        public Material getMaterial() {
            return material;
        }

        public String getLocaleName() {
            return getLocaleMessageString("world.categories." + name().toLowerCase(), true);
        }
    }
}
