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
import org.w3c.dom.html.HTMLElement;

import javax.swing.text.Style;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.MatchResult;
import java.util.regex.PatternSyntaxException;


/***************************************
 * 通过豆瓣电影的年度标签，爬电影信息
 *****************************************/
public class movieTest {

    public static void main(String[] args) throws Exception {
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit")
                .setLevel(Level.OFF);

        java.util.logging.Logger.getLogger("org.apache.commons.httpclient")
                .setLevel(Level.OFF);

        testFunction();
    }

    public static void testFunction() throws SQLException {

        passURLGetMovieInfo("http://movie.douban.com/subject/25909566/");
//        passURLGetMovieInfo("http://movie.douban.com/subject/1507386/");
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
     *                                            id(serial类型——自增长)
     */
    public static void passURLGetMovieInfo(String movieURL){
        System.out.println(movieURL);
        try {
            WebClient webClient = new WebClient(BrowserVersion.CHROME);
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getCurrentWindow().setInnerHeight(60000);
            HtmlPage moviePage = (HtmlPage) webClient.getPage(movieURL);
            getMovieDetailInfo(moviePage,movieURL);
//            insertUserNameToDB(getMovieDetailInfo(moviePage));

            webClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getMovieDetailInfo(HtmlPage moviePage,String movieURL) throws UnsupportedEncodingException, SQLException {
        // ID 对的
        String doubanMovieID = getNumberFromString(movieURL);
        //名称 对的
        HtmlElement classDiv = (HtmlElement) moviePage.getHtmlElementById("content");
        String doubanMovieName = classDiv.getElementsByAttribute("span","property","v:itemreviewed").get(0).asText();

        HtmlElement infoDiv = (HtmlElement) moviePage.getHtmlElementById("info");


        String infoDivTest = infoDiv.asText();
        System.out.println(infoDivTest);


        //演员
        String doubanMovieActor = "";
        if(infoDivTest.indexOf("主演") != -1){
            doubanMovieActor = infoDiv.getElementsByAttribute("span", "class", "actor").get(0).asText();
            doubanMovieActor = doubanMovieActor.substring(3, doubanMovieActor.length());
        }else {
            doubanMovieActor = "NULL";
        }

        System.out.println(doubanMovieActor);


        int plNum = infoDiv.getElementsByAttribute("span","class","pl").size();

        //语言doubanMovieLanguage
        String doubanMovieLanguage = "";
        if(infoDivTest.indexOf("语言") != -1){
            int doubanMovieLanguageIndex = infoDivTest.indexOf("语言");
            int doubanMovieLanguageEndIndex = infoDivTest.substring(doubanMovieLanguageIndex,infoDivTest.length()).indexOf("\n");
            doubanMovieLanguage = infoDivTest.substring(doubanMovieLanguageIndex+3 , doubanMovieLanguageIndex+doubanMovieLanguageEndIndex);
        }else {
            doubanMovieLanguage = "NULL";
        }

        System.out.println("语言" + doubanMovieLanguage);

//        String doubanMovieLanguage = infoDiv.getElementsByAttribute("span", "class", "pl").get(plNum - 5).getNextSibling().asText();

        //制片国家/地区 doubanMoviePlace
        String doubanMoviePlace = infoDiv.getElementsByAttribute("span", "class", "pl").get(plNum - 6).getNextSibling().asText();


        //片长 doubanMovieTime
        String doubanMovieTime = getNumberFromString(infoDiv.getElementsByAttribute("span", "property", "v:runtime").get(0).getAttribute("content"));

        //别名 doubanMovieOtherName
        String doubanMovieOtherName = infoDiv.getElementsByAttribute("span", "class", "pl").get(plNum - 2).getNextSibling().asText();



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

}
