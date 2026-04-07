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

package ua.mcchickenstudio.opencreative.indev;

import com.google.gson.*;
import com.google.gson.annotations.Since;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

import static ua.mcchickenstudio.opencreative.utils.ErrorUtils.sendDebugError;
import static ua.mcchickenstudio.opencreative.utils.FileUtils.getWanderJsonFile;
import static ua.mcchickenstudio.opencreative.utils.MessageUtils.getLocaleMessage;

/**
 * <h1>OfflineWander</h1>
 * This class represents wander, that could be offline
 * or online. Wander is a player, who plays on planets.
 * He has nickname, description, gender, favorite
 * worlds and last played world.
 */
public class OfflineWander {

    private static final Logger log = LoggerFactory.getLogger(OfflineWander.class);
    @Since(5.6)
    protected final @NotNull UUID uuid;
    @Since(5.6)
    protected @Nullable String name;
    @Since(5.6)
    protected @Nullable String description;
    @Since(5.6)
    protected @Nullable Gender gender;
    @Since(5.6)
    protected @Nullable Set<Integer> favoriteWorlds;
    @Since(5.6)
    protected int lastPlayedWorldId = -1;
    @Since(6.0)
    protected int visits = 0;
    @Since(6.0)
    protected Links links;
    @Since(5.6)
    protected boolean hideHints;
    @Since(5.8)
    protected @Nullable List<UUID> friends;
    @Since(5.8)
    protected Location lastLocation;

    public OfflineWander(@NotNull UUID uuid) {
        this.uuid = uuid;
        loadInfo();
    }

    public OfflineWander(@NotNull OfflinePlayer offlinePlayer) {
        this.uuid = offlinePlayer.getUniqueId();
        loadInfo();
    }

    public @NotNull OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    public boolean isOnline() {
        return Bukkit.getPlayer(uuid) != null;
    }

    public @NotNull UUID getUniqueId() {
        return uuid;
    }

