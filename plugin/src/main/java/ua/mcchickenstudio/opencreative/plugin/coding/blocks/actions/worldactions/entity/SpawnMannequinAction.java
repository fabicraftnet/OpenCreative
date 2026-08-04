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

package ua.mcchickenstudio.opencreative.plugin.coding.blocks.actions.worldactions.entity;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mannequin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import ua.mcchickenstudio.opencreative.plugin.coding.arguments.Arguments;
import ua.mcchickenstudio.opencreative.plugin.coding.blocks.actions.ActionType;
import ua.mcchickenstudio.opencreative.plugin.coding.blocks.actions.Target;
import ua.mcchickenstudio.opencreative.plugin.coding.blocks.actions.worldactions.WorldAction;
import ua.mcchickenstudio.opencreative.plugin.coding.blocks.executors.Executor;

import static ua.mcchickenstudio.opencreative.plugin.utils.ErrorUtils.sendCodingDebugLog;

public final class SpawnMannequinAction extends WorldAction {

    public SpawnMannequinAction(Executor executor, Target target, int x, Arguments args) {
        super(executor, target, x, args);
    }

    @Override
    protected void execute() {
        if (getWorld().getEntities().size() >= getPlanet().getLimits().getEntitiesLimit()) {
            sendCodingDebugLog(getPlanet(), "Too many entities: spawn entity action is cancelled.");
            return;
        }

        Component customName = getArguments().getComponent("name", Component.text(""), this);
        Component description = getArguments().getComponent("description", Component.text(""), this);
        ItemStack item = getArguments().getItem("head", new ItemStack(Material.AIR), this);
        boolean gravity = getArguments().getBoolean("gravity", true, this);
        boolean glowing = getArguments().getBoolean("glowing", false, this);
        boolean invulnerable = getArguments().getBoolean("invulnerable", false, this);
        boolean visibleByDefault = getArguments().getBoolean("visible-for-all", true, this);
        boolean invisible = getArguments().getBoolean("invisible", false, this);
        boolean customNameVisible = getArguments().getBoolean("show-name", true, this);
        PlayerProfile profile = null;

        if (item.getItemMeta() instanceof SkullMeta skullMeta)
        {
            profile = skullMeta.getPlayerProfile();
        }


        for (Location location : getArguments().getLocationList("locations", this)) {
            Entity spawnedEntity = getWorld().spawnEntity(location, EntityType.MANNEQUIN);

            if (spawnedEntity instanceof Mannequin man) {
                if (getArguments().pathExists("name")) {
                    man.customName(customName);
                }
                if (profile.hasTextures())
                {
                    man.setProfile(ResolvableProfile.resolvableProfile(profile));
                }

                if (getArguments().pathExists("description")) {
                    man.setDescription(description);
                }
                spawnedEntity.setGravity(gravity);
                spawnedEntity.setGlowing(glowing);
                spawnedEntity.setInvisible(invisible);
                spawnedEntity.setInvulnerable(invulnerable);
                spawnedEntity.setCustomNameVisible(customNameVisible);
                spawnedEntity.setVisibleByDefault(visibleByDefault);

            }
        }
    }

    @Override
    public @NotNull ActionType getActionType() {
        return ActionType.WORLD_SPAWN_MANNEQUIN;
    }
}
