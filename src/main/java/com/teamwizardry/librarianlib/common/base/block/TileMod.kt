package com.teamwizardry.librarianlib.common.base.block

import com.teamwizardry.librarianlib.common.core.LibLibConfig
import com.teamwizardry.librarianlib.common.network.PacketHandler
import com.teamwizardry.librarianlib.common.network.PacketSynchronization
import com.teamwizardry.librarianlib.common.util.saving.NBTSerializationHandlers
import com.teamwizardry.librarianlib.common.util.saving.SavingFieldCache
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.fml.common.registry.GameRegistry

/**
 * @author WireSegal
 * Created at 11:06 AM on 8/4/16.
 */
abstract class TileMod : TileEntity() {

    companion object {
        @JvmStatic
        fun registerTile(clazz: Class<out TileMod>, id: String) {
            SavingFieldCache.getClassFields(clazz)
            GameRegistry.registerTileEntity(clazz, id)
        }
    }

    /**
     * Using fast synchronization is quicker and less expensive than the NBT packet default.
     * However, it requires you to register both ByteBuf serializers and NBT serializers for custom types.
     * If you for some reason can only use NBT serializers, turn this property to false.
     */
    open val useFastSync: Boolean
        get() = true

    override fun shouldRefresh(world: World, pos: BlockPos, oldState: IBlockState, newState: IBlockState): Boolean {
        return oldState.block !== newState.block
    }

    override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        writeCustomNBT(compound)
        writeAutoNBT(compound)
        super.writeToNBT(compound)

        return compound
    }

    override fun readFromNBT(compound: NBTTagCompound) {
        readCustomNBT(compound)
        readAutoNBT(compound)
        super.readFromNBT(compound)
    }

    override fun getUpdateTag(): NBTTagCompound {
        return writeToNBT(super.getUpdateTag())
    }

    override fun getUpdatePacket(): SPacketUpdateTileEntity {
        return SPacketUpdateTileEntity(pos, -999, updateTag)
    }

    open fun writeCustomNBT(cmp: NBTTagCompound) {
        // NO-OP
    }

    open fun readCustomNBT(cmp: NBTTagCompound) {
        // NO-OP
    }

    fun writeAutoNBT(cmp: NBTTagCompound) {
        if (LibLibConfig.autoSaveTEs) {
            SavingFieldCache.getClassFields(javaClass).forEach {
                val handler = NBTSerializationHandlers.getWriterUnchecked(it.value.first)
                if (handler != null) {
                    val value = it.value.second(this)
                    if (value != null) cmp.setTag(it.key, handler(value))
                }
            }
        }
    }

    fun readAutoNBT(cmp: NBTTagCompound) {
        if (LibLibConfig.autoSaveTEs) {
            SavingFieldCache.getClassFields(javaClass).forEach {
                val handler = NBTSerializationHandlers.getReaderUnchecked(it.value.first)
                if (handler != null)
                    it.value.third(this, handler(cmp.getTag(it.key)))
            }
        }
    }

    override fun markDirty() {
        super.markDirty()
        if (!worldObj.isRemote)
            dispatchTileToNearbyPlayers()
    }

    override fun onDataPacket(net: NetworkManager, packet: SPacketUpdateTileEntity) {
        super.onDataPacket(net, packet)
        readCustomNBT(packet.nbtCompound)
        readAutoNBT(packet.nbtCompound)
    }

    open fun dispatchTileToNearbyPlayers() {
        if (worldObj is WorldServer) {
            val ws: WorldServer = worldObj as WorldServer

            for (player in ws.playerEntities) {
                val playerMP = player as EntityPlayerMP
                if (playerMP.getDistanceSq(getPos()) < 64 * 64
                        && ws.playerChunkMap.isPlayerWatchingChunk(playerMP, pos.x shr 4, pos.z shr 4)) {
                    if (useFastSync)
                        PacketHandler.NETWORK.sendTo(PacketSynchronization(this), playerMP)
                    else
                        playerMP.connection.sendPacket(updatePacket)
                }
            }
        }
    }
}
