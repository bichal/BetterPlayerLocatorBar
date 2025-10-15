package net.bichal.bplb.client.render;

public class TextureAnimator {
    private long lastFrameTime;
    private int currentFrame;
    private final int firstTime;
    private final int secondTime;

    public TextureAnimator(int firstTime, int secondTime) {
        this.lastFrameTime = System.currentTimeMillis();
        this.currentFrame = 0;
        this.firstTime = firstTime;
        this.secondTime = secondTime;
    }

    public int getCurrentFrame() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastFrameTime;

        int currentFrameTime = (currentFrame == 0) ? firstTime : secondTime;

        if (elapsed >= currentFrameTime * 50L) {
            lastFrameTime = now;
            currentFrame = (currentFrame + 1) % 2;
        }

        return currentFrame;
    }

    public void reset() {
        currentFrame = 0;
        lastFrameTime = System.currentTimeMillis();
    }
}
