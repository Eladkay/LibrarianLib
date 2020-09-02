@file:Suppress("LocalVariableName")

package com.teamwizardry.librarianlib.facade.testmod

import com.teamwizardry.librarianlib.core.util.kotlin.toRl
import com.teamwizardry.librarianlib.facade.FacadeScreen
import com.teamwizardry.librarianlib.facade.layer.GuiLayerEvents
import com.teamwizardry.librarianlib.facade.layers.SpriteLayer
import com.teamwizardry.librarianlib.facade.provided.SafetyNetErrorScreen
import com.teamwizardry.librarianlib.facade.safetyNet
import com.teamwizardry.librarianlib.facade.testmod.screens.*
import com.teamwizardry.librarianlib.facade.testmod.screens.pastry.PastryTestScreen
import com.teamwizardry.librarianlib.facade.testmod.value.RMValueTests
import com.teamwizardry.librarianlib.math.Easing
import com.teamwizardry.librarianlib.math.vec
import com.teamwizardry.librarianlib.mosaic.Mosaic
import com.teamwizardry.librarianlib.testbase.TestMod
import com.teamwizardry.librarianlib.testbase.objects.TestScreenConfig
import net.minecraft.util.text.StringTextComponent
import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.LogManager

@Mod("librarianlib-facade-test")
object LibrarianLibSpritesTestMod: TestMod("facade", "Facade", logger) {
    val groups: List<FacadeTestGroup> = listOf(
        FacadeTestGroup("basics", "Basics", listOf(
            FacadeTest("Empty", ::EmptyTestScreen),
            FacadeTest("zIndex", ::ZIndexTestScreen),
            FacadeTest("Layer Transform", ::LayerTransformTestScreen),
            FacadeTest("Layer MouseOver/MouseOff", ::LayerMouseOverOffTestScreen)
        )),
        FacadeTestGroup("layers", "Layers", listOf(
            FacadeTest("Simple Sprite", ::SimpleSpriteTestScreen),
            FacadeTest("Simple Text", ::SimpleTextTestScreen),
            FacadeTest("DragLayer", ::DragLayerTestScreen)
        )),
        FacadeTestGroup("animations", "Animations/Time", listOf(
            FacadeTest("Animations", ::AnimationTestScreen),
            FacadeTest("Scheduled Callbacks", ::ScheduledCallbacksTestScreen),
            FacadeTest("Scheduled Repeated Callbacks", ::ScheduledRepeatedCallbacksTestScreen)
        )),
        FacadeTestGroup("clipping_compositing", "Clipping/Compositing", listOf(
            FacadeTest("Clip to Bounds", ::ClipToBoundsTestScreen),
            FacadeTest("Masking", ::MaskingTestScreen),
            FacadeTest("Opacity", ::OpacityTestScreen),
            FacadeTest("Blending", ::BlendingTestScreen),
            FacadeTest("Render to FBO Scale", ::RenderFBOScaleTest),
            FacadeTest("Render to Quad Scale", ::RenderQuadScaleTest)
        )),
        FacadeTestGroup("advanced", "Advanced", listOf(
            FacadeTest("Yoga Simple Flex", ::SimpleYogaScreen),
            FacadeTest("Yoga List", ::YogaListScreen),
            FacadeTest("Pastry", ::PastryTestScreen)
        ))
    )

    init {
        groups.forEach { group ->
            +TestScreenConfig(group.id, group.name, itemGroup) {
                customScreen {
                    TestListScreen(group.name, group.tests)
                }
            }
        }

        +UnitTestSuite("rmvalue") {
            add<RMValueTests>()
        }
    }
}



internal val logger = LogManager.getLogger("LibrarianLib: Facade Test")
