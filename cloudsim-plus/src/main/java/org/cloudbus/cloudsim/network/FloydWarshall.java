/*
 * @(#)FloydWarshall.java	ver 1.2  6/20/2005
 *
 * Modified by Weishuai Yang (wyang@cs.binghamton.edu).
 * Originally written by Rahul Simha
 *
 */
package org.cloudbus.cloudsim.network;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * <a href="https://en.wikipedia.org/wiki/Floyd–Warshall_algorithm">Floyd-Warshall algorithm</a> to calculate the predecessor matrix and the delay
 * between all pairs of nodes. The delay represents the distance between the two vertices and it works as the weight for the Floyd-Warshall algorithm.
 *
 * @author Rahul Simha
 * @author Weishuai Yang
 * @version 1.2, 6/20/2005
 * @since CloudSim Toolkit 1.0
 */
public class FloydWarshall {

    /**
     * Number of vertices (network nodes).
     */
    private final int numVertices;

    /**
     * List with the indexes of all vertices, from 0 to {@link #numVertices}-1.
     */
    private final List<Integer> vertices;

    /**
     * Weights when k is equal to -1.
     * (used for dynamic programming).
     */
    private double[][] dk_minus_one;

    /**
     * The predecessor matrix (used for dynamic programming).
     */
    private int[][] pk;

    /**
     * (used for dynamic programming).
     */
    private int[][] pk_minus_one;

    /**
     * Creates a matrix of network nodes.
     *
     * @param numVertices number of network nodes
     */
    public FloydWarshall(final int numVertices) {
        this.numVertices = numVertices;
        this.vertices = IntStream.range(0, numVertices).mapToObj(i -> i).collect(toList());
        dk_minus_one = new double[numVertices][numVertices];
        pk = new int[numVertices][numVertices];
        pk_minus_one = new int[numVertices][numVertices];
    }

    /**
     * Computes the shortest path between a vertex to all the other ones,
     * for all existing vertices.
     * This is represented by the delay between all pairs vertices.
     *
     * @param originalDelayMatrix original delay matrix
     * @return the new delay matrix (dk)
     */
    public double[][] computeShortestPaths(double[][] originalDelayMatrix) {
        savePreviousDelays(originalDelayMatrix);
        return computeShortestPaths();
    }

    /**
     * Computes the shortest path between a vertex to all the other ones,
     * for all existing vertices.
     * This is represented by the delay between all pairs vertices.
     *
     * @return the new delay matrix (dk)
     */
    private double[][] computeShortestPaths() {
        double[][] dk = new double[numVertices][numVertices];

        for(int k: vertices) {
            computeShortestPathForSpecificNumberOfHops(dk, k);
        }

        return dk;
    }

    /**
     * Computes the shortest path between a vertex to all the other ones,
     * for all existing vertices, limited to a maximum number of hops.
     * This is represented by the delay between all pairs vertices.
     *
     * @param dk the delay matrix to be updated
     * @param k maximum number of hops to try finding the shortest path between each vertex i and j
     */
    private void computeShortestPathForSpecificNumberOfHops(double[][] dk, int k) {
        for(int i: vertices) {
            computeShortestPathFromVertexToAllVertices(dk, k, i);
        }

        updateMatrices((i,j) -> {
            dk_minus_one[i][j] = dk[i][j];
            pk_minus_one[i][j] = pk[i][j];
        });
    }

    /**
     * Iterates over the path from one vertex to all the other ones, for all existing vertices,
     * updating some matrices as defined by a  {@link BiConsumer}.
     *
     * @param updater a {@link BiConsumer} that updates all elements of any matrices, where the parameters
     *                that this BiConsumer receives are the index i and j representing the path
     *                between two vertices, for all existing vertices
     */
    private void updateMatrices(BiConsumer<Integer, Integer> updater){
        for(int i: vertices) {
            for(int j: vertices) {
                updater.accept(i, j);
            }
        }
    }

    /**
     * Computes the shortest path between only a specific vertex to all the other ones,
     * limited to a maximum number of hops, and then updating the delay matrix.
     * This is represented by the delay between all pairs vertices.
     *
     * @param dk the delay matrix to be updated
     * @param k maximum number of hops to try finding the shortest path between each vertex i and j
     * @param i the index of the vertex to compute its distance to all the other vertices
     */
    private void computeShortestPathFromVertexToAllVertices(double[][] dk, int k, int i) {
        for(int j: vertices) {
            pk[i][j] = -1;
            if (i != j) {
                // D_k[i][j] = min ( D_k-1[i][j], D_k-1[i][k] + D_k-1[k][j].
                if (dk_minus_one[i][j] <= dk_minus_one[i][k] + dk_minus_one[k][j]) {
                    dk[i][j] = dk_minus_one[i][j];
                    pk[i][j] = pk_minus_one[i][j];
                } else {
                    dk[i][j] = dk_minus_one[i][k] + dk_minus_one[k][j];
                    pk[i][j] = pk_minus_one[k][j];
                }
            }
        }
    }

    /**
     * Saves the delay matrix before updating.
     *
     * @param originalDelayMatrix the original delay matrix
     */
    private void savePreviousDelays(double[][] originalDelayMatrix) {
        for(int i: vertices) {
            for(int j: vertices) {
                dk_minus_one[i][j] = Double.MAX_VALUE;
                pk_minus_one[i][j] = -1;
                if (originalDelayMatrix[i][j] != 0) {
                    dk_minus_one[i][j] = originalDelayMatrix[i][j];
                    pk_minus_one[i][j] = i;
                }
                // NOTE: we have set the value to infinity and will exploit this to avoid a comparison.
            }
        }
    }

    /**
     * Gets predecessor matrix.
     *
     * @return predecessor matrix
     */
    public int[][] getPk() {
        return pk;
    }

    public int getNumVertices(){
        return numVertices;
    }
}
