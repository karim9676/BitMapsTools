import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.FontFormatException;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Base64;
import org.json.JSONObject;
import org.json.JSONArray;

public class BitmapGenerator {

    public static String getFontPath(String fileName) {
        return "../fonts/" + fileName + ".ttf";
    }

    /**
     * Creates an image with the text scaled so that its bounding box exactly fits within
     * the desiredHeight (taking into account an optional margin).
     *
     * The algorithm:
     * 1. Measure the text’s bounding box using a temporary TextLayout.
     * 2. Compute a scaling factor so that (boundingBoxHeight * scaleFactor) equals (desiredHeight - 2*margin).
     * 3. Apply an optional horizontal scaling (e.g. 1.2x) by using an AffineTransform.
     * 4. Recompute the text bounds with the final font.
     * 5. Create an image with width = (boundingBoxWidth + 2*margin) and height = desiredHeight.
     * 6. Draw the text at an offset that aligns the bounding box into the image.
     */
    private static BufferedImage createTextImage(String text, String fontFile, int initialFontSize, int desiredHeight,int margin,double scaleFactor,double horizontalScaleFactor)
            throws IOException, FontFormatException {
        String fontFilePath = getFontPath(fontFile);
        // Load the base font
        Font baseFont = Font.createFont(Font.TRUETYPE_FONT, new File(fontFilePath))
                            .deriveFont(Font.PLAIN, initialFontSize);

        // Create a temporary image for measurement
        BufferedImage tempImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2dTemp = tempImg.createGraphics();
        g2dTemp.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FontRenderContext frc = g2dTemp.getFontRenderContext();

        // Measure text bounds using TextLayout (bounds can be more accurate than ascent+descent)
        TextLayout layout = new TextLayout(text, baseFont, frc);
        Rectangle2D bounds = layout.getBounds();  // Note: bounds.getY() is often negative.
        double textBoundHeight = bounds.getHeight();

        // Use a small margin (so text does not touch image borders)
       
        // Compute vertical scaling factor so that the scaled bounding box fits the available height.
        double verticalScaleFactor = (desiredHeight - 2.0 * margin) / textBoundHeight;
        Font scaledFont = baseFont.deriveFont((float) (baseFont.getSize2D() * verticalScaleFactor));

        // Optionally apply horizontal scaling.
        //double horizontalScaleFactor = 1.5; // Adjust this factor to widen each character
        AffineTransform at = new AffineTransform();
        at.scale(horizontalScaleFactor, scaleFactor);
        Font finalFont = scaledFont.deriveFont(at);

        // Recompute text layout and its bounds using the final font.
        layout = new TextLayout(text, finalFont, frc);
        Rectangle2D finalBounds = layout.getBounds();
        // The width and height of the drawn text.
        int textWidth = (int) Math.ceil(finalBounds.getWidth());
        int textHeight = (int) Math.ceil(finalBounds.getHeight());

        // The final image dimensions:
        int finalImageWidth = textWidth + 2 * margin;
        int finalImageHeight = desiredHeight;  // we force the image height

        // Create the final image (using ARGB for quality)
        BufferedImage image = new BufferedImage(finalImageWidth, finalImageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Fill background with white.
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, finalImageWidth, finalImageHeight);
        // Set font and color.
        g2d.setFont(finalFont);
        g2d.setColor(Color.BLACK);

        // Compute the drawing offsets.
        // The TextLayout bounds' x and y indicate where the text drawing starts relative to (0,0).
        // To ensure the entire text appears in the image, we shift by -finalBounds.getX() horizontally,
        // and vertically we use: margin - finalBounds.getY() so that the top of the bounds is at 'margin'.
        float offsetX = margin - (float) finalBounds.getX();
        float offsetY = margin - (float) finalBounds.getY();

        // Draw the text.
        g2d.drawString(text, offsetX, offsetY);
        g2d.dispose();
        g2dTemp.dispose();

        return image;
    }

