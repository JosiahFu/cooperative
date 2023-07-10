package net.qxeii.cooperative.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.qxeii.cooperative.Cooperative;
import org.apache.commons.lang3.RandomUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

	@Shadow public abstract Entity getCameraEntity();

	@Inject(at = @At("RETURN"), method = "onDeath")
	public void onDeath(DamageSource damageSource, CallbackInfo ci) {
		double lowestDist = -1;
		ServerPlayerEntity player = (ServerPlayerEntity) this.getCameraEntity();
		Vec3d pos = player.getPos();
		ServerWorld world = player.getServerWorld();
		PlayerEntity nearestPlayer = null;
		Cooperative.LOGGER.info(String.valueOf(world.getPlayers()));
		for (ServerPlayerEntity cplayer : world.getPlayers()) {
			if (!cplayer.isDead() && cplayer.getUuid() != player.getUuid()) {
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
			double x = (xShift - 50);
			double z = (zShift - 50);
			Vec3d vec = Vec3d.ZERO.add(x, 0, z);
			pos2 = pos2.add(vec);
			BlockPos tp = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, BlockPos.ofFloored(pos2));
			player.setSpawnPoint(world.getRegistryKey(), tp, 0F, true, false);
		}
		else {
		player.setSpawnPoint(world.getRegistryKey(), world.getSpawnPos(), 0F,true, false);
		}
	}
}