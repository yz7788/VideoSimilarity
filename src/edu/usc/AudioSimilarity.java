package edu.usc;
import org.jfree.ui.RefineryUtilities;

import java.io.IOException;
import java.math.*;
import java.util.*;

import javax.sound.sampled.UnsupportedAudioFileException;

import edu.usc.Plot;

public class AudioSimilarity
{
	//private static double[] similarity = new double[450];

	public double[] getSimilarity (String filepath_1, String filepath_2, int[] indexes) throws UnsupportedAudioFileException, IOException {
		double[] similarity = new double[indexes.length];
		ReadAudioFile file_1 = new ReadAudioFile(filepath_1);
		double[] filter_data_1 = wavefilter(file_1.getdata());
		double[] hamming_data_1 = hammingwindow(filter_data_1);//query audio
		int file_1_length = file_1.getdata().length;
		
		ReadAudioFile file_2 = new ReadAudioFile(filepath_2);
		double[] filter_data_2 = wavefilter(file_2.getdata());//database audio
		
		int step = (file_2.getdata().length-file_1.getdata().length)/450;
		
		for (int j = 0; j < indexes.length; j++) {
			int i = indexes[j] * step;
			double[] data_2 = Arrays.copyOfRange(filter_data_2,i,i+file_1_length);
			double[] hamming_data_2 = hammingwindow(data_2);
			//System.out.println(data_2.length + "," +file_1_length);
			similarity[j] = cosinesimilarity(hamming_data_1, hamming_data_2);
		}
		
		//Plot chart = new Plot("Wave Plot","Wave",similarity);
	    //chart.pack( );     
	    //RefineryUtilities.centerFrameOnScreen( chart );          
	    //chart.setVisible( true ); 
	    
	    return similarity;
	}
	
	public static double[] wavefilter(double[] wavedata) {
		double[] filterdata = new double[wavedata.length];
		filterdata[0] = 0;
		for (int i=0;i< wavedata.length-1;i++) {
			filterdata[i+1] = 1.0 * wavedata[i+1] + (-0.9375) * wavedata[i];
		}
		return filterdata;
	}
	
	public static double[] hammingwindow(double[] wavedata) {
		int row = 32;
		int column = 16;
		double[] hamming_window = new double[row * column];
		for (int i=0; i< row * column; i++) {
			hamming_window[i] = 0.54 - 0.46 * Math.cos(2*Math.PI*i/(row*column-1));
		}
		double[] hamming_data = new double[wavedata.length + row*column-1];
		for(int i=0; i < wavedata.length + row*column-1; i++) {
			double sum = 0;
			for(int j=0; j <row * column; j++) {
				if(j<=i && i-j<wavedata.length) 
				{sum = sum + hamming_window[j] * wavedata[i-j] * wavedata[i-j];}
			}
			hamming_data[i] = sum;
		}
		return hamming_data;
	} 
	
	public static double cosinesimilarity(double[] data_1, double[] data_2) {
		double similarity,innerproduct = 0,squaresum_1 = 0, squaresum_2 = 0;
	    for (int i=0; i <data_1.length; i++) {
	    	innerproduct = innerproduct + data_1[i] * data_2[i];
	        squaresum_1 = squaresum_1 + data_1[i]*data_1[i];
	        squaresum_2 = squaresum_2 + data_2[i]*data_2[i];
	    }
	    similarity = innerproduct / (Math.sqrt(squaresum_1) * Math.sqrt(squaresum_2));
        return similarity;
	}
}
