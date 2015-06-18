import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.io.IOUtils;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;


/***************************************
 * ������ͬ�ǻ��������ȡ���Ľ�����浽���ݿ⣨����ͼƬ�Ķ��������ݣ�
 *****************************************/
public class crawlDoubanTongcheng {

    public static void main(String[] args) throws Exception {
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit")
                .setLevel(Level.OFF);

        java.util.logging.Logger.getLogger("org.apache.commons.httpclient")
                .setLevel(Level.OFF);

        testFunction();
    }

    public static void testFunction() throws SQLException {
        paDoubanTongCheng();//��ȡ����
    }


    /***************************************
     * ������ͬ�ǻ
     *****************************************/
    public static void paDoubanTongCheng() {

        try {
            WebClient webClient = new WebClient(BrowserVersion.CHROME);
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getCurrentWindow().setInnerHeight(60000);
            String[] activityClassName = {"music", "drama", "salon", "party", "film", "exhibition", "sports", "commonweal", "travel", "others"};
            for (int j = 0; j < activityClassName.length; j++) {
                HtmlPage classPage = (HtmlPage) webClient.getPage("http://www.douban.com/location/wuhan/events/future-" + activityClassName[j] + "?start=0");//�ӳ�ʼҳ���еõ��ܵ�ҳ������ҳ���list
                HtmlDivision ulDiv = (HtmlDivision) classPage.getElementById("db-events-list");
                int sumPageNumber = 0;
                if(classPage.asText().indexOf("ǰҳ") != -1){
                    sumPageNumber = Integer.parseInt(ulDiv.getElementsByAttribute("span", "class", "thispage").get(0).getAttribute("data-total-page"));//�ܵ�ҳ����
                }else{
                    sumPageNumber = 1;
                }
                int currentPageListNumber = ulDiv.getElementsByAttribute("li", "class", "list-entry").size();//ҳ��list������

                for (int i = 0; i < sumPageNumber; i++) {
                    HtmlPage everyPage = (HtmlPage) webClient.getPage("http://www.douban.com/location/wuhan/events/future-" + activityClassName[j] + "?start=" + Integer.toString(i * currentPageListNumber));
                    getCurrentPageContent(everyPage, j);
                }
            }
            webClient.closeAllWindows();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @param classPage ��HtmlPageҳ������ȡ������Ҫ������
     *                  ����ͬ����ȡ���ݣ�
     *                  ID   doubanActivityID                     douban_activity_id
     *                  ���� doubanActivityTitle                  douban_activity_title
     *                  URL doubanActivityURL                     douban_activity_url
     *                  ͼƬ doubanActivityImg                    douban_activity_img
     *                  ʱ�� doubanActivityTime                   douban_activity_time
     *                  �ص� doubanActivityPos                    douban_activity_pos
     *                  �μ����� doubanActivityJoinMan            douban_activity_joinman
     *                  ����Ȥ���� doubanActivityInterestMan      douban_activity_interestman
     *                  ��� doubanActivityClass                  douban_activity_class
     *                  ͼƬ��������ʽ doubanActivityImgBuffer    douban_activity_imgbuffer(bytea����)
     *                                                            id (serial���͡���������)
     */
    public static void getCurrentPageContent(HtmlPage classPage, int className) throws SQLException {
        HtmlDivision ulDiv = (HtmlDivision) classPage.getElementById("db-events-list");
        List<HtmlElement> allList = ulDiv.getElementsByAttribute("li", "class", "list-entry");
        for (int i = 0; i < allList.size(); i++) {
            String doubanActivityTitle = allList.get(i).getElementsByTagName("a").get(1).asText();
            String doubanActivityURL = allList.get(i).getElementsByTagName("a").get(0).getAttribute("href");
            String doubanActivityID = getNumberFromString(doubanActivityURL);
            String doubanActivityImg = allList.get(i).getElementsByTagName("img").get(0).getAttribute("src");
            String doubanActivityTime = getPosTime(allList.get(i).getElementsByTagName("li").get(0).asText(), "ʱ�䣺 ");
            String doubanActivityPos = getPosTime(allList.get(i).getElementsByTagName("li").get(1).asText(), "�ص㣺 ");
            String doubanActivityJoinMan = getNumberFromString(allList.get(i).getElementsByAttribute("p", "class", "counts").get(0).getElementsByTagName("span").get(0).asText());
            String doubanActivityInterestMan = getNumberFromString(allList.get(i).getElementsByAttribute("p", "class", "counts").get(0).getElementsByTagName("span").get(2).asText());
            String doubanActivityClass = Integer.toString(className);
            byte[] doubanActivityImgBuffer = GetImgFromHttp(doubanActivityImg);
            String[] doubanTongchengActivity = {doubanActivityID, doubanActivityTitle, doubanActivityURL, doubanActivityImg, doubanActivityTime, doubanActivityPos, doubanActivityJoinMan, doubanActivityInterestMan, doubanActivityClass};
            insert_mainData(doubanTongchengActivity,doubanActivityImgBuffer);
        }
    }

    /**
     * ����postgres���ݿ�
     */
    public static Connection getConn() {
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost:5432/crawler";
            try {
                conn = DriverManager.getConnection(url, "postgres", "admin");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return conn;
    }

    /**
     * �����ݿ��в�������
     *
     * @param
     */
    public static boolean insert_mainData(String[] doubanTongchengList,byte[] doubanActivityImgBuffer) throws SQLException {

//        System.out.println("*****��ʼ�������ݿ�....\n");

        Connection conn = getConn();
        //conn.setAutoCommit(false);

//        System.out.println("*****�������ݿ�ɹ�....\n");
        conn.setAutoCommit(false);
        try {

            // �ύproduct��batch
            String pro_query = "INSERT INTO douban_activity VALUES ('" + doubanTongchengList[0] + "', '" + doubanTongchengList[1] + "', '" + doubanTongchengList[2] +
                    "','" + doubanTongchengList[3] + "', '" + doubanTongchengList[4] +
                    "', '" + doubanTongchengList[5] + "'," + doubanTongchengList[6] + "," + doubanTongchengList[7] + ", " + doubanTongchengList[8] + ", ?)";
            PreparedStatement statemenet = conn.prepareStatement(pro_query);
            statemenet.setBytes(1,doubanActivityImgBuffer);
            statemenet.addBatch();


            statemenet.executeBatch();
            conn.commit();//�ύ����
            System.out.println("....�������ݳɹ�!");
            conn.setAutoCommit(true);

        } catch (SQLException e) {
            e.printStackTrace();
            //ȡ������
            try {
                conn.rollback();
            } catch (SQLException ee) {
                ee.printStackTrace();
            }
        } finally {
            conn.close();
        }

        return true;

    }

    /**
     * ��ͼƬ����Ϊ�������ļ�
     * @param http
     * @return
     */
    public static byte[] GetImgFromHttp(String http) {
        byte[] buffer = null;
        try {
            URL uploadUlr = new URL(http);
            HttpURLConnection connection = (HttpURLConnection)uploadUlr.openConnection();
            connection.setConnectTimeout(10*1000);
            connection.setRequestMethod("GET");
            connection.connect();
            InputStream is =  connection.getInputStream();
            int resCode = connection.getResponseCode();
            if (resCode==200) {


                buffer = IOUtils.toByteArray(is);

            }

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return buffer;
    }


    /**
     * @param a �ַ���
     * @return ���ַ�������ȡ������
     */
    public static String getNumberFromString(String a) {
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(a);
        String result = m.replaceAll("").trim();
        return result;
    }

    /**
     * @return 05��09�� ~ 08��01�� ÿ��һ������ 20:00-20:30
     * @param1 ʱ�䣺 05��09�� ~ 08��01�� ÿ��һ������ 20:00-20:30
     * @param2 ʱ�䣺
     */
    public static String getPosTime(String row, String a) {
        String regEx = "[" + a + "]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(row);
        return m.replaceAll("").trim();
    }

}
