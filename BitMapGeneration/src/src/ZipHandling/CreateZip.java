import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class CreateZip {

    /**
     * Recursively zips the contents of sourceFolderPath into the zipPath.
     *
     * @param sourceFolderPath the folder to be zipped
     * @param zipPath          the output zip file path
     * @return true if the folder was zipped successfully, false otherwise
     */
    public static boolean zipFolder(Path sourceFolderPath, Path zipPath) {
        boolean success = false;
        // Create the ZIP output stream
        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            // Walk the file tree starting at the source folder
            Files.walkFileTree(sourceFolderPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // Create a zip entry relative to the source folder
                    Path relativePath = sourceFolderPath.relativize(file);
                    ZipEntry zipEntry = new ZipEntry(relativePath.toString().replace("\\", "/"));
                    zs.putNextEntry(zipEntry);
                    // Copy the file content into the zip output stream
                    Files.copy(file, zs);
                    zs.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    // For directories (except the root), add an entry to the ZIP file.
                    if (!sourceFolderPath.equals(dir)) {
                        Path relativePath = sourceFolderPath.relativize(dir);
                        ZipEntry zipEntry = new ZipEntry(relativePath.toString().replace("\\", "/") + "/");
                        zs.putNextEntry(zipEntry);
                        zs.closeEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            success = true;
        } catch (IOException e) {
            System.err.println("Error occurred while zipping the folder: " + e.getMessage());
            e.printStackTrace();
        }
        return success;
    }

    /**
     * Recursively deletes a folder and its contents.
     *
     * @param folderPath the folder to be deleted
     * @throws IOException if an I/O error occurs
     */
    public static void deleteFolder(Path folderPath) throws IOException {
        Files.walkFileTree(folderPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void HandleZip(String source,String dstZipFile) {
        
        
        // Specify the folder to be zipped and the destination zip file.
     
        Path sourceFolder = Paths.get(source);
        Path zipFile = Paths.get(dstZipFile);
       
        // Zip the folder and check the returned status.
        boolean zipSuccess = zipFolder(sourceFolder, zipFile);
        if (zipSuccess) {
            System.out.println("Folder zipped successfully!");
            try {
                // Delete the source folder after successful zipping.
                deleteFolder(sourceFolder);
                System.out.println("Source folder deleted successfully.");
            } catch (IOException e) {
                System.err.println("Error occurred while deleting the folder: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Zipping the folder did not complete successfully. Source folder not deleted.");
        }
    }
}


