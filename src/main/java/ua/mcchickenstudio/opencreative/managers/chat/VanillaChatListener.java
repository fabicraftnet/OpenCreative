package ua.mcchickenstudio.opencreative.managers.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ua.mcchickenstudio.opencreative.listeners.player.ChatListener;

public class VanillaChatListener implements Listener {
    @EventHandler
    public void onChat(AsyncChatEvent event)
    {
        ChatListener.onChat(event);
    }
}
