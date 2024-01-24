package code;

import org.junit.Test;

import java.net.URI;

public class Code2 {


    @Test
    public void t1() throws Exception {
        show("wss://localhost:8080/ws?sdg=eg");

        show("http://localhost:8080/ws?sdg=eg");
        show("https://localhost:8080/ws?sdg=eg");
    }

    public void show(String url) throws Exception {
        URI uri = new URI(url);
        System.out.println(uri.getScheme());
        System.out.println(uri.getHost());
        System.out.println(uri.getPort());
        System.out.println(uri.getPath());
        System.out.println(uri.getQuery());
        System.out.println(uri.getRawPath());
        System.out.println(uri.getRawQuery());
        System.out.println("========================================");
    }

}
