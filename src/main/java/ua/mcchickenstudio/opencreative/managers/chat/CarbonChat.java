package ua.mcchickenstudio.opencreative.managers.chat;

import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.event.CarbonEvent;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.api.event.CarbonEventSubscription;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import net.draycia.carbon.api.util.ChatComponentRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import ua.mcchickenstudio.opencreative.OpenCreative;
import ua.mcchickenstudio.opencreative.listeners.player.ChatListener;
import ua.mcchickenstudio.opencreative.utils.hooks.HookUtils;

public final class CarbonChat implements ChatManager<CarbonChatEvent> {

    private static CarbonEventSubscription<CarbonChatEvent> listener;


    @Override
    public void setCancelled(CarbonChatEvent event, boolean bool) {event.cancelled(bool);
    }

    @Override
    public boolean isCancelled(CarbonChatEvent event){return event.cancelled();}
    @Override
    public Player getPlayer(CarbonChatEvent event){return Bukkit.getPlayer(event.sender().uuid());}
    @Override
    public Component getMessage(CarbonChatEvent event){return event.message();}

    @Override
    public void start() {
        listener = CarbonChatProvider.carbonChat().eventHandler().subscribe(CarbonChatEvent.class, 0, true, new CarbonChatListener());
    }

    @Override
    public Component render(CarbonChatEvent event)
    {
        return CarbonChatProvider.carbonChat().channelRegistry().defaultChannel().render(event.sender(), event.recipients().get(0), event.message(), event.originalMessage());
    }

    @Override
    public void shutdown() {
        listener.dispose();
    }
    @Override
    public boolean isWorking() {
        return HookUtils.isCarbonChatEnabled;
    }
    @Override
    public @NotNull String getName() { return "CarbonChat";}
}
