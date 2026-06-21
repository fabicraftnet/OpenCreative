package ua.mcchickenstudio.opencreative.coding.blocks.actions.variableactions.location;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import ua.mcchickenstudio.opencreative.coding.arguments.Arguments;
import ua.mcchickenstudio.opencreative.coding.blocks.actions.ActionType;
import ua.mcchickenstudio.opencreative.coding.blocks.actions.Target;
import ua.mcchickenstudio.opencreative.coding.blocks.actions.variableactions.VariableAction;
import ua.mcchickenstudio.opencreative.coding.blocks.executors.Executor;
import ua.mcchickenstudio.opencreative.coding.variables.VariableLink;

import static java.lang.Math.abs;

public final class CountBlocksInRegion extends VariableAction {

    public CountBlocksInRegion(Executor executor, Target target, int x, Arguments args) {
        super(executor, target, x, args);
    }

    @Override
    protected void execute() {
        if (!arguments.pathExists("result") || !arguments.pathExists("corner1") || !arguments.pathExists("corner2"))
            return;
        final VariableLink result = arguments.getVariableLink("result", this);
        final Location corner1 = arguments.getLocation("corner1", new Location(getWorld(), 0d, 0d, 0d), this);
        final Location corner2 = arguments.getLocation("corner2", new Location(getWorld(), 0d, 0d, 0d), this);

        int xVol = abs(corner2.getBlockX() - corner1.getBlockX()) + 1;
        int yVol = abs(corner2.getBlockY() - corner1.getBlockY()) + 1;
        int zVol = abs(corner2.getBlockZ() - corner1.getBlockZ()) + 1;

        setVarValue(result, (xVol * yVol * zVol));
    }

    @Override
    public @NotNull ActionType getActionType() { return ActionType.VAR_COUNT_BLOCKS_IN_REGION; }
}
