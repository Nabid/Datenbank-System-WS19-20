import java.util.*;


  /**
   * Compile the code with:
   *
   * javac TwoPL.java
   *
   * To execute the program you then have to use:
   *
   * java TwoPL
   *
   * You have to implement the immediateRestart, wait_die, and wound_wait methods, 
   * which should return a 2PL schedule if using the given deadlock prevention strategy.
   * 
   * You can of course also make changes to all other given classes if required.
   * 
   * This template specifies a class representing histories (and schedules) 
   * and multiple classes representing operations of histories.
   * 
   * Use the Wait class to indicate a waiting transcation.
   */
public class TwoPL {

  public static void main(String[] args) {
    checkS1();
  }

  private static void checkS1() {
    History s1 = new History();
    s1.addOperation(new Write(1, "x"));
    s1.addOperation(new Read(2, "x"));
    s1.addOperation(new Write(3, "y"));
    s1.addOperation(new Read(1, "y"));
    s1.addOperation(new Read(3, "z"));
    s1.addOperation(new Write(1, "x"));
    s1.addOperation(new Commit(1));
    s1.addOperation(new Write(2, "y"));
    s1.addOperation(new Commit(2));
    s1.addOperation(new Write(3, "y"));
    s1.addOperation(new Commit(3));

    History sres1 = new History(); sres1.addOperation(s1.getOperations());
    History sres2 = new History(); sres2.addOperation(s1.getOperations());
    History sres3 = new History(); sres3.addOperation(s1.getOperations());

    System.out.println("==== S1 ====");
    System.out.println("s1 = " + s1.toString());
    System.out.println("Immediate restart = " + immediateRestart(sres1).toString());
    // System.out.println("==== S1 ====");
    // System.out.println("s1 = " + sres1.toString());
    System.out.println("Wait die = " +  wait_die(sres2).toString());
    // System.out.println("==== S1 ====");
    // System.out.println("s1 = " + sres2.toString());
    System.out.println("Wound wait = " +  wound_wait(sres3).toString());
    // System.out.println("==== S1 ====");
    // System.out.println("s1 = " + sres3.toString());
  }


  public static History immediateRestart(History s){
    for (int i=0; i<s.getOperations().size(); i++) {
      Operation op = s.getOperations().get(i);
      Operation newScheduledOperation = null;

      String type = op.getOperationType();

      // check if transaction in restart
      if(s.isTransactionInRestart(op)) {
        s.addRestart(op);
        continue;
      }

      // check if transaction in wait
      if(s.isTransactionInWait(op)) {
        s.addWait(op);
        continue;
      }

      // COMMIT OPERATION
      if(type.equals("c")) {
        // add unlock operation for current transaction and remove it from lock list
        s.addUnlocksToSchedule(op);
        // for each unlock add operations in current position from wait list
        s.addWaitsToCurrentPosition(i);
        s.addRestartsToCurrentPosition(i);
        // add commit to schedule
        s.addSchedule(new Commit(op.getTransaction()));
      } else {
        // READ OR WRITE OPERATION
        boolean islocked = s.isPageLockedByOtherTransaction(op);
        if(!islocked) {
          if(type.equals("w")) newScheduledOperation = new WriteLock(op.getTransaction(), op.getPage());
          else if(type.equals("r")) newScheduledOperation = new ReadLock(op.getTransaction(), op.getPage());
          if(!s.isPageLockedBySameTransaction(op)) {
            s.addLock(newScheduledOperation);
            s.addSchedule(newScheduledOperation);
          }
          s.addSchedule(op);
        } else {
          s.addSchedule(new RequestedLock(op.getTransaction(), op.getPage(), op.getOperationType()));
          s.addRestart(op);
        }
      }
    }

    History result = new History();
    result.addOperation(s.getSchedule());
    return result;
  }

