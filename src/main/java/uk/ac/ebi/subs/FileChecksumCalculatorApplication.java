package uk.ac.ebi.subs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationPidFileWriter;
import uk.ac.ebi.subs.repository.repos.fileupload.FileRepository;
import uk.ac.ebi.subs.filechecksumcalculator.service.ChecksumCalculator;

/**
 * This is a command line Spring Boot application
 * for calculating the checksum for a given file.
 */
@SpringBootApplication
public class FileChecksumCalculatorApplication implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileChecksumCalculatorApplication.class);

    private FileRepository fileRepository;

    public FileChecksumCalculatorApplication(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(FileChecksumCalculatorApplication.class);
		ApplicationPidFileWriter applicationPidFileWriter = new ApplicationPidFileWriter();
		springApplication.addListeners( applicationPidFileWriter );
		springApplication.run(args);
	}

	@Override
	public void run(String... args) throws Exception {
    	LOGGER.info("FileChecksumCalculatorApplication started executing.");
		if (args.length > 0) {
			String fileId = args[0];
			LOGGER.info("Checksum calculation started fro file id: {}", fileId);
			ChecksumCalculator checksumCalculator = new ChecksumCalculator(fileRepository, fileId);
			checksumCalculator.validateFile();

			String checksum = checksumCalculator.calculateMD5();

			LOGGER.info("Calculated checksum: {}", checksum);

			checksumCalculator.updateFileWithChecksum(checksum);
		}
	}
}
