import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


/**
 * Compile the code with:
 *
 *     javac JoinExercise.java
 *
 * To execute the code, you need the Postgres JDBC driver.
 * You can download it at https://jdbc.postgresql.org/download.html
 * Save the .jar file in the same directory as your Java code.
 * To execute the program you then have to use (Replacing XX.XX.XX with the actual version):
 *
 *     java -cp postgresql-XX.XX.XX.jar:. JoinExercise
 *
 * Alternatively, you can use an IDE like IntelliJ or Eclipse and put the driver into the class-path (or even download it via Maven).
 *
 *
 * For the exercise write the code of the methods nestedLoopJoin and hashJoin.
 *
 * You may have to adapt the connection code of the class DBConnector for your database.
 */
public class JoinExercise {
    public static void main(String... args) throws SQLException {
        System.out.println("Join exercise: lineitem ⋈ orders");
        System.out.println("================================");

        System.out.print("Testing Database... ");
        if(DBConnector.testConnection()) {
            System.out.println("success.");
        } else {
            System.out.println(" ERROR!");
        }

        List<LineitemTuple> lineitems = DBConnector.getLineitems();
        List<OrderTuple> orders = DBConnector.getOrders();
        List<JoinResultTuple> nestedLoopJoinResult = new ArrayList<>(Math.max(lineitems.size(), orders.size()));
        List<JoinResultTuple> hashJoinResult = new ArrayList<>(Math.max(lineitems.size(), orders.size()));

        // Postgres Join as reference
        long startTime = System.currentTimeMillis();
        List<JoinResultTuple> postgresJoinResult = DBConnector.getJoinResult();
        long endTime = System.currentTimeMillis();
        System.out.println("Processing the join in postgres took " + (endTime - startTime) + "ms.");

        // Nested Loop Join
        startTime = System.currentTimeMillis();
        nestedLoopJoin(lineitems, orders, nestedLoopJoinResult);
        endTime = System.currentTimeMillis();
        System.out.println("Nested Loop Join took " + (endTime - startTime) + "ms.");

        // Hash Join
        startTime = System.currentTimeMillis();
        hashJoin(lineitems, orders, hashJoinResult);
        endTime = System.currentTimeMillis();
        System.out.println("Hash Join took " + (endTime - startTime) + "ms.");

        // Sanity check for results
        Collections.sort(postgresJoinResult);
        Collections.sort(nestedLoopJoinResult);
        Collections.sort(hashJoinResult);

        if(postgresJoinResult.equals(nestedLoopJoinResult)) {
            System.out.println("Nested Loop Join result correct.");
        } else {
            System.out.println("Nested Loop Join result incorrect!");
        }
        if(postgresJoinResult.equals(hashJoinResult)) {
            System.out.println("Hash Join Result correct.");
        } else {
            System.out.println("Hash Join Result incorrect!");
        }
    }

    /**
     * @param lineitems     List of tuples from the lineItems table.
     * @param orders        List of tuples from the orders table.
     * @param joinResult    Result of the lineitem ⋈ orders join.
     */
    static void nestedLoopJoin(List<LineitemTuple> lineitems, List<OrderTuple> orders, List<JoinResultTuple> joinResult) {
        //////////////
        // Your code//
        //////////////

        for(LineitemTuple _lineitem : lineitems) {
            for(OrderTuple _order : orders) {
                if(_lineitem.orderkey == _order.orderkey) {
                    joinResult.add(new JoinResultTuple(_lineitem.orderkey, _lineitem.shipdate, _order.orderdate));
                }
            }
        }
    }

    /**
     * @param lineitems     List of tuples from the lineItems table.
     * @param orders        List of tuples from the orders table.
     * @param joinResult    Result of the lineitem ⋈ orders join.
     */
    static void hashJoin(List<LineitemTuple> lineitems, List<OrderTuple> orders, List<JoinResultTuple> joinResult) {
        //////////////
        // Your code//
        //////////////

        Map<Integer, Date> _lineitemHashMap = new HashMap<Integer, Date>();

        // HASH PART
        //----------------------------
        for(LineitemTuple _lineitem : lineitems) {
            // insert into hashmap
            _lineitemHashMap.put(_lineitem.orderkey, _lineitem.shipdate);
        }

        // JOIN PART
        //----------------------------
        for(OrderTuple _order : orders) {
            if(_lineitemHashMap.containsKey(_order.orderkey)) {
                joinResult.add(new JoinResultTuple(_order.orderkey, _lineitemHashMap.get(_order.orderkey), _order.orderdate));
            }
        }
    }
}

abstract class OrderkeyTuple implements Comparable<OrderkeyTuple> {
    final int orderkey;

