package net.bichal.bplb.gui.config;

import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class SearchEngine {
    private final List<SearchNode> nodes = new ArrayList<>(128);
    private final int[] matchBuffer = new int[64];

    public void index(String key, Text label, int tabIndex) {
        nodes.add(new SearchNode(key, label, tabIndex));
    }

    public List<SearchResult> search(String query) {
        if (query.isEmpty()) return Collections.emptyList();

        String[] queryTokens = query.toLowerCase().split("\\s+");
        List<SearchResult> results = new ArrayList<>(nodes.size());

        for (SearchNode node : nodes) {
            int score = calculateScore(node, queryTokens);
            if (score > 0) {
                results.add(new SearchResult(node.key, node.label, node.tabIndex, score));
            }
        }

        results.sort(Comparator.comparingInt(SearchResult::score).reversed());
        return results;
    }

    private int calculateScore(SearchNode node, String[] queryTokens) {
        int score = 0;
        String fullText = node.label.getString().toLowerCase();

        for (String qToken : queryTokens) {
            int idx = fullText.indexOf(qToken);
            if (idx >= 0) {
                score += 100;
                if (idx == 0) score += 50;
                for (String nToken : node.tokens) {
                    if (nToken.startsWith(qToken)) score += 25;
                }
            }
        }
        return score;
    }

    private static final class SearchNode {
        final String key;
        final Text label;
        final int tabIndex;
        final String[] tokens;
        int score;

        SearchNode(String key, Text label, int tabIndex) {
            this.key = key;
            this.label = label;
            this.tabIndex = tabIndex;
            this.tokens = label.getString().toLowerCase().split("\\s+");
        }
    }

    public record SearchResult(String key, Text label, int tabIndex, int score) {}
}