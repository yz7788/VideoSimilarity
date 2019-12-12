package edu.usc;

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
  private Map<String, double[]> distanceplotMap = new HashMap<>();////
  private Map<String, int[]> top10IndexesMap = new HashMap<>();
  public Map<String, double[]> getDistances(String queryImagesPath) {
    long startTime = System.currentTimeMillis();
    String databaseImagesPath = "./static/database_video_images";
    Mat[] queryDescriptors = new Mat[150];
    File queryImagesFolder = new File(queryImagesPath);
    for (File imageFile: queryImagesFolder.listFiles()) {
      String[] parts = imageFile.getName().split("\\.");
      if (!parts[1].equals("jpg")) continue;
      getDescriptors(imageFile, queryDescriptors);//get descriptor of imageFile(n) to queryDescriptors[n]
    }
    System.out.println("--------query extracted, time: " + ((System.currentTimeMillis() - startTime) / 1000.0) + "--------");

    Map<String, Mat[]> databaseDescriptorMap = new HashMap<>();
    File databaseImagesFolder = new File(databaseImagesPath);
    for (File folder: databaseImagesFolder.listFiles()) {//databaseDescriptorMap: {folder name: flower, [descriptor of flower_1, descriptor of flower_2, descriptor of flower_3, ...]  }
      if (!folder.isDirectory()) continue;
      String folderName = folder.getName();
      //PriorityQueue<double, int> pQueue = new PriorityQueue<double, int>(); 
      TaFileStorage matSaver = new TaFileStorage();
      matSaver.open("./static/database_video_features/"+folderName+"_features.xml");
      Mat[] descriptorsArr = new Mat[600];
      //databaseDescriptorMap.put(folderName, new Mat[600]);
      for (File imageFile: folder.listFiles()) {
        String[] parts = imageFile.getName().split("\\.");
        if (!parts[1].equals("jpg")) continue;
        //getDescriptors(imageFile, databaseDescriptorMap.get(folderName));
        String fileName = parts[0];
        int index = getNumFromStr(fileName.substring(fileName.length() - 3));
        descriptorsArr[index] = matSaver.readMat(folderName+"_"+index);
      }
      databaseDescriptorMap.put(folderName, descriptorsArr);
      System.out.println("--------database " + folderName + " extracted, time: " + ((System.currentTimeMillis() - startTime) / 1000.0) + "--------");
    }

    DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
    Map<String, double[]> distanceMap = new HashMap<>();
    for (String folderName: databaseDescriptorMap.keySet()) {
      int[] top10Indexes = new int[10];
      double minDistance = Integer.MAX_VALUE;
      int minStartIndex = 0;
      Mat[] databaseDescriptors = databaseDescriptorMap.get(folderName);
      List<Mat> window = new ArrayList<>();
      for (int i = 0; i < 150; i++) {//window[n]: databaseDescriptors[n](the descriptor of flower_1 in flower folder)
        window.add(databaseDescriptors[i]);
      }
      double[] distanceplot = new double[450];
      for (int i = 150; i < 600; i++) {//
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
        int k = 0;
        int kMax = (i-150 >= 10 ? 10 : i-149);
        while (k < kMax && avgDistance >= distanceplot[top10Indexes[k]]) {
        	k += 1;
        }
        if (k < 10) {
        	for (int h = 9; h > k; h--) {
        		top10Indexes[h] = top10Indexes[h-1];
        	}
        	top10Indexes[k] = i - 150;
        }
        distanceplot[i-150] = avgDistance;////
        if (avgDistance < minDistance) {
          minDistance = avgDistance;
          minStartIndex = i - 150;
        }
        if (i >= 600) break;
        window.add(databaseDescriptors[i]);
        window.remove(0);
      }
      distanceMap.put(folderName, new double[] { minDistance, minStartIndex});
      this.distanceplotMap.put(folderName, distanceplot);
      this.top10IndexesMap.put(folderName, top10Indexes);
    }
    
    return distanceMap;
  }
  
  public Map<String, double[]> getAllDistances() {
	  return this.distanceplotMap;
  }
  
  public Map<String, int[]> getTop10Indexes() {
	  return this.top10IndexesMap;
  }

  private void getDescriptors(File imageFile, Mat[] descriptorsArr) {//e.g. in flower folder: descriptor[n] = descriptors of flower_n
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
