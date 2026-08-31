package ua.mcchickenstudio.opencreative.managers.voice;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;

import java.util.HashSet;
import java.util.UUID;

public class VoiceChatPlugin implements VoicechatPlugin {
    public VoicechatServerApi api;
    public HashSet<UUID> muted;
    @Override
    public String getPluginId() {
        return "Opencreative+";
    }

    @Override
    public void initialize(VoicechatApi api) {
        muted  = new HashSet<>();
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophone);
    }
    public void mute(UUID uuid) {
        muted.add(uuid);
    }
    public void unmute(UUID uuid) {
        muted.remove(uuid);
    }
    public void onMicrophone(MicrophonePacketEvent event) {
        if (muted.contains(event.getSenderConnection().getPlayer().getUuid()))
        {
            event.cancel();
        }
    }


    public void onServerStarted(VoicechatServerStartedEvent event) {
        api = event.getVoicechat();
    }
}
