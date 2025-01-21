package com.chess.engine.player.ai.OpeningBook;
import java.util.*;
public class OpeningBook {

    private final Map<String, List<WeightedMove>> book;

    public OpeningBook(String openingBookContent) {
        this.book = parseOpeningBook(openingBookContent);
    }

    private Map<String, List<WeightedMove>> parseOpeningBook(String content) {
        Map<String, List<WeightedMove>> bookMap = new HashMap<>();
        String[] lines = content.split("\n");

        String currentFen = null;
        for (String line : lines) {
            if (line.startsWith("pos")) {
                // Extract FEN from "pos ..."
                currentFen = line.substring(4).trim();
                bookMap.put(currentFen, new ArrayList<>());
            } else if (currentFen != null) {
                // Parse UCI moves and weights
                String[] parts = line.trim().split(" ");
                if (parts.length >= 2) {
                    String uciMove = parts[0];
                    int weight = Integer.parseInt(parts[1]);
                    bookMap.get(currentFen).add(new WeightedMove(uciMove, weight));
                }
            }
        }
        return bookMap;
    }

    public Optional<String> getBookMove(String fen, double randomnessFactor) {
        if (!book.containsKey(fen)) {
            return Optional.empty();
        }
        List<WeightedMove> moves = book.get(fen);

        // Weighted random selection
        int totalWeight = moves.stream().mapToInt(WeightedMove::weight).sum();
        double randomValue = Math.random() * totalWeight * randomnessFactor;

        for (WeightedMove move : moves) {
            randomValue -= move.weight();
            if (randomValue <= 0) {
                return Optional.of(move.uciMove());
            }
        }
        return Optional.of(moves.get(moves.size() - 1).uciMove()); // Fallback
    }

    private record WeightedMove(String uciMove, int weight) {}
}
