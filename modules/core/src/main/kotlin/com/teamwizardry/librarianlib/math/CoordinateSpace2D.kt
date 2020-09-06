package com.teamwizardry.librarianlib.math

import java.util.Collections
import java.util.IdentityHashMap
import kotlin.math.max
import kotlin.math.min

public interface CoordinateSpace2D {
    /**
     * The parent coordinate space. All points in this space are transformed relative to its parent.
     */
    public val parentSpace: CoordinateSpace2D?

    /**
     * The "normal" matrix that converts points from this space to the parent space.
     *
     * e.g. if the child space is embedded with an offset (x,y) within its parent, this will be
     * `Matrix3d().transform(x,y)`
     */
    public val transform: Matrix3d

    /**
     * The inverse of [transform]. Often the best way to get this is to apply inverse transforms instead of directly
     * inverting the matrix. This allows elegant failure state when scaling by zero. Inverting a matrix with zero
     * scale is impossible, but when applying inverse transforms this can be accounted for by ignoring inverse scales.
     */
    public val inverseTransform: Matrix3d

    /**
     * Create a matrix that, when applied to a point in this coordinate space, returns the corresponding point in the
     * [other] coordinate space.
     */
    public fun conversionMatrixTo(other: CoordinateSpace2D): Matrix3d {
        if (other === this) return Matrix3d.IDENTITY
        if (other === this.parentSpace) return this.transform
        if (other.parentSpace === this) return other.inverseTransform

        val lca = lowestCommonAncestor(other) ?: throw UnrelatedCoordinateSpaceException(this, other)

        if (lca === other) return this.matrixToParent(other)
        if (lca === this) return other.matrixFromParent(this)

        val matrix = MutableMatrix3d()
        matrix *= other.matrixFromParent(lca)
        matrix *= this.matrixToParent(lca)
        return matrix
    }

    /**
     * Create a matrix that, when applied to a point in the [other] coordinate space, returns the corresponding point
     * in the this coordinate space.
     */
    public fun conversionMatrixFrom(other: CoordinateSpace2D): Matrix3d = other.conversionMatrixTo(this)

    /**
     * Converts a point in this coordinate space into the corresponding point in the [other] coordinate space
     */
    public fun convertPointTo(point: Vec2d, other: CoordinateSpace2D): Vec2d = conversionMatrixTo(other) * point

    /**
     * Converts a point in the [other] coordinate space into the corresponding point in this coordinate space
     */
    public fun convertPointFrom(point: Vec2d, other: CoordinateSpace2D): Vec2d = other.convertPointTo(point, this)

    /**
     * Converts an offset in this coordinate space into the equivalent offset in the [other] coordinate space
     */
    @JvmDefault
    public fun convertOffsetTo(offset: Vec2d, other: CoordinateSpace2D): Vec2d = conversionMatrixTo(other).transformDelta(offset)

    /**
     * Converts an offset in the [other] coordinate space into the equivalent offset in this coordinate space
     */
    @JvmDefault
    public fun convertOffsetFrom(offset: Vec2d, other: CoordinateSpace2D): Vec2d = other.convertOffsetTo(offset, this)

    /**
     * Converts a rect in this coordinate space to the _**smallest bounding rectangle**_ around it in the [other]
     * coordinate space
     *
     * ## NOTE!
     *
     * This operation _**IS NOT REVERSIBLE**_. If there is any rotation returned rect will not equal the passed rect,
     * instead it will _contain_ it.
     */
    public fun convertRectTo(rect: Rect2d, other: CoordinateSpace2D): Rect2d {
        var min = rect.min
        var max = rect.max
        var minmax = vec(min.x, max.y)
        var maxmin = vec(max.x, min.y)

        val matrix = conversionMatrixTo(other)
        min = matrix * min
        max = matrix * max
        minmax = matrix * minmax
        maxmin = matrix * maxmin

        val pos = Vec2d.zip(min, max, minmax, maxmin) { a, b, c, d -> min(min(a, b), min(c, d)) }
        val size = Vec2d.zip(min, max, minmax, maxmin) { a, b, c, d -> max(max(a, b), max(c, d)) } - pos

        return Rect2d(pos, size)
    }

