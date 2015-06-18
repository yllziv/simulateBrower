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
 * 爬豆瓣同城活动，并将爬取到的结果保存到数据库（包括图片的二进制数据）
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
        paDoubanTongCheng();//爬取数据
    }


    /***************************************
     * 爬豆瓣同城活动
     *****************************************/
    public static void paDoubanTongCheng() {

        try {
            WebClient webClient = new WebClient(BrowserVersion.CHROME);
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getCurrentWindow().setInnerHeight(60000);
            String[] activityClassName = {"music", "drama", "salon", "party", "film", "exhibition", "sports", "commonweal", "travel", "others"};
            for (int j = 0; j < activityClassName.length; j++) {
                HtmlPage classPage = (HtmlPage) webClient.getPage("http://www.douban.com/location/wuhan/events/future-" + activityClassName[j] + "?start=0");//从初始页面中得到总得页面数和页面的list
                HtmlDivision ulDiv = (HtmlDivision) classPage.getElementById("db-events-list");
                int sumPageNumber = 0;
                if(classPage.asText().indexOf("前页") != -1){
                    sumPageNumber = Integer.parseInt(ulDiv.getElementsByAttribute("span", "class", "thispage").get(0).getAttribute("data-total-page"));//总的页面数
                }else{
                    sumPageNumber = 1;
                }
                int currentPageListNumber = ulDiv.getElementsByAttribute("li", "class", "list-entry").size();//页面list的数量

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
     * @param classPage 从HtmlPage页面中提取中所需要的内容
     *                  豆瓣同城爬取内容：
     *                  ID   doubanActivityID                     douban_activity_id
     *                  标题 doubanActivityTitle                  douban_activity_title
     *                  URL doubanActivityURL                     douban_activity_url
     *                  图片 doubanActivityImg                    douban_activity_img
     *                  时间 doubanActivityTime                   douban_activity_time
     *                  地点 doubanActivityPos                    douban_activity_pos
     *                  参加人数 doubanActivityJoinMan            douban_activity_joinman
     *                  感兴趣人数 doubanActivityInterestMan      douban_activity_interestman
     *                  类别 doubanActivityClass                  douban_activity_class
     *                  图片二进制形式 doubanActivityImgBuffer    douban_activity_imgbuffer(bytea类型)
     *                                                            id (serial类型——自增长)
     */
    public static void getCurrentPageContent(HtmlPage classPage, int className) throws SQLException {
        HtmlDivision ulDiv = (HtmlDivision) classPage.getElementById("db-events-list");
        List<HtmlElement> allList = ulDiv.getElementsByAttribute("li", "class", "list-entry");
        for (int i = 0; i < allList.size(); i++) {
            String doubanActivityTitle = allList.get(i).getElementsByTagName("a").get(1).asText();
            String doubanActivityURL = allList.get(i).getElementsByTagName("a").get(0).getAttribute("href");
            String doubanActivityID = getNumberFromString(doubanActivityURL);
            String doubanActivityImg = allList.get(i).getElementsByTagName("img").get(0).getAttribute("src");
            String doubanActivityTime = getPosTime(allList.get(i).getElementsByTagName("li").get(0).asText(), "时间： ");
            String doubanActivityPos = getPosTime(allList.get(i).getElementsByTagName("li").get(1).asText(), "地点： ");
            String doubanActivityJoinMan = getNumberFromString(allList.get(i).getElementsByAttribute("p", "class", "counts").get(0).getElementsByTagName("span").get(0).asText());
            String doubanActivityInterestMan = getNumberFromString(allList.get(i).getElementsByAttribute("p", "class", "counts").get(0).getElementsByTagName("span").get(2).asText());
            String doubanActivityClass = Integer.toString(className);
            byte[] doubanActivityImgBuffer = GetImgFromHttp(doubanActivityImg);
            String[] doubanTongchengActivity = {doubanActivityID, doubanActivityTitle, doubanActivityURL, doubanActivityImg, doubanActivityTime, doubanActivityPos, doubanActivityJoinMan, doubanActivityInterestMan, doubanActivityClass};
            insert_mainData(doubanTongchengActivity,doubanActivityImgBuffer);
        }
    }

    /**
     * 连接postgres数据库
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
     * 在数据库中插入数据
     *
     * @param
     */
    public static boolean insert_mainData(String[] doubanTongchengList,byte[] doubanActivityImgBuffer) throws SQLException {

//        System.out.println("*****开始连接数据库....\n");

        Connection conn = getConn();
        //conn.setAutoCommit(false);

//        System.out.println("*****连接数据库成功....\n");
        conn.setAutoCommit(false);
        try {

            // 提交product的batch
            String pro_query = "INSERT INTO douban_activity VALUES ('" + doubanTongchengList[0] + "', '" + doubanTongchengList[1] + "', '" + doubanTongchengList[2] +
                    "','" + doubanTongchengList[3] + "', '" + doubanTongchengList[4] +
                    "', '" + doubanTongchengList[5] + "'," + doubanTongchengList[6] + "," + doubanTongchengList[7] + ", " + doubanTongchengList[8] + ", ?)";
            PreparedStatement statemenet = conn.prepareStatement(pro_query);
            statemenet.setBytes(1,doubanActivityImgBuffer);
            statemenet.addBatch();


            statemenet.executeBatch();
            conn.commit();//提交事务
            System.out.println("....插入数据成功!");
            conn.setAutoCommit(true);

        } catch (SQLException e) {
            e.printStackTrace();
            //取消事务
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
     * 将图片保存为二进制文件
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
     * @param a 字符串
     * @return 从字符串中提取的数字
     */
    public static String getNumberFromString(String a) {
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(a);
        String result = m.replaceAll("").trim();
        return result;
    }

    /**
     * @return 05月09日 ~ 08月01日 每周一至周五 20:00-20:30
     * @param1 时间： 05月09日 ~ 08月01日 每周一至周五 20:00-20:30
     * @param2 时间：
     */
    public static String getPosTime(String row, String a) {
        String regEx = "[" + a + "]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(row);
        return m.replaceAll("").trim();
    }

}
