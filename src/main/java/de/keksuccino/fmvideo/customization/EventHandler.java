package de.keksuccino.fmvideo.customization;

import de.keksuccino.fancymenu.menu.button.ButtonCache;
import de.keksuccino.fancymenu.menu.fancy.MenuCustomization;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.LayoutEditorScreen;
import de.keksuccino.fmvideo.FmVideo;
import de.keksuccino.fmvideo.customization.background.VideoBackground;
import de.keksuccino.fmvideo.util.VideoUtils;
import de.keksuccino.fmvideo.video.VideoHandler;
import de.keksuccino.fmvideo.video.VideoRenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventHandler {

    protected LayoutEditorScreen lastEditorScreen = null;
    protected boolean stoppedInWorld = false;

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onScreenInitPre(GuiScreenEvent.InitGuiEvent.Pre e) {

        if (!ButtonCache.isCaching()) {
            if (MenuCustomization.isValidScreen(e.getGui())) {

                boolean newEditorInstance = false;
                if (e.getGui() instanceof LayoutEditorScreen) {
                    if ((lastEditorScreen == null) || (lastEditorScreen != e.getGui())) {
                        newEditorInstance = true;
                    }
                    lastEditorScreen = (LayoutEditorScreen) e.getGui();
                }

                if (MenuCustomization.isNewMenu() || newEditorInstance) {
                    //TODO remove debug
                    FmVideo.LOGGER.info("############### IS NEW MENU!");
                    boolean hasVideoBack = false;
                    for (VideoRenderer r : VideoHandler.getCachedRenderers()) {
                        r.setLooping(false);
                        if ((e.getGui() instanceof LayoutEditorScreen) || !VideoUtils.menuHasVideoBackground(e.getGui(), r)) {
                            //TODO remove debug
                            FmVideo.LOGGER.info("######################## STOPPING UNUSED VIDEO BACK: " + r.getMediaPath());
                            if (r.isPlaying()) {
                                r.pause();
                            }
                        } else if (!(e.getGui() instanceof LayoutEditorScreen)) {
                            //TODO remove debug
                            FmVideo.LOGGER.info("######################## MENU HAS VIDEO BACK: " + r.getMediaPath());
                            hasVideoBack = true;
                        }
                    }
                    //Reset time of all video backgrounds in menus without video background
                    if (!hasVideoBack) {
                        VideoBackground.lastRenderer = null;
                        for (VideoRenderer r : VideoHandler.getCachedRenderers()) {
                            r.setTime(0L);
                        }
                    }
                }

            }
        }

    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        //Stop and reset video backgrounds when in a world (when no screen is active)
        if (Minecraft.getInstance().currentScreen == null) {
            if (!this.stoppedInWorld) {
                MenuCustomization.setIsNewMenu(true);
                VideoBackground.lastRenderer = null;
                for (VideoRenderer r : VideoHandler.getCachedRenderers()) {
                    r.setLooping(false);
                    r.setTime(0L);
                    r.pause();
                }
            }
            this.stoppedInWorld = true;
        } else {
            this.stoppedInWorld = false;
        }
    }

//    AdvancedButton b = new AdvancedButton(20, 20, 100, 20, "TEST", true, (press) -> {
//        for (VideoRenderer r : VideoHandler.getCachedRenderers()) {
//            r.setLooping(false);
//            r.pause();
//        }
//    });
//
//    @SubscribeEvent
//    public void onDrawScreenPost(GuiScreenEvent.DrawScreenEvent.Post e) {
//
//        b.render(e.getMatrixStack(), e.getMouseX(), e.getMouseY(), e.getRenderPartialTicks());
//
//    }

}
