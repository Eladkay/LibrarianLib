package com.teamwizardry.librarianlib.common.base.block

import com.teamwizardry.librarianlib.client.core.JsonGenerationUtils
import com.teamwizardry.librarianlib.client.core.JsonGenerationUtils.getPathForBlockModel
import com.teamwizardry.librarianlib.client.core.ModelHandler
import com.teamwizardry.librarianlib.common.base.IModelGenerator
import com.teamwizardry.librarianlib.common.base.ModCreativeTab
import com.teamwizardry.librarianlib.common.base.item.IModItemProvider
import com.teamwizardry.librarianlib.common.util.VariantHelper
import com.teamwizardry.librarianlib.common.util.builders.json
import com.teamwizardry.librarianlib.common.util.currentModId
import net.minecraft.block.Block
import net.minecraft.block.BlockSlab
import net.minecraft.block.properties.IProperty
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import java.util.*


/**
 * @author WireSegal
 * Created at 9:50 AM on 1/10/17.
 */
@Suppress("LeakingThis")
open class BlockModSlab(name: String, val parent: IBlockState) : BlockSlab(parent.material), IModBlock, IModelGenerator {

    private val parentName = parent.block.registryName

    open val singleBlock: BlockModSlab = this

    protected inner class BlockDouble(name: String, parent: IBlockState) : BlockModSlab(name, parent) {
        override val singleBlock: BlockModSlab = this@BlockModSlab

        override fun isDouble() = true

        override fun generateMissingBlockstate(mapper: ((Block) -> Map<IBlockState, ModelResourceLocation>)?): Boolean {
            return false
        }

        override fun generateMissingItem(variant: String): Boolean {
            return false
        }
    }

    companion object {
        val DUMMY_PROP: PropertyEnum<Dummy> = PropertyEnum.create("block", Dummy::class.java)
    }

    val doubleBlock: BlockModSlab

    override val variants: Array<out String>

    override val bareName: String = name
    val modId: String

    val itemForm: ItemBlock? by lazy { createItemForm() }

    init {
        modId = currentModId
        this.variants = VariantHelper.beginSetupBlock(name, arrayOf())

        doubleBlock = if (!isDouble) BlockDouble(name + "_full", parent) else this

        VariantHelper.finishSetupBlock(this, name, itemForm, creativeTab)
    }

    override fun setUnlocalizedName(name: String): Block {
        super.setUnlocalizedName(name)
        VariantHelper.setUnlocalizedNameForBlock(this, modId, name, itemForm)
        return this
    }

    /**
     * Override this to have a custom ItemBlock implementation.
     */
    open fun createItemForm(): ItemBlock? {
        return if (isDouble) null else ItemModSlab(this)
    }

    /**
     * Override this to have a custom creative tab. Leave blank to have a default tab (or none if no default tab is set).
     */
    override val creativeTab: ModCreativeTab?
        get() = ModCreativeTab.defaultTabs[modId]


    override fun createBlockState()
            = if (isDouble) BlockStateContainer(this, DUMMY_PROP)
            else BlockStateContainer(this, HALF, DUMMY_PROP)

    override val ignoredProperties: Array<IProperty<*>>?
        get() = arrayOf(DUMMY_PROP)

    override fun isSideSolid(base_state: IBlockState, world: IBlockAccess, pos: BlockPos, side: EnumFacing?): Boolean {
        val state = getActualState(base_state, world, pos)
        return isDouble
                || state.getValue(BlockSlab.HALF) == EnumBlockHalf.TOP && side == EnumFacing.UP
                || state.getValue(BlockSlab.HALF) == EnumBlockHalf.BOTTOM && side == EnumFacing.DOWN
    }

    override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item? {
        return singleBlock.itemForm
    }

    override fun getStateFromMeta(meta: Int)
            = if (isDouble) defaultState
            else defaultState.withProperty(BlockSlab.HALF, if (meta == 8) EnumBlockHalf.TOP else EnumBlockHalf.BOTTOM)

    override fun getMetaFromState(state: IBlockState)
            = if (isDouble) 0
            else if (state.getValue(BlockSlab.HALF) == EnumBlockHalf.TOP) 8 else 0

    // Internal fixes for slab overriding

    override fun getTypeForItem(stack: ItemStack?) = Dummy.SLAB
    override fun isDouble() = false
    override fun getUnlocalizedName(meta: Int): String = unlocalizedName
    override fun getVariantProperty() = DUMMY_PROP

    enum class Dummy : EnumStringSerializable {
        SLAB
    }

    override fun generateMissingBlockstate(mapper: ((Block) -> Map<IBlockState, ModelResourceLocation>)?): Boolean {
        val name = ResourceLocation(parentName.resourceDomain, "blocks/${parentName.resourcePath}").toString()
        val simpleName = parentName.resourcePath

        ModelHandler.generateBlockJson(this, {
            JsonGenerationUtils.generateBlockStates(this, mapper) {
                when (it) {
                    "half=bottom" -> json { obj("model" to "${parentName}_bottom") }
                    "half=top" -> json { obj("model" to "${parentName}_top") }
                    else -> json { obj() }
                }
            }
        }, {
            mapOf(
                    getPathForBlockModel(this, "${simpleName}_bottom") to json {
                        obj(
                                "parent" to "block/half_slab",
                                "textures" to obj(
                                        "bottom" to name,
                                        "top" to name,
                                        "side" to name
                                )
                        )
                    },
                    getPathForBlockModel(this, "${simpleName}_top") to json {
                        obj(
                                "parent" to "block/upper_slab",
                                "textures" to obj(
                                        "bottom" to name,
                                        "top" to name,
                                        "side" to name
                                )
                        )
                    }
            )
        })
        return true
    }

    override fun generateMissingItem(variant: String): Boolean {
        val item = itemForm as? IModItemProvider ?: return false
        val name = ResourceLocation(parentName.resourceDomain, "block/${parentName.resourcePath}").toString()
        ModelHandler.generateItemJson(item) {
            mapOf(JsonGenerationUtils.getPathForItemModel(item as Item) to json { obj("parent" to name + "_bottom") })
        }
        return true
    }
}

