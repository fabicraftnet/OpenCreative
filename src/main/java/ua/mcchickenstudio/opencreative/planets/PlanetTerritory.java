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

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.util.TriState;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ua.mcchickenstudio.opencreative.OpenCreative;
import ua.mcchickenstudio.opencreative.coding.CodeScript;
import ua.mcchickenstudio.opencreative.coding.blocks.events.player.world.QuitEvent;
import ua.mcchickenstudio.opencreative.events.planet.PlanetLoadEvent;
import ua.mcchickenstudio.opencreative.events.planet.PlanetUnloadEvent;
import ua.mcchickenstudio.opencreative.utils.FileUtils;
import ua.mcchickenstudio.opencreative.utils.ItemUtils;
import ua.mcchickenstudio.opencreative.utils.world.WorldUtils;
import ua.mcchickenstudio.opencreative.utils.world.generators.EnvironmentCapable;
import ua.mcchickenstudio.opencreative.utils.world.generators.StructuresCapable;
import ua.mcchickenstudio.opencreative.utils.world.generators.WorldGenerator;
import ua.mcchickenstudio.opencreative.utils.world.generators.WorldGenerators;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static ua.mcchickenstudio.opencreative.utils.FileUtils.*;
import static ua.mcchickenstudio.opencreative.utils.MessageUtils.clearOnceMessages;
import static ua.mcchickenstudio.opencreative.utils.MessageUtils.getLocaleMessage;
import static ua.mcchickenstudio.opencreative.utils.PlayerUtils.isEntityInDevPlanet;
import static ua.mcchickenstudio.opencreative.utils.PlayerUtils.teleportToLobby;

/**
 * <h1>PlanetTerritory</h1>
 * This class represents a territory, where planet players can build or play.
 * It stores one-time information that will be removed on world unloading.
 */
public class PlanetTerritory {

    private final Planet planet;
    private final PlanetFlags flags;
    private final PlanetScoreboards scoreboards;
    private final PlanetRecipes recipes;
    private final Map<String, BossBar> bossBars = new HashMap<>();
    private final Set<BukkitRunnable> runningBukkitRunnables = ConcurrentHashMap.newKeySet();
    private final CodeScript script;

    private UUID worldID;
    private String biome;
    private int worldSize = 25;
    private String generator = "";
    private Location spawnLocation;
    private World.Environment environment;
    private boolean ignoreUnloading = false;
    private boolean autoSave = true;
    private final AtomicBoolean busy = new AtomicBoolean(false);

    public PlanetTerritory(@NotNull Planet planet) {
        this.planet = planet;
        flags = new PlanetFlags(planet);
        scoreboards = new PlanetScoreboards(planet);
        script = new CodeScript(planet);
        recipes = new PlanetRecipes(planet);
        loadInformation();
    }

    /**
     * Resets custom world size to owner's group world size.
     */
    public void resetWorldSize() {
        int worldSize = planet.getGroup().getWorldSize();
        FileUtils.removePlanetConfigParameter(planet, "size");
        if (this.worldSize == worldSize) return;
        this.worldSize = worldSize;
        if (getWorld() != null) {
            getWorld().getWorldBorder().setSize(worldSize);
            for (Player player : planet.getPlayers()) {
                showBorders(player);
            }
        }
    }

    /**
     * Sets custom size of world borders.
     *
     * @param size new size of world.
     * @param save whether ignore size from owner's group on next world load and use specified.
     */
    public void setWorldSize(int size, boolean save) {
        if (size < 0) return;
        this.worldSize = size;
        if (getWorld() != null) {
            getWorld().getWorldBorder().setSize(size);
            for (Player player : planet.getPlayers()) {
                showBorders(player);
            }
        }
        if (save) planet.getConfiguration().set("size", size);
    }

    private void loadInformation() {
        FileConfiguration config = getPlanetConfig(planet);
        World.Environment environment = World.Environment.NORMAL;
        if (config.getString("environment") != null) {
            try {
                environment = World.Environment.valueOf(config.getString("environment"));
            } catch (Exception ignored) {}
        }
        worldSize = config.getInt("size", planet.getGroup().getWorldSize());
        autoSave = config.getBoolean("autosave", true);
        biome = config.getString("biome", "");
        this.generator = config.getString("generator", "");
        this.environment = environment;
    }

