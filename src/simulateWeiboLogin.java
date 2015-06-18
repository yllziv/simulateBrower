import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

/**
 * Ä£ÄâÎ¢²©µÇÂ½
 */
public class simulateWeiboLogin {
    public static void main(String[] args){
        loginWeibo();
    }

    public static void loginWeibo() {
        try {
            WebClient webClient = new WebClient(BrowserVersion.CHROME);
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getCurrentWindow().setInnerHeight(60000);
            HtmlPage LoginPage = (HtmlPage) webClient.getPage("https://passport.weibo.cn/signin/login?entry=mweibo&res=wel&wm=3349&r=http%3A%2F%2Fm.weibo.cn%2F");//??¡À??¡Á??

            System.out.println(LoginPage.asText());

            final HtmlTextInput loginName = (HtmlTextInput) LoginPage.getElementById("loginName");
            final HtmlPasswordInput password = (HtmlPasswordInput) LoginPage.getElementById("loginPassword");

            loginName.setValueAttribute("15927408371");
            password.setValueAttribute("13754956538.");

            final HtmlElement button = (HtmlElement) LoginPage.getElementById("loginAction");
            button.click();
            webClient.setJavaScriptTimeout(10 * 1000);
            webClient.waitForBackgroundJavaScript(10 * 1000);

            final HtmlPage reservePage = (HtmlPage) webClient.getCurrentWindow().getEnclosedPage();
            System.out.println(reservePage.asText());

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
