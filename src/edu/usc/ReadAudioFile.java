package edu.usc;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class ReadAudioFile 
{
	private double[] data;
	public ReadAudioFile(String filepath_1) throws UnsupportedAudioFileException, IOException {
		File file = new File(filepath_1);
		AudioInputStream stream;
	    stream = AudioSystem.getAudioInputStream(file);
	    
	    int len = (int) stream.getFrameLength();
	    
	    double[] dataL = new double[len];
	    double[] dataR = new double[len];
	
	    ByteBuffer buf = ByteBuffer.allocate(4 * len);
	    byte[] bytes = new byte[4 * len];
	    try {
	        stream.read(bytes);
	        buf.put(bytes);
	        buf.rewind();
	
	        for(int i = 0; i < len; i++){
	            buf.order(ByteOrder.LITTLE_ENDIAN);
	            dataL[i] = buf.getShort() / 32768f;
	            dataR[i] = buf.getShort() / 32768f;
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        return;
	    }
	
	    this.data = new double[len];
	    for(int i = 0; i < len; i++){
	        this.data[i] = dataL[i] + dataR[i];
	        this.data[i] /= 2;
	    }
   	}
	
	public double[] getdata() {
		return this.data;
	}
}