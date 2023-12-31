package edu.hawaii.ti.iam.groupings.selenium.page;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.closeWebDriver;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.url;
import static edu.hawaii.ti.iam.groupings.selenium.core.Util.encodeUrl;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.hawaii.ti.iam.groupings.selenium.core.Property;
import edu.hawaii.ti.iam.groupings.selenium.core.User;

@SpringBootTest
public class HomePageTest {

    private static final Log logger = LogFactory.getLog(HomePageTest.class);

    private final Property property;

    // Constructor.
    public HomePageTest(@Autowired Property property) {
        this.property = property;
    }

    @AfterAll
    public static void afterAll() {
        closeWebDriver();
    }

    @BeforeEach
    public void setUp() {
        open(property.value("app.url.home"));
    }

    @Test
    public void navBarInfo() {
        final String urlFull = property.value("url.info");
        final String urlRel = property.value("url.relative.info");
        $x("//a[@href='/" + urlRel + "']").click();
        $x("/html/body/main/div[1]/div/div[1]/div[1]/h1").shouldHave(text("What is a UH Grouping?"));
        webdriver().shouldHave(url(urlFull));
    }

    @Test
    public void learnMoreButton() {
        String url = property.value("url.bwiki");
        $x("//a[@role='button'][@href='" + url + "']").click();
        webdriver().shouldHave(url("" + url + ""));
    }

    @Test
    public void navBarLogin() {
        String urlRel = property.value("url.relative.login");
        $x("//a[@href='/" + urlRel + "']").click();
        $("#uh-seal").shouldBe(visible);
        String loginUrl = property.value("cas.login.url");
        String appUrl = property.value("app.url.home");
        String serviceUrl = appUrl + "/login/cas"; // Why the extra ending?
        String expectedUrl = loginUrl + "?service=" + encodeUrl(serviceUrl);
        webdriver().shouldHave(url(expectedUrl));
    }

    @Test
    public void loginHereButton() {
        $x("//button[@class='btn btn-lg dark-teal-bg'][text()='Login Here']").click();
    }

    @Test
    public void groupingsIcon() {
        $x("//img[@alt='UH Groupings Logo']").click();
        webdriver().shouldHave(url(property.value("app.url.home") + "/")); // Inconsistent ending use.
        $x("//p[@class='lead'][text()='Manage your groupings in one place, use them in many.']").should(exist);
    }

    @Test
    public void equalOpportunity() {
        $x("//a[text()='equal opportunity/affirmative action institution']").click();
        webdriver().shouldHave(url(property.value("url.policy")));
    }

    @Test
    public void loggingInWithStudent() {
        User user = new User.Builder()
                .username(property.value("student.user.username"))
                .password(property.value("student.user.password"))
                .firstname(property.value("student.user.firstname"))
                .uhnumber(property.value("student.user.uhnumber"))
                .create();
        assertThat(user.getUsername(), not(equalTo("SET-IN-OVERRIDES")));

        navBarLogin();

        $("#username").val(user.getUsername());
        $("#password").val(user.getPassword());
        $x("//button[@name='submit']").click();

        closeWebDriver();
    }

    @Test
    public void usagePolicyTest() throws Exception {
        File downloadedFile = $("#footer-top > div > div > div.col-md-8.col-sm-7 > p:nth-child(2) > a").download();
        Path path = Paths.get(downloadedFile.getPath());
        InputStream downloadedPDF = Files.newInputStream(path);
        String downloadedHash = DigestUtils.sha1Hex(downloadedPDF);
        InputStream expectedPDF = Files.newInputStream(path);
        String expectedHash = DigestUtils.sha1Hex(expectedPDF);
        assertThat(expectedHash, equalTo(downloadedHash));
    }

}