    /**
     * Loads planet's files into worlds directory,
     * loads and setups build world, loads script and variables.
     */
    public @NotNull CompletableFuture<World> load() {
        CompletableFuture<World> future = new CompletableFuture<>();
        long startTime = System.currentTimeMillis();
        busy.set(true);
        planet.getConfiguration().load().whenComplete((result, configError) -> {
            // After loading config
            loadInformation();
            flags.loadFlags();
            planet.getWorldPlayers().loadPlayers();

            ConfigurationSection spawnSection = planet.getConfiguration().getConfig().getConfigurationSection("spawn");
            if (spawnSection != null) {
                double x = spawnSection.getDouble("x", 0);
                double y = spawnSection.getDouble("y", 0);
                double z = spawnSection.getDouble("z", 0);
                float yaw = (float) spawnSection.getDouble("yaw", 0);
                float pitch = (float) spawnSection.getDouble("pitch", 0);
                spawnLocation = new Location(null, x, y, z, yaw, pitch);
            }

            WorldGenerator worldGenerator = WorldGenerators.getInstance().getById(generator);
            WorldCreator creator = new WorldCreator(planet.getWorldName())
                    .environment(planet.getTerritory().getEnvironment());
            if (worldGenerator != null) {
                worldGenerator.modifyWorldCreator(creator, biome);
            }

            CompletableFuture<World> worldFuture = OpenCreative.getWorldManager().loadWorld(creator, planet);
            worldFuture.thenAccept(world -> {
                // After loading world
                if (world == null) {
                    busy.set(false);
                    future.complete(null);
                    return;
                }
                Bukkit.getScheduler().runTask(OpenCreative.getPlugin(), () -> {
                    setWorld(world.getUID());
                    world.setAutoSave(autoSave);
                    //setGameRuleIfExists("spawn_chunk_radius", 1);
                    //setGameRuleIfExists("command_blocks_work", false);
                    world.setGameRule(GameRules.COMMAND_BLOCKS_WORK, false);
                    world.setGameRule(GameRules.GLOBAL_SOUND_EVENTS, false);
                    world.setGameRule(GameRules.LIMITED_CRAFTING, true);
                    if (world.getEnvironment() == World.Environment.THE_END) {
                        if (world.getEnderDragonBattle() != null) {
                            world.getEnderDragonBattle().setPreviouslyKilled(true);
                            world.getEnderDragonBattle().getBossBar().setVisible(false);
                        }
                        Bukkit.getScheduler().runTaskLater(OpenCreative.getPlugin(), () -> {
                            for (Entity entity : world.getEntities()) {
                                if (entity instanceof EnderDragon dragon) {
                                    dragon.setHealth(0);
                                }
                            }
                        }, 10L);
                    }
                    planet.setLastActivityTime(System.currentTimeMillis());
                    world.setGameRule(GameRules.SHOW_ADVANCEMENT_MESSAGES, false);
                    world.getWorldBorder().setSize(worldSize);
                    long endTime = System.currentTimeMillis();
                    OpenCreative.getPlugin().getLogger().info("Planet " + planet.getId() + " loaded in " + (endTime - startTime) + " ms");
                    new PlanetLoadEvent(planet).callEvent();
                    busy.set(false);
                    future.complete(world);
                });
            }).exceptionally(worldError -> {
                // World failed to load
                busy.set(false);
                future.completeExceptionally(worldError);
                return null;
            });
        });
        return future;
    }

