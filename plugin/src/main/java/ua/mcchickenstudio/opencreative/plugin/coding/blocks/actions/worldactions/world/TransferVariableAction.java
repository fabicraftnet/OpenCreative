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

package ua.mcchickenstudio.opencreative.plugin.coding.blocks.actions.worldactions.world;

import org.jetbrains.annotations.NotNull;
import ua.mcchickenstudio.opencreative.plugin.OpenCreative;
import ua.mcchickenstudio.opencreative.plugin.coding.arguments.Arguments;
import ua.mcchickenstudio.opencreative.plugin.coding.blocks.actions.ActionType;
import ua.mcchickenstudio.opencreative.plugin.coding.blocks.actions.Target;
import ua.mcchickenstudio.opencreative.plugin.coding.blocks.actions.worldactions.WorldAction;
import ua.mcchickenstudio.opencreative.plugin.coding.blocks.events.world.other.VariableTransferEvent;
import ua.mcchickenstudio.opencreative.plugin.coding.blocks.executors.Executor;
import ua.mcchickenstudio.opencreative.plugin.planets.Planet;

public final class TransferVariableAction extends WorldAction {
    public TransferVariableAction(Executor executor, Target target, int x, Arguments args) {
        super(executor, target, x, args);
    }

    @Override
    protected void execute() {
        arguments.requireArguments(this, "world", "key", "value");
        String worldId = getArguments().getText("world", "0", this);
        Planet planet = OpenCreative.getPlanetsManager().getPlanetById(worldId);
        if (planet == null) return;
        if (!planet.isLoaded()) return;
        if (!planet.isOwner(getPlanet().getOwner())) return;
        String key = getArguments().getText("key", "key", this);
        String value = getArguments().getText("value", "value", this);
        new VariableTransferEvent(planet, key, value).callEvent();
    }

    @Override
    public @NotNull ActionType getActionType() {
        return ActionType.WORLD_TRANSFER_VARIABLE;
    }
}
