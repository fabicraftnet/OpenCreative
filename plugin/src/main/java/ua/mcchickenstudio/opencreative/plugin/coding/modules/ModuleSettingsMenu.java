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

package ua.mcchickenstudio.opencreative.plugin.coding.modules;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import ua.mcchickenstudio.opencreative.plugin.OpenCreative;
import ua.mcchickenstudio.opencreative.plugin.menus.AbstractMenu;
import ua.mcchickenstudio.opencreative.plugin.menus.ConfirmationMenu;
import ua.mcchickenstudio.opencreative.plugin.menus.buttons.ParameterButton;
import ua.mcchickenstudio.opencreative.plugin.planets.DevPlanet;
import ua.mcchickenstudio.opencreative.plugin.settings.Sounds;
import ua.mcchickenstudio.opencreative.plugin.utils.MessageUtils;
import ua.mcchickenstudio.opencreative.plugin.utils.PlayerConfirmation;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static ua.mcchickenstudio.opencreative.plugin.utils.ItemUtils.*;
import static ua.mcchickenstudio.opencreative.plugin.utils.MessageUtils.*;

public final class ModuleSettingsMenu extends AbstractMenu {

    private final Module module;
    private final Player player;
    private final ItemStack name = createItem(Material.JUNGLE_SIGN, 1, "menus.module-settings.items.change-name", "name");
    private final ItemStack description = createItem(Material.WRITABLE_BOOK, 1, "menus.module-settings.items.change-description", "description");
    private final ItemStack install = createItem(Material.CHERRY_CHEST_BOAT, 1, "menus.module-settings.items.install", "install");
    private final ItemStack delete = createItem(Material.TNT_MINECART, 1, "menus.module-settings.items.delete", "delete");
    private final ParameterButton access;
    private ItemStack moduleIcon;

    public ModuleSettingsMenu(Module module, Player player) {
        super(4, getLocaleMessage("menus.module-settings.title", false));
        this.module = module;
        this.player = player;
        moduleIcon = getModuleIcon();
        access = new ParameterButton(module.getInformation().isPublic() ? "public" : "private",
                List.of("public", "private"), "access", "menus.module-settings", "menus.module-settings.items.change-sharing",
                List.of(Material.LIME_DYE, Material.GRAY_DYE));
    }

    @Override
    public void fillItems(Player player) {
        setItem(10, name);
        setItem(11, description);
        setItem(16, access.getItem());

        setItem(27, install);
        setItem(28, DECORATION_PANE_ITEM);
        setItem(29, createItem(Material.BROWN_STAINED_GLASS_PANE, 1));

        setItem(31, moduleIcon);

        setItem(33, createItem(Material.BROWN_STAINED_GLASS_PANE, 1));
        setItem(34, DECORATION_PANE_ITEM);
        setItem(35, delete);
    }

