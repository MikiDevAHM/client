package me.eldodebug.soar.injection.mixin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.eldodebug.soar.GlideMeta;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;

import me.eldodebug.soar.injection.transformer.LwjglTransformer;
import me.eldodebug.soar.logger.GlideLogger;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class GlideTweaker implements ITweaker {

    private final List<String> launchArguments = new ArrayList<>();

    public static boolean hasOptifine = false;

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {

        this.makeProcessDpiAware();

        try {
            Class.forName("optifine.Patcher");
            hasOptifine = true;
        } catch(ClassNotFoundException e) {
            if (GlideMeta.BUILD_TYPE == GlideMeta.Type.DEV){
                GlideLogger.info("Optifine not present, continuing without Optifine");
            } else {
                GlideLogger.warn("Optifine is not present, contact us on discord if you think this is a mistake", e);
            }
        }

        this.launchArguments.addAll(args);

        if (profile != null) {
            launchArguments.add("--version");
            launchArguments.add(profile);
        }

        if (assetsDir != null) {
            launchArguments.add("--assetsDir");
            launchArguments.add(assetsDir.getAbsolutePath());
        }

        if (gameDir != null) {
            launchArguments.add("--gameDir");
            launchArguments.add(gameDir.getAbsolutePath());
        }
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {

        classLoader.registerTransformer(LwjglTransformer.class.getName());

        MixinBootstrap.init();

        MixinEnvironment env = MixinEnvironment.getDefaultEnvironment();
        Mixins.addConfiguration("mixins.soar.json");

        if (env.getObfuscationContext() == null) {
            env.setObfuscationContext("notch");
        }

        env.setSide(MixinEnvironment.Side.CLIENT);

        this.unlockLwjgl();
    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }

    @Override
    public String[] getLaunchArguments() {
        return launchArguments.toArray(new String[0]);
    }

    @SuppressWarnings("unchecked")
    private void unlockLwjgl() {
        try {
            Field transformerExceptions = LaunchClassLoader.class.getDeclaredField("classLoaderExceptions");
            transformerExceptions.setAccessible(true);
            Object o = transformerExceptions.get(Launch.classLoader);
            ((Set<String>) o).remove("org.lwjgl.");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            GlideLogger.error("Could not unlock LWJGL, this could have disastrous impacts on the ability of glide to launch", e);
        }
    }

    /**
     * Declares this process DPI-aware to Windows.
     */
    private void makeProcessDpiAware() {
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) { return; }

        try {
            boolean success = User32Ext.INSTANCE.SetProcessDPIAware();

            if (!success) { GlideLogger.warn("SetProcessDPIAware() has failed."); }
        } catch (Throwable t) {
            GlideLogger.error("Failed to set process DPI awareness", t);
        }
    }

    private interface User32Ext extends StdCallLibrary {
        User32Ext INSTANCE = Native.loadLibrary("user32", User32Ext.class);
        boolean SetProcessDPIAware();
    }
}