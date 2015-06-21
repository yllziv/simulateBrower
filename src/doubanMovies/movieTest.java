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
 * ͨ�������Ӱ����ȱ�ǩ������Ӱ��Ϣ
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
     *                                            id(serial���͡���������)
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
        // ID �Ե�
        String doubanMovieID = getNumberFromString(movieURL);
        //���� �Ե�
        HtmlElement classDiv = (HtmlElement) moviePage.getHtmlElementById("content");
        String doubanMovieName = classDiv.getElementsByAttribute("span","property","v:itemreviewed").get(0).asText();

        HtmlElement infoDiv = (HtmlElement) moviePage.getHtmlElementById("info");


        String infoDivTest = infoDiv.asText();
        System.out.println(infoDivTest);


        //��Ա
        String doubanMovieActor = "";
        if(infoDivTest.indexOf("����") != -1){
            doubanMovieActor = infoDiv.getElementsByAttribute("span", "class", "actor").get(0).asText();
            doubanMovieActor = doubanMovieActor.substring(3, doubanMovieActor.length());
        }else {
            doubanMovieActor = "NULL";
        }

        System.out.println(doubanMovieActor);


        int plNum = infoDiv.getElementsByAttribute("span","class","pl").size();

        //����doubanMovieLanguage
        String doubanMovieLanguage = "";
        if(infoDivTest.indexOf("����") != -1){
            int doubanMovieLanguageIndex = infoDivTest.indexOf("����");
            int doubanMovieLanguageEndIndex = infoDivTest.substring(doubanMovieLanguageIndex,infoDivTest.length()).indexOf("\n");
            doubanMovieLanguage = infoDivTest.substring(doubanMovieLanguageIndex+3 , doubanMovieLanguageIndex+doubanMovieLanguageEndIndex);
        }else {
            doubanMovieLanguage = "NULL";
        }

        System.out.println("����" + doubanMovieLanguage);

//        String doubanMovieLanguage = infoDiv.getElementsByAttribute("span", "class", "pl").get(plNum - 5).getNextSibling().asText();

        //��Ƭ����/���� doubanMoviePlace
        String doubanMoviePlace = infoDiv.getElementsByAttribute("span", "class", "pl").get(plNum - 6).getNextSibling().asText();


        //Ƭ�� doubanMovieTime
        String doubanMovieTime = getNumberFromString(infoDiv.getElementsByAttribute("span", "property", "v:runtime").get(0).getAttribute("content"));

        //���� doubanMovieOtherName
        String doubanMovieOtherName = infoDiv.getElementsByAttribute("span", "class", "pl").get(plNum - 2).getNextSibling().asText();



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

}
