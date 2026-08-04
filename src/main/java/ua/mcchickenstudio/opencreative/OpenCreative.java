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

package ua.mcchickenstudio.opencreative;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ua.mcchickenstudio.opencreative.coding.blocks.actions.worldactions.world.phys.data.PhysicsManager;
import ua.mcchickenstudio.opencreative.coding.prompters.CodingPrompter;
import ua.mcchickenstudio.opencreative.coding.prompters.DisabledCodingPrompter;
import ua.mcchickenstudio.opencreative.commands.*;
import ua.mcchickenstudio.opencreative.commands.minecraft.*;
import ua.mcchickenstudio.opencreative.commands.world.*;
import ua.mcchickenstudio.opencreative.commands.world.modes.BuildCommand;
import ua.mcchickenstudio.opencreative.commands.world.modes.DevCommand;
import ua.mcchickenstudio.opencreative.commands.world.modes.PlayCommand;
import ua.mcchickenstudio.opencreative.commands.world.reputation.DislikeCommand;
import ua.mcchickenstudio.opencreative.commands.world.reputation.LikeCommand;
import ua.mcchickenstudio.opencreative.listeners.CreativeListener;
import ua.mcchickenstudio.opencreative.listeners.creative.PlanetListener;
import ua.mcchickenstudio.opencreative.listeners.entity.EntityDamageListener;
import ua.mcchickenstudio.opencreative.listeners.entity.EntitySpawnListener;
import ua.mcchickenstudio.opencreative.listeners.entity.EntityStateListener;
import ua.mcchickenstudio.opencreative.listeners.entity.WitherCreationListener;
import ua.mcchickenstudio.opencreative.listeners.player.*;
import ua.mcchickenstudio.opencreative.listeners.world.BlockChangeListener;
import ua.mcchickenstudio.opencreative.listeners.world.RedstoneListener;
import ua.mcchickenstudio.opencreative.listeners.world.WorldListener;
import ua.mcchickenstudio.opencreative.managers.Managers;
import ua.mcchickenstudio.opencreative.managers.blocks.BlocksManager;
import ua.mcchickenstudio.opencreative.managers.disguises.DisguiseManager;
import ua.mcchickenstudio.opencreative.managers.downloader.DisabledDownloader;
import ua.mcchickenstudio.opencreative.managers.downloader.DownloadManager;
import ua.mcchickenstudio.opencreative.managers.economy.DisabledEconomy;
import ua.mcchickenstudio.opencreative.managers.economy.Economy;
import ua.mcchickenstudio.opencreative.managers.hints.HintManager;
import ua.mcchickenstudio.opencreative.managers.hints.Hints;
import ua.mcchickenstudio.opencreative.managers.modules.ModuleManager;
import ua.mcchickenstudio.opencreative.managers.modules.Moduler;
import ua.mcchickenstudio.opencreative.managers.packets.PacketManager;
import ua.mcchickenstudio.opencreative.managers.space.PlanetsManager;
import ua.mcchickenstudio.opencreative.managers.space.Space;
import ua.mcchickenstudio.opencreative.managers.stability.DisabledWatchdog;
import ua.mcchickenstudio.opencreative.managers.stability.StabilityManager;
import ua.mcchickenstudio.opencreative.managers.updater.HangarUpdater;
import ua.mcchickenstudio.opencreative.managers.updater.Updater;
import ua.mcchickenstudio.opencreative.managers.worlds.VanillaWorldManager;
import ua.mcchickenstudio.opencreative.managers.worlds.WorldManager;
import ua.mcchickenstudio.opencreative.menus.Menus;
import ua.mcchickenstudio.opencreative.settings.Settings;
import ua.mcchickenstudio.opencreative.settings.Sounds;
import ua.mcchickenstudio.opencreative.utils.FileUtils;
import ua.mcchickenstudio.opencreative.utils.PlayerUtils;
import ua.mcchickenstudio.opencreative.utils.hooks.HookUtils;
import ua.mcchickenstudio.opencreative.utils.hooks.Metrics;
import ua.mcchickenstudio.opencreative.utils.world.WorldUtils;
import ua.mcchickenstudio.opencreative.utils.world.platforms.DevPlatformer;
import ua.mcchickenstudio.opencreative.utils.world.platforms.HorizontalPlatformer;
import ua.mcchickenstudio.opencreative.utils.world.platforms.VerticalPlatformer;
import ua.mcchickenstudio.opencreative.wanders.OfflineWander;
import ua.mcchickenstudio.opencreative.wanders.Wander;

