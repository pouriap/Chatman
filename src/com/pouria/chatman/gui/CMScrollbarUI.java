/*
 * Copyright (c) 2020. Pouria Pirhadi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.pouria.chatman.gui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/**
 *
 * @author pouriap
 */
public class CMScrollbarUI extends BasicScrollBarUI{

	private static int CORNER_RADIUS = 5;
	private final Color trackColor;
	private final Color normalColor;
	private final Color normalShadowColor;
	private final Color hoverColor;
	private final Color hoverShadowColor;
	private final Color dragColor;
	private final Color dragShadowColor;

	public CMScrollbarUI(CMTheme theme){
		super();
		if(theme.getTextAreasTheme().equals("dark")){
			trackColor = new Color(45,45,45);
			normalColor = new Color(80,80,80);
			normalShadowColor = new Color(65,65,65);
			hoverColor = new Color(110, 110,110);
			hoverShadowColor = new Color(80,80,80);
			dragColor = new Color(75,75,75);
			dragShadowColor = new Color(50,50,50);
		}
		else{
			trackColor = new Color(220, 220, 220);
			normalColor = new Color(180, 180, 180);
			normalShadowColor = new Color(200, 200, 200);
			hoverColor = new Color(240, 240, 240);
			hoverShadowColor = new Color(200, 200, 200);
			dragColor = new Color(200,200,200);
			dragShadowColor = new Color(150,150,150);

		}
	}
	
	@Override
	protected JButton createIncreaseButton(int orientation){
		JButton button = new JButton();
		button.setOpaque(false);
		button.setContentAreaFilled(false);
		button.setBorderPainted(false);		
		//hidden button hehe
		//button.setIcon(new ImageIcon("C:\\Users\\Pouria\\Desktop\\down.png"));
		return button;
	}
	
	@Override
	protected JButton createDecreaseButton(int orientation){
		JButton button = new JButton();
		button.setOpaque(false);
		button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		//hidden button hehe
		//button.setIcon(new ImageIcon("C:\\Users\\Pouria\\Desktop\\up.png"));
		return button;
	}
	
	@Override
	protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds)
	{
		//no track hehe
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(trackColor);
		//g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
		g.fillRoundRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height,
			CORNER_RADIUS, CORNER_RADIUS);

		if(trackHighlight == DECREASE_HIGHLIGHT)        {
			paintDecreaseHighlight(g);
		}
		else if(trackHighlight == INCREASE_HIGHLIGHT)           {
			paintIncreaseHighlight(g);
		}
	}
	
	@Override
	protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds)
	{

		if(thumbBounds.isEmpty() || !scrollbar.isEnabled())     {
			return;
		}

		int w = thumbBounds.width;
		int h = thumbBounds.height;

		g.translate(thumbBounds.x, thumbBounds.y);

		//antialiasing
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Color color;
		Color shadowColor;
		if(isDragging) {
		  color = dragColor;
		  shadowColor = dragShadowColor;
		}else if(isThumbRollover()) {
		  color = hoverColor;
		  shadowColor = hoverShadowColor;
		}else {
		  color = normalColor;
		  shadowColor = normalShadowColor;
		}

		//thumb shadow
		g.setColor(shadowColor);
		g.drawRoundRect(0, 0, w-1, h-1, CORNER_RADIUS, CORNER_RADIUS);
		//thumb color
		g.setColor(color);
		g.fillRoundRect(0, 0, w-1, h-1, CORNER_RADIUS, CORNER_RADIUS);

		g.translate(-thumbBounds.x, -thumbBounds.y);
	}

}