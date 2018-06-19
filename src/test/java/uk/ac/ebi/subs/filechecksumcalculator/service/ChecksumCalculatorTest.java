package uk.ac.ebi.subs.filechecksumcalculator.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.data.fileupload.FileStatus;
import uk.ac.ebi.subs.filechecksumcalculator.exception.ErrorMessages;
import uk.ac.ebi.subs.filechecksumcalculator.exception.FileNotFoundException;
import uk.ac.ebi.subs.repository.model.fileupload.File;
import uk.ac.ebi.subs.repository.repos.fileupload.FileRepository;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ChecksumCalculatorTest {

    private static final String TUS_FILE_ID = "ABC123";
    private static final String EXPECTED_MD5_CHECKSUM = "0f5c434bbcf15faee52bd3545972398e";
    private static final String TEST_FILE_FOR_CHECKSUM_CALCULATION = "test_file_for_checksum_calculation.txt";
    private static final String SUBMISSION_ID = "112233-aabbcc-223344";

    private ChecksumCalculator checksumCalculator;

    @MockBean
    private FileRepository fileRepository;

    private File fileToCompute;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        fileToCompute = createTestFile();
    }

    @Test
    public void whenTusFileIDIsNotSetOnTheService_ThenThrowsIllegalStateException() {
        this.thrown.expect(IllegalStateException.class);
        this.thrown.expectMessage(ErrorMessages.TUS_ID_NULL);

        checksumCalculator = new ChecksumCalculator(fileRepository, null);
        checksumCalculator.validateFile();
    }

    @Test
    public void whenFileNotExistsInRepository_ThenThrowsFileNotFoundException() {
        when(fileRepository.findByGeneratedTusId(TUS_FILE_ID)).thenReturn(null);

        this.thrown.expect(FileNotFoundException.class);
        this.thrown.expectMessage(String.format(FileNotFoundException.FILE_NOT_FOUND_MESSAGE, TUS_FILE_ID));

        checksumCalculator = new ChecksumCalculator(fileRepository, TUS_FILE_ID);
        checksumCalculator.validateFile();
    }

    @Test
    public void whenFileStatusIsNotCorrectForChecksumCalculation_ThenTrowsIllegalStateException() {
        fileToCompute.setStatus(FileStatus.UPLOADING);
        when(fileRepository.findByGeneratedTusId(TUS_FILE_ID)).thenReturn(fileToCompute);

        this.thrown.expect(IllegalStateException.class);
        this.thrown.expectMessage(
                String.format(ErrorMessages.FILE_IN_ILLEGAL_STATE_MESSAGE, fileToCompute.getFilename())
        );

        checksumCalculator = new ChecksumCalculator(fileRepository, TUS_FILE_ID);
        checksumCalculator.validateFile();
    }

    @Test
    public void whenServiceSetCorrectly_ThenChecksumGenerationWorks() throws IOException, InterruptedException {
        when(fileRepository.findByGeneratedTusId(TUS_FILE_ID)).thenReturn(fileToCompute);

        checksumCalculator = new ChecksumCalculator(fileRepository, TUS_FILE_ID);

        assertTrue(checksumCalculator.validateFile());

        String calculatedChecksum = checksumCalculator.calculateMD5();

        assertThat(calculatedChecksum, is(equalTo(EXPECTED_MD5_CHECKSUM)));
    }

    @Test
    public void whenServiceSetCorrectlyAndFileUpdatedWithCheckSum_ThenChecksumPersistedCorrectly() throws IOException, InterruptedException {
        when(fileRepository.findByGeneratedTusId(TUS_FILE_ID)).thenReturn(fileToCompute);

        checksumCalculator = new ChecksumCalculator(fileRepository, TUS_FILE_ID);
        checksumCalculator.validateFile();

        String calculatedChecksum = checksumCalculator.calculateMD5();
        checksumCalculator.updateFileWithChecksum(calculatedChecksum);

        assertThat(fileToCompute.getChecksum(), is(equalTo(EXPECTED_MD5_CHECKSUM)));
        assertThat(fileToCompute.getStatus(), is(equalTo(FileStatus.READY_FOR_ARCHIVE)));
    }

    private File createTestFile() {
        File file = new File();
        file.setFilename(TEST_FILE_FOR_CHECKSUM_CALCULATION);
        file.setStatus(FileStatus.READY_FOR_CHECKSUM);
        file.setSubmissionId(SUBMISSION_ID);

        ClassLoader classLoader = getClass().getClassLoader();
        String filePath = new java.io.File(classLoader.getResource(TEST_FILE_FOR_CHECKSUM_CALCULATION).getFile()).getAbsolutePath();


        file.setTargetPath(filePath);

        return file;
    }
}
