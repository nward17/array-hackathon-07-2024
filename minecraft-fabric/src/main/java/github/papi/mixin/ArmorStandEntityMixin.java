package github.papi.mixin;


import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;

@Mixin(ArmorStandEntity.class)
public abstract class ArmorStandEntityMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("papi");

    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At("TAIL"))
    private void onConstruct(EntityType<? extends ArmorStandEntity> entityType, World world, CallbackInfo ci) {
        try {
            Method setMarkerMethod = ArmorStandEntity.class.getDeclaredMethod("setMarker", boolean.class);
            setMarkerMethod.setAccessible(true);
            setMarkerMethod.invoke(this, true);
        } catch (Exception e) {
            LOGGER.error("Failed to set marker for armor stand", e);
        }
    }
}