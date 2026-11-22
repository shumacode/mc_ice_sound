package com.dingqw.icesound.mixin;

import com.dingqw.icesound.Ice_sound;
import net.minecraft.client.sound.AbstractSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 客户端播放声音时会通过 AbstractSoundInstance#getPitch() 读取音高。
 * 这里对模组注册的「冰 + 动作」事件再乘一个倍率，实现每种冰的每种声音独立变调。
 */
@Mixin(AbstractSoundInstance.class)
public class SoundPitchMixin {

	@Inject(method = "getPitch", at = @At("RETURN"), cancellable = true)
	private void iceSoundPitch(CallbackInfoReturnable<Float> cir) {
		AbstractSoundInstance instance = (AbstractSoundInstance) (Object) this;
		float multiplier = Ice_sound.pitchMultiplier(instance.getId());
		if (multiplier != 1.0F) {
			cir.setReturnValue(cir.getReturnValue() * multiplier);
		}
	}
}
