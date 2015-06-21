import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.util.List;

/**
 * 爬58同城
 */
public class crawl58 {
    public static void main(String[] args){
        pa58TageA();
    }
    public static void pa58TageA() {
        try {
            WebClient webClient = new WebClient(BrowserVersion.CHROME);
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getCurrentWindow().setInnerHeight(60000);
            HtmlPage classPage = (HtmlPage) webClient.getPage("http://wh.58.com/danche/?PGTID=14340278354750.16343053430318832&ClickID=1");
            HtmlElement classDiv = (HtmlElement) classPage.getHtmlElementById("infolist");
            List<HtmlElement> classTable = classDiv.getElementsByAttribute("table", "class", "tbimg");
            System.out.println("共有class为tbimg的table数量： " + classTable.size());//1

            List<HtmlElement> trContent = classTable.get(0).getHtmlElementsByTagName("tr");
            System.out.println("共有为摩托车列表的数量： " + trContent.size());//50

            List<HtmlElement> tdContent = trContent.get(0).getHtmlElementsByTagName("td");// 第一个列表
            System.out.println("一个摩托车列表td的数量： " + tdContent.size());//50

            List<HtmlElement> oneListTagA = tdContent.get(0).getHtmlElementsByTagName("a");
            String nextPageUrl = oneListTagA.get(0).getAttribute("href");
            System.out.println("该摩托车列表的链接地址为： " + nextPageUrl);

            String imgUrl = oneListTagA.get(0).getHtmlElementsByTagName("img").get(0).getAttribute("lazy_src");
            System.out.println("该摩托车列表图片链接为：" + imgUrl);

//            oneListTagA.get(0).click();//点击第一个列表的超链接
//            HtmlPage detailPage = (HtmlPage) webClient.getCurrentWindow().getEnclosedPage();
////            System.out.println(detailPage.asText());
//            List<HtmlElement> tagSpanClass = detailPage.getElementById("header").getElementsByTagName("span");
//            String tagSpanName = tagSpanClass.get(1).asText();
//            System.out.println(tagSpanName);


//            System.out.println(classPage.asText());
            webClient.closeAllWindows();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
