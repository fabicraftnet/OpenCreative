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

package ua.mcchickenstudio.opencreative.settings.filters;

import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ua.mcchickenstudio.opencreative.OpenCreative;
import ua.mcchickenstudio.opencreative.indev.messages.PlaceholderReplacer;
import ua.mcchickenstudio.opencreative.settings.Sounds;

import java.util.List;

import static ua.mcchickenstudio.opencreative.utils.MessageUtils.getLocaleComponent;
import static ua.mcchickenstudio.opencreative.utils.MessageUtils.substring;

public record FilterResult(@NotNull String originalMessage, @NotNull String filteredMessage, @NotNull Filter.Context context, @Nullable FilterRule rule, @NotNull List<String> matches) {

    public void onViolation(@NotNull Player player) {
        if (rule == null) return;
        rule.onViolation(player, this);
    }

}
