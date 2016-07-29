package com.teamwizardry.librarianlib.api.gui.components;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import com.teamwizardry.librarianlib.api.gui.GuiComponent;
import com.teamwizardry.librarianlib.api.gui.HandlerList;
import com.teamwizardry.librarianlib.api.gui.Option;
import com.teamwizardry.librarianlib.api.util.misc.Color;
import com.teamwizardry.librarianlib.client.TextWrapper;
import com.teamwizardry.librarianlib.math.Vec2;

public class ComponentMarkup extends GuiComponent<ComponentMarkup> {
	
	public final Option<ComponentMarkup, Integer> start = new Option<>(0);
	public final Option<ComponentMarkup, Integer> end = new Option<>(Integer.MAX_VALUE);
	
	List<MarkupElement> elements = new ArrayList<>();
	
	public ComponentMarkup(int posX, int posY, int width, int height) {
		super(posX, posY, width, height);
		
		mouseClick.add((c, pos, button) -> {
			for (MarkupElement element : elements) {
				if(element.isMouseOver(pos.xi, pos.yi)) {
					element.click.fireAll((h) -> h.click());
					return true;
				}
			}
			return false;
		}); 
	}
	
	@Override
	public Vec2 relativePos(Vec2 pos) {
		return super.relativePos(pos).add(0, start.getValue(this));
	}
	
	public MarkupElement create(String text) {
		int x = 0;
		int y = 0;
		if(elements.size() > 0) {
			MarkupElement prev = elements.get(elements.size()-1);
			x = prev.endX();
			y = prev.endY();
		}
		MarkupElement element = new MarkupElement(y, x, size.xi, text);
		elements.add(element);
		return element;
	}
	
	@Override
	public void drawComponent(Vec2 mousePos, float partialTicks) {
		int start = this.start.getValue(this);
		int end = this.end.getValue(this);
		GlStateManager.translate(0, -start, 0);
		for (MarkupElement element : elements) {
			if( ( element.posY >= start && element.posY <= end ) ||
					( element.posY + element.height() >= start && element.posY + element.height() <= end ) ||
					( element.posY <= start && element.posY+element.height() >= end ))
				element.render(element.isMouseOver(mousePos.xi, mousePos.yi));
		}
		GlStateManager.translate(0, start, 0);
	}
	
	public static class MarkupElement {
		public int posY;
		public final Option<Boolean, String> format = new Option<>("");
		public final Option<Boolean, Color> color = new Option<>(Color.BLACK);
		public final Option<Boolean, Boolean> dropShadow = new Option<>(false);
		public final HandlerList<IClickHandler> click = new HandlerList<>();
		public List<String> lines = new ArrayList<>();
		private int[] lengths;
		public int firstLineOffset;
		
		public MarkupElement(int posY, int firstLineOffset, int width, String text) {
			TextWrapper.wrap(Minecraft.getMinecraft().fontRendererObj, lines, text, firstLineOffset, width);
			this.posY = posY;
			this.firstLineOffset = firstLineOffset;
			lengths = new int[lines.size()];
			for (int i = 0; i < lengths.length; i++) {
				lengths[i] = Minecraft.getMinecraft().fontRendererObj.getStringWidth(lines.get(i));
			}
		}
		
		public void render(boolean hover) {
			int i = 0;
			for (String line : lines) {
				drawLine(line, i == 0 ? firstLineOffset : 0, posY + i*Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT, hover);
				i++;
			}
		}
		
		protected void drawLine(String line, int x, int y, boolean hover) {
			Minecraft.getMinecraft().fontRendererObj.drawString(format.getValue(hover) + line, x, y, color.getValue(hover).hexARGB(), dropShadow.getValue(hover));
		}
		
		public boolean isMouseOver(int x, int y) {
			y -= posY;
			int height = Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
			for (int i = 0; i < lengths.length; i++) {
				int xPos = (i == 0) ? firstLineOffset : 0;
				if(y >= i*height && y < (i+1)*height &&
					x >= xPos && x < xPos+lengths[i]) {
					return true;
				}
			}
			return false;
		}
		
		public int endX() {
			return (lengths.length == 1 ? firstLineOffset : 0) + lengths[lengths.length-1];
		}
		
		public int endY() {
			return posY + Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * (lines.size()-1);
		}
		
		public int height() {
			return Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * lines.size();
		}

		@FunctionalInterface
		public static interface IClickHandler {
			void click();
		}
	}

}
