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
package com.pouria.chatman.connection;

import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.util.Args;

import java.io.*;

/**
 *
 * @author pouriap
 */
public class FileBodyWithProgress extends FileBody{
	
	private final File file;
	private long sentBytes;
	private boolean isComplete = false;
	private FileUploadCallback callback = null;
	private int callbackCallInterval = 1000;
	private final long fileSize;
	
	public FileBodyWithProgress(final File file){
		super(file);
		this.file = file;
		fileSize = file.length();
	}
	
	public FileBodyWithProgress(final File file, final ContentType contentType, final String filename){
		super(file, contentType, filename);
		this.file = file;
		fileSize = file.length();
	}
	
	public FileBodyWithProgress(File file, ContentType contentType) {
		super(file, contentType);
		this.file = file;
		fileSize = file.length();
	}
	
    @Override
    public void writeTo(final OutputStream out) throws IOException {
        Args.notNull(out, "Output stream");
        try (InputStream in = new FileInputStream(this.file)) {
			startProgressTracking();
            final byte[] tmp = new byte[4096];
            int l;
            while ((l = in.read(tmp)) != -1) {
                out.write(tmp, 0, l);
				sentBytes += tmp.length;
            }
            out.flush();
			isComplete = true;
        }
    }
	
	public int getSentPercent(){
		int percent = (int)((float)sentBytes*100/(float)fileSize);
		return percent;
	}
	
	/**
	 * Sets a callback to be called every 'interval' after file upload has started
	 * @param callback callback to be called
	 * @param interval interval in milliseconds
	 */
	public void setProgressCallback(FileUploadCallback callback, int interval){
		this.callback = callback;
		this.callbackCallInterval = interval;
	}
	
	private void startProgressTracking(){
		
		if(callback == null){
			return;
		}
		
		Runnable r= () -> {
			while(!isComplete){
				try{
					Thread.sleep(callbackCallInterval);
				}catch(Exception e){}
				callback.call(getSentPercent());
			}
			callback.call(100);
		};
		
		Thread th = new Thread(r, "File-Upload-Progress-Tracker");
		th.start();
	}
	
}
