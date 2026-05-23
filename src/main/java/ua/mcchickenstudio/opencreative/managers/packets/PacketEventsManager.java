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

package ua.mcchickenstudio.opencreative.managers.packets;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTList;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.nbt.NBTType;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.protocol.world.blockentity.BlockEntityTypes;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import ua.mcchickenstudio.opencreative.OpenCreative;
import ua.mcchickenstudio.opencreative.managers.Toggleable;
import ua.mcchickenstudio.opencreative.utils.world.cache.ChunkCache;

import java.util.*;

import static ua.mcchickenstudio.opencreative.utils.ErrorUtils.sendDebugError;
import static ua.mcchickenstudio.opencreative.utils.MessageUtils.getLocaleComponent;
import static ua.mcchickenstudio.opencreative.utils.world.WorldUtils.isDevPlanet;

/**
 * This class represents an implementation of PacketEvents
 * for packets actions.
 */
public final class PacketEventsManager implements PacketManager, Toggleable {

    //private PacketEventsManager.SignTextListener signListener;
    private PacketEventsManager.ChunkPacketListener chunkListener;

    @Override
    public void start() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(OpenCreative.getPlugin()));
        PacketEvents.getAPI().load();
        PacketEvents.getAPI().init();
        chunkListener = new PacketEventsManager.ChunkPacketListener();
        //signListener = new PacketEventsManager.SignTextListener();
        PacketEvents.getAPI().getEventManager().registerListeners(chunkListener);
    }

    @Override
    public void shutdown() {
        if (chunkListener != null) {
            PacketEvents.getAPI().getEventManager().unregisterListeners(chunkListener);
            chunkListener = null;
        }
    }

    @Override
    public boolean isWorking() {
        return PacketEvents.getAPI().isInitialized();
    }

    @Override
    public void displayGlowingBlock(@NotNull Player player, @NotNull Location location) {
        World world = player.getWorld();
        UUID uuid = UUID.randomUUID();
        int id = 300;
        if (location.getX() == location.getBlockX() && location.getZ() == location.getBlockZ()) {
            location.add(0.5, 0, 0.5);
        }
        PacketWrapper<?> spawnEntityPacket = getSpawnFallingBlockPacket(id, uuid, location);
        PacketWrapper<?> entityDataPacket = getFallingBlockDataPacket(id);
        PacketWrapper<?> createTeamPacket = getTeamCreationPacket(uuid, NamedTextColor.GREEN);
        PacketWrapper<?> hideGlowingPacket = getRemoveEntityPacket(id);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, createTeamPacket);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, spawnEntityPacket);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, entityDataPacket);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.getWorld() == world) {
                    PacketEvents.getAPI().getPlayerManager().sendPacket(player, hideGlowingPacket);
                    PacketEvents.getAPI().getPlayerManager().sendPacket(player, getTeamDeletionPacket());
                }
            }
        }.runTaskLater(OpenCreative.getPlugin(), 60L);
    }

    @Override
    public void sendChestOpenAnimation(@NotNull Player player, @NotNull Block block) {
        Bukkit.getScheduler().runTaskAsynchronously(OpenCreative.getPlugin(), () -> {
            WrapperPlayServerBlockAction packet = new WrapperPlayServerBlockAction(new Vector3i(block.getX(), block.getY(), block.getZ()), 1, 1, SpigotConversionUtil.fromBukkitBlockData(block.getBlockData()).getGlobalId());
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
        });
    }

    @Override
    public void sendChestCloseAnimation(@NotNull Player player, @NotNull Block block) {
        Bukkit.getScheduler().runTaskAsynchronously(OpenCreative.getPlugin(), () -> {
            WrapperPlayServerBlockAction packet = new WrapperPlayServerBlockAction(new Vector3i(block.getX(), block.getY(), block.getZ()), 1, 0, SpigotConversionUtil.fromBukkitBlockData(block.getBlockData()).getGlobalId());
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
        });
    }

    @Override
    public void displayAsSpectatorName(@NotNull Player player, @NotNull Player receiver) {
        WrapperPlayServerPlayerInfoUpdate packet = new WrapperPlayServerPlayerInfoUpdate(
            EnumSet.of(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_GAME_MODE),
            Collections.singletonList(
                new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                        new UserProfile(player.getUniqueId(), player.getName()),
                        true,
                        player.getPing(),
                        com.github.retrooper.packetevents.protocol.player.GameMode.SPECTATOR,
                        Component.text(player.getName()),
                        null)));
        PacketEvents.getAPI().getPlayerManager().sendPacket(receiver, packet);
    }

    @Override
    public void removeSpectatorName(@NotNull Player player, @NotNull Player receiver) {
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        WrapperPlayServerPlayerInfoUpdate packet = new WrapperPlayServerPlayerInfoUpdate(
                EnumSet.of(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_GAME_MODE),
                Collections.singletonList(
                        new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                                new UserProfile(player.getUniqueId(), player.getName()),
                                true,
                                player.getPing(),
                                SpigotConversionUtil.fromBukkitGameMode(player.getGameMode()),
                                Component.text(player.getName()),
                                null)));
        PacketEvents.getAPI().getPlayerManager().sendPacket(receiver, packet);
    }

    @Override
    public void showBlockForPlayer(@NotNull Player player, @NotNull Location location, @NotNull Material material) {
        WrapperPlayServerBlockChange packet = new WrapperPlayServerBlockChange(new Vector3i(location.getBlockX(), location.getBlockY(), location.getBlockZ()), SpigotConversionUtil.fromBukkitBlockData(material.createBlockData()));
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    @Override
    public @NotNull String getName() {
        return "PacketEvents Packet Manager";
    }

    private WrapperPlayServerDestroyEntities getRemoveEntityPacket(int id) {
        return new WrapperPlayServerDestroyEntities(id);
    }

    private WrapperPlayServerTeams getTeamCreationPacket(UUID uuid, NamedTextColor color) {
        return new WrapperPlayServerTeams("oc_block_display", WrapperPlayServerTeams.TeamMode.CREATE,
                new WrapperPlayServerTeams.ScoreBoardTeamInfo(Component.text("oc_block_display"),
                        null, null, WrapperPlayServerTeams.NameTagVisibility.NEVER, WrapperPlayServerTeams.CollisionRule.NEVER,
                        color, WrapperPlayServerTeams.OptionData.NONE), uuid.toString());
    }

    private WrapperPlayServerTeams getTeamDeletionPacket() {
        return new WrapperPlayServerTeams("oc_block_display", WrapperPlayServerTeams.TeamMode.REMOVE,
                (WrapperPlayServerTeams.ScoreBoardTeamInfo) null, new ArrayList<>());
    }

    private WrapperPlayServerEntityMetadata getFallingBlockDataPacket(int id) {
        List<EntityData<?>> entityData = new ArrayList<>();
        entityData.add(new EntityData<>(0, EntityDataTypes.BYTE, (byte) (0x20 | 0x40)));
        entityData.add(new EntityData<>(5, EntityDataTypes.BOOLEAN, true));
        entityData.add(new EntityData<>(16, EntityDataTypes.INT, 2));
        return new WrapperPlayServerEntityMetadata(id, entityData);
    }

    private WrapperPlayServerSpawnEntity getSpawnFallingBlockPacket(int id, UUID uuid, Location location) {
        return new WrapperPlayServerSpawnEntity(id, uuid, EntityTypes.SLIME, new com.github.retrooper.packetevents.protocol.world.Location(new Vector3d(location.getX(), location.getY(), location.getZ()), location.getYaw(), location.getPitch()), 0F, 1, null);
    }

    private static class SignTextListener extends PacketListenerCommon implements PacketListener {

        @Override
        public void onPacketSend(PacketSendEvent event) {
            if (event.getPacketType() != PacketType.Play.Server.BLOCK_ENTITY_DATA) {
                return;
            }
            if (!(event.getPlayer() instanceof Player player)) return;
            World world = player.getWorld();
            if (!isDevPlanet(world)) return;

            WrapperPlayServerBlockEntityData packet = new WrapperPlayServerBlockEntityData(event);
            if (packet.getBlockEntityType() == BlockEntityTypes.SIGN || packet.getBlockEntityType() == BlockEntityTypes.HANGING_SIGN) {
                NBTCompound nbt = packet.getNBT();
                NBTCompound frontText = nbt.getCompoundTagOrNull("front_text");
                if (frontText == null) {
                    return;
                }

                List<String> lines = new ArrayList<>();
                NBTList<NBTCompound> signLinesNBT = frontText.getCompoundListTagOrNull("messages");
                if (signLinesNBT != null) {
                    for (NBTCompound line : signLinesNBT.getTags()) {
                        lines.add(line.getStringTagValueOrDefault("text", ""));
                    }
                }

                NBTList<NBTString> newLines = new NBTList<>(NBTType.STRING);
                for (String line : lines) {
                    if (line.isEmpty()) continue;
                    Component component = getLocaleComponent("blocks." + line, false);
                    String json = GsonComponentSerializer.gson().serialize(component);
                    newLines.addTag(new NBTString(json));
                }

                frontText.setTag("messages", newLines);
                nbt.setTag("front_text", frontText);
                packet.setNBT(nbt);
                event.markForReEncode(true);
            }
        }
    }

    private static class ChunkPacketListener extends PacketListenerCommon implements PacketListener {

        @Override
        public void onPacketSend(PacketSendEvent event) {
            if (event.getPacketType() != PacketType.Play.Server.CHUNK_DATA) return;
            if (!(event.getPlayer() instanceof Player player)) return;
            World world = player.getWorld();
            if (!isDevPlanet(world)) return;
            try {
                WrapperPlayServerChunkData chunkPacket = new WrapperPlayServerChunkData(event);
                Column column = chunkPacket.getColumn();

                int chunkX = column.getX();
                int chunkZ = column.getZ();

                ChunkCache.preLoad(world, chunkX, chunkZ);
            } catch (Exception error) {
                sendDebugError("Cannot preload chunks.", error);
            }
        }
    }
}
