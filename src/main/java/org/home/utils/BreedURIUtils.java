package org.home.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.IOException;

public class BreedURIUtils {
    
    private static final Pattern namePattern; 
    private static final String URL_DELIMITER = "/";
    private static final String GROUP_NAME = "name";

    static {
         // Match expression after word "breeds" and between two URL delimiters
        String breedNameRegEx = String.format("breeds\\%1$s(?<%2$s>.*)\\%1$s", URL_DELIMITER, GROUP_NAME);
        namePattern = Pattern.compile(breedNameRegEx);
    }
    
    // Get Breed name from the URL
    public static String  getBreedName(String uri) {
       try{
            Matcher matcher = namePattern.matcher(uri);
            if(matcher.find()) {
                return matcher.group(GROUP_NAME);
            } else {
                return uri;
            }
       } catch (Exception ex) {
           ex.printStackTrace();
       }
       return null;
    }

    // Get Breed image name from the URL
    public static String getImageName(String uri) {
        String[] urlParts = uri.split(URL_DELIMITER);
        return urlParts[urlParts.length - 1];
    }

    // Get Breed image as a byte array
    public static CompletionStage<byte[]> downloadImage(String image) {

        CompletionStage<byte[]> stage = CompletableFuture.supplyAsync(() -> {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            try (InputStream stream = new URL(image).openStream()){
                
                byte[] chunk = new byte[4096];
                int bytesRead;
                
                while ((bytesRead = stream.read(chunk)) > 0) {
                    outputStream.write(chunk, 0, bytesRead);
                }

            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }

            return outputStream.toByteArray();
        });

        return stage;
    }
}