  public static History wait_die(History s){
    for (int i=0; i<s.getOperations().size(); i++) {
      Operation op = s.getOperations().get(i);
      Operation newScheduledOperation = null;

      String type = op.getOperationType();

      // check if transaction in restart
      if(s.isTransactionInRestart(op)) {
        s.addRestart(op);
        continue;
      }

      // check if transaction in wait
      if(s.isTransactionInWait(op)) {
        s.addWait(op);
        continue;
      }

      // COMMIT OPERATION
      if(type.equals("c")) {
        // add unlock operation for current transaction and remove it from lock list
        s.addUnlocksToSchedule(op);
        // for each unlock add operations in current position from wait list
        s.addWaitsToCurrentPosition(i);
        s.addRestartsToCurrentPosition(i);
        // add commit to schedule
        s.addSchedule(new Commit(op.getTransaction()));
      } else {
        // READ OR WRITE OPERATION
        boolean islocked = s.isPageLockedByOtherTransaction(op);
        if(!islocked) {
          if(type.equals("w")) newScheduledOperation = new WriteLock(op.getTransaction(), op.getPage());
          else if(type.equals("r")) newScheduledOperation = new ReadLock(op.getTransaction(), op.getPage());
          if(!s.isPageLockedBySameTransaction(op)) {
            s.addLock(newScheduledOperation);
            s.addSchedule(newScheduledOperation);
          }
          s.addSchedule(op);
        } else {
          boolean islockedByOlderOp = s.isPageLockedByOtherOlderTransaction(op);
          if(islockedByOlderOp) {
            // current op is young -> restart
            s.addSchedule(new RequestedLock(op.getTransaction(), op.getPage(), op.getOperationType()));
            // add also in restart list
            s.addRestart(op);
            // send current operation to the end of input operation
            //s.addOperation(op);
          } else {
            s.addSchedule(new RequestedLock(op.getTransaction(), op.getPage(), op.getOperationType()));
            // current op is old -> wait
            s.addWait(op);
          }
        }
      }
    }

    History result = new History();
    result.addOperation(s.getSchedule());
    return result;
  }

  public static History wound_wait(History s){
    for (int i=0; i<s.getOperations().size(); i++) {
      Operation op = s.getOperations().get(i);
      Operation newScheduledOperation = null;

      String type = op.getOperationType();

      // check if transaction in restart
      if(s.isTransactionInRestart(op)) {
        s.addRestart(op);
        continue;
      }

      // check if transaction in wait
      if(s.isTransactionInWait(op)) {
        s.addWait(op);
        continue;
      }

      // COMMIT OPERATION
      if(type.equals("c")) {
        // add unlock operation for current transaction and remove it from lock list
        s.addUnlocksToSchedule(op);
        // for each unlock add operations in current position from wait list
        s.addWaitsToCurrentPosition(i);
        s.addRestartsToCurrentPosition(i);
        // add commit to schedule
        s.addSchedule(new Commit(op.getTransaction()));
      } else {
        // READ OR WRITE OPERATION
        boolean islocked = s.isPageLockedByOtherTransaction(op);
        if(!islocked) {
          if(type.equals("w")) newScheduledOperation = new WriteLock(op.getTransaction(), op.getPage());
          else if(type.equals("r")) newScheduledOperation = new ReadLock(op.getTransaction(), op.getPage());
          if(!s.isPageLockedBySameTransaction(op)) {
            s.addLock(newScheduledOperation);
            s.addSchedule(newScheduledOperation);
          }
          s.addSchedule(op);
        } else {
          boolean islockedByOlderOp = s.isPageLockedByOtherOlderTransaction(op);
          if(!islockedByOlderOp) {
            // current op is young -> restart
            s.addSchedule(new RequestedLock(op.getTransaction(), op.getPage(), op.getOperationType()));
            // add also in restart list
            s.addRestart(op);
            // send current operation to the end of input operation
            //s.addOperation(op);
          } else {
            s.addSchedule(new RequestedLock(op.getTransaction(), op.getPage(), op.getOperationType()));
            // current op is old -> wait
            s.addWait(op);
          }
        }
      }
    }

    History result = new History();
    result.addOperation(s.getSchedule());
    return result;
  }

}





