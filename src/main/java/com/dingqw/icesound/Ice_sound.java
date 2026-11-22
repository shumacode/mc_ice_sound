package com.dingqw.icesound;

import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ice_sound implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("ice_sound");

	// 放置、破坏、行走、挖掘四个事件共用同一个 ice_ice_ice.ogg，各自的变调写在 sounds.json 里。
	public static final SoundEvent ICE_PLACE = SoundEvent.of(Identifier.of("ice_sound", "ice_place"));
	public static final SoundEvent ICE_BREAK = SoundEvent.of(Identifier.of("ice_sound", "ice_break"));
	public static final SoundEvent ICE_STEP = SoundEvent.of(Identifier.of("ice_sound", "ice_step"));
	public static final SoundEvent ICE_HIT = SoundEvent.of(Identifier.of("ice_sound", "ice_hit"));

	// 三种冰各用一个声音组：组里的 pitch 就是该方块的整体音高倍率（后续会再乘上各动作的固定系数）。
	public static final BlockSoundGroup ICE_SOUND_GROUP = createGroup(1.0F);
	public static final BlockSoundGroup PACKED_ICE_SOUND_GROUP = createGroup(0.9F);
	public static final BlockSoundGroup BLUE_ICE_SOUND_GROUP = createGroup(0.8F);

	private static BlockSoundGroup createGroup(float pitch) {
		return new BlockSoundGroup(
				1.0F,      // volume
				pitch,     // pitch
				ICE_BREAK, // breakSound
				ICE_STEP,  // stepSound
				ICE_PLACE, // placeSound
				ICE_HIT,   // hitSound
				BlockSoundGroup.GLASS.getFallSound() // fallSound：不替换，沿用原版冰块（玻璃声音组）的落地音
		);
	}

	@Override
	public void onInitialize() {
		Registry.register(Registries.SOUND_EVENT, ICE_PLACE.getId(), ICE_PLACE);
		Registry.register(Registries.SOUND_EVENT, ICE_BREAK.getId(), ICE_BREAK);
		Registry.register(Registries.SOUND_EVENT, ICE_STEP.getId(), ICE_STEP);
		Registry.register(Registries.SOUND_EVENT, ICE_HIT.getId(), ICE_HIT);
		LOGGER.info("冰冰冰");
	}
}
