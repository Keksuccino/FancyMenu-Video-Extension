package de.keksuccino.fmvideo.customization.background;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.LayoutEditorScreen;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content.BackgroundOptionsPopup;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content.ChooseFilePopup;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.UIBase;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.popup.FMNotificationPopup;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.popup.FMPopup;
import de.keksuccino.fmvideo.customization.VideoPropertiesPopup;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.gui.content.AdvancedTextField;
import de.keksuccino.konkrete.gui.screens.popup.PopupHandler;
import de.keksuccino.konkrete.input.KeyboardData;
import de.keksuccino.konkrete.input.KeyboardHandler;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.localization.Locals;
import de.keksuccino.konkrete.math.MathUtils;
import de.keksuccino.konkrete.properties.PropertiesSection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

import java.awt.*;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

public class VideoBackgroundOptionsPopup extends FMPopup {

    protected LayoutEditorScreen handler;
    protected BackgroundOptionsPopup parent;

    protected BackgroundOptionsPopup.BackgroundOptionsSwitcher typeSwitcher;

    protected AdvancedButton doneButton;
    protected AdvancedButton cancelButton;
    protected AdvancedButton chooseVideoButton;
    protected AdvancedButton videoPropertiesButton;

    protected AdvancedTextField linkInputField;

    protected String selectedVideoPath = null;
    protected VideoPropertiesPopup.VideoProperties props;
    protected Consumer<String> callback;

