package com.juaanp.seamlesstrading.client;

import com.juaanp.seamlesstrading.config.CommonConfig;
import com.juaanp.seamlesstrading.config.ConfigHelper;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ConfigScreenBase extends OptionsSubScreen {
    private static final Component TITLE = Component.translatable("seamlesstrading.config.title");
    private static final Component RESET = Component.translatable("seamlesstrading.config.reset");

    protected final Options options;
    protected Button resetButton;
    protected final Button doneButton = Button.builder(CommonComponents.GUI_DONE, button -> onClose()).width(Button.SMALL_WIDTH).build();

    private Boolean lastScrollNewOffers = null;

    public ConfigScreenBase(Screen lastScreen, Options options) {
        super(lastScreen, options, TITLE);
        this.options = options;
    }

    @Override
    protected void addFooter() {
        LinearLayout linearLayout = layout.addToFooter(LinearLayout.horizontal().spacing(8));
        if (resetButton != null) {
            linearLayout.addChild(resetButton);
            linearLayout.addChild(doneButton);
        } else {
            super.addFooter();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        setResetButtonState(isAnyNonDefault());
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void addOptions() {
        if (resetButton == null) {
            createResetButton();
        }

        initializeTrackingFields();

        OptionInstance<Boolean> scrollNewOffers = OptionInstance.createBoolean(
                "seamlesstrading.config.scrollNewOffers",
                getScrollNewOffers(),
                this::setScrollNewOffers
        );

        this.list.addBig(scrollNewOffers);
    }

    protected boolean isAnyNonDefault() {
        return getScrollNewOffers() != CommonConfig.getDefaultScrollNewOffers();
    }

    private void createResetButton() {
        resetButton = Button.builder(RESET, button -> resetToDefaults())
                .width(Button.SMALL_WIDTH)
                .build();
    }

    protected void setResetButtonState(boolean state) {
        if (resetButton != null) {
            resetButton.active = state;
        }
    }

    private void resetToDefaults() {
        setScrollNewOffers(CommonConfig.getDefaultScrollNewOffers());
        saveConfig();

        this.minecraft.setScreen(this.lastScreen);
        this.minecraft.setScreen(new ConfigScreenBase(this.lastScreen, this.options));
    }

    private void initializeTrackingFields() {
        lastScrollNewOffers = getScrollNewOffers();
    }

    private static class EmptyWidget extends AbstractWidget {
        public EmptyWidget(int width, int height) {
            super(0, 0, width, height, Component.empty());
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
    }

    protected boolean getScrollNewOffers() {
        return CommonConfig.getInstance().isScrollNewOffers();
    }

    protected void setScrollNewOffers(boolean enabled) {
        CommonConfig.getInstance().setScrollNewOffers(enabled);
    }

    protected void saveConfig() {
        ConfigHelper.save();
    }

    @Override
    public void onClose() {
        saveConfig();
        super.onClose();
    }

    @Override
    public void removed() {
        saveConfig();
        super.removed();
    }
}