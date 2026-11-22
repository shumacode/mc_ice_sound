package com.dingqw.icesound.config;

import com.dingqw.icesound.Ice_sound;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 模组配置：
 * <ul>
 *     <li>四种音效类型开关：放置 / 破坏 / 行走 / 挖掘</li>
 *     <li>三种方块开关：冰 / 浮冰 / 蓝冰</li>
 *     <li>每个「方块 × 音效」的最终音高</li>
 * </ul>
 * 「全局音效」不是独立字段，而是四种音效全开时的状态，点它等于一键把四种音效同时开或关。
 */
public final class IceSoundConfig {
	public static final int BLOCK_ICE = 0;
	public static final int BLOCK_PACKED_ICE = 1;
	public static final int BLOCK_BLUE_ICE = 2;
	public static final int BLOCK_COUNT = 3;

	public static final int ACTION_PLACE = 0;
	public static final int ACTION_BREAK = 1;
	public static final int ACTION_STEP = 2;
	public static final int ACTION_HIT = 3;
	public static final int ACTION_COUNT = 4;

	/** 滑块可调范围。 */
	public static final float MIN_PITCH = 0.5F;
	public static final float MAX_PITCH = 1.5F;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static IceSoundConfig instance;

	// 音效类型开关
	public boolean place = true;
	public boolean breakSound = true;
	public boolean step = true;
	public boolean hit = true;

	// 方块开关
	public boolean ice = true;
	public boolean packedIce = true;
	public boolean blueIce = true;

	/** 最终音高，下标 [方块][音效]，顺序见上面的常量。 */
	public float[][] pitch = defaultPitch();

	public static float[][] defaultPitch() {
		return new float[][] {
				// 放置   破坏   行走   挖掘
				{1.00F, 0.85F, 1.20F, 1.00F}, // 冰
				{0.90F, 0.77F, 1.08F, 0.90F}, // 浮冰
				{0.80F, 0.68F, 0.96F, 0.80F}  // 蓝冰
		};
	}

	public static IceSoundConfig get() {
		if (instance == null) {
			instance = read();
		}
		return instance;
	}

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("ice_sound.json");
	}

	private static IceSoundConfig read() {
		Path path = path();
		if (!Files.isRegularFile(path)) {
			return new IceSoundConfig();
		}
		try {
			IceSoundConfig loaded = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), IceSoundConfig.class);
			if (loaded == null) {
				return new IceSoundConfig();
			}
			loaded.pitch = normalizePitch(loaded.pitch);
			return loaded;
		} catch (Exception e) {
			Ice_sound.LOGGER.warn("[ice_sound] 读取配置失败，使用默认值", e);
			return new IceSoundConfig();
		}
	}

	/** 旧配置或手改坏掉的配置容错：补齐缺失项并限制在可调范围内。 */
	private static float[][] normalizePitch(float[][] raw) {
		float[][] defaults = defaultPitch();
		float[][] result = new float[BLOCK_COUNT][ACTION_COUNT];
		for (int block = 0; block < BLOCK_COUNT; block++) {
			for (int action = 0; action < ACTION_COUNT; action++) {
				float value = defaults[block][action];
				if (raw != null && raw.length > block && raw[block] != null && raw[block].length > action) {
					float candidate = raw[block][action];
					if (!Float.isNaN(candidate) && !Float.isInfinite(candidate)) {
						value = candidate;
					}
				}
				result[block][action] = Math.max(MIN_PITCH, Math.min(MAX_PITCH, value));
			}
		}
		return result;
	}

	public void save() {
		try {
			Path path = path();
			Files.createDirectories(path.getParent());
			Files.writeString(path, GSON.toJson(this), StandardCharsets.UTF_8);
			Ice_sound.invalidateAll();
		} catch (IOException e) {
			Ice_sound.LOGGER.warn("[ice_sound] 保存配置失败", e);
		}
	}

	public boolean isActionEnabled(int action) {
		return switch (action) {
			case ACTION_PLACE -> this.place;
			case ACTION_BREAK -> this.breakSound;
			case ACTION_STEP -> this.step;
			case ACTION_HIT -> this.hit;
			default -> false;
		};
	}

	public void setAction(int action, boolean enabled) {
		switch (action) {
			case ACTION_PLACE -> this.place = enabled;
			case ACTION_BREAK -> this.breakSound = enabled;
			case ACTION_STEP -> this.step = enabled;
			case ACTION_HIT -> this.hit = enabled;
			default -> {
			}
		}
	}

	public boolean isBlockEnabled(int block) {
		return switch (block) {
			case BLOCK_ICE -> this.ice;
			case BLOCK_PACKED_ICE -> this.packedIce;
			case BLOCK_BLUE_ICE -> this.blueIce;
			default -> false;
		};
	}

	public void setBlock(int block, boolean enabled) {
		switch (block) {
			case BLOCK_ICE -> this.ice = enabled;
			case BLOCK_PACKED_ICE -> this.packedIce = enabled;
			case BLOCK_BLUE_ICE -> this.blueIce = enabled;
			default -> {
			}
		}
	}

	/** 四种音效是否全开（全局开关的显示状态）。 */
	public boolean allActionsEnabled() {
		return this.place && this.breakSound && this.step && this.hit;
	}

	/** 是否至少有一种音效在用模组音效。 */
	public boolean anyActionEnabled() {
		return this.place || this.breakSound || this.step || this.hit;
	}

	/** 一键设置四种音效（全局开关的行为）。 */
	public void setAllActions(boolean enabled) {
		this.place = enabled;
		this.breakSound = enabled;
		this.step = enabled;
		this.hit = enabled;
	}

	public IceSoundConfig copy() {
		IceSoundConfig copy = new IceSoundConfig();
		copy.copyFrom(this);
		return copy;
	}

	public void copyFrom(IceSoundConfig other) {
		this.place = other.place;
		this.breakSound = other.breakSound;
		this.step = other.step;
		this.hit = other.hit;
		this.ice = other.ice;
		this.packedIce = other.packedIce;
		this.blueIce = other.blueIce;

		float[][] copy = new float[BLOCK_COUNT][ACTION_COUNT];
		for (int block = 0; block < BLOCK_COUNT; block++) {
			System.arraycopy(other.pitch[block], 0, copy[block], 0, ACTION_COUNT);
		}
		this.pitch = copy;
	}
}
