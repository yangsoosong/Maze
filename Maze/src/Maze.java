// Assignment 10, Pt. 2: Maze Game

import java.util.*;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

/*
Justin Xia: xiaju
Erica Yee: ericayee
Yangsoo Song: songyang25
*/

// to represent a maze game
class Maze extends World {
    
    // Constants: use to change game size
    // defines the maze width (in cells, up to 100)
    static final int MAZE_WIDTH = 30;
    // defines the maze height (in cells, up to 60)
    static final int MAZE_HEIGHT = 30;
    // defines the cell dimensions (in pixels)
    static final int CELL_SIZE = 
            Math.min(600 / Maze.MAZE_HEIGHT, 1500 / Maze.MAZE_WIDTH);
    
    // fields of Maze
    //hashmap to represent representatives of each node
    HashMap<Node, Node> representatives;
    //represent the edges in the final hashmap
    ArrayList<Edge> edgesInTree;
    //worklist of all edges available in the maze, sorted by edge weights
    ArrayList<Edge> worklist;
    int row; // number of rows in maze
    int col; // number of columns in maze
    int nCells; // number of cells in maze
    ArrayList<ArrayList<Node>> maze; // represent the maze as a 2d array
    ArrayList<Node> mazeList; // represent the maze as a single list
    Player p1; // represent a player manually traversing the maze
    HashMap<Node, Node> cameFromEdge;
    ArrayList<Node> shortestPath; // represent the shortest path to finish maze
    ArrayList<Node> visited; // represents all visited nodes
    
    int time; // represents time elapsed
    boolean togglePath; // toggle for showing player's path
    // if true, animate the selected path
    boolean toggleSearch;
    int bfsLength;
    int dfsLength;
    
    // constructor
    Maze() {
        this.row = MAZE_HEIGHT;
        this.col = MAZE_WIDTH;
        this.nCells = MAZE_WIDTH * MAZE_HEIGHT;
        
        this.initMaze();
        this.initRep();
        this.initWorkList();
        this.initMazeList();
        this.kruskalAlgo();
        this.makePath();      
        cameFromEdge = new HashMap<Node, Node>();
        p1 = new Player(0, 0, 0);
        shortestPath = new ArrayList<Node>();
        visited = new ArrayList<Node>();
        this.time = 0;
        togglePath = false;
        toggleSearch = false;
        this.bfs(this.maze.get(0).get(0),
                this.maze.get(Maze.MAZE_HEIGHT - 1).get(
                        Maze.MAZE_WIDTH - 1));
        bfsLength = this.visited.size();
        this.constructShortestPath();
        this.dfs(this.maze.get(0).get(0),
                this.maze.get(Maze.MAZE_HEIGHT - 1).get(
                        Maze.MAZE_WIDTH - 1));
        dfsLength = this.visited.size();
    }

    // EFFECT: changes the world based on key presses
    public void onKeyEvent(String key) {
        // pressing "r" creates a new random maze
        if (key.equals("r")) {
            this.initMaze();
            this.initRep();
            this.initWorkList();
            this.initMazeList();
            this.kruskalAlgo();
            this.makePath();
            this.makeScene();
            this.shortestPath = new ArrayList<Node>();
            this.p1 = new Player(0, 0, 0);
            this.time = 0;
            toggleSearch = false;
            this.bfs(this.maze.get(0).get(0),
                    this.maze.get(Maze.MAZE_HEIGHT - 1).get(
                            Maze.MAZE_WIDTH - 1));
            bfsLength = this.visited.size();
            this.constructShortestPath();
            this.dfs(this.maze.get(0).get(0),
                    this.maze.get(Maze.MAZE_HEIGHT - 1).get(
                            Maze.MAZE_WIDTH - 1));
            dfsLength = this.visited.size();
        }
        // use breadth-first search to find the path
        if (key.equals("b")) {
            this.time = 0;
            this.bfs(this.maze.get(0).get(0), 
                this.maze.get(MAZE_HEIGHT - 1).get(MAZE_WIDTH - 1));
            toggleSearch = true;
        }
        // use depth-first search to find the path
        if (key.equals("d")) {
            this.time = 0;
            this.dfs(this.maze.get(0).get(0), 
                    this.maze.get(MAZE_HEIGHT - 1).get(MAZE_WIDTH - 1));
            toggleSearch = true;
        }
        if (key.equals("left") && p1.col - 1 >= 0) {
            p1.movePlayer(maze.get(p1.row).get(p1.col - 1), key);
        }
        if (key.equals("right") && p1.col + 1 < Maze.MAZE_WIDTH) {
            p1.movePlayer(maze.get(p1.row).get(p1.col + 1), key);
        }
        if (key.equals("up") && p1.row - 1 >= 0) {
            p1.movePlayer(maze.get(p1.row - 1).get(p1.col), key);
        }
        if (key.equals("down") && p1.row + 1 < Maze.MAZE_HEIGHT) {
            p1.movePlayer(maze.get(p1.row + 1).get(p1.col), key);
        }
        if (key.equals("t")) {
            togglePath = !togglePath;
        }
    }
    
