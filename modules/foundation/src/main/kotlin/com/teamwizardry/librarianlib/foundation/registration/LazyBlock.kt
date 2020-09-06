package com.teamwizardry.librarianlib.foundation.registration

import net.minecraft.block.Block
import net.minecraft.item.Item
import java.lang.IllegalStateException
import kotlin.reflect.KProperty

/**
 * A lazy access to a block instance and its item (if it has one). The result of adding a [BlockSpec] to a registration
 * manager. Instances can be created with a block directly if needed.
 */
public class LazyBlock private constructor(private var blockInstance: (() -> Block)?, private var itemInstance: (() -> Item?)?) {
    public constructor(spec: BlockSpec): this(spec::blockInstance, spec::itemInstance)
    public constructor(blockInstance: Block): this(blockInstance, null)
    public constructor(blockInstance: Block, itemInstance: Item?): this({ blockInstance }, { itemInstance })

    /**
     * Creates an empty block which can be configured later using [from]. This is useful for creating final fields which
     * can be populated later.
     */
    public constructor(): this(null, null)

    /**
     * Copies the other LazyBlock into this LazyBlock. This is useful for creating final fields which can be populated
     * later.
     */
    public fun from(other: LazyBlock) {
        blockInstance = other.blockInstance
        itemInstance = other.itemInstance
    }

    /**
     * Get the block instance
     */
    public fun get(): Block {
        return (blockInstance ?: throw IllegalStateException("LazyBlock not initialized"))()
    }

    /**
     * Get the item instance, if it exists
     */
    public fun getItem(): Item? {
        return (itemInstance ?: throw IllegalStateException("LazyBlock not initialized"))()
    }

    @JvmSynthetic
    public operator fun getValue(thisRef: Any?, property: KProperty<*>): Block {
        return get()
    }
}