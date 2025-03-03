package com.juaanp.seamlesstrading.client;

import com.juaanp.seamlesstrading.config.CommonConfig;
import com.juaanp.seamlesstrading.config.ConfigHelper;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

import static net.minecraft.client.OptionInstance.createBoolean;

public class ConfigScreenBase extends OptionsSubScreen {
    private static final Component TITLE = Component.translatable("seamlesstrading.config.title");
    private static final Component RESET = Component.translatable("seamlesstrading.config.reset");
    private static final Component FISHING_CATEGORY = Component.translatable("seamlesstrading.config.category.fishing");
    private static final Component FLUIDS_CATEGORY = Component.translatable("seamlesstrading.config.category.fluids");
    private static final Component FLUID_SELECTOR_BUTTON = Component.translatable("seamlesstrading.config.fluidSelector.button");

    protected final Options options;
    protected Button resetButton;
    protected final Button doneButton = Button.builder(CommonComponents.GUI_DONE, button -> onClose()).width(Button.SMALL_WIDTH).build();

    public ConfigScreenBase(Screen lastScreen, Options options) {
        super(lastScreen, options, TITLE);
        this.options = options;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        setResetButtonState(isAnyNonDefault());
        super.render(graphics, mouseX, mouseY, partialTick);
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

    protected boolean isAnyNonDefault() {
        return getForceOpenWater() != CommonConfig.getDefaultForceOpenWater() ||
               getWaterEnabled() != CommonConfig.getDefaultWaterEnabled() ||
               getLavaEnabled() != CommonConfig.getDefaultLavaEnabled() ||
               getEmptyEnabled() != CommonConfig.getDefaultEmptyEnabled() ||
               getOtherFluidsEnabled() != CommonConfig.getDefaultOtherFluidsEnabled();
    }

    private void resetToDefaults() {
        setForceOpenWater(CommonConfig.getDefaultForceOpenWater());
        setWaterEnabled(CommonConfig.getDefaultWaterEnabled());
        setLavaEnabled(CommonConfig.getDefaultLavaEnabled());
        setEmptyEnabled(CommonConfig.getDefaultEmptyEnabled());
        setOtherFluidsEnabled(CommonConfig.getDefaultOtherFluidsEnabled());
        
        // Reset de los estados de fluidos individuales
        Map<ResourceLocation, Boolean> fluidStates = CommonConfig.getInstance().getFluidStates();
        fluidStates.clear();
        
        saveConfig();
        
        // Recargar la pantalla
        this.minecraft.setScreen(this.lastScreen);
        this.minecraft.setScreen(new ConfigScreenBase(this.lastScreen, this.options));
    }

    // Fields to track the initial values before user changes
    private Boolean lastForceOpenWater = null;
    private Boolean lastWaterEnabled = null;
    private Boolean lastLavaEnabled = null;
    private Boolean lastEmptyEnabled = null;
    private Boolean lastOtherFluidsEnabled = null;

    @Override
    protected void addOptions() {
        if (resetButton == null) {
            createResetButton();
        }

        // Initialize all tracking fields at the beginning
        initializeTrackingFields();

        // Create option instances
        OptionInstance<Boolean> forceOpenWaterToggle = createBoolean(
                "seamlesstrading.config.forceOpenWater",
                getForceOpenWater(),
                this::setForceOpenWater
        );

        OptionInstance<Boolean> waterToggle = createBoolean(
                "seamlesstrading.config.waterEnabled",
                getWaterEnabled(),
                this::setWaterEnabled
        );

        OptionInstance<Boolean> lavaToggle = createBoolean(
                "seamlesstrading.config.lavaEnabled",
                getLavaEnabled(),
                this::setLavaEnabled
        );

        OptionInstance<Boolean> emptyToggle = createBoolean(
                "seamlesstrading.config.emptyEnabled",
                getEmptyEnabled(),
                this::setEmptyEnabled
        );

        OptionInstance<Boolean> otherFluidsToggle = createBoolean(
                "seamlesstrading.config.otherFluidsEnabled",
                getOtherFluidsEnabled(),
                this::setOtherFluidsEnabled
        );

        // Create header widgets for categories
        StringWidget fishingHeader = new StringWidget(FISHING_CATEGORY, this.font);
        StringWidget fluidsHeader = new StringWidget(FLUIDS_CATEGORY, this.font);
        
        // Botón para abrir la pantalla del selector de fluidos
        Button fluidSelectorButton = Button.builder(FLUID_SELECTOR_BUTTON, (button) -> {
            // Guardar la configuración actual antes de cambiar de pantalla
            this.saveConfig();
            // Crear la pantalla de fluidos usando OptionsSubScreen
            FluidListScreen fluidScreen = new FluidListScreen(this, this.options);
            this.minecraft.setScreen(fluidScreen);
        }).build();

        // Add spacing before first category
        this.list.addSmall(new EmptyWidget(10, 8), new EmptyWidget(10, 8));

        // Add fishing category header
        this.list.addSmall(fishingHeader, null);
        this.list.addBig(forceOpenWaterToggle);

        // Add spacing between categories
        this.list.addSmall(new EmptyWidget(10, 16), new EmptyWidget(10, 16));

        // Add fluids category header
        this.list.addSmall(fluidsHeader, null);
        this.list.addBig(waterToggle);
        this.list.addBig(lavaToggle);
        this.list.addBig(emptyToggle);
        this.list.addBig(otherFluidsToggle);
        
        // Add spacing before fluid selector button
        this.list.addSmall(new EmptyWidget(10, 16), new EmptyWidget(10, 16));
        
        // Add fluid selector button
        this.list.addSmall(fluidSelectorButton, null);
    }

    private void initializeTrackingFields() {
        lastForceOpenWater = getForceOpenWater();
        lastWaterEnabled = getWaterEnabled();
        lastLavaEnabled = getLavaEnabled();
        lastEmptyEnabled = getEmptyEnabled();
        lastOtherFluidsEnabled = getOtherFluidsEnabled();
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

    protected boolean getForceOpenWater() {
        return CommonConfig.getInstance().forceOpenWater();
    }

    protected void setForceOpenWater(boolean force) {
        CommonConfig.getInstance().setForceOpenWater(force);
    }

    protected boolean getWaterEnabled() {
        return CommonConfig.getInstance().isWaterEnabled();
    }

    protected void setWaterEnabled(boolean enabled) {
        CommonConfig.getInstance().setWaterEnabled(enabled);
    }

    protected boolean getLavaEnabled() {
        return CommonConfig.getInstance().isLavaEnabled();
    }

    protected void setLavaEnabled(boolean enabled) {
        CommonConfig.getInstance().setLavaEnabled(enabled);
    }

    protected boolean getEmptyEnabled() {
        return CommonConfig.getInstance().isEmptyEnabled();
    }

    protected void setEmptyEnabled(boolean enabled) {
        CommonConfig.getInstance().setEmptyEnabled(enabled);
    }

    protected boolean getOtherFluidsEnabled() {
        return CommonConfig.getInstance().isOtherFluidsEnabled();
    }

    protected void setOtherFluidsEnabled(boolean enabled) {
        CommonConfig.getInstance().setOtherFluidsEnabled(enabled);
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