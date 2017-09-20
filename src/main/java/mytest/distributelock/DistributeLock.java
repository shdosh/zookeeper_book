package mytest.distributelock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by shidongsheng.it on 2017/9/20.
 */
public class DistributeLock implements Watcher {

    //多线程计数器
    private static CountDownLatch countDownLatch = new CountDownLatch(1);
    //链接主线程阻塞控制
    private static CountDownLatch connectCountDown = new CountDownLatch(1);
    //原生zookeeper客户端
    private static ZooKeeper zooKeeper = null;
    //节点状态对象
    private static Stat stat = new Stat();

    //分布式锁根节点
    private static String path = "/zk-lock-root";

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {

        System.out.println("主线程：" + Thread.currentThread().getName());

        //异步建立链接
        zooKeeper = new ZooKeeper("domain1.book.zookeeper:2181",3000,new DistributeLock());

        //建立链接完成
        connectCountDown.await();

        //创建锁的根节点
        zooKeeper.create(path,"LockTest".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        final BigInteger threadCount = new BigInteger("1");
        //多线程并发
        for(int i = 1; i <= 10; i++){

             Thread thread = new Thread(new Runnable() {
                 @Override
                 public void run() {
                     System.out.println("并发线程" + threadCount.toString() + "："+Thread.currentThread().getName() + "初始化");

                     //线程阻塞等待最后一个线程并发
                     try {
                         countDownLatch.await();
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                     System.out.println("并发线程" + threadCount.toString() + "："+Thread.currentThread().getName() + "开始");

                     threadCount.add(new BigInteger("1"));

                     try {
                         //创建锁节点
                         String createLockPath = zooKeeper.create(path+"/zk-lock",threadCount.toString().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE , CreateMode.EPHEMERAL_SEQUENTIAL);
                         System.out.println("锁节点相对路径：" + createLockPath);

                         //获取根节点所有的子节点列表
                         List<String> lockSonNodeList = zooKeeper.getChildren(path,true);
                         System.out.println(lockSonNodeList);

                     } catch (KeeperException e) {
                         e.printStackTrace();
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                 }
             });
        }
        Thread.sleep(20);
        countDownLatch.countDown();
    }

    /**
     * 监听事件
     * @param event
     */
    @Override
    public void process(WatchedEvent event) {
        System.out.println("监听事件线程："+Thread.currentThread().getName());

        if(Event.KeeperState.SyncConnected == event.getState()){
            System.out.println("Path："+event.getPath());
            if(Event.EventType.None == event.getType() || null == event.getPath()){
                connectCountDown.countDown();
            }
        }
    }
}