    // initialize the hashmap that describes the representatives of each node
    // to the node itself
    HashMap<Node, Node> initRep() {
        representatives = new HashMap<Node, Node>();
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                representatives.put(maze.get(i).get(j), maze.get(i).get(j));
            }
        }
        return representatives;
    }
    
    
    // initialize the nodes of the maze. set all the walls to up initially
    ArrayList<ArrayList<Node>> initMaze() {
        maze = new ArrayList<ArrayList<Node>>();
        for (int i = 0; i < this.row; i++) {
            maze.add(new ArrayList<Node>());
            for (int j = 0; j < this.col; j++) {
                maze.get(i).add(new Node(i, j, (i * col) + j, 
                        true, true, true, true));
            }
        }
        return maze;
    }
    
    
    // initialize the 1d maze like the 2d maze
    ArrayList<Node> initMazeList() {
        mazeList = new ArrayList<Node>();
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                mazeList.add(maze.get(i).get(j));
            }
        }
        return mazeList;
    }
    
    // initialize the worklist of all possible edges. Set random weights to
    // each edge
    ArrayList<Edge> initWorkList() {
        worklist = new ArrayList<Edge>();
        Random rand = new Random();
        
        for (int i = 0; i < this.row; i++) {
            for (int j = 0; j < this.col; j++) {
                if (i != this.row - 1) {
                    worklist.add(new Edge(maze.get(i).get(j), 
                        maze.get(i + 1).get(j), rand.nextInt(nCells)));
                }
                if (j != this.col - 1) {
                    worklist.add(new Edge(maze.get(i).get(j), 
                        maze.get(i).get(j + 1), rand.nextInt(nCells)));
                }
            }
        }
        
        // sort the worklist in ascending order
        Collections.sort(worklist);
        return worklist;
    }
    
    // return completed spanning tree (list of edges) using kruskal's algorithm
    ArrayList<Edge> kruskalAlgo() {
        edgesInTree = new ArrayList<Edge>();
        // create minimum spanning tree
        while ((edgesInTree.size() < mazeList.size() - 1)) {
            // find shortest edge (first in list)
            Edge first = this.worklist.get(0);
            // does this edge create a cycle? (union/find) 
            if (this.find(representatives, first.a) ==
                    (this.find(representatives, first.b))) {
                this.worklist.remove(first);
            }
            else {
                edgesInTree.add(first);
                union(representatives, find(representatives, first.a), 
                        find(representatives, first.b));
                union(representatives, find(representatives, first.b), 
                        find(representatives, first.a));
                this.worklist.remove(first);
            }
        }
        return edgesInTree;
    }
    
    // if a node name maps to itself, then it is the representative; otherwise,
    // follow the links in the representatives map, and recursively look up 
    // the representative for the current node's parent.
    Node find(HashMap<Node, Node> hm, Node key) {
        Node temp = key;
        if (hm.get(key) == (key)) {
            return temp;
        }
        else {
            temp = hm.get(key);
            return this.find(hm, temp);
        }
    }
    
    // links a representative to another representative
    HashMap<Node, Node> union(HashMap<Node, Node> hm, Node link, Node n) {
        hm.put(n, link);
        return hm;
    }
    
    // determines correct walls for each node
    void makePath() {
        for (Edge e : this.edgesInTree) {
            if (e.b.col - e.a.col == 1) {
                e.b.left = false;
                e.a.right = false;
            }

            if (e.b.row - e.a.row == 1) {
                e.b.up = false;
                e.a.down = false;
            }
        }
    }
    
    // draw the maze
    // first cell is green, last cell is purple
    public WorldScene makeScene() {
        WorldScene ws =  
                new WorldScene(row * CELL_SIZE, col * CELL_SIZE);
        this.drawBG(ws);
        this.drawEP(ws);
        
        if (togglePath) {
            this.drawTravelled(ws);
        }
        
        this.drawVisited(ws);

        p1.drawPlayer(ws);
        
        for (Node n : mazeList) {
            n.drawLines(ws);
        }
        this.drawBotRight(ws); 
        
        this.winScene(ws);
        
        return ws;
    }
    
    // draw the bottom and right walls of the maze
    void drawBotRight(WorldScene ws) {

        ws.placeImageXY(new LineImage(new Posn(0, this.row * CELL_SIZE), 
                Color.BLACK), this.col * CELL_SIZE, 
                this.row * CELL_SIZE / 2);
        ws.placeImageXY(new LineImage(new Posn(this.col * CELL_SIZE, 0), 
                Color.BLACK), this.col * CELL_SIZE / 2, 
                this.row * CELL_SIZE);
    }
    
    // breadth first search
    boolean bfs(Node from, Node to) {
        return searchHelp(from, to, new Queue<Node>());
    }
    
    // depth first search
    boolean dfs(Node from, Node to) {
        return searchHelp(from, to, new Stack<Node>());
    }
    
    // helper for breadth first and depth first search
    boolean searchHelp(Node from, Node to, ICollection<Node> worklist) {
        Deque<Node> alreadySeen = new Deque<Node>();
        this.cameFromEdge = new HashMap<Node, Node>();
        this.visited = new ArrayList<Node>();
       
        // Initialize the worklist with the from Node
        worklist.add(from);
        // As long as the worklist isn't empty...
        while (!worklist.isEmpty()) {
            Node next = worklist.remove();
            if (next.equals(to)) {
                visited.add(next);
                this.constructFinalPath();
                return true; // Success!
            }
            else if (alreadySeen.contains(next)) {
                // do nothing: we've already seen this one
            }
            else {
                // add next to visited
                visited.add(next);
                // add all the neighbors of next to the worklist for further 
                // processing
                if (!next.up) {
                    worklist.add(maze.get(next.row - 1).get(next.col));
                    if (!alreadySeen.contains(maze.get(next.row - 1).get(next.col))) {
                        cameFromEdge.put(maze.get(next.row - 1).get(next.col), next);
                    }
                }
                if (!next.left) {
                    worklist.add(maze.get(next.row).get(next.col - 1));
                    if (!alreadySeen.contains(maze.get(next.row).get(next.col - 1))) {
                        cameFromEdge.put(maze.get(next.row).get(next.col - 1), next);
                    }
                }
                if (!next.right) {
                    worklist.add(maze.get(next.row).get(next.col + 1));
                    if (!alreadySeen.contains(maze.get(next.row).get(next.col + 1))) {
                        cameFromEdge.put(maze.get(next.row).get(next.col + 1), next);
                    }
                }
                if (!next.down) {
                    worklist.add(maze.get(next.row + 1).get(next.col));
                    if (!alreadySeen.contains(maze.get(next.row + 1).get(next.col))) {
                        cameFromEdge.put(maze.get(next.row + 1).get(next.col), next);
                    }
                }
                // add next to alreadySeen, since we're done with it
                alreadySeen.addAtHead(next);
            }
        }
        // We haven't found the to vertex, and there are no more to try
        return false;
    }
    
    // construct the visited path
    ArrayList<Node> constructFinalPath() {
        Node next = maze.get(Maze.MAZE_HEIGHT - 1)
                .get(Maze.MAZE_WIDTH - 1);
        ArrayList<Node> finalPath = new ArrayList<Node>();
        next.partOfFinalPath = true;
        
        while (next != maze.get(0).get(0)) {
            next = cameFromEdge.get(next);
            next.partOfFinalPath = true;
            finalPath.add(next);
        }
        
        return finalPath;
    }
    
    // construct the shortest path
    ArrayList<Node> constructShortestPath() {
        for (Node n : this.mazeList) {
            if (n.partOfFinalPath) {
                shortestPath.add(n);
            }
        }
        
        return shortestPath;
    }
    
    // draw background of scene grey
    WorldScene drawBG(WorldScene ws) {
        ws.placeImageXY(new RectangleImage(Maze.CELL_SIZE * Maze.MAZE_WIDTH,
                Maze.CELL_SIZE * Maze.MAZE_HEIGHT, OutlineMode.SOLID, 
                Color.gray), 
                Maze.CELL_SIZE * Maze.MAZE_WIDTH / 2, 
                Maze.CELL_SIZE * Maze.MAZE_HEIGHT / 2);
        return ws;
    }
    
    // EFFECT: changes the state of the world after each tick
    public void onTick() {
        this.time = this.time + 1;
    }
    
    // animate visited cells
    public void drawVisited(WorldScene ws) {
        String efText = "";
        if (this.toggleSearch) {
            for (int i = 0; i < Math.min(visited.size(), time); i++) {
                ws.placeImageXY(new RectangleImage(Maze.CELL_SIZE, Maze.CELL_SIZE,
                        OutlineMode.SOLID, visited.get(i).visitCol(time - visited.size())), 
                        visited.get(i).col * Maze.CELL_SIZE + Maze.CELL_SIZE / 2,
                        visited.get(i).row * Maze.CELL_SIZE + Maze.CELL_SIZE / 2); 
            }
            
            if (bfsLength > dfsLength) {
                efText = "DFS was more efficient by: " 
                        + (bfsLength - dfsLength);
            }
            else {
                efText = "BFS was more efficient by: " 
                        + (dfsLength - bfsLength);
            }
        }
        ws.placeImageXY(
                new TextImage(efText, Maze.MAZE_WIDTH * Maze.CELL_SIZE / 20
                        , Color.WHITE), 
                MAZE_WIDTH * CELL_SIZE / 2, MAZE_HEIGHT * 5);
        
    }

    // notify player they won and return the number of wrong moves
    public void winScene(WorldScene ws) {
        int totMoves = 0;
        if (p1.row == (this.row - 1) && 
                p1.col == (this.col - 1)) {
            toggleSearch = false;
            for (Node n : this.mazeList) {
                if (n.travelled) {
                    n.drawCell(Color.ORANGE, ws);
                    totMoves++;
                }
                
                if (n.partOfFinalPath) {
                    n.drawCell(Color.magenta, ws);  
                }
            }
            ws.placeImageXY(
                    new TextImage("You Win!!", 
                            Maze.MAZE_WIDTH * Maze.CELL_SIZE / 20, Color.WHITE), 
                    MAZE_WIDTH * CELL_SIZE / 2, MAZE_HEIGHT * 8);
            
            String wmText = "Wrong Moves: " + 
                    (totMoves - shortestPath.size() + 1);
            
            ws.placeImageXY(
                    new TextImage(wmText, 
                            Maze.MAZE_WIDTH * Maze.CELL_SIZE / 20, Color.WHITE), 
                    MAZE_WIDTH * CELL_SIZE / 2, MAZE_HEIGHT * 14);
        }
    }
    
    // color in the top left and bottom right cells
    void drawEP(WorldScene ws) {
        Node toDraw = this.maze.get(0).get(0);
        toDraw.drawCell(Color.GREEN, ws);
        toDraw = this.maze.get(Maze.MAZE_HEIGHT - 1).get(Maze.MAZE_WIDTH - 1);
        toDraw.drawCell(Color.BLUE, ws);
    }
    
    // color the cells that have been travelled to
    WorldScene drawTravelled(WorldScene ws) {
        for (Node n : this.mazeList) {
            if (n.travelled) {
                n.drawCell(Color.ORANGE, ws);
            }
        }
        
        return ws;
    }
}

