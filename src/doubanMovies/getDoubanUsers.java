package doubanMovies;

import java.io.IOException;
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
 * 通过豆瓣用户组名称，爬豆瓣用户的电影信息。一直被封，还有待改进。
 *****************************************/
public class getDoubanUsers {

    public static void main(String[] args) throws Exception {
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit")
                .setLevel(Level.OFF);

        java.util.logging.Logger.getLogger("org.apache.commons.httpclient")
                .setLevel(Level.OFF);

        testFunction();
    }

    public static void testFunction() throws SQLException {
//        pushGroupURLToDB();//豆瓣小组的名称保存到数据库中
        crawlUserName();
    }

    /***************************************
     * 豆瓣小组的名称保存到数据库中
     *****************************************/
    public static void pushGroupURLToDB() throws SQLException {
        String[] doubanGroupNames = {
                "movie","Gia-club","JD","webwebweb","zhuangb","spoil","classicreading","tao6","moonpie","yly","idealife","Junko","lifeway","trip","phy520","youzhaopin","alone-","girlsonroad","wohuozhe","qinmen","10036","kaopulove","qiong","beijing","skyfree","128828","travelguide","soku","tcm","75354","camera","hg","yha","the8","taotaopaoxiao","icook","ohmygod","buybook","Yi-club","bedreader","111410","111978","cultwomen","15285","coldknowledges","dayaferday","benothing","KUSOMOVIE","cookbook","youth26","ustv","lvguangsenlin","diulian","douban911","nothingness.","capricorn","tb","lvxing","tomorrow","LifeTips","onlylonely","jiekou","Junko520","tangguohe","rebekah","14771","w-w","shanghai","bra","187330","prettyeyes","pock","cheer","52725","loveyogurt","roujuanjuan","myjob","14185","youpifu","meili","asshole","blabla","13027","Englishtrans","xingzuo","zhenjing","paihangbang","43643","songshuhui","mytaobao","113127","cat","fangzi","dayima","BigBangTheory","zhwikipedia","beijingzufang","41659","wanggou","aimisweet","trijoint","gotohk","just_lomo","douban250","procrastinators","kangxilaile","blackeye","yiqilai","chinaisgreatest","Parfumare","weare80s","loveface","185997","ha-no-point","SG160","pihao","93611","Eason","jianfei","13043","brands","SUNHOPE123","travel","ikeafansclub","67371","yinxing","64992","EmirKusturica","dashan","ai_Junko","summerlover","73987","withoutyou","59054","socreative","shengren","shoulian","mancook","25872","eusuee","68428","Oh-no","114011","verycd","MUJI","no-self-control","DiyGril","19062","haircut","neverinlove","Bcover","annbaby","bj","E-Chatting","psychology-self","classicgoods","part-time","junkosayno","shanghailife","Unsleepless","82716","lj","Decent.","dorama","tbgw","susanmiller","uniqlo","EnglishMajor","wakao","GossipGirl","calico","foreverScorpio","62126","xiaochao","towhands","Brit-Pop","Cult_Movie","macintosh","yanjung","colourlovers","recommend","cancer","65903","universalcrazy","twindiemusic","MobileParty","22220","11512","huzhuyou","wuhan","Stubborn.fool","xianzhongxue","201681","aboutbbc","faduanxin","mm80","xiaoxin","JPpeople","WORD","lingyi","Anti-Parents","insidestory","39458","wgyh","guoer","movie_view","notonly","xmdb","qiliuhai","sodagreen","xinyoulinxi","witchstudio","DIYFUN","deserts","40831","ka-tvb","litterature","horrormovies","11518","lovelydog","citypictorial","animated","shoe","16092","love-distance","o0","lei","art","lala","57157","bjbookstore","31029","HayaoMiyazaki","10481","justmyself","english","faye","economics","postcard","hangzhou","mian","cd","aletter","philosophy","luoshui","turedream","masterpiece","18297","10197","Leo","datou","MedicatedDiet","onepiece","ynlj","35312","quriben","nomusiciwilldie","TGYGXZ","thebigbang","bluebug","dushu","Hexy","aries","xiandouban","182822","qicai","NO1tuan","psy-sci","BM","11595","syz","ATSteam","prettygirl","Sharing","yaoqing","chocolate","fitness","ADbar","Taurus","cdch","124632","59240","43332","b-things","lovefilm","the80S","10178","lipbalm","gz","ourcapricorn","197226","56107","sfw","12658","241320","professionlife","174966","emotional","supernature","21524","sfforget","formosa","wang2","43017","18697","xyang","movie","Gia-club","the8","webwebweb","weare80s","traveling","idealife","benothing","10084","JD","trijoint","buybook","cheer","justmyself","camera","15285","art","zhwikipedia","moonpie","HelpWenChuan","14966","yanjung","lonelyland","10243","beijing","98004","socreative","score","nz","14859","twindiemusic","just_lomo","trip","fashionwoman","xyang","ustv","13027","13069","annbaby","HayaoMiyazaki","Shanghai","citypictorial","ohmygod","google","17328","yha","lifeway","english","11518","single","19062","writeress","no-self-control","imdb"," hzhouse"," Post-Rock"," 531651"," hrgroup"," szduoduo"," 359985"," 170252"," newyork"," 241691"," 253825"," 369706"," cm180"," 101382"," luluc"," allergy"," Urumqiers"," 501321"," 545251"," jianfeibang"," yinxing"," classicreading"," asoiaf-plot"," EnglishMajor"," Listening"," tg"," english"," E-Chatting"," bedreader"," JLPT."," christian"," douban-E-group"," draw-arch"," IDmag"," angryeditor"," philosophy"," eslpod"," writeress"," philology"," 12658"," SHIREN"," lijiangtour"," chuichui"," yigong"," 148218"," guoer"," beibaoke"," gotohk"," 254667"," travelguide"," trip2"," lasagonglue"," 125945"," 339319"," ynlj"," travel2work"," 306522"," 393140"," 330279"," yly"," gotrip"," DOubanTA"," dota22"," 142641"," dorama"," lonelylife"," gorgorleslie"," memeda"," movie_view"," usdvd"," haha"," pucool"," masa521"," 280261"," midi"," ustv"," ashin"," 297384"," 410326"," 413541"," szyyt"," toufa"," shoulian"," allergy"," zhimei"," 142931"," fangshai"," 130378"," 101382"," 247863"," 204251"," emeonline"," skincare"," loveface"," 253145"," 255664"," 170997"," lenaxin"," zhengrong"," makeup2011"," 208360"," lovelydog"," 84926"," 100009"," NBA"," catcat"," gou"," moeo"," moonpie"," 34020"," mancook"," runners"," swim"," handmade-soap"," 264368"," zhaogongzuo"," senso"," 90882"," SHCat"," 30464"," cat"," 19831"," 236744"," creative"," marketing"," 241691"," yaoqingzhuce"," huaweidevice"," 272098"," ExcelMaster"," python"," dlsp"," ereading"," headphone"," creative2.0"," 458472"," NEX-5C"," 459340"," douban250"," 14185"," blackberry"
        };
        for (int i = 0; i < doubanGroupNames.length; i++) {
            insertGroupNameToDB(doubanGroupNames[i]);
        }
    }

