package de.keksuccino.fmvideo.customization;

import de.keksuccino.fancymenu.menu.button.ButtonCache;
import de.keksuccino.fancymenu.menu.fancy.MenuCustomization;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.LayoutEditorScreen;
import de.keksuccino.fmvideo.FmVideo;
import de.keksuccino.fmvideo.video.VideoHandler;
import de.keksuccino.fmvideo.video.VideoRenderer;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventHandler {

    protected LayoutEditorScreen lastEditorScreen = null;

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
                    for (VideoRenderer r : VideoHandler.getCachedRenderers()) {
                        r.setLooping(false);
                        if (r.isPlaying()) {
                            r.pause();
                        }
                    }
                }

            }
        }

    }

    AdvancedButton b = new AdvancedButton(20, 20, 100, 20, "TEST", true, (press) -> {
        for (VideoRenderer r : VideoHandler.getCachedRenderers()) {
            r.setLooping(false);
            r.pause();
        }
    });

    @SubscribeEvent
    public void onDrawScreenPost(GuiScreenEvent.DrawScreenEvent.Post e) {

//        b.render(e.getMatrixStack(), e.getMouseX(), e.getMouseY(), e.getRenderPartialTicks());

    }

}
