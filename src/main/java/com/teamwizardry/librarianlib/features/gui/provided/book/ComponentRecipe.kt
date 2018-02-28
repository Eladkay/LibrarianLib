package com.teamwizardry.librarianlib.features.gui.provided.book

import com.teamwizardry.librarianlib.features.gui.component.GuiComponent
import com.teamwizardry.librarianlib.features.gui.component.GuiComponentEvents
import com.teamwizardry.librarianlib.features.gui.components.ComponentStack
import com.teamwizardry.librarianlib.features.sprite.Sprite
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.recipebook.GhostRecipe
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.inventory.Slot
import net.minecraft.item.crafting.IRecipe
import net.minecraft.item.crafting.Ingredient
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.IShapedRecipe
import net.minecraftforge.fml.common.registry.ForgeRegistries
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_SMOOTH
import java.awt.Color

class ComponentRecipe(posX: Int, posY: Int, width: Int, height: Int, mainColor: Color, key: ResourceLocation, arrow: Sprite) : GuiComponent(posX, posY, width, height) {

    init {

        val recipe = ForgeRegistries.RECIPES.getValue(key)

        if (recipe != null) {
            val ghostRecipe = createGhostRecipe(recipe, Minecraft.getMinecraft().player.openContainer.inventorySlots)

            val output = ComponentStack(
                    (size.x / 2.0 - 8 + 40).toInt(), (size.y / 2.0 - 8).toInt() - 8)
            output.stack.setValue(recipe.recipeOutput)
            add(output)

            var row = -1
            var column = -1
            for (i in 1 until recipe.ingredients.size + 1) {
                val ingredient = ghostRecipe.get(i)

                val x = (-8 + size.x / 2.0 - 24).toInt()
                val y = (-8 + size.y / 2.0 - 8).toInt()
                val stack = ComponentStack(x + row * 16, y + column * 16)
                stack.stack.setValue(ingredient.item)
                add(stack)
                stack.BUS.hook(GuiComponentEvents.ComponentTickEvent::class.java) {
                    if (!stack.stack.getValue(stack).isItemEqual(ingredient.item)) {
                        stack.stack.setValue(ingredient.item)
                    }
                }

                if (++row >= 2) {
                    column++
                    row = -1
                }
            }

            BUS.hook(GuiComponentEvents.PostDrawEvent::class.java) { event ->
                GlStateManager.pushMatrix()
                GlStateManager.enableBlend()
                GlStateManager.enableAlpha()
                GlStateManager.translate(
                        (size.x / 2.0 + arrow.width / 2.0 + 16.0).toInt().toFloat(), (size.y / 2.0 + arrow.height / 2.0 - 8 + 1).toInt().toFloat(), 0f)
                GlStateManager.rotate(180f, 0f, 0f, 1f)
                GlStateManager.color(1f, 0.5f, 1f, 1f)
                arrow.bind()
                arrow.draw(event.partialTicks.toInt(), 0f, 0f)
                GlStateManager.popMatrix()

                GlStateManager.pushMatrix()
                GlStateManager.enableBlend()
                GlStateManager.enableAlpha()
                GlStateManager.disableCull()
                GlStateManager.color(1f, 1f, 1f, 1f)
                //GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE);
                GlStateManager.disableTexture2D()
                GlStateManager.shadeModel(GL_SMOOTH)

                val x = (-8 + size.x / 2.0 - 24.0 - 16.toDouble()).toInt()
                val y = (-8 + size.y / 2.0 - 16.toDouble() - 8.0).toInt()
                val bandWidth = 1
                val excess = 6

                GlStateManager.translate(x - bandWidth / 2.0, y.toDouble(), 500.0)

                val color = mainColor.darker().darker()
                val fadeOff = Color(color.red, color.green, color.blue, 20)

                val tessellator = Tessellator.getInstance()
                val buffer = tessellator.buffer

                for (i in 1..2) {
                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
                    buffer.pos((i * 16 + bandWidth).toDouble(), (0 - excess).toDouble(), 200.0).color(fadeOff.red, fadeOff.green, fadeOff.blue, fadeOff.alpha).endVertex()
                    buffer.pos((i * 16).toDouble(), (0 - excess).toDouble(), 200.0).color(fadeOff.red, fadeOff.green, fadeOff.blue, fadeOff.alpha).endVertex()
                    buffer.pos((i * 16).toDouble(), 24.0, 200.0).color(color.red, color.green, color.blue, color.alpha).endVertex()
                    buffer.pos((i * 16 + bandWidth).toDouble(), 24.0, 200.0).color(color.red, color.green, color.blue, color.alpha).endVertex()
                    tessellator.draw()

                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
                    buffer.pos((i * 16 + bandWidth).toDouble(), (48 + excess).toDouble(), 200.0).color(fadeOff.red, fadeOff.green, fadeOff.blue, fadeOff.alpha).endVertex()
                    buffer.pos((i * 16).toDouble(), (48 + excess).toDouble(), 200.0).color(fadeOff.red, fadeOff.green, fadeOff.blue, fadeOff.alpha).endVertex()
                    buffer.pos((i * 16).toDouble(), 24.0, 200.0).color(color.red, color.green, color.blue, color.alpha).endVertex()
                    buffer.pos((i * 16 + bandWidth).toDouble(), 24.0, 200.0).color(color.red, color.green, color.blue, color.alpha).endVertex()
                    tessellator.draw()
                }

                for (i in 1..2) {
                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
                    buffer.pos((0 - excess).toDouble(), (i * 16 + bandWidth).toDouble(), 200.0).color(fadeOff.red, fadeOff.green, fadeOff.blue, fadeOff.alpha).endVertex()
                    buffer.pos((0 - excess).toDouble(), (i * 16).toDouble(), 200.0).color(fadeOff.red, fadeOff.green, fadeOff.blue, fadeOff.alpha).endVertex()
                    buffer.pos(24.0, (i * 16).toDouble(), 200.0).color(color.red, color.green, color.blue, color.alpha).endVertex()
                    buffer.pos(24.0, (i * 16 + bandWidth).toDouble(), 200.0).color(color.red, color.green, color.blue, color.alpha).endVertex()
                    tessellator.draw()

                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
                    buffer.pos((48 + excess).toDouble(), (i * 16 + bandWidth).toDouble(), 200.0).color(fadeOff.red, fadeOff.green, fadeOff.blue, fadeOff.alpha).endVertex()
                    buffer.pos((48 + excess).toDouble(), (i * 16).toDouble(), 200.0).color(fadeOff.red, fadeOff.green, fadeOff.blue, fadeOff.alpha).endVertex()
                    buffer.pos(24.0, (i * 16).toDouble(), 200.0).color(color.red, color.green, color.blue, color.alpha).endVertex()
                    buffer.pos(24.0, (i * 16 + bandWidth).toDouble(), 200.0).color(color.red, color.green, color.blue, color.alpha).endVertex()
                    tessellator.draw()
                }

                GlStateManager.popMatrix()
                RenderHelper.enableStandardItemLighting()
            }
        }
    }

    private fun createGhostRecipe(recipe: IRecipe, slots: List<Slot>): GhostRecipe {
        val ghostRecipe = GhostRecipe()
        val itemstack = recipe.recipeOutput
        ghostRecipe.recipe = recipe
        ghostRecipe.addIngredient(Ingredient.fromStacks(itemstack),
                (size.x - 60).toInt(), (size.y / 2.0 - 32).toInt())
        val i = 3
        val j = 3
        val k = (recipe as? IShapedRecipe)?.recipeWidth ?: i
        var l = 1
        val iterator = recipe.ingredients.iterator()

        for (i1 in 0 until j) {
            for (j1 in 0 until k) {
                if (!iterator.hasNext()) return ghostRecipe

                val ingredient = iterator.next()

                if (ingredient !== Ingredient.EMPTY) {
                    val slot = slots[l]
                    ghostRecipe.addIngredient(ingredient, slot.xPos, slot.yPos)
                }

                ++l
            }

            if (k < i) {
                l += i - k
            }
        }

        return ghostRecipe
    }
}