    /**
     * Converts a rect in the [other] coordinate space to the _**smallest bounding rectangle**_ around it in this
     * coordinate space
     *
     * ## NOTE!
     *
     * This operation _**IS NOT REVERSIBLE**_. If there is any rotation the returned rect will not equal the passed
     * rect, instead it will _contain_ it.
     */
    public fun convertRectFrom(rect: Rect2d, other: CoordinateSpace2D): Rect2d = other.convertRectTo(rect, this)

    /**
     * Converts a point in this coordinate space into the corresponding point in the parent coordinate space.
     *
     * If this space has no parent, this method returns the original point.
     */
    public fun convertPointToParent(point: Vec2d): Vec2d = parentSpace?.let { convertPointTo(point, it) } ?: point

    /**
     * Converts a point in the parent coordinate space into the corresponding point in this coordinate space.
     *
     * If this space has no parent, this method returns the original point.
     */
    public fun convertPointFromParent(point: Vec2d): Vec2d = parentSpace?.let { convertPointFrom(point, it) } ?: point

    /**
     * Converts a rect in this coordinate space to the _**smallest bounding rectangle**_ around it in the parent
     * coordinate space
     *
     * If this space has no parent, this method returns the original rect.
     *
     * ## NOTE!
     *
     * This operation _**IS NOT REVERSIBLE**_. If there is any rotation returned rect will not equal the passed rect,
     * instead it will _contain_ it.
     */
    public fun convertRectToParent(rect: Rect2d): Rect2d = parentSpace?.let { convertRectTo(rect, it) } ?: rect

    /**
     * Converts a rect in the parent coordinate space to the _**smallest bounding rectangle**_ around it in this
     * coordinate space
     *
     * If this space has no parent, this method returns the original rect.
     *
     * ## NOTE!
     *
     * This operation _**IS NOT REVERSIBLE**_. If there is any rotation the returned rect will not equal the passed
     * rect, instead it will _contain_ it.
     */
    public fun convertRectFromParent(rect: Rect2d): Rect2d = parentSpace?.let { convertRectFrom(rect, it) } ?: rect

    private fun lowestCommonAncestor(other: CoordinateSpace2D): CoordinateSpace2D? {
        // check for straight-line relationships next (doing both in parallel because that minimizes time
        // when the distance is short)
        var thisAncestor = this.parentSpace
        var otherAncestor = other.parentSpace
        while (thisAncestor != null || otherAncestor != null) {
            if (thisAncestor === other) return other
            if (otherAncestor === this) return this
            thisAncestor = thisAncestor?.parentSpace
            otherAncestor = otherAncestor?.parentSpace
        }

        val ancestors: MutableSet<CoordinateSpace2D> = Collections.newSetFromMap<CoordinateSpace2D>(IdentityHashMap())
        var ancestor = this.parentSpace
        while (ancestor != null) {
            ancestors.add(ancestor)
            ancestor = ancestor.parentSpace
        }
        ancestor = other.parentSpace
        while (ancestor != null) {
            if (ancestor in ancestors) return ancestor
            ancestor = ancestor.parentSpace
        }

        return null
    }

    /**
     * The matrix to get our coordinates back to [parent]'s space. [parent] is one of our ancestors
     */
    private fun matrixToParent(parent: CoordinateSpace2D): MutableMatrix3d {
        val ancestors = mutableListOf<CoordinateSpace2D>()
        var space: CoordinateSpace2D = this
        while (space !== parent) {
            ancestors.add(space)
            space = space.parentSpace!!
        }

        val matrix = MutableMatrix3d()
        ancestors.reversed().forEach {
            matrix *= it.transform
        }
        return matrix
    }

    /**
     * The matrix to get [other]'s coordinates down to our space. [other] is one of our ancestors
     */
    private fun matrixFromParent(other: CoordinateSpace2D): MutableMatrix3d {
        val ancestors = mutableListOf<CoordinateSpace2D>()
        var space: CoordinateSpace2D = this
        while (space !== other) {
            ancestors.add(space)
            space = space.parentSpace!!
        }

        val matrix = MutableMatrix3d()
        ancestors.forEach {
            matrix *= it.inverseTransform
        }
        return matrix
    }
}

public class UnrelatedCoordinateSpaceException(public val space1: CoordinateSpace2D, public val space2: CoordinateSpace2D): RuntimeException()