/**
 * Represents a history (and schedule, as schedules are prefixes of histories) as introduced in the lecture.
 */
class History {
  private ArrayList<Operation> operations;
  private ArrayList<Operation> locks;
  private ArrayList<Operation> waits;
  private ArrayList<Operation> restarts;
  private ArrayList<Operation> schedule;

  /**
   * Creates a new empty history
   */
  public History(){
    operations = new ArrayList<>();
    locks = new ArrayList<>();
    waits = new ArrayList<>();
    restarts = new ArrayList<>();
    schedule = new ArrayList<>();
  }

  public History(History h){
    operations = (ArrayList<Operation>) h.getOperations();
    locks = new ArrayList<>();
    waits = new ArrayList<>();
    restarts = new ArrayList<>();
    schedule = new ArrayList<>();
  }

  public static History newInstance(History h) {
    return new History(h);
  }

  /**
   *
   * @return the list of all operations in the history
   */
  public List<Operation> getOperations() {
    return operations;
  }

  /**
   * Adds a new operation to the history
   * @param op the operation to add
   */
  public void addOperation(List<Operation> op){
    this.operations.addAll(op);
  }

  public void addOperation(Operation op){
    this.operations.add(op);
  }

  public void addSchedule(Operation op){
    if(op == null) return;
    this.schedule.add(op);
  }

  public List<Operation> getSchedule(){
    return this.schedule;
  }

  public void addLock(Operation op){
    // add a lock only if it is not already locked by same transaction
    boolean exists = false;
    if(op == null) return;
    for (Operation lock : locks) {
      if(lock == null) continue;
      if(lock.getPage() == op.getPage() 
          && lock.getTransaction() == op.getTransaction()) {
            exists = true;
          }
    }
    if(!exists) this.locks.add(op);
  }

  public void addWait(Operation op){
    this.waits.add(op);
  }
  
  public void addRestart(Operation op){
    this.restarts.add(op);
  }


  @Override
  public String toString() {
    return operations.toString();
  }

  public boolean isPageLockedBySameTransaction(Operation o) {
    for (Operation tmpOperation : locks) {
      if(tmpOperation == null) continue;
      if(tmpOperation.getPage() == o.getPage() 
          && tmpOperation.getTransaction() == o.getTransaction()) { 
        return true;
      }
    }
    return false;
  }

  public boolean isPageLockedByOtherTransaction(Operation o) {
    for (Operation tmpOperation : locks) {
      if(tmpOperation == null) continue;
      if(tmpOperation.getPage() == o.getPage() 
          && tmpOperation.getTransaction() != o.getTransaction()) { 
        return true;
      }
    }
    return false;
  }

  public boolean isPageLockedByOtherOlderTransaction(Operation o) {
    for (Operation lock : locks) {
      if(lock == null) continue;
      if(lock.getPage() == o.getPage()
          && lock.getTransaction() < o.getTransaction()) { 
        return true;
      }
    }
    return false;
  }

  public void addUnlocksToSchedule(Operation o) {
    ArrayList<Operation> tmpList = (ArrayList<Operation>) locks.clone();
    for (Operation lock : locks) {
      if(lock == null) continue;
      if(lock.getTransaction() == o.getTransaction()) {
        String str = lock.getOperationType();
        Operation operation = null;
        if(str.equals("wl")) {
          operation = new WriteUnlock(lock.getTransaction(), lock.getPage());
        } else if(str.equals("rl")) {
          operation = new ReadUnlock(lock.getTransaction(), lock.getPage());
        }
        this.operations.add(operation);
        tmpList.remove(lock);
      }
    }
    this.locks = tmpList;
  }

