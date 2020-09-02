package com.teamwizardry.librarianlib.facade.testmod.screens

import com.teamwizardry.librarianlib.core.util.kotlin.toRl
import com.teamwizardry.librarianlib.facade.FacadeScreen
import com.teamwizardry.librarianlib.facade.layer.GuiLayerEvents
import com.teamwizardry.librarianlib.facade.layers.SpriteLayer
import com.teamwizardry.librarianlib.math.vec
import com.teamwizardry.librarianlib.mosaic.Mosaic
import net.minecraft.util.text.ITextComponent

class LayerMouseOverOffTestScreen(title: ITextComponent): FacadeScreen(title) {
    init {
        val dirt = Mosaic("minecraft:textures/block/dirt.png".toRl(), 16, 16).getSprite("")
        val stone = Mosaic("minecraft:textures/block/stone.png".toRl(), 16, 16).getSprite("")
        val layer = SpriteLayer(dirt)
        layer.pos = vec(32, 32)

        layer.BUS.hook<GuiLayerEvents.MouseMoveOver> {
            layer.sprite = stone
        }
        layer.BUS.hook<GuiLayerEvents.MouseMoveOff> {
            layer.sprite = dirt
        }
        facade.root.add(layer)
    }
}