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

import com.pouria.chatman.connection.AbstractPOSTHander;
import com.pouria.chatman.connection.CMFormDataParser;
import com.pouria.chatman.connection.HttpClient;
import com.pouria.chatman.connection.HttpServer;
import com.pouria.chatman.messages.*;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import org.junit.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import static org.junit.Assert.*;


public class TestMessagesSendAndReceive {

	static HttpServer server;
	static HttpClient client;
	static CMMessage receivedMessage;
	static int port = 7357;

	@After
	public void tearDown() throws Exception{
		server.stop();
	}

	@Before
	public void setUp() throws Exception{
		server = new HttpServer(port, new AbstractPOSTHander() {
			@Override
			public void handle(HttpServerExchange exchange, FormData formData) throws Exception{
				CMFormDataParser parser = new CMFormDataParser(formData);
				receivedMessage = parser.parseAsCMMessage();
			}
		});
		server.start();
	}

	@BeforeClass
	public static void beforeClass() throws Exception{
		client = new HttpClient(port, new String[]{"127.0.0.1"}, System.out::println);
		client.setServer("127.0.0.1");
	}

	@Test
	public void testFileMessage() throws Exception{

		server.stop();
		server = new HttpServer(port, new AbstractPOSTHander() {
			@Override
			public void handle(HttpServerExchange exchange, FormData formData) throws Exception{
				CMFormDataParser parser = new CMFormDataParser(formData);
				receivedMessage = parser.parseAsCMMessage();
				FileMessage fm = (FileMessage) receivedMessage;
				File savedFile = File.createTempFile("saved","cmtemp");
				Files.copy(fm.getFile().toPath(), savedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				receivedMessage = FileMessage.getNew(CMMessage.Direction.IN,fm.getSender(),fm.getFileName(),savedFile,fm.getSenderTheme(),0);
			}
		});
		server.start();

		File file = File.createTempFile("پیشوندـفارسی",".cmtest");
		byte[] fileContent = "this is the test file contents. که دارای حروف فارسی هم می باشد".getBytes();
		Files.write(file.toPath(),fileContent, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		CMMessage fileMsg = FileMessage.getNew(CMMessage.Direction.OUT, "sender", file.getName(), file, "senderTheme", 0);
		boolean sent = client.sendFile(file,fileMsg.getAsJSONString());
		assertTrue(sent);
		File receivedFile = ((FileMessage)receivedMessage).getFile();
		assertEquals(file.getName(),((FileMessage)receivedMessage).getFileName());
		assertArrayEquals(Files.readAllBytes(file.toPath()), Files.readAllBytes(receivedFile.toPath()));

	}

	@Test
	public void testTextMessage() throws Exception{
		CMMessage  textMsg = TextMessage.getNew(CMMessage.Direction.OUT, "sender","content","senderTheme",0 );
		testMessageIsSent(textMsg);
	}

	@Test
	public void testPingMessage(){
		PingMessage pingMsg = PingMessage.getNew(CMMessage.Direction.OUT,"senderTheme");
		testMessageIsSent(pingMsg);
	}

	@Test
	public void testShowGUIMessage(){
		ShowGUIMessage msg = ShowGUIMessage.getNew(CMMessage.Direction.OUT);
		testMessageIsSent(msg);
	}

	@Test
	public void testAbortShutdownMessage(){
		AbortShutdownMessage msg = AbortShutdownMessage.getNew(CMMessage.Direction.OUT, "sender", "senderTheme", 0);
		testMessageIsSent(msg);
	}

	@Test
	public void testRequestThemeMessage(){
		RequestThemeMessage msg = RequestThemeMessage.getNew(CMMessage.Direction.OUT, "themeName");
		testMessageIsSent(msg);
	}

	@Test
	public void testShutdownMessage(){
		ShutdownMessage msg = ShutdownMessage.getNew(CMMessage.Direction.OUT, "sender","senderTheme", 0);
		testMessageIsSent(msg);
	}

	@Test
	public void testThemeFileMessage(){
		ThemeFileMessage msg = ThemeFileMessage.getNew(CMMessage.Direction.OUT, "themeName", "themeDataBase64Encoded");
		testMessageIsSent(msg);
	}

	private void testMessageIsSent(CMMessage message){
		boolean sent = client.sendText(message.getAsJSONString());
		assertTrue(sent);
		assertEquals(receivedMessage.getAsJSONString(), message.getAsJSONString());
	}

}
