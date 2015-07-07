import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
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
import org.apache.xerces.util.SynchronizedSymbolTable;


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
     *                  ��վ��� webType                          web_type
     *                  ����Ȥ���� doubanActivityInterestMan      douban_activity_interestman
     *                  ��� doubanActivityClass                  douban_activity_class
     *                  ͼƬ��������ʽ doubanActivityImgBuffer    douban_activity_imgbuffer(bytea����)
     *                  ��ʼʱ�� doubanStartTime                  douban_start_time(time without time zone)
     *                  ��ֹʱ�� doubanEndTime                    douban_end_time(time without time zone)
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

            Date doubanStartTime = getDateArray(doubanActivityTime).get(0);
            Date doubanEndTime = getDateArray(doubanActivityTime).get(1);

            String doubanActivityPos = getPosTime(allList.get(i).getElementsByTagName("li").get(1).asText(), "�ص㣺 ");
            String doubanActivityJoinMan = getNumberFromString(allList.get(i).getElementsByAttribute("p", "class", "counts").get(0).getElementsByTagName("span").get(0).asText());
            String webType = "7";
            String doubanActivityInterestMan = getNumberFromString(allList.get(i).getElementsByAttribute("p", "class", "counts").get(0).getElementsByTagName("span").get(2).asText());
            String doubanActivityClass = Integer.toString(className);
            byte[] doubanActivityImgBuffer = GetImgFromHttp(doubanActivityImg);
            String[] doubanTongchengActivity = {doubanActivityID, doubanActivityTitle, doubanActivityURL, doubanActivityImg, doubanActivityTime, doubanActivityPos, doubanActivityJoinMan,webType, doubanActivityInterestMan, doubanActivityClass};
//            System.out.println(doubanStartTime);

            insert_mainData(doubanTongchengActivity,doubanActivityImgBuffer,doubanStartTime,doubanEndTime);
        }
    }

    /**
     * ����postgres���ݿ�
     */
    public static Connection getConn() {
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://202.114.114.34:5432/crawler";
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
    public static boolean insert_mainData(String[] doubanTongchengList,byte[] doubanActivityImgBuffer,Date doubanStartTime,Date doubanEndTime) throws SQLException {

//        System.out.println("*****��ʼ�������ݿ�....\n");

        Connection conn = getConn();
        //conn.setAutoCommit(false);

//        System.out.println("*****�������ݿ�ɹ�....\n");
        conn.setAutoCommit(false);
        try {

            // �ύproduct��batch  ,5����ʼʱ��
            String pro_query = "INSERT INTO info_show VALUES (" + doubanTongchengList[0] + ", '" + doubanTongchengList[1] + "', '" + doubanTongchengList[2] +
                    "','" + doubanTongchengList[3] + "', '"+ doubanTongchengList[4] +
                    "', '" + doubanTongchengList[5] + "'," + doubanTongchengList[6] + ","+ doubanTongchengList[7] + "," + doubanTongchengList[8] + ", " + doubanTongchengList[9] + ", ?,?,?)";
            PreparedStatement statemenet = conn.prepareStatement(pro_query);
            statemenet.setBytes(1,doubanActivityImgBuffer);
            statemenet.setTimestamp(2, new java.sql.Timestamp(doubanStartTime.getTime()));
            statemenet.setTimestamp(3, new java.sql.Timestamp(doubanEndTime.getTime()));

            statemenet.addBatch();

            statemenet.executeBatch();
            conn.commit();//�ύ����
            insert_topicData(doubanTongchengList[0]);
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

    public static boolean insert_topicData(String doubanId) throws SQLException {

//        System.out.println("*****��ʼ�������ݿ�....\n");

        Connection conn = getConn();
        //conn.setAutoCommit(false);

//        System.out.println("*****�������ݿ�ɹ�....\n");
        conn.setAutoCommit(false);
        try {

            // �ύproduct��batch  ,5����ʼʱ��
            String pro_query = "INSERT INTO topic_show VALUES (" + doubanId + ", " + 0 + ", " + 7 + ")";
            PreparedStatement statemenet = conn.prepareStatement(pro_query);
            statemenet.addBatch();

            statemenet.executeBatch();
            conn.commit();//�ύ����
            System.out.println("....����Topic���ݳɹ�!");
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

    /**
     * �õ���ʼʱ�����ֹʱ��
     * @param test
     * @return
     */
    public static ArrayList<Date> getDateArray(String test) {
        Date startTime = new Date();
        Date endTime = new Date();
        if(test.indexOf("~") == -1){//û����ֹʱ��
            startTime = getDate(test);
        }else {// ����ֹʱ��
            startTime = getDate(test);
            endTime   = getDate(test.substring(test.indexOf("~")+1,test.length()));
        }
        ArrayList<Date> dateArray = new ArrayList<Date>();
        dateArray.add(startTime);
        dateArray.add(endTime);
        return dateArray;
    }

    public static Date getDate(String myTest) {

        Date date = new Date();
        try {
            String yearString = "2015";
            String monthString = "";
            String dayString = "";
            myTest = new String(myTest.getBytes("GBK"));


            Pattern pMonth = Pattern.compile("(\\d*)(?=��)");
            Matcher mMonth = pMonth.matcher(myTest);
            if (mMonth.find()) {
                monthString = mMonth.group();
            }

            Pattern pDay = Pattern.compile("(?<=��)(\\d*)(?=��)");
            Matcher mDay = pDay.matcher(myTest);
            if (mDay.find()) {
                dayString = mDay.group();
            }

            GregorianCalendar gc = new GregorianCalendar();
            if (yearString.length() > 0 && monthString.length() > 0 && dayString.length() > 0) {
                gc.set(Calendar.YEAR, Integer.parseInt(yearString));//������
                gc.set(Calendar.MONTH, Integer.parseInt(monthString) - 1);//����0��1��..�Դ������
                gc.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayString));//������
                date = gc.getTime();
//                System.out.println(new SimpleDateFormat("yyyy-MM-dd").format(date));
//                return (new SimpleDateFormat("yyyy-MM-dd").format(date));
            } else if (yearString.length() > 0 && monthString.length() > 0) {

                gc.set(Calendar.YEAR, Integer.parseInt(yearString));//������
                gc.set(Calendar.MONTH, Integer.parseInt(monthString) - 1);//����0��1��..�Դ������
                date = gc.getTime();
//                return (new SimpleDateFormat("yyyy-MM").format(date));
            } else if (yearString.length() > 0) {
                gc.set(Calendar.YEAR, Integer.parseInt(yearString));//������
                date = gc.getTime();
//                return (new SimpleDateFormat("yyyy").format(date));

            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return date;
    }

}
