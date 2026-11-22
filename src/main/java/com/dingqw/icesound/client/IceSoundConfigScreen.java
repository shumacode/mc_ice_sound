package com.dingqw.icesound.client;

import com.dingqw.icesound.Ice_sound;
import com.dingqw.icesound.config.IceSoundConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 模组配置界面：
 * <ul>
 *     <li>最上面「全局音效」是一键开关，等于同时切换下面四种音效</li>
 *     <li>中间是四种音效类型：放置 / 破坏 / 行走 / 挖掘</li>
 *     <li>再下面是三种方块：冰 / 浮冰 / 蓝冰</li>
 *     <li>最下面是 4 列（音效）× 3 行（方块）的音高滑块</li>
 * </ul>
 */
public class IceSoundConfigScreen extends Screen {
	private static final String[] ACTION_LABELS = {"放置", "破坏", "行走", "挖掘"};
	private static final String[] BLOCK_LABELS = {"冰", "浮冰", "蓝冰"};
	private static final int GAP = 4;

	private final Screen parent;
	private final IceSoundConfig snapshot = IceSoundConfig.get().copy();

	private ButtonWidget masterButton;
	private final ButtonWidget[] actionButtons = new ButtonWidget[IceSoundConfig.ACTION_COUNT];
	private final ButtonWidget[] blockButtons = new ButtonWidget[IceSoundConfig.BLOCK_COUNT];
	private final PitchSlider[][] sliders = new PitchSlider[IceSoundConfig.BLOCK_COUNT][IceSoundConfig.ACTION_COUNT];

	private int labelX;
	private int gridLeft;
	private int sliderWidth;
	private int headerY;
	private int sliderTop;

