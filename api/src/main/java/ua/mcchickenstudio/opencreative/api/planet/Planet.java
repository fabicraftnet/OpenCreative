package ua.mcchickenstudio.opencreative.api.planet;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * <h1>Planet</h1>
 * This class represents a Planet, the individual place for players that
 * consists of two worlds: for building and for developing. It has owner,
 * world size, sharing, world mode, limits, flags, players data, variables
 * and states.
 *
 * <p>Planet files are stored in ./planets/planetID folder.</p>
 *
 * @author McChicken Studio
 * @version 6.0
 * @since 1.0
 */
public interface Planet {
    /**
     * Returns information of planet, that stores
     * display name, description, custom ID and icon.
     *
     * @return planet's info.
     */
    PlanetInfo getInformation();

    /**
     * Returns list of all players in world.
     * <p>
     * Includes all players from build world and developer's world.
     *
     * @return list of all online players in world.
     */
    List<Player> getPlayers();
}
