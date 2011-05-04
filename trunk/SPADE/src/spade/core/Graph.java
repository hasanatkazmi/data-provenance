/*
--------------------------------------------------------------------------------
SPADE - Support for Provenance Auditing in Distributed Environments.
Copyright (C) 2011 SRI International

This program is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
--------------------------------------------------------------------------------
 */
package spade.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Graph {

    // This class uses a set of vertices and a set of edges to represent the
    // graph.

    private Set<AbstractVertex> vertexSet;
    private Set<AbstractEdge> edgeSet;
    public String details;

    public Graph() {
        vertexSet = new HashSet<AbstractVertex>();
        edgeSet = new HashSet<AbstractEdge>();
        details = "";
    }

    public boolean putVertex(AbstractVertex inputVertex) {
        // Add the vertex to the vertex set. If the vertex is a network vertex,
        // then the networkTrigger() method is called.
        if (inputVertex.getAnnotation("type").equalsIgnoreCase("Network")) {
            networkTrigger(inputVertex);
        }
        return vertexSet.add(inputVertex);
    }

    public boolean putEdge(AbstractEdge inputEdge) {
        // Add the edge to the edge set.
        return edgeSet.add(inputEdge);
    }

    public Set<AbstractVertex> vertexSet() {
        return vertexSet;
    }

    public Set<AbstractEdge> edgeSet() {
        return edgeSet;
    }

    public void networkTrigger(AbstractVertex networkVertex) {
        try {
            Socket querySocket = new Socket("hostname", 3333);
            PrintStream socketOutputStream = new PrintStream(querySocket.getOutputStream(), true);
            BufferedReader socketInputStream = new BufferedReader(new InputStreamReader(querySocket.getInputStream()));
            socketOutputStream.println("query expression here");
            String inputLine;
            while ((inputLine = socketInputStream.readLine()) != null) {
                // process incoming text here
            }
        } catch (Exception exception) {

        }
    }

    // This method is used to create a new graph as an intersection of the two
    // given input graphs. This is done simply by using set functions on the
    // vertex and edge sets.
    public static Graph intersection(Graph graph1, Graph graph2) {
        Graph resultGraph = new Graph();
        Set<AbstractVertex> vertices = new HashSet<AbstractVertex>();
        Set<AbstractEdge> edges = new HashSet<AbstractEdge>();

        vertices.addAll(graph1.vertexSet());
        vertices.retainAll(graph2.vertexSet());
        edges.addAll(graph1.edgeSet());
        edges.retainAll(graph2.edgeSet());

        resultGraph.vertexSet().addAll(vertices);
        resultGraph.edgeSet().addAll(edges);

        return resultGraph;
    }

    // This method is used to create a new graph as a union of the two
    // given input graphs. This is done simply by using set functions on the
    // vertex and edge sets.
    public static Graph union(Graph inputLineage1, Graph inputLineage2) {
        Graph resultGraph = new Graph();
        Set<AbstractVertex> vertices = new HashSet<AbstractVertex>();
        Set<AbstractEdge> edges = new HashSet<AbstractEdge>();

        vertices.addAll(inputLineage1.vertexSet());
        vertices.addAll(inputLineage2.vertexSet());
        edges.addAll(inputLineage1.edgeSet());
        edges.addAll(inputLineage2.edgeSet());

        resultGraph.vertexSet().addAll(vertices);
        resultGraph.edgeSet().addAll(edges);

        return resultGraph;
    }

    // This method is used to export the graph to a given output stream. This is
    // useful for sending graph information across the network.
    public void export(PrintStream outputStream) {
        Iterator vertexIterator = vertexSet().iterator();
        while (vertexIterator.hasNext()) {
            AbstractVertex incomingVertex = (AbstractVertex) vertexIterator.next();
            String vertexString = "";
            Map<String, String> annotations = incomingVertex.getAnnotations();
            for (Iterator iterator = annotations.keySet().iterator(); iterator.hasNext();) {
                String key = (String) iterator.next();
                String value = (String) annotations.get(key);
                if ((key.equalsIgnoreCase("type")) || (key.equalsIgnoreCase("storageId"))) {
                    continue;
                }
                key = key.replaceAll(" ", "\\ ");
                key = key.replaceAll(":", "\\:");
                value = value.replaceAll(" ", "\\ ");
                value = value.replaceAll(":", "\\:");
                vertexString = vertexString + key + ":" + value + " ";
            }
            vertexString = vertexString.substring(0, vertexString.length() - 1);
            outputStream.print("id:" + incomingVertex.hashCode() + " ");
            outputStream.print("type:" + incomingVertex.getAnnotation("type") + " ");
            outputStream.print(vertexString + ";\n");
        }

        Iterator edgeIterator = edgeSet().iterator();
        while (edgeIterator.hasNext()) {
            AbstractEdge incomingEdge = (AbstractEdge) vertexIterator.next();
            String edgeString = "";
            Map<String, String> annotations = incomingEdge.getAnnotations();
            for (Iterator iterator = annotations.keySet().iterator(); iterator.hasNext();) {
                String key = (String) iterator.next();
                String value = (String) annotations.get(key);
                if ((key.equalsIgnoreCase("storageId")) || (key.equalsIgnoreCase("type"))) {
                    continue;
                }
                key = key.replaceAll(" ", "\\ ");
                key = key.replaceAll(":", "\\:");
                value = value.replaceAll(" ", "\\ ");
                value = value.replaceAll(":", "\\:");
                edgeString = edgeString + key + ":" + value + " ";
            }
            edgeString = edgeString.substring(0, edgeString.length() - 1);
            outputStream.print("id:" + incomingEdge.hashCode() + " ");
            outputStream.print("type:" + incomingEdge.getAnnotation("type") + " ");
            outputStream.print("from:" + incomingEdge.getSrcVertex().hashCode() + " ");
            outputStream.print("to:" + incomingEdge.getDstVertex().hashCode() + " ");
            outputStream.print(edgeString + ";\n");
        }
    }

    // This method is used to export the graph to a DOT file which is useful for
    // visualization.
    public void exportDOT(String path) {
        try {
            spade.storage.Graphviz outputStorage = new spade.storage.Graphviz();
            outputStorage.initialize(path);
            Iterator vertexIterator = vertexSet().iterator();
            Iterator edgeIterator = edgeSet().iterator();
            while (vertexIterator.hasNext()) {
                outputStorage.putVertex((AbstractVertex) vertexIterator.next());
            }
            while (edgeIterator.hasNext()) {
                outputStorage.putEdge((AbstractEdge) edgeIterator.next());
            }
            outputStorage.shutdown();
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
        }
    }
}
