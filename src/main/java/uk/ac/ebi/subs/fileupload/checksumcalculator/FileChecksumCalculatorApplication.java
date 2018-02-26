package uk.ac.ebi.subs.fileupload.checksumcalculator;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationPidFileWriter;
import uk.ac.ebi.subs.fileupload.checksumcalculator.service.ChecksumCalculator;
import uk.ac.ebi.subs.repository.repos.fileupload.FileRepository;

/**
 * This is a command line Spring Boot application
 * for calculating the checksum for a given file.
 */
@SpringBootApplication
public class FileChecksumCalculatorApplication implements CommandLineRunner {

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
		if (args.length > 0) {
			String fileId = args[0];
			ChecksumCalculator checksumCalculator = new ChecksumCalculator(fileRepository, fileId);
			checksumCalculator.validateFile();

			String checksum = checksumCalculator.calculateMD5();

			checksumCalculator.updateFileWithChecksum(checksum);
		}
	}
}
