import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.FontFormatException;
import java.util.Base64;
import org.json.JSONObject;
import org.json.JSONArray;
public class BitmapGenerator {

    public static String getFontPath(String fileName) {
	    String path = "../fonts/" + fileName + ".ttf";
	    return path;
    }	
    
    
	public static String getBitmap(String text, String fontFile,  int initialFontSize, int desiredHeight) {

		String fontFilePath= getFontPath(fontFile);
		String ret="";
		try {
			// Load font from file
				// Load font from file
		 	Font font = Font.createFont(Font.TRUETYPE_FONT, new File(fontFilePath)).deriveFont(Font.PLAIN, initialFontSize);

			// 2. Create a dummy image to obtain initial FontMetrics.
			BufferedImage dummyImage = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_BINARY);
			Graphics2D dummyGraphics = dummyImage.createGraphics();
			FontMetrics metrics = dummyGraphics.getFontMetrics(font);

			int textWidth = metrics.stringWidth(text);
			int textHeight = metrics.getHeight();

			System.out.println("Initial FontMetrics:");
			System.out.println("Width: " + textWidth);
			System.out.println("Height: " + textHeight);

			// 3. Scale the font to meet the desired height.
			/*float scaleFactor = (float) desiredHeight / textHeight;
			font = font.deriveFont(font.getSize2D() * scaleFactor);
			*/
			// Update FontMetrics after scaling.
			metrics = dummyGraphics.getFontMetrics(font);
			textWidth = metrics.stringWidth(text);
			textHeight = metrics.getHeight();

			System.out.println("After scaling FontMetrics:");
			System.out.println("Width: " + textWidth);
			System.out.println("Height: " + textHeight);

			// 4. Define the final bitmap dimensions.
			// Optionally, force a minimum width per character (e.g., 8 pixels per character).
			int minWidth = 8 * text.length();
			int bitmapWidth = Math.max(minWidth, textWidth);
			int bitmapHeight = desiredHeight;  // Use the desired height




			System.out.println("Final bitmap dimensions:");
			System.out.println("Width: " + bitmapWidth);
			System.out.println("Height: " + bitmapHeight);

			// 5. Create the final image.
			// Create an image with calculated dimensions
			BufferedImage image = new BufferedImage(bitmapWidth, bitmapHeight, BufferedImage.TYPE_BYTE_BINARY);
			Graphics2D graphics = image.createGraphics();
			graphics.setFont(font);
			graphics.drawString(text, 0, metrics.getAscent());

			// Generate bitmap data with 8-bit width rows
			StringBuilder printedBitmap = new StringBuilder();
			int lines_added =0;

			for (int y = 0; y < bitmapHeight; y++) {
					StringBuilder rowPrint = new StringBuilder();
					boolean hasNonZeroPixel = false;
					for (int x = 0; x < bitmapWidth; x++) {
						int pixel = image.getRGB(x, y) == -16777216 ? 0 : 1; 
						// Black = 0, White = 1
						rowPrint.append(pixel == 1 ? "#" : "."); 
						if (pixel == 1) {
							hasNonZeroPixel = true;
						}

					}
					if (hasNonZeroPixel) { // Only store rows with at least one non-zero pixel
						printedBitmap.append(rowPrint).append("\n");
						lines_added++;
					}
			}
			return printedBitmap.toString();
	  	}
		catch (IOException | FontFormatException e) {
			System.err.println("Error processing font or writing header file: " + e.getMessage());	
	    }    
  	  return ret;
}
    
    public static String generateBitmapToHeader(String text, String fontFile, int initialFontSize, int desiredHeight) 
    {
    
	String  base64Encoded="";
	String fontFilePath= getFontPath(fontFile);
	JSONArray jsonArray = new JSONArray();

	try {
	    // Load font from file
	 	Font font = Font.createFont(Font.TRUETYPE_FONT, new File(fontFilePath)).deriveFont(Font.PLAIN, initialFontSize);

		// 2. Create a dummy image to obtain initial FontMetrics.
		BufferedImage dummyImage = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_BINARY);
		Graphics2D dummyGraphics = dummyImage.createGraphics();
		FontMetrics metrics = dummyGraphics.getFontMetrics(font);

		int textWidth = metrics.stringWidth(text);
		int textHeight = metrics.getHeight();

		System.out.println("Initial FontMetrics:");
		System.out.println("Width: " + textWidth);
		System.out.println("Height: " + textHeight);

		// 3. Scale the font to meet the desired height.
		float scaleFactor = (float) desiredHeight / textHeight;
		font = font.deriveFont(font.getSize2D() * scaleFactor);

		// Update FontMetrics after scaling.
		metrics = dummyGraphics.getFontMetrics(font);
		textWidth = metrics.stringWidth(text);
		textHeight = metrics.getHeight();

		System.out.println("After scaling FontMetrics:");
		System.out.println("Width: " + textWidth);
		System.out.println("Height: " + textHeight);

		// 4. Define the final bitmap dimensions.
		// Optionally, force a minimum width per character (e.g., 8 pixels per character).
		int minWidth = 8 * text.length();
		int bitmapWidth = Math.max(minWidth, textWidth);
		int bitmapHeight = desiredHeight;  // Use the desired height

		System.out.println("Final bitmap dimensions:");
		System.out.println("Width: " + bitmapWidth);
		System.out.println("Height: " + bitmapHeight);

		// 5. Create the final image.
		BufferedImage image = new BufferedImage(bitmapWidth, bitmapHeight, BufferedImage.TYPE_BYTE_BINARY);
		Graphics2D graphics = image.createGraphics();
		graphics.setFont(font);
		graphics.drawString(text, 0, metrics.getAscent());

		// Prepare to generate bitmap data row by row.
		int linesAdded =0;
		for (int y = 0; y < bitmapHeight; y++) {
				int row = 0;
				StringBuilder rowBitMapData = new StringBuilder();
				boolean hasNonZeroPixel = false;
				// Process each pixel in the row.
				for (int x = 0; x < bitmapWidth; x++) {
					// For TYPE_BYTE_BINARY, typically black is 0 (0xFF000000) and white is something else.
					int pixel = image.getRGB(x, y) == -16777216 ? 0 : 1; // Black = 0, non-black = 1
					row = (row << 1) | pixel;
					if (pixel == 1) {
						hasNonZeroPixel = true;
					}
					
					// Every 8 pixels, convert the accumulated bits to a hexadecimal byte.
					if ((x + 1) % 8 == 0) {
						rowBitMapData.append(String.format("%02X, ", row));
						row = 0;
					}
				}
				// If the row width isn’t a multiple of 8, pad the remaining bits.
				if (bitmapWidth % 8 != 0) {
					row <<= (8 - (bitmapWidth % 8));
					rowBitMapData.append(String.format("%02X, ", row));
				}
				
				// Only include rows that contain at least one "on" pixel.
				if (hasNonZeroPixel) {
					// Remove spaces and commas to get a clean hex string.
					String hexString = rowBitMapData.toString().replaceAll("\\s|,", "");
					// Convert hex string to byte array.
					byte[] byteArray = new byte[hexString.length() / 2];
					for (int i = 0; i < hexString.length(); i += 2) {
						byteArray[i / 2] = (byte) Integer.parseInt(hexString.substring(i, i + 2), 16);
					}
					// Base64 encode the byte array for this row.
					String baseRow64Encoded = Base64.getEncoder().encodeToString(byteArray);
					jsonArray.put(baseRow64Encoded);
					linesAdded++;
					
										
				}
			}
			//System.out.println(rowBitMapData);	
			// Print the bitmap visually
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("bitmap", jsonArray);
			jsonObject.put("height", linesAdded); // Example height value
			jsonObject.put("width", bitmapWidth);  // Example width value
			return jsonObject.toString(4); // Pretty print with indentation	
	 	}
		catch (IOException | FontFormatException e) {
		        System.err.println("Error processing font or writing header file: " + e.getMessage());
		}
    	return base64Encoded;
	}
	
	
	public static void main(String[] args) {
		if (args.length < 4) {
			System.out.println("Usage: java BitmapGenerator <text> <fontFile> <initialFontSize> <desiredHeight>");
			return;
		}

		String text = args[0];
		String fontFile = args[1];
		int initialFontSize = Integer.parseInt(args[2]);
		int desiredHeight = Integer.parseInt(args[3]); // desired height can be any value (e.g., between 8 and 16)

		String jsonOutput = generateBitmapToHeader(text, fontFile, initialFontSize, desiredHeight);
		System.out.println("\nJSON Output:");
		System.out.println(jsonOutput);
		
		
		
		String bitMap=getBitmap(text, fontFile, initialFontSize,desiredHeight);
        	System.out.println(bitMap);
	}
}

