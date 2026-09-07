package ua.mcchickenstudio.opencreative.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.mcchickenstudio.opencreative.coding.variables.VariableLink;
import ua.mcchickenstudio.opencreative.settings.Sounds;
import ua.mcchickenstudio.opencreative.utils.MessageUtils;

import java.time.Duration;
import java.util.List;

import static ua.mcchickenstudio.opencreative.utils.ItemUtils.*;
import static ua.mcchickenstudio.opencreative.utils.MessageUtils.*;

/**
 * Dialog handler for setting developer values.
 */
@SuppressWarnings("UnstableApiUsage")
public final class DialogDev {
    private static VariableLink.VariableType getVariableType(ItemMeta meta) {
        TextColor color = meta.displayName().color();
        VariableLink.VariableType type = VariableLink.VariableType.LOCAL;
        if (color.equals(NamedTextColor.YELLOW)) {
            type = VariableLink.VariableType.GLOBAL;
        } else if (color.equals(NamedTextColor.GREEN)) {
            type = VariableLink.VariableType.SAVED;
        }
        return type;
    }
    public Dialog textValue(PlayerInteractEvent event, Player player, ItemStack currentItem)
    {

        String serialized = currentItem.getItemMeta().hasItemName() ?
                textSerializer.serialize(currentItem.getItemMeta().itemName())
                : userMM.serialize(currentItem.getItemMeta().displayName().compact());

        return Dialog.create(buider -> buider.empty()

                .base(DialogBase.builder(getLocaleMessageComponent("dialog.dev.text.title"))
                        .inputs(List.of(
                                DialogInput.text("text",180,getLocaleMessageComponent("dialog.dev.text.inputs.text"),true,serialized ,256, TextDialogInput.MultilineOptions.create(20,null))
                        ))


                        .build())
                .type(DialogType.notice(
                        ActionButton.create(getLocaleMessageComponent("dialog.dev.text.inputs.confirm"),null,100,
                                DialogAction.customClick(
                                        (view, audience) -> applyText(view, player, currentItem)
                                        , ClickCallback.Options.builder().build()
                                )
                                ))
                ));
    }
    private void applyText(DialogResponseView view, Player player, ItemStack item)
    {
        ItemMeta meta = item.getItemMeta();

        Component newName = toComponent(view.getText("text").split("\n")[0]);
        meta.itemName(Component.text(view.getText("text")));
        meta.displayName(newName);
        item.setItemMeta(meta);
        Sounds.DEV_TEXT_SET.play(player);
        setPersistentData(item, getCodingValueKey(), "TEXT");
        player.getInventory().setItemInMainHand(item);
        player.showTitle(Title.title(
                (getLocaleMessageComponent("world.dev-mode.set-variable")), meta.displayName(),
                Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(2), Duration.ofMillis(750))
        ));
    }
    public Dialog numberValue(PlayerInteractEvent event, Player player, ItemStack currentItem)
    {
        String serialized =  MessageUtils.textSerializer.serialize(currentItem.getItemMeta().hasDisplayName() ? currentItem.getItemMeta().displayName() : currentItem.getItemMeta().itemName());
        return Dialog.create(buider -> buider.empty()

                .base(DialogBase.builder(getLocaleMessageComponent("dialog.dev.number.title"))
                        .inputs(List.of(
                                DialogInput.text("number",180,getLocaleMessageComponent("dialog.dev.number.inputs.number"),true,serialized ,32, TextDialogInput.MultilineOptions.create(1,null))
                        ))
                        .body(List.of(
                                DialogBody.plainMessage(toComponent("<sprite:gui:icon/info>").hoverEvent(miniMessage.deserialize(getLocaleMessageString("dialog.dev.number.hint"))))
                        ))

                        .build())
                .type(DialogType.notice(
                        ActionButton.create(getLocaleMessageComponent("dialog.dev.number.inputs.confirm"),null,100,
                                DialogAction.customClick(
                                        (view, audience) -> applyNumber(view, player, currentItem)
                                        , ClickCallback.Options.builder().build()
                                )
                        ))
                ));
    }
    private void applyNumber(DialogResponseView view, Player player, ItemStack item)
    {
        String numberString = view.getText("number");
        if (numberString.equalsIgnoreCase("p") || numberString.equalsIgnoreCase("pi")) {
            numberString = "3.1415926";
        }
        Double number = parseTicks(numberString);
        if (number == null) {
            player.showTitle(Title.title(
                    Component.empty(), (getLocaleMessageComponent("world.dev-mode.set-variable-number-error")),
                    Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(2), Duration.ofMillis(750))
            ));
        }
        ItemMeta meta = item.getItemMeta();
        meta.displayName(toComponent("§a" + number));
        item.setItemMeta(meta);
        setPersistentData(item, getCodingValueKey(), "NUMBER");
        Sounds.DEV_NUMBER_SET.play(player);
        player.setItemInHand(item);
        player.showTitle(Title.title(
                (getLocaleMessageComponent("world.dev-mode.set-variable")), meta.displayName(),
                Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(2), Duration.ofMillis(750))
        ));
    }
    public Dialog variableValue(PlayerInteractEvent event, Player player, ItemStack currentItem)
    {
        String serialized = ((TextComponent) currentItem.getItemMeta().displayName()).content();
        VariableLink.VariableType type = getVariableType(currentItem.getItemMeta());
        return Dialog.create(buider -> buider.empty()

                .base(DialogBase.builder(getLocaleMessageComponent("dialog.dev.variable.title"))
                        .inputs(List.of(
                                DialogInput.text("text",180,getLocaleMessageComponent("dialog.dev.variable.inputs.name"),true,serialized ,32, TextDialogInput.MultilineOptions.create(1,null)),
                                DialogInput.singleOption("space",getLocaleMessageComponent("dialog.dev.variable.inputs.space.title"),
                                        List.of(
                                                SingleOptionDialogInput.OptionEntry.create("GLOBAL",getLocaleMessageComponent("dialog.dev.variable.inputs.space.global"), type == VariableLink.VariableType.GLOBAL),
                                                SingleOptionDialogInput.OptionEntry.create("LOCAL",getLocaleMessageComponent("dialog.dev.variable.inputs.space.local"), type == VariableLink.VariableType.LOCAL),
                                                SingleOptionDialogInput.OptionEntry.create("SAVED",getLocaleMessageComponent("dialog.dev.variable.inputs.space.saved"), type == VariableLink.VariableType.SAVED)
                                        )).build()
                        ))


                        .build())
                .type(DialogType.notice(
                        ActionButton.create(getLocaleMessageComponent("dialog.dev.variable.inputs.confirm"),null,100,
                                DialogAction.customClick(
                                        (view, audience) -> applyVariable(view, player, currentItem)
                                        , ClickCallback.Options.builder().build()
                                )
                        ))
                ));
    }
    private void applyVariable(DialogResponseView view, Player player, ItemStack item)
    {
        Component newValue = Component.text(view.getText("text"));
        ItemMeta meta = item.getItemMeta();
        String type = view.getText("space");
        newValue = newValue.color(type.equalsIgnoreCase("GLOBAL") ? NamedTextColor.YELLOW : type.equalsIgnoreCase("LOCAL") ? NamedTextColor.RED : NamedTextColor.GREEN);
        meta.displayName(newValue);
        item.setItemMeta(meta);
        setPersistentData(item, getCodingValueKey(), "VARIABLE");
        setPersistentData(item, getCodingVariableTypeKey(), type);
        Sounds.DEV_VARIABLE_SET.play(player);
        player.getInventory().setItemInMainHand(item);
        player.showTitle(Title.title(
                (getLocaleMessageComponent("world.dev-mode.set-variable")), meta.displayName(),
                Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(2), Duration.ofMillis(750))
        ));
    }
    public Dialog vectorValue(PlayerInteractEvent event, Player player, ItemStack currentItem)
    {
        String[] vector = ((TextComponent) currentItem.getItemMeta().displayName()).content().split(" ");
        if (vector.length != 3) {
            vector = new String[]{"0.0","0.0","0.0"};
        }
        String[] finalVector = vector;
        return Dialog.create(buider -> buider.empty()

                .base(DialogBase.builder(getLocaleMessageComponent("dialog.dev.vector.title"))
                        .inputs(List.of(
                                DialogInput.text("x",180,getLocaleMessageComponent("dialog.dev.vector.inputs.x"),true,finalVector[0] ,32, TextDialogInput.MultilineOptions.create(1,null)),
                                DialogInput.text("y",180,getLocaleMessageComponent("dialog.dev.vector.inputs.y"),true,finalVector[1] ,32, TextDialogInput.MultilineOptions.create(1,null)),
                                DialogInput.text("z",180,getLocaleMessageComponent("dialog.dev.vector.inputs.z"),true,finalVector[2] ,32, TextDialogInput.MultilineOptions.create(1,null))
                        ))
                        .body(List.of(
                                DialogBody.plainMessage(toComponent("<sprite:gui:icon/info>").hoverEvent(miniMessage.deserialize(getLocaleMessageString("dialog.dev.vector.hint"))))
                        ))

                        .build())
                .type(DialogType.notice(
                        ActionButton.create(getLocaleMessageComponent("dialog.dev.vector.inputs.confirm"),null,100,
                                DialogAction.customClick(
                                        (view, audience) -> applyVector(view, player, currentItem)
                                        , ClickCallback.Options.builder().build()
                                )
                        ))
                ));
    }
    private void applyVector(DialogResponseView view, Player player, ItemStack item)
    {
        String xInput = view.getText("x");
        String yInput = view.getText("y");
        String zInput = view.getText("z");
        double x = 0;
        double y = 0;
        double z = 0;

        try {
            x = Double.parseDouble(xInput);
        } catch (NumberFormatException ignored){}
        try {
            y = Double.parseDouble(yInput);
        } catch (NumberFormatException ignored){}
        try {
            z = Double.parseDouble(zInput);
        } catch (NumberFormatException ignored){}
        ItemMeta meta = item.getItemMeta();
        meta.displayName(toComponent("§a" + x+" "+y+" "+z));
        item.setItemMeta(meta);
        setPersistentData(item, getCodingValueKey(), "VECTOR");
        player.showTitle(Title.title(
                toComponent(getLocaleMessageString("world.dev-mode.set-variable")), meta.displayName(),
                Title.Times.times(Duration.ofMillis(750), Duration.ofSeconds(2), Duration.ofMillis(500))
        ));
        Sounds.DEV_VECTOR_SET.play(player);
    }
    public Dialog locationValue(PlayerInteractEvent event, Player player, ItemStack currentItem)
    {
        String[] vector = ((TextComponent) currentItem.getItemMeta().displayName()).content().split(" ");
        if (vector.length != 3) {
            vector = new String[]{"0.0","0.0","0.0"};
        }
        String[] finalVector = vector;
        return Dialog.create(buider -> buider.empty()

                .base(DialogBase.builder(getLocaleMessageComponent("dialog.dev.vector.title"))
                        .inputs(List.of(
                                DialogInput.text("x",180,getLocaleMessageComponent("dialog.dev.vector.inputs.x"),true,finalVector[0] ,32, TextDialogInput.MultilineOptions.create(1,null)),
                                DialogInput.text("y",180,getLocaleMessageComponent("dialog.dev.vector.inputs.y"),true,finalVector[1] ,32, TextDialogInput.MultilineOptions.create(1,null)),
                                DialogInput.text("z",180,getLocaleMessageComponent("dialog.dev.vector.inputs.z"),true,finalVector[2] ,32, TextDialogInput.MultilineOptions.create(1,null))
                        ))
                        .body(List.of(
                                DialogBody.plainMessage(toComponent("<sprite:gui:icon/info>").hoverEvent(miniMessage.deserialize(getLocaleMessageString("dialog.dev.vector.hint"))))
                        ))

                        .build())
                .type(DialogType.notice(
                        ActionButton.create(getLocaleMessageComponent("dialog.dev.vector.inputs.confirm"),null,60,
                                DialogAction.customClick(
                                        (view, audience) -> applyVector(view, player, currentItem)
                                        , ClickCallback.Options.builder().build()
                                )
                        ))
                ));
    }
    private void locationVector(DialogResponseView view, Player player, ItemStack item)
    {
        String xInput = view.getText("x");
        String yInput = view.getText("y");
        String zInput = view.getText("z");
        double x = 0;
        double y = 0;
        double z = 0;

        try {
            x = Double.parseDouble(xInput);
        } catch (NumberFormatException ignored){}
        try {
            y = Double.parseDouble(yInput);
        } catch (NumberFormatException ignored){}
        try {
            z = Double.parseDouble(zInput);
        } catch (NumberFormatException ignored){}
        ItemMeta meta = item.getItemMeta();
        meta.displayName(toComponent("§a" + x+" "+y+" "+z));
        item.setItemMeta(meta);
        setPersistentData(item, getCodingValueKey(), "VECTOR");
        player.showTitle(Title.title(
                toComponent(getLocaleMessageString("world.dev-mode.set-variable")), meta.displayName(),
                Title.Times.times(Duration.ofMillis(750), Duration.ofSeconds(2), Duration.ofMillis(500))
        ));
        Sounds.DEV_VECTOR_SET.play(player);
    }

}
