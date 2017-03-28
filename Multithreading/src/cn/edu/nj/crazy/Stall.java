package cn.edu.nj.crazy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by apple on 17/3/23.
 */
public class Stall {
  //0 means no space;1 means a free space
  private int state;
  private Lock lock = new ReentrantLock();
  private Condition full = lock.newCondition();//for the situation no car to park out
  private Condition free = lock.newCondition();//for the situation no space to park in
  private boolean[] stallParkSpace;

  public Stall(int stallNumber) {// init status
    this.state = stallNumber;
    stallParkSpace = new boolean[stallNumber];
    for (int i = 0; i < stallNumber; i++) {
      stallParkSpace[i] = true;
    }
  }

  /**
   * Parking
   */
  public void parking() {
    try {
      lock.lock();
      //using while is in order to prevent the early notification or accident
      while (state == 0) {

        free.await();

      }
      // has empty parking space
      List freeSpaceList = getfreeSpaceList();
      Random random = new Random();
      int tempIndex = random.nextInt(freeSpaceList.size());
      int index = (int) freeSpaceList.get(tempIndex);
      System.out.println("Parking:" + Thread.currentThread().getName() + " ,index:" + index);
      setFreeSpace(false, index);
      state -= 1;
      System.out.println("Parking full,State:" + state);
      full.signalAll();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      lock.unlock();
    }
  }

  /**
   * ParkingOut
   */
  public void parkingOut() {
    lock.lock();
    try {
      //full empty
      while (state == stallParkSpace.length) {
        System.out.println("parkingOut:");
        full.await();
      }
      List parkedSpaceList = getParkedSpace();//
      Random random = new Random();
      int tempIndex = random.nextInt(parkedSpaceList.size());
      int index = (int) parkedSpaceList.get(tempIndex);
      System.out.println("Parking:" + Thread.currentThread().getName() + " ,index:" + index);
      setFreeSpace(true, index);
      state += 1;
      System.out.println("ParkingOut will be notifyAll.state:" + state);
      free.signalAll();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      lock.unlock();
    }
  }


  private void setFreeSpace(boolean flag, int index) {
    if (index >= stallParkSpace.length) {
      System.out.println("Error. index out of bound");
      return;
    }
    stallParkSpace[index] = flag;
  }


  private List getfreeSpaceList() {
    List list = new ArrayList();
    for (int i = 0; i < stallParkSpace.length; i++) {
      if (stallParkSpace[i]) {
        list.add(i);//the empty park space
      }
    }
    return list;
  }

  private List getParkedSpace() {
    List list = new ArrayList();
    for (int i = 0; i < stallParkSpace.length; i++) {
      if (!stallParkSpace[i]) {
        list.add(i);
      }
    }
    return list;
  }


  public class ParkingOut implements Runnable {
    private Stall stall;

    public ParkingOut(Stall stall) {
      this.stall = stall;
    }

    @Override
    public void run() {
      while (true) {
        stall.parkingOut();
      }
    }

  }


  public class ParkingIn implements Runnable {
    private Stall stall;

    public ParkingIn(Stall stall) {
      this.stall = stall;
    }

    @Override
    public void run() {
      while (true) {
        stall.parking();
      }
    }
  }
}



