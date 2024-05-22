import java.io.*;
import java.util.*;

public class DirectedGraphLab {

    static class Graph {
        private Map<String, Map<String, Integer>> adjacencyList = new HashMap<>();

        public void addEdge(String from, String to) {
            adjacencyList.putIfAbsent(from, new HashMap<>());
            adjacencyList.get(from).put(to, adjacencyList.get(from).getOrDefault(to, 0) + 1);
        }

        public Map<String, Map<String, Integer>> getAdjacencyList() {
            return adjacencyList;
        }

        public void display() {
            for (String node : adjacencyList.keySet()) {
                System.out.println(node + " -> " + adjacencyList.get(node));
            }
        }

        public Set<String> getNodes() {
            return adjacencyList.keySet();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java DirectedGraphLab <input_file>");
            System.exit(1);
        }

        String filename = args[0];
        Graph graph = new Graph();
        readFromFileAndGenerateGraph(graph, filename);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Choose an option:");
            System.out.println("1. Display graph");
            System.out.println("2. Query bridge words");
            System.out.println("3. Generate new text");
            System.out.println("4. Calculate shortest path");
            System.out.println("5. Random walk");
            System.out.println("6. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine();  // Consume newline

            switch (choice) {
                case 1:
                    graph.display();
                    break;
                case 2:
                    System.out.print("Enter first word: ");
                    String word1 = scanner.nextLine();
                    System.out.print("Enter second word: ");
                    String word2 = scanner.nextLine();
                    System.out.println(queryBridgeWords(graph, word1, word2));
                    break;
                case 3:
                    System.out.print("Enter new text: ");
                    String newText = scanner.nextLine();
                    System.out.println(generateNewText(graph, newText));
                    break;
                case 4:
                    System.out.print("Enter start word: ");
                    String startWord = scanner.nextLine();
                    System.out.print("Enter end word: ");
                    String endWord = scanner.nextLine();
                    System.out.println(calcShortestPath(graph, startWord, endWord));
                    break;
                case 5:
                    System.out.println(randomWalk(graph));
                    break;
                case 6:
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void readFromFileAndGenerateGraph(Graph graph, String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            String prevWord = null;

            while ((line = br.readLine()) != null) {
                String[] words = line.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");

                for (String word : words) {
                    if (prevWord != null) {
                        graph.addEdge(prevWord, word);
                    }
                    prevWord = word;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String queryBridgeWords(Graph graph, String word1, String word2) {
        Map<String, Map<String, Integer>> adjacencyList = graph.getAdjacencyList();
        if (!adjacencyList.containsKey(word1) || !adjacencyList.containsKey(word2)) {
            return "No \"" + word1 + "\" or \"" + word2 + "\" in the graph!";
        }

        Set<String> bridgeWords = new HashSet<>();
        for (String w : adjacencyList.get(word1).keySet()) {
            if (adjacencyList.get(w).containsKey(word2)) {
                bridgeWords.add(w);
            }
        }

        if (bridgeWords.isEmpty()) {
            return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";
        } else {
            return "The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are: " + String.join(", ", bridgeWords);
        }
    }

    private static String generateNewText(Graph graph, String inputText) {
        String[] words = inputText.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length - 1; i++) {
            result.append(words[i]).append(" ");
            String bridgeWord = getRandomBridgeWord(graph, words[i], words[i + 1]);
            if (bridgeWord != null) {
                result.append(bridgeWord).append(" ");
            }
        }
        result.append(words[words.length - 1]);
        return result.toString();
    }

    private static String getRandomBridgeWord(Graph graph, String word1, String word2) {
        List<String> bridgeWords = new ArrayList<>();
        for (String w : graph.getAdjacencyList().getOrDefault(word1, Collections.emptyMap()).keySet()) {
            if (graph.getAdjacencyList().getOrDefault(w, Collections.emptyMap()).containsKey(word2)) {
                bridgeWords.add(w);
            }
        }
        if (bridgeWords.isEmpty()) {
            return null;
        } else {
            return bridgeWords.get(new Random().nextInt(bridgeWords.size()));
        }
    }

    private static String calcShortestPath(Graph graph, String startWord, String endWord) {
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get));

        distances.put(startWord, 0);
        queue.add(startWord);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(endWord)) {
                break;
            }
            for (Map.Entry<String, Integer> neighbor : graph.getAdjacencyList().getOrDefault(current, Collections.emptyMap()).entrySet()) {
                int newDist = distances.get(current) + neighbor.getValue();
                if (newDist < distances.getOrDefault(neighbor.getKey(), Integer.MAX_VALUE)) {
                    distances.put(neighbor.getKey(), newDist);
                    predecessors.put(neighbor.getKey(), current);
                    queue.add(neighbor.getKey());
                }
            }
        }

        if (!distances.containsKey(endWord)) {
            return "No path from \"" + startWord + "\" to \"" + endWord + "\"";
        }

        List<String> path = new LinkedList<>();
        for (String at = endWord; at != null; at = predecessors.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return "Shortest path from \"" + startWord + "\" to \"" + endWord + "\": " + String.join(" -> ", path) + " with length " + distances.get(endWord);
    }

    private static String randomWalk(Graph graph) {
        List<String> nodes = new ArrayList<>(graph.getNodes());
        if (nodes.isEmpty()) {
            return "The graph is empty!";
        }

        Random random = new Random();
        StringBuilder walk = new StringBuilder();
        String currentNode = nodes.get(random.nextInt(nodes.size()));

        while (true) {
            walk.append(currentNode).append(" ");
            Map<String, Integer> edges = graph.getAdjacencyList().get(currentNode);
            if (edges == null || edges.isEmpty()) {
                break;
            }
            List<String> neighbors = new ArrayList<>(edges.keySet());
            currentNode = neighbors.get(random.nextInt(neighbors.size()));
        }

        return walk.toString().trim();
    }
}