    /***************************************
     * 通过豆瓣小组的名称得到用户名，并将用户名保存到数据库中
     *****************************************/
    public static void crawlUserName(){
        String[] doubanGroupNames = {
                "movie","Gia-club","JD","webwebweb","zhuangb","spoil","classicreading","tao6","moonpie","yly","idealife","Junko","lifeway","trip","phy520","youzhaopin","alone-","girlsonroad","wohuozhe","qinmen","10036","kaopulove","qiong","beijing","skyfree","128828","travelguide","soku","tcm","75354","camera","hg","yha","the8","taotaopaoxiao","icook","ohmygod","buybook","Yi-club","bedreader","111410","111978","cultwomen","15285","coldknowledges","dayaferday","benothing","KUSOMOVIE","cookbook","youth26","ustv","lvguangsenlin","diulian","douban911","nothingness.","capricorn","tb","lvxing","tomorrow","LifeTips","onlylonely","jiekou","Junko520","tangguohe","rebekah","14771","w-w","shanghai","bra","187330","prettyeyes","pock","cheer","52725","loveyogurt","roujuanjuan","myjob","14185","youpifu","meili","asshole","blabla","13027","Englishtrans","xingzuo","zhenjing","paihangbang","43643","songshuhui","mytaobao","113127","cat","fangzi","dayima","BigBangTheory","zhwikipedia","beijingzufang","41659","wanggou","aimisweet","trijoint","gotohk","just_lomo","douban250","procrastinators","kangxilaile","blackeye","yiqilai","chinaisgreatest","Parfumare","weare80s","loveface","185997","ha-no-point","SG160","pihao","93611","Eason","jianfei","13043","brands","SUNHOPE123","travel","ikeafansclub","67371","yinxing","64992","EmirKusturica","dashan","ai_Junko","summerlover","73987","withoutyou","59054","socreative","shengren","shoulian","mancook","25872","eusuee","68428","Oh-no","114011","verycd","MUJI","no-self-control","DiyGril","19062","haircut","neverinlove","Bcover","annbaby","bj","E-Chatting","psychology-self","classicgoods","part-time","junkosayno","shanghailife","Unsleepless","82716","lj","Decent.","dorama","tbgw","susanmiller","uniqlo","EnglishMajor","wakao","GossipGirl","calico","foreverScorpio","62126","xiaochao","towhands","Brit-Pop","Cult_Movie","macintosh","yanjung","colourlovers","recommend","cancer","65903","universalcrazy","twindiemusic","MobileParty","22220","11512","huzhuyou","wuhan","Stubborn.fool","xianzhongxue","201681","aboutbbc","faduanxin","mm80","xiaoxin","JPpeople","WORD","lingyi","Anti-Parents","insidestory","39458","wgyh","guoer","movie_view","notonly","xmdb","qiliuhai","sodagreen","xinyoulinxi","witchstudio","DIYFUN","deserts","40831","ka-tvb","litterature","horrormovies","11518","lovelydog","citypictorial","animated","shoe","16092","love-distance","o0","lei","art","lala","57157","bjbookstore","31029","HayaoMiyazaki","10481","justmyself","english","faye","economics","postcard","hangzhou","mian","cd","aletter","philosophy","luoshui","turedream","masterpiece","18297","10197","Leo","datou","MedicatedDiet","onepiece","ynlj","35312","quriben","nomusiciwilldie","TGYGXZ","thebigbang","bluebug","dushu","Hexy","aries","xiandouban","182822","qicai","NO1tuan","psy-sci","BM","11595","syz","ATSteam","prettygirl","Sharing","yaoqing","chocolate","fitness","ADbar","Taurus","cdch","124632","59240","43332","b-things","lovefilm","the80S","10178","lipbalm","gz","ourcapricorn","197226","56107","sfw","12658","241320","professionlife","174966","emotional","supernature","21524","sfforget","formosa","wang2","43017","18697","xyang","movie","Gia-club","the8","webwebweb","weare80s","traveling","idealife","benothing","10084","JD","trijoint","buybook","cheer","justmyself","camera","15285","art","zhwikipedia","moonpie","HelpWenChuan","14966","yanjung","lonelyland","10243","beijing","98004","socreative","score","nz","14859","twindiemusic","just_lomo","trip","fashionwoman","xyang","ustv","13027","13069","annbaby","HayaoMiyazaki","Shanghai","citypictorial","ohmygod","google","17328","yha","lifeway","english","11518","single","19062","writeress","no-self-control","imdb"," hzhouse"," Post-Rock"," 531651"," hrgroup"," szduoduo"," 359985"," 170252"," newyork"," 241691"," 253825"," 369706"," cm180"," 101382"," luluc"," allergy"," Urumqiers"," 501321"," 545251"," jianfeibang"," yinxing"," classicreading"," asoiaf-plot"," EnglishMajor"," Listening"," tg"," english"," E-Chatting"," bedreader"," JLPT."," christian"," douban-E-group"," draw-arch"," IDmag"," angryeditor"," philosophy"," eslpod"," writeress"," philology"," 12658"," SHIREN"," lijiangtour"," chuichui"," yigong"," 148218"," guoer"," beibaoke"," gotohk"," 254667"," travelguide"," trip2"," lasagonglue"," 125945"," 339319"," ynlj"," travel2work"," 306522"," 393140"," 330279"," yly"," gotrip"," DOubanTA"," dota22"," 142641"," dorama"," lonelylife"," gorgorleslie"," memeda"," movie_view"," usdvd"," haha"," pucool"," masa521"," 280261"," midi"," ustv"," ashin"," 297384"," 410326"," 413541"," szyyt"," toufa"," shoulian"," allergy"," zhimei"," 142931"," fangshai"," 130378"," 101382"," 247863"," 204251"," emeonline"," skincare"," loveface"," 253145"," 255664"," 170997"," lenaxin"," zhengrong"," makeup2011"," 208360"," lovelydog"," 84926"," 100009"," NBA"," catcat"," gou"," moeo"," moonpie"," 34020"," mancook"," runners"," swim"," handmade-soap"," 264368"," zhaogongzuo"," senso"," 90882"," SHCat"," 30464"," cat"," 19831"," 236744"," creative"," marketing"," 241691"," yaoqingzhuce"," huaweidevice"," 272098"," ExcelMaster"," python"," dlsp"," ereading"," headphone"," creative2.0"," 458472"," NEX-5C"," 459340"," douban250"," 14185"," blackberry"
        };
        for (int i = 0; i < doubanGroupNames.length; i++) {
            try {
                System.out.println("start " + String.valueOf(i) + " " + doubanGroupNames[i]);
                WebClient webClient = new WebClient(BrowserVersion.CHROME);
                webClient.getOptions().setJavaScriptEnabled(false);
                webClient.getCurrentWindow().setInnerHeight(60000);
                HtmlPage classPage = (HtmlPage) webClient.getPage("http://www.douban.com/group/"+doubanGroupNames[i]+"/members?start=0");

                HtmlElement classDiv = (HtmlElement) classPage.getHtmlElementById("content");

                //得到总的页面数：sumPageNumber
                HtmlElement pageNavDiv = (HtmlElement) classDiv.getElementsByAttribute("div", "class", "paginator").get(0);
                int sumPageNumber = Integer.parseInt(pageNavDiv.getElementsByAttribute("span", "class", "thispage").get(0).getAttribute("data-total-page"));//总的页面数
                System.out.println(sumPageNumber);

                //循环整个页面：
                for(int k = 0; k < sumPageNumber; k++){
                    HtmlPage classOnePage = (HtmlPage) webClient.getPage("http://www.douban.com/group/"+doubanGroupNames[i]+"/members?start=" + String.valueOf(35*k));
                    HtmlElement classOneDiv = (HtmlElement) classOnePage.getHtmlElementById("content");
                    List<HtmlElement> userNames = classOneDiv.getElementsByAttribute("div", "class", "name");
                    for(int j = 0; j < userNames.size(); j++){
                        insertUserNameToDB(userNames.get(j).getElementsByTagName("a").get(0).getAttribute("href"));//将用户URL插入数据库
                    }
                }

                webClient.closeAllWindows();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
     * 豆瓣小组的名称保存到数据库中
     *
     * @param
     */
    public static boolean insertGroupNameToDB(String doubanGroupName) throws SQLException {

//        System.out.println("*****start to connect ....\n");

        Connection conn = getConn();

//        System.out.println("*****connected success....\n");
        conn.setAutoCommit(false);
        try {
                // 提交product的batch
                String pro_query = "INSERT INTO groupname VALUES ('" + doubanGroupName + "')";
                Statement statemenet = conn.createStatement();
                statemenet.addBatch(pro_query);


                statemenet.executeBatch();
                conn.commit();//提交事务
                System.out.println("....insert data ok!");
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
     * 豆瓣用户的名称保存到数据库中
     *
     * @param
     */
    public static boolean insertUserNameToDB(String doubanUserName) throws SQLException {

//        System.out.println("*****start to connect ....\n");

        Connection conn = getConn();

//        System.out.println("*****connected success....\n");
        conn.setAutoCommit(false);
        try {
            // 提交product的batch
            String pro_query = "INSERT INTO username VALUES ('" + doubanUserName + "')";
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


}
