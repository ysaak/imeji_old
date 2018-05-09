package ysaak.imeji.utils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class FileUtils {

	public static String calculateHash(InputStream inputStream) throws NoSuchAlgorithmException, IOException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");

		//Create byte array to read data in chunks
		byte[] byteArray = new byte[1024];
		int bytesCount;

		//Read file data and update in message digest
		while ((bytesCount = inputStream.read(byteArray)) != -1) {
			digest.update(byteArray, 0, bytesCount);
		}

		//Get the hash's bytes
		byte[] bytes = digest.digest();

		BigInteger i = new BigInteger(1, digest.digest());
		return i.toString(16);
	}
}
