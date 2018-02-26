package uk.ac.ebi.subs.fileupload.checksumcalculator.service;

import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.fileupload.checksumcalculator.exception.ErrorMessages;
import uk.ac.ebi.subs.fileupload.checksumcalculator.exception.FileNotFoundException;
import uk.ac.ebi.subs.repository.model.fileupload.File;
import uk.ac.ebi.subs.repository.model.fileupload.FileStatus;
import uk.ac.ebi.subs.repository.repos.fileupload.FileRepository;

import java.io.IOException;

import static uk.ac.ebi.subs.fileupload.checksumcalculator.exception.ErrorMessages.FILE_IN_ILLEGAL_STATE_MESSAGE;

/**
 * This service checks if the file is available with the given ID in the repository.
 * It also checks if the file is in the correct state for calculating it checksum.
 * If all validation successful, then it calculates the checksum of the file and
 * updates the {@link File} in the repository. It also updates its status.
 */
@Service
public class ChecksumCalculatorService {

    private String tusFileID;

    private FileRepository fileRepository;

    private File fileToCompute;

    public ChecksumCalculatorService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
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

    public boolean validateFile(String tusFileID) {
        this.tusFileID = tusFileID;
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
        return MD5Calculator.computeMD5Checksum(getFileToCompute().getTargetPath());
    }

    public void updateFileWithChecksum(String checksum) {
        getFileToCompute();
        fileToCompute.setChecksum(checksum);
        fileToCompute.setStatus(FileStatus.READY_FOR_ARCHIVE);

        fileRepository.save(fileToCompute);
    }
}
