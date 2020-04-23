//// Assignment 9
//// Harmon Thomas
//// tharmon
//// Song Yangsoo
//// songyang25
//
//
//import tester.*;
//import javalib.impworld.*;
//import java.awt.Color;
//import javalib.worldimages.*;
//import java.util.*;
//
//
// class DisjointSet {  
//    int set;  
//    int sizes;  
//    int size;  
//    public DisjointSet(int size) {  
//         this.set = size;  
//         for (int i = 0; i < size; i++) {  this.set = i;  }  
//         this.sizes = new int[size];  
//         for (int i = 0; i < size; i++) {  this.sizes[i] = 1; }  
//         this.size = size;  
//    }  
//    public int find(int item) {
//         int root = item;
//         // find the root
//         while (set[root] != root) {
//               root = set[root];
//         }
//         // now shorten the paths
//         int curr = item;
//         while (set[curr] != root) {
//               set[curr] = root;
//         }
//         return root;
//    }
//    public int join(int item1, int item2) {  
//         int group1 = find(item1);  
//         int group2 = find(item2);  
//         --size;  
//         if (sizes[group1] > sizes[group2]) {  
//              set[group2] = group1;  
//              sizes[group1] += sizes[group2];  
//              return group1;  
//         } else {  
//              set[group1] = group2;  
//              sizes[group2] += sizes[group1];                 
//              return group2;  
//         }  
//    }  
//}  
//
//Maze createRandomMaze(int rows, int columns) {  
//    Maze maze = new Maze(rows, columns);  
//    // create all walls  
//    List<Wall> walls = maze.getAllInnerWalls();  
//    // remove all the walls you can  
//    DisjointSet diset = new DisjointSet(rows*columns);  
//    while (diset.size() > 1) {  
//         int wallIndex = random.nextInt(walls.size());  
//         int cell1 = walls.get(wallIndex).cell1;  
//         int cell2 = walls.get(wallIndex).cell2;  
//         if (diset.find(cell1) != diset.find(cell2)) {  
//              // we can remove the wall  
//              maze.removeWall(walls.get(wallIndex));  
//              diset.join(cell1, cell2);  
//         }  
//         walls.remove(wallIndex);  
//    }  
//    return maze;  
//}  
//
//// Represents a single square of the game area
//class Cell {
//    // represents absolute height of this cell, in feet
//    double height;
//    // In logical coordinates, with the origin at the top-left corner of the screen
//    int x;
//    int y;
//    // the four adjacent cells to this one
//    Cell left;
//    Cell top;
//    Cell right;
//    Cell bottom;
//    // reports whether this cell is flooded or not
//    boolean isTraveled;
//
//    // create a new Cell 
//    Cell(double height, int x, int y, 
//            Cell left, Cell top, Cell right, Cell bottom, boolean isTraveled) {
//        this.height = height;
//        this.x = x;
//        this.y = y;
//        this.left = left;
//        this.top = top;
//        this.right = right;
//        this.bottom = bottom;
//        this.isTraveled = isTraveled;
//    }
//
//    // create a new cell without any neighbors
//    Cell(double height, int x, int y, boolean isTraveled) {
//        this.height = height;
//        this.x = x;
//        this.y = y;
//        this.isTraveled = isTraveled;
//    }
//
//    // draw the given cell onto the given scene
//    public void drawCell(Cell c, WorldScene s, int waterHeight) {
//        s.placeImageXY(
//                new RectangleImage(
//                        ForbiddenIslandWorld.CELL_SIZE,
//                        ForbiddenIslandWorld.CELL_SIZE,
//                        OutlineMode.SOLID,
//                        Color.white),
//                c.x * ForbiddenIslandWorld.CELL_SIZE + ForbiddenIslandWorld.CELL_SIZE / 2,
//                c.y * ForbiddenIslandWorld.CELL_SIZE + ForbiddenIslandWorld.CELL_SIZE / 2);
//    }
//
//
//class ForbiddenIslandWorld extends World {
//    // defines a constant for the size of the island
//    static final int ISLAND_SIZE = 64;
//    // defines a constant for the size of a cell
//    static final int CELL_SIZE = 10;
//    // defines a constant for island height
//    static final int ISLAND_HEIGHT = ISLAND_SIZE / 2;
//    // a random generator
//    Random rand = new Random();
//    // All the cells of the game, including the ocean
//    IList<Cell> board;
//    // the current height of the ocean
//
//    // an Array list of Cells
//    ArrayList<ArrayList<Cell>> cells;
//    // All cells height represented in Arraylists
//    ArrayList<ArrayList<Double>> cellHeights;
//
//    // constructor for the game
//    ForbiddenIslandWorld() {
//        this.createRegularMountain();
//    }
//
//
//    // draw this World
//    public WorldScene makeScene() {
//        // draw the cells
//        WorldScene scene = this.getEmptyScene();
//        //            System.out.println(board == null);
//        for (Cell c: this.board) {
//            //            System.out.println(c.height); 
//            c.drawCell(c, scene, waterHeight);     
//        }
//
//        return scene;
//    }
//
//}
//
//
////an interface to hold Lists of T
//interface IList<T> extends Iterable<T> {
//    // is this list of T the same as that list of T?
//    boolean sameList(IList<T> that);
//    // is this Empty list of T the same as that EmptyList?
//    boolean sameEmptyList(Mt<T> that);
//    // is this Cons list of T the same as that ConsList?
//    boolean sameConsList(Cons<T> that);
//    // is this cons?
//    boolean isCons();
//    // is this empty?
//    boolean isMT();
//    // treat this IList as Cons
//    Cons<T> asCons();
//    // returns the size of this list
//    int size();
//    // remove the given T from this list
//    IList<T> remove(T t);
//    // add the given T to the front of this list
//    IList<T> add(T t);
//    // does this list contain the given T
//    boolean has(T t);
//}
//
//
////a class to represent ConsList<T>
//class Cons<T> implements IList<T> {
//    T first;
//    IList<T> rest;
//    Iterator<T> iterator;
//
//    Cons(T first, IList<T> rest) {
//        this.first = first;
//        this.rest = rest;
//    }
//
//    // standard iterator over this
//    public Iterator<T> iterator() {
//        return new IListIterator<T>(this);
//    }
//
//    // is this list of T the same as that list?
//    public boolean sameList(IList<T> that) {
//        return that.sameConsList(this);
//    }
//
//    // is this list of T the same as that empty list?
//    public boolean sameEmptyList(Mt<T> that) {
//        return false;
//    }
//
//    // is this list of T the same as that cons list?
//    public boolean sameConsList(Cons<T> that) {
//        return this.first.equals(that.first) &&
//                this.rest.sameList(that.rest);
//    }
//
//    // Is this a cons list?
//    public boolean isCons() {
//        return true;
//    }
//
//    // is this an empty list?
//    public boolean isMT() {
//        return false;
//    }
//
//    //return the size of this IList
//    public int size() {
//        return 1 + this.rest.size();
//    }
//
//    // treat this list as cons -> return this
//    public Cons<T> asCons() {
//        return this;
//    }
//
//    //does this list contain the given T
//    public boolean has(T t) {
//        return (t.equals(this.first)) || this.rest.has(t);
//    }
//
//    // remove the given T from this list
//    public IList<T> remove(T t) {
//        if (t == this.first) {
//            return this.rest;
//        }
//        else {
//            return new Cons<T>(this.first, this.rest.remove(t));
//        }
//    }
//
//    // add the given item to the front of this list
//    public IList<T> add(T t) {
//        return new Cons<T>(t, this);
//    }
//}
//
////a class to represent MtList<T>
//class Mt<T> implements IList<T> {
//
//    // standard iterator over this
//    public Iterator<T> iterator() {
//        return new IListIterator<T>(this);
//    }
//
//    // is this list the same as that list?
//    public boolean sameList(IList<T> that) {
//        return that.sameEmptyList(this);
//    }
//
//    // is this list the same as that empty list?
//    public boolean sameEmptyList(Mt<T> that) {
//        return true;
//    }
//
//    // is this list the same as that cons list?
//    public boolean sameConsList(Cons<T> that) {
//        return false;
//    }
//
//    // Is this a cons list?
//    public boolean isCons() {
//        return false;
//    }
//
//    // is this an empty list?
//    public boolean isMT() {
//        return true;
//    }
//
//    //return the size of this IList
//    public int size() {
//        return 0;
//    }
//
//    // treat this list as cons -> return this
//    public Cons<T> asCons() {
//        throw new RuntimeException("You can't do this");
//    }
//
//    //does this list contain the given T
//    public boolean has(T t) {
//        return false;
//    }
//
//    // remove the given T from this list
//    public IList<T> remove(T t) {
//        return new Mt<T>();
//    }
//
//    // add the given item to the front of this list
//    public IList<T> add(T t) {
//        return new Cons<T>(t, this);
//    }
//}
//
//class IListIterator<T> implements Iterator<T> {
//    IList<T> loItems;
//
//
//    IListIterator(IList<T> loItems) {
//        this.loItems = loItems;
//    }
//
//    // cannot remove from the iter
//    public void remove() {
//        throw new UnsupportedOperationException("Don't do this!");
//    }
//
//
//    // does this item have a following item in the list?
//    public boolean hasNext() {
//        return this.loItems.isCons();
//    }
//
//
//    // move the iterator up one element in the list
//    public T next() {
//        Cons<T> itemsAsCons = this.loItems.asCons();
//        T nextItem = itemsAsCons.first;
//        this.loItems = itemsAsCons.rest;
//
//        return nextItem;
//    }
//
//
//}
//
//}