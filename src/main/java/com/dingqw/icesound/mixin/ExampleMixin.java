package com.dingqw.icesound.mixin;

import com.dingqw.icesound.Ice_sound;
import net.minecraft.block.AbstractBlock;
import net.minecraft.sound.BlockSoundGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 方块状态取声音组的入口。放置、破坏、踩踏、挖掘、落地音效都会调用 AbstractBlockState#getSoundGroup()，
 * 在这里按配置把冰 / 浮冰 / 蓝冰换成模组音效（配置全关时返回原版）。
 */
@Mixin(AbstractBlock.AbstractBlockState.class)
public class ExampleMixin {

	@Inject(method = "getSoundGroup", at = @At("HEAD"), cancellable = true)
	private void changeIceSound(CallbackInfoReturnable<BlockSoundGroup> cir) {
		AbstractBlock.AbstractBlockState state = (AbstractBlock.AbstractBlockState) (Object) this;

		BlockSoundGroup group = Ice_sound.soundGroupFor(state.getBlock());
		if (group != null) {
			cir.setReturnValue(group);
		}
	}
}