    public ItemStack getModuleIcon() {
        ItemStack item = clearItemMeta(module.getInformation().getIcon().clone());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtils.toComponent( MessageUtils.getLocaleItemNameString("menus.module-settings.items.module.name")
                .replace("%moduleName%", module.getInformation().getDisplayName())));
        List<Component> lore = new ArrayList<>();
        for (Component loreLine : MessageUtils.getLocaleItemDescription("menus.module-settings.items.module.lore")) {
            if (((TextComponent)loreLine).content().contains("%moduleDescription%")) {
                String[] newLines = module.getInformation().getDescription().split("\\\\n");
                for (String newLine : newLines) {
                    lore.add(
                            loreLine.replaceText(TextReplacementConfig.builder()
                                    .match("%moduleDescription%")
                                    .replacement(toComponent(newLine))
                                    .build()));
                }
            } else {
                lore.add(MessageUtils.parseModuleLines(module, loreLine));
            }
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        clearItemFlags(item);
        return item;
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event) {
        if (!isClickedInMenuSlots(event) || !isPlayerClicked(event)) {
            return;
        }
        event.setCancelled(true);
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null) {
            return;
        }
        if (itemEquals(currentItem, name)) {
            player.showTitle(Title.title(
                    (getLocaleMessage("settings.module-name.title")), (getLocaleMessage("settings.module-name.subtitle")),
                    Title.Times.times(Duration.ofMillis(100), Duration.ofSeconds(30), Duration.ofMillis(130))
            ));
            player.sendMessage(toComponent(getLocaleMessageString("settings.module-name.usage").replace("%player%", player.getName())));
            player.closeInventory();
            if (!PlayerConfirmation.hasConfirmation(player)) {
                PlayerConfirmation.setConfirmation(player, PlayerConfirmation.MODULE_NAME_CHANGE, module.getId());
            }
        } else if (itemEquals(currentItem, description)) {
            player.showTitle(Title.title(
                    (getLocaleMessage("settings.module-description.title")), (getLocaleMessage("settings.module-description.subtitle")),
                    Title.Times.times(Duration.ofMillis(100), Duration.ofSeconds(30), Duration.ofMillis(130))
            ));
            player.sendMessage(getLocaleMessage("settings.module-description.usage"));
            player.closeInventory();
            if (!PlayerConfirmation.hasConfirmation(player)) {
                PlayerConfirmation.setConfirmation(player, PlayerConfirmation.MODULE_DESCRIPTION_CHANGE, module.getId());
            }
        } else if (itemEquals(currentItem, access.getItem())) {
            access.next();
            setItem(event.getRawSlot(), access.getItem());
            if ("public".equals(access.getCurrentValue().toString())) {
                module.getInformation().setPublic(true);
                player.sendMessage(getLocaleMessage("settings.module-sharing.enabled"));
                Sounds.WORLD_SETTINGS_SHARING_PUBLIC.play(player);
            } else {
                module.getInformation().setPublic(false);
                player.sendMessage(getLocaleMessage("settings.module-sharing.disabled"));
                Sounds.WORLD_SETTINGS_SHARING_PRIVATE.play(player);
            }
            moduleIcon = getModuleIcon();
            setItem(31, moduleIcon);
        } else if (itemEquals(currentItem, moduleIcon)) {
            if (event.getCursor().isEmpty()) {
                player.sendMessage(getLocaleMessage("settings.module-icon.error"));
                Sounds.PLAYER_FAIL.play(player);
            } else {
                module.getInformation().setIcon(event.getCursor());
                player.sendMessage(getLocaleMessage("settings.module-icon.changed"));
                moduleIcon = getModuleIcon();
                setItem(31, moduleIcon);
                event.setCursor(null);
            }
        } else if (itemEquals(currentItem, install)) {
            player.closeInventory();
            DevPlanet devPlanet = OpenCreative.getPlanetsManager().getDevPlanet(player);
            if (devPlanet == null) {
                player.sendMessage(getLocaleMessage("only-in-dev-world"));
                Sounds.PLAYER_FAIL.play(player);
                return;
            }
            if (!devPlanet.getPlanet().getWorldPlayers().canDevelop(player)) {
                player.sendMessage(getLocaleMessage("not-developer"));
                Sounds.PLAYER_FAIL.play(player);
                return;
            }
            module.place(devPlanet, player);
        } else if (itemEquals(currentItem, delete)) {
            player.closeInventory();
            Bukkit.getScheduler().scheduleSyncDelayedTask(OpenCreative.getPlugin(),
                    () -> new ConfirmationMenu(
                            toComponent( getLocaleMessageString("menus.confirmation.delete-module", false).replace("%name%", substring(ChatColor.stripColor(module.getInformation().getDisplayName()), 20))),
                            Material.TNT,
                            getLocaleItemName("menus.confirmation.items.delete-module.name"),
                            getLocaleItemDescription("menus.confirmation.items.delete-module.lore"),
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    player.closeInventory();
                                    if (!OpenCreative.getModuleManager().getModules().contains(module)) {
                                        cancel();
                                        return;
                                    }
                                    if (!module.isOwner(player)) {
                                        cancel();
                                        return;
                                    }
                                    Sounds.WORLD_DELETION.play(player);
                                    OpenCreative.getPlugin().getLogger().info("Module " + module.getId() + " is being deleted by module's owner " + player.getName());
                                    OpenCreative.getModuleManager().deleteModule(module);
                                    Bukkit.getServer().getScheduler().runTaskLater(OpenCreative.getPlugin(), () ->
                                            player.sendMessage(toComponent(MessageUtils.getLocaleMessageString("modules.deleted")
                                                    .replace("%moduleID%", String.valueOf(module.getId())))), 60);
                                }
                            }).open(player), 5L);
        }

    }

    @Override
    public void onOpen(@NotNull InventoryOpenEvent event) {
        if (!module.isOwner(player)) {
            event.setCancelled(true);
            return;
        }
        Sounds.MENU_OPEN_MODULE_SETTINGS.play(player);
    }
}
