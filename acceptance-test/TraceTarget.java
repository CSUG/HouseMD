import java.lang.Exception;
import java.lang.Thread;

public class TraceTarget {

    public static void main(String[] args) {
        while (true){
            addOne(0);
            try{
                Thread.sleep(500L);
            }catch (Exception e){
                break;
            }
        }
    }

    public static int addOne(int i){ return i + 1; }
}