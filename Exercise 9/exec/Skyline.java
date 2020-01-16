
import java.util.*;

public class Skyline {

    /**
     * Compile the code with:
     *
     *     javac Skyline.java
     *
     * To execute the program you then have to use:
     *
     *     java Skyline
     *
     * For the exercise write the code of the method calculateSkyline.
     * You should use the class FakeRTree, which simulates some methods of a real R tree.
     *
     * The template will check for correct results and will inform you upon execution.
     *
     * To help you the template contains the classes Point(x,y) and Rectangle(x,y,SizeX,SizeY),
     * which represent Points (with coordinates x,y) and Rectangles (with coordinates x,y,x+SizeX,y+SizeY)
     *
     * The FakeRTree has the methods:
     *      queryNN(Point p), which returns the nearest Point p' to Point p (or null if the tree is empty)
     *      queryNN(Point p, Rectangle R), which returns the nearest Point p' to Point p, which is contained in Rectangle R
     *                                          (or null if there is none)
     *      queryOverlap(Rectangle R), which returns all Points in the tree that are contained in R.
     * AUTHOR: PRIOM BISWAS, MD NABID IMTEAJ
     */
    public static void main (String[] args) {

        if(test1() && test2()){
            System.out.println("=======================================");
            System.out.println("Result of solution is correct.");
            System.out.println("=======================================");
        }else {
            System.out.println("=======================================");
            System.out.println("Result of solution is not correct.");
            System.out.println("=======================================");
        }

    }

    private static boolean test1(){
        System.out.println("=======================================");
        System.out.println("Starting Test 1");
        System.out.println("=======================================");
        FakeRTree tree = new FakeRTree();
        tree.add(10,20);
        tree.add(12,10);
        tree.add(8,11);
        tree.add(16,19);
        tree.add(6,4);
        tree.add(5,6);
        tree.add(14,12);
        tree.add(2,5);
        tree.add(3,10);
        tree.add(13,19);
        tree.add(17,5);
        tree.add(9,3);
        tree.add(20,8);
        tree.add(8,10);

        Set<Point> skyline = calculateSkyline(tree);
        System.out.println("Calculated skyline: " + skyline);
        Set<Point> q1 = new HashSet<Point>();
        q1.add(new Point(2,5));
        q1.add(new Point(9,3));
        q1.add(new Point(6,4));
        System.out.println("Expected skyline: " + q1);
        if (compareResults(skyline,q1)){

             System.out.println("Test one successful!");
             return true;
        }else{
            System.out.println("Test one failed!");
            return false;
        }
    }

    private static boolean test2(){
        System.out.println("=======================================");
        System.out.println("Starting Test 2");
        System.out.println("=======================================");
        FakeRTree tree = new FakeRTree();
        Random rand = new Random(234782343247L);
        int numPoints = 5000 ;
        int maxRange = 10000;
        for (int i = 0; i < numPoints; i++) {
            tree.add(new Point(rand.nextInt(maxRange),rand.nextInt(maxRange)));
        }
        Set<Point> skyline = calculateSkyline(tree);
        Set<Point> q2 = new HashSet<Point>();
        q2.add(new Point(404,54));
        q2.add(new Point(173,146));
        q2.add(new Point(126,292));
        q2.add(new Point(7,7709));
        q2.add(new Point(458,20));
        q2.add(new Point(5,8080));
        q2.add(new Point(1030,2));
        q2.add(new Point(12,377));
        System.out.println("Calculated skyline: " + skyline);
        System.out.println("Expected skyline: " + q2);
        if (compareResults(skyline,q2)){
            System.out.println("Test two successful!");
            return true;
        }else{
            System.out.println("Test two failed!");
            return false;
        }
    }


    private static boolean compareResults(Set<Point> r1, Set<Point> r2){
        return r1.containsAll(r2) && r1.size()==r2.size();
    }
    
    private static Set<Point> calculateSkyline(FakeRTree tree){
        //
        // Your code here!
        //
        // System.out.println("Inside calculateSkyline ------------------------ " + tree.points.size());
        // Scanner in = new Scanner(System.in);
        // System.out.println(">>> WAITING... ");
        // in.nextLine();
        Set<Point> ret = new HashSet<Point>();

        Point INF = new Point(9999, 9999);
        Point origin = new Point(0, 0);
        Point c = tree.queryNN(origin); // first point

        // return if there is no skyline points
        if(c == null) return null;
        ret.add(c);

        //System.out.println("Closest: " + c.toString());

        // exclude all (inclusive) points in this partition/w.r.t this skyline point
        Rectange r4 = new Rectange(c.x, c.y, INF.x, INF.y);
        // System.out.print("R4(before)[" + tree.points.size() + "]");
        // for(Point p: tree.points) System.out.print(p.toString() + ", ");
        // System.out.println();

        tree.points = tree.removePoints(r4);
        tree.points.remove(c);
        
        // System.out.print("R4(after)[" + tree.points.size() + "]");
        // for(Point p: tree.points) System.out.print(p.toString() + ", ");
        // System.out.println();

        // now devide into partitions
        // R3 R4
        // R1 R2
        // excluded partition
        Rectange r1 = new Rectange(origin.x, origin.y, c.x-origin.x, c.y-origin.y);

        // inclusive partion
        Rectange r2 = new Rectange(c.x, origin.y, INF.x, c.y);
        // System.out.println("R2 (before) " + tree.points.size());
        Partition p2 = new Partition(tree.queryOverlap(r2));
        if(p2.fakeRTree.points.size() > 0) ret.addAll(calculateSkyline(p2.fakeRTree));
        tree.points = tree.removePoints(r2);
        // System.out.println("R2 (after) " + tree.points.size());

        Rectange r3 = new Rectange(origin.x, c.y, c.x, INF.y);
        // System.out.println("R3 (before) " + tree.points.size());
        Partition p3 = new Partition(tree.queryOverlap(r3));
        if(p3.fakeRTree.points.size() > 0) ret.addAll(calculateSkyline(p3.fakeRTree));
        tree.points = tree.removePoints(r3);
        // System.out.println("R3 (after) " + tree.points.size());

        return ret;
    }











