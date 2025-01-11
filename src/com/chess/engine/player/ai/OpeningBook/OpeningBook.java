package com.chess.engine.player.ai.OpeningBook;
import java.util.*;
import java.util.stream.Collectors;

public class OpeningBook {

    private final Map<String, List<BookMove>> movesByPosition;
    private final Random rng;

    public OpeningBook(String fileContent) {
        this.rng = new Random();
        this.movesByPosition = new HashMap<>();
        loadBook(fileContent);
    }

    // Load the opening book from a string
    private void loadBook(String fileContent) {
        String[] entries = fileContent.trim().split("pos");
        for (String entry : entries) {
            String[] lines = entry.trim().split("\n");
            if (lines.length > 0) {
                String fen = lines[0].trim();
                List<BookMove> bookMoves = Arrays.stream(lines, 1, lines.length)
                        .map(line -> {
                            String[] parts = line.split(" ");
                            return new BookMove(parts[0], Integer.parseInt(parts[1]));
                        })
                        .collect(Collectors.toList());
                movesByPosition.put(removeMoveCountersFromFEN(fen), bookMoves);
            }
        }
    }

    public boolean hasBookMove(String positionFen) {
        return movesByPosition.containsKey(removeMoveCountersFromFEN(positionFen));
    }

    public Optional<String> getBookMove(String positionFen, double weightPow) {
        final double clamlpedWeightPow = Math.max(0, Math.min(1, weightPow)); // Clamp weightPow between 0 and 1
        List<BookMove> moves = movesByPosition.get(removeMoveCountersFromFEN(positionFen));

        if (moves != null && !moves.isEmpty()) {
            int totalWeight = moves.stream().mapToInt(move -> weightedPlayCount(move.numTimesPlayed, clamlpedWeightPow)).sum();

            List<Double> cumulativeProbabilities = new ArrayList<>();
            double cumulative = 0.0;
            for (BookMove move : moves) {
                double probability = (double) weightedPlayCount(move.numTimesPlayed, weightPow) / totalWeight;
                cumulative += probability;
                cumulativeProbabilities.add(cumulative);
            }

            double randomValue = rng.nextDouble();
            for (int i = 0; i < moves.size(); i++) {
                if (randomValue <= cumulativeProbabilities.get(i)) {
                    return Optional.of(moves.get(i).moveString);
                }
            }
        }

        return Optional.empty();
    }

    private int weightedPlayCount(int playCount, double weightPow) {
        return (int) Math.ceil(Math.pow(playCount, weightPow));
    }

    private String removeMoveCountersFromFEN(String fen) {
        String[] fenParts = fen.split(" ");
        return String.join(" ", Arrays.copyOf(fenParts, 4));
    }

    public static class BookMove {
        public final String moveString;
        public final int numTimesPlayed;

        public BookMove(String moveString, int numTimesPlayed) {
            this.moveString = moveString;
            this.numTimesPlayed = numTimesPlayed;
        }
    }
}
