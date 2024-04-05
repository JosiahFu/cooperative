package net.qxeii.cooperative.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import org.apache.commons.lang3.RandomUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    /**
     * How far away from other players this player must respawn in order to be teleported
     */
    @Unique
    private static final int MINIMUM_RESPAWN_DISTANCE = 16 * 2;

    @Inject(at = @At("HEAD"), method="respawnPlayer")
    public void respawnPlayer(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir) {
            double lowestDist = -1;
            Vec3d deathPos = player.getPos();
            MinecraftServer server = player.getServer();
            if (server == null) return;

            BlockPos spawnPos = player.getSpawnPointPosition();

            ServerPlayerEntity nearestPlayer = null;
            for (ServerPlayerEntity cplayer : server.getPlayerManager().getPlayerList()) {
                if (!cplayer.isDead() && cplayer.getUuid() != player.getUuid()) {
                    double dist = cplayer.squaredDistanceTo(deathPos);
                    if (dist < lowestDist || lowestDist == -1) {
                        lowestDist = dist;
                        nearestPlayer = cplayer;
                    }
                }
            }
            if (nearestPlayer != null && (spawnPos == null || !spawnPos.isWithinDistance(nearestPlayer.getBlockPos(), MINIMUM_RESPAWN_DISTANCE)) && deathPos.isInRange(nearestPlayer.getPos(), MINIMUM_RESPAWN_DISTANCE)) {
                ServerWorld newWorld = nearestPlayer.getServerWorld();
                BlockPos tp;
                if(!newWorld.getDimension().hasCeiling() && newWorld.getDimension().bedWorks()) {
                    Vec3d pos2 = nearestPlayer.getPos();
                    double xShift = RandomUtils.nextDouble(0, 100);
                    double zShift = RandomUtils.nextDouble(0, 100);
                    double x = (xShift - 50);
                    double z = (zShift - 50);
                    Vec3d vec = Vec3d.ZERO.add(x, 0, z);
                    pos2 = pos2.add(vec);
                    tp = newWorld.getTopPosition(Heightmap.Type.MOTION_BLOCKING, BlockPos.ofFloored(pos2));
                    tp = newWorld.getWorldBorder().clamp(tp.getX(), tp.getY(), tp.getZ());
                }
                else{
                    tp = nearestPlayer.getBlockPos();
                }
                player.setSpawnPoint(newWorld.getRegistryKey(), tp, 0F, true, false);

            }
        }
    @Inject(at = @At("TAIL"), method = "onPlayerConnect")
    public void joinServer(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        MinecraftServer server = player.getServer();
        if (server.getPlayerManager().loadPlayerData(player) == null) {
            double lowestDist = -1;
            Vec3d pos = player.getPos();
            ServerWorld world = player.getServerWorld();
            ServerPlayerEntity nearestPlayer = null;
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
                ServerWorld newWorld = nearestPlayer.getServerWorld();
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
                player.teleport(newWorld, tp.getX(), tp.getY(), tp.getZ(), 0, 0);
            } else {
                player.setSpawnPoint(world.getRegistryKey(), world.getSpawnPos(), 0F, false, false);
            }
        }
    }
    }
