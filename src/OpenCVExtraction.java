import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.xfeatures2d.SIFT;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.core.DMatch;
import org.opencv.core.MatOfDMatch;
import java.io.*;
import java.util.*;

public class OpenCVExtraction {
  public Map<String, double[]> getDistances(String queryImagesPath) {
    long startTime = System.currentTimeMillis();
    String databaseImagesPath = "../static/database_video_images";
    Mat[] queryDescriptors = new Mat[150];
    File queryImagesFolder = new File(queryImagesPath);
    for (File imageFile: queryImagesFolder.listFiles()) {
      String[] parts = imageFile.getName().split("\\.");
      if (!parts[1].equals("jpg")) continue;
      getDescriptors(imageFile, queryDescriptors);
    }
    System.out.println("--------query extracted, time: " + ((System.currentTimeMillis() - startTime) / 1000.0) + "--------");

    Map<String, Mat[]> databaseDescriptorMap = new HashMap<>();
    File databaseImagesFolder = new File(databaseImagesPath);
    for (File folder: databaseImagesFolder.listFiles()) {
      if (!folder.isDirectory()) continue;
      String folderName = folder.getName();
      databaseDescriptorMap.put(folderName, new Mat[600]);
      for (File imageFile: folder.listFiles()) {
        String[] parts = imageFile.getName().split("\\.");
        if (!parts[1].equals("jpg")) continue;
        getDescriptors(imageFile, databaseDescriptorMap.get(folderName));
      }
      System.out.println("--------database " + folderName + " extracted, time: " + ((System.currentTimeMillis() - startTime) / 1000.0) + "--------");
    }

    DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
    Map<String, double[]> distanceMap = new HashMap<>();
    for (String folderName: databaseDescriptorMap.keySet()) {
      double minDistance = Integer.MAX_VALUE;
      int minStartIndex = 0;
      Mat[] databaseDescriptors = databaseDescriptorMap.get(folderName);
      List<Mat> window = new ArrayList<>();
      for (int i = 0; i < 150; i++) {
        window.add(databaseDescriptors[i]);
      }
      for (int i = 150; i <= 600; i++) {
        double clipDistanceSum = 0;
        for (int j = 0; j < 150; j++) {
          MatOfDMatch matches = new MatOfDMatch();
          matcher.match(queryDescriptors[j], window.get(j), matches);
          DMatch[] matchArr = matches.toArray();
          double imageDistanceSum = 0;
          for (DMatch match: matchArr) {
            imageDistanceSum += match.distance;
          }
          clipDistanceSum += (matchArr.length == 0 ? 0 : imageDistanceSum / matchArr.length);
        }
        double avgDistance = clipDistanceSum / 150;
        if (avgDistance < minDistance) {
          minDistance = avgDistance;
          minStartIndex = i - 150;
        }
        if (i >= 600) break;
        window.add(databaseDescriptors[i]);
        window.remove(0);
      }
      distanceMap.put(folderName, new double[] { minDistance, minStartIndex});
    }
    return distanceMap;
  }

  private void getDescriptors(File imageFile, Mat[] descriptorsArr) {
    Mat image = Imgcodecs.imread(imageFile.getPath(), Imgcodecs.IMREAD_LOAD_GDAL);
    MatOfKeyPoint keyPoints = new MatOfKeyPoint();
    SIFT detector = SIFT.create(10);
    detector.detect(image, keyPoints);
    Mat descriptors = new Mat();
    detector.compute(image, keyPoints, descriptors);
    String[] parts = imageFile.getName().split("\\.");
    String fileName = parts[0];
    descriptorsArr[getNumFromStr(fileName.substring(fileName.length() - 3))] = descriptors;
  }

  private int getNumFromStr(String s) {
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) != '0') return Integer.valueOf(s.substring(i)) - 1;
    }
    return 0;
  }
}
