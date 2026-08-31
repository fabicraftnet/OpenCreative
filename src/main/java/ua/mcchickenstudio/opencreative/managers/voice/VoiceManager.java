package ua.mcchickenstudio.opencreative.managers.voice;

import org.bukkit.entity.Player;
import ua.mcchickenstudio.opencreative.managers.Manager;
import ua.mcchickenstudio.opencreative.managers.Toggleable;
import ua.mcchickenstudio.opencreative.planets.Planet;

public interface VoiceManager extends Manager, Toggleable {


    // disable voice per player?
    // disable per world?

    void mute(Player player);

    void unmute(Player player);
    void mute(Planet planet);

    void unmute(Planet planet);

}
