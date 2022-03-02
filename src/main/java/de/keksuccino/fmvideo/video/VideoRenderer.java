package de.keksuccino.fmvideo.video;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import nick1st.fancyvideo.api.MediaPlayer;
import nick1st.fancyvideo.api.MediaPlayers;

import javax.annotation.Nullable;
import java.awt.*;

public class VideoRenderer {

    protected String mediaPath;
    protected MediaPlayer player;
    protected int playerId = MediaPlayer.getNew();

    protected boolean playing = false;

    protected ResourceLocation lastFrame = null;

    public VideoRenderer(String mediaPathOrLink) {

        this.mediaPath = mediaPathOrLink;
        this.player = MediaPlayers.getPlayer(playerId);
        this.player.prepare(mediaPathOrLink);

    }

    public void render(MatrixStack matrix, int posX, int posY, int width, int height) {

        try {

            this.lastFrame = this.player.renderImage();

            if (this.lastFrame != null) {
                Minecraft.getInstance().textureManager.bindTexture(this.lastFrame);
                RenderSystem.enableBlend();
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                AbstractGui.blit(matrix, posX, posY, 0.0F, 0.0F, width, height, width, height);
                RenderSystem.disableBlend();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //TODO change to setPlaying()
    public void play() {
        if (!isPlaying()) {
            this.playing = true;
            this.player.playPrepared();
        }
    }

    //TODO change to setPlaying()
    public void pause() {
        if (isPlaying()) {
            this.playing = false;
            this.player.pause();
        }
    }

    public void stop() {
        this.playing = false;
        this.player.getTrueMediaPlayer().mediaPlayer().controls().stop();
    }

    public boolean isPlaying() {
        return this.playing;
    }

    public void setLooping(boolean b) {
        this.player.getTrueMediaPlayer().mediaPlayer().controls().setRepeat(b);
    }

    public boolean isLooping() {
        return this.player.getTrueMediaPlayer().mediaPlayer().controls().getRepeat();
    }

    /**
     * @param volume Value between 0 and 200.
     */
    public void setVolume(int volume) {
        this.player.volume(volume);
    }

    public int getVolume() {
        return this.player.getTrueMediaPlayer().mediaPlayer().audio().volume();
    }

    public void setTime(long time) {
        this.player.getTrueMediaPlayer().mediaPlayer().controls().setTime(time);
    }

    public void restart() {
        this.setTime(0L);
    }

    public ResourceLocation getLastFrame() {
        return this.lastFrame;
    }

    public boolean canPlay() {
        try {
            Dimension d = this.getVideoDimension();
            if (d != null) {
                return true;
            }
        } catch (Exception e) {}
        return false;
    }

    public String getMediaPath() {
        return this.mediaPath;
    }

    public int getPlayerId() {
        return this.playerId;
    }

    @Nullable
    public Dimension getVideoDimension() {
        return this.player.getTrueMediaPlayer().mediaPlayer().video().videoDimension();
    }

    public MediaPlayer getPlayer() {
        return this.player;
    }

    public void destroy() {
        MediaPlayers.removePlayer(this.playerId);
    }

}
