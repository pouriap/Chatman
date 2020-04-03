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

import com.pouria.chatman.CMHistory;
import com.pouria.chatman.classes.AbstractSQLPagination;
import com.pouria.chatman.messages.CMMessage;
import com.pouria.chatman.messages.DisplayableMessage;
import com.pouria.chatman.messages.TextMessage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class TestHistory {

	private static File testFile = new File("test.sqlite");
	private static CMHistory history;
	private static List<DisplayableMessage> messagesToSave = new ArrayList<>();

	@AfterClass
	public static void afterClass() throws Exception{
		if(testFile.isFile()){
			boolean deleted = testFile.delete();
			if(!deleted){
				throw new Exception("test file wasn't deleted");
			}
		}
	}

	@BeforeClass
	public static void beforeClass() throws Exception{
		history = new CMHistory(testFile.getName());
		history.createDBIfNotExist();
	}

	private void saveSampleHistory(){

		// April 3, 2020 9:43:45 AM
		long day1Time1 = 1585890825000L;
		// April 3, 2020 5:43:45 PM
		long day1Time2 = 1585919625000L;
		// April 4, 2020 9:43:45 AM
		long day2Time1 = 1585977225000L;
		// April 4, 2020 7:43:45 PM
		long day2Time2 = 1586013225000L;

		DisplayableMessage day1msg1 = TextMessage.getNew(CMMessage.Direction.OUT,
			"sender", "day1Time1", "theme", day1Time1);
		DisplayableMessage day1msg2 = TextMessage.getNew(CMMessage.Direction.OUT,
			"sender", "day1Time2", "theme", day1Time2);
		DisplayableMessage day2msg1 = TextMessage.getNew(CMMessage.Direction.OUT,
			"sender", "day2Time1", "theme", day2Time1);
		DisplayableMessage day2msg2 = TextMessage.getNew(CMMessage.Direction.OUT,
			"sender", "day2Time2", "theme", day2Time2);

		messagesToSave.add(day1msg1);
		messagesToSave.add(day1msg2);
		messagesToSave.add(day2msg1);
		messagesToSave.add(day2msg2);

		history.save(messagesToSave);
	}

	@Test
	public void testEverything() throws Exception{

		saveSampleHistory();

		final List<String> daysTexts = new ArrayList<>();
		AbstractSQLPagination pagination = new AbstractSQLPagination(1, testFile.getName(),
			"SELECT text FROM chat_sessions ORDER BY date DESC") {
			@Override
			protected void doPopulate(ResultSet row, int pageNumber) throws Exception{
				daysTexts.add(row.getString("text"));
			}
		};

		pagination.nextPage();
		//test if paginaiton works correctly
		assertEquals(daysTexts.size(),1);

		//first row is the latest day
		String day2TextActual = daysTexts.get(0);
		String day2TextExpected = messagesToSave.get(2).getAsHTMLString() +
			messagesToSave.get(3).getAsHTMLString();

		pagination.nextPage();
		//test if paginaiton works correctly
		assertEquals(daysTexts.size(),2);

		String day1TextActual = daysTexts.get(1);
		String day1TextExpected = messagesToSave.get(0).getAsHTMLString() +
			messagesToSave.get(1).getAsHTMLString();

		//test if everything was saved correctly
		assertEquals(day1TextExpected, day1TextActual);
		assertEquals(day2TextExpected, day2TextActual);

	}

}
