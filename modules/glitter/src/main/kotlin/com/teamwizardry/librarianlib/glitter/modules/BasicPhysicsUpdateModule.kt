package com.teamwizardry.librarianlib.glitter.modules

import com.teamwizardry.librarianlib.glitter.*
import com.teamwizardry.librarianlib.glitter.bindings.ConstantBinding
import java.lang.Math.abs

/**
 * A basic implementation of physics interaction between particles and the world.
 *
 * This module handles essentially the entire simulation of simple particles. It updates the previous position binding,
 * computes collisions and their effects on velocity, and advances the position to reflect those collisions and
 * velocity changes.
 *
 * The collision boxes are not updated every tick, as retrieving them is among the most costly operations the collider
 * does. A refresh may be manually requested by calling [GlitterWorldCollider.clearCache]
 *
 * The process of updating each tick proceeds as follows:
 *
 * 1. Store the current position in the previous position
 * 2. Dampen the velocity
 * 3. Apply gravitational acceleration
 * 4. Collide with the world
 *     1. Ray-trace the collision point
 *     2. Advance the position to the point of collision or the end of the velocity vector if there was no collision
 *     3. Return if no collision occurred
 *     4. Reflect velocity along the collision normal axis
 *     5. Apply friction along the two axes perpendicular to the normal
 * 5. If the particle just hit something, collide again to try to use up the rest of the velocity.
 *
 * Item 5 ensures that the downward velocity added to particles resting on the ground—and the resulting 0-distance
 * collision with the ground—doesn't lock them in place. In the second iteration that downward velocity will have
 * been zeroed or negated by the first collision, allowing the particle to slide or bounce as would be expected.
 *
 * Perfect bounciness is, as far as I am aware, infeasible. Discrete time steps don't mix well with linear motion:
 * http://jsfiddle.net/thecodewarrior/7a8snb49
 */
