import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.FontFormatException;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;  // For the Exception handling

public class RouteParser {

    /**
     * Creates an image with the text scaled to fit the desired height and optionally width, with bold styling.
     */
    public static class ScriptResult {
    
		private final int exitCode;
		private final String output;
		private final String errorOutput;

		public ScriptResult(int exitCode, String output, String errorOutput) {
		    this.exitCode = exitCode;
		    this.output = output;
		    this.errorOutput = errorOutput;
		}

		public int getExitCode() {
		    return exitCode;
		}

		public String getOutput() {
		    return output;
		}

		public String getErrorOutput() {
		    return errorOutput;
		}
	}

    public static class ScriptParams {
        String text;
        String fontPath;
        int size;
        int offsetX;
        int offsetY;
        int spacing;
        boolean printOutput;
        int imgHeight;
        String style;
        String bin_filename;

        // Constructor with default values
        public ScriptParams(String text, String fontPath,String binfile) {
            this.text = text;
            this.fontPath = fontPath;
            this.size = 12;           // Default size
            this.offsetX = 2;         // Default offset-x
            this.offsetY = -2;        // Default offset-y
            this.spacing = 1;         // Default spacing
            this.printOutput = true;  // Default print
            this.imgHeight = 16;      // Default img-height
            this.style = "regular";   // Default style
            this.bin_filename=binfile;
        }
         public ScriptParams(String text, String fontPath) {
            this.text = text;
            this.fontPath = fontPath;
            this.size = 12;           // Default size
            this.offsetX = 2;         // Default offset-x
            this.offsetY = -2;        // Default offset-y
            this.spacing = 1;         // Default spacing
            this.printOutput = true;  // Default print
            this.imgHeight = 16;      // Default img-height
            this.style = "regular";   // Default style
        }
    }
	public static ScriptResult runPythonScript(ScriptParams params) {
		try {
		    List<String> commandList = new ArrayList<>();
		    commandList.add("python3");
		    commandList.add("bit.py");
		    commandList.add(params.text);
		    commandList.add("-f");
		    commandList.add(params.fontPath);
		    commandList.add("-s");
		    commandList.add(String.valueOf(params.size));
		    commandList.add("--offset-x");
		    commandList.add(String.valueOf(params.offsetX));
		    commandList.add("--offset-y");
		    commandList.add(String.valueOf(params.offsetY));
		    commandList.add("--spacing");
		    commandList.add(String.valueOf(params.spacing));
		    if (params.printOutput) {
		        commandList.add("--print");
		    }
		    commandList.add("--img-height");
		    commandList.add(String.valueOf(params.imgHeight));
		    commandList.add("--style");
		    commandList.add(params.style);
		    if (!params.bin_filename.isBlank()) { 
				commandList.add("-o");
				commandList.add(params.bin_filename);
			}
		    String[] command = commandList.toArray(new String[0]);
		    
		    // Print the full command for debugging
		    String commandString = String.join(" ", command);
		    System.out.println("Executing command: " + commandString);

		    Process process = Runtime.getRuntime().exec(command);

		    // Capture standard output
		    StringBuilder outputBuilder = new StringBuilder();
		    BufferedReader reader = new BufferedReader(
		        new InputStreamReader(process.getInputStream()));
		    String line;
		    while ((line = reader.readLine()) != null) {
		        outputBuilder.append(line).append("\n");
		        System.out.println("Output: " + line); // Optional: keep printing if desired
		    }

		    // Capture error output
		    StringBuilder errorBuilder = new StringBuilder();
		    BufferedReader errorReader = new BufferedReader(
		        new InputStreamReader(process.getErrorStream()));
		    while ((line = errorReader.readLine()) != null) {
		        errorBuilder.append(line).append("\n");
		        System.err.println("Error: " + line); // Optional: keep printing if desired
		    }

		    int exitCode = process.waitFor();
		    System.out.println("Process exited with code: " + exitCode);

		    return new ScriptResult(exitCode, outputBuilder.toString(), errorBuilder.toString());

		} catch (IOException | InterruptedException e) {
		    e.printStackTrace();
		    return new ScriptResult(-1, "", e.getMessage());
		}
	}
 

