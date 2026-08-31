package ua.mcchickenstudio.opencreative.managers.voice;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ua.mcchickenstudio.opencreative.planets.Planet;
import ua.mcchickenstudio.opencreative.utils.hooks.HookUtils;

public final class DisabledVoice implements VoiceManager{

    @Override
    public void mute(Player player) {}
    @Override
    public void unmute(Player player){}
    @Override
    public void mute(Planet planet) {}
    @Override
    public void unmute(Planet planet){}
    @Override
    public void start() {}

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
