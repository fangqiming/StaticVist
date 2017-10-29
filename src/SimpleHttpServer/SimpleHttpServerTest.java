package SimpleHttpServer;

import org.junit.Test;

/**
 * Created by fangiming on 2017/10/29.
 */
public class SimpleHttpServerTest {

    @Test
    public void testServer(){
        SimpleHttpServer simpleHttpServer=new SimpleHttpServer();
        simpleHttpServer.setBasePath("C:\\Users\\fangiming\\Desktop\\haha\\hello");
        try{
            simpleHttpServer.start();
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("错误");
        }

    }
}