import java.text.SimpleDateFormat;
import java.util.*;

import static ua.mcchickenstudio.opencreative.utils.ErrorUtils.parseException;
import static ua.mcchickenstudio.opencreative.utils.ErrorUtils.sendCriticalErrorMessage;
import static ua.mcchickenstudio.opencreative.utils.PlayerUtils.isEntityInLobby;
import static ua.mcchickenstudio.opencreative.utils.PlayerUtils.teleportToLobby;

public final class OpenCreative extends JavaPlugin {
    private static final int planetConfigVersion = 1;
    private static OpenCreative plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<UUID, Wander> wanders = new HashMap<>();
    private final Managers managers = new Managers();
    private Settings settings;
    private DevPlatformer devPlatformer;

    /**
     * Get a plugin instance for operations with it.
     * <p>
     * Useful for accessing planets manager, or settings.
     *
     * @return plugin instance.
     **/
    public static @NotNull OpenCreative getPlugin() {
        return plugin;
    }

    /**
     * Returns OpenCreative+ settings.
     *
     * @return settings of plugin.
     */
    public static @NotNull Settings getSettings() {
        return getPlugin().settings;
    }

    /**
     * Returns managers registry.
     *
     * @return registry of managers.
     */
    public static @NotNull Managers getManagers() {
        return getPlugin().managers;
    }

    /**
     * Gets economy manager, that has money operations for players.
     *
     * @return economy manager.
     */
    public static Economy getEconomy() {
        return getPlugin().managers.get(Economy.class);
    }

    /**
     * Gets packet manager, that has packets modifiers methods.
     *
     * @return packet manager.
     */
    public static PacketManager getPacketManager() {
        return getPlugin().managers.get(PacketManager.class);
    }

    /**
     * Gets hint manager, that sends suggestions to players in action bar.
     *
     * @return hint manager.
     */
    @SuppressWarnings("unused")
    public static HintManager getHintManager() {
        return getPlugin().managers.get(HintManager.class);
    }

    /**
     * Gets physics manager, that handles physical objects..
     *
     * @return physics manager.
     */
    @SuppressWarnings("unused")
    public static PhysicsManager getPhysicsManager() {
        return getPlugin().managers.get(PhysicsManager.class);
    }

    /**
     * Gets blocks manager, that changes a lot
     * of blocks in world.
     *
     * @return blocks manager.
     */
    @SuppressWarnings("unused")
    public static BlocksManager getBlocksManager() {
        return getPlugin().managers.get(BlocksManager.class);
    }

    /**
     * Gets disguise manager, that disguises
     * entities as players, other entities, blocks.
     *
     * @return disguise manager.
     */
    @SuppressWarnings("unused")
    public static DisguiseManager getDisguiseManager() {
        return getPlugin().managers.get(DisguiseManager.class);
    }

    /**
     * Gets module manager, that creates
     * or deletes modules.
     *
     * @return modules manager.
     */
    public static ModuleManager getModuleManager() {
        return getPlugin().managers.get(ModuleManager.class);
    }

    /**
     * Gets coding platforms manager, that
     * creates and manipulates with dev platforms
     * in developer worlds.
     *
     * @return coding platforms manager.
     */
    @SuppressWarnings("unused")
    public static DevPlatformer getDevPlatformer() {
        return getPlugin().devPlatformer;
    }

    /**
     * Sets custom coding platforms manager.
     *
     * @param platformsManager developer platforms manager.
     */
    @SuppressWarnings("unused")
    public static void setDevPlatformer(@NotNull DevPlatformer platformsManager) {
        if (!(platformsManager instanceof VerticalPlatformer || platformsManager instanceof HorizontalPlatformer)) {
            getPlugin().getLogger().info("Now using dev platforms manager: " + platformsManager.getName());
        }
        getPlugin().devPlatformer = platformsManager;
    }

    /**
     * Gets world manager, that loads
     * and unloads worlds.
     *
     * @return world manager.
     */
    @SuppressWarnings("unused")
    public static WorldManager getWorldManager() {
        return getPlugin().managers.get(WorldManager.class);
    }