    /**
     * Saves the bitmap with configuration from font_config, supporting en_16bit and en_8bit.
     */
    public static void saveBitmap(JsonNode textData, String language, String screenType, 
                                  String basePath, String routeNumber, String fileName, boolean splitRoute) {
            String text = textData.get("text").asText();
        
             
            if (text.isBlank()) 
            	return;
	   		
            String fontFile= getFontPath(language);
            System.out.println("fontFile        : " + fontFile);
            System.out.println("lg        	: " + language);
            System.out.println("screenType      : " + screenType);
			System.out.println("fileName      : " + fileName);
			
            if(screenType.equals("0")) {
                language="en";
                fontFile= getFontPath(language);
                System.out.println("for route number fontFile        : " + fontFile);
            }
			// Construct the full file path: basePath/routeNumber/fileName
			String fullFilePath = Paths.get(basePath, routeNumber, fileName).toString();
			System.out.println("Full output path: " + fullFilePath);
			ScriptParams params = new ScriptParams(text, fontFile,fullFilePath);
			
				// Update parameters from JSON if they exist
			if (textData.has("fontSize")) {
				params.size = textData.get("fontHeight").asInt();
				System.out.println("size      : " + params.size);
			}
			if (textData.has("x_offset")) {
				params.offsetX = textData.get("x_offset").asInt();
			}
			if (textData.has("y_offset")) {
				params.offsetY = textData.get("y_offset").asInt();
				System.out.println("y_offset      : " + params.offsetY);
			}
			if (textData.has("spacing")) {
				params.spacing = textData.get("spacing").asInt();
			}
			if (textData.has("fontHeight")) {
				params.imgHeight = textData.get("fontSize").asInt();
				System.out.println("imgHeight      : " + params.imgHeight);
			}
			if (textData.has("fontWeight")) {
				String fontWeight = textData.get("fontWeight").asText();
				params.style = fontWeight.replace("font-", ""); // Convert "font-regular" to "regular"
			}
			ScriptResult result = runPythonScript(params);

			// Use the captured output
			String scriptOutput = result.getOutput();
			String scriptErrors = result.getErrorOutput();

			if (result.getExitCode() == 0) {
				System.out.println("Script output:\n" + scriptOutput);
			} else {
				System.out.println("Script failed with errors:\n" + scriptErrors);
			}
			

    }
    public static String getBitmap(JsonNode textData, String language, String screenType, boolean splitRoute) {
    
            String text = textData.get("text").asText();
            if (text.isBlank()) 
            	return text;
	   		
            String fontFile= getFontPath(language);
            System.out.println("fontFile        : " + fontFile);
            System.out.println("lg        	: " + language);
            System.out.println("screenType      : " + screenType);
			
			
            if(screenType.equals("0")) {
                language="en";
                fontFile= getFontPath(language);
                System.out.println("for route number fontFile        : " + fontFile);
            }
			// Construct the full file path: basePath/routeNumber/fileName
			String fullFilePath="";
			ScriptParams params = new ScriptParams(text, fontFile);
				// Update parameters from JSON if they exist
			if (textData.has("fontSize")) {
				params.size = textData.get("fontHeight").asInt();
				System.out.println("size      : " + params.size);
			}
			if (textData.has("x_offset")) {
				params.offsetX = textData.get("x_offset").asInt();
			}
			if (textData.has("y_offset")) {
				params.offsetY = textData.get("y_offset").asInt();
				System.out.println("y_offset      : " + params.offsetY);
			}
			if (textData.has("spacing")) {
				params.spacing = textData.get("spacing").asInt();
			}
			if (textData.has("fontHeight")) {
				params.imgHeight = textData.get("fontSize").asInt();
				System.out.println("imgHeight      : " + params.imgHeight);
			}
			if (textData.has("fontWeight")) {
				String fontWeight = textData.get("fontWeight").asText();
				params.style = fontWeight.replace("font-", ""); // Convert "font-regular" to "regular"
			}
			ScriptResult result = runPythonScript(params);

			// Use the captured output
			String scriptOutput = result.getOutput();
			String scriptErrors = result.getErrorOutput();

			if (result.getExitCode() == 0) {
				System.out.println("Script output:\n" + scriptOutput);
				return scriptOutput;
			} else {
				System.out.println("Script failed with errors:\n" + scriptErrors);
				return scriptErrors;
			}
		
    }

    public static class Language {
        private final String code;
        private final String name;
        private final String fontFile;

