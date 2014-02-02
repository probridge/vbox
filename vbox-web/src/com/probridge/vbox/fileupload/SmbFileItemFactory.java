package com.probridge.vbox.fileupload;

import java.io.File;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.FileCleaningTracker;

public class SmbFileItemFactory extends DiskFileItemFactory {
	public static final int DEFAULT_SIZE_THRESHOLD = 10240;
	private File repository;
	private int sizeThreshold = DEFAULT_SIZE_THRESHOLD;

	public FileItem createItem(String fieldName, String contentType,
			boolean isFormField, String fileName) {
		SmbFileItem result = new SmbFileItem(fieldName, contentType,
				isFormField, fileName, sizeThreshold, repository);
		FileCleaningTracker tracker = getFileCleaningTracker();
		if (tracker != null) {
			tracker.track(result.getTempFile(), this);
		}
		return result;
	}
}
