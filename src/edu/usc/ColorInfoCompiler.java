package edu.usc;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

public class ColorInfoCompiler {
	private int width = 352;//352
	private int height = 288;//288
	private int clusterK = 4;
	private int[][] queryColorData;
	public ColorInfoCompiler(File queryFolder) {
		this.queryColorData = new int[queryFolder.listFiles().length-1][16];
		for(File file: queryFolder.listFiles()) {
			String[] parts = file.getName().split("\\.");
			if (!parts[1].equals("rgb")) continue;
			String fileName = parts[0];
			int i = getNumFromStr(fileName.substring(fileName.length()-3));
			int[] colors = extract(file);
			for (int j = 0; j < 16; j++) {
				this.queryColorData[i][j] = colors[j];
			}
		}
	}
	public double[] getDistances(String csvPath, int[] indexes) throws FileNotFoundException, IOException {
		//int[][] records = new int[][];
		ArrayList<ArrayList<Integer>> records = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(csvPath))){
			String line;
			while((line = br.readLine())!=null) {
				String[] values = line.split(",");
				ArrayList<Integer> colorinfo = new ArrayList<>();
				for (int i=0;i<values.length;i++) {
					 colorinfo.add(Integer.parseInt(values[i]));
				}
				records.add(colorinfo);
			}
		}
		
		int[][] baseColorData = new int[records.size()][];
		for(int i = 0; i < records.size(); i++) {
			baseColorData[i] = new int[records.get(i).size()];
			for(int j=0; j<records.get(i).size(); j++) {
				baseColorData[i][j] = records.get(i).get(j);
			}
		}
		double[] colorDistances = new double[indexes.length];
		for (int k = 0; k < indexes.length; k++) {
			int iStart = indexes[k];
			double dist = 0;
			for (int i = 0; i < this.queryColorData.length; i++) {
				dist += hausdorffDistance(this.queryColorData[i], baseColorData[i+iStart]);
			}
			dist /= this.queryColorData.length;
			colorDistances[k] = dist;
		}
		return colorDistances;
	}

	private double hausdorffDistance(int[] colorInfo1, int[] colorInfo2) {
		double[][] distances = new double[4][4];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				int[] color1 = Arrays.copyOfRange(colorInfo1, 4*i, 4*i+3);
				int[] color2 = Arrays.copyOfRange(colorInfo2, 4*j, 4*j+3);
				distances[i][j] = colorDistance(color1, color2);
			}
		}
		double maxOfMinXY = 0;
		double maxOfMinYX = 0;
		for(int i = 0; i < 4; i++) {
			double minX = Integer.MAX_VALUE;
			double minY = Integer.MAX_VALUE;
			for(int j = 0; j < 4; j++) {
				minX = Math.min(distances[i][j], minX);
				minY = Math.min(distances[j][i], minY);
			}
			maxOfMinXY = Math.max(maxOfMinXY, minX);
			maxOfMinYX = Math.max(maxOfMinYX, minY);
		}
		return Math.max(maxOfMinXY, maxOfMinYX);
	}

	private double colorDistance(final int[] P_1, final int[] P_2) {
		int r = (P_1[0] + P_2[0])/2;
		int R = P_1[0] - P_2[0];
		int G = P_1[1] - P_2[1];
		int B = P_1[2] - P_2[2];
		return Math.sqrt((2+r/256)*R*R+4*G*G+(2+(255-r)/256)*B*B);
	}

	public int[] extract(File file){
		int[] data = new int[4*this.clusterK];
		try{ 
			BufferedImage img = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
	        // size of each frame = width x height x 3 (RGB)
	        int frameLength = this.width*this.height*3;
	          
	         //File file = new File(imgPath);
	         // Creates a random access file stream to read from, open for reading only
	         RandomAccessFile raf = new RandomAccessFile(file, "r");
	         raf.seek(0);

	         long len = frameLength;
	         byte[] bytes = new byte[(int) len];
	         // Reads up to len bytes of data from this file into an array of bytes.
	         raf.read(bytes);

	         int ind = 0;
	         int[][][] imagePixel = new int[288][352][4];
	         /*  
	             the first element of the array represents x coordinate of the image
	             the second element of the array represents y coordinate of the image
	             the third element of the array:
	                 [0] R value of the pixel
	                 [1] G value of the pixel
	                 [2] B value of the pixel
	                 [3] used to store which k group will the pixel be assigned to later
	         */
	         for(int y = 0; y < this.height; y++)
	         {
	             for(int x = 0; x < this.width; x++)
	             {
	                 byte a = 0;
	                 byte r = bytes[ind];
	                 byte g = bytes[ind+this.height*this.width];
	                 byte b = bytes[ind+this.height*this.width*2];
	                  
	                 // The following codes will assign RGB value for each pixel of the frame to the
	                 // ImagePixel array
	                 imagePixel[y][x][0] = Math.abs(r); // assign r value
	                 imagePixel[y][x][1] = Math.abs(g); // assign g value
	                 imagePixel[y][x][2] = Math.abs(b); // assign b value
	                  
	                 // Derrick testing output This is the same as pixel (100,100)
	                 // 35300 = 100*352 + 100 = 35300
	                 /*if(ind == 35300) {
	                     System.out.println(ImagePixel[y][x][0]+", "+ImagePixel[y][x][1]+", "+ImagePixel[y][x][2]+"(integers)");
	                 }*/
	                  
	                 // int is 32 bits
	                 // 0xff000000 (hex) equals to 11111111000000000000000000000000
	                 // This is for the alpha channel
	                 // 0xff hex to bit --> 11111111 (255 in decimal)
	                 // (r & 0xff) will 
	                 int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
	                 // The following is for the argb format where alpha channel is present
	                 // int pix = ((a << 24) + (r << 16) + (g << 8) + b);
	                  
	                 // This will set the color for a pixel of the image at the x, y location
	                 // pix is a 32 bits integer
	                 img.setRGB(x,y,pix);
	                 ind++;
	             }
	         }
	         int max = 255;
	         int min = 0;
	         int k = this.clusterK; //Set up the number of group for the k-mean process
	         int kMean[][] = new int[k][5];
	         /* The kMean matrix is used to store the RGB coordinate for k centroids for k-mean method
	          * kMean[k][0] R value for the centroid k
	          * kMean[k][1] G value for the centroid k
	          * kMean[k][2] B value for the centroid k
	          */
	         int kResult[][] = new int[k][5];
	         /* The k_result matrix is used to store the k-mean method result after it converges
	          * kResult[k][0]  R value of group k from the k-mean result
	          * kResult[k][1]  G value of group k from the k-mean result
	          * kResult[k][2]  B value of group k from the k-mean result
	          * kResult[k][3]  total count of pixels in group k from the k-mean result
	          * kResult[k][4]  % of pixels in group k from the k-mean result
	          */
	          
	         // Assign random RGB value for k starting centroids, stored in k_mean[x][y]
	         for(int x = 0; x < k; x++)
	         {
	             for(int y = 0; y < 3; y++)
	             {
	                 kMean[x][y] = (int)(Math.random() * ((max - min) + 1));
	             }
	             // show the initial assign value
	             // System.out.println("The RGB value for K"+(x+1)+" group centroid is "
	             //      +kMean[x][0]+", "+kMean[x][1]+", "+kMean[x][2]);
	         }
	          
	         int z = 0;
	         int krun =1;
	         while (z < 1) {
	             // iterate all pixel(x,y) on the image and assign each pixel to the closest centroid
	              
	             // After the first run, set z = 1 to stop the loop if all pixels are assigned to the same group
	             // System.out.print("Run: "+krun);
	             if(krun>1)
	             {
	                 z=1;
	             }
	             for(int y = 0; y < this.height; y++)
	             {
	                 for(int x = 0; x < this.width; x++)
	                 {
	                     //Calculate the Euclidean distance
	                     double distance = 0;
	                     int kFinal = 0;
	                     for (int j = 0; j < k; j++)
	                     {
	                         double kDis;;
	                         kDis = Math.sqrt(
	                                 Math.pow(imagePixel[y][x][0]-kMean[j][0], 2)
	                                 + Math.pow(imagePixel[y][x][1]-kMean[j][1], 2)
	                                 + Math.pow(imagePixel[y][x][2]-kMean[j][2], 2)
	                                 );
	                         /*if (y == 100 && x== 100)
	                         {
	                             System.out.println("k_final is now "+k_final);
	                             System.out.print("distance now is "+(int)distance);
	                             System.out.println(" and k_dis now is "+(int)k_dis+" for group "+j);                
	                         }*/
	                          
	                         if (j == 0 | kDis<distance)
	                         {
	                             distance = kDis; //Update the distance to current
	                             kFinal=j;
	                         }
	                         // this is the final group evaluation
	                         // If the k group determined on this run differs from the previous run
	                         // revert z back to 0, let's try another run
	                         if (j == k-1 && imagePixel[y][x][3] != kFinal)
	                         {
	                             z = 0;
	                             imagePixel[y][x][3] = kFinal;
	                         }
	                         /*
	                         if (y == 100 && x== 100 && j == k-1)
	                         {
	                             System.out.println(", final group is "+ImagePixel[y][x][3]);    
	                         }*/
	                     }
	  
	                 }
	             }
	              
	             // iterate all pixel(x,y) and recompute the new centroids
	             // the PixTotal[][] is to used to store the sum of R/G/B value for a certain K group
	             double pixTotal[][] = new double[k][4];
	             /*
	              *  PixTotal[k][0] - Total R value
	              *  PixTotal[k][1] - Total G value
	              *  PixTotal[k][2] - Total B value
	              *  PixTotal[k][3] - Used to store how many pixels being assigned to each k group
	              */
	             for(int y = 0; y < this.height; y++)
	             {
	                 for(int x = 0; x < this.width; x++)
	                 {
	                     int j = imagePixel[y][x][3];// which group the pixel is assigned to
	                     if (z==0)
	                     // only do this if this is not the last run
	                     // this will be used to calculate the RGB coordinate for the new k centroids
	                     {
	                         pixTotal[j][0]=pixTotal[j][0]+imagePixel[y][x][0]; //R
	                         pixTotal[j][1]=pixTotal[j][1]+imagePixel[y][x][1]; //G
	                         pixTotal[j][2]=pixTotal[j][2]+imagePixel[y][x][2]; //B
	                         pixTotal[j][3]++; //Total number of pixels in group j will increase by 1
	                     }
	                     // this is the last run
	                     else
	                     {
	                         kResult[j][3]++;
	                     }
	                      
	                 }

	             }
	             for(int x = 0; x < k; x++ )
	             {
	                 if (z == 0)
	                 {
	                     kMean[x][0] = Math.abs((int)(pixTotal[x][0]/pixTotal[x][3]));
	                     kMean[x][1] = Math.abs((int)(pixTotal[x][1]/pixTotal[x][3]));
	                     kMean[x][2] = Math.abs((int)(pixTotal[x][2]/pixTotal[x][3]));
	                 }
	                 else
	                 // Assign the final value as representative color of the image as a whole
	                 {
	                     kResult[x][0] = kMean[x][0];
	                     kResult[x][1] = kMean[x][1];
	                     kResult[x][2] = kMean[x][2];
	                     kResult[x][4] = (int)((double)kResult[x][3]/(double)(this.height*this.width)*100.0);
	                 }
	                  
	             }
	             /*
	             if (z==1)
	             {
	                 System.out.println("Total run: "+krun); 
	             }
	             */
	             krun++;
	              
	         } //end of the loop
	         data = new int[4 * k];
	         for(int x = 0; x < k; x++)
	         {
	        	 for(int y = 0; y<3; y++) {
	        		 data[4*x+y] = kResult[x][y];
	        	 }
	        	 data[4*x+3] = kResult[x][4];
	         }
	     }
	      
	     catch (FileNotFoundException e) 
	     {
	         e.printStackTrace();
	     } 
	     catch (IOException e) 
	     {
	         e.printStackTrace();
	     }
		return data;
	 }

	private int getNumFromStr(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) != '0') return Integer.valueOf(s.substring(i)) - 1;
		}
		return 0;
	}
}
