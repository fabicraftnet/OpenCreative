/*
 * OpenCreative+, Minecraft plugin.
 * (C) 2022-2026, McChicken Studio, mcchickenstudio@gmail.com
 *
 * OpenCreative+ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenCreative+ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ua.mcchickenstudio.opencreative.coding.blocks.actions.playeractions.appearance;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import ua.mcchickenstudio.opencreative.coding.arguments.Arguments;
import ua.mcchickenstudio.opencreative.coding.blocks.actions.ActionType;
import ua.mcchickenstudio.opencreative.coding.blocks.actions.Target;
import ua.mcchickenstudio.opencreative.coding.blocks.actions.playeractions.PlayerAction;
import ua.mcchickenstudio.opencreative.coding.blocks.executors.Executor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class SetSkinAction extends PlayerAction {
    public SetSkinAction(Executor executor, Target target, int x, Arguments args) {
        super(executor, target, x, args);
    }

    @Override
    public void executePlayer(@NotNull Player player) {
        ItemStack item = getArguments().getItem("head", new ItemStack(Material.AIR), this);


        if ((item.getItemMeta() instanceof SkullMeta head))
        {
            if (head.getPlayerProfile().hasTextures()) {
                PlayerProfile profile = player.getPlayerProfile();
                profile.setTextures(head.getPlayerProfile().getTextures());
                player.setPlayerProfile(profile);

            }
        }
        else if (item.getType() == Material.AIR)
        {
            PlayerProfile profile = player.getPlayerProfile();
            profile.setTextures(null);
            CompletableFuture<PlayerProfile> updatedProfile = profile.update();
            try {
                player.setPlayerProfile(updatedProfile.get());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }


    }

    @Override
    public @NotNull ActionType getActionType() {
        return ActionType.PLAYER_SET_SKIN;
    }
}
