package com.dingqw.icesound;

import com.dingqw.icesound.config.IceSoundConfig;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Ice_sound implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("ice_sound");

	private static final String[] BLOCK_IDS = {"ice", "packed_ice", "blue_ice"};
	private static final String[] ACTION_IDS = {"place", "break", "step", "hit"};

	/**
	 * 每个「方块 × 音效」一个独立声音事件，这样播放时才能单独识别是哪一种声音、套用各自的音高。
	 * 下标顺序与 {@link IceSoundConfig} 里的常量一致。
	 */
	public static final SoundEvent[][] SOUNDS = new SoundEvent[IceSoundConfig.BLOCK_COUNT][IceSoundConfig.ACTION_COUNT];

	/** 原版播放这四种声音时自带的音高系数（放置/破坏 ×0.8、行走 ×1.0、挖掘 ×0.5）。 */
	private static final float[] CALL_FACTOR = {0.8F, 0.8F, 1.0F, 0.5F};

	static {
		for (int block = 0; block < IceSoundConfig.BLOCK_COUNT; block++) {
			for (int action = 0; action < IceSoundConfig.ACTION_COUNT; action++) {
				SOUNDS[block][action] = SoundEvent.of(Identifier.of("ice_sound", BLOCK_IDS[block] + "_" + ACTION_IDS[action]));
			}
		}
	}

	// 三种冰各一个声音组（pitch 固定 1.0，真正的音高由 pitchMultipliers 在播放时施加）
	private static volatile BlockSoundGroup iceGroup;
	private static volatile BlockSoundGroup packedIceGroup;
	private static volatile BlockSoundGroup blueIceGroup;

	/** 声音事件 id → 音高倍率 */
	private static volatile Map<Identifier, Float> pitchMultipliers = Map.of();

	@Override
	public void onInitializeClient() {
		for (int block = 0; block < IceSoundConfig.BLOCK_COUNT; block++) {
			for (int action = 0; action < IceSoundConfig.ACTION_COUNT; action++) {
				SoundEvent event = SOUNDS[block][action];
				Registry.register(Registries.SOUND_EVENT, event.getId(), event);
			}
		}
		IceSoundConfig.get(); // 启动时读一次配置
		rebuildPitchCache();
		LOGGER.info("冰冰冰");
	}

	/** 配置变化后调用：声音组和音高倍率都要重建。 */
	public static void invalidateAll() {
		invalidateSoundGroups();
		rebuildPitchCache();
	}

	public static void invalidateSoundGroups() {
		iceGroup = null;
		packedIceGroup = null;
		blueIceGroup = null;
	}

	/** 重新计算「事件 id → 音高倍率」：倍率 = 目标音高 / 原版调用系数。 */
	public static void rebuildPitchCache() {
		IceSoundConfig config = IceSoundConfig.get();
		Map<Identifier, Float> map = new HashMap<>();
		for (int block = 0; block < IceSoundConfig.BLOCK_COUNT; block++) {
			if (!config.isBlockEnabled(block)) {
				continue;
			}
			for (int action = 0; action < IceSoundConfig.ACTION_COUNT; action++) {
				if (!config.isActionEnabled(action)) {
					continue;
				}
				map.put(SOUNDS[block][action].getId(), config.pitch[block][action] / CALL_FACTOR[action]);
			}
		}
		pitchMultipliers = map;
	}

	/** 由 {@link com.dingqw.icesound.mixin.SoundPitchMixin} 在播放时查询。 */
	public static float pitchMultiplier(Identifier soundId) {
		Float multiplier = pitchMultipliers.get(soundId);
		return multiplier == null ? 1.0F : multiplier;
	}

	/**
	 * 返回该方块应当使用的声音组。
	 *
	 * @return null 表示不干预，交给原版音效
	 */
	public static BlockSoundGroup soundGroupFor(Block block) {
		IceSoundConfig config = IceSoundConfig.get();
		if (!config.anyActionEnabled()) {
			return null;
		}

		if (block == Blocks.ICE) {
			if (!config.ice) {
				return null;
			}
			if (iceGroup == null) {
				iceGroup = createGroup(IceSoundConfig.BLOCK_ICE);
			}
			return iceGroup;
		}
		if (block == Blocks.PACKED_ICE) {
			if (!config.packedIce) {
				return null;
			}
			if (packedIceGroup == null) {
				packedIceGroup = createGroup(IceSoundConfig.BLOCK_PACKED_ICE);
			}
			return packedIceGroup;
		}
		if (block == Blocks.BLUE_ICE) {
			if (!config.blueIce) {
				return null;
			}
			if (blueIceGroup == null) {
				blueIceGroup = createGroup(IceSoundConfig.BLOCK_BLUE_ICE);
			}
			return blueIceGroup;
		}
		return null;
	}

	/** 按配置逐槽位决定用模组音效还是原版玻璃音效；落地音始终沿用原版。 */
	private static BlockSoundGroup createGroup(int block) {
		IceSoundConfig config = IceSoundConfig.get();
		return new BlockSoundGroup(
				1.0F,
				1.0F,
				sound(block, IceSoundConfig.ACTION_BREAK, config, BlockSoundGroup.GLASS.getBreakSound()),
				sound(block, IceSoundConfig.ACTION_STEP, config, BlockSoundGroup.GLASS.getStepSound()),
				sound(block, IceSoundConfig.ACTION_PLACE, config, BlockSoundGroup.GLASS.getPlaceSound()),
				sound(block, IceSoundConfig.ACTION_HIT, config, BlockSoundGroup.GLASS.getHitSound()),
				BlockSoundGroup.GLASS.getFallSound()
		);
	}

	private static SoundEvent sound(int block, int action, IceSoundConfig config, SoundEvent vanilla) {
		return config.isActionEnabled(action) ? SOUNDS[block][action] : vanilla;
	}
}
