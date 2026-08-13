package ua.mcchickenstudio.opencreative.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ua.mcchickenstudio.opencreative.OpenCreative;
import ua.mcchickenstudio.opencreative.coding.modules.Module;
import ua.mcchickenstudio.opencreative.planets.Planet;
import ua.mcchickenstudio.opencreative.settings.Sounds;

import java.util.Arrays;
import java.util.List;

import static ua.mcchickenstudio.opencreative.utils.MessageUtils.*;
import static ua.mcchickenstudio.opencreative.utils.MessageUtils.getLocaleMessageString;

/**
 * Dialog handler for setting player customizable icons.
 */
@SuppressWarnings("UnstableApiUsage")
public final class DialogItemDescription {


    public Dialog planetDescription(Planet planet, Player player, ItemStack icon)
    {
        String name = (planet.getInformation().displayNameString());
        String serialized = (planet.getInformation().descriptionString());
        serialized = serialized.replace("\\\\n","\n");
        serialized = serialized.replace("\\n","\n");
        String description = serialized;

        return Dialog.create(buider -> buider.empty()

                .base(DialogBase.builder(getLocaleMessageComponent("dialog.planet.title"))
                        .inputs(List.of(
                                DialogInput.text("name", 240, getLocaleMessageComponent("dialog.planet.inputs.name"),true,name,64, TextDialogInput.MultilineOptions.create(1,null)),
                                DialogInput.text("desc",240, getLocaleMessageComponent("dialog.planet.inputs.description"),true, description,256, TextDialogInput.MultilineOptions.create(8,null))
                        ))
                        .body(List.of(
                                DialogBody.item(icon).build(),
                                DialogBody.plainMessage(toComponent("<sprite:gui:icon/info>").hoverEvent(miniMessage.deserialize(getLocaleMessageString("dialog.planet.hint").replace("%max%",Integer.toString(OpenCreative.getSettings().getRequirements().getWorldNameMaxLength())))))
                        ))


                        .build())
                .type(DialogType.notice(
                        ActionButton.create(getLocaleMessageComponent("dialog.planet.inputs.confirm"),null,100,
                                DialogAction.customClick(
                                        (view, audience) -> applyToWorld(view, player, planet)
                                        , ClickCallback.Options.builder().build()
                                )
                        ))
                ));
    }
    public Dialog planetID(Planet planet, Player player, ItemStack icon)
    {
        String ID = (planet.getInformation().getCustomID());

        return Dialog.create(buider -> buider.empty()

                .base(DialogBase.builder(getLocaleMessageComponent("dialog.world-id.title"))
                        .inputs(List.of(
                                DialogInput.text("id", 240, getLocaleMessageComponent("dialog.world-id.inputs.id"),true,ID,16, TextDialogInput.MultilineOptions.create(1,null))
                        ))
                        .body(List.of(
                                DialogBody.item(icon).build(),
                                DialogBody.plainMessage(toComponent("<sprite:gui:icon/info>").hoverEvent(toComponent(getLocaleMessageString("dialog.world-id.hint").replace("%max%",Integer.toString(OpenCreative.getSettings().getRequirements().getCustomIdMaxLength())))))
                        ))


                        .build())
                .type(DialogType.notice(
                        ActionButton.create(getLocaleMessageComponent("dialog.world-id.inputs.confirm"),null,100,
                                DialogAction.customClick(
                                        (view, audience) -> applyID(view, player, planet)
                                        , ClickCallback.Options.builder().build()
                                )
                        ))
                ));
    }
    private void applyID(DialogResponseView view, Player player, Planet planet)
    {
        String input = view.getText("id");
        if (planet == null || !planet.isOwner(player)) return;
        String pattern = OpenCreative.getSettings().getRequirements().getCustomIdPattern();
        if (input.length() > OpenCreative.getSettings().getRequirements().getCustomIdMaxLength()
                || input.length() < OpenCreative.getSettings().getRequirements().getCustomIdMinLength()
                || Character.isDigit(input.charAt(0)) || !input.matches(pattern)) {
            player.sendMessage(toComponent(getLocaleMessageString("settings.world-id.error")
                    .replace("%min%", String.valueOf(OpenCreative.getSettings().getRequirements().getCustomIdMinLength()))
                    .replace("%max%", String.valueOf(OpenCreative.getSettings().getRequirements().getCustomIdMaxLength()))));
            return;
        }
        for (Planet searchablePlanet : OpenCreative.getPlanetsManager().getPlanets()) {
            if (searchablePlanet.getInformation().getCustomID().equalsIgnoreCase(input)) {
                player.sendMessage(getLocaleMessageComponent("settings.world-id.taken"));
                return;
            }
        }
        planet.getInformation().setCustomID(input);
        player.sendMessage(toComponent(getLocaleMessageString("settings.world-id.changed").replace("%id%", input)));
        planet.getInformation().updateIconAsync();
        OpenCreative.getPlugin().getLogger().info("[WORLD-CHAT: " + planet.getId() + "] " + player.getName() + " changed world's ID to: " + input);
        Sounds.WORLD_SETTINGS_CUSTOM_ID_SET.play(player);

    }

