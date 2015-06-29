package doubanMovies;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.MatchResult;
import java.util.regex.PatternSyntaxException;


/***************************************
 * ͨ�������Ӱ����ȱ�ǩ������Ӱ��Ϣ
 *****************************************/
public class getDoubanMovies {

    public static void main(String[] args) throws Exception {
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit")
                .setLevel(Level.OFF);

        java.util.logging.Logger.getLogger("org.apache.commons.httpclient")
                .setLevel(Level.OFF);

        testFunction();
    }

    public static void testFunction() throws SQLException {

        crawlUserName();
    }


    /***************************************
     * ͨ������С������Ƶõ��û����������û������浽���ݿ���
     *****************************************/
    public static void crawlUserName(){
        //ѭ�����
        for (int i = 2015; i < 2016; i++) {
            try {
                System.out.println("");
                System.out.println("start " + String.valueOf(i));
                WebClient webClient = new WebClient(BrowserVersion.CHROME);
                webClient.getOptions().setJavaScriptEnabled(false);
                webClient.getCurrentWindow().setInnerHeight(60000);

                //ѭ������ݱ�ǩ��ҳ����
                for(int k = 107; k < 178; k++){
                    System.out.print(String.valueOf(k) + " ");
                    HtmlPage classPage = (HtmlPage) webClient.getPage("http://www.douban.com/tag/"+String.valueOf(i)+"/movie?start="+String.valueOf(k*15));
                    HtmlElement classDiv = (HtmlElement) classPage.getHtmlElementById("content");
                    //�õ���ǰҳ���������currentListNumber����С��15����˵˵������û����
                    int currentListNumber = Integer.parseInt(String.valueOf(classDiv.getElementsByTagName("dl").size()));
                    if(currentListNumber == 0){
                        break;
                    }else{
                        //ѭ����ǰҳ��ĵ�Ӱ
                        for(int j = 0; j < currentListNumber; j++){
                            passURLGetMovieInfo(classDiv.getElementsByTagName("dl").get(j).getElementsByTagName("a").get(0).getAttribute("href"),i);
                            Thread.sleep(1000); //�ȴ� 1s
//                            passURLGetMovieInfo("http://movie.douban.com/subject/2156663/?from=tag_all",i);
                        }
                    }
                }

                webClient.closeAllWindows();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     *    �Ӷ����Ӱҳ������ȡ������Ҫ������
     *    ID   doubanMovieID                     douban_movie_id
     *    ���� doubanMovieName                   douban_movie_name
     *    ���� doubanMovieDirector               douban_movie_director
     *    ��Ա doubanMovieActor                  douban_movie_actor
     *    ��� doubanMovieClass                  douban_movie_class
     *    ���� doubanMovieLanguage               douban_movie_language
     *    ��ӳʱ�� doubanMovieReleaseTime         douban_movie_releasetime
     *    Ƭ�� doubanMovieTime                   douban_movie_time
     *    ���� doubanMovieOtherName              douban_movie_othername
     *    ����URL doubanMovieURL                 douban_movie_url
     *    IMDb���� IMDbMovieURL                  imdb_movie_url
     *    ͼƬURL doubanMovieImgURL                    douban_movie_imgurl
     *
     *    ��Ӱ�������� doubanMovieShortComment      douban_movie_shortcomment
     *    ��Ӱ�������� doubanMovieQuestionComment   douban_movie_questioncomment
     *    ��Ӱ�������� doubanMovieLongComment       douban_movie_longcomment
     *    ��Ӱ�������� doubanMovieTalkNum           douban_movie_talknum
     *    ��������    doubanMovieLookedMan         douban_movie_lookedman
     *    �뿴���� doubanMovieWantLookMan          douban_movie_wantlookman
     *
     *    ��Ӱ������    doubanMovieScore           douban_movie_score
     *    ��Ӱ��������  doubanMovieScorePeople     douban_movie_scorepeople
     *    ��Ӱ5�ǰٷֱ� douban5ScorePercent        douban_5scorepercent
     *    ��Ӱ4�ǰٷֱ� douban4ScorePercent        douban_4scorepercent
     *    ��Ӱ3�ǰٷֱ� douban3ScorePercent        douban_3scorepercent
     *    ��Ӱ2�ǰٷֱ� douban2ScorePercent        douban_2scorepercent
     *    ��Ӱ1�ǰٷֱ� douban1ScorePercent        douban_1scorepercent
     *    ������ doubanMovieAbstract            douban_movie_abstract
     *    ��Ƭ����/���� doubanMoviePlace            douban_movie_place
     *    ��Ӱ���     doubanMovieYear              douban_movie_year
     *                                            id(serial���͡���������)
     */
    public static void passURLGetMovieInfo(String movieURL,int year){
//        System.out.println(movieURL);
        try {
            WebClient webClient = new WebClient(BrowserVersion.CHROME);
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getCurrentWindow().setInnerHeight(60000);
            HtmlPage moviePage = (HtmlPage) webClient.getPage(movieURL);
//            Thread.sleep(1000); //�ȴ� 1s
            getMovieDetailInfo(moviePage,movieURL,year);

            webClient.closeAllWindows();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getMovieDetailInfo(HtmlPage moviePage,String movieURL,int year) throws UnsupportedEncodingException, SQLException {

        //���
        String doubanMovieYear = String.valueOf(year);
        doubanMovieYear = doubanMovieYear.replace("'", "");
        // ID
        String doubanMovieID = getNumberFromString(movieURL);
        //����
        HtmlElement classDiv = (HtmlElement) moviePage.getHtmlElementById("content");
        String doubanMovieName = classDiv.getElementsByAttribute("span","property","v:itemreviewed").get(0).asText();
        doubanMovieName = doubanMovieName.replace("'", "");


        HtmlElement infoDiv = (HtmlElement) moviePage.getHtmlElementById("info");
        String infoDivTest = infoDiv.asText();
        //����
        String doubanMovieDirector = getKeyWords("����",infoDiv.asText());
        doubanMovieDirector = doubanMovieDirector.replace("'", "");


        //��Ա
        String doubanMovieActor =  getKeyWords("����",infoDiv.asText());
        doubanMovieActor = doubanMovieActor.replace("'", "");


        //���doubanMovieClass
        String doubanMovieClass = getKeyWords("����",infoDiv.asText());
        doubanMovieClass = doubanMovieClass.replace("'", "");


        //����doubanMovieLanguage
        String doubanMovieLanguage = getKeyWords("����",infoDiv.asText());
        doubanMovieLanguage = doubanMovieLanguage.replace("'", "");


        //��Ƭ����/���� doubanMoviePlace
        String doubanMoviePlace = getKeyWords("��Ƭ����/����", infoDiv.asText());
        doubanMoviePlace = doubanMoviePlace.replace("'", "");


        //��ӳʱ�� doubanMovieReleaseTime
        String doubanMovieReleaseTime =  getKeyWords("��ӳ����", infoDiv.asText());
        doubanMovieReleaseTime = doubanMovieReleaseTime.replace("'", "");


        //Ƭ�� doubanMovieTime
        String doubanMovieTime = getKeyWords("Ƭ��", infoDiv.asText());
        doubanMovieTime = doubanMovieTime.replace("'", "");


        //���� doubanMovieOtherName
        String doubanMovieOtherName = getKeyWords("����", infoDiv.asText());
        doubanMovieOtherName = doubanMovieOtherName.replace("'", "");

        //�����ӰURL doubanMovieURL
        String doubanMovieURL = movieURL;
        doubanMovieURL = doubanMovieURL.replace("'", "");


        //IMDb���� IMDbMovieURL
        String IMDbMovieURL = "";
        if(infoDiv.asText().indexOf("IMDb����") != -1){
            int hrefANum = infoDiv.getElementsByTagName("a").size();
            IMDbMovieURL = infoDiv.getElementsByTagName("a").get(hrefANum - 1).getAttribute("href");
            IMDbMovieURL = IMDbMovieURL.replace("'", "");
        }else {
            IMDbMovieURL = "NULL";
        }


        //ͼƬURL doubanMovieImgURL
        String doubanMovieImgURL = moviePage.getHtmlElementById("mainpic").getElementsByTagName("img").get(0).getAttribute("src");
        doubanMovieImgURL = doubanMovieImgURL.replace("'", "");
        doubanMovieImgURL = doubanMovieImgURL.replace("'", "");


        //��Ӱ�������� doubanMovieShortComment
        String doubanMovieShortComment = getNumberFromString(moviePage.getHtmlElementById("comments-section").getElementsByAttribute("span", "class", "pl").get(0).asText());

        //��Ӱ�������� doubanMovieQuestionComment
        String doubanMovieQuestionComment = "";
        if(infoDiv.asText().indexOf("������") != -1){
            doubanMovieQuestionComment = getNumberFromString(moviePage.getHtmlElementById("askmatrix").getElementsByAttribute("span","class","pl").get(0).asText());

        }else {
            doubanMovieQuestionComment = "NULL";
        }

        //��Ӱ�������� doubanMovieLongComment
        String doubanMovieLongComment = getNumberFromString(moviePage.getHtmlElementById("review_section").getElementsByAttribute("span","class","pl").get(0).asText());


        //��Ӱ�������� doubanMovieTalkNum
        String doubanMovieTalkNum = "";
        if(infoDiv.asText().indexOf("ȥ�ⲿӰƬ��������") != -1){
            doubanMovieTalkNum = getNumberFromString(moviePage.getHtmlElementById("content").getElementsByAttribute("h2","class","discussion_link").get(0).asText());
        }else {
            doubanMovieTalkNum = "NULL";
        }

        //��������    doubanMovieLookedMan
        String doubanMovieLookedMan = "";
        if(moviePage.getHtmlElementById("subject-others-interests").getElementsByAttribute("div","class","subject-others-interests-ft").get(0).asText().indexOf("����") != -1) {
            doubanMovieLookedMan = getNumberFromString(moviePage.getHtmlElementById("subject-others-interests").getElementsByAttribute("div", "class", "subject-others-interests-ft").get(0).getElementsByTagName("a").get(0).asText());
        }else {
            doubanMovieLookedMan = "NULL";
        }
        //�뿴���� doubanMovieWantLookMan
        String doubanMovieWantLookMan = "";
        if(moviePage.getHtmlElementById("subject-others-interests").getElementsByAttribute("div","class","subject-others-interests-ft").get(0).asText().indexOf("�뿴") != -1) {
            doubanMovieWantLookMan = getNumberFromString(moviePage.getHtmlElementById("subject-others-interests").getElementsByAttribute("div", "class", "subject-others-interests-ft").get(0).getElementsByTagName("a").get(1).asText());
        }else {
            doubanMovieWantLookMan = "NULL";
        }
        //���֣�
        String doubanMovieScore = "";
        String doubanMovieScorePeople = "";
        String douban5ScorePercent = "";
        String douban4ScorePercent = "";
        String douban3ScorePercent = "";
        String douban2ScorePercent = "";
        String douban1ScorePercent = "";
        if(classDiv.asText().indexOf("������������") != -1){
            doubanMovieScore = "NULL";
            doubanMovieScorePeople = "NULL";
            douban5ScorePercent = "NULL";
            douban4ScorePercent = "NULL";
            douban3ScorePercent = "NULL";
            douban2ScorePercent = "NULL";
            douban1ScorePercent = "NULL";
        }else {

            //��Ӱ������    doubanMovieScore
            doubanMovieScore = moviePage.getHtmlElementById("interest_sectl").getElementsByAttribute("strong", "class", "ll rating_num").get(0).asText();

            //��Ӱ��������  doubanMovieScorePeople
            doubanMovieScorePeople = moviePage.getHtmlElementById("interest_sectl").getElementsByAttribute("span", "property", "v:votes").get(0).asText();

            //��Ӱ5�ǰٷֱ� douban5ScorePercent
            douban5ScorePercent = String.valueOf(Float.parseFloat(getNumberFromString(moviePage.getHtmlElementById("interest_sectl").getElementsByAttribute("span", "class", "stars5 starstop").get(0).getNextSibling().getNextSibling().getNextSibling().asText())) / 10.0);

            //��Ӱ4�ǰٷֱ� douban4ScorePercent
            douban4ScorePercent = String.valueOf(Float.parseFloat(getNumberFromString(moviePage.getHtmlElementById("interest_sectl").getElementsByAttribute("span", "class", "stars4 starstop").get(0).getNextSibling().getNextSibling().getNextSibling().asText())) / 10.0);

            //��Ӱ3�ǰٷֱ� douban3ScorePercent
            douban3ScorePercent = String.valueOf(Float.parseFloat(getNumberFromString(moviePage.getHtmlElementById("interest_sectl").getElementsByAttribute("span", "class", "stars3 starstop").get(0).getNextSibling().getNextSibling().getNextSibling().asText())) / 10.0);

            //��Ӱ2�ǰٷֱ� douban2ScorePercent
            douban2ScorePercent = String.valueOf(Float.parseFloat(getNumberFromString(moviePage.getHtmlElementById("interest_sectl").getElementsByAttribute("span", "class", "stars2 starstop").get(0).getNextSibling().getNextSibling().getNextSibling().asText())) / 10.0);

            //��Ӱ1�ǰٷֱ� douban1ScorePercent
            douban1ScorePercent = String.valueOf(Float.parseFloat(getNumberFromString(moviePage.getHtmlElementById("interest_sectl").getElementsByAttribute("span", "class", "stars1 starstop").get(0).getNextSibling().getNextSibling().getNextSibling().asText())) / 10.0);
        }
        //������ doubanMovieAbstract
        String doubanMovieAbstract = "";
        if(classDiv.asText().indexOf("������") != -1) {
            doubanMovieAbstract = moviePage.getHtmlElementById("link-report").getElementsByAttribute("span", "property", "v:summary").get(0).asText();
            doubanMovieAbstract = doubanMovieAbstract.replace("'", "");
        }else{
            doubanMovieAbstract = "NULL";
        }

        String[] doubanMovies = {
                doubanMovieID,doubanMovieName,doubanMovieDirector,doubanMovieActor,doubanMovieClass,doubanMovieLanguage,doubanMovieReleaseTime,doubanMovieTime,doubanMovieOtherName,
                doubanMovieURL,IMDbMovieURL,doubanMovieImgURL,doubanMovieShortComment,doubanMovieQuestionComment,doubanMovieLongComment,doubanMovieTalkNum,doubanMovieLookedMan,
                doubanMovieWantLookMan,doubanMovieScore, doubanMovieScorePeople,douban5ScorePercent,douban4ScorePercent,douban3ScorePercent,douban2ScorePercent,douban1ScorePercent,
                doubanMovieAbstract,doubanMoviePlace,doubanMovieYear
        };
//        for( int n = 0; n < doubanMovies.length; n++){
//            System.out.println(doubanMovies[n]);
//        }
//        System.out.println("INSERT INTO movie VALUES ('" + doubanMovies[0]  + "', '" +doubanMovies[1] + "', '" +doubanMovies[2] +  "', '" +doubanMovies[3]
//                +  "', '" +doubanMovies[4] +  "', '" +doubanMovies[5] +  "', '" +doubanMovies[6] +  "', '" +doubanMovies[7] +  "', '" +doubanMovies[8]
//                +  "', '" +doubanMovies[9] +  "', '" +doubanMovies[10] +  "', '" +doubanMovies[11] +  "', '" +doubanMovies[12] +  "', '" +doubanMovies[13]
//                +  "', '" +doubanMovies[14] +  "', '" +doubanMovies[15] +  "', '" +doubanMovies[16] +  "', '" +doubanMovies[17] +  "', '" +doubanMovies[18]
//                +  "', '" +doubanMovies[19] +  "', '" +doubanMovies[20] +  "', '" +doubanMovies[21] +  "', '" +doubanMovies[22] +  "', '" +doubanMovies[23]
//                + "', '" +doubanMovies[24] +  "', '" +doubanMovies[25] +  "', '" +doubanMovies[26] +  "')");
        insertUserNameToDB(doubanMovies);
    }

    /**
     * ����postgres���ݿ�
     */
    public static Connection getConn() {
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost:5432/doubanMovies";
            try {
                conn = DriverManager.getConnection(url, "yll", "1qaz");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return conn;
    }

    /**
     * �����û������Ʊ��浽���ݿ���
     *
     * @param
     */
    public static boolean insertUserNameToDB(String[] doubanMoviesList) throws SQLException {

//        System.out.println("*****start to connect ....\n");

        Connection conn = getConn();

//        System.out.println("*****connected success....\n");
        conn.setAutoCommit(false);
        try {
            // �ύproduct��batch
            String pro_query = "INSERT INTO movie VALUES ('" + doubanMoviesList[0]  + "', '" +doubanMoviesList[1] + "', '" +doubanMoviesList[2] +  "', '" +doubanMoviesList[3]
                    +  "', '" +doubanMoviesList[4] +  "', '" +doubanMoviesList[5] +  "', '" +doubanMoviesList[6] +  "', '" +doubanMoviesList[7] +  "', '" +doubanMoviesList[8]
                    +  "', '" +doubanMoviesList[9] +  "', '" +doubanMoviesList[10] +  "', '" +doubanMoviesList[11] +  "', " +doubanMoviesList[12] +  ", " +doubanMoviesList[13]
                    +  ", " +doubanMoviesList[14] +  ", " +doubanMoviesList[15] +  ", " +doubanMoviesList[16] +  ", " +doubanMoviesList[17] +  ", '" +doubanMoviesList[18]
                    +  "', " +doubanMoviesList[19] +  ", '" +doubanMoviesList[20] +  "', '" +doubanMoviesList[21] +  "', '" +doubanMoviesList[22] +  "', '" +doubanMoviesList[23]
                    + "', '" +doubanMoviesList[24] +  "', '" +doubanMoviesList[25] +  "', '" +doubanMoviesList[26]  +  "', " +doubanMoviesList[27] +  ")";
            Statement statemenet = conn.createStatement();
            statemenet.addBatch(pro_query);

            statemenet.executeBatch();
            conn.commit();//�ύ����
//            System.out.println("....insert data ok!");
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
     *
     * ��Ƭ����/����: ������
     * ����: Ӣ��
     * ��ӳ����: 2014-03-10
     *
     * ������һ���ַ��У�����("����","balala")�õ� "Ӣ��"
     */

    public static String getKeyWords(String keyWords,String rowString){
        String doubanMovieLanguage = "";
        if(rowString.indexOf(keyWords) != -1){
            int doubanMovieLanguageIndex = rowString.indexOf(keyWords);
            int doubanMovieLanguageEndIndex = rowString.substring(doubanMovieLanguageIndex,rowString.length()).indexOf("\n");
            doubanMovieLanguage = rowString.substring(doubanMovieLanguageIndex+keyWords.length()+2 , doubanMovieLanguageIndex+doubanMovieLanguageEndIndex);
        }else {
            doubanMovieLanguage = "NULL";
        }
        return doubanMovieLanguage;
    }

}
