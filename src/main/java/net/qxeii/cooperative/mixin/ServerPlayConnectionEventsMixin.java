package net.qxeii.cooperative.mixin;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypeRegistrar;
import net.minecraft.world.dimension.DimensionTypes;
import org.apache.commons.lang3.RandomUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;


@Mixin(net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.class)
class ServerPlayConnectionEventsMixin {

    @Inject(at = @At("HEAD"), method = "lambda$static$2")
    private static void joinServer(ServerPlayConnectionEvents.Join[] callbacks, ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server, CallbackInfo ci) {
        ServerPlayerEntity player = handler.getPlayer();
        if (player.getServer().getPlayerManager().loadPlayerData(player) == null) {
            double lowestDist = -1;
            Vec3d pos = player.getPos();
            ServerWorld world = player.getServerWorld();
            PlayerEntity nearestPlayer = null;
            for (String playername : server.getPlayerNames()) {
                ServerPlayerEntity cplayer = server.getPlayerManager().getPlayer(playername);
                if (!cplayer.isDead() && cplayer.getUuid() != player.getUuid()) {
                    double dist = cplayer.squaredDistanceTo(pos);
                    if (dist < lowestDist || lowestDist == -1) {
                        lowestDist = dist;
                        nearestPlayer = cplayer;
                    }
                }
            }
            if (nearestPlayer != null) {
                ServerWorld newWorld = ((ServerPlayerEntity) nearestPlayer).getServerWorld();
                BlockPos tp;
                if (!newWorld.getDimension().hasCeiling() && newWorld.getDimension().bedWorks()) {
                    Vec3d pos2 = nearestPlayer.getPos();
                    double xShift = RandomUtils.nextDouble(0, 100);
                    double zShift = RandomUtils.nextDouble(0, 100);
                    double x = (xShift - 50);
                    double z = (zShift - 50);
                    Vec3d vec = Vec3d.ZERO.add(x, 0, z);
                    pos2 = pos2.add(vec);
                    tp = newWorld.getTopPosition(Heightmap.Type.MOTION_BLOCKING, BlockPos.ofFloored(pos2));
                    tp = newWorld.getWorldBorder().clamp(tp.getX(), tp.getY(), tp.getZ());
                } else {
                    tp = nearestPlayer.getBlockPos();
                }
                player.setSpawnPoint(newWorld.getRegistryKey(), tp, 0F, true, false);
            } else {
                player.setSpawnPoint(world.getRegistryKey(), world.getSpawnPos(), 0F, false, false);
            }
        }
    }
}