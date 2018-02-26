package uk.ac.ebi.subs.filechecksumcalculator.exception;

public class FileNotFoundException extends RuntimeException {

    public static final String FILE_NOT_FOUND_MESSAGE = "File not found with TUS ID: %s";

    public FileNotFoundException(String tusID) {
        super(String.format(FILE_NOT_FOUND_MESSAGE, tusID));
    }
}
