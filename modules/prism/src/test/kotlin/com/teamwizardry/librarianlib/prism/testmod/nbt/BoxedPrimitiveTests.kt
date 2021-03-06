package com.teamwizardry.librarianlib.prism.testmod.nbt

import com.teamwizardry.librarianlib.core.util.kotlin.NBTBuilder
import com.teamwizardry.librarianlib.prism.nbt.BooleanSerializer
import com.teamwizardry.librarianlib.prism.nbt.ByteSerializer
import com.teamwizardry.librarianlib.prism.nbt.CharacterSerializer
import com.teamwizardry.librarianlib.prism.nbt.DoubleSerializer
import com.teamwizardry.librarianlib.prism.nbt.FloatSerializer
import com.teamwizardry.librarianlib.prism.nbt.IntegerSerializer
import com.teamwizardry.librarianlib.prism.nbt.LongSerializer
import com.teamwizardry.librarianlib.prism.nbt.ShortSerializer
import org.junit.jupiter.api.Test

internal class BoxedPrimitiveTests: NBTPrismTest() {
    @Test
    fun `read+write for Double should be symmetrical`()
        = simple<Double, DoubleSerializer>(1.0, NBTBuilder.double(1))
    @Test
    fun `read+write for Float should be symmetrical`()
        = simple<Float, FloatSerializer>(1f, NBTBuilder.float(1))
    @Test
    fun `read+write for Long should be symmetrical`()
        = simple<Long, LongSerializer>(1L, NBTBuilder.long(1))
    @Test
    fun `read+write for Integer should be symmetrical`()
        = simple<Int, IntegerSerializer>(1, NBTBuilder.int(1))
    @Test
    fun `read+write for Short should be symmetrical`()
        = simple<Short, ShortSerializer>(1.toShort(), NBTBuilder.short(1))
    @Test
    fun `read+write for Byte should be symmetrical`()
        = simple<Byte, ByteSerializer>(1.toByte(), NBTBuilder.byte(1))
    @Test
    fun `read+write for Character should be symmetrical`()
        = simple<Char, CharacterSerializer>(1.toChar(), NBTBuilder.int(1))
    @Test
    fun `read+write for Boolean true should be symmetrical`()
        = simple<Boolean, BooleanSerializer>(true, NBTBuilder.byte(1))
    @Test
    fun `read+write for Boolean false should be symmetrical`()
        = simple<Boolean, BooleanSerializer>(false, NBTBuilder.byte(0))

    @Test
    fun `read for Double with IntNBT should cast`()
        = simpleRead<Double, DoubleSerializer>(1.0, NBTBuilder.int(1))
    @Test
    fun `read for Float with IntNBT should cast`()
        = simpleRead<Float, FloatSerializer>(1f, NBTBuilder.int(1))
    @Test
    fun `read for Long with DoubleNBT should cast and clamp`()
        = simpleRead<Long, LongSerializer>(Long.MAX_VALUE, NBTBuilder.double(1e20))
    @Test
    fun `read for Int with DoubleNBT should cast and clamp`()
        = simpleRead<Int, IntegerSerializer>(Int.MAX_VALUE, NBTBuilder.double(1e10))
    @Test
    fun `read for Short with DoubleNBT should cast and truncate`()
        = simpleRead<Short, ShortSerializer>(100000.toShort(), NBTBuilder.double(1e5))
    @Test
    fun `read for Byte with DoubleNBT should cast and truncate`()
        = simpleRead<Byte, ByteSerializer>(1000.toByte(), NBTBuilder.double(1e3))
    @Test
    fun `read for Char with DoubleNBT should cast and truncate`()
        = simpleRead<Char, CharacterSerializer>(100000.toChar(), NBTBuilder.double(1e5))
}