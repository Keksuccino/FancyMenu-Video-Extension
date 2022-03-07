package de.keksuccino.fmvideo;

import java.io.File;

import de.keksuccino.fancymenu.api.background.MenuBackgroundTypeRegistry;
import de.keksuccino.fancymenu.api.buttonaction.ButtonActionRegistry;
import de.keksuccino.fancymenu.api.item.CustomizationItemRegistry;
import de.keksuccino.fancymenu.api.placeholder.PlaceholderTextRegistry;
import de.keksuccino.fmvideo.customization.EventHandler;
import de.keksuccino.fmvideo.customization.background.VideoBackgroundType;
import de.keksuccino.fmvideo.customization.buttonaction.LowerVideoVolumeButtonAction;
import de.keksuccino.fmvideo.customization.buttonaction.UpperVideoVolumeButtonAction;
import de.keksuccino.fmvideo.customization.item.VideoCustomizationItemContainer;
import de.keksuccino.fmvideo.customization.placeholder.VideoVolumePlaceholder;
import de.keksuccino.fmvideo.video.VideoVolumeHandler;
import de.keksuccino.konkrete.localization.Locals;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.tuple.Pair;
import de.keksuccino.konkrete.Konkrete;
import de.keksuccino.konkrete.config.Config;
import de.keksuccino.konkrete.config.exceptions.InvalidValueException;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("fmextension_video")
public class FmVideo {

    public static final String VERSION = "1.0.0";

    public static final Logger LOGGER = LogManager.getLogger();

    public static final File MOD_DIR = new File("config/fancymenu/extensions/fmvideo");

    public static Config config;

    public FmVideo() {
        try {

            ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));

            //Check if mod was loaded client- or server-side
            if (FMLEnvironment.dist == Dist.CLIENT) {

                if (!MOD_DIR.exists()) {
                    MOD_DIR.mkdirs();
                }

                updateConfig();

                VideoVolumeHandler.init();

                //Register video background type
                MenuBackgroundTypeRegistry.registerBackgroundType(new VideoBackgroundType());

                //Register video item container
                CustomizationItemRegistry.registerItem(new VideoCustomizationItemContainer());

                //Register button actions
                ButtonActionRegistry.registerButtonAction(new LowerVideoVolumeButtonAction());
                ButtonActionRegistry.registerButtonAction(new UpperVideoVolumeButtonAction());

                //Register placeholders
                PlaceholderTextRegistry.registerPlaceholder(new VideoVolumePlaceholder());

                Konkrete.addPostLoadingEvent("fmvideo", this::onClientSetup);

                MinecraftForge.EVENT_BUS.register(new EventHandler());

            } else {
                System.out.println("## WARNING ## 'FancyMenu Video Extension' is a client mod and has no effect when loaded on a server!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @SubscribeEvent
//    public void onRegisterCommands(RegisterCommandsEvent e) {
//
//        OpenGuiScreenCommand.register(e.getDispatcher());
//
//    }

    private void onClientSetup() {
        try {

            initLocals();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void initLocals() {
        String baseDir = "locals/";
        File f = new File(MOD_DIR.getPath() + "/locals");
        if (!f.exists()) {
            f.mkdirs();
        }

        Locals.copyLocalsFileToDir(new ResourceLocation("fmvideo", baseDir + "en_us.local"), "en_us", f.getPath());
//        Locals.copyLocalsFileToDir(new ResourceLocation("fmvideo", baseDir + "de_de.local"), "de_de", f.getPath());

        Locals.getLocalsFromDir(f.getPath());
    }

    public static void updateConfig() {
        try {

            config = new Config(MOD_DIR.getPath() + "/config.cfg");

            config.registerValue("dummy_value", true, "general");

            config.syncConfig();

            config.clearUnusedValues();

        } catch (InvalidValueException e) {
            e.printStackTrace();
        }
    }

}
