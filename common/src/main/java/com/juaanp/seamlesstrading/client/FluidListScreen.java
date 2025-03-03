package com.juaanp.seamlesstrading.client;

import com.juaanp.seamlesstrading.config.CommonConfig;
import com.juaanp.seamlesstrading.config.ConfigHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FluidListScreen extends OptionsSubScreen {
    private static final Component TITLE = Component.translatable("seamlesstrading.config.fluidSelector.title");
    private static final Component INFO_LABEL = Component.translatable("seamlesstrading.config.fluidSelector.info").withColor(0x999999);
    private static final int FOOTER_HEIGHT = 53;
    private static final ResourceLocation UNKNOWN_FLUID = ResourceLocation.withDefaultNamespace("textures/block/water_still.png");
    private static final ResourceLocation BLOCK_ATLAS = ResourceLocation.parse("textures/atlas/blocks.png");
    private FluidSelectionList fluidSelectionList;

    public FluidListScreen(Screen lastScreen, Options options) {
        super(lastScreen, options, TITLE);
        this.layout.setFooterHeight(FOOTER_HEIGHT);
    }

    @Override
    protected void addContents() {
        this.fluidSelectionList = (FluidSelectionList)this.layout.addToContents(new FluidSelectionList(this.minecraft));
    }

    @Override
    protected void addOptions() {
        // No añadimos opciones estándar
    }

    @Override
    protected void addFooter() {
        LinearLayout linearlayout = ((LinearLayout)this.layout.addToFooter(LinearLayout.vertical())).spacing(8);
        linearlayout.defaultCellSetting().alignHorizontallyCenter();
        linearlayout.addChild(new StringWidget(INFO_LABEL, this.font));
        linearlayout.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(200).build());
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();
        if (this.fluidSelectionList != null) {
            this.fluidSelectionList.updateSize(this.width, this.layout);
        }
    }

    @Override
    public void onClose() {
        ConfigHelper.save();
        super.onClose();
    }

    @Override
    public void removed() {
        ConfigHelper.save();
        super.removed();
    }

    class FluidSelectionList extends ObjectSelectionList<FluidSelectionList.FluidEntry> {
        public FluidSelectionList(Minecraft minecraft) {
            super(minecraft, FluidListScreen.this.width, FluidListScreen.this.height - 33 - FOOTER_HEIGHT, 33, 18);
            
            // Obtener todos los fluidos registrados
            List<Fluid> allFluids = new ArrayList<>();
            BuiltInRegistries.FLUID.forEach(allFluids::add);
            
            // Ordenar los fluidos por namespace y luego por path
            allFluids.sort(Comparator.<Fluid, String>comparing(
                fluid -> {
                    ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
                    return id.getNamespace();
                })
                .thenComparing(fluid -> {
                    ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
                    return id.getPath();
                })
            );
            
            // Añadir cada fluido a la lista, excepto el fluido vacío
            CommonConfig config = CommonConfig.getInstance();
            for (Fluid fluid : allFluids) {
                if (fluid != Fluids.EMPTY) {
                    boolean enabled = config.isFluidAllowed(fluid);
                    this.addEntry(new FluidEntry(fluid, enabled));
                }
            }
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        public class FluidEntry extends ObjectSelectionList.Entry<FluidEntry> {
            private final Fluid fluid;
            private boolean enabled;
            private final Component displayName;
            private final ResourceLocation fluidId;
            private long lastClickTime;
            private final TextureAtlasSprite fluidSprite;
            private final int fluidColor;

            public FluidEntry(Fluid fluid, boolean enabled) {
                this.fluid = fluid;
                this.enabled = enabled;
                this.fluidId = BuiltInRegistries.FLUID.getKey(fluid);
                this.displayName = formatFluidName(fluid);
                
                // Obtener la textura y color del fluido
                this.fluidSprite = getFluidSprite(fluid);
                this.fluidColor = getFluidColor(fluid);
            }
            
            /**
             * Intenta obtener el sprite del fluido
             */
            private TextureAtlasSprite getFluidSprite(Fluid fluid) {
                try {
                    // Intentar diferentes métodos para obtener la textura del fluido
                    
                    // 1. Intentar obtener la textura del fluido usando el patrón de nombre estándar
                    ResourceLocation fluidLoc = BuiltInRegistries.FLUID.getKey(fluid);
                    String namespace = fluidLoc.getNamespace();
                    String path = fluidLoc.getPath();
                    
                    // Intentar algunos patrones comunes para encontrar la textura del fluido
                    List<String> possiblePaths = List.of(
                        "block/" + path + "_still",
                        "block/fluid/" + path + "_still",
                        "fluid/" + path + "_still",
                        "block/" + path,
                        "fluid/" + path
                    );
                    
                    TextureAtlasSprite sprite = null;
                    for (String possiblePath : possiblePaths) {
                        try {
                            ResourceLocation textureLoc = ResourceLocation.fromNamespaceAndPath(namespace, possiblePath);
                            sprite = Minecraft.getInstance().getTextureAtlas(BLOCK_ATLAS).apply(textureLoc);
                            if (sprite != null && !sprite.contents().name().equals(Minecraft.getInstance().getTextureAtlas(BLOCK_ATLAS).apply(UNKNOWN_FLUID).contents().name())) {
                                return sprite;
                            }
                        } catch (Exception e) {
                            // Intentar con el siguiente patrón
                        }
                    }
                    
                    // Si no encontramos una textura válida, usar la de agua por defecto
                    return Minecraft.getInstance().getTextureAtlas(BLOCK_ATLAS).apply(UNKNOWN_FLUID);
                } catch (Exception e) {
                    // En caso de cualquier error, usar agua como fallback
                    return Minecraft.getInstance().getTextureAtlas(BLOCK_ATLAS).apply(UNKNOWN_FLUID);
                }
            }
            
            /**
             * Obtiene un color apropiado para el fluido
             */
            private int getFluidColor(Fluid fluid) {
                // Colores predeterminados para fluidos comunes
                if (fluid == Fluids.WATER) return 0x3F76E4;
                if (fluid == Fluids.LAVA) return 0xF05E1B;
                
                // Para otros fluidos, usar un color basado en su nombre
                String name = this.fluidId.getPath();
                if (name.contains("water")) return 0x3F76E4;
                if (name.contains("lava")) return 0xF05E1B;
                if (name.contains("oil")) return 0x3B3B3B;
                if (name.contains("milk")) return 0xFFFFFF;
                if (name.contains("honey")) return 0xD9AB27;
                if (name.contains("slime")) return 0x8AD480;
                if (name.contains("blood")) return 0xB71000;
                
                // Generar un color basado en el hash del nombre
                return name.hashCode() & 0xFFFFFF;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
                // Color del texto según el estado
                int textColor = this.enabled ? 0x55FF55 : 0xAAAAAA;
                
                // Si está siendo seleccionada, dibujar un fondo resaltado
                if (hovering) {
                    guiGraphics.fill(left, top, left + width, top + height, 0x22FFFFFF);
                }
                
                // Tamaño del icono del fluido
                int iconSize = 16;
                
                // Renderizar la textura del fluido con su color
                RenderSystem.setShaderColor(
                    ((this.fluidColor >> 16) & 0xFF) / 255.0f,  // Rojo
                    ((this.fluidColor >> 8) & 0xFF) / 255.0f,   // Verde
                    (this.fluidColor & 0xFF) / 255.0f,          // Azul
                    1.0f                                         // Alfa
                );
                
                // Dibujamos un fondo para el fluido
                guiGraphics.fill(left + 5, top + (height - iconSize) / 2, 
                                left + 5 + iconSize, top + (height - iconSize) / 2 + iconSize, 
                                0xFF000000 | this.fluidColor);
                
                // Renderizar la textura del fluido encima usando el método correcto
                if (this.fluidSprite != null) {
                    guiGraphics.blitSprite(
                        RenderType::guiTextured,
                        this.fluidSprite,
                        left + 5,                       // x
                        top + (height - iconSize) / 2,  // y
                        iconSize,                       // width
                        iconSize                        // height
                    );
                }
                
                // Restaurar el color por defecto
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                
                // Renderizar el nombre del fluido a la derecha del icono
                guiGraphics.drawString(FluidListScreen.this.font, this.displayName, 
                                     left + iconSize + 10, top + height / 2 - 4, textColor);
                
                // Indicador de estado
                String statusText = this.enabled ? "✓" : "✗";
                int statusColor = this.enabled ? 0x55FF55 : 0xFF5555;
                guiGraphics.drawString(FluidListScreen.this.font, statusText, 
                                     left + width - 15, top + height / 2 - 4, statusColor);
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (CommonInputs.selected(keyCode)) {
                    this.toggleEnabled();
                    return true;
                } else {
                    return super.keyPressed(keyCode, scanCode, modifiers);
                }
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

            private void toggleEnabled() {
                this.enabled = !this.enabled;
                CommonConfig.getInstance().setFluidEnabled(this.fluidId, this.enabled);
                FluidSelectionList.this.setSelected(this);
            }

            @Override
            public Component getNarration() {
                String status = this.enabled ? "enabled" : "disabled";
                return Component.translatable("narrator.select", this.displayName);
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