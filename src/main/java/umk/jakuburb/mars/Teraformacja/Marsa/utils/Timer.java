package umk.jakuburb.mars.Teraformacja.Marsa.utils;

import org.springframework.stereotype.Component;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

@Component
public class Timer implements Runnable{

    private List<Timerable> subscribers;
    private Thread thread;

    private long last;
    private long now;

    public Timer(){
        subscribers = new ArrayList<>();

        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        last = System.currentTimeMillis();

        try{
            while(true) {
                now = System.currentTimeMillis();
                if(now - last >= 1000){
                    subscribers.forEach(Timerable::doThing);
                    last = now;
                }
            }
        }catch(Exception e){
            System.out.println("Timer out: " + e);
        }
    }

    public void subscribe(Timerable t){
        subscribers.add(t);
    }

    public void unsubcribe(Timerable t){
        subscribers.remove(t);
    }


}
