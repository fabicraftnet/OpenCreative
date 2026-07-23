package ua.mcchickenstudio.opencreative.managers.chat;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ua.mcchickenstudio.opencreative.managers.Manager;
import ua.mcchickenstudio.opencreative.managers.Toggleable;

public interface ChatManager<T> extends Manager, Toggleable {



    void setCancelled(T event, boolean bool);

    abstract boolean isCancelled(T event);

    abstract Player getPlayer(T event);

    abstract Component getMessage(T event);

    abstract Component render(T event);
}
