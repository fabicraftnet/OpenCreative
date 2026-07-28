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

package ua.mcchickenstudio.opencreative.coding.blocks.actions.entityactions.other;

import com.destroystokyo.paper.SkinParts;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import ua.mcchickenstudio.opencreative.coding.arguments.Arguments;
import ua.mcchickenstudio.opencreative.coding.blocks.actions.ActionType;
import ua.mcchickenstudio.opencreative.coding.blocks.actions.Target;
import ua.mcchickenstudio.opencreative.coding.blocks.actions.entityactions.EntityAction;

import ua.mcchickenstudio.opencreative.coding.blocks.executors.Executor;
import ua.mcchickenstudio.opencreative.coding.exceptions.UnsupportedEntityException;

import java.util.Optional;

public final class SetMannequinSkinAction extends EntityAction {
    public SetMannequinSkinAction(Executor executor, Target target, int x, Arguments args) {
        super(executor, target, x, args);
    }

    @Override
    public void executeEntity(@NotNull Entity entity) {

        ItemStack item = getArguments().getItem("item", new ItemStack(Material.AIR), this);
        String cape = getArguments().getText("cape", "default", this);
        String model = getArguments().getText("model", "default",this);
        String head = getArguments().getText("head", "default",this);
        String body = getArguments().getText("body", "default",this);
        String leftarm = getArguments().getText("leftarm", "default",this);
        String rightarm = getArguments().getText("rightarm", "default",this);
        String leftleg = getArguments().getText("leftleg", "default",this);
        String rightleg = getArguments().getText("rightleg", "default",this);

        if (!(entity instanceof Mannequin mannequin))
        {
            throw new UnsupportedEntityException(Mannequin.class, entity);
        }

        //get properties
        String properties = "";
        if ((item.getItemMeta() instanceof SkullMeta skull))
        {
            if (skull.getPlayerProfile().hasTextures()) {

                properties = skull.getPlayerProfile().getProperties().stream().findFirst().get().getValue();
            }
        }
        else
        {
            Optional<ProfileProperty> opt = mannequin.getProfile().properties().stream().findFirst();
            if (opt.isPresent())
            {
                properties = opt.get().getValue();
            }
            else{
                //empty object
                return;
            }
        }

        //base64 to JSON
        String textures = new String(java.util.Base64.getDecoder().decode(properties));
        Bukkit.getLogger().info(textures);
        JsonElement texturesJson = JsonParser.parseString(textures);
        JsonObject object = texturesJson.getAsJsonObject();
        //cape
        if (getArguments().pathExists("cape")) {
            if (cape.isEmpty()) object.getAsJsonObject("textures").remove("CAPE");
            else object.getAsJsonObject("textures").getAsJsonObject("CAPE").addProperty("url", "http://textures.minecraft.net/texture/"+cape);
        }
        //model
        if (!model.equals("default"))
        {
            if (model.equals("slim"))
            {
                JsonObject metadata = new JsonObject();
                metadata.addProperty("model","slim");
                object.getAsJsonObject("textres").getAsJsonObject("SKIN").add("metadata",metadata);
            }
            else
            {
                object.getAsJsonObject("textures").getAsJsonObject("SKIN").remove("metadata");
            }

        }
        //build to base64
        Gson gson = new Gson();
        textures = gson.toJson(object);
        textures = java.util.Base64.getEncoder().encodeToString(textures.getBytes());
        //construct skinparts
        SkinParts.Mutable parts = mannequin.getSkinParts().mutableCopy();
        if (!head.equals("default")) parts.setHatsEnabled(head.equals("on"));
        if (!body.equals("default")) parts.setJacketEnabled(body.equals("on"));
        if (!leftarm.equals("default")) parts.setLeftSleeveEnabled(leftarm.equals("on"));
        if (!rightarm.equals("default")) parts.setRightSleeveEnabled(rightarm.equals("on"));
        if (!leftleg.equals("default")) parts.setLeftPantsEnabled(leftleg.equals("on"));
        if (!rightleg.equals("default")) parts.setRightPantsEnabled(rightarm.equals("on"));
        ResolvableProfile resolvableProfile = ResolvableProfile.resolvableProfile().addProperty(new ProfileProperty("textures",textures)).build();
        mannequin.setProfile(resolvableProfile);
        mannequin.setSkinParts(parts);

    }

    @Override
    public @NotNull ActionType getActionType() {
        return ActionType.ENTITY_SET_MANNEQUIN_SKIN;
    }
}
