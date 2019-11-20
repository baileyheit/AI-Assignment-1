package searchclient;

import java.lang.reflect.Array;
import java.util.*;

import org.w3c.dom.Node;

public abstract class Heuristic
implements Comparator<State>
{
	boolean [][] walls;
	char [][] goals;
	Color [] boxColors;
	Color [] agentColors;
	List<HashMap<Node, Integer>> allGoals;
	HashMap<Node, Integer> goalDist;
	List<Character> allGoalsChars;

	class Node {
		int x;
		int y;

		Node(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	public Heuristic(State initialState)
	{
		// Here's a chance to pre-process the static parts of the level.
		walls = State.walls;
		goals = State.goals;
		boxColors = initialState.boxColors;
		agentColors = initialState.agentColors;
		allGoals = new ArrayList<>();
		//parallel array list to hold character for each goal

		allGoalsChars = new ArrayList<>();
		//could we improve this??
		int columnNum = 0;
		int rowLength = 0;
		for (int i = 0; i< goals.length; i++) {
			char[] row = goals[i];
			for(int b = 0; b < row.length; b++) {
				if(row.length > columnNum) {
					columnNum = row.length;
				}
			}
		}
		for (int x = 0; x < goals.length; x++) {
			int count = 0;
			char[] row = goals[x];
			boolean[][] visited = new boolean[goals.length][columnNum];
			for(int y = 0; y < row.length; y++){
				char c = row[y];
				//find the goals
				if(c != '\0' && !walls[x][y]){

					goalDist = new HashMap<>();

					//adding goal to goalDist, making visited true and creating queue
					goalDist.put(new Node(x,y), count);
					visited[x][y]= true;
					Queue<Node> q = new LinkedList<>();
					q.add(new Node(x,y));

					//perform BFS starting from goal, add each cell as node in goalDist HashMap
					//with value as distance from goal
					while(q.size() !=0) {
						Node curr = q.remove();
						visited[curr.x][curr.y]= true;
						count++;
						List<Node> neighbors = getNeighbors(walls, curr);
						for (Node neighbor : neighbors) {
							if (visited[neighbor.x][neighbor.y]== false) {
								q.add(neighbor);
								goalDist.put(new Node(neighbor.x, neighbor.y), count);
								visited[neighbor.x][neighbor.y] = true;
							}
						}
					}
					allGoals.add(goalDist);
					allGoalsChars.add(c);
				}
			}
		}
	}

	//gets neighbors of current node
	private List<Node> getNeighbors(boolean[][] matrix, Node node) {
		List<Node> neighbors = new ArrayList<Node>();

		//left
		if(isValidPoint(matrix, node.x - 1, node.y)) {
			neighbors.add(new Node(node.x - 1, node.y));
		}

		//right
		if(isValidPoint(matrix, node.x + 1, node.y)) {
			neighbors.add(new Node(node.x + 1, node.y));
		}

		//above
		if(isValidPoint(matrix, node.x, node.y - 1)) {
			neighbors.add(new Node(node.x, node.y - 1));
		}

		//below
		if(isValidPoint(matrix, node.x, node.y + 1)) {
			neighbors.add(new Node(node.x, node.y + 1));
		}

		return neighbors;
	}

	//checks if cell is out of bounds or a wall
	private boolean isValidPoint(boolean[][] matrix, int x, int y) {
		int rowNum = matrix.length - 2;
		int colNum = matrix[0].length - 1;

		boolean bool = !(x <= 0 || x >= rowNum || y <= 0 || y >= colNum || matrix[x][y]);
		return bool;
	}

	private boolean nodeEqulas(Node first, Node second){
		return(first.x==second.x && first.y==second.y);
	}


	public int h(State n){
		//sum of minimum distance of each box at each goal
		int heur = 0;
		char[][] boxes = n.boxes;
		//hash map of x/y indices. value == 0 if not visited. value == 1 if visited.
		HashMap<int[], Integer> boxes_heur = new HashMap<>();

		//finding boxes
		for (int x = 0; x < boxes.length; x++) {
			char[] row = boxes[x];
			for(int y = 0; y < row.length; y++){
				char c = row[y];
				if(c != '\0' && !walls[x][y]){
					//put indices and default 0 into hashmap
					int[]indices = new int[]{x,y};
					boxes_heur.put(indices, 0);
				}
			}
		}
		//loop through goals

		Integer minManDist = 1000000000;
		for (int k = 0; k < allGoals.size(); k++) {
			ArrayList<Integer> manDists = new ArrayList<>();
			//setting up boxes for current hash map
			HashMap<Node, Integer> currHashMap = allGoals.get(k);

			//initialize min values
			int[] minIndices = new int[2];
			Node minKey = new Node(0,0);
			int min = 1000000000;
			//finds box with closest distance to goal
			for(int[] indices : boxes_heur.keySet()) {
				// x/y values of current box
				int x = indices[0];
				int y = indices[1];
				Integer newVal = 0;
				Node box = new Node(0,0);
				//check if box equals goal character (A=A)
				if (boxes[x][y] == allGoalsChars.get(k)) {
					//loop through hashmap keys
					for(Node key: currHashMap.keySet()){
						// if hashmap key equals indices and not visited yet
						if(key.x==x && key.y == y && boxes_heur.get(indices) != 1){
							box = key;
							newVal = currHashMap.get(box);
							break;
						}
					}
					//get current val
					//check if minimum
					if (newVal < min) {
							minKey = box;
							min = newVal;
							minIndices = indices;
					}
				}
			}
			heur += min;
			//change hashmap value of min indices to 1 to indicate visited
			boxes_heur.put(minIndices, 1);

			if(min==0){
				continue;
			}
			//indices of min box to goal
			int boxX = minIndices[0];
			int boxY = minIndices[1];

			int[] agentRows = n.agentRows;
			int[] agentCols = n.agentCols;

			//indices of agent position
			int agentX = agentRows[0];
			int agentY = agentCols[0];

			//manhatten distance
			int manDist = Math.abs(boxX-agentX) + Math.abs(boxY-agentY);

			//add to heuristic
			heur += manDist;
		}
		return heur;
	}

	public abstract int f(State n);

	@Override
	public int compare(State n1, State n2)
	{
		return this.f(n1) - this.f(n2);
	}
}

class HeuristicAStar
extends Heuristic
{
	public HeuristicAStar(State initialState)
	{
		super(initialState);
	}

	@Override
	public int f(State n)
	{
		return n.g() + this.h(n);
	}

	@Override
	public String toString()
	{
		return "A* evaluation";
	}
}

class HeuristicWeightedAStar
extends Heuristic
{
	private int w;

	public HeuristicWeightedAStar(State initialState, int w)
	{
		super(initialState);
		this.w = w;
	}

	@Override
	public int f(State n)
	{
		return n.g() + this.w * this.h(n);
	}

	@Override
	public String toString()
	{
		return String.format("WA*(%d) evaluation", this.w);
	}
}

class HeuristicGreedy
extends Heuristic
{
	public HeuristicGreedy(State initialState)
	{
		super(initialState);
	}

	@Override
	public int f(State n)
	{
		return this.h(n);
	}

	@Override
	public String toString()
	{
		return "greedy evaluation";
	}
}
