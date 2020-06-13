package io.github.tivj.teemutweaks.asm.tweaker;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
@IFMLLoadingPlugin.SortingIndex(-1)
public class Tweaker implements IFMLLoadingPlugin {
    public Tweaker() {
        //this method is stolen from Patcher
        boolean lwjglUnlock = false;
        try {
            Field transformerExceptions = LaunchClassLoader.class.getDeclaredField("classLoaderExceptions");
            transformerExceptions.setAccessible(true);
            Object o = transformerExceptions.get(Launch.classLoader);
            lwjglUnlock = ((Set)o).remove("org.lwjgl.");
        } catch (IllegalAccessException | NoSuchFieldException var6) {
            var6.printStackTrace();
        }

        if (!lwjglUnlock) {
            System.out.println("Failed to unlock LWJGL");
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{ClassTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
