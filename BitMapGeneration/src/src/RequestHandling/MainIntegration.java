package src.RequestHandling.MainIntegration;

import src.BitmapHandling.BitmapGenerator;
import src.BitmapHandling.BitmapGenerator;
import src.JsonParsing.RouteParser;
import src.ZipHandling.CreateZip;
import org.json.JSONObject;

public class MainIntegration {
   
   
    public static void main(String[] args) {
        // Example input string (can include any language characters)
      
      	// Action 
      	
      	// Parameters mapping ..
      	
      	
      	// 3 actions 
      	
      	// Generate Bitmap 
      	// Get base64 string 
      	// Parsing and Zip the Json 
      	
      	String cmd = args[0];
      	int required_list=0;
      	
      	if(cmd.equals("GetBase64Bitmap")) {
      		required_list= 4;
      		int cmd_len=1;
      		if (args.length < required_list+cmd_len) {
      		
      			
			String text = args[1];
			String fontFile = args[2];
			int initialFontSize = Integer.parseInt(args[3]);
			int desiredHeight = Integer.parseInt(args[4]); // desired height can be any value (e.g., between 8 and 16)
			String jsonOutput = BitmapGenerator.generateBitmapToHeader(text, fontFile, initialFontSize, desiredHeight);
			System.out.println("\nJSON Output:");
			System.out.println(jsonOutput);
      
      		}
      	
      	
      	}
      	else if(cmd.equals("GetRawBitmap")) {
      		required_list= 4;
      		int cmd_len=1;
      		if (args.length < required_list+cmd_len) {
      		
	      		String text = args[1];
			String fontFile = args[2];
			int initialFontSize = Integer.parseInt(args[3]);
			int desiredHeight = Integer.parseInt(args[4]); // desired height can be any value (e.g., between 8 and 16)

			String bitMap=BitmapGenerator.getBitmap(text, fontFile, initialFontSize,desiredHeight);
			System.out.println("Bitmap Output:");
			System.out.println(bitMap);
			
			
      		}
      	}
      	else if(cmd.equals("ParseZipBitMap")) {
      		required_list= 4;
      		int cmd_len=1;
      		if (args.length < required_list+cmd_len) {
      			String jsonString = args[1];
			String baseFolder = "../"+args[2];
			String ZipFilePath = args[2];
      			RouteParser.ParseBitMapJson(jsonString,baseFolder);
      			CreateZip.HandleZip(baseFolder,ZipFilePath);
      			
      		}
      	}
      	
      	
    }
}

