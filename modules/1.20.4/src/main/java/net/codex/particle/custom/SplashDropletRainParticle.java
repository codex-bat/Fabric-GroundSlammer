package net.codex.particle.custom;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;

public class SplashDropletRainParticle extends SpriteBillboardParticle {

    private final SpriteProvider spriteProvider;

    private float rCol = 1.0f;
    private float gCol = 1.0f;
    private float bCol = 1.0f;

    protected SplashDropletRainParticle(ClientWorld world, double x, double y, double z,
                                        double vx, double vy, double vz,
                                        SpriteProvider spriteProvider) {
        super(world, x, y, z, vx, vy, vz);
        this.spriteProvider = spriteProvider;
        this.setSprite(spriteProvider.getSprite(this.random));

        this.collidesWithWorld = true;

        this.gravityStrength = 0.35F;
        this.velocityMultiplier = 1F;

        this.scale = 0.05F;
        this.setColor(1.0F, 1.0F, 1.0F);

        this.maxAge = 200;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.onGround) {
            double landY = this.y + 0.01;

            SplashDropletLandingParticle landing = new SplashDropletLandingParticle(
                    this.world,
                    this.x, landY, this.z,
                    this.spriteProvider,
                    this.rCol, this.gCol, this.bCol
            );

            MinecraftClient.getInstance().particleManager.addParticle(landing);

            this.markDead();
            return;
        }

        if (this.y < this.world.getBottomY()) {
            this.markDead();
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleFactory<SplashDropletParticleEffect> {

        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(
                SplashDropletParticleEffect effect,
                ClientWorld world,
                double x, double y, double z,
                double vx, double vy, double vz
        ) {
            double upward = vy * effect.heightMultiplier;

            SplashDropletRainParticle particle = new SplashDropletRainParticle(
                    world,
                    x, y, z,
                    vx,
                    upward,
                    vz,
                    spriteProvider
            );

            particle.setColor(effect.red, effect.green, effect.blue);
            particle.rCol = effect.red;
            particle.gCol = effect.green;
            particle.bCol = effect.blue;

            particle.scale *= effect.amountMultiplier;
            particle.maxAge = (int) (particle.maxAge * effect.amountMultiplier);

            return particle;
        }
    }
}