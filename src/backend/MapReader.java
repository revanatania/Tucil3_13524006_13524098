import java.io.*;
import java.util.*;

public class MapReader {

    public static Map readFromFile(String filePath) throws IOException {
        File file = new File(filePath);

        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }
        if (!file.canRead()) {
            throw new IOException("File cannot be read: " + filePath);
        }
        if (!filePath.endsWith(".txt")) {
            throw new IOException("File must have .txt extension: " + filePath);
        }

        BufferedReader br = new BufferedReader(new FileReader(file));

        String firstLine = br.readLine();
        if (firstLine == null || firstLine.trim().isEmpty()) {
            br.close();
            throw new IOException("File is empty or missing first line (N M)");
        }

        String[] dims = firstLine.trim().split("\\s+");
        if (dims.length < 2) {
            br.close();
            throw new IOException("First line must contain exactly 2 integers (N M)");
        }

        int n, m;
        try {
            n = Integer.parseInt(dims[0]);
            m = Integer.parseInt(dims[1]);
        } catch (NumberFormatException e) {
            br.close();
            throw new IOException("N and M must be integers, got: " + dims[0] + " " + dims[1]);
        }

        if (n <= 0 || m <= 0) {
            br.close();
            throw new IOException("N and M must be positive integers, got: " + n + " " + m);
        }

        Map map = new Map(n, m);

        for (int i = 0; i < n; i++) {
            String line = br.readLine();
            if (line == null) {
                br.close();
                throw new IOException("Grid is incomplete: expected " + n + " rows, missing row " + (i + 1));
            }
            if (line.length() != m) {
                br.close();
                throw new IOException("Grid row " + (i + 1) + " has length " + line.length() + ", expected " + m);
            }
            for (int j = 0; j < m; j++) {
                map.grid[i][j] = line.charAt(j);
            }
        }

        for (int i = 0; i < n; i++) {
            String line = br.readLine();
            if (line == null || line.trim().isEmpty()) {
                br.close();
                throw new IOException("Cost matrix is incomplete: missing row " + (i + 1));
            }
            String[] tokens = line.trim().split("\\s+");
            if (tokens.length != m) {
                br.close();
                throw new IOException("Cost row " + (i + 1) + " has " + tokens.length + " values, expected " + m);
            }
            for (int j = 0; j < m; j++) {
                try {
                    map.cost[i][j] = Integer.parseInt(tokens[j]);
                } catch (NumberFormatException e) {
                    br.close();
                    throw new IOException("Cost at row " + (i + 1) + " col " + (j + 1) + " is not an integer: " + tokens[j]);
                }
            }
        }

        br.close();
        map.initialize();
        return map;
    }
}