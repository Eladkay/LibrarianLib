package com.teamwizardry.librarianlib.math

import net.minecraft.util.math.Vec3d

public operator fun Vec3d.plus(other: Vec3d): Vec3d = vec(this.x + other.x, this.y + other.y, this.z + other.z)
public operator fun Vec3d.minus(other: Vec3d): Vec3d = vec(this.x - other.x, this.y - other.y, this.z - other.z)
public operator fun Vec3d.times(other: Vec3d): Vec3d = vec(this.x * other.x, this.y * other.y, this.z * other.z)
public operator fun Vec3d.div(other: Vec3d): Vec3d = vec(this.x / other.x, this.y / other.y, this.z / other.z)

@Suppress("NOTHING_TO_INLINE")
@JvmSynthetic
public inline operator fun Vec3d.div(other: Number): Vec3d = div(other.toDouble())
public operator fun Vec3d.div(other: Double): Vec3d = vec(this.x / other, this.y / other, this.z / other)

@Suppress("NOTHING_TO_INLINE")
@JvmSynthetic
public inline operator fun Vec3d.times(other: Number): Vec3d = times(other.toDouble())
public operator fun Vec3d.times(other: Double): Vec3d = vec(this.x * other, this.y * other, this.z * other)

public operator fun Vec3d.unaryMinus(): Vec3d = this * -1

public infix fun Vec3d.dot(other: Vec3d): Double = this.dotProduct(other)
public infix fun Vec3d.cross(other: Vec3d): Vec3d = this.crossProduct(other)

@JvmSynthetic
public operator fun Vec3d.component1(): Double = this.x

@JvmSynthetic
public operator fun Vec3d.component2(): Double = this.y

@JvmSynthetic
public operator fun Vec3d.component3(): Double = this.z