  public void addWaitsToCurrentPosition(int index) {
    this.operations.addAll(index, this.waits);
    this.waits.clear();
  }

  public void addRestartsToCurrentPosition(int index) {
    this.operations.addAll(index, this.restarts);
    clearRestarts();
  }

  public void clearRestarts() {
    this.restarts.clear();
  }

  public ArrayList<Operation> getRestarts() {
    return this.restarts;
  }

  public boolean isTransactionInRestart(Operation op) {
    boolean exists = false;
    for (Operation restart : restarts) {
      if(restart.getTransaction() == op.getTransaction()) {
        exists = true;
        break;
      }
    }
    return exists;
  }

  public boolean isTransactionInWait(Operation op) {
    boolean exists = false;
    for (Operation wait : waits) {
      if(!op.getOperationType().equals("c") && wait.getTransaction() == op.getTransaction() && wait.getPage() == op.getPage()) {
        exists = true;
        break;
      } else if(op.getOperationType().equals("c") && wait.getTransaction() == op.getTransaction()) {
        exists = true;
        break;
      }
    }
    return exists;
  }
  
}


/**
 * Represents a single operation of a history.
 * The operation types are structured as follows:
 *            -------------Operation----------------------------- Wait
 *            |                                  |
 *  --ScheduleOperation--            --SynchronizationOperation----
 *  |                   |            |                             |
 * IOOperation     Commit/Abort  LockOperation              UnlockOperation
 *      |                             |                             |
 *  Read/Write                  ReadLock/WriteLock       ReadUnlock/WriteUnlock
 */
abstract class Operation {
  protected int transaction;
  protected String page;

  /**
   * Initializes a new operation of a given transaction and page
   * @param transaction The transaction number
   * @param page  The page this operation is executed on ("" if not applicable)
   */
  public Operation(int transaction, String page) {
    this.transaction = transaction;
    this.page = page;
  }

  /**
   * 
   * @return The transaction ID
   */
  public int getTransaction() {
    return transaction;
  }

  /**
   * 
   * @return The page of this operation
   */
  public String getPage() {
    return page;
  }

  public String getOperationType() {
    String [] type = this.toString().split("_");
    return type[0];
  }
}

/**
 * Represents all operations that can be contained in a schedule 
 * (e.g.: read, write, commit, abort)
 */
abstract class ScheduleOperation extends Operation {
  public ScheduleOperation(int transaction, String page) {
    super(transaction, page);
  }
}

/**
 * Represents the read and write operations
 */
abstract class IOOperation extends ScheduleOperation{
  public IOOperation(int transaction, String page) {
    super(transaction, page);
  }

  /**
   * Returns the LockOperation required for this IOOperation.
   * Read operations return ReadLock and write operations return WriteLock
   */
  public abstract LockOperation getLockOperation();
}

/**
 * Represents all operations used for synchronization
 * (e.g.: lock and unlock operations)
 */
abstract class SynchronizationOperation extends Operation{
  public SynchronizationOperation(int transaction, String page) {
    super(transaction, page);
  }
}

/**
 * Represents all lock operations (e.g.: read and write locks)
 */
abstract class LockOperation extends SynchronizationOperation{
  public LockOperation(int transaction, String page) {
    super(transaction, page);
  }

  /**
   * Checks if the given lock is compatible with the other lock, according to the compatibility matrix given in the lecture.
   * @param other The other LockOperation to check against
   * @return True if compatible, False if not
   */
  public boolean isCompatible(LockOperation other){
    if(page != other.page) return true;
    return false;
  }

  /**
   * Returns the UnlockOperation required to release this lock
   * @return Fitting UnlockOperation
   */
  abstract public UnlockOperation getUnlockOperation();
}

/**
 * Represents all unlock operations (e.g.: read and write locks)
 */
abstract class UnlockOperation extends SynchronizationOperation{
  public UnlockOperation(int transaction, String page) {
    super(transaction, page);
  }
}

