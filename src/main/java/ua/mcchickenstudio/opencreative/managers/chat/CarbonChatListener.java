package ua.mcchickenstudio.opencreative.managers.chat;

import net.draycia.carbon.api.event.CarbonEventSubscriber;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import ua.mcchickenstudio.opencreative.listeners.player.ChatListener;

public class CarbonChatListener implements CarbonEventSubscriber<CarbonChatEvent> {

    @Override
    public void on(CarbonChatEvent event) {
        ChatListener.onChat(event);
    }
}