// to represent a node
class Node extends DNode<Integer> {
    // row of node
    int row;
    // column of node
    int col; 
    // number of node from left to right, top to bottom
    int nCell;
    // represent neighboring walls
    boolean left;
    boolean right;
    boolean up;
    boolean down;
    boolean partOfFinalPath;
    boolean travelled;
    
    Node(int row, int col, int nCell, boolean left, boolean right, boolean up, 
            boolean down) {
        super(nCell);
        this.row = row;
        this.col = col;
        this.nCell = nCell;
        this.left = left;
        this.right = right;
        this.up = up;
        this.down = down;
        partOfFinalPath = false;
        travelled = false;
    }
    
    // draw cell grid lines
    WorldScene drawLines(WorldScene ws) {
        if (this.left) {
            ws.placeImageXY(new LineImage(new Posn(0, Maze.CELL_SIZE), 
                    Color.BLACK), this.col * Maze.CELL_SIZE,
                    this.row * Maze.CELL_SIZE + Maze.CELL_SIZE / 2);
        }
        if (this.up) {
            ws.placeImageXY(new LineImage(new Posn(Maze.CELL_SIZE, 0), 
                    Color.BLACK), this.col * 
                    Maze.CELL_SIZE + Maze.CELL_SIZE / 2,
                    this.row * Maze.CELL_SIZE);
        }
        return ws;
    }
    
