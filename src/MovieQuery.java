import org.jfree.ui.RefineryUtilities;
import org.opencv.core.Core;
import java.io.*;
import java.util.*;
import org.opencv.core.Mat;

public class MovieQuery {
  public static void main(String[] args) {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

    RGBToImage converter = new RGBToImage();
    OpenCVExtraction extractor = new OpenCVExtraction();

    String queryVideoPath = "/Users/wujiachen/Desktop/zhuzai/576project/MovieQuery/static/query_videos/second";
    String queryImagesPath = "/Users/wujiachen/Desktop/zhuzai/576project/MovieQuery/static/query_images_second";
    converter.convert(queryVideoPath, queryImagesPath);
    Map<String, double[]> distanceMap = extractor.getDistances(queryImagesPath);
    
    for (String folderName: distanceMap.keySet()) {
      double[] pair = distanceMap.get(folderName);
      System.out.println(folderName + ": " + pair[0] + ", " + (int)pair[1]);
    }
    
    Plot chart = new Plot("Measurement of similarity",
            "Similarity between different parts of two video",extractor.getDistancesPlot() );
    chart.pack( );          
    RefineryUtilities.centerFrameOnScreen( chart );          
    chart.setVisible( true ); 
  }
}
