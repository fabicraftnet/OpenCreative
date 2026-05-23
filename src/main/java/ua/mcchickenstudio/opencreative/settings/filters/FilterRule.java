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

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ua.mcchickenstudio.opencreative.settings.Command;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterRule {

    private final String id;
    private final boolean warnPlayer;
    private final boolean notifyStaff;

    private final boolean checkChat;
    private final boolean checkAnvils;
    private final boolean checkBooks;
    private final boolean checkSigns;

    private final FilterTextAction action;
    private final String replacement;
    private final List<Pattern> patterns;
    private final List<Command> commands;

    public FilterRule(@NotNull String id, boolean warnPlayer, boolean notifyStaff,
                      boolean checkChat, boolean checkAnvils,
                      boolean checkBooks, boolean checkSigns,
                      @NotNull FilterTextAction action, @NotNull String replacement,
                      @NotNull List<Pattern> patterns, @NotNull List<Command> commands) {
        this.warnPlayer = warnPlayer;
        this.notifyStaff = notifyStaff;
        this.action = action;
        this.replacement = replacement;
        this.patterns = patterns;
        this.commands = commands;
        this.checkChat = checkChat;
        this.checkBooks = checkBooks;
        this.checkAnvils = checkAnvils;
        this.checkSigns = checkSigns;
        this.id = id;
    }

    public boolean matches(@NotNull String text) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(text).find()) {
                return true;
            }
        }
        return false;
    }

    public boolean shouldCheckAnvils() {
        return checkAnvils;
    }

    public boolean shouldCheckBooks() {
        return checkBooks;
    }

    public boolean shouldCheckChat() {
        return checkChat;
    }

    public boolean shouldCheckSigns() {
        return checkSigns;
    }

    public boolean shouldWarnPlayer() {
        return warnPlayer;
    }

    public boolean shouldNotifyStaff() {
        return notifyStaff;
    }

    public @NotNull String filterText(@NotNull String text) {
        return switch (action) {
            case FULL -> replacement.repeat(text.length()).substring(0, text.length());
            case SPACES -> {
                StringBuilder builder = new StringBuilder();
                for (char character : text.toCharArray()) {
                    builder.append(character == ' ' ? character : replacement);
                }
                yield builder.toString();
            }
            case COVER_WORDS -> {
                String result = text;
                for (Pattern pattern : patterns) {
                    Matcher matcher = pattern.matcher(result);
                    StringBuilder builder = new StringBuilder();
                    while (matcher.find()) {
                        String word = matcher.group();
                        boolean isWhitelisted = false;
                        for (Pattern whitelisted : Filter.getInstance().getWhitelistPatterns()) {
                            if (whitelisted.matcher(word).find()) {
                                isWhitelisted = true;
                                break;
                            }
                        }
                        if (isWhitelisted) continue;
                        String covered = replacement.repeat(word.length());
                        matcher.appendReplacement(builder, covered);
                    }
                    matcher.appendTail(builder);
                    result = builder.toString();
                }
                yield result;
            }
            case REPLACE_WORDS -> {
                String result = text;
                for (Pattern pattern : patterns) {
                    Matcher matcher = pattern.matcher(result);
                    while (matcher.find()) {
                        String word = matcher.group();
                        boolean isWhitelisted = false;
                        for (Pattern whitelisted : Filter.getInstance().getWhitelistPatterns()) {
                            if (whitelisted.matcher(word).find()) {
                                isWhitelisted = true;
                                break;
                            }
                        }
                        if (isWhitelisted) continue;
                        result = result.replace(word, replacement);
                    }
                }
                yield result;
            }
        };
    }

    public void executeCommands(@NotNull Player player,
                                @NotNull String content,
                                @NotNull String result) {
        for (Command command : commands) {
            command.execute(player, Map.of("%player%", player.getName(),
                    "%content%", content, "%result%", result,
                    "%rule%", id));
        }
    }

    public @NotNull String getId() {
        return id;
    }
}
