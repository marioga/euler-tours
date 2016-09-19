package com.marioga.eulertours;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Stack;

public class EulerTourFinder {
    private Deque<Integer> mEulerTour = new ArrayDeque<>();
    private Deque<Integer> mUnexploredInCycle;
    private int mEdgesInCycles = 0;
    private boolean mCycleFound;

    private int mV;
    private int mE;
    private HashMap<Integer, HashMap<Integer, Integer>> mUnusedEdges =
            new HashMap<>();
    private int[] mUnusedDegrees;

    public EulerTourFinder(String fileName) {
        // Read graph from file
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fileName));
            mV = Integer.parseInt(reader.readLine());
            mE = Integer.parseInt(reader.readLine());
            mUnusedDegrees = new int[mV];

            String currentLine;
            String[] lines;
            int v;
            int w;
            for (int i = 0; i < mE; i++) {
                currentLine = reader.readLine();
                lines = currentLine.trim().split("\\s+");
                v = Integer.parseInt(lines[0]);
                w = Integer.parseInt(lines[1]);
                addEdge(v, w);
                addEdge(w, v);
            }
            for (int i = 0; i < mV; i++) {
                if (mUnusedDegrees[i] % 2 == 1) {
                    // Degree of i is odd
                    return;
                }
            }
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
        mUnexploredInCycle = new ArrayDeque<>();
        mUnexploredInCycle.push(0);
        computeTour();
    }

    private void addEdge(int v, int w) {
        if (mUnusedEdges.get(v) == null) {
            mUnusedEdges.put(v, new HashMap<Integer, Integer>());
        }
        mUnusedDegrees[v]++;
        if (!mUnusedEdges.get(v).containsKey(w)) {
            mUnusedEdges.get(v).put(w, 1);
        } else {
            mUnusedEdges.get(v).put(w,
                    mUnusedEdges.get(v).get(w) + 1);
        }
    }

    private void computeTour() {
        // These arrays are reused in every DFS
        boolean[] marked = new boolean[mV];
        int[] edgeTo = new int[mV];

        while (mEdgesInCycles < mE) {
            int v = -1;
            if (!mUnexploredInCycle.isEmpty()) {
                v = mUnexploredInCycle.pop();
            } else {
                // Graph is not connected
                mEulerTour = new ArrayDeque<>();
                return;
            }

            if (mUnusedDegrees[v] > 0) {
                computeSelfCycles(v);
                computeParallelEdges(v);
                while (mUnusedDegrees[v] > 0) {
                    for (int i = 0; i < mV; i++) {
                        marked[i] = false;
                    }
                    mCycleFound = false;
                    computeCycleFrom(v, -1, v, marked, edgeTo);
                }
            }
            mEulerTour.offerFirst(v);
        }
        while (!mUnexploredInCycle.isEmpty()) {
            mEulerTour.offerFirst(mUnexploredInCycle.pop());
        }
    }

    private void removeEdge(int v, int w) {
        mUnusedEdges.get(v).put(w, mUnusedEdges.get(v).get(w) - 1);
        mUnusedEdges.get(w).put(v, mUnusedEdges.get(w).get(v) - 1);
        mUnusedDegrees[v]--;
        mUnusedDegrees[w]--;
    }

    private void computeSelfCycles(int v) {
        if (mUnusedEdges.get(v).containsKey(v)) {
            int selfDegree = mUnusedEdges.get(v).get(v);
            mUnusedDegrees[v] -= selfDegree;
            mEdgesInCycles += selfDegree / 2;
            while (selfDegree > 0) {
                mEulerTour.offerFirst(v);
                selfDegree -= 2;
            }
            mUnusedEdges.get(v).put(v, 0);
        }
    }

    private void computeParallelEdges(int v) {
        for (int w : mUnusedEdges.get(v).keySet()) {
            while (mUnusedEdges.get(v).get(w) >= 2) {
                mUnexploredInCycle.push(v);
                mUnexploredInCycle.push(w);
                removeEdge(v, w);
                removeEdge(w, v);
                mEdgesInCycles += 2;
            }
        }
    }

    private void computeCycleFrom(int base, int prev, int curr,
            boolean[] marked, int[] edgeTo) {
        marked[curr] = true;
        for (int w : mUnusedEdges.get(curr).keySet()) {
            if (mCycleFound) {
                return;
            }
            if (mUnusedEdges.get(curr).get(w) > 0) {
                if (!marked[w]) {
                    edgeTo[w] = curr;
                    computeCycleFrom(base, curr, w, marked, edgeTo);
                } else if (w == base && base != prev) { // Found cycle involving base
                    mCycleFound = true;
                    removeEdge(curr, base);
                    mEdgesInCycles++;
                    Stack<Integer> temp = new Stack<>();
                    for (int v = curr; v != base; v = edgeTo[v]) {
                        removeEdge(edgeTo[v], v);
                        mEdgesInCycles++;
                        temp.push(v);
                    }
                    mUnexploredInCycle.push(base);
                    while (!temp.isEmpty()) {
                        mUnexploredInCycle.push(temp.pop());
                    }
                }
            }
        }
    }

    public boolean hasEulerTour() {
        return mEulerTour.iterator().hasNext();
    }

    public Iterable<Integer> tour() {
        return mEulerTour;
    }

    public static void main(String[] args) {
        EulerTourFinder finder = new EulerTourFinder(args[0]);
        OutputStream os;
        try {
            os = new FileOutputStream("output.txt");
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            PrintWriter out = new PrintWriter(osw);
            if (finder.hasEulerTour()) {
                for (int s : finder.tour()) {
                    out.write(s + "\n");
                }
            } else {
                out.write("Not Eulerian!\n");
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}