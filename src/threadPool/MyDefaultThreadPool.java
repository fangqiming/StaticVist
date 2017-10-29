package threadPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by fangiming on 2017/10/28.
 */
public class MyDefaultThreadPool<Job extends Runnable> implements MyThreadPool<Job> {
    private static final int max_worker_number=10;
    private static final int default_worker_number=5;
    private static final int min_worker_numer=1;

    private final LinkedList<Job> jobs=new LinkedList<Job>();

    private final List<Worker> workers= Collections.synchronizedList(new ArrayList<Worker>());

    private int workerNum=default_worker_number;
    private AtomicLong threadNum=new AtomicLong();


    public MyDefaultThreadPool(){
        initializeWorkers(default_worker_number);
    }

    public MyDefaultThreadPool(int num){    //启动的时候，默认就初始化了工作线程
        workerNum=num>max_worker_number ? max_worker_number : num<min_worker_numer ? min_worker_numer : num;
        initializeWorkers(workerNum);
    }

    private void initializeWorkers(int num){
        for(int i=0;i<num;i++){
            Worker worker=new Worker();
            workers.add(worker);
            Thread thread=new Thread(worker,"ThreadPool-Worker-"+threadNum.incrementAndGet());
            thread.start();
        }
    }

    @Override
    public void execute(Job job) {
        if(job!=null){
            synchronized (jobs){
                jobs.addLast(job);
                jobs.notify();
            }
        }
    }

    @Override
    public void shutdown() {
        for(Worker o : workers)
            o.shutdown();
    }

    @Override
    public void addWorkers(int num) {
        synchronized (jobs){
            if(num+this.workerNum>max_worker_number){
                num=max_worker_number-this.workerNum;
            }
            initializeWorkers(num);
            this.workerNum+=num;
        }
    }

    @Override
    public void removeWorkers(int num) {
        synchronized (jobs){
            if(num>=this.workerNum){
                throw new IllegalArgumentException("beyond workNum");
            }
            int count=0;
            while(count<num){
                Worker worker=workers.get(count);
                if(workers.remove(worker)){
                    worker.shutdown();
                    count++;
                }
            }
            this.workerNum-=count;
        }
    }

    @Override
    public int getJobSize() {
        return jobs.size();
    }

    class Worker implements Runnable{

        private volatile boolean running=true;  //似乎并没有线程共享该变量。。。
        @Override
        public void run() {
            while(running){     //一直循环，直到被叫停
                Job job=null;
                synchronized (jobs){
                    while (jobs.isEmpty()){
                        try{
                            jobs.wait();
                        }catch (InterruptedException e){
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    job=jobs.removeFirst();
                }
                if(job!=null){
                    try{
                        job.run();
                    }catch(Exception e){}
                }
            }
        }

        public void shutdown(){
            running=false;
        }
    }
}