    /**
     * Gets download manager, that
     * uploads world acrhive and allows
     * players to download it.
     *
     * @return download manager.
     */
    @SuppressWarnings("unused")
    public static DownloadManager getDownloadManager() {
        return getPlugin().managers.get(DownloadManager.class);
    }

    /**
     * Gets coding prompt manager, that
     * generates code by players prompts.
     *
     * @return coding prompter.
     */
    @SuppressWarnings("unused")
    public static CodingPrompter getCodingPrompter() {
        return getPlugin().managers.get(CodingPrompter.class);
    }

    /**
     * Gets planets manager, that stores planets in base
     * and has methods to create, find and delete them.
     *
     * @return planets manager.
     */
    public static PlanetsManager getPlanetsManager() {
        return getPlugin().managers.get(PlanetsManager.class);
    }

    /**
     * Gets stability manager, that checks server's
     * performance and makes sure everything is fine.
     *
     * @return stability manager.
     */
    public static StabilityManager getStability() {
        return getPlugin().managers.get(StabilityManager.class);
    }

    /**
     * Gets update manager, that has methods to
     * check available updates for plugin.
     */
    public static Updater getUpdater() {
        return getPlugin().managers.get(Updater.class);
    }

    /**
     * Returns version of the current planet's config format.
     *
     * @return planet's config version number.
     */
    public static int getPlanetConfigVersion() {
        return planetConfigVersion;
    }

    /**
     * Returns online wander, that plays on server.
     *
     * @return wander - if online, otherwise - null
     */
    public static @Nullable Wander getWander(@NotNull UUID uuid) {
        return getPlugin().wanders.get(uuid);
    }

    /**
     * Returns online wander casted by player.
     *
     * @return wander of player.
     */
    public static @NotNull Wander getWander(@NotNull Player player) {
        Wander wander = getWander(player.getUniqueId());
        if (wander == null) {
            wander = getPlugin().registerWander(player);
        }
        return wander;
    }

    /**
     * Returns offline wander, that can be online or offline.
     *
     * @return offline wander.
     */
    public static @NotNull OfflineWander getOfflineWander(@NotNull UUID uuid) {
        return new OfflineWander(uuid);
    }

