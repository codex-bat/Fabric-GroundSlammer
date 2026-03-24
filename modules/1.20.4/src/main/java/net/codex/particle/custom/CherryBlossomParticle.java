package net.codex.particle.custom;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleType;

public class CherryBlossomParticle extends SpriteBillboardParticle {

    private final SpriteProvider spriteProvider;
    private final double spawnX, spawnY, spawnZ;
    private final double baseAngle, orbitRadius, verticalRadius, spinMultiplier;
    private final float initialRotation, rotationAmount;

    protected CherryBlossomParticle(ClientWorld world, double x, double y, double z,
                                    double vx, double vy, double vz,
                                    SpriteProvider spriteProvider,
                                    CherryBlossomParticleEffect effect) {
        super(world, x, y, z, vx, vy, vz);
        this.spriteProvider = spriteProvider;

        this.spawnX = x;
        this.spawnY = y;
        this.spawnZ = z;

        // Cherry‑specific: slightly smaller and shorter lived
        this.scale = 0.08f + random.nextFloat() * 0.12f;   // smaller than leaves
        this.maxAge = 30 + random.nextInt(25);             // shorter lifetime

        this.setBoundingBoxSpacing(0.04f, 0.04f);

        this.baseAngle = random.nextDouble() * Math.PI * 2.0;
        this.orbitRadius = 0.12 + random.nextDouble() * 0.8;   // tighter orbit
        this.verticalRadius = 0.25 + random.nextDouble() * 0.5; // less vertical drift
        this.spinMultiplier = 0.0002 + random.nextDouble() * 0.00006;
        this.initialRotation = random.nextFloat() * 0.35f;
        this.rotationAmount = (random.nextFloat() - 0.5f) * 0.6f; // more rotation variation

        this.alpha = 1.0f;
        this.gravityStrength = 0.0f;

        // Pick a random sprite from the provider (cherry1-4)
        this.setSprite(spriteProvider);
    }

    // Copy the easing methods from LeafParticle
    private static double easeInOutSine(double t) {
        return -(Math.cos(Math.PI * t) - 1.0) / 2.0;
    }

    private static double easeOutQuad(double t) {
        return 1 - (1 - t) * (1 - t);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.age >= this.maxAge) {
            this.markDead();
            return;
        }

        double t = (double) this.age / (double) this.maxAge;
        double eased = easeInOutSine(t);
        double radialEase = easeOutQuad(t);

        this.prevAngle = this.angle;
        this.angle = initialRotation + (float) (rotationAmount * eased);

        double orbitalAngle = baseAngle + spinMultiplier * 2.0 * Math.PI * eased;
        double quatreMod = 1.0 + 0.18 * Math.sin(4.0 * Math.PI * eased + baseAngle);
        double currentRadius = orbitRadius * (0.25 + 0.75 * radialEase) * quatreMod;

        double nx = spawnX + Math.cos(orbitalAngle) * currentRadius;
        double nz = spawnZ + Math.sin(orbitalAngle) * currentRadius;

        double ny = spawnY + Math.sin(t * Math.PI) * verticalRadius * 0.95;
        ny += 0.03 * easeOutQuad(t);

        this.setPos(nx, ny, nz);

        double fadeStart = 0.65;
        if (t < fadeStart) {
            this.alpha = 1.0f;
        } else {
            double fadeT = (t - fadeStart) / (1.0 - fadeStart);
            this.alpha = (float) (1.0 - easeInOutSine(fadeT));
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Factory implements ParticleFactory<CherryBlossomParticleEffect> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(CherryBlossomParticleEffect effect,
                                       ClientWorld world,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new CherryBlossomParticle(world, x, y, z, vx, vy, vz, spriteProvider, effect);
        }
    }
}