package ua.mcchickenstudio.opencreative.coding.blocks.actions.playeractions.communication;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ua.mcchickenstudio.opencreative.coding.arguments.Arguments;
import ua.mcchickenstudio.opencreative.coding.blocks.actions.ActionType;
import ua.mcchickenstudio.opencreative.coding.blocks.actions.Target;
import ua.mcchickenstudio.opencreative.coding.blocks.actions.playeractions.PlayerAction;
import ua.mcchickenstudio.opencreative.coding.blocks.executors.Executor;

public final class PlaySoundFromEntityAction extends PlayerAction {

    public PlaySoundFromEntityAction(Executor executor, Target target, int x, Arguments args) {
        super(executor, target, x, args);
    }

    @Override
    public void executePlayer(@NotNull Player player) {
        if (!arguments.pathExists("sound")) return;
        final String sndID = arguments.getText("sound", "", this);
        final float volume = arguments.getFloat("volume", 1f, this);
        final float pitch = arguments.getFloat("pitch", 1f, this);
        final String sourceName = arguments.getText("source", player.getName(), this);
        final String categoryStr = arguments.getText("category", "master", this);

        if (volume <= 0 || pitch > 2 || pitch < 0.1) return;

        final SoundCategory category = SoundCategory.valueOf(categoryStr.toUpperCase());
        final Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(sndID));
        if (sound == null) return;

        Entity source = null;
        for (Entity e : getWorld().getEntities()) {
            if (e.getName().equalsIgnoreCase(sourceName) || e.getUniqueId().toString().equals(sourceName)) {
                source = e;
                break;
            }
        }
        if (source == null) return;

        player.playSound(source, sound, category, volume, pitch);
    }

    @Override
    public @NotNull ActionType getActionType() { return ActionType.PLAYER_PLAY_SOUND_FROM_ENTITY; }
}
