package com.teamwizardry.librarianlib.glitter.testmod.entity

import com.teamwizardry.librarianlib.core.util.sided.SidedRunnable
import com.teamwizardry.librarianlib.glitter.testmod.init.TestEntities
import com.teamwizardry.librarianlib.glitter.testmod.systems.ParticleSystems
import net.minecraft.entity.Entity
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.IPacket
import net.minecraft.world.World
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.datasync.EntityDataManager
import net.minecraftforge.fml.network.NetworkHooks

class ParticleSpawnerEntity(world: World): Entity(TestEntities.spawner, world) {
    var system: String
        get() = this.dataManager[SYSTEM]
        set(value) {
            this.dataManager[SYSTEM] = value
        }

    init {
        canUpdate(true)
    }

    override fun createSpawnPacket(): IPacket<*> {
        return NetworkHooks.getEntitySpawningPacket(this)
    }

    override fun readAdditional(compound: CompoundNBT) {
        system = compound.getString("System")
    }

    override fun writeAdditional(compound: CompoundNBT) {
        compound.putString("System", system)
    }

    override fun registerData() {
        dataManager.register(SYSTEM, "")
    }

    override fun canBeCollidedWith(): Boolean {
        return true
    }

    override fun tick() {
        super.tick()
        if(world.isRemote) {
            SidedRunnable.client {
                ParticleSystems.spawn(system, this)
            }
        }
    }

    override fun hitByEntity(entity: Entity): Boolean {
        if(entity is PlayerEntity) {
            this.remove()
            return true
        }
        return false
    }

    companion object {
        val SYSTEM = EntityDataManager.createKey(ParticleSpawnerEntity::class.java, DataSerializers.STRING)
    }

}