        public Language(String code, String name, String fontFile) {
            this.code = code;
            this.name = name;
            this.fontFile = fontFile;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public String getFontFile() { return fontFile; }
    }

    public static final List<Language> AVAILABLE_LANGUAGES = List.of(
        new Language("en", "English", "NotoSans-Regular"),
        new Language("hi", "Hindi", "mangal"),
        new Language("ch", "Chattisgarh", "krutidev"),
        new Language("te", "Telugu", "gautami"),
        new Language("ta", "Tamil", "latha"),
        new Language("kn", "Kannada", "tunga"),
        new Language("ml", "Malayalam", "mangal"),
        new Language("mr", "Marathi", "mangal"),
        new Language("gu", "Gujarati", "shruti"),
        new Language("pa", "Punjabi", "raavi"),
        new Language("bn", "Bengali", "vrinda"),
        new Language("or", "Odia", "Kalinga"),
        new Language("as", "Assamese", "vrinda"),
        new Language("ur", "Urdu", "mangal"),
        new Language("sd", "Sindhi", "mangal"),
        new Language("ks", "Kashmiri", "mangal"),
        new Language("sa", "Sanskrit", "mangal"),
        new Language("ne", "Nepali", "mangal"),
        new Language("kok", "Konkani", "mangal"),
        new Language("mai", "Maithili", "mangal"),
        new Language("bho", "Bhojpuri", "mangal")
    );

    public static String getFontFileForLanguage(String langCode) {
        for (Language lang : AVAILABLE_LANGUAGES) {
            if (lang.getCode().equalsIgnoreCase(langCode)) {
                return lang.getFontFile();
            }
        }
        return "arial"; // Default font
    }

    public static String getFontPath(String langCode) {
        return "../fonts/" + getFontFileForLanguage(langCode) + ".ttf";
    }