    private boolean isChristmas() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            return calendar.get(Calendar.MONTH) == Calendar.DECEMBER && calendar.get(Calendar.DAY_OF_MONTH) == 25;
        } catch (Exception error) {
            return false;
        }
    }

    private boolean isHalloween() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            return calendar.get(Calendar.MONTH) == Calendar.OCTOBER && calendar.get(Calendar.DAY_OF_MONTH) == 31;
        } catch (Exception error) {
            return false;
        }
    }

    @Override
    public void onLoad() {
        getLogger().info("""
                This software was made by Ukrainians, suffering from never-ending air alerts, explosions, and deaths.
                We're AGAINST THE WAR. This software IS NOT DESIGNED for those who support killing and robbing another country.
                Let us have fun, like players who create their worlds...
                
                McChicken Studio 2017–2026
                """);
    }

    @Override
    public void onEnable() {
        plugin = this;
        long startTime = System.currentTimeMillis();
        getLogger().info("Starting OpenCreative+ " + getPluginMeta().getVersion() + ", please wait...");

        loadCore();
        loadManagers();

        finalizeStartup(startTime);
        new Metrics(this, 22001);
    }

    @Override
    public void onDisable() {
        getLogger().info("Shutting down OpenCreative+, please wait...");
        try {
            Component message = this.miniMessage.deserialize(
                    "Shutting down Open<gradient:#dbdbdb:#A3E2FF>Creative</gradient><color:#74D3FF>+ <gray>" + getPluginMeta().getVersion() + "<white>, please wait..."
            );
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(message);
                if (WorldUtils.isPlanet(player.getWorld())) {
                    teleportToLobby(player);
                }
            }
            unloadManagers();
        } catch (Exception error) {
            OpenCreative.getPlugin().getLogger().severe("Failed to unload OpenCreative+ :(" + parseException(error, false));
        }
        getLogger().info("""
                Goodbye from OpenCreative+
                
                Made by McChicken Studio 2017–2026""");
    }

    /**
     * Loads OpenCreative+ settings, localization files,
     * registers commands and events.
     */
    private void loadCore() {
        settings = new Settings();
        HookUtils.loadHooks();
        settings.load(false);
        registerCommands();
        registerEvents();
        //Ticker.runTicker();
        FileUtils.loadLocales();
        PlayerUtils.loadPermissions();
    }

    /**
     * Loads and assigns OpenCreative+ managers.
     */
    private void loadManagers() {
        managers.register(WorldManager.class, new VanillaWorldManager());
        managers.register(PlanetsManager.class, new Space());
        managers.register(ModuleManager.class, new Moduler());
        managers.start(PlanetsManager.class, ModuleManager.class);
        if (devPlatformer == null) devPlatformer = new HorizontalPlatformer();
        managers.registerIfAbsent(CodingPrompter.class, new DisabledCodingPrompter());
        managers.registerIfAbsent(StabilityManager.class, new DisabledWatchdog());
        managers.registerIfAbsent(DownloadManager.class, new DisabledDownloader());
        managers.registerIfAbsent(Economy.class, new DisabledEconomy());
        managers.register(PhysicsManager.class, new PhysicsManager());
        managers.register(Updater.class, new HangarUpdater());
        managers.register(HintManager.class, new Hints());
        managers.register(PacketManager.class, HookUtils.getPacketManager());
        managers.register(BlocksManager.class, HookUtils.getBlocks());
        managers.register(DisguiseManager.class, HookUtils.getDisguises());
        managers.start(CodingPrompter.class, StabilityManager.class, DownloadManager.class,
                Economy.class, Updater.class, BlocksManager.class, HintManager.class,
                DisguiseManager.class, PacketManager.class, PhysicsManager.class,
                WorldManager.class);
    }

    /**
     * Shutdowns all managers of OpenCreative+.
     */
    private void unloadManagers() {
        managers.shutdown(PlanetsManager.class, ModuleManager.class,
                DownloadManager.class, Economy.class, StabilityManager.class,
                BlocksManager.class, PacketManager.class, DisguiseManager.class,
                CodingPrompter.class, Updater.class, HintManager.class,
                WorldManager.class);
    }

    /**
     * Notifies console and players about successful
     * startup of OpenCreative+.
     *
     * @param startTime timestamp of the beginning of startup.
     */
    private void finalizeStartup(long startTime) {
        long loadedTime = System.currentTimeMillis() - startTime;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (WorldUtils.isPlanet(player.getWorld())) {
                teleportToLobby(player);
            } else if (isEntityInLobby(player)) {
                player.removePotionEffect(PotionEffectType.BLINDNESS);
                Sounds.LOBBY.play(player);
                player.clearTitle();
            }
        }
        getServer().sendActionBar(
                this.miniMessage.deserialize(
                        "<white>Open<gradient:#dbdbdb:#A3E2FF>Creative</gradient><color:#74D3FF>+ <gray>" + getPluginMeta().getVersion() + "<white> is loaded for " + loadedTime + " ms."
                )
        );
        getLogger().info(String.join("\n",
                "OpenCreative+ " + getPluginMeta().getVersion() + " is loaded for " + loadedTime + " ms.",
                "",
                " Welcome to OpenCreative+ " + getPluginMeta().getVersion() + "!",
                "",
                "  Running on " + Bukkit.getMinecraftVersion() + " server",
                "  Current time " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()),
                isChristmas() ? "  Ho-ho-ho! Merry Christmas, server owners! :-) ❆" :
                        isHalloween() ? "  Spo-o-o-oky Halloween, server owners! O_o 🎃" : "",
                "  ",
                "  Made by McChicken Studio 2017–2026",
                ""
        ));
        if (settings.isFirstLaunch()) {
            Bukkit.getScheduler().runTaskLater(this, () -> getLogger().info("""
                    Thank you for installing OpenCreative+!
                    
                    You can always get help on our Discord server:
                    https://discord.gg/sSFCXUeq63
                    
                    Check wiki pages about setting up:
                    https://gitlab.com/eagles-creative/opencreative/-/wikis/home
                    """), 30L); //FIXME The intention is probably to have the message after all the other log spam in the startup. Should be moved to ServerPostLoad event or whatever that was called without the arbitary delay
        }
    }

    /**
     * Registers plugin's commands and their tab completer. If some
     * commands failed to register, it notifies, but doesn't disable
     * entire plugin.
     */
    private void registerCommands() {
        this.getLogger().info("Registering OpenCreative+ commands...");
        int registeredCommands = 0;
        Map<String, Class<? extends CommandExecutor>> commands = new HashMap<>();
        commands.put("creative", CreativeCommand.class);
        commands.put("spawn", SpawnCommand.class);
        commands.put("menu", MenuCommand.class);
        commands.put("world", WorldCommand.class);
        commands.put("chat", ChatCommand.class);
        commands.put("join", JoinCommand.class);
        commands.put("ad", AdvertisementCommand.class);
        commands.put("play", PlayCommand.class);
        commands.put("build", BuildCommand.class);
        commands.put("dev", DevCommand.class);
        commands.put("environment", EnvironmentCommand.class);
        commands.put("like", LikeCommand.class);
        commands.put("dislike", DislikeCommand.class);
        commands.put("locate", LocateCommand.class);
        commands.put("gamemode", GamemodeCommand.class);
        commands.put("give", GiveCommand.class);
        commands.put("teleport", TeleportCommand.class);
        commands.put("edit", EditCommand.class);
        commands.put("playsound", PlaySoundCommand.class);
        commands.put("stopsound", StopSoundCommand.class);
        commands.put("time", TimeCommand.class);
        commands.put("weather", WeatherCommand.class);
        commands.put("value", ValueCommand.class);
        commands.put("module", ModuleCommand.class);
        commands.put("jointo", JoinToCommand.class);
        commands.put("ownworlds", OwnMenuCommand.class);
        for (String commandName : commands.keySet()) {
            PluginCommand command = getCommand(commandName);
            if (command != null) {
                try {
                    command.setExecutor(commands.get(commandName).getDeclaredConstructor().newInstance());
                    registeredCommands++;
                } catch (Exception error) {
                    sendCriticalErrorMessage("Couldn't register command " + commandName, error);
                }
            } else {
                sendCriticalErrorMessage("Couldn't get command with name " + commandName + ", it is null. Maybe it doesn't exist in plugins.yml?");
            }
        }
        getLogger().info("OpenCreative+ registered " + (registeredCommands == commands.size() ? "all" : registeredCommands + "/" + commands.size()) + " commands.");
    }

    /**
     * Registers plugin's event listeners. If some listener is failed
     * to register, then it notifies, but doesn't disable entire plugin.
     */
    private void registerEvents() {
        getLogger().info("Registering OpenCreative+ event listeners...");
        int registeredListeners = 0;
        Class<?>[] listeners = new Class[]{
                ChangedWorld.class, EntitySpawnListener.class, EntityDamageListener.class,
                JoinListener.class, QuitListener.class, RespawnListener.class,
                DeathListener.class, TeleportListener.class, MoveListener.class,
                ChatListener.class, InteractListener.class, DropItemListener.class,
                PlaceBlockListener.class, DestroyBlockListener.class, BucketListener.class,
                ClickListener.class, RedstoneListener.class, BlockChangeListener.class,
                Menus.class, GameModeListener.class, EntityStateListener.class,
                CreativeListener.class, PotionListener.class, PlanetListener.class,
                CraftListener.class, WorldListener.class, WitherCreationListener.class
        };
        for (Class<?> listenerClass : listeners) {
            try {
                getServer().getPluginManager().registerEvents(
                        (Listener) listenerClass.getDeclaredConstructor().newInstance(), this
                );
                registeredListeners++;
            } catch (Exception exception) {
                sendCriticalErrorMessage("Couldn't register event listener: " + listenerClass.getSimpleName(), exception);
            }
        }
        getLogger().info("OpenCreative+ registered " + (registeredListeners == listeners.length ? "all" : registeredListeners + "/" + listeners.length) + " event listeners.");
    }

    public @NotNull Wander registerWander(@NotNull Player player) {
        Wander wander = new Wander(player);
        wanders.put(player.getUniqueId(), wander);
        return wander;
    }

    public void unregisterWander(@NotNull Player player) {
        wanders.remove(player.getUniqueId());
    }

}
