package com.teamwizardry.librarianlib.foundation.registration

import com.teamwizardry.librarianlib.core.util.IncompleteBuilderException
import com.teamwizardry.librarianlib.foundation.item.IFoundationItem
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer
import net.minecraft.item.Food
import net.minecraft.item.Item
import net.minecraft.item.Rarity
import net.minecraft.tags.Tag
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.generators.ItemModelBuilder
import net.minecraftforge.client.model.generators.ItemModelProvider
import net.minecraftforge.client.model.generators.ModelFile
import net.minecraftforge.client.model.generators.ModelProvider
import net.minecraftforge.common.ToolType
import java.util.concurrent.Callable
import java.util.function.Consumer
import java.util.function.Supplier
import java.util.function.Function

/**
 * The specs for creating and registering an item.
 */
class ItemSpec(
    /**
     * The registry name, sans mod ID
     */
    val name: String
) {
    var itemProperties: Item.Properties = Item.Properties()

    /**
     * The mod ID to register this block under. This is populated by the [RegistrationManager].
     */
    var modid: String = ""
        @JvmSynthetic
        internal set

    /**
     * The registry name of the block. The [mod ID][modid] is populated by the [RegistrationManager].
     */
    val registryName: ResourceLocation
        get() = ResourceLocation(modid, name)

    /**
     * What item group this item should be in
     */
    var itemGroup: ItemGroupSpec = ItemGroupSpec.DEFAULT
        private set

    /**
     * The information used during data generation
     */
    val datagen: DataGen = DataGen()

    private var itemConstructor: Function<ItemSpec, Item> = Function { Item(it.itemProperties) }

    /**
     * Sets the item group this item should be in
     */
    fun itemGroup(value: ItemGroupSpec): ItemSpec = build {
        this.itemGroup = value
    }

    /**
     * Sets the custom [BlockItem] constructor for deferred evaluation
     */
    fun item(constructor: Function<ItemSpec, Item>): ItemSpec = build {
        this.itemConstructor = constructor
    }

    /**
     * Configures the information used for data generation
     */
    fun datagen(data: Consumer<DataGen>): ItemSpec = build {
        data.accept(this.datagen)
    }

    /**
     * Configures the information used for data generation
     */
    @JvmSynthetic
    inline fun datagen(crossinline data: DataGen.() -> Unit): ItemSpec = datagen(Consumer { it.data() })

    /**
     * Sets this item's food type
     */
    fun food(food: Food): ItemSpec = build { itemProperties.food(food) }

    /**
     * Sets the maximum stack size for this item
     */
    fun maxStackSize(maxStackSize: Int): ItemSpec = build { itemProperties.maxStackSize(maxStackSize) }

    /**
     * Sets the max damage (i.e. durability) of this item. This also implicitly sets the max stack size to 1.
     */
    fun maxDamage(maxDamage: Int): ItemSpec = build { itemProperties.maxDamage(maxDamage) }

    /**
     * Sets the container item for this item. e.g. bucket for a lava bucket, bottle for a dragon's breath, etc. This is
     * the item left behind in the crafting grid after a recipe completes.
     */
    fun containerItem(containerItem: Item): ItemSpec = build { itemProperties.containerItem(containerItem) }

    /**
     * Sets this item's rarity
     */
    fun rarity(rarity: Rarity): ItemSpec = build { itemProperties.rarity(rarity) }

    /**
     * Removes the ability to repair this item
     */
    fun setNoRepair(): ItemSpec = build { itemProperties.setNoRepair() }

    /**
     * Sets the tool level of this item
     */
    fun addToolType(type: ToolType, level: Int): ItemSpec = build { itemProperties.addToolType(type, level) }

    /**
     * Sets the [ItemStackTileEntityRenderer]. Note that [IBakedModel.isBuiltInRenderer] must return true for this to
     * be used.
     */
    fun setISTER(ister: Supplier<Callable<ItemStackTileEntityRenderer>>): ItemSpec = build { itemProperties.setISTER(ister) }

    val itemInstance: Item by lazy {
        try {
            itemConstructor.apply(this).setRegistryName(registryName)
        } catch(e: Exception) {
            throw RuntimeException("Error instantiating item $registryName", e)
        }
    }

    val lazy: LazyItem = LazyItem(this)

    /**
     * Information used when generating data
     */
    inner class DataGen {
        var model: Consumer<ItemModelProvider>? = null
        val names: MutableMap<String, String> = mutableMapOf()
        val tags: MutableList<Tag<Item>> = mutableListOf()

        /**
         * Sets the model generation function. Note: this will override [IFoundationItem.generateItemModel].
         */
        fun model(model: Consumer<ItemModelProvider>): DataGen {
            this.model = model
            return this
        }

        /**
         * Sets the model generation function. Note: this will override [IFoundationItem.generateItemModel].
         */
        @JvmSynthetic
        inline fun model(crossinline model: ItemModelProvider.() -> Unit): DataGen = model(Consumer { it.model() })

        /**
         * Sets the model generation function to create a simple model using the texture located at
         * `yourmodid:item/item_id.png`.
         */
        fun simpleModel(): DataGen = model {
            getBuilder(this@ItemSpec.name)
                .parent(ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", modLoc("${ModelProvider.ITEM_FOLDER}/${this@ItemSpec.name}"))
        }

        /**
         * Sets the name of this block in the generated en_us lang file
         */
        fun name(name: String): DataGen = name("en_us", name)

        /**
         * Sets the name of this block in the generated lang file
         */
        fun name(locale: String, name: String): DataGen {
            this.names[locale] = name
            return this
        }

        /**
         * Adds the passed tags to this block
         */
        fun tags(vararg tags: Tag<Item>): DataGen {
            this.tags.addAll(tags)
            return this
        }
    }

    private inline fun build(block: () -> Unit): ItemSpec {
        block()
        return this
    }
}