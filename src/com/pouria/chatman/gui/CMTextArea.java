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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;


public class CMTextArea {

	private JEditorPane editorPane;
	private String defaultHTML = "<html><head></head><body></body></html>";
	private StyleSheet css;
	private String textColor = "white";
	private String hiddenTextColor = "white";


	public CMTextArea(JEditorPane editorPane){
		this.editorPane = editorPane;
		editorPane.setText(defaultHTML);
	}

	public String getText(){
		String html = editorPane.getText();
		Document doc = Jsoup.parse(html);
		doc.outputSettings().prettyPrint(false);
		Element textDiv = doc.select("body").first();
		return (textDiv != null)? textDiv.html() : "";
	}

	public void setText(String text){
		this.setText(text, false);
	}

	public void appendText(String text){
		this.setText(text,true);
	}

	private void setText(String text, boolean append){

		String html = editorPane.getText();
		Document doc = Jsoup.parse(html);
		doc.outputSettings().prettyPrint(false);
		Element textDiv = doc.select("body").first();

		if(textDiv == null){
			throw new RuntimeException("could not get html element: " + "body");
		}

		if(append){
			textDiv.append(text);
		}
		else{
			textDiv.html(text);
		}


		editorPane.setText(doc.outerHtml());

	}

	public void setTheme(CMTheme theme){

		textColor = (theme.getTextAreasTheme().equals("dark"))? CMCSS.WHITE.val : CMCSS.BLACK.val;
		hiddenTextColor = (theme.getTextAreasTheme().equals("dark"))? CMCSS.BLACK.val : CMCSS.WHITE.val;

		//input caret color according to text color
		editorPane.setCaretColor(Color.getColor(textColor));

		if(css == null){
			css = new StyleSheet();
			css.importStyleSheet(getClass().getResource("TextArea.css"));
		}

		css.addRule("body{color:" + textColor + "}");

		this.setStyleSheet(css);

	}

	private void setStyleSheet(StyleSheet styleSheet){
		String currentText = this.getText();
		//add the stylesheet
		HTMLEditorKit kit = (HTMLEditorKit) editorPane.getEditorKit();
		kit.setStyleSheet(styleSheet);
		editorPane.setEditorKit(kit);
		//setting kit deleted everything so
		editorPane.setText(defaultHTML);
		this.setText(currentText);
	}

	public void showTime(){
		css.addRule(".time{font-size: 10px;");
		css.addRule(".time{color:" + textColor + "}");
		this.setStyleSheet(css);
	}

	public void hideTime(){
		css.addRule(".time{font-size: 0px;");
		css.addRule(".time{color:" + hiddenTextColor + "}");
		this.setStyleSheet(css);
	}


}