public class BasicPhysicsUpdateModule @JvmOverloads constructor(
    /**
     * The position of the particle.
     */
    @JvmField public val position: ReadWriteParticleBinding,
    /**
     * The previous position of the particle. Populated so renderers can properly position the particle between ticks.
     */
    @JvmField public val previousPosition: WriteParticleBinding,
    /**
     * The velocity of the particle. If the binding is a [ReadWriteParticleBinding] the final velocity will be
     * stored in this binding.
     *
     * Velocity is set, not added.
     */
    @JvmField public val velocity: ReadParticleBinding,
    /**
     * If enabled, will allow the particle to collide with blocks in the world
     */
    @JvmField public val enableCollision: Boolean = false,
    /**
     * The acceleration of gravity. Positive gravity imparts a downward acceleration on the particle.
     *
     * Subtracted from particle's speed every tick.
     *
     * 0.04 is a good number to start with.
     */
    @JvmField public val gravity: ReadParticleBinding = ConstantBinding(0.04),
    /**
     * The fraction of velocity conserved upon impact. A bounciness of 0 means the particle will completely stop
     * when impacting a surface. 1.0 means the particle will bounce back with all of the velocity it had impacting,
     * essentially negating the collision axis. However, due to inaccuracies as yet unidentified, a bounciness of
     * 1.0 doesn't result in 100% velocity preservation.
     *
     * Only useful if enableCollision is set to true.
     *
     * 0.2 is a good number to start with.
     */
    @JvmField public val bounciness: ReadParticleBinding = ConstantBinding(0.2),
    /**
     * The friction of the particle upon impact. Every time the particle impacts (or rests upon) a block, the two
     * axes perpendicular to the side being hit will be reduced by this fraction. Friction of 0 would mean perfectly
     * slippery, no velocity lost when rubbing against an object
     * Multiplies particle speed.
     *
     * Friction sliding against blocks. Only useful if enableCollision is set to true.
     *
     * 0.2 is a good number to start with.
     */
    @JvmField public val friction: ReadParticleBinding = ConstantBinding(0.2),
    /**
     * The damping, or "drag" of the particle. Every tick the velocity will be reduced by this fraction. Setting
     * the damping to 0.01 means that the particle will reach 10% velocity in just over 10 seconds
     * (0.99^229 ≈ 0.1, log.99(0.1) ≈ 229).
     * Multiplies particle speed.
     *
     * Friction in the air basically.
     *
     * 0.01 is a good number to start with.
     */
    @JvmField public val damping: ReadParticleBinding = ConstantBinding(0.01)
): ParticleUpdateModule {
    init {
        position.require(3)
        previousPosition.require(3)
        velocity.require(3)
        gravity.require(1)
        bounciness.require(1)
        friction.require(1)
        damping.require(1)
    }

    private var posX: Double = 0.0
    private var posY: Double = 0.0
    private var posZ: Double = 0.0
    private var velX: Double = 0.0
    private var velY: Double = 0.0
    private var velZ: Double = 0.0
    private val rayHit = RayHitResult()

    override fun update(particle: DoubleArray) {
        position.load(particle)
        posX = position.contents[0]
        posY = position.contents[1]
        posZ = position.contents[2]

        velocity.load(particle)
        velX = velocity.contents[0]
        velY = velocity.contents[1]
        velZ = velocity.contents[2]

        // (1. in class docs)
        previousPosition.contents[0] = posX
        previousPosition.contents[1] = posY
        previousPosition.contents[2] = posZ
        previousPosition.store(particle)

        gravity.load(particle)
        bounciness.load(particle)
        friction.load(particle)
        damping.load(particle)

        // (2. in class docs)
        dampen()

        // (3. in class docs)
        accelerate()

        if (enableCollision) {
            // (4. in class docs)
            collide()

            // (5. in class docs)
            if (rayHit.collisionFraction < 1.0) {
                collide(velocityMultiplier = 1 - rayHit.collisionFraction)
            }
        } else {
            posX += velX
            posY += velY
            posZ += velZ
        }

        position.contents[0] = posX
        position.contents[1] = posY
        position.contents[2] = posZ
        position.store(particle)

        velocity.contents[0] = velX
        velocity.contents[1] = velY
        velocity.contents[2] = velZ
        if (velocity is WriteParticleBinding) {
            velocity.store(particle)
        }
    }

    private fun dampen() {
        val damping = this.damping.contents[0]
        velX *= 1 - damping
        velY *= 1 - damping
        velZ *= 1 - damping
    }

    private fun accelerate() {
        val gravity = this.gravity.contents[0]
        velY -= gravity
    }

    private fun collide(velocityMultiplier: Double = 1.0) {
        // (4.1 in class docs)
        GlitterWorldCollider.collide(rayHit,
            posX, posY, posZ,
            velX * velocityMultiplier, velY * velocityMultiplier, velZ * velocityMultiplier
        )

        // (4.2 in class docs)
        posX += velX * rayHit.collisionFraction * velocityMultiplier
        posY += velY * rayHit.collisionFraction * velocityMultiplier
        posZ += velZ * rayHit.collisionFraction * velocityMultiplier

        // (4.3 in class docs)
        if (rayHit.collisionFraction >= 1.0) {
            return
        }

        val axisX = abs(rayHit.collisionNormalX)
        val axisY = abs(rayHit.collisionNormalY)
        val axisZ = abs(rayHit.collisionNormalZ)

        val bounciness = this.bounciness.contents[0]
        val friction = this.friction.contents[0]

        // (4.4 in class docs)
        velX *= 1 - axisX * (1.0 + bounciness)
        velY *= 1 - axisY * (1.0 + bounciness)
        velZ *= 1 - axisZ * (1.0 + bounciness)

        // (4.5 in class docs)
        velX *= 1 - (1 - axisX) * friction
        velY *= 1 - (1 - axisY) * friction
        velZ *= 1 - (1 - axisZ) * friction
    }
}