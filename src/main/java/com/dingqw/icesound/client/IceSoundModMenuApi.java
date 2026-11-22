package com.dingqw.icesound.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * 把配置界面挂到 Mod Menu 的「配置」按钮上。Mod Menu 没装时这个类不会被加载。
 */
public class IceSoundModMenuApi implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return IceSoundConfigScreen::new;
	}
}
