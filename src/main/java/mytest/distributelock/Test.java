package mytest.distributelock;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by shidongsheng.it on 2017/9/20.
 */
public class Test {
    private static CountDownLatch countDownLatch = new CountDownLatch(10);
    public static void main(String[] args) throws InterruptedException {
        Integer integer = new Integer(1);
        System.out.println(Integer.valueOf("1",2));

        for(int i = 0; i < 10; i++){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("初始化");
                    try {
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("开始");

                }
            });
            thread.start();
        }
        Thread.sleep(10);
        System.out.println("主线程");
        countDownLatch.countDown();
        System.out.println(countDownLatch.getCount());

        List<String> list = new ArrayList<String>();
        list.add("hhh");
        System.out.println(list);
    }
}
