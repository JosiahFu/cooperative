package net.qxeii.cooperative.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import org.apache.commons.lang3.RandomUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    @Inject(at = @At("HEAD"), method = "onPlayerConnected")
    public void joinServer(ServerPlayerEntity player, CallbackInfo ci) {
        if (player.getServer().getPlayerManager().loadPlayerData(player) == null) {
            double lowestDist = -1;
            Vec3d pos = player.getPos();
            ServerWorld world = player.getServerWorld();
            PlayerEntity nearestPlayer = null;
            for (ServerPlayerEntity cplayer : world.getPlayers()) {
                if (cplayer.isAlive() && cplayer.getUuid() != player.getUuid()) {
                    double dist = cplayer.squaredDistanceTo(pos);
                    if (dist < lowestDist || lowestDist == -1) {
                        lowestDist = dist;
                        nearestPlayer = cplayer;
                    }
                }
            }
            if (nearestPlayer != null) {
                Vec3d pos2 = nearestPlayer.getPos();
                double xShift = RandomUtils.nextDouble(0, 100);
                double zShift = RandomUtils.nextDouble(0, 100);
                double x = Math.min(Math.max((xShift - 50), 0), 255);
                double z = Math.min(Math.max((zShift - 50), 0), 255);
                Vec3d vec = Vec3d.ZERO.add(x, 0, z);
                pos2 = pos2.add(vec);
                BlockPos tp = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, BlockPos.ofFloored(pos2));
                player.setSpawnPoint(world.getRegistryKey(), tp, 0F, true, false);
                player.setPosition(Vec3d.of(tp));
                player.networkHandler.requestTeleport(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
            }
        }
    }
}