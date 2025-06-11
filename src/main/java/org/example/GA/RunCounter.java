package org.example.GA;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RunCounter {
    public static int getAndIncrementRunCount(long i) {
        String COUNTER_FILE = "run_counter"+i+".txt";
        try {
            // Create file if it doesn't exist
            if (!Files.exists(Paths.get(COUNTER_FILE))) {
                Files.write(Paths.get(COUNTER_FILE), "0".getBytes());
                return 0;
            }

            // Read current count
            String content = new String(Files.readAllBytes(Paths.get(COUNTER_FILE)));
            int count = Integer.parseInt(content.trim());
            count++;

            // Increment and save
            Files.write(Paths.get(COUNTER_FILE), String.valueOf(count).getBytes());

            return count;
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            return 0; // Fallback
        }
    }

    public static void resetCounter(int i) {
        String COUNTER_FILE = "run_counter"+i+".txt";
        try {
            Files.write(Paths.get(COUNTER_FILE), "0".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
