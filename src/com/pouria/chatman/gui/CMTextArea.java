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
	}

	public String getText(){
		String html = editorPane.getText();
		Document doc = Jsoup.parse(html);
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
