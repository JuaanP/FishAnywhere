package com.juaanp.seamlesstrading.client;

import com.juaanp.seamlesstrading.config.CommonConfig;
import com.juaanp.seamlesstrading.config.ConfigHelper;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ConfigScreenBase extends OptionsSubScreen {
    private static final Component TITLE = Component.translatable("seamlesstrading.config.title");
    private static final Component RESET = Component.translatable("seamlesstrading.config.reset");

    protected OptionsList list;
    protected Button resetButton;

    private Boolean lastScrollNewOffers = null;

    public ConfigScreenBase(Screen lastScreen, Options options) {
        super(lastScreen, options, TITLE);
    }

    @Override
    protected void init() {
        this.list = new OptionsList(this.minecraft, this.width, this.height - 64, this);
        
        addOptions();
        
        initializeTrackingFields();
        
        this.addRenderableWidget(this.list);
        
        super.init();
    }

    @Override
    protected void addFooter() {
        this.resetButton = Button.builder(RESET, button -> resetToDefaults())
                .width(100)
                .build();
        
        this.layout.addToFooter(resetButton);
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .width(100)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        setResetButtonState(isAnyNonDefault());
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    protected void addOptions() {
        OptionInstance<Boolean> scrollNewOffers = OptionInstance.createBoolean(
                "seamlesstrading.config.scrollNewOffers",
                getScrollNewOffers(),
                this::setScrollNewOffers
        );

        this.list.addBig(scrollNewOffers);
    }

    protected void setResetButtonState(boolean state) {
        if (resetButton != null) {
            resetButton.active = state;
        }
    }

    protected boolean isAnyNonDefault() {
        return getScrollNewOffers() != CommonConfig.getDefaultScrollNewOffers();
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