package uk.ac.ebi.subs.fileupload.checksumcalculator.service;

import uk.ac.ebi.subs.fileupload.checksumcalculator.exception.ErrorMessages;
import uk.ac.ebi.subs.fileupload.checksumcalculator.exception.FileNotFoundException;
import uk.ac.ebi.subs.repository.model.fileupload.File;
import uk.ac.ebi.subs.repository.model.fileupload.FileStatus;
import uk.ac.ebi.subs.repository.repos.fileupload.FileRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static uk.ac.ebi.subs.fileupload.checksumcalculator.exception.ErrorMessages.FILE_IN_ILLEGAL_STATE_MESSAGE;

/**
 * This service checks if the file is available with the given ID in the repository.
 * It also checks if the file is in the correct state for calculating it checksum.
 * If all validation successful, then it calculates the checksum of the file and
 * updates the {@link File} in the repository. It also updates its status.
 */
public class ChecksumCalculator {

    private String tusFileID;

    private FileRepository fileRepository;

    private File fileToCompute;

    public ChecksumCalculator(FileRepository fileRepository, String tusFileID) {
        this.fileRepository = fileRepository;
        this.tusFileID = tusFileID;
    }

    private File getFileToCompute() {
        if (fileToCompute == null) {
            if (tusFileID == null) {
                throw new IllegalStateException(ErrorMessages.TUS_ID_NULL);
            }
            this.fileToCompute = fileRepository.findByGeneratedTusId(tusFileID);
        }

        return fileToCompute;
    }

    public boolean validateFile() {
        return isFileExists() && isFileReadyForComputeChecksum();
    }

    private boolean isFileExists() {
        if (getFileToCompute() == null) {
            throw new FileNotFoundException(tusFileID);
        }

        return true;
    }

    private boolean isFileReadyForComputeChecksum() {
        if (!FileStatus.READY_FOR_CHECKSUM.equals(getFileToCompute().getStatus())) {
            throw new IllegalStateException(
                    String.format(FILE_IN_ILLEGAL_STATE_MESSAGE, fileToCompute.getFilename()));
        }

        return true;
    }

    public String calculateMD5() throws IOException, InterruptedException {
        String checksum = "";
        String commandForComputeMD5OnLSF = "md5sum " + getFileToCompute().getTargetPath();

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

    public void updateFileWithChecksum(String checksum) {
        getFileToCompute();
        fileToCompute.setChecksum(checksum);
        fileToCompute.setStatus(FileStatus.READY_FOR_ARCHIVE);

        fileRepository.save(fileToCompute);
    }
}