	public IceSoundConfigScreen(Screen parent) {
		super(Text.literal("冰块音效配置"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		IceSoundConfig config = IceSoundConfig.get();
		int centerY = this.height / 2;

		// ---- 全局音效 ----
		int masterWidth = Math.min(260, this.width - 40);
		this.masterButton = this.addDrawableChild(toggle((this.width - masterWidth) / 2, centerY - 96, masterWidth, 20,
				"全局音效", config::allActionsEnabled, value -> {
					config.setAllActions(value);
					Ice_sound.invalidateAll();
				}));

		// ---- 四种音效类型 ----
		int actionWidth = Math.min(130, (this.width - 40 - GAP * 3) / 4);
		int actionTotal = actionWidth * 4 + GAP * 3;
		int actionLeft = (this.width - actionTotal) / 2;
		for (int action = 0; action < IceSoundConfig.ACTION_COUNT; action++) {
			int index = action;
			this.actionButtons[action] = this.addDrawableChild(toggle(
					actionLeft + action * (actionWidth + GAP), centerY - 68, actionWidth, 20,
					ACTION_LABELS[action],
					() -> config.isActionEnabled(index),
					value -> {
						config.setAction(index, value);
						Ice_sound.invalidateAll();
					}));
		}

		// ---- 三种方块 ----
		int blockWidth = Math.min(130, (this.width - 40 - GAP * 2) / 3);
		int blockTotal = blockWidth * 3 + GAP * 2;
		int blockLeft = (this.width - blockTotal) / 2;
		for (int block = 0; block < IceSoundConfig.BLOCK_COUNT; block++) {
			int index = block;
			this.blockButtons[block] = this.addDrawableChild(toggle(
					blockLeft + block * (blockWidth + GAP), centerY - 42, blockWidth, 20,
					BLOCK_LABELS[block],
					() -> config.isBlockEnabled(index),
					value -> {
						config.setBlock(index, value);
						Ice_sound.invalidateAll();
					}));
		}

		// ---- 音高滑块：4 列（音效）× 3 行（方块） ----
		this.sliderWidth = Math.max(72, Math.min(150, (this.width - 34 - 24 - GAP * 3) / 4));
		int gridWidth = this.sliderWidth * 4 + GAP * 3;
		int totalWidth = 34 + gridWidth;
		int left = (this.width - totalWidth) / 2;
		this.labelX = left;
		this.gridLeft = left + 34;
		this.headerY = centerY - 18;
		this.sliderTop = centerY - 4;

		for (int block = 0; block < IceSoundConfig.BLOCK_COUNT; block++) {
			for (int action = 0; action < IceSoundConfig.ACTION_COUNT; action++) {
				this.sliders[block][action] = this.addDrawableChild(new PitchSlider(
						this.gridLeft + action * (this.sliderWidth + GAP),
						this.sliderTop + block * 22,
						this.sliderWidth, 20, block, action));
			}
		}

		// ---- 完成 / 取消 ----
		int halfWidth = Math.min(150, (this.width - 40 - GAP) / 2);
		int buttonTotal = halfWidth * 2 + GAP;
		int buttonLeft = (this.width - buttonTotal) / 2;
		int bottom = this.height - 40;

		this.addDrawableChild(ButtonWidget.builder(Text.literal("完成"), button -> {
			config.save();
			this.close();
		}).dimensions(buttonLeft, bottom, halfWidth, 20).build());

		this.addDrawableChild(ButtonWidget.builder(Text.literal("取消"), button -> {
			IceSoundConfig.get().copyFrom(this.snapshot);
			Ice_sound.invalidateAll();
			this.close();
		}).dimensions(buttonLeft + halfWidth + GAP, bottom, halfWidth, 20).build());

		this.refresh();
	}

	/** 让全局开关显示跟随四种音效的实际状态，并把当前不起作用的按钮/滑块置灰。 */
	private void refresh() {
		IceSoundConfig config = IceSoundConfig.get();

		if (this.masterButton != null) {
			this.masterButton.setMessage(labelText("全局音效", config.allActionsEnabled()));
		}
		for (int action = 0; action < IceSoundConfig.ACTION_COUNT; action++) {
			if (this.actionButtons[action] != null) {
				this.actionButtons[action].setMessage(labelText(ACTION_LABELS[action], config.isActionEnabled(action)));
			}
		}

		boolean actionsOn = config.anyActionEnabled();
		for (int block = 0; block < IceSoundConfig.BLOCK_COUNT; block++) {
			if (this.blockButtons[block] != null) {
				this.blockButtons[block].active = actionsOn;
			}
			for (int action = 0; action < IceSoundConfig.ACTION_COUNT; action++) {
				if (this.sliders[block][action] != null) {
					this.sliders[block][action].active = actionsOn
							&& config.isBlockEnabled(block)
							&& config.isActionEnabled(action);
				}
			}
		}
	}

	private ButtonWidget toggle(int x, int y, int width, int height, String label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
		return ButtonWidget.builder(labelText(label, getter.get()), button -> {
			setter.accept(!getter.get());
			button.setMessage(labelText(label, getter.get()));
			this.refresh();
		}).dimensions(x, y, width, height).build();
	}

	private static Text labelText(String label, boolean enabled) {
		return Text.literal(label + "：" + (enabled ? "开" : "关"));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);

		for (int action = 0; action < IceSoundConfig.ACTION_COUNT; action++) {
			int columnCenter = this.gridLeft + action * (this.sliderWidth + GAP) + this.sliderWidth / 2;
			context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(ACTION_LABELS[action]), columnCenter, this.headerY, 0xA0A0A0);
		}
		for (int block = 0; block < IceSoundConfig.BLOCK_COUNT; block++) {
			context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(BLOCK_LABELS[block]),
					this.labelX + 17, this.sliderTop + block * 22 + 6, 0xA0A0A0);
		}
	}

	@Override
	public void close() {
		MinecraftClient client = this.client;
		if (client != null) {
			client.setScreen(this.parent);
		}
	}

	/** 单个「方块 × 音效」的音高滑块。 */
	private static final class PitchSlider extends SliderWidget {
		private final int block;
		private final int action;
		private final float min = IceSoundConfig.MIN_PITCH;
		private final float max = IceSoundConfig.MAX_PITCH;
		private boolean ready;

		PitchSlider(int x, int y, int width, int height, int block, int action) {
			super(x, y, width, height, Text.empty(),
					progress(IceSoundConfig.get().pitch[block][action]));
			this.block = block;
			this.action = action;
			this.ready = true;
			this.updateMessage();
		}

		private static double progress(float value) {
			double raw = (value - IceSoundConfig.MIN_PITCH) / (double) (IceSoundConfig.MAX_PITCH - IceSoundConfig.MIN_PITCH);
			return Math.max(0.0, Math.min(1.0, raw));
		}

		private float currentValue() {
			return this.min + (this.max - this.min) * (float) this.value;
		}

		@Override
		protected void updateMessage() {
			if (!this.ready) {
				return; // 父类构造过程中可能调用，此时字段还没赋值
			}
			this.setMessage(Text.literal(String.format(Locale.ROOT, "%.2f", this.currentValue())));
		}

		@Override
		protected void applyValue() {
			if (!this.ready) {
				return;
			}
			IceSoundConfig.get().pitch[this.block][this.action] = this.currentValue();
			Ice_sound.invalidateAll();
		}
	}
}