    public static class Partition {
        FakeRTree fakeRTree;

        public Partition(Set<Point> points) {
            this.fakeRTree = new FakeRTree();
            this.fakeRTree.points.addAll(points);
        }

        public FakeRTree getRTree() {
            return this.fakeRTree;
        }

        public void add(Point p) {
            this.fakeRTree.add(p);
        }
    }

    public static class Point {
        int x;
        int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Calculates the distance between this Point and another one
         * @param other The other Point to compare against
         * @return The distance between the Points
         */
        public double distance(Point other){
            return Math.sqrt(Math.pow((x-other.x),2)+Math.pow((y-other.y),2));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return x == point.x &&
                    y == point.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return "(" + x +"," + y +')';
        }
    }

    public static class Rectange {
        int x;
        int y;
        int sizeX;
        int sizeY;

        public Rectange(int x, int y, int sizeX, int sizeY) {
            this.x = x;
            this.y = y;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
        }

        /**
         * Checks if Point p is contained in the Rectangle (with inclusive borders)
         * @param p Point p to check against
         * @return True if p is within the Rectangle, False otherwise
         */
        public boolean contains(Point p){
            return (p.x>=x && p.x <= x+sizeX) && (p.y>=y && p.y <= y+sizeY);
        }

        /**
         * Checks if Point p is contained in the Rectangle (with exclusive borders)
         * @param p Point p to check against
         * @return True if p is within the Rectangle, False otherwise
         */
        public boolean containsInner(Point p){
            return (p.x>x && p.x < x+sizeX) && (p.y>y && p.y < y+sizeY);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Rectange rectange = (Rectange) o;
            return x == rectange.x &&
                    y == rectange.y &&
                    sizeX == rectange.sizeX &&
                    sizeY == rectange.sizeY;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, sizeX, sizeY);
        }

        @Override
        public String toString() {
            return "(" + x + "," + y +"," + sizeX +"," + sizeY +')';
        }
    }



    public static class FakeRTree {

        Set<Point> points;

        public FakeRTree() {
            points = new HashSet<Point>();
        }


        void add(int x, int y) {
            points.add(new Point(x,y));
        }
        void add(Point p) {
            points.add(p);
        }

        /**
         * Returns the closest Point to q, or null if the tree is empty.
         * @param q Query Point, to compare to
         * @return Closest Point, or null
         */
        Point queryNN(Point q) {
            Point ret = null;
            double dbest = Double.MAX_VALUE;
            for (Point p : points) {
                double d = q.distance(p);
                if (d<dbest) {
                    ret = p;
                    dbest = d;
                }
            }
            return ret;
        }

        /**
         * Returns the closest Point to q, or null if the tree is empty.
         * @param q Query Point, to compare to
         * @param rec Rectangle to restrict query (Rectangle boundaries are not inclusive).
         * @return Closest Point, or null
         */
        Point queryNN(Point q, Rectange rec) {
            Point ret = null;
            double dbest = Double.MAX_VALUE;
            for (Point p : points) {
                double d = q.distance(p);
                if (d<dbest && rec.containsInner(p)) {
                    ret = p;
                    dbest = d;
                }
            }
            return ret;
        }

        /**
         * Returns all points contained in the Rectangle
         * @param q Query Rectangle
         * @return A set of Points contained in the Rectangle.
         */
        Set<Point> queryOverlap(Rectange q) {
            HashSet<Point> ret = new HashSet<Point>();
            for (Point p: points) {
                if (q.contains(p)) ret.add(p);
            }
            return ret;
        }

        Set<Point> queryOverlapInner(Rectange q) {
            HashSet<Point> ret = new HashSet<Point>();
            for (Point p: points) {
                if (q.containsInner(p)) ret.add(p);
            }
            return ret;
        }

        Set<Point> removePoints(Rectange q) {
            // System.out.println("Rectangle: " + q.toString());
            Set<Point> overlap = queryOverlap(q);
            // System.out.print("overlap: "); for(Point p: overlap) System.out.print(p.toString() + ", "); System.out.println();
            Set<Point> differenceSet = points;
            differenceSet.removeAll(overlap);
            // System.out.print("differences: "); for(Point p: differenceSet) System.out.print(p.toString() + ", "); System.out.println();
            return differenceSet;
        }

    }
}