    // determines color of cell when visited for animating the search
    // final path color is dark grey until entire path has been animated
    Color visitCol(int t) {
        if (this.partOfFinalPath && t > -1) {
            return Color.lightGray;
        }
        else {
            return Color.darkGray;
        }
    }
    
    // draw Cell using given color
    void drawCell(Color col, WorldScene ws) {
        ws.placeImageXY(new RectangleImage(Maze.CELL_SIZE, Maze.CELL_SIZE,
                OutlineMode.SOLID, col), 
                this.col * Maze.CELL_SIZE + Maze.CELL_SIZE / 2,
                this.row * Maze.CELL_SIZE + Maze.CELL_SIZE / 2);  
    }
}

// to represent an edge
class Edge implements Comparable<Edge> {
    // represent nodes that make an edge
    Node a; 
    Node b;
    // weight of the edge
    int weight;
    
    Edge(Node a, Node b, int weight) {
        this.a = a;
        this.b = b;
        this.weight = weight;
    }
    
    // comparator for Collections.sort()
    public int compareTo(Edge e) {
        return this.weight - e.weight;
    }
}

// to represent a player manually moving through the maze
class Player {
    // coordinates of player on maze
    int row;
    int col;
    int nCell; // Number of the cell that the player is on.
    
    Player(int row, int col, int nCell) {
        this.row = row;
        this.col = col;
        this.nCell = nCell;
    }
    
