
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

package ua.mcchickenstudio.opencreative.plugin.listeners.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import ua.mcchickenstudio.opencreative.plugin.OpenCreative;
import ua.mcchickenstudio.opencreative.plugin.coding.blocks.events.player.fighting.PlayerDeathEvent;
import ua.mcchickenstudio.opencreative.plugin.coding.blocks.events.player.fighting.PlayerKilledPlayerEvent;
import ua.mcchickenstudio.opencreative.plugin.planets.Planet;
import ua.mcchickenstudio.opencreative.plugin.planets.PlanetFlags;
import ua.mcchickenstudio.opencreative.plugin.settings.items.Items;
import ua.mcchickenstudio.opencreative.plugin.utils.PlayerUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static ua.mcchickenstudio.opencreative.plugin.utils.MessageUtils.getLocaleMessage;
import static ua.mcchickenstudio.opencreative.plugin.utils.MessageUtils.getLocaleMessageString;
import static ua.mcchickenstudio.opencreative.plugin.utils.world.WorldUtils.isLobbyWorld;

public final class DeathListener implements Listener {

    public static final Map<UUID, Location> deathLocations = new HashMap<>();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        Player player = event.getPlayer();
        Planet planet = OpenCreative.getPlanetsManager().getPlanetByPlayer(player);
        if (planet != null) {
            event.deathMessage(null);
            deathLocations.put(player.getUniqueId(), planet.getTerritory().getSpawnLocation());
            if (planet.getFlagValue(PlanetFlags.PlanetFlag.DEATH_MESSAGES) == 1) {
                for (Player p : planet.getPlayers()) {
                    p.sendMessage("§7 " + player.getName() + "§f " + translateDeathMessage(player));
                }
            }
            event.getDrops().remove(Items.WORLD_SETTINGS.get(player));
            new PlayerDeathEvent(player, event).callEvent();
            Player killer = player.getKiller();
            if (killer != null) {
                new PlayerKilledPlayerEvent(killer, player, event).callEvent();
            }
            player.showTitle(Title.title(
                    (getLocaleMessage("deaths.title", false)), Component.text("§7 " + player.getName() + "§f " + translateDeathMessage(player)),
                    Title.Times.times(Duration.ofMillis(750), Duration.ofSeconds(2), Duration.ofMillis(500))
            ));
        } else if (isLobbyWorld(event.getPlayer().getWorld())) {
            event.deathMessage(null);
            event.setKeepInventory(true);
            event.setCancelled(true);
            PlayerUtils.teleportToLobby(player);
        }

    }

    private String translateDeathMessage(Player player) {
        EntityDamageEvent damageEvent = player.getLastDamageCause();
        if (damageEvent == null) return getLocaleMessageString("deaths.custom");
        Entity damager = player.getKiller();
        if (damageEvent instanceof EntityDamageByEntityEvent damageByEntityEvent) {
            damager = damageByEntityEvent.getDamager();
        }
        return switch (damageEvent.getCause()) {
            case BLOCK_EXPLOSION -> getLocaleMessageString("deaths.block-explosion");
            case CONTACT -> getLocaleMessageString("deaths.contact");
            case CRAMMING -> getLocaleMessageString("deaths.cramming");
            case DRAGON_BREATH -> getLocaleMessageString("deaths.dragon-breath");
            case DROWNING -> getLocaleMessageString("deaths.drowning");
            case DRYOUT -> getLocaleMessageString("deaths.dryout");
            case ENTITY_ATTACK ->
                    getLocaleMessageString("deaths.entity-attack").replace("%entity%", (damager == null ? "" : damager.getName().substring(0, Math.min(damager.getName().length(), 30))));
            case ENTITY_EXPLOSION ->
                    getLocaleMessageString("deaths.entity-explosion").replace("%entity%", (damager == null ? "" : damager.getName().substring(0, Math.min(damager.getName().length(), 30))));
            case ENTITY_SWEEP_ATTACK ->
                    getLocaleMessageString("deaths.entity-sweep-attack").replace("%entity%", (damager == null ? "" : damager.getName().substring(0, Math.min(damager.getName().length(), 30))));
            case FALL -> getLocaleMessageString("deaths.fall");
            case FALLING_BLOCK -> getLocaleMessageString("deaths.falling-block");
            case FIRE -> getLocaleMessageString("deaths.fire");
            case FIRE_TICK -> getLocaleMessageString("deaths.fire-tick");
            case FLY_INTO_WALL -> getLocaleMessageString("deaths.fly-into-wall");
            case HOT_FLOOR -> getLocaleMessageString("deaths.hot-floor");
            case LAVA -> getLocaleMessageString("deaths.lava");
            case LIGHTNING -> getLocaleMessageString("deaths.lightning");
            case MAGIC -> getLocaleMessageString("deaths.magic");
            case MELTING -> getLocaleMessageString("deaths.melting");
            case POISON -> getLocaleMessageString("deaths.poison");
            case PROJECTILE -> getLocaleMessageString("deaths.projectile");
            case STARVATION -> getLocaleMessageString("deaths.starvation");
            case SUFFOCATION -> getLocaleMessageString("deaths.suffocation");
            case SUICIDE -> getLocaleMessageString("deaths.suicide");
            case THORNS -> getLocaleMessageString("deaths.thorns");
            case VOID -> getLocaleMessageString("deaths.void");
            case WITHER -> getLocaleMessageString("deaths.wither");
            default -> getLocaleMessageString("deaths.custom");
        };
    }
}