    public @Nullable String getName() {
        return name;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public @Nullable Gender getGender() {
        return gender;
    }

    public @Nullable Location getLastLocation() {
        return lastLocation;
    }

    public @Nullable String getLink(@NotNull String type) {
        if (links == null) return null;
        return links.getLink(type);
    }

    public void setLink(@NotNull String type, @NotNull String link) {
        if (links == null) {
            this.links = new Links();
        }
        links.setLink(type, link);
        saveData();
    }

    public void removeLink(@NotNull String type) {
        if (links == null) return;
        links.setLink(type, null);
        saveData();
    }

    public int getLastPlayedWorldId() {
        return lastPlayedWorldId;
    }

    public int getVisits() {
        return visits;
    }

    public boolean shouldHideHints() {
        return hideHints;
    }

    public @NotNull Set<Integer> getFavoriteWorlds() {
        return favoriteWorlds != null ? favoriteWorlds : Set.of();
    }

    public @NotNull List<UUID> getFriends() {
        return friends != null ? friends : List.of();
    }

    public void setLastPlayedWorldId(int lastPlayedWorldId) {
        this.lastPlayedWorldId = lastPlayedWorldId;
        saveData();
    }

    public void setVisits(int visits) {
        this.visits = visits;
        saveData();
    }

    public void setName(@NotNull String name) {
        this.name = name;
        saveData();
    }

    public void setDescription(@NotNull String description) {
        this.description = description;
        saveData();
    }

    public boolean addFriend(@NotNull UUID friendUUID) {
        if (uuid.equals(friendUUID)) {
            return false;
        }
        if (getFriends().contains(friendUUID)) {
            return false;
        }
        if (friends == null) friends = new ArrayList<>();
        friends.add(friendUUID);
        saveData();
        return true;
    }

    public boolean removeFriend(@NotNull UUID friendUUID) {
        if (uuid.equals(friendUUID)) {
            return false;
        }
        if (friends == null || !getFriends().contains(friendUUID)) {
            return false;
        }
        friends.remove(friendUUID);
        saveData();
        return true;
    }

    public boolean addFavoriteWorld(int worldId) {
        if (getFavoriteWorlds().contains(worldId)) {
            return false;
        }
        if (favoriteWorlds == null) favoriteWorlds = new HashSet<>();
        favoriteWorlds.add(worldId);
        saveData();
        return true;
    }

    public boolean removeFavoriteWorld(int worldId) {
        if (favoriteWorlds == null || !getFavoriteWorlds().contains(worldId)) {
            return false;
        }
        favoriteWorlds.remove(worldId);
        saveData();
        return true;
    }

    public boolean setGender(@NotNull Gender gender) {
        if (this.gender == gender) {
            return false;
        }
        this.gender = gender;
        saveData();
        return true;
    }

    public boolean clearData() {
        File jsonFile = getWanderJsonFile(this,false);
        if (jsonFile == null || !jsonFile.exists()) {
            return false;
        }
        this.name = null;
        this.description = null;
        this.gender = null;
        this.lastPlayedWorldId = -1;
        this.getFavoriteWorlds().clear();
        this.lastLocation = null;
        try {
            return jsonFile.delete();
        } catch (Exception error) {
            sendDebugError("Can't delete wander file: " + uuid, error);
            return false;
        }
    }

    public void loadInfo() {
        File jsonFile = getWanderJsonFile(this,false);
        if (jsonFile == null || !jsonFile.exists()) {
            return;
        }
        try (Reader reader = new FileReader(jsonFile)) {
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(reader, JsonObject.class);

            if (json.has("name")) {
                this.name = json.get("name").getAsString();
            }

            if (json.has("description")) {
                this.description = json.get("description").getAsString();
            }

            if (json.has("gender")) {
                this.gender = Gender.getGender(json.get("gender").getAsString());
            }

            if (json.has("favoriteWorlds")) {
                JsonArray arr = json.getAsJsonArray("favoriteWorlds");
                this.favoriteWorlds = new HashSet<>();
                for (JsonElement el : arr) {
                    this.favoriteWorlds.add(el.getAsInt());
                }
            }

            if (json.has("friends")) {
                JsonArray arr = json.getAsJsonArray("friends");
                this.friends = new ArrayList<>();
                for (JsonElement el : arr) {
                    try {
                        UUID friend = UUID.fromString(el.getAsString());
                        this.friends.add(friend);
                    } catch (Exception ignored) {}
                }
            }

            if (json.has("lastPlayedWorldId")) {
                this.lastPlayedWorldId = json.get("lastPlayedWorldId").getAsInt();
            }

            if (json.has("hideHints")) {
                this.hideHints = json.get("hideHints").getAsBoolean();
            }

            if (json.has("lastLocation")) {
                JsonObject lastLoc = json.get("lastLocation").getAsJsonObject();
                double x = lastLoc.get("x").getAsDouble();
                double y = lastLoc.get("y").getAsDouble();
                double z = lastLoc.get("z").getAsDouble();
                float yaw = lastLoc.get("yaw").getAsFloat();
                float pitch = lastLoc.get("pitch").getAsFloat();
                this.lastLocation = new Location(null, x, y, z, yaw, pitch);
            }

            if (json.has("socialLinks") && json.get("socialLinks").isJsonObject()) {
                links = new Links();
                JsonObject socialLinks = json.getAsJsonObject("socialLinks");
                for (Map.Entry<String, JsonElement> entry : socialLinks.entrySet()) {
                    String type = entry.getKey();
                    String value = entry.getValue().getAsString();
                    links.setLink(type, value);
                }
            }

        } catch (Exception error) {
            sendDebugError("Failed to load info for wander " + uuid, error);
        }
    }

    public void saveData() {
        try {
            if (!shouldSaveData()) {
                return;
            }
            File jsonFile = getWanderJsonFile(this,true);
            if (jsonFile == null) {
                return;
            }
            JsonObject json = getJsonObject();
            try (Writer writer = new FileWriter(jsonFile)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(json, writer);
            }
        } catch (Exception error) {
            sendDebugError("Failed to save wander data: " + uuid, error);
        }
    }

    private boolean shouldSaveData() {
        return favoriteWorlds != null || description != null || gender != null || lastPlayedWorldId != -1;
    }

    private JsonObject getJsonObject() {
        JsonObject json = new JsonObject();
        json.addProperty("uuid", uuid.toString());
        if (name != null) json.addProperty("name", name);
        if (description != null) json.addProperty("description", description);
        if (gender != null) json.addProperty("gender", gender.name());
        JsonArray favoriteWorlds = new JsonArray();
        if (this.favoriteWorlds != null) {
            for (int id : this.favoriteWorlds) {
                favoriteWorlds.add(id);
            }
        }
        if (!favoriteWorlds.isEmpty()) json.add("favoriteWorlds", favoriteWorlds);
        if (lastPlayedWorldId != -1) json.addProperty("lastPlayedWorldId", lastPlayedWorldId);
        if (visits > 0) json.addProperty("visits", visits);
        if (hideHints) json.addProperty("hideHints", true);
        if (lastLocation != null) {
            JsonObject lastLoc = new JsonObject();
            lastLoc.addProperty("x", lastLocation.getX());
            lastLoc.addProperty("y", lastLocation.getY());
            lastLoc.addProperty("z", lastLocation.getZ());
            lastLoc.addProperty("yaw", lastLocation.getYaw());
            lastLoc.addProperty("pitch", lastLocation.getPitch());
            json.add("lastLocation", lastLoc);
        }
        if (links != null && !links.getLinks().isEmpty()) {
            JsonObject socialLinks = new JsonObject();
            for (String type : links.getLinks().keySet()) {
                socialLinks.addProperty(type, links.getLinks().get(type));
            }
            json.add("socialLinks", socialLinks);
        }
        return json;
    }

    public enum Gender {

        MALE,
        FEMALE,
        NON_BINARY,
        UNKNOWN;

        public static @NotNull Gender getGender(@NotNull String text) {
            for (Gender gender : values()) {
                if (gender.name().equalsIgnoreCase(text)) {
                    return gender;
                }
            }
            return UNKNOWN;
        }

        public final @NotNull String getLocaleName() {
            return getLocaleMessage("profiles.genders." + name().toLowerCase().replace("_", "-"), false);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OfflineWander wander) {
            return uuid.equals(wander.uuid);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        return uuid.toString();
    }
}
