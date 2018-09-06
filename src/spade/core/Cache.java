/*
 --------------------------------------------------------------------------------
 SPADE - Support for Provenance Auditing in Distributed Environments.
 Copyright (C) 2017 SRI International

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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * This Singleton class encapsulates the caching policy
 * and implementation for graph vertices and edges.
 *
 * @author Raza Ahmad
 */
public class Cache implements Serializable
{
    private static Set<Graph> cachedGraphs;
    private static Graph mainGraphCache;
    private static Cache cacheInstance;

    public static Cache getInstance()
    {
        if(cacheInstance == null)
        {
            cacheInstance = new Cache();
        }
        return cacheInstance;
    }

    private Cache()
    {
        cachedGraphs = new HashSet<>();
        mainGraphCache = new Graph();
    }

    public void removeGraph(Graph graph)
    {
        cachedGraphs.remove(graph);
        mainGraphCache.remove(graph);
    }

    public static Graph findValidResponse(String query, String queryTime)
    {
        // TODO: implement the policy of finding response matches
        // if (queryTime > responseTime + TTL); then return null
        for(Graph graph : cachedGraphs)
        {
            if(graph.getQueryString().equals(query))
            {
                return graph;
            }
        }
        return null;
    }

    public void addGraph(Graph graph)
    {
        cachedGraphs.add(graph);
    }

    public void addVertex(AbstractVertex vertex)
    {
        mainGraphCache.putVertex(vertex);
    }

    public void addEdge(AbstractEdge edge)
    {
        mainGraphCache.putEdge(edge);
    }

    public Graph getCache()
    {
        return mainGraphCache;
    }

    public int getVertexCount()
    {
        return mainGraphCache.vertexSet().size();
    }

    public int getEdgeCount()
    {
        return mainGraphCache.edgeSet().size();
    }

    public int getSize()
    {
        return getVertexCount() + getEdgeCount();
    }

    public static boolean isVertexPresent(String vertexHash)
    {
        return false;
    }

    public static boolean isEdgePresent(String edgeHash)
    {
        return false;
    }

    public static void addItem(AbstractEdge edge)
    {
    }

    public static void addItem(AbstractVertex vertex)
    {
    }
}
