package dev.worldcleanup.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityDropMixin {
    @Inject(
        method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At("RETURN")
    )
    private void worldCleanup$rememberMobSource(
        ServerLevel level,
        ItemStack stack,
        Vec3 offset,
        CallbackInfoReturnable<ItemEntity> callback
    ) {
        Entity self = (Entity) (Object) this;
        ItemEntity dropped = callback.getReturnValue();
        if (self instanceof Mob && dropped != null) {
            dropped.setThrower(self);
        }
    }
}
