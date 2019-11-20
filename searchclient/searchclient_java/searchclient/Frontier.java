package searchclient;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public interface Frontier
{
    void add(State state);
    State pop();
    boolean isEmpty();
    int size();
    boolean contains(State state);
    String getName();
}

class FrontierBFS
        implements Frontier
{
    private final ArrayDeque<State> queue = new ArrayDeque<>(65536);
    private final HashSet<State> set = new HashSet<>(65536);

    @Override
    public void add(State state)
    {
        this.queue.addLast(state);
        this.set.add(state);
    }

    @Override
    public State pop()
    {
        State state = this.queue.pollFirst();
        this.set.remove(state);
        return state;
    }

    @Override
    public boolean isEmpty()
    {
        return this.queue.isEmpty();
    }

    @Override
    public int size()
    {
        return this.queue.size();
    }

    @Override
    public boolean contains(State state)
    {
        return this.set.contains(state);
    }

    @Override
    public String getName()
    {
        return "breadth-first search";
    }
}

class FrontierDFS
        implements Frontier
{
	 private final ArrayList<State> list = new ArrayList<>(65536);
	 private final HashSet<State> set = new HashSet<>(65536);

    @Override
    public void add(State state)
    {
    	 this.list.add(state);
       this.set.add(state);
    }

    @Override
    public State pop()
    {
    	State state = this.list.get(list.size()-1);
    	this.list.remove(state);
        this.set.remove(state);
        return state;
    }

    @Override
    public boolean isEmpty()
    {
        return this.list.isEmpty();
    }

    @Override
    public int size()
    {
        return this.list.size();
    }

    @Override
    public boolean contains(State state)
    {
        return this.set.contains(state);
    }

    @Override
    public String getName()
    {
        return "depth-first search";
    }
}

class FrontierBestFirst
        implements Frontier
{
    private Heuristic heuristic;
    private PriorityQueue<State> pQueue;
    private HashSet<State> stateSet = new HashSet<State>();

    public FrontierBestFirst(Heuristic h)
    {
        this.heuristic = h;
        pQueue = new PriorityQueue<State>(h);
    }

    @Override
    public void add(State state)
    {
        pQueue.add(state);
        stateSet.add(state);
    }

    @Override
    public State pop()
    {
        State minNode = pQueue.poll();
        stateSet.remove(minNode);
        return minNode;
    }

    @Override
    public boolean isEmpty()
    {
        return pQueue.isEmpty();
    }

    @Override
    public int size()
    {
        return pQueue.size();
    }

    @Override
    public boolean contains(State state)
    {
        return pQueue.contains(state);
    }

    @Override
    public String getName()
    {
        return String.format("best-first search using %s", this.heuristic.toString());
    }
}
