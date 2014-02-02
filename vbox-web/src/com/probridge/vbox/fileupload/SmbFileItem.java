package com.probridge.vbox.fileupload;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;

public class SmbFileItem extends DiskFileItem {

	private static final long serialVersionUID = -753079934959519220L;

	/**
	 * The size of the item, in bytes. This is used to cache the size when a
	 * file item is moved from its original location.
	 */
	long size = -1;

	public SmbFileItem(String fieldName, String contentType,
			boolean isFormField, String fileName, int sizeThreshold,
			File repository) {
		super(fieldName, contentType, isFormField, fileName, sizeThreshold,
				repository);
	}
	
	@Override
	protected File getTempFile() {
		return super.getTempFile();
	}

	public void write(SmbFile smbFile) throws Exception {
		if (isInMemory()) {
			SmbFileOutputStream fout = null;
			try {
				fout = new SmbFileOutputStream(smbFile);
				fout.write(get());
			} finally {
				if (fout != null) {
					fout.close();
				}
			}
		} else {
			File outputFile = getStoreLocation();
			if (outputFile != null) {
				// Save the length of the file
				size = outputFile.length();
				BufferedInputStream in = null;
				BufferedOutputStream out = null;
				try {
					in = new BufferedInputStream(
							new FileInputStream(outputFile));
					out = new BufferedOutputStream(
							new SmbFileOutputStream(smbFile));
					IOUtils.copy(in, out);
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							// ignore
						}
					}
					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {
							// ignore
						}
					}
				}
			} else {
				/*
				 * For whatever reason we cannot write the file to disk.
				 */
				throw new FileUploadException(
						"Cannot write uploaded file to smb location!");
			}
		}
	}
}
