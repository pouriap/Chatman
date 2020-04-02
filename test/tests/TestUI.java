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

package tests;

import com.pouria.chatman.gui.CMTextArea;
import org.jsoup.nodes.Entities;
import org.junit.Test;

import javax.swing.*;

import static org.junit.Assert.assertEquals;


public class TestUI {

	@Test
	public void testEditorPaneTextSetAndGet(){
		JEditorPane editorPane = new JEditorPane();
		CMTextArea textArea = new CMTextArea(editorPane);
		String txt = "<div>as df سل ام &lt;<tml?!#@)$(*&%+</div>";
		txt = Entities.escape(txt);
		textArea.setText(txt);
		assertEquals(txt, textArea.getText());
	}

}
