package github.papi.mixin;

import github.papi.PlayerPIIManager;
import github.papi.http.HTTP;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.dimension.PortalManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PortalManager.class)
public class PortalManagerMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("papi");
    private static final PlayerPIIManager piiManager = new PlayerPIIManager();

    @Inject(method = "createTeleportTarget", at = @At("HEAD"), cancellable = true)
    private void preventNetherTeleportTarget(ServerWorld world, Entity entity, CallbackInfoReturnable<TeleportTarget> cir) {
        cir.setReturnValue(null);

        ServerPlayerEntity player = (ServerPlayerEntity) entity;
        String username = player.getNameForScoreboard();

        LOGGER.info("Attempting to opt-out {}", username);

        try {
            HTTP.optOut(username);
            piiManager.clearPlayerPII(player);
            LOGGER.info("Opted out {}", username);
        } catch (Exception e) {
            LOGGER.error("Failed to opt-out user {}: {}", username, e.getMessage());
        }
    }
}