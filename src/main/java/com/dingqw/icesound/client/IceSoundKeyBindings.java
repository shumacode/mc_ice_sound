package com.dingqw.icesound.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * 打开配置界面的快捷键，默认是键盘上的 [ 键，可以在原版「选项 → 按键绑定」里改。
 */
public final class IceSoundKeyBindings {
	private static final String CATEGORY = "key.categories.ice_sound";
	private static final String OPEN_CONFIG_KEY = "key.ice_sound.open_config";

	private static KeyBinding openConfig;

	private IceSoundKeyBindings() {
	}

	public static void register() {
		openConfig = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				OPEN_CONFIG_KEY,
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_LEFT_BRACKET,
				CATEGORY));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openConfig.wasPressed()) {
				openConfigScreen(client);
			}
		});
	}

	private static void openConfigScreen(MinecraftClient client) {
		// 只在游戏内（没有其它界面打开时）响应，避免和容器 / 聊天栏冲突
		if (client.currentScreen == null) {
			client.setScreen(new IceSoundConfigScreen(null));
		}
	}
}