    OrderkeyTuple(int orderkey) {
        this.orderkey = orderkey;
    }

    @Override
    public int compareTo(OrderkeyTuple o) {
        return Integer.compare(this.orderkey, o.orderkey);
    }
}

class LineitemTuple extends OrderkeyTuple {
    final Date shipdate;

    LineitemTuple(int orderkey, Date shipdate) {
        super(orderkey);
        this.shipdate = shipdate;
    }
}

class OrderTuple extends OrderkeyTuple {
    final Date orderdate;

    OrderTuple(int orderkey, Date orderdate) {
        super(orderkey);
        this.orderdate = orderdate;
    }
}

class JoinResultTuple extends OrderkeyTuple {
    final Date shipdate;
    final Date orderdate;

    JoinResultTuple(int orderkey, Date shipdate, Date orderdate) {
        super(orderkey);
        this.shipdate = shipdate;
        this.orderdate = orderdate;
    }


    @Override
    public boolean equals(Object other) {
        return other instanceof JoinResultTuple &&
                ((JoinResultTuple) other).orderkey == this.orderkey &&
                ((JoinResultTuple) other).shipdate.equals(this.shipdate) &&
                ((JoinResultTuple) other).orderdate.equals(this.orderdate);
    }

    @Override
    public int compareTo(OrderkeyTuple o)
    {
        if(!(o instanceof JoinResultTuple) || orderkey != o.orderkey) {
            return super.compareTo(o);
        }

        JoinResultTuple j = (JoinResultTuple) o;

        if(!shipdate.equals(j.shipdate)) {
            return shipdate.compareTo(j.shipdate);
        }

        if(!orderdate.equals(j.orderdate)) {
            return orderdate.compareTo(j.orderdate);
        }

        return 0;
    }


    @Override
    public int hashCode() {
        return this.orderkey ^ this.shipdate.hashCode() ^ this.orderdate.hashCode();
    }
}


class DBConnector {
    private static final String HOST = "localhost";
    private static final short PORT = 5432;
    //private static final short PORT = 49442;
    private static final String USER = "postgres";
    private static final String PASSWORD = "1234";
    private static final String DATABASE = "tpch";

    private static Connection conn = null;

    private static final int ORDERKEY_BOUND = 50_000;

    {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    static boolean testConnection() {
        try {
            getConnection();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getJDBCString() {
        return String.format("jdbc:postgresql://%s:%d/%s", HOST, PORT, DATABASE);
    }

    private static Connection getConnection() throws SQLException {
        if(DBConnector.conn == null) {
            DBConnector.conn = DriverManager.getConnection(getJDBCString(), USER, PASSWORD);
        }
        return DBConnector.conn;
    }

    static List<LineitemTuple> getLineitems() throws SQLException {
        List<LineitemTuple> result = new ArrayList<>();
        String q = "SELECT l_orderkey, l_shipdate FROM lineitem WHERE l_orderkey < ?";
        PreparedStatement s = getConnection().prepareStatement(q);
        s.setInt(1, ORDERKEY_BOUND);
        ResultSet rs = s.executeQuery();
        while(rs.next()) {
            int orderkey = rs.getInt(1);
            Date shipdate = rs.getDate(2);
            result.add(new LineitemTuple(orderkey, shipdate));
        }
        return result;
    }

    static List<OrderTuple> getOrders() throws SQLException {
        List<OrderTuple> result = new ArrayList<>();
        String q = "SELECT o_orderkey, o_orderdate FROM orders WHERE o_orderkey < ?";
        PreparedStatement s = getConnection().prepareStatement(q);
        s.setInt(1, ORDERKEY_BOUND);
        ResultSet rs = s.executeQuery();
        while(rs.next()) {
            int orderkey = rs.getInt(1);
            Date orderdate = rs.getDate(2);
            result.add(new OrderTuple(orderkey, orderdate));
        }
        return result;
    }

    static List<JoinResultTuple> getJoinResult() throws SQLException {
        List<JoinResultTuple> result = new ArrayList<>();
        String q = "SELECT l_orderkey, l_shipdate, o_orderdate FROM lineitem JOIN orders ON l_orderkey = o_orderkey WHERE o_orderkey < ? AND l_orderkey < ?";
        PreparedStatement s = getConnection().prepareStatement(q);
        s.setInt(1, ORDERKEY_BOUND);
        s.setInt(2, ORDERKEY_BOUND);
        ResultSet rs = s.executeQuery();
        while(rs.next()) {
            int orderkey = rs.getInt(1);
            Date shipdate = rs.getDate(2);
            Date orderdate = rs.getDate(3);
            result.add(new JoinResultTuple(orderkey, shipdate, orderdate));
        }
        return result;
    }
}
