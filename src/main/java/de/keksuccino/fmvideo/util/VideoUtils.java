package de.keksuccino.fmvideo.util;

import de.keksuccino.fancymenu.menu.fancy.MenuCustomizationProperties;
import de.keksuccino.fmvideo.popup.VideoPropertiesPopup;
import de.keksuccino.fmvideo.video.VideoHandler;
import de.keksuccino.fmvideo.video.VideoRenderer;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.properties.PropertiesSet;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

public class VideoUtils {

    public static List<VideoRenderer> getRenderersOfMenu(Screen screen) {
        List<VideoRenderer> l = new ArrayList<>();
        try {
            List<PropertiesSet> layouts = MenuCustomizationProperties.getPropertiesWithIdentifier(screen.getClass().getName());
            if ((layouts != null) && !layouts.isEmpty()) {
                for (PropertiesSet s : layouts) {
                    for (PropertiesSection sec : s.getPropertiesOfType("customization")) {
                        String action = sec.getEntryValue("action");
                        if (action != null) {
                            if (action.equalsIgnoreCase("api:custombackground")) {
                                String typeId = sec.getEntryValue("type_identifier");
                                if ((typeId != null) && typeId.equalsIgnoreCase("fancymenu_extension:video_background")) {
                                    String value = sec.getEntryValue("input_string");
                                    if (value != null) {
                                        PropertiesSection videoProps = readValueString(value);
                                        String videoPathLink = videoProps.getEntryValue("video");
                                        if (videoPathLink != null) {
                                            l.add(VideoHandler.getRenderer(videoPathLink));
                                        }
                                    }
                                }
                            }
                            if (action.equalsIgnoreCase("custom_layout_element:fancymenu_extension:video_item")) {
                                String videoPathLink = sec.getEntryValue("video");
                                if (videoPathLink != null) {
                                    l.add(VideoHandler.getRenderer(videoPathLink));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return l;
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
