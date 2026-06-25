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

package ua.mcchickenstudio.opencreative.listeners.world;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import ua.mcchickenstudio.opencreative.OpenCreative;
import ua.mcchickenstudio.opencreative.planets.Planet;

import static ua.mcchickenstudio.opencreative.utils.world.WorldUtils.isDevPlanet;
import static ua.mcchickenstudio.opencreative.utils.world.WorldUtils.isPlanet;

/**
 * <h1>WorldListener</h1>
 * This class represents a listener for world events.
 */
public final class WorldListener implements Listener {

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (!isPlanet(event.getWorld())) {
            return;
        }
        Planet planet = OpenCreative.getPlanetsManager().getPlanetByWorld(event.getWorld());
        if (planet == null) return;
        if (isDevPlanet(event.getWorld())) {
            planet.getDevPlanet().setWorld(event.getWorld().getUID());
        } else {
            planet.getTerritory().setWorld(event.getWorld().getUID());
        }
    }

    @EventHandler
    public void onWorldLoad(WorldUnloadEvent event) {
        if (!isPlanet(event.getWorld())) {
            return;
        }
        Planet planet = OpenCreative.getPlanetsManager().getPlanetByWorld(event.getWorld());
        if (planet == null) return;
        if (isDevPlanet(event.getWorld())) {
            planet.getDevPlanet().setWorld(event.getWorld().getUID());
        } else {
            planet.getTerritory().setWorld(event.getWorld().getUID());
        }
    }

}
