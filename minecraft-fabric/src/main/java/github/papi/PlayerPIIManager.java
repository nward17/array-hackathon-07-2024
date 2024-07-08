package github.papi;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PlayerPIIManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("papi");
    private static final Map<UUID, List<ArmorStandEntity>> armorStandMap = new HashMap<>();
    private static final double OFFSET_FROM_HEAD = 1.8; // Adjust this value to be the height where the name tag would normally appear
    private static final double LINE_HEIGHT = 0.3; // Adjust this value to the desired line height

    public void setPlayerPII(ServerPlayerEntity player, ServerWorld world, PII playerPII) {
        UUID playerUUID = player.getUuid();
        List<ArmorStandEntity> armorStands = armorStandMap.get(playerUUID);

        LOGGER.info("Setting pii for {}", playerPII.getFirstName());

        if (armorStands == null) {
            armorStands = new ArrayList<>();
            armorStands.add(createArmorStand(world, player.getPos(), playerPII.getFirstName() + " " + playerPII.getLastName() + " (" + playerPII.getAge() + ")", Formatting.RED, OFFSET_FROM_HEAD));
            armorStands.add(createArmorStand(world, player.getPos(), playerPII.getCity() + ", " + playerPII.getState(), Formatting.RED, OFFSET_FROM_HEAD - LINE_HEIGHT));
            armorStands.add(createArmorStand(world, player.getPos(), playerPII.getOccupation(), Formatting.RED, OFFSET_FROM_HEAD - LINE_HEIGHT));

            armorStandMap.put(playerUUID, armorStands);

            for (ArmorStandEntity armorStand : armorStands) {
                world.spawnEntity(armorStand);
            }

            LOGGER.info("Created ArmorStands for player: {}", playerUUID);
        }
    }

    public void clearPlayerPII(ServerPlayerEntity player) {
        UUID playerUUID = player.getUuid();
        List<ArmorStandEntity> armorStands = armorStandMap.get(playerUUID);

        if (armorStands == null || armorStands.isEmpty()) {
            // User never had any armor stands, create one with their original username
            armorStands = new ArrayList<>();
            armorStands.add(createArmorStand((ServerWorld) player.getWorld(), player.getPos(), player.getName().getString(), Formatting.WHITE, OFFSET_FROM_HEAD));
            armorStandMap.put(playerUUID, armorStands);

            ArmorStandEntity armorStand = armorStands.get(0);

            // Position the armor stand correctly
            player.getWorld().spawnEntity(armorStand);

            LOGGER.info("Created default ArmorStand for player with original username: {}", playerUUID);
        } else {
            ArmorStandEntity armorStand = armorStands.get(0);
            for (int i = 1; i < armorStands.size(); i++) {
                armorStands.get(i).remove(Entity.RemovalReason.DISCARDED);
            }

            armorStands.clear(); // Ensure this list is mutable
            armorStands.add(armorStand);

            String playerName = player.getName().getString();
            armorStand.setCustomName(Text.literal(playerName).formatted(Formatting.WHITE));

            LOGGER.info("Cleared PII for player: {}", playerUUID);
        }
    }

    public void destroyPlayerPII(ServerPlayerEntity player) {
        UUID playerUUID = player.getUuid();
        List<ArmorStandEntity> armorStands = armorStandMap.remove(playerUUID);

        if (armorStands != null) {
            for (ArmorStandEntity armorStand : armorStands) {
                armorStand.remove(Entity.RemovalReason.DISCARDED);
            }
            LOGGER.info("Removed ArmorStands for player: {}", playerUUID);
        }
    }

    public void updatePlayerPIIPosition() {
        for (UUID playerUUID : armorStandMap.keySet()) {
            ServerPlayerEntity player = null;
            for (ServerWorld world : armorStandMap.get(playerUUID).get(0).getWorld().getServer().getWorlds()) {
                player = (ServerPlayerEntity) world.getPlayerByUuid(playerUUID);
                if (player != null) break;
            }
            if (player == null) continue;

            List<ArmorStandEntity> armorStands = armorStandMap.get(playerUUID);
            if (armorStands != null) {
                Vec3d playerPos = player.getPos();
                int size = armorStands.size();
                for (int i = 0; i < size; i++) {
                    ArmorStandEntity armorStand = armorStands.get(size - 1 - i);
                    armorStand.refreshPositionAndAngles(playerPos.x, playerPos.y + OFFSET_FROM_HEAD + (i * LINE_HEIGHT), playerPos.z, player.getYaw(), player.getPitch());
                }
            }
        }
    }

    private ArmorStandEntity createArmorStand(ServerWorld world, Vec3d pos, String text, Formatting color, double yOffset) {
        ArmorStandEntity armorStand = new ArmorStandEntity(world, pos.x, pos.y + yOffset, pos.z);
        armorStand.setCustomName(Text.literal(text).formatted(color));
        armorStand.setCustomNameVisible(true);
        armorStand.setInvisible(true);
        armorStand.setNoGravity(true);
        return armorStand;
    }
}