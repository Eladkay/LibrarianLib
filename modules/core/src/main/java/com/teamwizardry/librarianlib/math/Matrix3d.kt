package com.teamwizardry.librarianlib.math

import com.teamwizardry.librarianlib.core.util.kotlin.threadLocal
import net.minecraft.util.math.Vec3d
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToLong

// adapted from flow/math: https://github.com/flow/math
open class Matrix3d: Cloneable {
    internal var m00: Double
    internal var m01: Double
    internal var m02: Double
    internal var m10: Double
    internal var m11: Double
    internal var m12: Double
    internal var m20: Double
    internal var m21: Double
    internal var m22: Double
    @Volatile
    @Transient
    private var hashCode = 0

    constructor(m: Matrix3d): this(
        m.m00, m.m01, m.m02,
        m.m10, m.m11, m.m12,
        m.m20, m.m21, m.m22)

    constructor() : this(
        1f, 0f, 0f,
        0f, 1f, 0f,
        0f, 0f, 1f)

    constructor(
        m00: Float, m01: Float, m02: Float,
        m10: Float, m11: Float, m12: Float,
        m20: Float, m21: Float, m22: Float): this(
        m00.toDouble(), m01.toDouble(), m02.toDouble(),
        m10.toDouble(), m11.toDouble(), m12.toDouble(),
        m20.toDouble(), m21.toDouble(), m22.toDouble())

    constructor(
        m00: Double, m01: Double, m02: Double,
        m10: Double, m11: Double, m12: Double,
        m20: Double, m21: Double, m22: Double) {
        this.m00 = m00
        this.m01 = m01
        this.m02 = m02
        this.m10 = m10
        this.m11 = m11
        this.m12 = m12
        this.m20 = m20
        this.m21 = m21
        this.m22 = m22
    }

    operator fun get(row: Int, col: Int): Double {
        when (row) {
            0 -> {
                when (col) {
                    0 -> return m00
                    1 -> return m01
                    2 -> return m02
                }
                when (col) {
                    0 -> return m10
                    1 -> return m11
                    2 -> return m12
                }
                when (col) {
                    0 -> return m20
                    1 -> return m21
                    2 -> return m22
                }
            }
            1 -> {
                when (col) {
                    0 -> return m10
                    1 -> return m11
                    2 -> return m12
                }
                when (col) {
                    0 -> return m20
                    1 -> return m21
                    2 -> return m22
                }
            }
            2 -> when (col) {
                0 -> return m20
                1 -> return m21
                2 -> return m22
            }
        }
        throw IllegalArgumentException(
            (if (row < 0 || row > 2) "row must be greater than zero and smaller than 3. " else "") + if (col < 0 || col > 2) "col must be greater than zero and smaller than 3." else "")
    }

    open fun add(m: Matrix3d): Matrix3d {
        return Matrix3d(
            m00 + m.m00, m01 + m.m01, m02 + m.m02,
            m10 + m.m10, m11 + m.m11, m12 + m.m12,
            m20 + m.m20, m21 + m.m21, m22 + m.m22)
    }

    /** Operator function for Kotlin  */
    open operator fun plus(m: Matrix3d): Matrix3d {
        return add(m)
    }

    open fun sub(m: Matrix3d): Matrix3d {
        return Matrix3d(
            m00 - m.m00, m01 - m.m01, m02 - m.m02,
            m10 - m.m10, m11 - m.m11, m12 - m.m12,
            m20 - m.m20, m21 - m.m21, m22 - m.m22)
    }

    /** Operator function for Kotlin  */
    open operator fun minus(m: Matrix3d): Matrix3d {
        return sub(m)
    }

    open fun mul(a: Float): Matrix3d {
        return mul(a.toDouble())
    }

    /** Operator function for Kotlin  */
    open operator fun times(a: Float): Matrix3d {
        return mul(a)
    }

    open fun mul(a: Double): Matrix3d {
        return Matrix3d(
            m00 * a, m01 * a, m02 * a,
            m10 * a, m11 * a, m12 * a,
            m20 * a, m21 * a, m22 * a)
    }

    /** Operator function for Kotlin  */
    open operator fun times(a: Double): Matrix3d {
        return mul(a)
    }

    open fun mul(m: Matrix3d): Matrix3d {
        return Matrix3d(
            m00 * m.m00 + m01 * m.m10 + m02 * m.m20, m00 * m.m01 + m01 * m.m11 + m02 * m.m21,
            m00 * m.m02 + m01 * m.m12 + m02 * m.m22, m10 * m.m00 + m11 * m.m10 + m12 * m.m20,
            m10 * m.m01 + m11 * m.m11 + m12 * m.m21, m10 * m.m02 + m11 * m.m12 + m12 * m.m22,
            m20 * m.m00 + m21 * m.m10 + m22 * m.m20, m20 * m.m01 + m21 * m.m11 + m22 * m.m21,
            m20 * m.m02 + m21 * m.m12 + m22 * m.m22)
    }

