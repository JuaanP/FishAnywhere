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
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ConfigScreenBase extends Screen {
    private static final Component TITLE = Component.translatable("seamlesstrading.config.title");
    private static final Component RESET = Component.translatable("seamlesstrading.config.reset");

    protected final Screen lastScreen;
    protected final Options options;
    protected Button resetButton;
    protected Button doneButton;
    protected OptionsList list;

    public ConfigScreenBase(Screen lastScreen, Options options) {
        super(TITLE);
        this.lastScreen = lastScreen;
        this.options = options;
    }

    @Override
    protected void init() {
        this.list = new OptionsList(this.minecraft, this.width, this.height - 64, 32, this.height - 32, 25);

        this.resetButton = Button.builder(RESET, button -> resetToDefaults())
                .pos(this.width / 2 - 155, this.height - 29)
                .size(150, 20)
                .build();

        this.doneButton = Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .pos(this.width / 2 + 5, this.height - 29)
                .size(150, 20)
                .build();

        this.addRenderableWidget(this.resetButton);
        this.addRenderableWidget(this.doneButton);

        addOptions();

        this.addRenderableWidget(list);

        initializeTrackingFields();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderDirtBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.list.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 5, 16777215);
        setResetButtonState(isAnyNonDefault());
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

    private Boolean lastScrollNewOffers = null;

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
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void removed() {
        saveConfig();
        super.removed();
    }
}