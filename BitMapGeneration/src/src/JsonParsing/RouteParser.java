import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;

public class RouteParser {


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

		// Ensure the base directory exists.
		Path baseDir = Paths.get(basePath);
		if (!Files.exists(baseDir)) {
			Files.createDirectories(baseDir);
		}
	    Path masterFilePath = baseDir.resolve("master_route.txt");
		try (FileWriter masterFile = new FileWriter(masterFilePath.toFile())) 
		{

	        for (JsonNode busNode : rootNode) {
	            JsonNode routeNode = busNode.get("route");
	            String routeNumber = routeNode.get("routeNumber").asText();
	            masterFile.write(routeNumber + "\n");
			   // Create a directory for this route under the base directory.
                    Path routeDir = baseDir.resolve(routeNumber);
                    if (!Files.exists(routeDir)) {
                        Files.createDirectories(routeDir);
                    }
					Path routeDetailsPath = routeDir.resolve("route_details.txt");
                    try (FileWriter routeFile = new FileWriter(routeDetailsPath.toFile())) 		
                    {
				        routeFile.write("Ver:" + busNode.get("version").asText() + "\n");
				        routeFile.write("Src:" + routeNode.get("source").asText() +",");
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
				                while (screenTypes.hasNext()) { // Example: front, rear, side, internal
				                    Map.Entry<String, JsonNode> screenEntry = screenTypes.next();
				                    String screenType = screenEntry.getKey();
				                    if (screenType.equals("front")) 
				                        screenType = "0";
				                    else if (screenType.equals("side")) 
				                        screenType = "1";
				                    else if (screenType.equals("rear")) 
				                        screenType = "2";
				                    else if (screenType.equals("internal")) 
				                        screenType = "3";
				                    JsonNode screenData = screenEntry.getValue();
				                    if (screenData.has("format") && screenData.has("texts")) {
				                        String format = screenData.get("format").asText();
				                        JsonNode textsNode = screenData.get("texts");
				                        if (format.equals("three")) 
				                            format = "3";
				                        else if (format.equals("two")) 
				                            format = "2";
				                        else if (format.equals("single")) 
				                            format = "1";
				                        routeFile.write("L:" + language + ",");
				                        routeFile.write("D:" + screenType + ",");
				                        routeFile.write("F:" +format+ "\n");
				                        Iterator<Map.Entry<String, JsonNode>> textItems = textsNode.fields();
				                        while (textItems.hasNext()) {
				                            String fileName =   language+"_"+screenType;      
				                            Map.Entry<String, JsonNode> textEntry = textItems.next();
				                            String label = textEntry.getKey();
				                            if(label.equals("sideText")) {
				                            	 label = "0";
				                            }
				                            else if (label.equals("upperHalfText")) { 
				                            	 label = "1";
				                            }
				                            else if (label.equals("lowerHalfText")) {
				                            	 label = "2";
				                            }
				                            else if (label.equals("text")) {
				                            	 label = "3";
				                            }
				                            else {
				                            	 label = "3"; // default
				                            }
				                            fileName +="_"+label;
				                            JsonNode textData = textEntry.getValue();
				                            if (textData.has("scrollType")) {
				                                String ScrollType= textData.get("scrollType").asText();
				                                if(ScrollType.equals("Right To Left"))
				                                    ScrollType="0"; // right to left
				                                else if(ScrollType.equals("Left To Right"))
				                                    ScrollType="1"; // left to right    
				                                else if(ScrollType.equals("Fixed")) 
				                                    ScrollType="2"; // fixed      
				                                else     
				                                    ScrollType="0"; // default
				                                fileName +="_"+ScrollType;
				                                //routeFile.write("ST" + ":" + textData.get("scrollType").asText() + ",");
				                            }
				                            if (textData.has("position")) {
				                                String Position = textData.get("position").asText();
				                                if(Position.equals("Right"))
				                                   Position="0"; // right to left
				                                else if(Position.equals("Left"))
				                                    Position="1"; // left to right    
				                                else if(Position.equals("Center")) 
				                                    Position="2"; // fixed      
				                                else     
				                                    Position="0"; // default
				                              
				                                fileName +="_"+Position;
                
				                            }
				                            if (textData.has("scrollSpeed")) {
				                                fileName +="_"+textData.get("scrollSpeed").asText();
				                                
				                            }
				                            if (textData.has("fontSize")) {
				                                fileName +="_"+textData.get("fontSize").asText();
				                               
				                            }
				                            if (textData.has("text")) {
				                                String textValue = textData.get("text").asText();				              
				                            }

				                            if (textData.has("bitmap")) {
				                                JsonNode bitmapNode = textData.get("bitmap");
				                                fileName  += ".bin";
				                                routeFile.write("FILE" + ":" +fileName);
				                                					                                saveBitmap(bitmapNode,basePath,routeNumber,fileName);
				                                fileName="";
				                                routeFile.write("\n");
				                            }
				                        }
				                    }
				                }
				            }
				        } else {
				            routeFile.write("No display configurations available.\n");
				        }

			            routeFile.close();
		            }
		        }
		        masterFile.close();
		        //routeFile.write("master router created Successfullt.\n");   

		    }
           
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveBitmap(JsonNode bitmapNode,String basePath,String routeNumber, String fileName) {
        if (bitmapNode == null || bitmapNode.get("bitmap").asText().isEmpty()) {
            return;
        }

        String[] base64Rows = bitmapNode.get("bitmap").asText().split(",");
        int width = bitmapNode.get("width").asInt();
        int height = bitmapNode.get("height").asInt();
        byte[] decodedBytes = decodeBitmap(base64Rows);

        if (decodedBytes.length == 0) 
            return;

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ByteBuffer headerBuffer = ByteBuffer.allocate(8);
            headerBuffer.putInt(width);
            headerBuffer.putInt(height);
            outputStream.write(headerBuffer.array());
            outputStream.write(decodedBytes);
            
            
            Path directoryPath = Paths.get(basePath, routeNumber);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }
            
                // Create the full file path inside the route directory.
            Path filePath = directoryPath.resolve(fileName);

            
            
            Files.write(filePath, outputStream.toByteArray());
            System.out.println("Saved bitmap with header: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] decodeBitmap(String[] base64Rows) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (String row : base64Rows) {
                byte[] decodedRow = Base64.getDecoder().decode(row);
                outputStream.write(decodedRow);
            }
            return outputStream.toByteArray();
        } catch (Exception e) {
            System.err.println("Invalid Base64 encoding detected, skipping bitmap.");
            return new byte[0];
        }
    }
}