    /** Operator function for Kotlin  */
    open operator fun times(m: Matrix3d): Matrix3d {
        return mul(m)
    }

    open operator fun div(a: Float): Matrix3d {
        return div(a.toDouble())
    }

    open operator fun div(a: Double): Matrix3d {
        return Matrix3d(
            m00 / a, m01 / a, m02 / a,
            m10 / a, m11 / a, m12 / a,
            m20 / a, m21 / a, m22 / a)
    }

    open operator fun div(m: Matrix3d): Matrix3d {
        return mul(m.invert())
    }

    open fun pow(pow: Float): Matrix3d {
        return pow(pow.toDouble())
    }

    open fun pow(pow: Double): Matrix3d {
        return Matrix3d(
            m00.pow(pow), m01.pow(pow), m02.pow(pow),
            m10.pow(pow), m11.pow(pow), m12.pow(pow),
            m20.pow(pow), m21.pow(pow), m22.pow(pow))
    }

    open fun translate(v: Vec2d): Matrix3d {
        return translate(v.x, v.y)
    }

    open fun translate(x: Float, y: Float): Matrix3d {
        return translate(x.toDouble(), y.toDouble())
    }

    open fun translate(x: Double, y: Double): Matrix3d {
        return createTranslation(x, y).mul(this).toImmutable()
    }

    open fun scale(scale: Float): Matrix3d {
        return scale(scale.toDouble())
    }

    open fun scale(scale: Double): Matrix3d {
        return scale(scale, scale, scale)
    }

    open fun scale(v: Vec3d): Matrix3d {
        return scale(v.getX(), v.getY(), v.getZ())
    }

    open fun scale(x: Float, y: Float, z: Float): Matrix3d {
        return scale(x.toDouble(), y.toDouble(), z.toDouble())
    }

    open fun scale(x: Double, y: Double, z: Double): Matrix3d {
        return createScaling(x, y, z).mul(this)
    }

    open fun rotate(rot: Quaterniond): Matrix3d {
        return createRotation(rot).mul(this)
    }

    fun transformAffine2d(v: Vec2d): Vec2d {
        return transformAffine2d(v.x, v.y)
    }

    fun transformAffine2d(x: Double, y: Double): Vec2d {
        return vec(
            m00 * x + m01 * y + m02 * 1,
            m10 * x + m11 * y + m12 * 1)
    }

    fun transform(v: Vec3d): Vec3d {
        return transform(v.getX(), v.getY(), v.getZ())
    }

    fun transform(x: Float, y: Float, z: Float): Vec3d {
        return transform(x.toDouble(), y.toDouble(), z.toDouble())
    }

    fun transform(x: Double, y: Double, z: Double): Vec3d {
        return vec(
            m00 * x + m01 * y + m02 * z,
            m10 * x + m11 * y + m12 * z,
            m20 * x + m21 * y + m22 * z)
    }

    open fun floor(): Matrix3d {
        return Matrix3d(
            floor(m00), floor(m01), floor(m02),
            floor(m10), floor(m11), floor(m12),
            floor(m20), floor(m21), floor(m22))
    }

    open fun ceil(): Matrix3d {
        return Matrix3d(
            kotlin.math.ceil(m00), kotlin.math.ceil(m01), kotlin.math.ceil(m02),
            kotlin.math.ceil(m10), kotlin.math.ceil(m11), kotlin.math.ceil(m12),
            kotlin.math.ceil(m20), kotlin.math.ceil(m21), kotlin.math.ceil(m22))
    }

    open fun round(): Matrix3d {
        return Matrix3d(
            m00.roundToLong().toFloat(), m01.roundToLong().toFloat(), m02.roundToLong().toFloat(),
            m10.roundToLong().toFloat(), m11.roundToLong().toFloat(), m12.roundToLong().toFloat(),
            m20.roundToLong().toFloat(), m21.roundToLong().toFloat(), m22.roundToLong().toFloat())
    }

    open fun abs(): Matrix3d {
        return Matrix3d(
            kotlin.math.abs(m00), kotlin.math.abs(m01), kotlin.math.abs(m02),
            kotlin.math.abs(m10), kotlin.math.abs(m11), kotlin.math.abs(m12),
            kotlin.math.abs(m20), kotlin.math.abs(m21), kotlin.math.abs(m22))
    }

    open fun negate(): Matrix3d {
        return Matrix3d(
            -m00, -m01, -m02,
            -m10, -m11, -m12,
            -m20, -m21, -m22)
    }

    /** Operator function for Kotlin  */
    @JvmSynthetic
    open operator fun unaryMinus(): Matrix3d {
        return negate()
    }
    @JvmSynthetic
    operator fun times(v: Vec3d): Vec3d = transform(v)
    /** Applies an affine transform on the passed vector. Equivalent to `transform(v.x, v.y, 1).xy` */
    @JvmSynthetic
    operator fun times(v: Vec2d): Vec2d = transformAffine2d(v)

