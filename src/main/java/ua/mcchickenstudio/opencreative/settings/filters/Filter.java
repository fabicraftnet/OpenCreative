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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Filter {

    private final List<FilterRule> rules = new ArrayList<>();
    private final List<Pattern> whitelist = new ArrayList<>();
    private static Filter instance;

    public static @NotNull Filter getInstance() {
        if (instance == null) {
            instance = new Filter();
        }
        return instance;
    }

    public @NotNull FilterResult checkContent(@NotNull String text,
                                              @NotNull Context context) {
        String editedText = text.toLowerCase();
        for (FilterRule rule : rules) {
            if (context.isCompatibleRule(rule) && rule.matches(editedText)) {
                return new FilterResult(rule.filterText(editedText), rule);
            }
        }
        return new FilterResult(text, null);
    }

    public @NotNull List<Pattern> getWhitelistPatterns() {
        return whitelist;
    }

    public void addWhitelist(@NotNull Pattern pattern) {
        whitelist.add(pattern);
    }

    public void addRule(@NotNull FilterRule rule) {
        rules.add(rule);
    }

    public void clearRules() {
        rules.clear();
        whitelist.clear();
    }

    public enum Context {

        CHAT,
        SIGN,
        ANVIL,
        BOOK;

        public boolean isCompatibleRule(@NotNull FilterRule rule) {
            return switch (this) {
                case CHAT -> rule.shouldCheckChat();
                case SIGN -> rule.shouldCheckSigns();
                case ANVIL -> rule.shouldCheckAnvils();
                case BOOK -> rule.shouldCheckBooks();
            };
        }

    }

}
