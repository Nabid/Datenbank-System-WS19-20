import java.util.*;
import java.util.stream.Collectors;



public class Recovery {

    /**
     * Compile the code with:
     *
     * javac Recovery.java
     *
     * To execute the program you then have to use:
     *
     * java Recovery
     * 
     * This template contains two important mock classes:
     * 
     *  - Database -
     *  A database mock class, whose "execute" method is to be used to simulate executing operations like "A+=100" on a database.
     * 
     *  - Log -
     *  A mock database log file. It can be used to read and modify the log file to perform the desired operations.
     * 
     * Your task is to implement the methods:
     *  - getLoserTransactions(Log): analyze the log to retrieve a set of all loser transactions
     *  - redo(Log, Database): Perform all redo operations (as shown in the lecture) on the mock Database
     *  - undo(Log, Database): Undo all operations of the loser transactions on the Database and write CLRs to the log.
     *
     */
    public static void main(String[] args) {

        if (!test1()) {
            System.err.println("Test 1 failed.");
        } else {
            System.out.println("Test 1 successful");
        }

    }

    public static boolean test1() {
        System.out.println("--------- Test 1 ---------");
        System.out.println("--- Input log: ---");
        Log log = new Log();
        try {
            log.addLog("#1", "T1", "BOT", "", "", "");
            log.addLog("#2", "T2", "BOT", "", "", "");
            log.addLog("#3", "T1", "A", "A-=50", "A+=50", "#1");
            log.addLog("#4", "T2", "C", "C+=100", "C-=100", "#2");
            log.addLog("#5", "T1", "B", "B+=50", "B-=50", "#3");
            log.addLog("#6", "T1", "COMMIT", "", "", "#5");
            log.addLog("#7", "T2", "A", "A-=100", "A+=100", "#4");
        } catch (Log.LSNAlreadyExistsException e) {
            e.printStackTrace();
        }
        System.out.println(log.toString());
        System.out.println("--- Tests: ---");
        // Loser
        HashSet<String> expectedLosers = new HashSet<>();
        expectedLosers.add("T2");
        Set<String> losers = getLoserTransactions(log);
        System.out.println("Loser Transactions: " + losers.toString());
        System.out.println("Expected Loser Transactions: " + expectedLosers.toString());
        if (!expectedLosers.equals(losers)) {
            return false;
        }
        // Redo
        Database db = new Database();
        redo(log, db);
        if(!db.toString().equals("A-=50\nC+=100\nB+=50\nA-=100")){
            System.err.println("Redo operations not as expected.");
            return false;
        }

        // Undo
        undo(log, db);
        if(!db.toString().equals("A-=50\nC+=100\nB+=50\nA-=100\nA+=100\nC-=100")){
            System.err.println("Undo operations not as expected.");
            return false;
        }
        if(!log.toString().equals("[#1, T1, BOT, , , ]\n[#2, T2, BOT, , , ]\n[#3, T1, A, A-=50, A+=50, #1]\n[#4, T2, C, C+=100, C-=100, #2]\n[#5, T1, B, B+=50, B-=50, #3]\n[#6, T1, COMMIT, , , #5]\n[#7, T2, A, A-=100, A+=100, #4]\n<#7', T2, A, A+=100, #7, #4>\n<#4', T2, C, C-=100, #7', #2>\n<#2', T2, BOT, , #4', 0>")){
            System.err.println("CLRs not as expected.");
            return false;
        }

        return true;
    }

    /**
     * Returns a set of all loser transactions in the given log
     * @param log The log to be analyzed
     * @return set of all loser transactions
     */
    public static Set<String> getLoserTransactions(Log log) {
        //
        // Your code here!
        //
        
        return new HashSet<>();
    }

    /**
     * Performs the redo operations as specified in the database lecture.
     * @param log The log to get the redo information from.
     * @param db The database to execute the operations on.
     */
    public static void redo(Log log, Database db) {
        //
        // Your code here!
        //
    }

    /**
     * Performs the undo operations as specified in the database lecture.
     * @param log The log to get the undo information from and write the CLRs to
     * @param db The database to execute the operations on.
     */
    public static void undo(Log log, Database db) {
        //
        // Your code here!
        //
    }

    
}

/**
 * A mock database class
 */
class Database {
    /**
     * Creates an empty mock database, with no operations written.
     */
    public Database(){
        operations = new ArrayList<>();
    }

    /**
     * Executes a given operation on the database
     * @param op The operation to execute
     */
    public void execute(String op){
        operations.add(op);
    }

    /**
     * 
     * @return A list of all operations.
     */
    public List<String> getOperations(){
        return new ArrayList<>(operations);
    }

    @Override
    public String toString() {
        return operations.stream().map(x-> x.toString()).collect(Collectors.joining( "\n" ));
    }

    private List<String> operations;
}

