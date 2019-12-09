package com.audiosimilarity;

import com.musicg.fingerprint.*;
import com.musicg.wave.*;

public class AudioSimilarity {
	public static void main(String[] args) {
		String music1 = args[0];
		String music2 = args[1];
		
		Wave wave1 = new Wave(music1);
		Wave wave2 = new Wave(music2);
		
		FingerprintSimilarity similarity = wave1.getFingerprintSimilarity(wave2);
		
		System.out.println("Similarity score: " + similarity.getScore() + ", Similarity: " + similarity.getSimilarity());
		
	}
}
