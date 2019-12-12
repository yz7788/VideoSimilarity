package edu.usc;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import org.opencv.core.Core;
import java.util.*;

public class MovieQuery {

	private JFrame frame;
	private JTable table;
	private JLabel label1 = new JLabel("Query Video");
	private JLabel label2 = new JLabel("DataBase Video");
	private int queryImageIndex = 0;
	private int dataBaseImageIndex = 0;
	private int currentVideoIndex = 0;
	private int[] startIndexes = new int[3];
	private File[] queryImages = new File[150];
	private File[][] dataBaseVideos = new File[3][600];
	private Timer queryTimer, dataBaseTimer;
	private JSlider slider = new JSlider(1, 600);
	private long queryClipTime = 0;
	private long dataBaseClipTime = 0;
	private Clip queryClip;
	private Clip[] dataBaseClips = new Clip[3];
	private String[][] optimalMovies = new String[3][4];
	private JLabel charLabel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MovieQuery window = new MovieQuery();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MovieQuery() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 900, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		//add button for selecting movie we want to query
		JButton btnNewButton = new JButton("Select Movie");
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setBounds(123, 6, 550, 400);
				
				fileChooser.setCurrentDirectory(new java.io.File("."));
				fileChooser.setDialogTitle("Select Video Folder");
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setAcceptAllFileFilterUsed(false);
				
