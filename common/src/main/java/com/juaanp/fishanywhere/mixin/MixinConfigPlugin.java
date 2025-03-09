package com.juaanp.fishanywhere.mixin;

import com.juaanp.fishanywhere.Constants;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinConfigPlugin implements IMixinConfigPlugin {
    private static final String FORGE_MARKER_CLASS = "net.minecraftforge.fml.common.Mod";
    private static final boolean IS_FORGE = isClassPresent(FORGE_MARKER_CLASS);
    
    // Es entorno de desarrollo
    private static final boolean IS_DEV_ENV = isDevEnvironment();

    private static boolean isClassPresent(String className) {
        try {
            Class.forName(className, false, MixinConfigPlugin.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    private static boolean isDevEnvironment() {
        // Comprobar algunas condiciones comunes para entornos de desarrollo
        return System.getProperty("fishanywhere.development") != null || 
               System.getProperty("forge.logging.markers", "").contains("SCAN") ||
               System.getProperty("fabric.development") != null;
    }

    @Override
    public void onLoad(String mixinPackage) {
        Constants.LOG.info("Loading mixins for " + mixinPackage + " (Forge: " + IS_FORGE + ", Dev: " + IS_DEV_ENV + ")");
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // Para Forge, evitamos nuestro mixin común y usamos el específico de Forge
        if (IS_FORGE && mixinClassName.equals("com.juaanp." + Constants.MOD_ID + ".mixin.FishingHookMixin")) {
            Constants.LOG.debug("Skipping common mixin " + mixinClassName + " in Forge environment");
            return false;
        }
        
        // En entornos de desarrollo, podemos ser más permisivos si hay errores
        if (IS_DEV_ENV) {
            Constants.LOG.debug("Applying mixin " + mixinClassName + " to " + targetClassName + " in development environment");
        }
        
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
} 