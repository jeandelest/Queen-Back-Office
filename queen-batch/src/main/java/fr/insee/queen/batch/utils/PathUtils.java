package fr.insee.queen.batch.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import fr.insee.queen.batch.config.BatchOption;
import fr.insee.queen.batch.exception.FolderException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Operation on paths
 * - isDirectoryExist
 * - isDirContainsFileExtension
 * - getListFileName
 * - isFileExist
 * - getExtensionByStringHandling
 *
 * @author Claudel Benjamin
 */
public class PathUtils {
    private static final Logger logger = LogManager.getLogger(PathUtils.class);

    private PathUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * This method is used to create all the directories from the FOLDER_IN property
     *
     * @param FOLDER_IN
     * @throws FolderException
     */
    public static void createFolderTreeIn(String FOLDER_IN) throws FolderException {
        if (PathUtils.isDirectoryExist(FOLDER_IN)) {
            if (!PathUtils.isDirectoryExist(FOLDER_IN + "/nomenclatures/json")) {
                try {
                    if (!PathUtils.isDirectoryExist(FOLDER_IN + "/nomenclatures/")) {
                        PathUtils.createDirectory(FOLDER_IN + "/nomenclatures");
                    }
                    if (!PathUtils.isDirectoryExist(FOLDER_IN + "/nomenclatures/json")) {
                        PathUtils.createDirectory(FOLDER_IN + "/nomenclatures/json");
                    }
                } catch (Exception e) {
                    throw new FolderException(String.format("Directory '%s/nomenclatures/json' has not been created", FOLDER_IN));
                }
            }
            if (!PathUtils.isDirectoryExist(FOLDER_IN + "/sample")) {
                try {
                    PathUtils.createDirectory(FOLDER_IN + "/sample");
                } catch (Exception e) {
                    throw new FolderException(String.format("Directory '%s/sample' has not been created", FOLDER_IN));
                }
            }
            if (!PathUtils.isDirectoryExist(FOLDER_IN + "/processing")) {
                try {
                    PathUtils.createDirectory(FOLDER_IN + "/processing");
                } catch (Exception e) {
                    throw new FolderException(String.format("Directory '%s/processing' has not been created", FOLDER_IN));
                }
            }
        } else {
            throw new FolderException(String.format("Directory '%s' doesn't exist", FOLDER_IN));
        }
    }

    /**
     * This method is used to create all the directories from the FOLDER_OUT property
     *
     * @param FOLDER_OUT
     * @throws FolderException
     */
    public static void createFolderTreeOut(String FOLDER_OUT) throws FolderException {
        if (PathUtils.isDirectoryExist(FOLDER_OUT)) {
            if (!PathUtils.isDirectoryExist(FOLDER_OUT + "/nomenclatures/json")) {
                try {
                    if (!PathUtils.isDirectoryExist(FOLDER_OUT + "/nomenclatures/")) {
                        PathUtils.createDirectory(FOLDER_OUT + "/nomenclatures");
                    }
                    PathUtils.createDirectory(FOLDER_OUT + "/nomenclatures/json");

                } catch (Exception e) {
                    throw new FolderException(String.format("Directory '%s/nomenclatures/json' has not been created", FOLDER_OUT));
                }
            }
            if (!PathUtils.isDirectoryExist(FOLDER_OUT + "/sample")) {
                PathUtils.createDirectory(FOLDER_OUT + "/sample");
            }
            if (!PathUtils.isDirectoryExist(FOLDER_OUT + "/extractdata")) {
                PathUtils.createDirectory(FOLDER_OUT + "/extractdata");
            }
        } else {
            throw new FolderException(String.format("Directory '%s' doesn't exist", FOLDER_OUT));
        }
    }