    private void applyToWorld(DialogResponseView view, Player player, Planet planet)
    {
        String newName = view.getText("name");
        String uncoloredName = ((TextComponent) fromInputToComponent(newName)).content();
        if (uncoloredName.length() > OpenCreative.getSettings().getRequirements().getWorldNameMaxLength() || uncoloredName.length()
                < OpenCreative.getSettings().getRequirements().getWorldNameMinLength()) {
            player.sendMessage(toComponent(getLocaleMessageString("settings.world-name.error")
                    .replace("%min%", String.valueOf(OpenCreative.getSettings().getRequirements().getWorldNameMinLength()))
                    .replace("%max%", String.valueOf(OpenCreative.getSettings().getRequirements().getWorldNameMaxLength()))));
            return;
        }
        planet.getInformation().setDisplayName(newName);
        player.sendMessage(toComponent(getLocaleMessageString("settings.world-name.changed").replace("%name%", newName)));
        planet.getInformation().updateIconAsync();
        //OpenCreative.getPlugin().getLogger().info("[WORLD-CHAT: " + planet.getId() + "] " + player.getName() + " renamed world to: " + input);

        String input = view.getText("desc");
        if (planet == null || !planet.isOwner(player)) return;
        String[] lines = input.split("\n");
        int max = (Arrays.stream(lines).map(s -> ((TextComponent) fromInputToComponent(s)).content().length()).mapToInt(i->i).max().getAsInt());
        OpenCreative.getPlugin().getLogger().info("Yo the max is "+max);
        if (max > OpenCreative.getSettings().getRequirements().getWorldNameMaxLength() ||
                max < OpenCreative.getSettings().getRequirements().getWorldDescriptionMinLength()) {
            player.sendMessage(toComponent(getLocaleMessageString("settings.world-description.error")
                    .replace("%min%", String.valueOf(OpenCreative.getSettings().getRequirements().getWorldDescriptionMinLength()))
                    .replace("%max%", String.valueOf(OpenCreative.getSettings().getRequirements().getWorldDescriptionMaxLength()))));
            return;
        }
        planet.getInformation().setDescription(String.join("\\n", lines ));
        planet.getInformation().updateIconAsync();
        OpenCreative.getPlugin().getLogger().info("[WORLD-CHAT: " + planet.getId() + "] " + player.getName() + " changed world's description to: " + input);
        Sounds.WORLD_SETTINGS_NAME_CHANGE.play(player);

    }

    public Dialog moduleDescription(Module module, Player player, ItemStack icon)
    {
        String name = (module.getInformation().getDisplayName());
        String serialized = (module.getInformation().getDescription());
        serialized = serialized.replace("\\\\n","\n");
        serialized = serialized.replace("\\n","\n");
        String description = serialized;

        return Dialog.create(buider -> buider.empty()

                .base(DialogBase.builder(getLocaleMessageComponent("dialog.module.title"))
                        .inputs(List.of(
                                DialogInput.text("name", 240,Component.text("dialog.module.inputs.name"),true,name,64, TextDialogInput.MultilineOptions.create(1,null)),
                                DialogInput.text("desc",240,Component.text("dialog.module.inputs.description"),true, description,256, TextDialogInput.MultilineOptions.create(8,null))
                        ))
                        .body(List.of(
                                DialogBody.item(icon).build(),
                                DialogBody.plainMessage(toComponent("<sprite:gui:icon/info>").hoverEvent(miniMessage.deserialize(getLocaleMessageString("dialog.module.hint").replace("%max%",Integer.toString(OpenCreative.getSettings().getRequirements().getWorldNameMaxLength())))))
                        ))


                        .build())
                .type(DialogType.notice(
                        ActionButton.create(getLocaleMessageComponent("dialog.module.inputs.confirm"),null,100,
                                DialogAction.customClick(
                                        (view, audience) -> applyToModule(view, player, module)
                                        , ClickCallback.Options.builder().build()
                                )
                        ))
                ));
    }
    private void applyToModule(DialogResponseView view, Player player, Module module)
    {
        String newName = view.getText("name");
        String uncoloredName = ((TextComponent) fromInputToComponent(newName)).content();
        if (uncoloredName.length() > OpenCreative.getSettings().getRequirements().getModuleNameMaxLength() || uncoloredName.length()
                < OpenCreative.getSettings().getRequirements().getWorldNameMinLength()) {
            player.sendMessage(toComponent(getLocaleMessageString("settings.world-name.error")
                    .replace("%min%", String.valueOf(OpenCreative.getSettings().getRequirements().getWorldNameMinLength()))
                    .replace("%max%", String.valueOf(OpenCreative.getSettings().getRequirements().getWorldNameMaxLength()))));
            return;
        }
        module.getInformation().setDisplayName(newName);
        player.sendMessage(toComponent(getLocaleMessageString("settings.world-name.changed").replace("%name%", newName)));
        module.getInformation().updateIconAsync();
        //OpenCreative.getPlugin().getLogger().info("[WORLD-CHAT: " + planet.getId() + "] " + player.getName() + " renamed world to: " + input);

        String input = view.getText("desc");
        if (module == null || !module.isOwner(player)) return;
        String[] lines = input.split("\n");
        int max = (Arrays.stream(lines).map(s -> ((TextComponent) fromInputToComponent(s)).content().length()).mapToInt(i->i).max().getAsInt());
        if (max > OpenCreative.getSettings().getRequirements().getWorldNameMaxLength() ||
                max < OpenCreative.getSettings().getRequirements().getWorldDescriptionMinLength()) {
            player.sendMessage(toComponent(getLocaleMessageString("settings.world-description.error")
                    .replace("%min%", String.valueOf(OpenCreative.getSettings().getRequirements().getWorldDescriptionMinLength()))
                    .replace("%max%", String.valueOf(OpenCreative.getSettings().getRequirements().getWorldDescriptionMaxLength()))));
            return;
        }
        module.getInformation().setDescription(String.join("\\n", lines ));
        module.getInformation().updateIconAsync();
        //OpenCreative.getPlugin().getLogger().info("[WORLD-CHAT: " + module.getId() + "] " + player.getName() + " changed world's description to: " + input);
        Sounds.WORLD_SETTINGS_NAME_CHANGE.play(player);

    }
}