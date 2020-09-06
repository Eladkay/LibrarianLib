package com.teamwizardry.librarianlib.facade.layer

import org.lwjgl.util.yoga.YGValue
import org.lwjgl.util.yoga.Yoga
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KProperty

//TODO: Yoga.YGUndefined

/**
 * A yoga property that can contain an enum
 *
 * TODO: make this actually use enums
 */
public class YogaEnumProperty(private val yogaNode: Long, private val getter: (Long) -> Int, private val setter: (Long, Int) -> Unit) {
    public operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return getter(yogaNode)
    }

    public operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        setter(yogaNode, value)
    }
}

/**
 * A yoga property that can contain a float
 */
public class YogaFloatProperty(private val yogaNode: Long, private val getter: (Long) -> Float, private val setter: (Long, Float) -> Unit) {
    public operator fun getValue(thisRef: Any?, property: KProperty<*>): Float {
        return getter(yogaNode)
    }

    public operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
        setter(yogaNode, value)
    }
}

/**
 * A yoga property that can contain either a fixed pixel value or a percentage
 */
public open class YogaPercentageProperty(
    protected val yogaNode: Long,
    private val getter: (Long, YGValue) -> YGValue,
    private val pixelSetter: (Long, Float) -> Unit,
    private val percentSetter: (Long, Float) -> Unit
) {
    /**
     * The pixel value of this property. Returns zero if the property contains a percentage value.
     */
    public var px: Float
        get() = ygValue {
            getter(yogaNode, it)
            if (it.unit() == Yoga.YGUnitPoint)
                it.value()
            else
                0f
        }
        set(value) {
            pixelSetter(yogaNode, value)
        }

    public var percent: Float
        get() = ygValue {
            getter(yogaNode, it)
            if (it.unit() == Yoga.YGUnitPercent)
                it.value()
            else
                0f
        }
        set(value) {
            percentSetter(yogaNode, value)
        }

    /**
     * The units of the value contained in this property.
     *
     * @See Yoga.YGUnitUndefined
     * @See Yoga.YGUnitPercent
     * @See Yoga.YGUnitPoint
     */
    public val unit: Int
        get() = ygValue {
            getter(yogaNode, it)
            it.unit()
        }
}

public class YogaAutoProperty(
    yogaNode: Long,
    getter: (Long, YGValue) -> YGValue,
    pixelSetter: (Long, Float) -> Unit,
    percentSetter: (Long, Float) -> Unit,
    private val autoSetter: (Long) -> Unit
): YogaPercentageProperty(yogaNode, getter, pixelSetter, percentSetter) {
    /**
     * Whether the property's value is currently `auto`. Set this to true using [auto]
     */
    public val isAuto: Boolean
        get() = unit == Yoga.YGUnitAuto

    /**
     * Set the property's value to `auto`
     */
    public fun auto() {
        autoSetter(yogaNode)
    }
}

@OptIn(ExperimentalContracts::class)
private inline fun <T> ygValue(block: (YGValue) -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    val ygVal = YGValue.malloc()
    val result = block(ygVal)
    ygVal.free()
    return result
}
