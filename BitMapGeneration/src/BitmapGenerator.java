import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
    public static String generateBitmapBase64Json(String text, String fontFile, int width,int hight) {
    
    	String  ret="";
	String fontFilePath= getFontPath(fontFile);
	
	
	JSONArray jsonArray = new JSONArray();

	try {
	    // Load font from file
	 Font font = Font.createFont(Font.TRUETYPE_FONT, new 		File(fontFilePath)).deriveFont(Font.PLAIN, width);

	// Create a dummy image to calculate text dimensions
	BufferedImage dummyImage = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_BINARY);
	Graphics2D dummyGraphics = dummyImage.createGraphics();

	FontMetrics metrics = dummyGraphics.getFontMetrics(font);
	int textWidth = metrics.stringWidth(text);
	int textHeight = metrics.getHeight();

	// Force width to 8 pixels per character and height to 16 pixels
	int bitmapWidth = Math.max(width, textWidth);
	int bitmapHeight = Math.max(hight, textHeight);

	// Scale down the font if text height is too large
	if (textHeight > bitmapHeight) {
		float scaleFactor = (float) bitmapHeight / textHeight;
		font = font.deriveFont(font.getSize2D() * scaleFactor);

	}
             
            // Create an image with calculated dimensions
            BufferedImage image = new BufferedImage(bitmapWidth, bitmapHeight, BufferedImage.TYPE_BYTE_BINARY);
            Graphics2D graphics = image.createGraphics();
            graphics.setFont(font);
            graphics.drawString(text, 0, metrics.getAscent());

	    // Generate bitmap data with 8-bit width rows
	    StringBuilder bitmapData = new StringBuilder();
	    StringBuilder printedBitmap = new StringBuilder();
	    int byteCounter = 0;
	    int lines_added =0;
	   
	   for (int y = 0; y < bitmapHeight; y++) {
		int row = 0;
		StringBuilder rowPrint = new StringBuilder();
		StringBuilder rowBitMapData = new StringBuilder();
		boolean hasNonZeroPixel = false;
		for (int x = 0; x < bitmapWidth; x++) {
			int pixel = image.getRGB(x, y) == -16777216 ? 0 : 1; 
			// Black = 0, White = 1
			row = (row << 1) | pixel;  
			rowPrint.append(pixel == 1 ? "1" : "0"); 
			if (pixel == 1) {
				hasNonZeroPixel = true;
			}
			if ((x + 1) % 8 == 0) {
				rowBitMapData.append(String.format("%02X, ", row));
				byteCounter++;
				row = 0;
			}
		}
		if (bitmapWidth % 8 != 0) {
			row <<= (8 - (bitmapWidth % 8));
			rowBitMapData.append(String.format("%02X, ", row));
			byteCounter++;
		}
		if (hasNonZeroPixel) { // Only store rows with at least one non-zero pixel
			printedBitmap.append(rowPrint).append("\n");
			String hexString = rowBitMapData.toString().replaceAll("\\s|0x|,", ""); // Remove
			byte[] byteArray = new byte[hexString.length() / 2];
			for (int i = 0; i < hexString.length(); i += 2) {
				byteArray[i / 2] = (byte) Integer.parseInt(hexString.substring(i, i + 2), 16);
			}
			// Encode byte array to Base64 for a row
			String baseRow64Encoded = Base64.getEncoder().encodeToString(byteArray);	
			
			
			// add data into json object 
			jsonArray.put(baseRow64Encoded);
			
			lines_added++;
			
			
		}
	}
	// Print the bitmap visually
	JSONObject jsonObject = new JSONObject();
	jsonObject.put("bitmap", jsonArray);
	jsonObject.put("height", lines_added); // Example height value
	jsonObject.put("width", bitmapWidth);  // Example width value
	return jsonObject.toString(4); // Pretty print with indentation

    }
    catch (IOException | FontFormatException e) {
            System.err.println("Error processing font or writing header file: " + e.getMessage());
    }
    return ret;
}
public static String getBitmap(String text, String fontFile, int width,int hight) {
    
    	String fontFilePath= getFontPath(fontFile);
	String ret="";
	try {
	    // Load font from file
	 Font font = Font.createFont(Font.TRUETYPE_FONT, new File(fontFilePath)).deriveFont(Font.PLAIN, hight);

	// Create a dummy image to calculate text dimensions
	BufferedImage dummyImage = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_BINARY);
	Graphics2D dummyGraphics = dummyImage.createGraphics();

	FontMetrics metrics = dummyGraphics.getFontMetrics(font);
	int textWidth = metrics.stringWidth(text);
	int textHeight = metrics.getHeight();

	// Force width to 8 pixels per character and height to 16 pixels
	int bitmapWidth = Math.max(width, textWidth);
	int bitmapHeight = Math.max(hight, textHeight);

	// Scale down the font if text height is too large
	if (textHeight > bitmapHeight) {
		float scaleFactor = (float) bitmapHeight / textHeight;
		font = font.deriveFont(font.getSize2D() * scaleFactor);

	}
             
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

public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: java BitmapGenerator <text> <fontFile> <size>");
            return;
        }
        String text = args[0];
        String fontFil = args[1];
        int size=14;
	size = Integer.parseInt(args[2]);
	int higt=16;
	higt = Integer.parseInt(args[3]);
	
        String base64Encoded=generateBitmapBase64Json(text, fontFil, size,higt);
        String bitMap=getBitmap(text, fontFil, size,higt);
        System.out.println(base64Encoded);
        System.out.println(bitMap);
        
    }
}

