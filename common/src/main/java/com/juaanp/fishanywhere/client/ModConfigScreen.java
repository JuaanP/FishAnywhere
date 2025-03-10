package com.juaanp.fishanywhere.client;

import com.juaanp.fishanywhere.Constants;
import com.juaanp.fishanywhere.config.CommonConfig;
import com.juaanp.fishanywhere.config.ConfigHelper;
import com.juaanp.fishanywhere.util.FluidRegistryHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ModConfigScreen extends Screen {
    // Componentes de texto
    private static final Component TITLE = Component.translatable(Constants.MOD_ID + ".config.title");
    private static final Component OPTIONS_TITLE = Component.translatable(Constants.MOD_ID + ".config.optionsTitle");
    private static final Component FORCE_OPEN_WATER = Component.translatable(Constants.MOD_ID + ".config.forceOpenWater");
    private static final Component RESET = Component.translatable(Constants.MOD_ID + ".config.reset");
    private static final Component FLUIDS_TITLE = Component.translatable(Constants.MOD_ID + ".config.fluidsTitle");

    // Constantes de posición y dimensiones - DISEÑO DE TRES SECCIONES
    private static final int TITLE_SECTION_HEIGHT = 20;           // Reducido de 30 a 20
    private static final int OPTIONS_SECTION_TOP = TITLE_SECTION_HEIGHT;  
    private static final int OPTIONS_SECTION_HEIGHT = 40;         // Reducido de 60 a 40
    private static final int FLUIDS_SECTION_TOP = OPTIONS_SECTION_TOP + OPTIONS_SECTION_HEIGHT;
    private static final int BOTTOM_BUTTON_SECTION_HEIGHT = 40;   // Espacio para botones inferiores
    private static final int FLUIDS_LIST_ITEM_HEIGHT = 18;

    private static final ResourceLocation BLOCK_ATLAS = TextureAtlas.LOCATION_BLOCKS;

    // Referencias a elementos de interfaz
    protected final Screen lastScreen;
    protected Button resetButton;
    protected Button doneButton;
    protected CycleButton<Boolean> forceOpenWaterButton;
    protected FluidSelectionList fluidsList;
    private Boolean lastForceOpenWater = null;

    public ModConfigScreen(Screen lastScreen) {
        super(TITLE);
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        // SECCIÓN 1: TÍTULO (ya se renderiza automáticamente)
        
        // SECCIÓN 2: OPCIONES - Posición ajustada para estar más cerca del título
        // Calcular la posición Y del botón para estar más cerca del título
        int optionButtonY = OPTIONS_SECTION_TOP + 10; // Solo 10px desde el inicio de la sección de opciones
        
        // Botón forceOpenWater con tooltip
        this.forceOpenWaterButton = CycleButton.onOffBuilder(getforceOpenWater())
                .withTooltip(value -> {
                    Component tooltipText = Component.translatable(Constants.MOD_ID + ".config.forceOpenWater.tooltip");
                    return net.minecraft.client.gui.components.Tooltip.create(tooltipText);
                })
                .create(this.width / 2 - 150, optionButtonY, 300, 20, 
                       FORCE_OPEN_WATER, 
                       (button, value) -> setforceOpenWater(value));

        // SECCIÓN 3: LISTA DE FLUIDOS - Ajustada para respetar el footer
        // Calcular altura disponible para la lista de fluidos, dejando espacio para los botones
        int fluidsListHeight = this.height - FLUIDS_SECTION_TOP - BOTTOM_BUTTON_SECTION_HEIGHT;
        
        // Inicializar lista de fluidos con altura limitada y límite inferior ajustado
        this.fluidsList = new FluidSelectionList(
            this.minecraft,
            fluidsListHeight
        );

        // SECCIÓN 4: BOTONES INFERIORES
        int buttonY = this.height - 29; // 29 píxeles desde abajo
        this.resetButton = Button.builder(RESET, button -> resetToDefaults())
                .pos(this.width / 2 - 155, buttonY)
                .size(150, 20)
                .build();

        this.doneButton = Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .pos(this.width / 2 + 5, buttonY)
                .size(150, 20)
                .build();

        // Agregar todos los widgets a la pantalla
        this.addRenderableWidget(this.forceOpenWaterButton);
        this.addRenderableWidget(this.resetButton);
        this.addRenderableWidget(this.doneButton);
        this.addRenderableWidget(this.fluidsList);

        initializeTrackingFields();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Fondo
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        
        // --- SECCIÓN 1: TÍTULO ---
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 5, 0xFFFFFF);

        // --- SECCIÓN 2: OPCIONES ---
        // Separador después de la sección de opciones
        guiGraphics.fill(0, FLUIDS_SECTION_TOP - 5, this.width, FLUIDS_SECTION_TOP - 4, 0x66FFFFFF);

        // --- SECCIÓN 3: LISTA DE FLUIDOS ---
        // Título de fluidos
        guiGraphics.drawCenteredString(this.font, FLUIDS_TITLE, this.width / 2, FLUIDS_SECTION_TOP + 5, 0xFFFFFF);

        // Renderizar widgets y finalizar
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        setResetButtonState(isAnyNonDefault());
    }

    protected void setResetButtonState(boolean state) {
        if (resetButton != null) {
            resetButton.active = state;
        }
    }

    protected boolean isAnyNonDefault() {
        // Verificar si alguna configuración no es la predeterminada
        boolean openWaterChanged = getforceOpenWater() != CommonConfig.getDefaultForceOpenWater();

        // Consideramos que por defecto deben estar todos los fluidos habilitados
        boolean fluidsChanged = false;
        Set<ResourceLocation> allowedFluids = CommonConfig.getInstance().getAllowedFluids();
        Set<ResourceLocation> allValidFluidIds = FluidRegistryHelper.getAllFluidIds();
        
        // Si no todos los fluidos válidos están permitidos, o si hay fluidos permitidos
        // que no están en la lista de válidos, entonces consideramos que ha cambiado
        if (allowedFluids.size() != allValidFluidIds.size() || !allowedFluids.containsAll(allValidFluidIds)) {
            fluidsChanged = true;
        }

        return openWaterChanged || fluidsChanged;
    }

    private void resetToDefaults() {
        // Restablecer forceOpenWater a su valor predeterminado
        boolean defaultValue = CommonConfig.getDefaultForceOpenWater();
        setforceOpenWater(defaultValue);
        this.forceOpenWaterButton.setValue(defaultValue);

        // Restablecer fluidos permitidos a todos los disponibles
        // en lugar de hacerlo manualmente, usamos el método del CommonConfig
        CommonConfig.getInstance().resetToDefaults();
        
        // Asegurarnos de forzar la carga de todos los fluidos
        FluidRegistryHelper.forceInitialize();
        CommonConfig.getInstance().forceLoadAllFluids();

        saveConfig();

        // Recargar la pantalla para mostrar valores actualizados
        this.minecraft.setScreen(new ModConfigScreen(this.lastScreen));
    }

    private void initializeTrackingFields() {
        lastForceOpenWater = getforceOpenWater();
    }

    protected boolean getforceOpenWater() {
        return CommonConfig.getInstance().forceOpenWater();
    }

    protected void setforceOpenWater(boolean enabled) {
        CommonConfig.getInstance().setForceOpenWater(enabled);
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

    // Lista de selección de fluidos integrada
    class FluidSelectionList extends ObjectSelectionList<FluidSelectionList.AbstractFluidEntry> {
        // Nueva clase base abstracta para entradas de la lista
        abstract class AbstractFluidEntry extends ObjectSelectionList.Entry<AbstractFluidEntry> {}
        
        public FluidSelectionList(Minecraft minecraft, int height) {
            super(minecraft,
                  ModConfigScreen.this.width,
                  height,
                  FLUIDS_SECTION_TOP + 20,
                  ModConfigScreen.this.height - BOTTOM_BUTTON_SECTION_HEIGHT - 5);
            
            // Obtener fluidos agrupados por mod desde el helper
            Map<String, List<Fluid>> fluidsByMod = FluidRegistryHelper.getFluidsByMod();
            
            // Crear encabezados y entradas para cada mod
            for (Map.Entry<String, List<Fluid>> entry : fluidsByMod.entrySet()) {
                String modId = entry.getKey();
                List<Fluid> fluids = entry.getValue();
                
                // Solo mostrar encabezados y fluidos si hay fluidos disponibles
                if (!fluids.isEmpty()) {
                    // Crear un encabezado para el mod
                    this.addEntry(new ModHeaderEntry(modId));
                    
                    // Añadir cada fluido del mod como una entrada
                    for (Fluid fluid : fluids) {
                        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid);
                        boolean enabled = CommonConfig.getInstance().isFluidAllowed(fluid);
                        
                        this.addEntry(new FluidEntry(fluid, fluidId, enabled));
                    }
                }
            }
        }

        @Override
        public int getRowWidth() {
            return width - 40; // Margen de 20px a cada lado
        }
        
        @Override
        protected void renderItem(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, 
                                int index, int left, int top, int width, int height) {
            // Reducir el espacio vertical efectivo para cada elemento
            super.renderItem(guiGraphics, mouseX, mouseY, partialTick, index, left, top, width, height - 2);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // Calcular el límite inferior exacto
            int bottomLimit = ModConfigScreen.this.height - BOTTOM_BUTTON_SECTION_HEIGHT + 5;
            
            // Dibujar con recorte (scissor)
            var rect = this.getRectangle();
            guiGraphics.pose().pushPose();
            guiGraphics.enableScissor(
                rect.left(),
                rect.top(),
                rect.width(),
                bottomLimit - rect.top()
            );
            
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            
            guiGraphics.disableScissor();
            guiGraphics.pose().popPose();
            
            // Dibujar separador
            guiGraphics.fill(0, bottomLimit - 1, ModConfigScreen.this.width, bottomLimit, 0x66FFFFFF);
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput output) {
            super.updateWidgetNarration(output);
        }

        // Clase para encabezados de mod - ahora extiende AbstractFluidEntry
        class ModHeaderEntry extends AbstractFluidEntry {
            private final Component modName;
            
            public ModHeaderEntry(String modId) {
                this.modName = formatModName(modId);
            }
            
            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", this.modName);
            }
            
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                return super.mouseClicked(mouseX, mouseY, button);
            }
            
            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, 
                             int mouseX, int mouseY, boolean isHovered, float partialTick) {
                // Hacer el encabezado más compacto
                top += 1;
                height -= 2;
                
                // Ajustar el encabezado para que sea más compacto
                guiGraphics.fill(left, top, left + width, top + height, 0x66000000);
                
                // Dibujar el nombre del mod centrado y en negrita - ajustar posición vertical
                Font font = ModConfigScreen.this.font;
                int textWidth = font.width(this.modName);
                guiGraphics.drawString(font, this.modName, 
                          left + width / 2 - textWidth / 2, top + (height - 8) / 2, 0xFFFF55);
                
                // Dibujar líneas decorativas a los lados del nombre
                int lineY = top + height / 2;
                int linePadding = 10;
                int lineWidth = (width - textWidth) / 2 - linePadding * 2;
                
                if (lineWidth > 5) {
                    guiGraphics.fill(left + linePadding, lineY, left + linePadding + lineWidth, lineY + 1, 0x44FFFFFF);
                    guiGraphics.fill(left + width - linePadding - lineWidth, lineY, left + width - linePadding, lineY + 1, 0x44FFFFFF);
                }
            }
            
            /**
             * Formatea el ID del mod para mostrarlo de forma legible
             */
            private Component formatModName(String modId) {
                if ("minecraft".equals(modId)) {
                    return Component.literal("Minecraft");
                }
                
                // Convertir el modId a Title Case
                String formattedName = java.util.Arrays.stream(modId.split("_"))
                    .map(word -> word.isEmpty() ? "" : word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                    .collect(Collectors.joining(" "));
                
                return Component.literal(formattedName);
            }
        }

        // Modificar FluidEntry para extender de AbstractFluidEntry
        class FluidEntry extends AbstractFluidEntry {
            private final Fluid fluid;
            private final ResourceLocation fluidId;
            private boolean enabled;
            private final Component displayName;
            private final TextureAtlasSprite fluidSprite;
            private final int iconSize = 12; // Reducido de 16 a 12
            private long lastClickTime;

            public FluidEntry(Fluid fluid, ResourceLocation fluidId, boolean enabled) {
                this.fluid = fluid;
                this.fluidId = fluidId;
                this.enabled = enabled;
                this.displayName = formatFluidName(fluid);

                // Obtener el sprite del fluido - enfoque compatible con múltiples loaders
                TextureAtlasSprite sprite = null;
                
                // Fluidos vanilla - sabemos exactamente dónde están sus texturas
                if (fluid == Fluids.WATER) {
                    sprite = minecraft.getTextureAtlas(BLOCK_ATLAS).apply(ResourceLocation.withDefaultNamespace("block/water_still"));
                } 
                else if (fluid == Fluids.LAVA) {
                    sprite = minecraft.getTextureAtlas(BLOCK_ATLAS).apply(ResourceLocation.withDefaultNamespace("block/lava_still"));
                }
                else {
                    // Para fluidos modded - intentar múltiples convenciones de naming conocidas
                    String modId = fluidId.getNamespace();
                    String fluidName = fluidId.getPath();
                    
                    // Lista de posibles patrones para la ubicación de la textura
                    String[] possiblePaths = {
                        "block/" + fluidName + "_still",
                        "blocks/" + fluidName + "_still",
                        "fluids/" + fluidName + "_still",
                        "fluid/" + fluidName + "_still",
                        "block/fluid/" + fluidName + "_still",
                        "blocks/fluid/" + fluidName + "_still",
                        "block/" + fluidName,
                        "blocks/" + fluidName,
                        "fluids/" + fluidName,
                        "fluid/" + fluidName
                    };
                    
                    // Intentar cada patrón posible hasta encontrar uno que funcione
                    for (String path : possiblePaths) {
                        ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(modId, path);
                        TextureAtlasSprite testSprite = minecraft.getTextureAtlas(BLOCK_ATLAS).apply(textureLocation);
                        
                        // Verificar si el sprite es válido (no es el sprite por defecto)
                        if (testSprite != null && !testSprite.toString().contains("minecraft:missingno")) {
                            sprite = testSprite;
                            break;
                        }
                    }
                }

                this.fluidSprite = sprite;
                this.lastClickTime = 0L;
            }

            @Override
            public Component getNarration() {
                String status = this.enabled ? "enabled" : "disabled";
                return Component.translatable("narrator.select", this.displayName);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    // Reproducir sonido de clic de botón
                    minecraft.getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)
                    );
                    
                    this.toggleEnabled();
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (keyCode == 257 || keyCode == 32 || keyCode == 335) { // Enter, Space, Numpad Enter
                    // Reproducir sonido de clic de botón
                    minecraft.getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)
                    );
                    
                    this.toggleEnabled();
                    return true;
                } else {
                    return super.keyPressed(keyCode, scanCode, modifiers);
                }
            }

            private void toggleEnabled() {
                this.enabled = !this.enabled;
                CommonConfig.getInstance().setFluidEnabled(this.fluidId, this.enabled);
                FluidSelectionList.this.setSelected(this);
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
                // Para hacer los elementos más compactos, ajustamos visualmente
                top += 1;
                height -= 2;
                
                int textColor = 0xFFFFFF;
                if (isHovered) {
                    // Dibujar un fondo resaltado cuando el ratón está encima
                    guiGraphics.fill(left, top, left + width, top + height, 0x80FFFFFF);
                    textColor = 0xFFFF00;
                }
                
                // Renderizar el icono del fluido
                if (fluidSprite != null) {
                    RenderSystem.setShaderTexture(0, BLOCK_ATLAS);
                    
                    // Aplicar color solo para fluidos específicos que lo necesitan
                    if (fluid == Fluids.WATER) {
                        // Color azul reconocible para el agua
                        RenderSystem.setShaderColor(0.25F, 0.47F, 0.9F, 1.0F);
                    } 
                    else {
                        // Para otros fluidos, no aplicar tinte
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    }
                    
                    // Dibujar el sprite
                    guiGraphics.blit(
                         left + 5,                       // x
                         top + (height - iconSize) / 2,  // y
                         0,                              // blitOffset
                         iconSize,                       // width
                         iconSize,                       // height
                         this.fluidSprite);
                    
                    // Restaurar color por defecto después de dibujar
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                }
                
                // Renderizar el nombre del fluido a la derecha del icono - ajustar posición vertical
                guiGraphics.drawString(ModConfigScreen.this.font, this.displayName, 
                           left + iconSize + 8, top + (height - 8) / 2, textColor);
                
                // Indicador de estado - ajustar posición vertical
                String statusText = this.enabled ? "✓" : "✗";
                int statusColor = this.enabled ? 0x55FF55 : 0xFF5555;
                guiGraphics.drawString(ModConfigScreen.this.font, statusText, 
                           left + width - 12, top + (height - 8) / 2, statusColor);
            }

            /**
             * Formatea el nombre del fluido para mostrarlo de forma legible
             */
            private Component formatFluidName(Fluid fluid) {
                ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
                
                // 1. Intentar obtener el nombre del bucket si existe
                Item bucket = fluid.getBucket();
                if (bucket != null && bucket != net.minecraft.world.item.Items.AIR) {
                    // Crear un ItemStack para obtener el nombre del bucket
                    ItemStack bucketStack = new ItemStack(bucket);
                    String bucketName = bucketStack.getHoverName().getString();
                    
                    // Eliminar referencias a "bucket" o "bucket of"
                    String simplifiedName = bucketName.replaceAll("(?i)bucket\\s+of\\s+", "")
                                                     .replaceAll("(?i)\\s+bucket", "");
                    
                    if (!simplifiedName.isEmpty() && !simplifiedName.equals(bucketName)) {
                        // Ya no incluimos el namespace porque el encabezado del mod lo muestra
                        return Component.literal(simplifiedName);
                    }
                }
                
                // 2. Formatear el ID del fluido directamente
                String path = id.getPath();
                String formattedName = formatPathName(path);
                
                // Ya no incluir el namespace
                return Component.literal(formattedName);
            }

            /**
             * Convierte un path en snake_case a Title Case con espacios
             */
            private String formatPathName(String path) {
                return java.util.Arrays.stream(path.split("_"))
                    .map(word -> word.isEmpty() ? "" : word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                    .collect(Collectors.joining(" "));
            }
        }
    }
}