    // move the player to the given cell
    public Player movePlayer(Node cell, String key) {
        if (key.equals("left") && !cell.right) {
            this.col = col - 1;
            cell.travelled = true;
        }
        if (key.equals("right") && !cell.left) {
            this.col = col + 1;
            cell.travelled = true;
        }
        if (key.equals("up") && !cell.down) {
            this.row = row - 1;
            cell.travelled = true;
        }
        if (key.equals("down") && !cell.up) {
            this.row = row + 1;
            cell.travelled = true;
        }
        return this;
    }
    
    // draw the player on its current cell
    public WorldScene drawPlayer(WorldScene ws) {
        ws.placeImageXY(new RectangleImage(Maze.CELL_SIZE, Maze.CELL_SIZE,
                OutlineMode.SOLID, Color.CYAN), 
                this.col * Maze.CELL_SIZE + Maze.CELL_SIZE / 2,
                this.row * Maze.CELL_SIZE + Maze.CELL_SIZE / 2);
        return ws;
    }
}

// to represent examples and tests
class ExampleMaze {
    
    // runs the game
    void testMaze(Tester t) {
        this.init();
        
        this.m1.bigBang(Maze.MAZE_WIDTH * Maze.CELL_SIZE, 
                Maze.MAZE_HEIGHT * Maze.CELL_SIZE, .1);
    }
    
