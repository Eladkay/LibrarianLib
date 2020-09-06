package com.teamwizardry.librarianlib.foundation

import com.teamwizardry.librarianlib.core.util.MiscUtil
import com.teamwizardry.librarianlib.foundation.registration.RegistrationManager
import net.alexwells.kottle.FMLKotlinModLoadingContext
import net.minecraft.block.Block
import net.minecraft.entity.EntityType
import net.minecraft.fluid.Fluid
import net.minecraft.inventory.container.ContainerType
import net.minecraft.item.Item
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.SoundEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.registries.IForgeRegistry

/**
 *
 * Order of events:
 * - constructor
 * - [createRegistries]
 * - Forge registries
 * - mod registries
 * - [commonSetup]
 * - [clientSetup]/[dedicatedServerSetup]
 * - [interModCommsEnqueue]
 * - [interModCommsProcess]
 *
 * @constructor
 * @param kottleContext Pass a value of true if your mod uses Kottle's kotlin language provider
 */
@Suppress("LeakingThis")
public abstract class BaseMod @JvmOverloads constructor(private val kottleContext: Boolean = false){
    public val modid: String = MiscUtil.getModId(this.javaClass)
    public val registrationManager: RegistrationManager

    init {
        val eventBus = modLoadingContextEventBus()
        registrationManager = RegistrationManager(modid, eventBus)
        eventBus.register(this)
        MinecraftForge.EVENT_BUS.register(this)
    }

    /**
     * Create any custom registries here using the [RegistryBuilder][net.minecraftforge.registries.RegistryBuilder]
     */
    protected open fun createRegistries() {}
    protected open fun registerBlocks(registry: IForgeRegistry<Block>) {}
    protected open fun registerItems(registry: IForgeRegistry<Item>) {}
    protected open fun registerTileEntities(registry: IForgeRegistry<TileEntityType<*>>) {}
    protected open fun registerEntities(registry: IForgeRegistry<EntityType<*>>) {}
    protected open fun registerFluids(registry: IForgeRegistry<Fluid>) {}
    protected open fun registerContainers(registry: IForgeRegistry<ContainerType<*>>) {}
    protected open fun registerSounds(registry: IForgeRegistry<SoundEvent>) {}

    /**
     * Called in parallel after all the registries have been populated
     */
    protected open fun commonSetup(e: FMLCommonSetupEvent) { }

    /**
     * Called in parallel after [commonSetup] to do client-specific setup
     */
    protected open fun clientSetup(e: FMLClientSetupEvent) { }

    /**
     * Called in parallel after [commonSetup] to do dedicated server-specific setup
     */
    protected open fun dedicatedServerSetup(e: FMLDedicatedServerSetupEvent) { }

    /**
     * Send events over inter-mod comms
     */
    protected open fun interModCommsEnqueue(e: InterModEnqueueEvent) { }

    /**
     * Process events received over inter-mod comms
     */
    protected open fun interModCommsProcess(e: InterModProcessEvent) { }

    /**
     * Gets the mod loading event bus. Override this if your mod uses a non-standard mod loader. Mods using the Kottle
     * mod language provider should pass true to the `kottleContext` constructor parameter instead of overriding this
     * method.
     */
    protected open fun modLoadingContextEventBus(): IEventBus {
        return if(kottleContext) {
            FMLKotlinModLoadingContext.get().modEventBus
        } else {
            FMLJavaModLoadingContext.get().modEventBus
        }
    }

    //region Internal implementation
    @SubscribeEvent
    @JvmSynthetic
    internal fun baseCreateRegistries(e: RegistryEvent.NewRegistry) {
        createRegistries()
    }

    @SubscribeEvent
    @JvmSynthetic
    internal fun baseRegisterBlocks(e: RegistryEvent.Register<Block>) {
        registerBlocks(e.registry)
    }

    @SubscribeEvent
    @JvmSynthetic
    internal fun baseRegisterItems(e: RegistryEvent.Register<Item>) {
        registerItems(e.registry)
    }

    @SubscribeEvent
    @JvmSynthetic
    internal fun baseRegisterTileEntities(e: RegistryEvent.Register<TileEntityType<*>>) {
        registerTileEntities(e.registry)
    }

    @SubscribeEvent
    @JvmSynthetic
    internal fun baseRegisterEntities(e: RegistryEvent.Register<EntityType<*>>) {
        registerEntities(e.registry)
    }

    @SubscribeEvent
    @JvmSynthetic
    internal fun baseRegisterFluids(e: RegistryEvent.Register<Fluid>) {
        registerFluids(e.registry)
    }

    @SubscribeEvent
    @JvmSynthetic
    internal fun baseRegisterContainers(e: RegistryEvent.Register<ContainerType<*>>) {
        registerContainers(e.registry)
    }

    @SubscribeEvent
    @JvmSynthetic
    internal fun baseRegisterSounds(e: RegistryEvent.Register<SoundEvent>) {
        registerSounds(e.registry)
    }

    @SubscribeEvent
    @JvmSynthetic
    internal fun baseCommonSetup(e: FMLCommonSetupEvent) {
        commonSetup(e)
    }

    @SubscribeEvent
    @JvmSynthetic
    internal fun baseClientSetup(e: FMLClientSetupEvent) {
        clientSetup(e)
    }

    @SubscribeEvent
    @JvmSynthetic
    internal fun baseDedicatedServerSetup(e: FMLDedicatedServerSetupEvent) {
        dedicatedServerSetup(e)
    }

    @SubscribeEvent
    @JvmSynthetic
    internal fun baseInterModCommsEnqueue(e: InterModEnqueueEvent) {
        interModCommsEnqueue(e)
    }

    @SubscribeEvent
    @JvmSynthetic
    internal fun baseInterModCommsProcess(e: InterModProcessEvent) {
        interModCommsProcess(e)
    }
    //endregion
}