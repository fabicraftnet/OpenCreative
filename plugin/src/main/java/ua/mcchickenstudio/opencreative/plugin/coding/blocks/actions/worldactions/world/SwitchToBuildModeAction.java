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
import ua.mcchickenstudio.opencreative.plugin.coding.arguments.Arguments;
import ua.mcchickenstudio.opencreative.plugin.coding.blocks.actions.ActionType;
import ua.mcchickenstudio.opencreative.plugin.coding.blocks.actions.Target;
import ua.mcchickenstudio.opencreative.plugin.coding.blocks.actions.worldactions.WorldAction;
import ua.mcchickenstudio.opencreative.plugin.coding.blocks.executors.Executor;
import ua.mcchickenstudio.opencreative.plugin.events.planet.PlanetModeChangeEvent;
import ua.mcchickenstudio.opencreative.plugin.planets.Planet;

import static ua.mcchickenstudio.opencreative.plugin.utils.ErrorUtils.notifyBuildModeByCode;

public final class SwitchToBuildModeAction extends WorldAction {
    public SwitchToBuildModeAction(Executor executor, Target target, int x, Arguments args) {
        super(executor, target, x, args);
    }

    @Override
    protected void execute() {
        if (getPlanet().getMode() != Planet.Mode.BUILD) {
            PlanetModeChangeEvent event = new PlanetModeChangeEvent(getPlanet(), getPlanet().getMode(), Planet.Mode.BUILD);
            event.callEvent();
            if (!event.isCancelled()) {
                getPlanet().setMode(Planet.Mode.BUILD);
                notifyBuildModeByCode(getExecutor(), this);
            }
        }
    }

    @Override
    public @NotNull ActionType getActionType() {
        return ActionType.WORLD_SWITCH_TO_BUILD_MODE;
    }
}