    Maze m1;
    
    /* A-----B-----C
     * |     |     |
     * |  1  12 2  |
     * |     |     |
     * D--10-E--4--F
     * |     |     |
     * |  3  9  4  |
     * |     |     |
     * G-----H-----J
     */
    
    Node n1 = new Node(1, 1, 1, true, true, true, true);
    Node n2 = new Node(1, 2, 2, true, true, true, true);
    Node n3 = new Node(1, 3, 3, true, true, true, true);
    Node n4 = new Node(2, 1, 4, true, true, true, true);
    Node n5 = new Node(1, 1, 1, false, false, false, false);
    
    Edge be = new Edge(this.n1, this.n2, 12);
    Edge de = new Edge(this.n1, this.n3, 10);
    Edge ef = new Edge(this.n2, this.n4, 4);
    Edge eh = new Edge(this.n3, this.n4, 9);
    
    ArrayList<Node> arr1 = new ArrayList<Node>();
    ArrayList<Edge> arr2 = new ArrayList<Edge>();
    ArrayList<Integer> arr3 = new ArrayList<Integer>();
    HashMap<Node, Node> hm1 = new HashMap<Node, Node>();
    
    ArrayList<Edge> edgesInTree = new ArrayList<Edge>();
    
    Player player;
    
    // initialize sample data
    void init() {
        m1 = new Maze();
        this.arr1.add(n1);
        this.arr1.add(n2);
        this.arr1.add(n3);
        this.arr1.add(n4);
        
        this.arr2.add(be);
        this.arr2.add(de);
        this.arr2.add(ef);
        this.arr2.add(eh);
        
        this.arr3.add(1);
        this.arr3.add(2);
        this.arr3.add(3);
        this.arr3.add(4);
        
        player = new Player(0, 0, 0);
        n5 = new Node(0, 0, 0, false, false, false, false);
    }
    
    /**FINISH/FIX**/
    // test initRep that produces the hashmap
    void testInitRep(Tester t) {
        this.init();
        this.m1.initRep();
        t.checkExpect(this.m1.representatives.isEmpty(), false);
        t.checkExpect(this.m1.representatives.size(), 
                Maze.MAZE_HEIGHT * Maze.MAZE_WIDTH);
        t.checkExpect(this.m1.representatives.get(m1.maze.get(0).get(0)),
                m1.maze.get(0).get(0)); 
        t.checkExpect(this.m1.representatives.get(
                m1.maze.get(Maze.MAZE_HEIGHT - 1).get(
                        Maze.MAZE_WIDTH - 1)),
                m1.maze.get(Maze.MAZE_HEIGHT - 1).get(
                        Maze.MAZE_WIDTH - 1)); 
        
    }
    
    // test initMaze that produces the arrayList<arrayList<Node>>
    void testInitMaze(Tester t) {
        this.init();
        this.m1.initMaze();
        t.checkExpect(this.m1.maze.get(0).get(0),
                new Node(0, 0, 0, true, true, true, true));
        t.checkExpect(this.m1.maze.get(1).get(1),
                new Node(1, 1, m1.col + 1, 
                        true, true, true, true));
    }
    
    // test initMazeList that produces the arrayList<Node>
    void testInitMazeList(Tester t) {
        this.init();
        this.m1.initMazeList();
        t.checkExpect(this.m1.mazeList.get(0).row, 0);
        t.checkExpect(this.m1.mazeList.get(0).col, 0);
        t.checkExpect(this.m1.mazeList.get(0).nCell, 0);
        t.checkExpect(this.m1.mazeList.get(2).row, 0);
        t.checkExpect(this.m1.mazeList.get(2).col, 2);
        t.checkExpect(this.m1.mazeList.get(2).nCell, 2);
    }
    
