package de.keksuccino.fmvideo.util;

import de.keksuccino.fancymenu.menu.fancy.MenuCustomizationProperties;
import de.keksuccino.fmvideo.popup.VideoPropertiesPopup;
import de.keksuccino.fmvideo.video.VideoRenderer;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.properties.PropertiesSet;
import net.minecraft.client.gui.screen.Screen;

import java.util.List;

public class VideoUtils {

    public static boolean menuHasVideoBackground(Screen screen, VideoRenderer renderer) {
        List<PropertiesSet> layouts = MenuCustomizationProperties.getPropertiesWithIdentifier(screen.getClass().getName());
        if ((layouts != null) && !layouts.isEmpty()) {
            for (PropertiesSet s : layouts) {
                for (PropertiesSection sec : s.getPropertiesOfType("customization")) {
                    String action = sec.getEntryValue("action");
                    if ((action != null) && (action.equalsIgnoreCase("api:custombackground"))) {
                        String value = sec.getEntryValue("input_string");
                        if (value != null) {
                            PropertiesSection videoProps = readValueString(value);
                            String videoPathLink = videoProps.getEntryValue("video");
                            if (videoPathLink != null) {
                                if (renderer.getMediaPath().equals(videoPathLink)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static String buildValueString(String video, boolean isLocal, VideoPropertiesPopup.VideoProperties props) {
        String value = "";
        value += "video:" + video + ";";
        value += "islocal:" + isLocal + ";";
        if (props != null) {
            value += "loop:" + props.looping + ";";
            value += "volume:" + props.volume + ";";
        }
        return value;
    }

    public static PropertiesSection readValueString(String value) {
        PropertiesSection s = new PropertiesSection("video_properties");
        if ((value != null) && value.contains(";")) {
            for (String v : value.split("[;]")) {
                if (v.contains(":")) {
                    String name = v.split("[:]", 2)[0];
                    String content = v.split("[:]", 2)[1];
                    s.addEntry(name, content);
                }
            }
        }
        return s;
    }

}
