package candlelight;

import candlelight.payload.VMatrix;
import candlelight.payload.SCC;
import candlelight.payload.WCC;
import it.unimi.dsi.fastutil.ints.*;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static candlelight.Constants.MAX_MATRIX_VALUE;

public class GraphUtil {
    public static void printNodeAttribute(Graph graph, String name, int nodeId) {
        for (Node n : graph.getNodes()) {
            if (n.getStoreId() == nodeId) {
                System.out.println(n.getAttribute(name));
            }
        }
    }

    public static FastGraph ofComponent(int[] component, FastGraph graph) {
        FastGraph nGraph = new FastGraph(component.length);

        for (int v0 : component) {
            for (int v1 : component) {
                if (graph.hasEdge(v0, v1)) {
                    nGraph.addEdge(v0, v1);
                }
            }
        }

        return nGraph;
    }

    public static SCC stronglyConnectedComponents(FastGraph graph) {
        boolean[] visited = new boolean[graph.V()];

        IntStack stack = new IntArrayList();

        for (int i = 0; i < graph.V(); i++)
            if (!visited[i])
                graph.fillOrder(i, visited, stack);

        graph = graph.makeTranspose();

        Arrays.fill(visited, false);

        List<int[]> components = new ArrayList<>();
        IntList temp = new IntArrayList();

        while (!stack.isEmpty()) {
            int v = stack.popInt();

            if (!visited[v]) {
                graph.dfs(v, visited, temp);
                components.add(temp.toIntArray());
                temp.clear();
            }
        }

        return new SCC(components.toArray(new int[0][]));
    }

    public static WCC weaklyConnectedComponents(FastGraph graph) {
        boolean[] visited = new boolean[graph.V()];

        IntStack nodes = IntStream.range(0, graph.V()).collect(
                (Supplier<IntStack>) IntArrayList::new,
                IntStack::push,
                (integers, integers2) -> { }
        );

        graph = graph.makeUndirected();

        IntList temp = new IntArrayList();
        List<int[]> components = new ArrayList<>();

        while (!nodes.isEmpty()) {
            int from = nodes.popInt();

            if (!visited[from]) {
                graph.dfs(from, visited, temp);
                components.add(temp.toIntArray());
                temp.clear();
            }
        }

        return new WCC(components.toArray(new int[0][]));
    }

    public static IntList getDegrees(FastGraph graph) {
        IntList degrees = new IntArrayList();

        for (int v : graph.getVertices()) {
            degrees.add(graph.getEdges(v).size());
        }

        return degrees;
    }

    public static float getUndirectedAverageDegree(FastGraph graph) {
        return (float) graph.E() / graph.V();
    }

    public static int getMaxDegree(FastGraph graph) {
        int max = 0;

        for (int v : graph.getVertices()) {
            max = Math.max(graph.getEdges(v).size(), max);
        }

        return max;
    }

    public static VMatrix undirShortestPaths(FastGraph graph) {
        VMatrix res = new VMatrix();

        for (int v0 : graph.getVertices()) {
            for (int v1 : graph.getVertices()) {
                if (graph.hasEdge(v0, v1)) {
                    res.put(v0, v1, 1);
                    res.put(v1, v0, 1);
                }

                else {
                    res.put(v0, v1, MAX_MATRIX_VALUE);
                    res.put(v1, v0, MAX_MATRIX_VALUE);
                }
            }
        }

        for (int k : graph.getVertices()) {
            for (int i : graph.getVertices()) {
                for (int j : graph.getVertices()) {
                    int val = Math.min(res.get(i, j), res.get(i, k) + res.get(k, j));
                    res.put(i, j, val);
                }
            }
        }

        return res;
    }

    public static float commonNeighborsIndex(FastGraph graph, int v0, int v1) {
        IntSet intersection = new IntOpenHashSet(graph.getEdges(v0));
        intersection.retainAll(graph.getEdges(v1));

        return intersection.size();
    }

    public static float jaccardsIndex(FastGraph graph, int v0, int v1) {
        IntSet intersection = new IntOpenHashSet(graph.getEdges(v0));
        intersection.retainAll(graph.getEdges(v1));

        IntSet union = new IntOpenHashSet(graph.getEdges(v0));
        union.addAll(graph.getEdges(v1));

        return (float) intersection.size() / union.size();
    }

    public static float adamicAdarIndex(FastGraph graph, int v0, int v1) {
        IntSet intersection = new IntOpenHashSet(graph.getEdges(v0));
        intersection.retainAll(graph.getEdges(v1));

        float res = 0;

        for (int v : intersection) {
            res = res + 1 / (float) Math.log(graph.getEdges(v).size());
        }

        return res;
    }

    public static float preferentialAttachmentIndex(FastGraph graph, int v0, int v1) {
        return graph.getEdges(v0).size() * graph.getEdges(v1).size();
    }

    public static float degreeCenrality(FastGraph graph, int v) {
        return graph.getEdges(v).size();
    }

    public static float closenessCentrality(FastGraph graph, int v) {
        VMatrix paths = undirShortestPaths(graph);

        IntSet vertices = graph.getVertices();

        float s = 0;
        for (int e : vertices)
            s += paths.get(v, e);

        return (vertices.size() - 1) / s;
    }

    public static float betweennessCentrality(FastGraph graph, int v) {

    }
}