/**
 * A mock database Log class
 */
class Log {

    /**
     * Returns an log entry by LSN. If the entry does not exists, an exception is thrown.
     * @param lsn The LSN to search for
     * @return The log entry or an exception
     */
    public Entry getEntryByLSN(String lsn) {
        return entries.stream().filter(x -> x.getLsn() == lsn).findFirst().orElseThrow();
    }

    /**
     * Returns all entries of the specified transaction
     * @param tid The transaction ID to filter by
     * @return A list of entries
     */
    public List<Entry> getEntriesByTid(String tid){
        return entries.stream().filter(x -> x.getTid() == tid).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Returns all entries in the log
     * @return The full list of entries
     */
    public List<Entry> getEntries(){
        return new ArrayList<>(entries);
    }

    /**
     * Returns a list of all distinct transaction IDs in the log
     * @return a list of all distinct transaction IDs
     */
    public List<String> getTids(){
        return entries.stream().map(x -> x.getTid()).distinct().collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Adds a normal log entry to the log
     * @param lsn The LSN of the entry
     * @param tid The transaction ID of the entry
     * @param page The modified page of the entry
     * @param redo The redo operation of the entry
     * @param undo The undo operation of the entry
     * @param prevLsn The previous LSN of the entry
     * @throws LSNAlreadyExistsException if the LSN already exists in the log
     */
    public void addLog(String lsn, String tid, String page, String redo, String undo, String prevLsn)
            throws LSNAlreadyExistsException {
        if(entries.stream().anyMatch(x -> x.getLsn() == lsn)){
            throw new LSNAlreadyExistsException("LSN already taken: " + lsn);
        }else{
            entries.add(new Entry(lsn,tid,page,redo,undo,prevLsn,null));
        }
        
    }

    /**
     * Adds a CLR to the log
     * @param lsn The LSN of the entry
     * @param tid The transaction ID of the entry
     * @param page The modified page of the entry
     * @param redo The redo operation of the entry
     * @param prevLsn The previous LSN of the entry
     * @param undoNextLsn The next LSN to undo
     * @throws LSNAlreadyExistsException if the LSN already exists in the log
     */
    public void addCLR(String lsn, String tid, String page, String redo, String prevLsn,String undoNextLsn)
            throws LSNAlreadyExistsException {
        if(entries.stream().anyMatch(x -> x.getLsn() == lsn)){
            throw new LSNAlreadyExistsException("LSN already taken: " + lsn);
        }else{
            entries.add(new Entry(lsn,tid,page,redo,null,prevLsn,undoNextLsn));
        }
    }

    /**
     * Creates a new empty log
     */
    public Log() {
        entries = new ArrayList<>();
    }

    @Override
    public String toString() {
        return entries.stream().map(x-> x.toString()).collect(Collectors.joining( "\n" ));
    }

    private List<Entry> entries;


    public class LSNAlreadyExistsException extends Exception{
        public LSNAlreadyExistsException(String errorMessage) {
            super(errorMessage);
        }
    }

    /**
     * A single log entry
     */
    public class Entry{
        Entry(String lsn, String tid, String page, String redo, String undo, String prevLsn,
                String undoNextLsn) {
            this.lsn = lsn;
            this.tid = tid;
            this.page = page;
            this.redo = redo;
            this.undo = undo;
            this.prevLsn = prevLsn;
            this.undoNextLsn = undoNextLsn;
        }

        /**
         * 
         * @return true if the log entry is a CLR
         */
        public boolean isCLR(){
            return undoNextLsn != null;
        }

        /**
         * 
         * @return true if the log entry is a BOT operation
         */
        public boolean isBOT(){
            return page.equals("BOT");
        }

        /**
         * 
         * @return true if the log entry is a COMMIT operation
         */
        public boolean isCOMMIT(){
            return page.equals("COMMIT");
        }
        
        /**
         * 
         * @return true if the log entry is a ABORT operation
         */
        public boolean isABORT(){
            return page.equals("ABORT");
        }
           
        private String lsn;
        private String tid;
        private String page;
        private String redo;
        private String undo;
        private String prevLsn;
        private String undoNextLsn;

        public String getLsn() {
            return lsn;
        }

        public String getTid() {
            return tid;
        }

        public String getPage() {
            return page;
        }

        public String getRedo() {
            return redo;
        }

        public String getUndo() {
            return undo;
        }

        public String getPrevLsn() {
            return prevLsn;
        }

        public String getUndoNextLsn() {
            return undoNextLsn;
        }

        @Override
        public String toString() {
            if(isCLR()){
                return "<"+lsn+", "+tid+", "+page+", "+redo+", "+prevLsn+", "+undoNextLsn+">";
            }else{
                return "["+lsn+", "+tid+", "+page+", "+redo+", "+undo+", "+prevLsn+"]";
            }
        }
    }

}