package com.juaanp.fishanywhere.client;

import com.juaanp.fishanywhere.Constants;
import com.juaanp.fishanywhere.config.CommonConfig;
import com.juaanp.fishanywhere.config.ConfigHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
    private static final int TITLE_SECTION_HEIGHT = 30;           // Sección superior para título
    private static final int OPTIONS_SECTION_HEIGHT = 100;        // Sección media para opciones
    private static final int OPTIONS_SECTION_TOP = TITLE_SECTION_HEIGHT;  
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
        
        // SECCIÓN 2: OPCIONES
        // Botón forceOpenWater con tooltip
        this.forceOpenWaterButton = CycleButton.onOffBuilder(getforceOpenWater())
                .withTooltip(value -> {
                    Component tooltip = Component.translatable(Constants.MOD_ID + ".config.forceOpenWater.tooltip");
                    return this.minecraft.font.split(tooltip, 200);
                })
                .create(this.width / 2 - 150, OPTIONS_SECTION_TOP + 30, 300, 20, 
                       FORCE_OPEN_WATER, 
                       (button, value) -> setforceOpenWater(value));
        
        // SECCIÓN 3: LISTA DE FLUIDOS
        // Calcular altura disponible para la lista de fluidos
        int fluidsListHeight = this.height - FLUIDS_SECTION_TOP - BOTTOM_BUTTON_SECTION_HEIGHT;
        
        // Inicializar lista de fluidos con altura limitada
        this.fluidsList = new FluidSelectionList(
            this.minecraft,
            fluidsListHeight
        );
        
        // SECCIÓN 4: BOTONES INFERIORES
        int buttonY = this.height - 29; // 29 píxeles desde abajo
        this.resetButton = new Button(this.width / 2 - 155, buttonY, 150, 20,
                RESET, button -> resetToDefaults());

        this.doneButton = new Button(this.width / 2 + 5, buttonY, 150, 20,
                CommonComponents.GUI_DONE, button -> onClose());

        // Agregar todos los widgets a la pantalla
        this.addRenderableWidget(this.forceOpenWaterButton);
        this.addRenderableWidget(this.resetButton);
        this.addRenderableWidget(this.doneButton);
        this.addRenderableWidget(this.fluidsList);

        initializeTrackingFields();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        // Fondo
        this.renderDirtBackground(0);
        
        // --- SECCIÓN 1: TÍTULO ---
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 10, 0xFFFFFF);
        
        // --- SECCIÓN 2: OPCIONES ---
        // Título de opciones
        drawCenteredString(poseStack, this.font, OPTIONS_TITLE, this.width / 2, OPTIONS_SECTION_TOP + 10, 0xFFFFFF);
        
        // Separador después de la sección de opciones
        fill(poseStack, 0, FLUIDS_SECTION_TOP - 5, this.width, FLUIDS_SECTION_TOP - 4, 0x66FFFFFF);
        
        // --- SECCIÓN 3: LISTA DE FLUIDOS ---
        // Título de fluidos
        drawCenteredString(poseStack, this.font, FLUIDS_TITLE, this.width / 2, FLUIDS_SECTION_TOP + 5, 0xFFFFFF);
        
        // Renderizar widgets y finalizar
        super.render(poseStack, mouseX, mouseY, partialTick);
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
        
        boolean fluidsChanged = false;
        Set<ResourceLocation> allowedFluids = CommonConfig.getInstance().getAllowedFluids();
        if (allowedFluids.size() != 1 || !allowedFluids.contains(Registry.FLUID.getKey(Fluids.WATER))) {
            fluidsChanged = true;
        }
        
        return openWaterChanged || fluidsChanged;
    }

    private void resetToDefaults() {
        // Restablecer forceOpenWater a su valor predeterminado
        boolean defaultValue = CommonConfig.getDefaultForceOpenWater();
        setforceOpenWater(defaultValue);
        this.forceOpenWaterButton.setValue(defaultValue);
        
        // Restablecer fluidos permitidos (solo agua)
        Set<ResourceLocation> defaultFluids = Set.of(Registry.FLUID.getKey(Fluids.WATER));
        CommonConfig.getInstance().setAllowedFluids(defaultFluids);
        
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
    class FluidSelectionList extends ObjectSelectionList<FluidSelectionList.FluidEntry> {
        public FluidSelectionList(Minecraft minecraft, int height) {
            super(minecraft, 
                  ModConfigScreen.this.width,  // Ancho de la lista = ancho de pantalla
                  height,                     // Altura calculada disponible
                  FLUIDS_SECTION_TOP + 20,    // Top de la lista (después del título)
                  FLUIDS_SECTION_TOP + 20 + height, // Bottom de la lista
                  FLUIDS_LIST_ITEM_HEIGHT);   // Altura de cada ítem
            
            // Configurar la lista para que sea más compacta
            this.setRenderBackground(false);
            this.setRenderTopAndBottom(false);
            
            // Obtener todos los fluidos disponibles y crear entradas en la lista
            List<Fluid> allFluids = new ArrayList<>();
            Registry.FLUID.forEach(fluid -> {
                if (fluid != Fluids.EMPTY) {
                    allFluids.add(fluid);
                }
            });
            
            // Ordenar los fluidos por nombre
            List<Fluid> sortedFluids = allFluids.stream()
                .sorted(Comparator.comparing(fluid -> 
                    Registry.FLUID.getKey(fluid).toString()))
                .collect(Collectors.toList());
            
            // Agregar entradas a la lista
            Set<ResourceLocation> allowedFluids = CommonConfig.getInstance().getAllowedFluids();
            for (Fluid fluid : sortedFluids) {
                ResourceLocation fluidId = Registry.FLUID.getKey(fluid);
                boolean isEnabled = allowedFluids.contains(fluidId);
                this.addEntry(new FluidEntry(fluid, fluidId, isEnabled));
            }
        }
        
        @Override
        public int getRowWidth() {
            return width - 40; // Margen de 20px a cada lado
        }
        
        @Override
        protected int getScrollbarPosition() {
            return width - 10; // Posición de la barra de desplazamiento
        }
        
        // Clase para cada entrada de fluido en la lista
        class FluidEntry extends ObjectSelectionList.Entry<FluidEntry> {
            private final Fluid fluid;
            private final ResourceLocation fluidId;
            private boolean enabled;
            private final Component displayName;
            private final TextureAtlasSprite fluidSprite;
            private final int iconSize = 16;
            private long lastClickTime;
            
            public FluidEntry(Fluid fluid, ResourceLocation fluidId, boolean enabled) {
                this.fluid = fluid;
                this.fluidId = fluidId;
                this.enabled = enabled;
                this.displayName = formatFluidName(fluid);
                
                // Obtener el sprite del fluido - enfoque compatible con 1.19.2
                TextureAtlasSprite sprite = null;
                
                // En 1.19.2 necesitamos usar un enfoque diferente para obtener la textura del fluido
                if (fluid == Fluids.WATER) {
                    sprite = minecraft.getTextureAtlas(BLOCK_ATLAS).apply(new ResourceLocation("minecraft", "block/water_still"));
                } 
                else if (fluid == Fluids.LAVA) {
                    sprite = minecraft.getTextureAtlas(BLOCK_ATLAS).apply(new ResourceLocation("minecraft", "block/lava_still"));
                }
                else {
                    // Para otros fluidos, intentamos inferir la textura basada en convenciones de naming
                    // Esto es una aproximación y puede no funcionar para todos los fluidos modded
                    String path = "block/" + fluidId.getPath() + "_still";
                    sprite = minecraft.getTextureAtlas(BLOCK_ATLAS).apply(new ResourceLocation(fluidId.getNamespace(), path));
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
                    this.toggleEnabled();
                    this.lastClickTime = Util.getMillis();
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }
            
            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (keyCode == 257 || keyCode == 32 || keyCode == 335) { // Enter, Space, Numpad Enter
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
            public void render(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
                int textColor = 0xFFFFFF;
                if (isHovered) {
                    // Dibujar un fondo resaltado cuando el ratón está encima
                    fill(poseStack, left, top, left + width, top + height, 0x80FFFFFF);
                    textColor = 0xFFFF00;
                }
                
                // Renderizar el icono del fluido
                if (fluidSprite != null) {
                    RenderSystem.setShaderTexture(0, BLOCK_ATLAS);
                    
                    // Usar una aproximación para el color del fluido
                    int fluidColor = getFluidColor(fluid);
                    
                    float r = ((fluidColor >> 16) & 0xFF) / 255.0F;
                    float g = ((fluidColor >> 8) & 0xFF) / 255.0F;
                    float b = (fluidColor & 0xFF) / 255.0F;
                    float a = ((fluidColor >> 24) & 0xFF) / 255.0F;
                    
                    RenderSystem.setShaderColor(r, g, b, a);
                    
                    // Dibujar el sprite
                    blit(poseStack, 
                         left + 5,                       // x
                         top + (height - iconSize) / 2,  // y
                         0,                              // blitOffset
                         iconSize,                       // width
                         iconSize,                       // height
                         this.fluidSprite);
                }
                
                // Restaurar el color por defecto
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                
                // Renderizar el nombre del fluido a la derecha del icono
                drawString(poseStack, ModConfigScreen.this.font, this.displayName, 
                           left + iconSize + 10, top + height / 2 - 4, textColor);
                
                // Indicador de estado
                String statusText = this.enabled ? "✓" : "✗";
                int statusColor = this.enabled ? 0x55FF55 : 0xFF5555;
                drawString(poseStack, ModConfigScreen.this.font, statusText, 
                           left + width - 15, top + height / 2 - 4, statusColor);
            }
            
            /**
             * Obtiene un color aproximado para el fluido
             */
            private int getFluidColor(Fluid fluid) {
                // Colores conocidos para fluidos vanilla
                if (fluid == Fluids.WATER) {
                    return 0x3F76E4FF; // Azul agua
                }
                else if (fluid == Fluids.FLOWING_WATER) {
                    return 0x3F76E4FF; // Azul agua
                }
                else if (fluid == Fluids.LAVA) {
                    return 0xFFD700FF; // Naranja/amarillo lava
                }
                else if (fluid == Fluids.FLOWING_LAVA) {
                    return 0xFFD700FF; // Naranja/amarillo lava
                }
                
                // Para otros fluidos, usar blanco
                return 0xFFFFFFFF;
            }
            
            /**
             * Formatea el nombre del fluido para mostrarlo de forma legible
             */
            private Component formatFluidName(Fluid fluid) {
                ResourceLocation id = Registry.FLUID.getKey(fluid);
                
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
                        return Component.literal(simplifiedName + " (" + id.getNamespace() + ")");
                    }
                }
                
                // 2. Formatear el ID del fluido directamente
                String path = id.getPath();
                String formattedName = formatPathName(path);
                
                return Component.literal(formattedName + " (" + id.getNamespace() + ")");
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