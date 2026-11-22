package com.dingqw.icesound.mixin;

import com.dingqw.icesound.Ice_sound;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.sound.BlockSoundGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 方块状态取声音组的入口。放置、破坏、踩踏、挖掘、落地音效都会调用 AbstractBlockState#getSoundGroup()，
 * 在这里把冰 / 浮冰 / 蓝冰换成各自的声音组（冰最接近原调，浮冰略低，蓝冰最低）。
 */
@Mixin(AbstractBlock.AbstractBlockState.class)
public class ExampleMixin {

	@Inject(method = "getSoundGroup", at = @At("HEAD"), cancellable = true)
	private void changeIceSound(CallbackInfoReturnable<BlockSoundGroup> cir) {
		AbstractBlock.AbstractBlockState state = (AbstractBlock.AbstractBlockState) (Object) this;

		BlockSoundGroup group;
		if (state.isOf(Blocks.ICE)) {
			group = Ice_sound.ICE_SOUND_GROUP;
		} else if (state.isOf(Blocks.PACKED_ICE)) {
			group = Ice_sound.PACKED_ICE_SOUND_GROUP;
		} else if (state.isOf(Blocks.BLUE_ICE)) {
			group = Ice_sound.BLUE_ICE_SOUND_GROUP;
		} else {
			return;
		}

		cir.setReturnValue(group);
	}
}