    public VideoBackgroundOptionsPopup(LayoutEditorScreen handler, BackgroundOptionsPopup parent, String value, Consumer<String> callback) {

        super(240);
        this.handler = handler;
        this.parent = parent;
        this.props = new VideoPropertiesPopup.VideoProperties();
        this.callback = callback;

        KeyboardHandler.addKeyPressedListener(this::onEnterPressed);
        KeyboardHandler.addKeyPressedListener(this::onEscapePressed);

        this.typeSwitcher = new BackgroundOptionsPopup.BackgroundOptionsSwitcher(120, true);
        this.typeSwitcher.setButtonColor(UIBase.getButtonIdleColor(), UIBase.getButtonHoverColor(), UIBase.getButtonBorderIdleColor(), UIBase.getButtonBorderHoverColor(), 1);
        this.typeSwitcher.setValueBackgroundColor(UIBase.getButtonIdleColor());

        this.typeSwitcher.addValue(Locals.localize("fancymenu.fmvideo.backgroundoptions.videotype.local"));
        this.typeSwitcher.addValue(Locals.localize("fancymenu.fmvideo.backgroundoptions.videotype.online"));

        this.chooseVideoButton = new AdvancedButton(0, 0, 100, 20, Locals.localize("fancymenu.fmvideo.backgroundoptions.videotype.local.choose"), true, (press) -> {
            ChooseFilePopup p = new ChooseFilePopup((call) -> {
                if (call != null) {
                    this.selectedVideoPath = call;
                }
                PopupHandler.displayPopup(this);
            }, "mp4");
            if (this.selectedVideoPath != null) {
                p.setText(this.selectedVideoPath);
            }
            PopupHandler.displayPopup(p);
        });
        this.addButton(this.chooseVideoButton);

        this.videoPropertiesButton = new AdvancedButton(0, 0, 100, 20, Locals.localize("fancymenu.fmvideo.videoproperties"), true, (press) -> {
            VideoPropertiesPopup p = new VideoPropertiesPopup(this, props, (call) -> {
                if (call != null) {
                    this.props = call;
                }
            });
            PopupHandler.displayPopup(p);
        });
        this.addButton(this.videoPropertiesButton);

        this.doneButton = new AdvancedButton(0, 0, 100, 20, Locals.localize("popup.done"), true, (press) -> {
            this.onClose();
        });
        this.addButton(this.doneButton);

        this.cancelButton = new AdvancedButton(0, 0, 100, 20, Locals.localize("fancymenu.fmvideo.backgroundoptions.cancel"), true, (press) -> {
            PopupHandler.displayPopup(this.parent);
        });
        this.addButton(this.cancelButton);

        this.linkInputField = new AdvancedTextField(Minecraft.getInstance().fontRenderer, 0, 0, 200, 20, true, null);
        this.linkInputField.setMaxStringLength(100000);

        try {
            if (value != null) {
                PropertiesSection videoMeta = readValueString(value);
                if (videoMeta.hasEntry("video") && videoMeta.hasEntry("islocal")) {
                    if (videoMeta.getEntryValue("islocal").equals("true")) {
                        this.selectedVideoPath = videoMeta.getEntryValue("video");
                        this.typeSwitcher.setSelectedValue(Locals.localize("fancymenu.fmvideo.backgroundoptions.videotype.local"));
                    } else {
                        this.linkInputField.setText(videoMeta.getEntryValue("video"));
                        this.typeSwitcher.setSelectedValue(Locals.localize("fancymenu.fmvideo.backgroundoptions.videotype.online"));
                    }
                }
                if (videoMeta.hasEntry("volume")) {
                    String metaVolume = videoMeta.getEntryValue("volume");
                    if (MathUtils.isInteger(metaVolume)) {
                        this.props.volume = Integer.parseInt(metaVolume);
                    }
                }
                if (videoMeta.hasEntry("loop")) {
                    if (videoMeta.getEntryValue("loop").equals("true")) {
                        this.props.looping = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, Screen renderIn) {
        super.render(matrix, mouseX, mouseY, renderIn);

        int xCenter = renderIn.width / 2;
        int yCenter = renderIn.height / 2;
        String selected = this.typeSwitcher.getSelectedValue();

        this.chooseVideoButton.visible = false;

        this.typeSwitcher.render(matrix, xCenter - (this.typeSwitcher.getTotalWidth() / 2), yCenter - 85);

        if (selected.equals(Locals.localize("fancymenu.fmvideo.backgroundoptions.videotype.local"))) {

            String pathString = Locals.localize("fancymenu.fmvideo.backgroundoptions.videotype.local.novideo");
            if (this.selectedVideoPath != null) {
                String s = this.selectedVideoPath;
                if (s.length() > 30) {
                    s = ".." + s.substring(s.length() - 1 - 29);
                }
                pathString = Locals.localize("fancymenu.fmvideo.backgroundoptions.videotype.local.selected", s);
            }
            drawCenteredString(matrix, Minecraft.getInstance().fontRenderer, pathString, xCenter, yCenter - 30, -1);

            this.chooseVideoButton.visible = true;
            this.chooseVideoButton.setX(xCenter - (this.chooseVideoButton.getWidth() / 2));
            this.chooseVideoButton.setY(yCenter - 15);

        }
        if (selected.equals(Locals.localize("fancymenu.fmvideo.backgroundoptions.videotype.online"))) {

            drawCenteredString(matrix, Minecraft.getInstance().fontRenderer, Locals.localize("fancymenu.fmvideo.backgroundoptions.videotype.online.enterlink"), xCenter, yCenter - 30, -1);

            this.linkInputField.setX(xCenter - (this.linkInputField.getWidth() / 2));
            this.linkInputField.setY(yCenter -15);
            this.linkInputField.render(matrix, mouseX, mouseY, Minecraft.getInstance().getRenderPartialTicks());

        }

        this.videoPropertiesButton.setX(xCenter - (this.videoPropertiesButton.getWidth() / 2));
        this.videoPropertiesButton.setY(yCenter + 20);

        this.cancelButton.setX(xCenter - (this.cancelButton.getWidth()) - 5);
        this.cancelButton.setY(yCenter + 80);

        this.doneButton.setX(xCenter + 5);
        this.doneButton.setY(yCenter + 80);

        this.renderButtons(matrix, mouseX, mouseY);
    }

    protected String buildValueString() {
        String selected = this.typeSwitcher.getSelectedValue();
        boolean isLocal = selected.equals(Locals.localize("fancymenu.fmvideo.backgroundoptions.videotype.local"));
        String video;
        if (isLocal) {
            video = this.selectedVideoPath;
        } else {
            video = this.linkInputField.getText();
        }
        return buildValueString(video, isLocal, this.props);
    }

    public static String buildValueString(String video, boolean isLocal, VideoPropertiesPopup.VideoProperties props) {
        String value = "";
        value += "video:" + video + ";";
        value += "islocal:" + isLocal + ";";
        value += "loop:" + props.looping + ";";
        value += "volume:" + props.volume + ";";
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

    protected void onClose() {
        String selected = this.typeSwitcher.getSelectedValue();
        boolean isLocal = selected.equals(Locals.localize("fancymenu.fmvideo.backgroundoptions.videotype.local"));
        if (!isLocal) {
            if (isValidUrl(this.linkInputField.getText())) {
                this.callback.accept(this.buildValueString());
                PopupHandler.displayPopup(this.parent);
            } else {
                FMNotificationPopup p = new FMNotificationPopup(300, new Color(0, 0, 0, 0), 240, () -> {
                    PopupHandler.displayPopup(this);
                }, StringUtils.splitLines(Locals.localize("fancymenu.fmvideo.backgroundoptions.videotype.online.invalidlink"), "%n%"));
                PopupHandler.displayPopup(p);
            }
        } else {
            if (this.selectedVideoPath != null) {
                File f = new File(this.selectedVideoPath);
                if (f.isFile() && f.getPath().toLowerCase().endsWith(".mp4")) {
                    this.callback.accept(this.buildValueString());
                    PopupHandler.displayPopup(this.parent);
                } else {
                    FMNotificationPopup p = new FMNotificationPopup(300, new Color(0, 0, 0, 0), 240, () -> {
                        PopupHandler.displayPopup(this);
                    }, StringUtils.splitLines(Locals.localize("fancymenu.fmvideo.backgroundoptions.videotype.local.invalidpath"), "%n%"));
                    PopupHandler.displayPopup(p);
                }
            } else {
                FMNotificationPopup p = new FMNotificationPopup(300, new Color(0, 0, 0, 0), 240, () -> {
                    PopupHandler.displayPopup(this);
                }, StringUtils.splitLines(Locals.localize("fancymenu.fmvideo.backgroundoptions.videotype.local.invalidpath"), "%n%"));
                PopupHandler.displayPopup(p);
            }
        }
    }

    protected void onEnterPressed(KeyboardData d) {
        if (d.keycode == 257 && this.isDisplayed()) {
            this.onClose();
        }
    }

    protected void onEscapePressed(KeyboardData d) {
        if (d.keycode == 256 && this.isDisplayed()) {
            this.callback.accept(null);
            PopupHandler.displayPopup(this.parent);
        }
    }

    protected static boolean isValidUrl(String url) {
        if ((url != null) && (url.startsWith("http://") || url.startsWith("https://"))) {
            try {
                URL u = new URL(url);
                HttpURLConnection c = (HttpURLConnection)u.openConnection();
                c.addRequestProperty("User-Agent", "Mozilla/4.0");
                c.setRequestMethod("HEAD");
                int r = c.getResponseCode();
                if (r == 200) {
                    return true;
                }
            } catch (Exception e1) {
                try {
                    URL u = new URL(url);
                    HttpURLConnection c = (HttpURLConnection)u.openConnection();
                    c.addRequestProperty("User-Agent", "Mozilla/4.0");
                    int r = c.getResponseCode();
                    if (r == 200) {
                        return true;
                    }
                } catch (Exception e2) {}
            }
            return false;
        }
        return false;
    }

}
