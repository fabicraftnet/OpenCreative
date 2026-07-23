package ua.mcchickenstudio.opencreative.managers.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import ua.mcchickenstudio.opencreative.OpenCreative;
import ua.mcchickenstudio.opencreative.listeners.player.ChatListener;
import ua.mcchickenstudio.opencreative.utils.hooks.HookUtils;

public final class VanillaChat implements ChatManager<AsyncChatEvent>, Listener {

    @Override
    public void setCancelled(AsyncChatEvent event, boolean bool) {event.setCancelled(bool);
        Bukkit.getLogger().info(bool+" "+event.isCancelled());}

    @Override
    public boolean isCancelled(AsyncChatEvent event){return event.isCancelled();}
    @Override
    public Player getPlayer(AsyncChatEvent event){return event.getPlayer();}
    @Override
    public Component getMessage(AsyncChatEvent event){return event.message();}


    @Override
    public void start() {
        OpenCreative.getPlugin().getServer().getPluginManager().registerEvents(new VanillaChatListener(), OpenCreative.getPlugin());
    }

    @Override
    public Component render(AsyncChatEvent event)
    {
        return Component.text("CarbonChat is missing!");
    }
    @Override
    public void shutdown() {}
    @Override
    public boolean isWorking() {
        return !HookUtils.isCarbonChatEnabled;
    }
    @Override
    public @NotNull String getName() { return "VanillaChat";}
}
