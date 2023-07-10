package net.qxeii.cooperative.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.RandomUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
	@Inject(at = @At("HEAD"), method = "findRespawnPosition")
	static void setRespawnPos(ServerWorld world, BlockPos pos, float angle, boolean forced, boolean alive, CallbackInfoReturnable<Optional<Vec3d>> cir) {
		double lowestDist = -1;
		PlayerEntity nearestPlayer = null;
		for (ServerPlayerEntity player : world.getPlayers()) {
			if (player.isAlive()) {
				double dist = player.squaredDistanceTo(Vec3d.of(pos));
				if (dist < lowestDist || lowestDist == -1) {
					lowestDist = dist;
					nearestPlayer = player;
				}
			}
		}
		if(nearestPlayer != null) {
			Vec3d pos2 = nearestPlayer.getPos();
			double x = RandomUtils.nextDouble(-50,50);
			double y = RandomUtils.nextDouble(-50,50);
			double z = RandomUtils.nextDouble(-50,50);
			Vec3d vec = Vec3d.ZERO.add(x,y,z);
			pos2 = pos2.add(vec);
			pos = BlockPos.ofFloored(pos2);
		}


	}
}