/**
 * The read operation, representing a read of a page in a history.
 */
class Read extends IOOperation {

  public Read(int transaction, String page) {
    super(transaction, page);
  }

  @Override
  public String toString() {
    return "r_"+transaction+"("+page+")";
  }
  @Override
  public LockOperation getLockOperation(){
    return new ReadLock(transaction,page);
  }
}

/**
 * The write operation, representing a write of a page in a history.
 */
class Write extends IOOperation {

  public Write(int transaction, String page) {
    super(transaction, page);
  }

  @Override
  public String toString() {
    return "w_"+transaction+"("+page+")";
  }
  @Override
  public LockOperation getLockOperation(){
    return new WriteLock(transaction,page);
  }
}

/**
 * Represents a waiting transaction
 */
class Wait extends Operation{

  public Wait(int transaction) {
    super(transaction, "");
  }

  @Override
  public String toString() {
    return "wait_"+transaction;
  }
}

/**
 * Represents a restart transaction
 */
class Restart extends Operation{

  public Restart(int transaction) {
    super(transaction, "");
  }

  @Override
  public String toString() {
    return "restart_"+transaction;
  }
}

/**
 * Represents a commit operation of a transaction
 */
class Commit extends ScheduleOperation{

  public Commit(int transaction) {
    super(transaction, "");
  }

  @Override
  public String toString() {
    return "c_"+transaction;
  }
}

/**
 * Represents an abort operation of a transaction
 */
class Abort extends ScheduleOperation{

  public Abort(int transaction) {
    super(transaction, "");
  }

  @Override
  public String toString() {
    return "a_"+transaction;
  }
}

/**
 * The releasing unlock operation for a fitting ReadLock operation.
 */
class ReadUnlock extends UnlockOperation {

  public ReadUnlock(int transaction, String page) {
    super(transaction, page);
  }

  @Override
  public String toString() {
    return "ru_"+transaction+"("+page+")";
  }

}

/**
 * The releasing unlock operation for a fitting WriteLock operation.
 */
class WriteUnlock extends UnlockOperation {

  public WriteUnlock(int transaction, String page) {
    super(transaction, page);
  }

  @Override
  public String toString() {
    return "wu_"+transaction+"("+page+")";
  }
}

/**
 * Represents acquiring a read lock.
 */
class ReadLock extends LockOperation {

  public ReadLock(int transaction, String page) {
    super(transaction, page);
  }

  @Override
  public String toString() {
    return "rl_"+transaction+"("+page+")";
  }

  @Override
  public boolean isCompatible(LockOperation other){
    if(super.isCompatible(other)) return true;
    return other.getClass().isInstance(ReadLock.class);  
  }

  @Override
  public UnlockOperation getUnlockOperation() {
    return new ReadUnlock(transaction,page);
  }

}

/**
 * Represents acquiring a read lock.
 */
class WriteLock extends LockOperation {

  public WriteLock(int transaction, String page) {
    super(transaction, page);
  }

  @Override
  public String toString() {
    return "wl_"+transaction+"("+page+")";
  }

  @Override
  public boolean isCompatible(LockOperation other){
    if(super.isCompatible(other)) return true;
    return other.page == page && other.transaction == transaction;
  }

  @Override
  public UnlockOperation getUnlockOperation() {
    return new WriteUnlock(transaction,page);
  }
}

class RequestedLock extends LockOperation {
  String type = "";

  public RequestedLock(int transaction, String page, String type) {
    super(transaction, page);
    this.type = type;
  }

  @Override
  public String toString() {
    return this.type + "l_"+transaction+"("+page+")*";
  }

  @Override
  public boolean isCompatible(LockOperation other){
    if(super.isCompatible(other)) return true;
    return other.page == page && other.transaction == transaction;
  }

  @Override
  public UnlockOperation getUnlockOperation() {
    return new WriteUnlock(transaction,page);
  }
}
