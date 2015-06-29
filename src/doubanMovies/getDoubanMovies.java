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
 * 通过豆瓣电影的年度标签，爬电影信息
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
     * 通过豆瓣小组的名称得到用户名，并将用户名保存到数据库中
     *****************************************/
    public static void crawlUserName(){
        //循环年份
        for (int i = 2015; i < 2016; i++) {
            try {
                System.out.println("");
                System.out.println("start " + String.valueOf(i));
                WebClient webClient = new WebClient(BrowserVersion.CHROME);
                webClient.getOptions().setJavaScriptEnabled(false);
                webClient.getCurrentWindow().setInnerHeight(60000);

                //循环该年份标签的页数：
                for(int k = 107; k < 178; k++){
                    System.out.print(String.valueOf(k) + " ");
                    HtmlPage classPage = (HtmlPage) webClient.getPage("http://www.douban.com/tag/"+String.valueOf(i)+"/movie?start="+String.valueOf(k*15));
                    HtmlElement classDiv = (HtmlElement) classPage.getHtmlElementById("content");
                    //得到当前页面的数量：currentListNumber，若小于15，则说说明后面没有了
                    int currentListNumber = Integer.parseInt(String.valueOf(classDiv.getElementsByTagName("dl").size()));
                    if(currentListNumber == 0){
                        break;
                    }else{
                        //循环当前页面的电影
                        for(int j = 0; j < currentListNumber; j++){
                            passURLGetMovieInfo(classDiv.getElementsByTagName("dl").get(j).getElementsByTagName("a").get(0).getAttribute("href"),i);
                            Thread.sleep(1000); //等待 1s
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
     *    从豆瓣电影页面中提取中所需要的内容
     *    ID   doubanMovieID                     douban_movie_id
     *    名称 doubanMovieName                   douban_movie_name
     *    导演 doubanMovieDirector               douban_movie_director
     *    演员 doubanMovieActor                  douban_movie_actor
     *    类别 doubanMovieClass                  douban_movie_class
     *    语言 doubanMovieLanguage               douban_movie_language
     *    上映时间 doubanMovieReleaseTime         douban_movie_releasetime
     *    片长 doubanMovieTime                   douban_movie_time
     *    别名 doubanMovieOtherName              douban_movie_othername
     *    豆瓣URL doubanMovieURL                 douban_movie_url
     *    IMDb链接 IMDbMovieURL                  imdb_movie_url
     *    图片URL doubanMovieImgURL                    douban_movie_imgurl
     *
     *    电影短评数量 doubanMovieShortComment      douban_movie_shortcomment
     *    电影问题数量 doubanMovieQuestionComment   douban_movie_questioncomment
     *    电影长评数量 doubanMovieLongComment       douban_movie_longcomment
     *    电影讨论数量 doubanMovieTalkNum           douban_movie_talknum
     *    看过人数    doubanMovieLookedMan         douban_movie_lookedman
     *    想看人数 doubanMovieWantLookMan          douban_movie_wantlookman
     *
     *    电影总评分    doubanMovieScore           douban_movie_score
     *    电影评分人数  doubanMovieScorePeople     douban_movie_scorepeople
     *    电影5星百分比 douban5ScorePercent        douban_5scorepercent
     *    电影4星百分比 douban4ScorePercent        douban_4scorepercent
     *    电影3星百分比 douban3ScorePercent        douban_3scorepercent
     *    电影2星百分比 douban2ScorePercent        douban_2scorepercent
     *    电影1星百分比 douban1ScorePercent        douban_1scorepercent
     *    剧情简介 doubanMovieAbstract            douban_movie_abstract
     *    制片国家/地区 doubanMoviePlace            douban_movie_place
     *    电影年代     doubanMovieYear              douban_movie_year
     *                                            id(serial类型——自增长)
     */
    public static void passURLGetMovieInfo(String movieURL,int year){
//        System.out.println(movieURL);
        try {
            WebClient webClient = new WebClient(BrowserVersion.CHROME);
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getCurrentWindow().setInnerHeight(60000);
            HtmlPage moviePage = (HtmlPage) webClient.getPage(movieURL);
//            Thread.sleep(1000); //等待 1s
            getMovieDetailInfo(moviePage,movieURL,year);

            webClient.closeAllWindows();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getMovieDetailInfo(HtmlPage moviePage,String movieURL,int year) throws UnsupportedEncodingException, SQLException {

        //年代
        String doubanMovieYear = String.valueOf(year);
        doubanMovieYear = doubanMovieYear.replace("'", "");
        // ID
        String doubanMovieID = getNumberFromString(movieURL);
        //名称
        HtmlElement classDiv = (HtmlElement) moviePage.getHtmlElementById("content");
        String doubanMovieName = classDiv.getElementsByAttribute("span","property","v:itemreviewed").get(0).asText();
        doubanMovieName = doubanMovieName.replace("'", "");


        HtmlElement infoDiv = (HtmlElement) moviePage.getHtmlElementById("info");
        String infoDivTest = infoDiv.asText();
        //导演
        String doubanMovieDirector = getKeyWords("导演",infoDiv.asText());
        doubanMovieDirector = doubanMovieDirector.replace("'", "");


        //演员
        String doubanMovieActor =  getKeyWords("主演",infoDiv.asText());
        doubanMovieActor = doubanMovieActor.replace("'", "");


        //类别doubanMovieClass
        String doubanMovieClass = getKeyWords("类型",infoDiv.asText());
        doubanMovieClass = doubanMovieClass.replace("'", "");


        //语言doubanMovieLanguage
        String doubanMovieLanguage = getKeyWords("语言",infoDiv.asText());
        doubanMovieLanguage = doubanMovieLanguage.replace("'", "");


        //制片国家/地区 doubanMoviePlace
        String doubanMoviePlace = getKeyWords("制片国家/地区", infoDiv.asText());
        doubanMoviePlace = doubanMoviePlace.replace("'", "");


        //上映时间 doubanMovieReleaseTime
        String doubanMovieReleaseTime =  getKeyWords("上映日期", infoDiv.asText());
        doubanMovieReleaseTime = doubanMovieReleaseTime.replace("'", "");


        //片长 doubanMovieTime
        String doubanMovieTime = getKeyWords("片长", infoDiv.asText());
        doubanMovieTime = doubanMovieTime.replace("'", "");


        //别名 doubanMovieOtherName
        String doubanMovieOtherName = getKeyWords("又名", infoDiv.asText());
        doubanMovieOtherName = doubanMovieOtherName.replace("'", "");

        //豆瓣电影URL doubanMovieURL
        String doubanMovieURL = movieURL;
        doubanMovieURL = doubanMovieURL.replace("'", "");


        //IMDb链接 IMDbMovieURL
        String IMDbMovieURL = "";
        if(infoDiv.asText().indexOf("IMDb链接") != -1){
            int hrefANum = infoDiv.getElementsByTagName("a").size();
            IMDbMovieURL = infoDiv.getElementsByTagName("a").get(hrefANum - 1).getAttribute("href");
            IMDbMovieURL = IMDbMovieURL.replace("'", "");
        }else {
            IMDbMovieURL = "NULL";
        }


        //图片URL doubanMovieImgURL
        String doubanMovieImgURL = moviePage.getHtmlElementById("mainpic").getElementsByTagName("img").get(0).getAttribute("src");
        doubanMovieImgURL = doubanMovieImgURL.replace("'", "");
        doubanMovieImgURL = doubanMovieImgURL.replace("'", "");


        //电影短评数量 doubanMovieShortComment
        String doubanMovieShortComment = getNumberFromString(moviePage.getHtmlElementById("comments-section").getElementsByAttribute("span", "class", "pl").get(0).asText());

        //电影问题数量 doubanMovieQuestionComment
        String doubanMovieQuestionComment = "";
        if(infoDiv.asText().indexOf("的问题") != -1){
            doubanMovieQuestionComment = getNumberFromString(moviePage.getHtmlElementById("askmatrix").getElementsByAttribute("span","class","pl").get(0).asText());

        }else {
            doubanMovieQuestionComment = "NULL";
        }

        //电影长评数量 doubanMovieLongComment
        String doubanMovieLongComment = getNumberFromString(moviePage.getHtmlElementById("review_section").getElementsByAttribute("span","class","pl").get(0).asText());


        //电影讨论数量 doubanMovieTalkNum
        String doubanMovieTalkNum = "";
        if(infoDiv.asText().indexOf("去这部影片的讨论区") != -1){
            doubanMovieTalkNum = getNumberFromString(moviePage.getHtmlElementById("content").getElementsByAttribute("h2","class","discussion_link").get(0).asText());
        }else {
            doubanMovieTalkNum = "NULL";
        }

        //看过人数    doubanMovieLookedMan
        String doubanMovieLookedMan = "";
        if(moviePage.getHtmlElementById("subject-others-interests").getElementsByAttribute("div","class","subject-others-interests-ft").get(0).asText().indexOf("看过") != -1) {
            doubanMovieLookedMan = getNumberFromString(moviePage.getHtmlElementById("subject-others-interests").getElementsByAttribute("div", "class", "subject-others-interests-ft").get(0).getElementsByTagName("a").get(0).asText());
        }else {
            doubanMovieLookedMan = "NULL";
        }
        //想看人数 doubanMovieWantLookMan
        String doubanMovieWantLookMan = "";
        if(moviePage.getHtmlElementById("subject-others-interests").getElementsByAttribute("div","class","subject-others-interests-ft").get(0).asText().indexOf("想看") != -1) {
            doubanMovieWantLookMan = getNumberFromString(moviePage.getHtmlElementById("subject-others-interests").getElementsByAttribute("div", "class", "subject-others-interests-ft").get(0).getElementsByTagName("a").get(1).asText());
        }else {
            doubanMovieWantLookMan = "NULL";
        }
        //评分：
        String doubanMovieScore = "";
        String doubanMovieScorePeople = "";
        String douban5ScorePercent = "";
        String douban4ScorePercent = "";
        String douban3ScorePercent = "";
        String douban2ScorePercent = "";
        String douban1ScorePercent = "";
        if(classDiv.asText().indexOf("评价人数不足") != -1){
            doubanMovieScore = "NULL";
            doubanMovieScorePeople = "NULL";
            douban5ScorePercent = "NULL";
            douban4ScorePercent = "NULL";
            douban3ScorePercent = "NULL";
            douban2ScorePercent = "NULL";
            douban1ScorePercent = "NULL";
        }else {

            //电影总评分    doubanMovieScore
            doubanMovieScore = moviePage.getHtmlElementById("interest_sectl").getElementsByAttribute("strong", "class", "ll rating_num").get(0).asText();

            //电影评分人数  doubanMovieScorePeople
            doubanMovieScorePeople = moviePage.getHtmlElementById("interest_sectl").getElementsByAttribute("span", "property", "v:votes").get(0).asText();

            //电影5星百分比 douban5ScorePercent
            douban5ScorePercent = String.valueOf(Float.parseFloat(getNumberFromString(moviePage.getHtmlElementById("interest_sectl").getElementsByAttribute("span", "class", "stars5 starstop").get(0).getNextSibling().getNextSibling().getNextSibling().asText())) / 10.0);

            //电影4星百分比 douban4ScorePercent
            douban4ScorePercent = String.valueOf(Float.parseFloat(getNumberFromString(moviePage.getHtmlElementById("interest_sectl").getElementsByAttribute("span", "class", "stars4 starstop").get(0).getNextSibling().getNextSibling().getNextSibling().asText())) / 10.0);

            //电影3星百分比 douban3ScorePercent
            douban3ScorePercent = String.valueOf(Float.parseFloat(getNumberFromString(moviePage.getHtmlElementById("interest_sectl").getElementsByAttribute("span", "class", "stars3 starstop").get(0).getNextSibling().getNextSibling().getNextSibling().asText())) / 10.0);

            //电影2星百分比 douban2ScorePercent
            douban2ScorePercent = String.valueOf(Float.parseFloat(getNumberFromString(moviePage.getHtmlElementById("interest_sectl").getElementsByAttribute("span", "class", "stars2 starstop").get(0).getNextSibling().getNextSibling().getNextSibling().asText())) / 10.0);

            //电影1星百分比 douban1ScorePercent
            douban1ScorePercent = String.valueOf(Float.parseFloat(getNumberFromString(moviePage.getHtmlElementById("interest_sectl").getElementsByAttribute("span", "class", "stars1 starstop").get(0).getNextSibling().getNextSibling().getNextSibling().asText())) / 10.0);
        }
        //剧情简介 doubanMovieAbstract
        String doubanMovieAbstract = "";
        if(classDiv.asText().indexOf("剧情简介") != -1) {
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
     * 连接postgres数据库
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
     * 豆瓣用户的名称保存到数据库中
     *
     * @param
     */
    public static boolean insertUserNameToDB(String[] doubanMoviesList) throws SQLException {

//        System.out.println("*****start to connect ....\n");

        Connection conn = getConn();

//        System.out.println("*****connected success....\n");
        conn.setAutoCommit(false);
        try {
            // 提交product的batch
            String pro_query = "INSERT INTO movie VALUES ('" + doubanMoviesList[0]  + "', '" +doubanMoviesList[1] + "', '" +doubanMoviesList[2] +  "', '" +doubanMoviesList[3]
                    +  "', '" +doubanMoviesList[4] +  "', '" +doubanMoviesList[5] +  "', '" +doubanMoviesList[6] +  "', '" +doubanMoviesList[7] +  "', '" +doubanMoviesList[8]
                    +  "', '" +doubanMoviesList[9] +  "', '" +doubanMoviesList[10] +  "', '" +doubanMoviesList[11] +  "', " +doubanMoviesList[12] +  ", " +doubanMoviesList[13]
                    +  ", " +doubanMoviesList[14] +  ", " +doubanMoviesList[15] +  ", " +doubanMoviesList[16] +  ", " +doubanMoviesList[17] +  ", '" +doubanMoviesList[18]
                    +  "', " +doubanMoviesList[19] +  ", '" +doubanMoviesList[20] +  "', '" +doubanMoviesList[21] +  "', '" +doubanMoviesList[22] +  "', '" +doubanMoviesList[23]
                    + "', '" +doubanMoviesList[24] +  "', '" +doubanMoviesList[25] +  "', '" +doubanMoviesList[26]  +  "', " +doubanMoviesList[27] +  ")";
            Statement statemenet = conn.createStatement();
            statemenet.addBatch(pro_query);

            statemenet.executeBatch();
            conn.commit();//提交事务
//            System.out.println("....insert data ok!");
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
     *
     * 制片国家/地区: 新西兰
     * 语言: 英语
     * 上映日期: 2014-03-10
     *
     * 从上述一堆字符中，输入("语言","balala")得到 "英语"
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
