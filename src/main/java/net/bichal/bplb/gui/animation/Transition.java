package net.bichal.bplb.gui.animation;

public final class Transition {
    private final float springK;
    private final float damping;
    private float current;
    private float target;
    private float velocity;
    private long lastUpdate;
    private boolean initialized = false;

    public Transition(float initial, float springK, float damping) {
        this.current = initial;
        this.target = initial;
        this.springK = springK;
        this.damping = damping;
        this.lastUpdate = System.nanoTime();
    }

    public void setTarget(float target) {
        this.target = target;
    }

    public float update() {
        long now = System.nanoTime();
        float dt = (now - lastUpdate) * 1e-9f;
        lastUpdate = now;

        if (!initialized) {
            current = target;
            velocity = 0;
            initialized = true;
            return current;
        }

        dt = Math.min(dt, 0.033f);

        float diff = current - target;
        float force = -diff * springK;
        float dampingForce = velocity * damping;
        velocity += (force - dampingForce) * dt;
        current += velocity * dt;

        return current;
    }

    public float getCurrent() {return current;}

    public boolean isAnimating() {return Math.abs(current - target) > 0.1f;}
}