    public static JsonNode parseJson(String json) { 
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(json);
        } catch (Exception e) {
            System.err.println("Failed to parse JSON: " + e.getMessage());
            e.printStackTrace();
        }
        return rootNode;
    }
    public static void ParseBitMapJson(String jsonString, String basePath) {
        try {
            JsonNode rootNode = parseJson(jsonString);
            Path baseDir = Paths.get(basePath);
            if (!Files.exists(baseDir)) Files.createDirectories(baseDir);

            try (FileWriter masterFile = new FileWriter(baseDir.resolve("master_route.txt").toFile())) {
                for (JsonNode busNode : rootNode) {
                    JsonNode routeNode = busNode.get("route");
                    String routeNumber = routeNode.get("splitRoute").asBoolean() 
                        ? routeNode.get("routeNumber1").asText() + "_" + routeNode.get("routeNumber2").asText()
                        : routeNode.get("routeNumber").asText();
                    masterFile.write(routeNumber + "\n");

                    Path routeDir = baseDir.resolve(routeNumber);
                    if (!Files.exists(routeDir)) Files.createDirectories(routeDir);

                    try (FileWriter routeFile = new FileWriter(routeDir.resolve("route_details.txt").toFile())) {
                        routeFile.write("Ver:" + busNode.get("version").asText() + "\n");
                        routeFile.write("Src:" + routeNode.get("source").asText() + ",");
                        routeFile.write("Sep:" + routeNode.get("separation").asText() + ",");
                        routeFile.write("Dst:" + routeNode.get("destination").asText() + ",");
                        routeFile.write("Via:" + routeNode.get("via").asText() + "\n");

                        JsonNode displayConfigNode = busNode.get("displayConfig");
                      
                        if (displayConfigNode != null) {
                            Iterator<Map.Entry<String, JsonNode>> languages = displayConfigNode.fields();
                            while (languages.hasNext()) {
                                Map.Entry<String, JsonNode> languageEntry = languages.next();
                                String language = languageEntry.getKey();
                                JsonNode screens = languageEntry.getValue();

                                Iterator<Map.Entry<String, JsonNode>> screenTypes = screens.fields();
                                while (screenTypes.hasNext()) {
                                    Map.Entry<String, JsonNode> screenEntry = screenTypes.next();
                                    String screenType = screenEntry.getKey();
                                    String screenCode;
                                    switch (screenType) {
                                        case "front": screenCode = "0"; break;
                                        case "side": screenCode = "1"; break;
                                        case "rear": screenCode = "2"; break;
                                        case "internal": screenCode = "3"; break;
                                        default: screenCode = screenType; break;
                                    }
                                    JsonNode screenData = screenEntry.getValue();

                                    if (screenData.has("format") && screenData.has("texts")) {
                                        String format = screenData.get("format").asText();
                                        String formatCode;
                                        switch (format) {
                                            case "three": formatCode = "3"; break;
                                            case "two": formatCode = "2"; break;
                                            case "single": formatCode = "1"; break;
                                            default: formatCode = format; break;
                                        }
                                        JsonNode textsNode = screenData.get("texts");

                                        routeFile.write("L:" + language + ",D:" + screenCode + ",F:" + formatCode + "\n");

                                        Iterator<Map.Entry<String, JsonNode>> textItems = textsNode.fields();
                                        while (textItems.hasNext()) {
                                            Map.Entry<String, JsonNode> textEntry = textItems.next();
                                            String label = textEntry.getKey();
                                            String labelCode;
                                            switch (label) {
                                                case "sideText": labelCode = "0"; break;
                                                case "upperHalfText": labelCode = "1"; break;
                                                case "lowerHalfText": labelCode = "2"; break;
                                                case "text": labelCode = "3"; break;
                                                default: labelCode = "3"; break;
                                            }
                                            JsonNode textData = textEntry.getValue();

                                            String fileName = language + "_" + screenCode + "_" + labelCode;
                                            if (textData.has("scrollType")) {
                                                String scrollType = textData.get("scrollType").asText();
                                                String scrollCode;
                                                switch (scrollType) {
                                                    case "Right To Left": scrollCode = "0"; break;
                                                    case "Left To Right": scrollCode = "1"; break;
                                                    case "Fixed": scrollCode = "2"; break;
                                                    default: scrollCode = "0"; break;
                                                }
                                                fileName += "_" + scrollCode;
                                            }
                                            if (textData.has("position")) {
                                                String position = textData.get("position").asText();
                                                String positionCode;
                                                switch (position) {
                                                    case "Right": positionCode = "0"; break;
                                                    case "Left": positionCode = "1"; break;
                                                    case "Center": positionCode = "2"; break;
                                                    default: positionCode = "0"; break;
                                                }
                                                fileName += "_" + positionCode;
                                            }
                                            if (textData.has("scrollSpeed")) fileName += "_" + textData.get("scrollSpeed").asText();
                                            if (textData.has("fontSize")) fileName += "_" + textData.get("fontSize").asText();
                                            fileName += ".bin";

                                            if (textData.has("text") && !textData.get("text").asText().isBlank()) {
                                                saveBitmap(textData, language, labelCode, basePath, 
                                                           routeNumber, fileName, routeNode.get("splitRoute").asBoolean());
                                                routeFile.write("FILE:" + fileName + "\n");
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            routeFile.write("No display configurations or font config available.\n");
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    	
   }
    public static boolean zipFolder(Path sourceFolderPath, Path zipPath) {
        boolean success = false;
        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            Files.walkFileTree(sourceFolderPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path relativePath = sourceFolderPath.relativize(file);
                    ZipEntry zipEntry = new ZipEntry(relativePath.toString().replace("\\", "/"));
                    zs.putNextEntry(zipEntry);
                    Files.copy(file, zs);
                    zs.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
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

    public static void HandleZip(String source, String dstZipFile) {
        Path sourceFolder = Paths.get(source);
        Path zipFile = Paths.get(dstZipFile);
        boolean zipSuccess = zipFolder(sourceFolder, zipFile);
        if (zipSuccess) {
            System.out.println("Folder zipped successfully!");
            try {
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

    public static void main(String[] args) {
        String jsonFile = args[0];
        System.out.println("Json file: " + args[0]);
        String jsonString = "";
        try {
            jsonString = new String(Files.readAllBytes(Paths.get(jsonFile)), StandardCharsets.UTF_8);
            System.out.println("JSON string from file: ");
            System.out.println(jsonString);
        } catch (IOException e) {
            System.out.println("JSON ERROR");
            e.printStackTrace();
        }
        String baseFolder = "../" + args[1];
        
        String zipFilePath = args[2];
        if (!jsonString.isEmpty()) {
            ParseBitMapJson(jsonString, baseFolder);
            HandleZip(baseFolder, zipFilePath);
        }
    }
}