    /**
     * Saves planet's data and unloads planet's build and dev world.
     */
    public synchronized @NotNull CompletableFuture<Void> unload() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (ignoreUnloading) {
            future.complete(null);
            return future;
        }
        if (OpenCreative.getPlugin().isEnabled()) {
            Bukkit.getScheduler().runTaskLater(OpenCreative.getPlugin(), () -> {
                handleUnloadProcess(true).whenComplete((result, error) -> {
                    future.complete(null);
                });
            }, 5);
        } else {
            handleUnloadProcess(false);
            future.complete(null);
        }
        return future;
    }

    /**
     * Saves planet's data and unloads planet's build and dev world.
     */
    private @NotNull CompletableFuture<Void> handleUnloadProcess(boolean asyncSaveData) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        long startTime = System.currentTimeMillis();
        if (!planet.isLoaded()) {
            if (planet.getDevPlanet().isLoaded()) {
                planet.getDevPlanet().unload(asyncSaveData);
                long endTime = System.currentTimeMillis();
                OpenCreative.getPlugin().getLogger().info("Planet " + planet.getId() + " unloaded only dev in " + (endTime - startTime) + " ms");
            }
            future.complete(null);
            return future;
        }

        World world = getWorld();
        if (world != null) {
            for (Entity entity : world.getEntitiesByClass(Item.class)) {
                if (entity instanceof Item item) {
                    item.setItemStack(ItemUtils.fixItem(item.getItemStack()));
                }
            }
        }
        for (Player player : planet.getPlayers()) {
            new QuitEvent(player).callEvent();
        }
        if (asyncSaveData) {
            Bukkit.getScheduler().runTaskAsynchronously(OpenCreative.getPlugin(), this::saveData);
        } else {
            this.saveData();
        }
        if (world != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getWorld().equals(world)) {
                    teleportToLobby(player);
                }
            }
            if (asyncSaveData) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    world.unloadChunk(chunk.getX(), chunk.getZ(), autoSave);
                }
                try {
                    // 1.21+ Content:
                    Method saveMethod = world.getClass().getMethod("save", boolean.class);
                    saveMethod.invoke(world, false);
                } catch (Exception ignored) {
                    world.save();
                }
                busy.set(true);
                Bukkit.getScheduler().runTaskLater(OpenCreative.getPlugin(), () -> {
                    OpenCreative.getWorldManager().unloadWorld(world, false, planet).whenComplete((result, error) -> {
                        busy.set(false);
                        future.complete(null);
                    });
                }, 60);
            } else {
                OpenCreative.getWorldManager().unloadWorld(world, autoSave, planet);
                future.complete(null);
            }
        }
        worldID = null;

        if (planet.getDevPlanet().isLoaded()) {
            planet.getDevPlanet().unload(asyncSaveData);
        }

        new PlanetUnloadEvent(planet).callEvent();
        long endTime = System.currentTimeMillis();
        OpenCreative.getPlugin().getLogger().info("Planet " + planet.getId() + " unloaded in " + (endTime - startTime) + " ms");
        return future;
    }

    private void saveData() {
        planet.setLastActivityTime(System.currentTimeMillis());
        Set<Location> changes = planet.getDevPlanet().getChangedColumns();
        if (!changes.isEmpty()) {
            List<String> changesString = new ArrayList<>();
            for (Location location : changes) {
                changesString.add(location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ());
            }
            planet.getConfiguration().set("changed-code-columns", changesString);
        } else {
            FileUtils.removePlanetConfigParameter(planet, "changed-code-columns");
        }
        planet.getConfiguration().set("environment", planet.getTerritory().getEnvironment().name());
        planet.getVariables().save();
        clearData();
    }

    /**
     * Clears temporary information stored in memory.
     * Used on world unload.
     */
    public void clearData() {
        stopBukkitRunnables();
        bossBars.clear();
        scoreboards.clear();
        flags.clear();
        script.getExecutors().clear();
        planet.getVariables().unload();
        planet.getWorldPlayers().clear();
        planet.getLimits().clear();
        script.unload();
        spawnLocation = null;
        clearOnceMessages(planet);
        recipes.clear();
        planet.getConfiguration().unload();
    }

    public void addBukkitRunnable(BukkitRunnable runnable) {
        runningBukkitRunnables.add(runnable);
    }

    public void scheduleRunnable(@NotNull PlanetRunnable runnable, long delay) {
        runningBukkitRunnables.add(runnable);
        runnable.runTaskLater(OpenCreative.getPlugin(), delay);
    }

    public void scheduleAsyncRunnable(@NotNull PlanetRunnable runnable, long delay) {
        runningBukkitRunnables.add(runnable);
        runnable.runTaskLaterAsynchronously(OpenCreative.getPlugin(), delay);
    }

    public void removeBukkitRunnable(BukkitRunnable runnable) {
        runningBukkitRunnables.remove(runnable);
    }

    /**
     * Stops all running bukkit runnables and tasks in world.
     */
    public void stopBukkitRunnables() {
        if (OpenCreative.getPlugin().isEnabled()) {
            Bukkit.getScheduler().runTaskAsynchronously(OpenCreative.getPlugin(), () -> {
                for (BukkitRunnable runnable : new HashSet<>(runningBukkitRunnables)) {
                    try {
                        if (runnable != null && !runnable.isCancelled()) {
                            runnable.cancel();
                        }
                    } catch (IllegalStateException ignored) {}
                }
                runningBukkitRunnables.clear();
            });
        } else {
            for (BukkitRunnable runnable : new HashSet<>(runningBukkitRunnables)) {
                try {
                    if (runnable != null && !runnable.isCancelled()) {
                        runnable.cancel();
                    }
                } catch (IllegalStateException ignored) {}
            }
            runningBukkitRunnables.clear();
        }
    }

    /**
     * Returns flags of planet, that store additional settings.
     *
     * @return planet's flags.
     */
    public PlanetFlags getFlags() {
        return flags;
    }

    /**
     * Returns map of IDs and boss bars.
     *
     * @return map of IDs and boss bars.
     */
    public Map<String, BossBar> getBossBars() {
        return bossBars;
    }

    public World.Environment getEnvironment() {
        return environment;
    }

    /**
     * Returns world of planet for buildings.
     * If world is unloaded, returns null.
     *
     * @return planet's world, or null - if world is unloaded.
     */
    public @Nullable World getWorld() {
        return Bukkit.getWorld(worldID);
    }

    /**
     * Returns size of world, that will be used
     * to set world borders.
     *
     * @return size of world.
     */
    public int getWorldSize() {
        return worldSize;
    }

    /**
     * Returns code script of world, that stores
     * executors and actions.
     *
     * @return code script.
     */
    public @NotNull CodeScript getScript() {
        return script;
    }

    public @NotNull CompletableFuture<World> generateWorld(WorldGenerator generator, World.Environment environment,
                                         long seed, boolean generateStructures, String biome) {
        CompletableFuture<World> future = new CompletableFuture<>();
        WorldCreator worldCreator = new WorldCreator(planet.getWorldName());
        if (generator instanceof StructuresCapable) {
            worldCreator.generateStructures(generateStructures);
        }
        worldCreator.type(WorldType.FLAT);
        if (generator instanceof EnvironmentCapable) {
            worldCreator.environment(environment);
        }
        worldCreator.seed(seed);

        generator.modifyWorldCreator(worldCreator, biome);
        planet.getVariables().load();

        OpenCreative.getWorldManager().createWorld(worldCreator, planet).thenAccept(world -> {
            if (world == null) {
                future.completeExceptionally(new NullPointerException("Created world is null"));
                return;
            }
            Bukkit.getScheduler().runTask(OpenCreative.getPlugin(), () -> {
                world.setAutoSave(true);
                //setGameRuleIfExists("spawn_chunk_radius", 1);
                world.getWorldBorder().setSize(getWorldSize());

                world.setGameRule(GameRules.MOB_DROPS, true);
                world.setGameRule(GameRules.SPAWN_MOBS, false);
                world.setGameRule(GameRules.ADVANCE_TIME, false);
                world.setGameRule(GameRules.ADVANCE_WEATHER, false);
                world.setGameRule(GameRules.KEEP_INVENTORY, false);
                world.setGameRule(GameRules.MOB_GRIEFING, true);
                world.setGameRule(GameRules.NATURAL_HEALTH_REGENERATION, true);
                world.setGameRule(GameRules.IMMEDIATE_RESPAWN, false);
                world.setGameRule(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER, 0);
                world.setGameRule(GameRules.SHOW_DEATH_MESSAGES, false);
                world.setGameRule(GameRules.SHOW_ADVANCEMENT_MESSAGES, false);
                world.setGameRule(GameRules.GLOBAL_SOUND_EVENTS, false);

                world.setTime(0);
                for (Entity entity : world.getEntities()) {
                    if (entity.getType() != EntityType.PLAYER) entity.remove();
                }

                generator.afterCreation(world);
                Bukkit.getScheduler().runTaskLater(OpenCreative.getPlugin(), () -> {
                    for (Entity entity : world.getEntities()) {
                        if (entity instanceof EnderDragon dragon) {
                            dragon.setHealth(0);
                        }
                    }
                }, 10L);
                future.complete(world);
            });
        }).exceptionally(error -> {
            future.completeExceptionally(error);
            return null;
        });
        return future;
    }

    @SuppressWarnings("unchecked")
    public void setGameRuleIfExists(@NotNull String gameRule, boolean value) {
        try {
            GameRule<?> rule = GameRule.getByName(gameRule.toLowerCase());
            if (rule != null && getWorld() != null) {
                getWorld().setGameRule((GameRule<? super Boolean>) rule, value);
            }
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    public void setGameRuleIfExists(@NotNull String gameRule, int value) {
        try {
            GameRule<?> rule = GameRule.getByName(gameRule.toLowerCase());
            if (rule != null && getWorld() != null) {
                getWorld().setGameRule((GameRule<? super Integer>) rule, value);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Shows custom world borders for player.
     *
     * @param player player to show.
     */
    public void showBorders(@NotNull Player player) {
        if (isEntityInDevPlanet(player)) return;
        WorldBorder border = Bukkit.createWorldBorder();
        border.setSize(player.getWorld().getWorldBorder().getSize());
        if (planet.getMode() == Planet.Mode.PLAYING) {
            PlanetPlayer planetPlayer = planet.getWorldPlayers().getPlanetPlayer(player);
            if (planetPlayer != null && planetPlayer.getWorldSize() != null) {
                border.setSize(planetPlayer.getWorldSize());
            }
        }
        switch (planet.getFlagValue(PlanetFlags.PlanetFlag.WORLD_BORDERS)) {
            case 1 -> border.setSize(border.getSize()); // Default
            case 2 -> border.changeSize(border.getSize() + 0.001, 3600 * 20); // Green
            case 3 -> {
                border.setSize(border.getSize() + 0.1);
                player.setWorldBorder(border);
                border.changeSize(border.getSize() - 0.1, 3600 * 20); // Red
            }
            case 4 -> border.setSize(border.getMaxSize()); // Not visible
        }
        player.setWorldBorder(border);
    }

    /**
     * Returns scoreboards of planet.
     *
     * @return planet's scoreboards.
     */
    public @NotNull PlanetScoreboards getScoreboards() {
        return scoreboards;
    }

    /**
     * Returns recipes of planet.
     *
     * @return planet's recipes.
     */
    public @NotNull PlanetRecipes getRecipes() {
        return recipes;
    }

    /**
     * Returns spawn location, where players should appear
     * after connecting to planet.
     *
     * @return spawn location of planet
     */
    public @NotNull Location getSpawnLocation() {
        World world = getWorld();
        if (world == null) {
            return Objects.requireNonNullElseGet(spawnLocation, () -> new Location(null, 0, 0, 0));
        }
        if (spawnLocation != null) {
            spawnLocation.setWorld(world);
            Location location = spawnLocation;
            if (!world.getWorldBorder().isInside(location)) {
                spawnLocation = world.getWorldBorder().getCenter();
            }
            return spawnLocation;
        }
        return world.getSpawnLocation();
    }

    /**
     * Sets new spawn location for planet, where player
     * will appear after joining to planet.
     *
     * @param spawnLocation new spawn location.
     */
    public void setSpawnLocation(@NotNull Location spawnLocation) {
        this.spawnLocation = WorldUtils.roundLocation(spawnLocation);
        World world = getWorld();
        this.spawnLocation.setWorld(world);
        if (world != null) world.setSpawnLocation(spawnLocation);

        Map<String, Double> configLocation = WorldUtils.fromLocationToMap(spawnLocation);
        planet.getConfiguration().set("spawn", configLocation);
    }

    /**
     * Sets ID of planet's build world.
     *
     * @param uuid uuid of world, null - if not loaded.
     */
    public void setWorld(@Nullable UUID uuid) {
        worldID = uuid;
    }

    /**
     * Returns unique ID of loaded build world.
     *
     * @return uuid of world, null - if not loaded.
     */
    public @Nullable UUID getWorldUUID() {
        return worldID;
    }

    /**
     * Checks whether world should save territory changes.
     *
     * @return true - will be saved, false - not.
     */
    public boolean isAutoSave() {
        return autoSave;
    }

    /**
     * Checks whether world is busy for deleting or unloading,
     * so players shouldn't be able to join it.
     *
     * @return true - is busy, false - not.
     */
    public boolean isBusy() {
        return busy.get();
    }

    /**
     * Sets whether world should ignore unloading.
     *
     * @param ignoreUnloading ignore unloading.
     */
    public void setIgnoreUnloading(boolean ignoreUnloading) {
        this.ignoreUnloading = ignoreUnloading;
    }

    /**
     * Checks whether world will be not unloaded
     * by unload request.
     *
     * @return true - world cannot be unloaded, false - can be.
     */
    public boolean isIgnoringUnload() {
        return ignoreUnloading;
    }

    /**
     * Sets world generator to specified one.
     *
     * @param generator new world generator.
     */
    public void setGenerator(@Nullable WorldGenerator generator) {
        if (generator == null) {
            this.generator = "";
            removePlanetConfigParameter(planet, "generator");
            return;
        }
        this.generator = generator.getID();
        planet.getConfiguration().set("generator", generator.getID());
    }

    /**
     * Sets auto-save option to specified value.
     *
     * @param autoSave true - build world changes will be saved.
     *                 <p>false - build world changes will be not saved.</p>
     */
    public void setAutoSave(boolean autoSave) {
        if (this.autoSave == autoSave) return;
        this.autoSave = autoSave;
        for (Player planetPlayer : planet.getPlayers()) {
            planetPlayer.sendMessage(getLocaleMessage("settings.autosave." + (autoSave ? "enabled" : "disabled")));
        }
        if (getWorld() != null) {
            getWorld().setAutoSave(autoSave);
        }
        planet.getConfiguration().set("autosave", !autoSave ? false : null);
    }
}
