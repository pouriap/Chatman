/*
 * Copyright (C) 2020 pouriap
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pouria.chatman.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 *
 * @author pouriap
 */
public class CMScrollbar extends BasicScrollBarUI{
	
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
		g.setColor(new Color(45, 45, 45));
		//g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
		g.fillRoundRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height, 7, 7);

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
		  color = new Color(75,75,75);
		  shadowColor = new Color(45,45,45);
		}else if(isThumbRollover()) {
		  color = new Color(120,120,120);
		  shadowColor = new Color(70,70,70);
		}else {
		  color = new Color(100,100,100);
		  shadowColor = new Color(70,70,70);
		}

		//thumb shadow
		g.setColor(shadowColor);
		g.drawRoundRect(0, 0, w-1, h-1, 7, 7);
		//thumb color
		g.setColor(color);
		g.fillRoundRect(0, 0, w-1, h-1, 7, 7);			

		g.translate(-thumbBounds.x, -thumbBounds.y);
	}

}