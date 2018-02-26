package uk.ac.ebi.subs.fileupload.checksumcalculator.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MD5Calculator {

    public static String computeMD5Checksum(String filePath) throws IOException, InterruptedException {
        String checksum = "";
        String commandForComputeMD5OnLSF = "md5sum " + filePath;

        StringBuffer output = new StringBuffer();

        java.lang.Runtime rt = java.lang.Runtime.getRuntime();
        Process process = rt.exec(commandForComputeMD5OnLSF);
        process.waitFor();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line = "";
        while ((line = reader.readLine())!= null) {
            output.append(line + "\n");
        }

        String[] checksumArray = output.toString().split(" ");

        if (checksumArray.length > 0) {
            checksum = checksumArray[0];
        } else {
            throw new RuntimeException("Something went wrong with checksum calculation");
        }

        return checksum;
    }
}
