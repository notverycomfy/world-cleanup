package dev.worldcleanup.mixin;

import dev.worldcleanup.WorldCleanup;
import dev.worldcleanup.CleanupTrackedItem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin implements CleanupTrackedItem {
    @Shadow
    private int age;

    @Unique
    private int worldCleanup$targetLifetime = 6000;

    @Unique
    private int worldCleanup$nextRefresh;

    @Unique
    private boolean worldCleanup$playerThrown;

    @Unique
    private boolean worldCleanup$mobDropped;

    @Inject(method = "tick", at = @At("HEAD"))
    private void worldCleanup$classify(CallbackInfo callback) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (self.level().isClientSide()) return;

        worldCleanup$playerThrown |= self.getOwner() instanceof Player;
        worldCleanup$mobDropped |= self.getOwner() instanceof Mob;
        if (self.tickCount >= worldCleanup$nextRefresh) {
            worldCleanup$targetLifetime = WorldCleanup.lifetimeTicks(self, worldCleanup$playerThrown);
            worldCleanup$nextRefresh = self.tickCount + WorldCleanup.refreshTicks();
        }
    }

    @Override
    public int worldCleanup$sourceKind() {
        ItemEntity self = (ItemEntity) (Object) this;
        if (worldCleanup$playerThrown || self.getOwner() instanceof Player) return 2;
        if (worldCleanup$mobDropped || self.getOwner() instanceof Mob) return 1;
        return 0;
    }

    @Inject(method = "tryToMerge", at = @At("HEAD"), cancellable = true)
    private void worldCleanup$keepDifferentSourcesSeparate(ItemEntity other, CallbackInfo callback) {
        int thisSource = worldCleanup$sourceKind();
        int otherSource = ((CleanupTrackedItem) other).worldCleanup$sourceKind();
        if (thisSource != otherSource) callback.cancel();
    }

    @Inject(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V", ordinal = 1),
        cancellable = true
    )
    private void worldCleanup$extendVanillaLifetime(CallbackInfo callback) {
        if (age < worldCleanup$targetLifetime) callback.cancel();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void worldCleanup$shortenLifetime(CallbackInfo callback) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (!self.level().isClientSide() && self.isAlive() && age >= worldCleanup$targetLifetime) {
            self.discard();
        }
    }
}