    /**
     * This method create all the directories for the extraction
     *
     * @param batchOption
     * @param out
     * @param campaignId
     */
    public static void createFolderTreeExtract(BatchOption batchOption, String out, String campaignId) {
        if (!PathUtils.isDirectoryExist(out + "/extractdata/" + campaignId)) {
            PathUtils.createDirectory(out + "/extractdata/" + campaignId);
        }
        if (batchOption.equals(BatchOption.EXTRACTDATA)) {
            PathUtils.createDirectory(out + "/extractdata/" + campaignId + "/differential");
            PathUtils.createDirectory(out + "/extractdata/" + campaignId + "/differential/data");
            PathUtils.createDirectory(out + "/extractdata/" + campaignId + "/differential/paradata");
        }
        if (batchOption.equals(BatchOption.EXTRACTDATACOMPLETE)) {
            PathUtils.createDirectory(out + "/extractdata/" + campaignId + "/complete");
            PathUtils.createDirectory(out + "/extractdata/" + campaignId + "/complete/data");
            PathUtils.createDirectory(out + "/extractdata/" + campaignId + "/complete/paradata");
        }
    }

    /**
     * This method get the current time for the naming of files
     *
     * @return the exact time in String format
     */
    public static String getTimestampForPath() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date dateNow = new Date();
        return formatter.format(dateNow);
    }

    /**
     * This method check if a directory contains an error file created after the step "clean and reset"
     *
     * @param pathToDirectory
     * @param fileType
     * @param extension
     * @return boolean
     */
    public static boolean isDirContainsErrorFile(Path pathToDirectory, String fileType, String extension) {
        boolean isDirContainsErrorFile = false;
        List<String> fileNames = getListFileName(pathToDirectory);
        for (int i = 0; i < fileNames.size(); i++) {
            if (fileNames.get(i).contains(fileType) && fileNames.get(i).contains(extension)) {
                isDirContainsErrorFile = true;
            }
        }
        return isDirContainsErrorFile;
    }

    /**
     * Check if a directory exists
     *
     * @param path path of directory
     * @return true if directory exists
     */
    public static boolean isDirectoryExist(String path) {
        File tmpDir = new File(path);
        return tmpDir.exists() && tmpDir.isDirectory();
    }

    /**
     * Check if a directory contains a file with a given extension
     *
     * @param directory directory to check
     * @return true if directory contains the given file extension
     */
    public static boolean isDirContainsFileExtension(final Path directory, String filename) {
        try {
            Iterator<Path> i = Files.newDirectoryStream(directory).iterator();
            while (i.hasNext()) {
                if (filename.equals(i.next().getFileName().toString())) {
                    return true;
                }
            }
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return false;
        }
        return false;
    }

    /**
     * get the list of filenames in a given directory
     *
     * @param directory directory to explore
     * @return the list of filenames in the directory
     */
    public static List<String> getListFileName(final Path directory) {
        List<String> listFileName = new ArrayList<>();
        try {
            Iterator<Path> i = Files.newDirectoryStream(directory).iterator();
            while (i.hasNext()) {
                String fileName = i.next().getFileName().toString();
                listFileName.add(fileName);
            }
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        return listFileName;
    }

    /**
     * Check if a file exists
     *
     * @param fileName fileName to check
     * @return true if file exists
     */
    public static boolean isFileExist(String fileName) {
        File tmpDir = new File(fileName);
        return tmpDir.exists() && tmpDir.isFile();
    }

    /**
     * get the extention of a given filename
     *
     * @param filename filename to check
     * @return the extention of the file
     */
    public static Optional<String> getExtensionByStringHandling(String filename) {
        return Optional.ofNullable(filename).filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf('.') + 1));
    }

    /**
     * get the file name without extention of a given filename
     *
     * @param filename filename to check
     * @return the filename without extention
     */
    public static String getFileNameWithoutExtension(String filename) {
        return filename.replaceFirst("[.][^.]+$", "");
    }

    /**
     * Moving a file to a destination
     *
     * @param file
     * @param destination
     */
    public static void moveFile(String file, String destination) {
        new File(file).renameTo(new File(destination));
    }

    public static void createDirectory(String path) {
        File sampleDir = new File(path);
        sampleDir.mkdir();
    }
}


