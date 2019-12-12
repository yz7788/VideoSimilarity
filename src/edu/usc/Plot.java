package edu.usc;

import java.awt.Color;
import java.util.*;

import javax.swing.JLabel;

import java.awt.BasicStroke; 

import org.jfree.chart.ChartPanel; 
import org.jfree.chart.JFreeChart; 
import org.jfree.data.xy.XYDataset; 
import org.jfree.data.xy.XYSeries; 
import org.jfree.ui.ApplicationFrame; 
import org.jfree.chart.plot.XYPlot; 
import org.jfree.chart.ChartFactory; 
import org.jfree.chart.plot.PlotOrientation; 
import org.jfree.data.xy.XYSeriesCollection; 
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

public class Plot extends ApplicationFrame {

   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

public Plot( String applicationTitle, String chartTitle, Map<String, double[]> distance, JLabel jLabel) {
      super(applicationTitle);
      JFreeChart xylineChart = ChartFactory.createXYLineChart(
         chartTitle ,
         "Category" ,
         "Score" ,
         createDataset(distance) ,
         PlotOrientation.VERTICAL ,
         true , true , false);
         
      ChartPanel chartPanel = new ChartPanel( xylineChart );
      chartPanel.setPreferredSize( new java.awt.Dimension( 330 , 300 ) );
      final XYPlot plot = xylineChart.getXYPlot( );
      
      XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer( );
      renderer.setSeriesPaint( 0 , Color.RED );
      renderer.setSeriesStroke( 0 , new BasicStroke( 1.0f ) );
      plot.setRenderer( renderer ); 
      setContentPane( chartPanel ); 
      jLabel.add(chartPanel);
   }
   
   private XYDataset createDataset(Map<String, double[]> distance) {
	  Map<String, XYSeries> distancesMap = new HashMap<>();
	  //XYSeries distances = new XYSeries[distance.keySet().size()];
	  
	  for(String foldername: distance.keySet()) {
		  final XYSeries xyseries = new XYSeries(foldername);
		  for (int i=0; i<distance.get(foldername).length;i++) {
			  xyseries.add(i, distance.get(foldername)[i]);
		  }
		  distancesMap.put(foldername, xyseries);
	  }
             
      
      final XYSeriesCollection dataset = new XYSeriesCollection( );          
      for (String foldername: distance.keySet()) {
    	  dataset.addSeries(distancesMap.get(foldername));
      }       
      return dataset;
   }
}