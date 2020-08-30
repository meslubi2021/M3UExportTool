package com.company;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Created by Gamma on 4/30/2014.
 */
class CopyFilesToStick {

	public static void copy_files_using_file_channels(File source, File dest) {

		try {
			try (FileChannel inputChannel = new FileInputStream(source).getChannel();
					FileChannel outputChannel = new FileOutputStream(dest).getChannel()) {
				outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void copy_http(String source, File dest) {
		try (BufferedInputStream in = new BufferedInputStream(new URL(source).openStream());
				FileOutputStream fileOutputStream = new FileOutputStream(dest)) {
			byte dataBuffer[] = new byte[1024];
			int bytesRead;

			while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
				fileOutputStream.write(dataBuffer, 0, bytesRead);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void copy_files_using_default_method(File source, File dest) {
		try {
			Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
