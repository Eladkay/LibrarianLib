@file:JvmName("EnvironmentUtil")
package com.teamwizardry.librarianlib.core.util

import com.teamwizardry.librarianlib.core.util.kotlin.getOrNull
import cpw.mods.modlauncher.Launcher
import cpw.mods.modlauncher.api.IEnvironment
import cpw.mods.modlauncher.api.INameMappingService
import net.minecraft.util.math.Vec3i
import java.util.function.BiFunction
import kotlin.reflect.jvm.javaMethod

private val obfTest = Vec3i::getX.javaMethod!! // has to be a separate field, otherwise it gets optimized away

/**
 * True if the current environment is obfuscated
 */
public val IS_OBFUSCATED: Boolean = obfTest.name == "func_177958_n"

/**
 * True if this mod is running in a development environment.
 */
public val IS_DEV_ENV: Boolean = run {
    val target = Launcher.INSTANCE?.environment()?.getProperty(IEnvironment.Keys.LAUNCHTARGET.get())?.getOrNull()
        ?: return@run false
    return@run target.startsWith("fmldev") || target.startsWith("fmluserdev")
}

private val nameMappingService: BiFunction<INameMappingService.Domain, String, String>? by lazy {
    Launcher.INSTANCE?.environment()?.findNameMapping("srg")?.getOrNull()
}

/**
 * Deobfuscate the passed srg field or method name using the current mappings
 */
public fun mapSrgName(srgName: String): String {
    val nameMappingService = nameMappingService ?: return srgName
    return if(srgName.startsWith("field_")) {
        nameMappingService.apply(INameMappingService.Domain.FIELD, srgName)
    } else if(srgName.startsWith("func_")) {
        nameMappingService.apply(INameMappingService.Domain.METHOD, srgName)
    } else {
        throw IllegalArgumentException("`$srgName` isn't a srg field or method name")
    }
}