    // tests initWorkList that produces the arrayList<Edge>
    void testInitWorkList(Tester t) {
        this.init();
        this.m1.initWorkList();
        // test if edges have random weights
        t.checkRange(this.m1.worklist.get(10).weight, 0, this.m1.nCells - 1);
        t.checkRange(this.m1.worklist.get(20).weight, 0, this.m1.nCells - 1);
        // test if worklist is sorted
        t.checkExpect(this.m1.worklist.get(0).weight < 
                this.m1.worklist.get(10).weight, true);
        t.checkExpect(this.m1.worklist.get(0).weight < 
                this.m1.worklist.get(100).weight, true);
            
        // test edge comparator
        t.checkExpect(this.ef.compareTo(this.eh), -5);
        t.checkExpect(this.ef.compareTo(this.ef), 0);
        t.checkExpect(this.eh.compareTo(this.ef), 5);
            
    }
    
    /**WRITE TESTS**/
    // tests for kruskalAlgo that produces the spanning tree
    void testKruskalAlgo(Tester t) {
        this.init();
        this.m1.initMaze();
        this.m1.initRep();
        this.m1.initWorkList();
        this.m1.initMazeList();
        this.m1.edgesInTree = new ArrayList<Edge>();
        t.checkExpect(this.m1.edgesInTree.size(), 0);
        t.checkExpect(this.m1.worklist.size(),
                2 * (Maze.MAZE_HEIGHT) * (Maze.MAZE_WIDTH) -
                Maze.MAZE_HEIGHT - Maze.MAZE_WIDTH);
        
        this.m1.kruskalAlgo();
        t.checkExpect(this.m1.edgesInTree.size(), 
                Maze.MAZE_HEIGHT * Maze.MAZE_WIDTH - 1);
        t.checkRange(this.m1.worklist.size(), 0, 
                2 * (Maze.MAZE_HEIGHT) * (Maze.MAZE_WIDTH) -
                Maze.MAZE_HEIGHT - Maze.MAZE_WIDTH - this.m1.nCells  + 1);
    }
    
    // test union/find methods using declared elements
    void testHM(Tester t) {
        this.init();
        
        for (Node i : arr1) {
            this.hm1.put(i, i);
        }
        
        t.checkExpect(hm1.size(), 4);
        
        // initially all ints are linked to themselves
        t.checkExpect(this.hm1.get(n1), n1);
        t.checkExpect(this.m1.find(this.hm1, n1), n1);
        t.checkExpect(this.m1.find(this.hm1, n4), n4);
        
        // link two representatives
        this.m1.union(this.hm1, n4, n2);
        this.m1.union(this.hm1, n1, n4);
        
        // after linking
        t.checkExpect(this.hm1.get(n4), n1);     
        t.checkExpect(this.m1.find(this.hm1, n4), n1);
        t.checkExpect(this.hm1.get(n2), n4);
        t.checkExpect(this.m1.find(this.hm1, n2), n1);        
    }
    
    /**WRITE TESTS**/
    // tests for makePath to determine correct walls of each node
    void testMakePath(Tester t) {
        this.init();
        this.m1.makePath();
        
        for (Edge e : this.edgesInTree) {
            if (e.b.col - e.a.col == 1) {
                t.checkExpect(e.b.left, false);
                t.checkExpect(e.a.right, false);
            }
            if (e.b.row - e.a.row == 1) {
                t.checkExpect(e.b.down, false);
                t.checkExpect(e.a.up, false);
            }

        }
        // for the wrong case, the walls are built
        // even though there should be none(since they're connected)
        for (Edge e : this.arr2) {
            if (e.b.col - e.a.col == 1) {
                t.checkExpect(e.b.left, true);
                t.checkExpect(e.a.right, true);
            }
            if (e.b.row - e.a.row == 1) {
                t.checkExpect(e.b.down, true);
                t.checkExpect(e.a.up, true);
            }

        }
        
        t.checkExpect(this.m1.edgesInTree.size(), 
                Maze.MAZE_HEIGHT * Maze.MAZE_WIDTH - 1);
    }
    
    // tests for dfs
    void testDfs(Tester t) {
        this.init();
        t.checkExpect(m1.dfs(m1.maze.get(0).get(0), 
                m1.maze.get(Maze.MAZE_HEIGHT - 1).get(
                        Maze.MAZE_WIDTH - 1)), true);    
    }
    
