package com.newrelic.instrumentation.labs.sap.ximonitor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.newrelic.agent.config.ConfigFileHelper;
import com.newrelic.api.agent.NewRelic;

public class XIChannelLogger {
	
	private static XIChannelLogger INSTANCE = null;
	private static final int ONE_KB = 1024;
	private static final int ONE_MB = 1024 * ONE_KB;
	private static final int ONE_GB = 1024 * ONE_MB;
	private static final int DEFAULT_SIZE = 100 * ONE_KB;
	private static final Pattern allDigits = Pattern.compile("[0-9]+");
	
		
	private PrintWriter output;
	private File outputFile;
	private FileWriter fileWriter;
	private long rolloverSize = DEFAULT_SIZE;
	private long created;
	private long rotationInterval;
	private String lock = new String();
	private int fileCount = 0;
	private int maxFiles = 3;
	
	static {
		XICommunctionChannelConfig config = XIChannelReporter.currentChannelConfig;
		if(config == null) {
			XIChannelReporter.init();
			config = XIChannelReporter.currentChannelConfig;
		}
		INSTANCE = new XIChannelLogger(config);		

	}
	
	private static int getRolloverSize(String size) {
		if(onlyDigits(size)) {
			return Integer.parseInt(size);
		}
		String tmp = size.toLowerCase();
		if(tmp.endsWith("kb") || tmp.endsWith("k")) {
			tmp = tmp.replace("kb", "").replace("k", "");
			int i = Integer.parseInt(tmp);
			return i * ONE_KB;
		}
		
		if(tmp.endsWith("mb") || tmp.endsWith("m")) {
			tmp = tmp.replace("mb", "").replace("m", "");
			int i = Integer.parseInt(tmp);
			return i * ONE_MB;
		}
		if(tmp.endsWith("gb") || tmp.endsWith("g")) {
			tmp = tmp.replace("gb", "").replace("g", "");
			int i = Integer.parseInt(tmp);
			return i * ONE_GB;
		}
		return DEFAULT_SIZE;
	}
	
	private static boolean onlyDigits(String s) {
		Matcher matcher = allDigits.matcher(s);
		return matcher.matches();
	}
	
	private XIChannelLogger(XICommunctionChannelConfig config ) {
		try {
			
			String filename = config.getChannelLog();
			if(filename == null || filename.isEmpty()) {
				File agentdir = ConfigFileHelper.getNewRelicDirectory();
				outputFile = new File(agentdir, XIChannelReporter.log_file_name);
			} else {
				File file = new File(filename);
				if(file.isDirectory()) {
					outputFile = new File(file,XIChannelReporter.log_file_name);
				} else {
					outputFile = file;
				}
			}
			fileWriter = new FileWriter(outputFile);
			output = new PrintWriter(fileWriter);
			FileTime createTime = (FileTime) Files.getAttribute(outputFile.toPath(), "creationTime");
			if(createTime != null) {
				created = createTime.to(TimeUnit.MILLISECONDS);
			}
			NewRelic.getAgent().getLogger().log(Level.FINE, "Set output file creation at {0}", created);
			rotationInterval = config.getRolloverMinutes();
			NewRelic.getAgent().getLogger().log(Level.FINE, "Set rotation interval to {0} minutes", rotationInterval);
			
			String rollSize = config.getRolloverSize();
			rolloverSize = getRolloverSize(rollSize);
			NewRelic.getAgent().getLogger().log(Level.FINE, "Set rotation size to {0}", rolloverSize);
			
			Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new FileChecker(), 0L, 1L, TimeUnit.MINUTES);
		} catch (IOException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Error creating XIChannelLogger output file");
		}
	}
	
	public static void log(String message) {
		if(INSTANCE.output != null) {
			INSTANCE.writeMessage(message);
		}
	}
	
	protected void writeMessage(String message) {
		synchronized (lock) {
			output.println(message);
			output.flush();
		}
	}
	
	private class FileChecker implements Runnable {
		
		public void run() {
			long currentSize =  outputFile.length();
			boolean rotate = currentSize >= rolloverSize;
			
			if(!rotate && rotationInterval > 0) {
				long current = System.currentTimeMillis();
				if(current - created > rotationInterval) {
					NewRelic.getAgent().getLogger().log(Level.FINE, "Rotating XILog since current {0} - created {1} is greater than {2}", current, created, rotationInterval);
					rotate = true;
				}
			} else {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Rotating XILog since current size {0} is greater than {1}",currentSize,rolloverSize);
			}
			if (rotate) {
				int fileNum = fileCount % maxFiles + 1;
				fileCount++;
				if(fileCount == 10000) fileCount = 0;
				
				synchronized (lock) {
					File target = new File(outputFile.getParent(), outputFile.getName() + "." + fileNum);
					try {
						Files.copy(outputFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						NewRelic.getAgent().getLogger().log(Level.FINER, e, "Failed to copy existing file");
					}
					if (output != null) {
						try {
							output.close();
							fileWriter.close();
							fileWriter = new FileWriter(outputFile, false);
							fileWriter.flush();
							output = new PrintWriter(fileWriter);
							FileTime createTime = (FileTime) Files.getAttribute(outputFile.toPath(), "creationTime");
							if(createTime != null) {
								created = createTime.to(TimeUnit.MILLISECONDS);
							}
							NewRelic.getAgent().getLogger().log(Level.FINE, "Set output file creation at {0}", created);
						} catch (IOException e) {
							NewRelic.getAgent().getLogger().log(Level.FINER, e, "Failed to rotate existing file");
						}
					}
				} 
			} else {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Not rotating XILog");
			}
		}
	}

}
