package uk.ac.ebi.subs.fileupload.checksumcalculator;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationPidFileWriter;
import uk.ac.ebi.subs.fileupload.checksumcalculator.service.ChecksumCalculatorService;

/**
 * This is a command line Spring Boot application
 * for calculating the checksum for a given file.
 */
@SpringBootApplication
public class FileChecksumCalculatorApplication implements CommandLineRunner {

	private ChecksumCalculatorService checksumCalculatorService;

	public FileChecksumCalculatorApplication(ChecksumCalculatorService checksumCalculatorService) {
		this.checksumCalculatorService = checksumCalculatorService;
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
			checksumCalculatorService.validateFile(fileId);

			String checksum = checksumCalculatorService.calculateMD5();

			checksumCalculatorService.updateFileWithChecksum(checksum);
		}
	}
}
