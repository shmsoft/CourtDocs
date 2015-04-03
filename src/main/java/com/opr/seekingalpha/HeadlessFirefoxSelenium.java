package com.opr.seekingalpha;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;


/**
 * Created by mark on 1/2/15.
 */
public class HeadlessFirefoxSelenium {
    static public void main(String[] args) {

    }
    public void test() {
        WebDriver driver = new HtmlUnitDriver();
        // Go to the Google home page
        driver.get("http://www.google.com/");
        // Check the title of the page
        System.out.println("Page title is: " + driver.getTitle());
        driver.quit();
    }
}
