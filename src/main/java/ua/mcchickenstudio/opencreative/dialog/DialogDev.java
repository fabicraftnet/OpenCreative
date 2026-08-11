package ua.mcchickenstudio.opencreative.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.mcchickenstudio.opencreative.settings.Sounds;
import ua.mcchickenstudio.opencreative.utils.MessageUtils;

import java.time.Duration;
import java.util.List;

import static ua.mcchickenstudio.opencreative.utils.ItemUtils.getCodingValueKey;
import static ua.mcchickenstudio.opencreative.utils.ItemUtils.setPersistentData;
import static ua.mcchickenstudio.opencreative.utils.MessageUtils.*;

/**
 * Dialog handler for setting developer values.
 */
@SuppressWarnings("UnstableApiUsage")
public final class DialogDev {

    public Dialog textValue(PlayerInteractEvent event, Player player, ItemStack currentItem)
    {
        String serialized = userMM.serialize(currentItem.getItemMeta().displayName().compact()) ;

        return Dialog.create(buider -> buider.empty()

                .base(DialogBase.builder(Component.text("Enter text value"))
                        .inputs(List.of(
                                DialogInput.text("text",180,Component.text("Text"),true,serialized ,256, TextDialogInput.MultilineOptions.create(20,null))
                        ))


                        .build())
                .type(DialogType.notice(
                        ActionButton.create(Component.text("Done"),null,60,
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

        Component newName = toComponent(view.getText("text"));
        meta.displayName(newName);
        item.setItemMeta(meta);
        Sounds.DEV_TEXT_SET.play(player);
        setPersistentData(item, getCodingValueKey(), "TEXT");
        player.getInventory().setItemInMainHand(item);
        player.showTitle(Title.title(
                (getLocaleMessage("world.dev-mode.set-variable")), meta.displayName(),
                Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(2), Duration.ofMillis(750))
        ));
    }


}
