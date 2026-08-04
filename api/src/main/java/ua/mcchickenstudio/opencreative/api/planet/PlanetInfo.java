package ua.mcchickenstudio.opencreative.api.planet;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * <h1>PlanetInfo</h1>
 * This class represents an information of planet. It contains
 * display name, description, custom ID, category, reputation
 * and icon of planet.
 *
 * <p>This information will be displayed in worlds browser
 * or in advertisement messages.
 * </p>
 */
public interface PlanetInfo {
    /**
     * Loads information from world's settings.
     */
    void loadInformation();

    /**
     * Updates icon in asynchronous task. Used to not
     * load the main thread.
     */
    void updateIconAsync();

    /**
     * Updates icon with current planet's information.
     */
    void updateIcon();

    /**
     * Removes custom ID from world.
     */
    void resetCustomID();

    /**
     * Returns text component of display name, that can be
     * used in item stacks or texts.
     *
     * @return display name of planet.
     */
    Component displayName();

    /**
     * Returns text component of description, that can be
     * used in item stacks or texts.
     *
     * @return description of planet.
     */
    Component description();

    /**
     * Returns display name, that stores in
     * world's settings.
     *
     * @return display name of planet.
     */
    String getDisplayName();

    /**
     * Sets new display name of planet, that will be displayed
     * in worlds browser menu and advertisements.
     *
     * @param name new display name.
     */
    void setDisplayName(String name);

    /**
     * Returns description, that stores in
     * world's settings.
     *
     * @return description of planet.
     */
    String getDescription();

    /**
     * Sets new description of planet, that will be displayed
     * in worlds browser menu.
     *
     * @param description new description.
     */
    void setDescription(String description);

    /**
     * Returns text custom ID of planet,
     * that can be used with /join.
     *
     * @return custom ID of planet.
     */
    String getCustomID();

    /**
     * Sets new text custom ID, that can be used to
     * join world with short /join command.
     *
     * @param customID new custom ID.
     */
    void setCustomID(@NotNull String customID);

    /**
     * Returns icon of planet. If planet is closed,
     * it will have type of barrier.
     *
     * @return icon of planet
     */
    ItemStack getIcon();

    /**
     * Sets new item stack as planet's icon. Name, lore
     * and enchantments will be removed from item.
     *
     * @param itemStack new icon.
     */
    void setIcon(ItemStack itemStack);

    /**
     * Returns count of unique visitors.
     *
     * @return count of uniques.
     */
    int getUniques();

    /**
     * Sets count of unique visitors.
     *
     * @param uniques new count.
     */
    void setUniques(int uniques);

    /**
     * Returns rating of planet. It's calculated
     * by subtracting likes count by count of dislikes.
     * It's displayed in worlds browser menu.
     *
     * @return reputation of planet.
     */
    int getReputation();

    /**
     * Sets rating of planet. It's displayed
     * in worlds browser menu.
     *
     * @param reputation new reputation.
     */
    void setPlanetReputation(int reputation);

    /**
     * Checks if world can be used as template in
     * worlds generation menu.
     *
     * @return true - can be used as template, false - not.
     */
    boolean isDownloadable();

    /**
     * Sets can be world used as template to generate
     * a new world.
     *
     * @param downloadable true - can be used, false - not.
     */
    void setDownloadable(boolean downloadable);

    /**
     * Returns inaccurate amount of players in world, that changes
     * with entering or leaving the world, doesn't check real amount
     * of world.
     * <p>Useful to avoid lags by checking all world players.
     *
     * @return inaccurate amount of players in world.
     * @see Planet#getPlayers()
     */
    int getAsyncOnline();

    /**
     * Returns assigned planet.
     *
     * @return assigned planet.
     */
    @NotNull Planet getPlanet();
}
