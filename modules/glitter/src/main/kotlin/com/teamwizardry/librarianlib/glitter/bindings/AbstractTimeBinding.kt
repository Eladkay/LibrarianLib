package com.teamwizardry.librarianlib.glitter.bindings

import com.teamwizardry.librarianlib.math.Easing
import com.teamwizardry.librarianlib.glitter.ParticleSystem
import com.teamwizardry.librarianlib.glitter.ReadParticleBinding

public abstract class AbstractTimeBinding(
    /**
     * The lifetime binding for the particle. Generally [ParticleSystem.lifetime]
     */
    public open val lifetime: ReadParticleBinding,
    /**
     * The age binding for the particle. Generally [ParticleSystem.age]
     */
    public open val age: ReadParticleBinding,
    /**
     * The multiplier for the normalized age. If this value is > 1 the movement will loop, and if this value is < 1
     * the movement will end before the end of the path.
     */
    public open val timescale: ReadParticleBinding? = ConstantBinding(1.0),
    /**
     * The time offset for the normalized age. Applied before the [timescale], so regardless of [timescale]'s value,
     * if the offset is 0.5, the animation will begin halfway along the path
     */
    public open val offset: ReadParticleBinding? = ConstantBinding(0.0),
    /**
     * The easing to use if you want one to manipulate the binding.
     */
    public open val easing: Easing = Easing.linear
): ReadParticleBinding {
    protected var time: Double = 0.0

    override fun load(particle: DoubleArray) {
        age.load(particle)
        lifetime.load(particle)
        var t = age.contents[0] / lifetime.contents[0]

        if (easing != Easing.linear) t = easing.ease(t.toFloat()).toDouble()
        if (offset != null) t += offset!!.contents[0]
        if (timescale != null) t *= timescale!!.contents[0]
        if (t != 0.0) {
            t %= 1
            // because 1 % 1 = 0, but it should go up to 1 inclusive. If t is really 0 the above if won't pass
            if (t == 0.0) t = 1.0
        }
        time = t
    }
}