				int result = fileChooser.showOpenDialog(frame);
				if (result == JFileChooser.APPROVE_OPTION) {
					File queryVideoFolder = fileChooser.getSelectedFile();
					String queryImagesPath = "./static/query_images_" + queryVideoFolder.getName();
					String queryAudioPath = queryVideoFolder.getPath() + "/" + queryVideoFolder.getName() + ".wav";
				    RGBToImage converter = new RGBToImage();
				    String dataBaseImagesPath = "./static/database_video_images";
				    String dataBaseVideosPath = "./static/database_videos";
				    String dataBaseAudioDir = "./static/database_videos/";
				    OpenCVExtraction extractor = new OpenCVExtraction();
				    AudioSimilarity audioCompiler = new AudioSimilarity();
				    
				    converter.convert(queryVideoFolder.getPath(), queryImagesPath);
				    System.out.println(queryImagesPath);
				    Map<String, double[]> distanceMap = extractor.getDistances(queryImagesPath);
				    Map<String, double[]> featureDistancesMap = extractor.getAllDistances();
				    Map<String, int[]> indexesMap = extractor.getTop10Indexes();
				    String[] optimalImagePaths = new String[3];
				    String[] optimalVideoPaths = new String[3];
				    
				    List<Map.Entry<String, double[]>> entries = new ArrayList<>();
				    for (Map.Entry<String, double[]> entry: distanceMap.entrySet()) {
				    	entries.add(entry);
				    }
				    Collections.sort(entries, new Comparator<Map.Entry<String, double[]>>() {
				    	@Override
				    	public int compare(Map.Entry<String, double[]> entry1, Map.Entry<String, double[]> entry2) {
				    		return (int)(entry1.getValue()[0] - entry2.getValue()[0]);
				    	}
				    });
				    // Get top 5 candidates videos and calculate audio similarity for their top 10 indexes 
				    Map<String, double[]> finalDistanceMap = new HashMap<>();
				    for (int i = 0; i < 5; i++) {
				    	String name = entries.get(i).getKey();
				    	String baseAudioPath = dataBaseAudioDir + name + "/" + name + ".wav";
				    	int[] indexes = indexesMap.get(name);
				    	double[] featureDistances = featureDistancesMap.get(name);
				    	try {
							double[] audioSim = audioCompiler.getSimilarity(queryAudioPath, baseAudioPath, indexes);
							double minDistance = Integer.MAX_VALUE;
							double minFeatureDistance = 0, minAudioDistance = 0;
						    int minStartIndex = 0;
							for (int j = 0; j < indexes.length; j++) {
					    		double combinedDistance = (1.0-audioSim[j]) * featureDistances[indexes[j]];
					    		if (combinedDistance < minDistance) {
					    			minDistance = combinedDistance;
					    			minFeatureDistance = featureDistances[indexes[j]];
					    			minAudioDistance = 1.0 - audioSim[j];
					    			minStartIndex = indexes[j];
					    		}
					    	}
							finalDistanceMap.put(name, new double[] { minDistance, minStartIndex, minFeatureDistance, minAudioDistance});
						} catch (UnsupportedAudioFileException | IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    }
				    List<Map.Entry<String, double[]>> finalEntries = new ArrayList<>();
				    for (Map.Entry<String, double[]> entry: finalDistanceMap.entrySet()) {
				    	finalEntries.add(entry);
				    }
				    Collections.sort(finalEntries, new Comparator<Map.Entry<String, double[]>>() {
				    	@Override
				    	public int compare(Map.Entry<String, double[]> entry1, Map.Entry<String, double[]> entry2) {
				    		return (int)(entry1.getValue()[0] - entry2.getValue()[0]);
				    	}
				    });
				    
				    for (int i = 0; i < 3; i++) {
				    	String name = finalEntries.get(i).getKey();
				    	double[] pair = finalDistanceMap.get(name);
				    	optimalImagePaths[i] = dataBaseImagesPath + "/" + name;
				    	optimalVideoPaths[i] = dataBaseVideosPath + "/" + name;
				    	startIndexes[i] = (int)pair[1];
				    	table.setValueAt(name, i, 0);
				    	table.setValueAt(String.valueOf((int)pair[0]), i, 1);
				    	table.setValueAt(String.valueOf((int)pair[2]), i, 2);
				    	table.setValueAt(String.valueOf(pair[3]), i, 3);
				    }
				   
				    
				    Plot chart = new Plot("Measurement of similarity",
				            "Similarity between different parts of two video", extractor.getAllDistances(), charLabel);
				    chart.pack( );          
//				    RefineryUtilities.centerFrameOnScreen( chart );          
//				    chart.setVisible( true ); 
					
					getSortedImages(new File(queryImagesPath), queryImages);
				    for (int i = 0; i < 3; i++) {
				    	String path = optimalImagePaths[i];
				    	dataBaseVideos[i] = new File[600];
				    	getSortedImages(new File(path), dataBaseVideos[i]);
				    	
						try {
							String fileName = optimalImagePaths[i].substring(optimalImagePaths[i].lastIndexOf('/') + 1);
							AudioInputStream dataBaseStream = AudioSystem.getAudioInputStream(new File(optimalVideoPaths[i] + "/" + fileName + ".wav"));
					        AudioFormat dataBaseFormat = dataBaseStream.getFormat();
					        DataLine.Info dataBaseInfo = new DataLine.Info(Clip.class, dataBaseFormat);
					        dataBaseClips[i] = (Clip) AudioSystem.getLine(dataBaseInfo);
					        dataBaseClips[i].open(dataBaseStream);
						} catch (UnsupportedAudioFileException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (LineUnavailableException e) {
							e.printStackTrace();
						}
				    }
					try {
						if (queryTimer != null) queryTimer.stop();
						if (dataBaseTimer != null) dataBaseTimer.stop();
						queryImageIndex = 0;
						dataBaseImageIndex = startIndexes[currentVideoIndex];
						BufferedImage image1 = ImageIO.read(queryImages[queryImageIndex]);
						label1.setIcon(new ImageIcon(image1));
						BufferedImage image2 = ImageIO.read(dataBaseVideos[currentVideoIndex][dataBaseImageIndex]);
						label2.setIcon(new ImageIcon(image2));
						slider.setValue(dataBaseImageIndex + 1);
				    	dataBaseClipTime = (long)(((dataBaseImageIndex + 1)/ 600.0) * dataBaseClips[currentVideoIndex].getMicrosecondLength());
//				    	dataBaseClips[currentVideoIndex].setMicrosecondPosition(dataBaseClipTime);  
				        frame.setVisible(true);
				        
				        AudioInputStream queryStream = AudioSystem.getAudioInputStream(new File(queryAudioPath));
				        AudioFormat queryFormat = queryStream.getFormat();
				        DataLine.Info queryInfo = new DataLine.Info(Clip.class, queryFormat);
				        queryClip = (Clip) AudioSystem.getLine(queryInfo);
				        queryClip.open(queryStream);
				        JOptionPane.showMessageDialog(frame, "Query finished");
					} catch (IOException e) {
						e.printStackTrace();
					} catch (UnsupportedAudioFileException e) {
						e.printStackTrace();
					} catch (LineUnavailableException e) {
						e.printStackTrace();
					}
				}
			}
		});
		btnNewButton.setBounds(131, 91, 117, 29);
		frame.getContentPane().add(btnNewButton);
		
		//add table for 3 most similar movies in the database
		String[] columns = {"Movie Name", "Mixed Dist", "Feature Dist", "Audio Dist"};
		table = new JTable(optimalMovies, columns);
		table.setDefaultEditor(Object.class, null);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowSelectionInterval(0, 0);	
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
		    public void valueChanged(ListSelectionEvent event) {
		        currentVideoIndex = table.getSelectedRow();
		        if (queryImages[0] != null) {
					try {
						if (dataBaseTimer != null) dataBaseTimer.stop();
						if (dataBaseClips[currentVideoIndex] != null) dataBaseClips[currentVideoIndex].stop();
						dataBaseImageIndex = startIndexes[currentVideoIndex];
						slider.setValue(dataBaseImageIndex + 1);
						BufferedImage image2 = ImageIO.read(dataBaseVideos[currentVideoIndex][dataBaseImageIndex]);
						label2.setIcon(new ImageIcon(image2));
				        frame.setVisible(true);
					} catch (IOException e) {
						e.printStackTrace();
					}
		        }
		    }
		});
        JScrollPane srollPane = new JScrollPane(table); 
        srollPane.setBounds(64, 176, 300, 150);
        frame.getContentPane().add(srollPane);
        
        JButton queryPlayButton = new JButton("Play");
        queryPlayButton.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent event) {
			    ActionListener actionListener = new ActionListener() {
			    	public void actionPerformed(ActionEvent ae) {
			    		if (queryImages[0] != null) {
							try {
								BufferedImage image = ImageIO.read(queryImages[queryImageIndex]);
								label1.setIcon(new ImageIcon(image));
						        frame.setVisible(true);
						        if (queryImageIndex < 149) queryImageIndex++;
							} catch (IOException  e) {
								e.printStackTrace();
							}
			    		}
			    	}
			    };
			    queryTimer = new Timer(33, actionListener);
			    queryTimer.start();
			    if (queryClip != null) {
			    	queryClip.setMicrosecondPosition(queryClipTime);
			    	queryClip.start();
			    }
        	}
        });
        queryPlayButton.setBounds(60, 699, 90, 29);
        frame.getContentPane().add(queryPlayButton);
        
        JButton queryPauseButton = new JButton("Pause");
        queryPauseButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        	}
        });
        queryPauseButton.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent event) {
        		if (queryTimer != null) queryTimer.stop();
        		if (queryClip != null) {
            		queryClipTime = queryClip.getMicrosecondPosition();
            		queryClip.stop();
        		}
        	}
        });
        queryPauseButton.setBounds(186, 699, 90, 29);
        frame.getContentPane().add(queryPauseButton);
        
        JButton queryStopButton = new JButton("Stop");
        queryStopButton.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent event) {
        		if (queryImages[0] != null) {
    				try {
    					if (queryTimer != null) queryTimer.stop();
    					queryImageIndex = 0;
    					BufferedImage image = ImageIO.read(queryImages[queryImageIndex]);
    					label1.setIcon(new ImageIcon(image));
    			        frame.setVisible(true);
    				    if (queryClip != null) {
    				    	queryClip.stop();
    				    	queryClipTime = 0;
    				    	queryClip.setMicrosecondPosition(queryClipTime);  				    	
    				    }
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
        		}
        	}
        });
        queryStopButton.setBounds(305, 699, 90, 29);
        frame.getContentPane().add(queryStopButton);

        JButton dataBasePlayButton = new JButton("Play");
        dataBasePlayButton.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent event) {
			    ActionListener actionListener = new ActionListener() {
			    	public void actionPerformed(ActionEvent ae) {
			    		if (dataBaseVideos[currentVideoIndex][0] != null) {
							try {
								BufferedImage image = ImageIO.read(dataBaseVideos[currentVideoIndex][dataBaseImageIndex]);
								label2.setIcon(new ImageIcon(image));
								slider.setValue(dataBaseImageIndex + 1);
						        frame.setVisible(true);
						        if (dataBaseImageIndex < 599) dataBaseImageIndex++;
							} catch (IOException  e) {
								e.printStackTrace();
							}
			    		}
			    	}
			    };
			    dataBaseTimer = new Timer(33, actionListener);
			    dataBaseTimer.start();
			    if (dataBaseClips[currentVideoIndex] != null) {
			    	dataBaseClips[currentVideoIndex].setMicrosecondPosition(dataBaseClipTime);
			    	dataBaseClips[currentVideoIndex].start();
			    }
        	}
        });
        dataBasePlayButton.setBounds(511, 699, 90, 29);
        frame.getContentPane().add(dataBasePlayButton);
        
        JButton dataBasePauseButton = new JButton("Pause");
        dataBasePauseButton.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent event) {
        		if (dataBaseTimer != null) dataBaseTimer.stop();
        		if (dataBaseClips[currentVideoIndex] != null) {
            		dataBaseClipTime = dataBaseClips[currentVideoIndex].getMicrosecondPosition();
            		dataBaseClips[currentVideoIndex].stop();
        		}
        	}
        });
        dataBasePauseButton.setBounds(636, 699, 90, 29);
        frame.getContentPane().add(dataBasePauseButton);
        
        JButton dataBaseStopButton = new JButton("Stop");
        dataBaseStopButton.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent event) {
        		if (dataBaseVideos[currentVideoIndex][0] != null) {
    				try {
    					if (dataBaseTimer != null) dataBaseTimer.stop();
    					dataBaseImageIndex = startIndexes[currentVideoIndex];
    					BufferedImage image = ImageIO.read(dataBaseVideos[currentVideoIndex][dataBaseImageIndex]);
    					label2.setIcon(new ImageIcon(image));
    					slider.setValue(dataBaseImageIndex + 1);
    			        frame.setVisible(true);
    				    if (dataBaseClips[currentVideoIndex] != null) {
    				    	dataBaseClips[currentVideoIndex].stop();
    				    	dataBaseClipTime = (long)(((dataBaseImageIndex + 1)/ 600.0) * dataBaseClips[currentVideoIndex].getMicrosecondLength());
    				    	dataBaseClips[currentVideoIndex].setMicrosecondPosition(dataBaseClipTime);  				    	
    				    }
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
        		}
        	}
        });
        dataBaseStopButton.setBounds(756, 699, 90, 29);
        frame.getContentPane().add(dataBaseStopButton);
        
        Border border = BorderFactory.createLineBorder(Color.GRAY, 1);
        label1.setHorizontalAlignment(SwingConstants.CENTER);
        label1.setBounds(60, 378, 335, 288);
        label1.setBorder(border);
        label2.setHorizontalAlignment(SwingConstants.CENTER);
        label2.setBounds(511, 378, 335, 288);
        label2.setBorder(border);
        frame.getContentPane().add(label1);
        frame.getContentPane().add(label2);
        slider.addChangeListener(new ChangeListener() {
        	public void stateChanged(ChangeEvent event) {
                JSlider sliderCopy = (JSlider) event.getSource();
                if (!sliderCopy.getValueIsAdjusting()) {
                  int value = sliderCopy.getValue();
                  if (dataBaseVideos[currentVideoIndex][0] != null) {
      				try {
      					dataBaseImageIndex = value - 1;
    					BufferedImage image = ImageIO.read(dataBaseVideos[currentVideoIndex][dataBaseImageIndex]);
    					label2.setIcon(new ImageIcon(image));
				    	dataBaseClipTime = (long)((value / 600.0) * dataBaseClips[currentVideoIndex].getMicrosecondLength());
    			        frame.setVisible(true);
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
                  }
                }
        	}
        });
        
        slider.setBounds(501, 337, 355, 29);
        slider.setValue(1);
        frame.getContentPane().add(slider);    
        
        charLabel = new JLabel("Similarity Chart");
        charLabel.setHorizontalAlignment(SwingConstants.CENTER);
        charLabel.setBounds(454, 25, 330, 300);
        charLabel.setBorder(border);
        frame.getContentPane().add(charLabel);
	}
	
	private int getNumFromStr(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) != '0') return Integer.valueOf(s.substring(i)) - 1;
		}
		return 0;
	}
	
	private void getSortedImages(File folder, File[] imageArr) {
	    for (File imageFile: folder.listFiles()) {
	    	if (imageFile.isDirectory()) continue;
	    	String[] parts = imageFile.getName().split("\\.");
	    	if (!parts[1].equals("jpg")) continue;
	    	String fileName = parts[0];
	    	int index = getNumFromStr(fileName.substring(fileName.length() - 3));
	    	imageArr[index] = imageFile;
	    }
	}
}
