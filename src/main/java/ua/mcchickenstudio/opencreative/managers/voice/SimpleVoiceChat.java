package ua.mcchickenstudio.opencreative.managers.voice;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.audiosender.AudioSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ua.mcchickenstudio.opencreative.OpenCreative;
import ua.mcchickenstudio.opencreative.planets.Planet;
import ua.mcchickenstudio.opencreative.utils.hooks.HookUtils;

public final class SimpleVoiceChat implements VoiceManager{

    private VoiceChatPlugin voiceChatPlugin;

    public void mute(Player player) {
        voiceChatPlugin.mute(player.getUniqueId());
    }
    public void mute(Planet planet) {
        for (Player player : planet.getPlayers()) {
            voiceChatPlugin.mute(player.getUniqueId());
        }
    }

    public void unmute(Player player) {
        voiceChatPlugin.unmute(player.getUniqueId());
    }
    public void unmute(Planet planet) {
        for (Player player : planet.getPlayers()) {
            voiceChatPlugin.unmute(player.getUniqueId());
        }
    }

    @Override
    public void start() {
        BukkitVoicechatService service = OpenCreative.getPlugin().getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service != null) {
            voiceChatPlugin = new VoiceChatPlugin();
            service.registerPlugin(voiceChatPlugin);
        }
    }

    @Override
    public void shutdown() {}

    @Override
    public @NotNull String getName() {
        return "SimpleVoiceChat";
    }

    @Override
    public boolean isWorking() {
        return HookUtils.isPluginEnabled("dont do it this way");
    }
}