    open fun transpose(): Matrix3d {
        return Matrix3d(
            m00, m10, m20,
            m01, m11, m21,
            m02, m12, m22)
    }

    fun trace(): Double {
        return m00 + m11 + m22
    }

    fun determinant(): Double {
        return m00 * (m11 * m22 - m12 * m21) - m01 * (m10 * m22 - m12 * m20) + m02 * (m10 * m21 - m11 * m20)
    }

    open fun invert(): Matrix3d {
        val det = determinant()
        if (kotlin.math.abs(det) < DBL_EPSILON) {
            throw ArithmeticException("Cannot inverse a matrix with a zero determinant")
        }
        return Matrix3d(
            (m11 * m22 - m21 * m12) / det, -(m01 * m22 - m21 * m02) / det, (m01 * m12 - m02 * m11) / det,
            -(m10 * m22 - m20 * m12) / det, (m00 * m22 - m20 * m02) / det, -(m00 * m12 - m10 * m02) / det,
            (m10 * m21 - m20 * m11) / det, -(m00 * m21 - m20 * m01) / det, (m00 * m11 - m01 * m10) / det)
    }

    fun toArray(): DoubleArray {
        return toArray(false)
    }

    fun toArray(columnMajor: Boolean): DoubleArray {
        return if (columnMajor) {
            doubleArrayOf(m00, m10, m20, m01, m11, m21, m02, m12, m22)
        } else {
            doubleArrayOf(m00, m01, m02, m10, m11, m12, m20, m21, m22)
        }
    }

    override fun toString(): String {
        return (m00.toString() + " " + m01 + " " + m02 + "\n"
            + m10 + " " + m11 + " " + m12 + "\n"
            + m20 + " " + m21 + " " + m22 + "\n")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        val matrix3 = other as? Matrix3d ?: return false

        return matrix3.m00 == 0.0 && matrix3.m01 == 0.0 && matrix3.m02 == 0.0 &&
            matrix3.m10 == 0.0 && matrix3.m11 == 0.0 && matrix3.m12 == 0.0 &&
            matrix3.m20 == 0.0 && matrix3.m21 == 0.0 && matrix3.m22 == 0.0
    }

    override fun hashCode(): Int {
        if (hashCode == 0) {
            var result = m00.hashCode()
            result = 31 * result + m01.hashCode()
            result = 31 * result + m02.hashCode()
            result = 31 * result + m10.hashCode()
            result = 31 * result + m11.hashCode()
            result = 31 * result + m12.hashCode()
            result = 31 * result + m20.hashCode()
            result = 31 * result + m21.hashCode()
            result = 31 * result + m22.hashCode()
            hashCode = if(result == 0) 1 else result
        }
        return hashCode
    }

    override fun clone(): Matrix3d {
        return Matrix3d(this)
    }

    open fun toMutable(): MutableMatrix3d {
        return MutableMatrix3d(this)
    }

    companion object {
        private val DBL_EPSILON = Double.fromBits(0x3cb0000000000000L)
        val ZERO = Matrix3d(
            0f, 0f, 0f,
            0f, 0f, 0f,
            0f, 0f, 0f)
        val IDENTITY = Matrix3d()

        private val temporaryMatrix: MutableMatrix3d by threadLocal { MutableMatrix3d() }

        internal fun createScaling(x: Double, y: Double, z: Double): MutableMatrix3d {
            return temporaryMatrix.set(
                x, 0.0, 0.0,
                0.0, y, 0.0,
                0.0, 0.0, z)
        }

        internal fun createTranslation(x: Double, y: Double): MutableMatrix3d {
            return temporaryMatrix.set(
                1.0, 0.0, x,
                0.0, 1.0, y,
                0.0, 0.0, 1.0)
        }

        internal fun createRotation(rot: Quaterniond): MutableMatrix3d {
            var rot = rot
            rot = rot.normalize()

            return temporaryMatrix.set(
                1 - 2 * rot.y * rot.y - 2 * rot.z * rot.z,
                    2 * rot.x * rot.y - 2 * rot.w * rot.z,
                    2 * rot.x * rot.z + 2 * rot.w * rot.y,
                    2 * rot.x * rot.y + 2 * rot.w * rot.z,
                1 - 2 * rot.x * rot.x - 2 * rot.z * rot.z,
                    2 * rot.y * rot.z - 2 * rot.w * rot.x,
                    2 * rot.x * rot.z - 2 * rot.w * rot.y,
                    2 * rot.y * rot.z + 2 * rot.x * rot.w,
                1 - 2 * rot.x * rot.x - 2 * rot.y * rot.y)
        }
    }
}
