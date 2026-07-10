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
import ua.mcchickenstudio.opencreative.OpenCreative;
import ua.mcchickenstudio.opencreative.coding.arguments.Arguments;
import ua.mcchickenstudio.opencreative.coding.blocks.actions.ActionType;
import ua.mcchickenstudio.opencreative.coding.blocks.actions.Target;
import ua.mcchickenstudio.opencreative.coding.blocks.actions.playeractions.PlayerAction;
import ua.mcchickenstudio.opencreative.coding.blocks.executors.Executor;
import ua.mcchickenstudio.opencreative.wanders.Wander;

import java.util.concurrent.CompletableFuture;

public final class SetSkinAction extends PlayerAction {
    public SetSkinAction(Executor executor, Target target, int x, Arguments args) {
        super(executor, target, x, args);
    }

    @Override
    public void executePlayer(@NotNull Player player) {
        if (getPlanet().getLimits().cantOpenMenu(player)) {
            return;
        }
        Wander wander = OpenCreative.getWander(player);
        ItemStack item = getArguments().getItem("skin", new ItemStack(Material.AIR), this);
        if (item.isEmpty()) {
            // Reset textures
            PlayerProfile profile = player.getPlayerProfile();
            profile.setTextures(wander.getJoinTextures());
            CompletableFuture<PlayerProfile> updatedProfile = profile.update();
            try {
                player.setPlayerProfile(updatedProfile.get());
                wander.setTexturesWereChanged(false);
            } catch (Exception error) {
                throw new RuntimeException("Failed to reset the skin", error);
            }
            return;
        }
        if (item.getItemMeta() instanceof SkullMeta head) {
            PlayerProfile headProfile = head.getPlayerProfile();
            if (headProfile == null) {
                return;
            }
            if (headProfile.hasTextures()) {
                PlayerProfile profile = player.getPlayerProfile();
                profile.setTextures(headProfile.getTextures());
                CompletableFuture<PlayerProfile> updatedProfile = profile.update();
                try {
                    wander.setTexturesWereChanged(true);
                    player.setPlayerProfile(updatedProfile.get());
                } catch (Exception error) {
                    throw new RuntimeException("Failed to change the skin", error);
                }
            }
        }
    }

    @Override
    public @NotNull ActionType getActionType() {
        return ActionType.PLAYER_SET_SKIN;
    }
}
