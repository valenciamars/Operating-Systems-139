/*
 * Maria Valencia
 * CSC 139 spring 2025
 * Memory Management assignment 
 * 
 * to Run :
 *  javac MemoryManager.java
 *  java MemoryManager
 * 
 * Tested in ECS environment on linux
 */

import java.io.*;  
import java.util.*;  

public class MemoryManager {

    // Helper class to store page info
    static class PageEntry {
        int page;
        int frame;
        int lastUsed;  // used for LRU
        int nextUse;   // used for Optimal

        PageEntry(int page, int frame) {
            this.page = page;
            this.frame = frame;
        }
    }

    static int[] requests;  // array to store all page requests
    static int numPages, numFrames, numRequests;  // input variables

    public static void main(String[] args) throws IOException {
        // Read input file
        Scanner sc = new Scanner(new File("input.txt"));
        BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));

        // First line: number of pages, frames, and requests
        numPages = sc.nextInt();
        numFrames = sc.nextInt();
        numRequests = sc.nextInt();
        requests = new int[numRequests];

        // Read all the page requests
        for (int i = 0; i < numRequests; i++) {
            requests[i] = sc.nextInt();
        }

        // Run all three algorithms
        runFIFO(writer);
        runOptimal(writer);
        runLRU(writer);

        writer.close();  // close the writer
    }

    // FIFO page replacement policy
    static void runFIFO(BufferedWriter writer) throws IOException {
        writer.write("FIFO\n");
        LinkedList<Integer> queue = new LinkedList<>();  // to track the FIFO order
        HashMap<Integer, Integer> pageToFrame = new HashMap<>();  // page -> frame map
        int frameIndex = 0;  // keeps track of next free frame
        int pageFaults = 0;  // counter for page faults

        for (int i = 0; i < numRequests; i++) {
            int page = requests[i];  // current page request
            if (pageToFrame.containsKey(page)) {
                // Page is already loaded, no fault
                writer.write("Page " + page + " already in frame " + pageToFrame.get(page) + "\n");
            } else {
                // Page fault occurs
                pageFaults++;
                if (pageToFrame.size() < numFrames) {
                    // Frame is available, no need to evict
                    pageToFrame.put(page, frameIndex);
                    queue.add(page);
                    writer.write("Page " + page + " loaded into frame " + frameIndex + "\n");
                    frameIndex++;
                } else {
                    // Need to evict oldest page
                    int evict = queue.poll();  // remove oldest page
                    int freedFrame = pageToFrame.get(evict);
                    pageToFrame.remove(evict);
                    pageToFrame.put(page, freedFrame);
                    queue.add(page);
                    writer.write("Page " + evict + " unloaded from Frame " + freedFrame + ", Page " + page + " loaded into Frame " + freedFrame + "\n");
                }
            }
        }
        writer.write(pageFaults + " page faults\n\n");  // total page faults for FIFO
    }

    // Optimal page replacement policy
    static void runOptimal(BufferedWriter writer) throws IOException {
        writer.write("Optimal\n");
        HashMap<Integer, Integer> pageToFrame = new HashMap<>();  // page -> frame
        int frameIndex = 0;
        int pageFaults = 0;

        for (int i = 0; i < numRequests; i++) {
            int page = requests[i];
            if (pageToFrame.containsKey(page)) {
                writer.write("Page " + page + " already in frame " + pageToFrame.get(page) + "\n");
            } else {
                pageFaults++;
                if (pageToFrame.size() < numFrames) {
                    pageToFrame.put(page, frameIndex);
                    writer.write("Page " + page + " loaded into frame " + frameIndex + "\n");
                    frameIndex++;
                } else {
                    // Find the page that will be used farthest in future
                    int farthestIndex = -1, farthestPage = -1;
                    HashMap<Integer, Integer> futureUse = new HashMap<>();

                    // Predict next use for pages in memory
                    for (int j = i + 1; j < numRequests; j++) {
                        if (!futureUse.containsKey(requests[j]) && pageToFrame.containsKey(requests[j])) {
                            futureUse.put(requests[j], j);
                        }
                    }

                    for (int p : pageToFrame.keySet()) {
                        int index = futureUse.getOrDefault(p, Integer.MAX_VALUE); //integer.Max_Value is infinity
                        if (index > farthestIndex) {
                            farthestIndex = index;
                            farthestPage = p;
                        }
                    }

                    int freedFrame = pageToFrame.get(farthestPage);
                    pageToFrame.remove(farthestPage);
                    pageToFrame.put(page, freedFrame);
                    writer.write("Page " + farthestPage + " unloaded from frame " + freedFrame + ", Page " + page + " loaded into Frame " + freedFrame + "\n");
                }
            }
        }
        writer.write(pageFaults + " page faults\n\n");  // total page faults for Optimal
    }

    // LRU page replacement policy
    static void runLRU(BufferedWriter writer) throws IOException {
        writer.write("LRU\n");
        HashMap<Integer, Integer> pageToFrame = new HashMap<>();  // page -> frame
        HashMap<Integer, Integer> lastUsed = new HashMap<>();  // page -> last used index
        int frameIndex = 0;
        int pageFaults = 0;

        for (int i = 0; i < numRequests; i++) {
            int page = requests[i];
            if (pageToFrame.containsKey(page)) {
                writer.write("Page " + page + " already in frame " + pageToFrame.get(page) + "\n");
                lastUsed.put(page, i);  // update last used
            } else {
                pageFaults++;
                if (pageToFrame.size() < numFrames) {
                    pageToFrame.put(page, frameIndex);
                    lastUsed.put(page, i);
                    writer.write("Page " + page + " loaded into frame " + frameIndex + "\n");
                    frameIndex++;
                } else {
                    // Find least recently used page
                    int lruTime = Integer.MAX_VALUE;
                    int lruPage = -1;
                    for (int p : pageToFrame.keySet()) {
                        if (lastUsed.get(p) < lruTime) {
                            lruTime = lastUsed.get(p);
                            lruPage = p;
                        }
                    }

                    int freedFrame = pageToFrame.get(lruPage);
                    pageToFrame.remove(lruPage);
                    lastUsed.remove(lruPage);
                    pageToFrame.put(page, freedFrame);
                    lastUsed.put(page, i);
                    writer.write("Page " + lruPage + " unloaded from frame " + freedFrame + ", Page " + page + " loaded into Frame " + freedFrame + "\n");
                }
            }
        }
        writer.write(pageFaults + " page faults\n");  // total page faults for LRU
    }
}
