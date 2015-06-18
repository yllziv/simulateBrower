
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.*;
import static java.lang.System.*;

public class getPhoneNumber
{
    public static void main(String[] args) {
        //windows
        String path = "C:/Users/ziv/IdeaProjects/simulateBrower/rectangle/";
//        String url = "http://image.58.com/showphone.aspx?t=v55&v=390EB134967C595F24A56BE6A5F370487";
        String url = "http://image.58.com/showphone.aspx?t=v55&v=32DBF4D30F92576ERB77C93F9CB1F6907";
        String cmd = "python  " + path + "pipei.py " + url + " " + path;

        Runtime run = Runtime.getRuntime();
        try {
            Process p = run.exec(cmd);
            BufferedInputStream in = new BufferedInputStream(p.getInputStream());
            BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
            String lineStr;
            while ((lineStr = inBr.readLine()) != null)
                System.out.println(lineStr);
            inBr.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