    public static String getBitmap(String text, String fontFile, int initialFontSize, int desiredHeight,int margin,double scaleFactor,double horizontalScaleFactor) {
        try {
            BufferedImage image = createTextImage(text, fontFile, initialFontSize, desiredHeight,margin,scaleFactor,horizontalScaleFactor);
            int width = image.getWidth();
            int height = image.getHeight();
            StringBuilder printedBitmap = new StringBuilder();

            // Create a text-based bitmap output
            for (int y = 0; y < height; y++) {
                StringBuilder rowPrint = new StringBuilder();
                boolean hasNonZeroPixel = false;
                for (int x = 0; x < width; x++) {
                    int argb = image.getRGB(x, y);
                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >> 8) & 0xFF;
                    int b = argb & 0xFF;
                    int luminance = (r + g + b) / 3;
                    int pixel = luminance < 128 ? 1 : 0;
                    rowPrint.append(pixel == 1 ? "#" : ".");
                    if (pixel == 1) {
                        hasNonZeroPixel = true;
                    }
                }
                if (hasNonZeroPixel) {
                    printedBitmap.append(rowPrint).append("\n");
                }
            }
            return printedBitmap.toString();
        } catch (IOException | FontFormatException e) {
            System.err.println("Error processing font: " + e.getMessage());
            return "";
        }
    }

    public static String generateBitmapToHeader(String text, String fontFile, int initialFontSize, int desiredHeight,int margin,double scaleFactor,double horizontalScaleFactor) {
        JSONArray jsonArray = new JSONArray();
        int linesAdded = 0;
        try {
             BufferedImage image = createTextImage(text, fontFile, initialFontSize, desiredHeight,margin,scaleFactor,horizontalScaleFactor);
            int width = image.getWidth();
            int height = image.getHeight();

            for (int y = 0; y < height; y++) {
                int bitAccumulator = 0;
                int bitsInAccumulator = 0;
                boolean hasNonZeroPixel = false;
                java.io.ByteArrayOutputStream rowBytes = new java.io.ByteArrayOutputStream();

                for (int x = 0; x < width; x++) {
                    int argb = image.getRGB(x, y);
                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >> 8) & 0xFF;
                    int b = argb & 0xFF;
                    int luminance = (r + g + b) / 3;
                    int pixel = luminance < 128 ? 1 : 0;
                    if (pixel == 1) {
                        hasNonZeroPixel = true;
                    }
                    bitAccumulator = (bitAccumulator << 1) | pixel;
                    bitsInAccumulator++;
                    if (bitsInAccumulator == 8) {
                        rowBytes.write(bitAccumulator);
                        bitAccumulator = 0;
                        bitsInAccumulator = 0;
                    }
                }
                if (bitsInAccumulator > 0) {
                    bitAccumulator = bitAccumulator << (8 - bitsInAccumulator);
                    rowBytes.write(bitAccumulator);
                }
                if (hasNonZeroPixel) {
                    byte[] byteArray = rowBytes.toByteArray();
                    String baseRow64Encoded = Base64.getEncoder().encodeToString(byteArray);
                    jsonArray.put(baseRow64Encoded);
                    linesAdded++;
                }
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("bitmap", jsonArray);
            jsonObject.put("height", linesAdded);
            jsonObject.put("width", image.getWidth());
            return jsonObject.toString(4);
        } catch (IOException | FontFormatException e) {
            System.err.println("Error processing font: " + e.getMessage());
            return "";
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java BitmapGenerator <text> <fontFile> <initialFontSize> <desiredHeight>");
            return;
        }

        String text = args[0];
        String fontFile = args[1];
        //int initialFontSize = 1;
        int initialFontSize = Integer.parseInt(args[2]);
        int desiredHeight = Integer.parseInt(args[3]);
        int margin=2;
        margin = Integer.parseInt(args[4]);
       
        // Parse a double value from the 5th command line argument
        double scaleFactor = 1.0;
        scaleFactor = Double.parseDouble(args[5]);
        double horizontalScaleFactor = 1.5;
        horizontalScaleFactor= Double.parseDouble(args[6]);
        
        
       /* if(desiredHeight == 16)
        	desiredHeight=20;
	else if(desiredHeight == 8) {
		initialFontSize=2;
        	desiredHeight=12;
        }*/ 	
        String bitmapOutput = getBitmap(text, fontFile, initialFontSize, desiredHeight,margin,scaleFactor,horizontalScaleFactor);
        System.out.println("Bitmap Representation:");
        System.out.println(bitmapOutput);

        String jsonOutput = generateBitmapToHeader(text, fontFile, initialFontSize, desiredHeight,margin,scaleFactor,horizontalScaleFactor);
        System.out.println("JSON Output:");
        System.out.println(jsonOutput);
    }
}