    // tests for dfs
    void testBfs(Tester t) {
        this.init();
        t.checkExpect(m1.bfs(m1.maze.get(0).get(0), 
                m1.maze.get(Maze.MAZE_HEIGHT - 1).get(Maze.MAZE_WIDTH - 1)),
                true);    
    }
    
    // test for constructFinalPath
    void constructFinalPath(Tester t) {
        this.init();
        int shortestPossible = (int) (Math.pow((Math.pow(Maze.MAZE_HEIGHT, 2) +
                Math.pow(Maze.MAZE_WIDTH, 2)), .5));
        int longestPossible = Maze.MAZE_HEIGHT + Maze.MAZE_WIDTH;
        t.checkRange(m1.constructFinalPath().size(),
                shortestPossible, longestPossible);
        t.checkExpect(m1.maze.get(0).get(0).partOfFinalPath, true);
        t.checkExpect(m1.maze.get(Maze.MAZE_HEIGHT - 1)
                .get(Maze.MAZE_HEIGHT - 1).partOfFinalPath, true);
    }
    
    // test for constructShortestPath
    void constructShortestPath(Tester t) {
        this.init();
        int shortestPossible = (int) (Math.pow((Math.pow(Maze.MAZE_HEIGHT, 2) +
                Math.pow(Maze.MAZE_WIDTH, 2)), .5));
        int longestPossible = Maze.MAZE_HEIGHT + Maze.MAZE_WIDTH;
        t.checkRange(m1.constructShortestPath().size(),
                shortestPossible, longestPossible);
        t.checkExpect(m1.constructShortestPath().contains(
                m1.maze.get(Maze.MAZE_HEIGHT - 1).get(Maze.MAZE_WIDTH - 1)),
                true);
        t.checkExpect(m1.constructShortestPath().contains(
                m1.maze.get(0).get(0)), true);
    }
    
    // test for visitCol
    void testVisitCol(Tester t) {
        this.init();
        t.checkExpect(m1.maze.get(0).get(0).visitCol(0), Color.lightGray);
        t.checkExpect(m1.maze.get(0).get(0).visitCol(-1), Color.darkGray);
        t.checkExpect(new Node(0, 0, 0, true, true, true, true).visitCol(0),
                Color.darkGray);
        t.checkExpect(new Node(0, 0, 0, true, true, true, true).visitCol(-1),
                Color.darkGray);
    }
    
    // test for movePlayer
    void testMovePlayer(Tester t) {
        this.init();
        t.checkExpect(player.col, 0);
        t.checkExpect(player.row, 0);
        t.checkExpect(n1.travelled, false);
        player.movePlayer(n1, "left");
        t.checkExpect(player.col, 0);
        t.checkExpect(player.row, 0);
        t.checkExpect(n1.travelled, false);
        player.movePlayer(n1, "right");
        t.checkExpect(player.col, 0);
        t.checkExpect(player.row, 0);
        t.checkExpect(n1.travelled, false);
        player.movePlayer(n1, "up");
        t.checkExpect(player.col, 0);
        t.checkExpect(player.row, 0);
        t.checkExpect(n1.travelled, false);
        player.movePlayer(n1, "down");
        t.checkExpect(player.col, 0);
        t.checkExpect(m1.p1.row, 0);
        t.checkExpect(n1.travelled, false);
        player.movePlayer(n5, "left");
        t.checkExpect(player.col, -1);
        t.checkExpect(player.row, 0);
        t.checkExpect(n5.travelled, true);
        this.init();
        player.movePlayer(n5, "right");
        t.checkExpect(player.col, 1);
        t.checkExpect(player.row, 0);
        t.checkExpect(n5.travelled, true);
        this.init();
        player.movePlayer(n5, "up");
        t.checkExpect(player.col, 0);
        t.checkExpect(player.row, -1);
        t.checkExpect(n5.travelled, true);
        this.init();
        player.movePlayer(n5, "down");
        t.checkExpect(player.col, 0);
        t.checkExpect(player.row, 1);
        t.checkExpect(n5.travelled, true);
        
    }
    // test for onTick
    void testOnTick(Tester t) {
        this.init();
        
        t.checkExpect(m1.time, 0);
        
        
        t.checkExpect(m1.time, 0);
    }
}