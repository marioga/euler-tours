package com.marioga.eulertours;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Random;

public class EulerGraphGeneratorTester {
    private static final String FILENAME = "graph.txt";
    private static final Random RND = new Random();
    private static final int VERTEX_MEAN = 6;
    private static final int HALF_DEGREE_UPPER_BOUND = 3;

    private static int findVertex(int[] degrees, int guess) {
        int total = 0;
        int i = 0;
        while (guess >= total) {
            total += degrees[i++];
        }
        return i - 1;
    }

    public static void main(String[] args) {
        try {
            OutputStream os = new FileOutputStream(FILENAME);
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            PrintWriter out = new PrintWriter(osw);
            int V = VERTEX_MEAN / 2 + RND.nextInt(VERTEX_MEAN);
            out.write(V + System.lineSeparator());
            int E = 0;
            int[] degrees = new int[V];
            for (int i = 0; i < V; i++) {
                int halfDegree = 1 + RND.nextInt(HALF_DEGREE_UPPER_BOUND);
                degrees[i] = 2 * halfDegree;
                E += halfDegree;
            }
            out.write(E + System.lineSeparator());
            for (int i = 0; i < E; i++) {
                int v = findVertex(degrees, RND.nextInt(2 * E - 2 * i));
                degrees[v]--;
                int w = findVertex(degrees, RND.nextInt(2 * E - 2 * i - 1));
                degrees[w]--;
                out.write(v + " " + w + System.lineSeparator());
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}