package ua.mcchickenstudio.opencreative.coding.blocks.actions.variableactions.location;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import ua.mcchickenstudio.opencreative.coding.arguments.Arguments;
import ua.mcchickenstudio.opencreative.coding.blocks.actions.ActionType;
import ua.mcchickenstudio.opencreative.coding.blocks.actions.Target;
import ua.mcchickenstudio.opencreative.coding.blocks.actions.variableactions.VariableAction;
import ua.mcchickenstudio.opencreative.coding.blocks.executors.Executor;
import ua.mcchickenstudio.opencreative.coding.variables.VariableLink;

public final class MultiplyLocation extends VariableAction {

    public MultiplyLocation(Executor executor, Target target, int x, Arguments args) {
        super(executor, target, x, args);
    }

    @Override
    protected void execute() {
        if (!arguments.pathExists("result") || !arguments.pathExists("location") || !arguments.pathExists("factor")) return;
        final VariableLink result = arguments.getVariableLink("result", this);
        final Location location = arguments.getLocation("location", new Location(getWorld(), 0d, 0d, 0d), this);
        final double factor = arguments.getDouble("factor", 0d, this);
        setVarValue(result, location.clone().multiply(factor));
    }

    @Override
    public @NotNull ActionType getActionType() { return ActionType.VAR_MULTIPLY_LOCATION; }
}
