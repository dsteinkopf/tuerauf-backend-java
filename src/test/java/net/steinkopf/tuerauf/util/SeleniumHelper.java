package net.steinkopf.tuerauf.util;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Helper Methods for Selenium tests.
 */
public class SeleniumHelper {

    private static final Logger logger = Logger.getLogger(SeleniumHelper.class);

    private static final int DELAY = 500;
    private static final int MAX_WAIT_TIME = 30000;
    private static final String ANCHOR = "a";
    private static final long DEFAULT_PAGE_LOAD_WAIT = 10;


    public static WebElement getParentElement(final WebElement webElement){
        return webElement.findElement(By.xpath(".."));
    }

    public static WebElement findFirstChildElementOfType(final WebElement webElement, final String elementType){
        final List<WebElement> allFormChildElements = webElement.findElements(By.xpath("*"));

        for(final WebElement item : allFormChildElements){
            if(item.getTagName().equals(elementType)){
                return item;
            }
        }

        for(final WebElement item : allFormChildElements){
            final WebElement recursiveItem = findFirstChildElementOfType(item, elementType);
            if(recursiveItem!=null){
                return recursiveItem;
            }
        }
        return null;
    }

    public static WebElement findFirstChildAnchorElement(final WebElement webElement, final boolean nonJSAnchorsOnly){
        final List<WebElement> allFormChildElements = webElement.findElements(By.xpath("*"));

        for(final WebElement item : allFormChildElements){
            if(item.getTagName().equals(ANCHOR)){
                if(nonJSAnchorsOnly && isNonJSAnchor(item)){
                    return item;
                }else if(!nonJSAnchorsOnly){
                    return item;
                }
            }
        }

        for(final WebElement item : allFormChildElements){
            final WebElement recursiveItem = findFirstChildAnchorElement(item, nonJSAnchorsOnly);
            if(recursiveItem!=null){
                return recursiveItem;
            }
        }
        return null;
    }

    public static boolean isNonJSAnchor(final WebElement webElement){

        if(webElement.getTagName().equals(ANCHOR)){
            if(webElement.toString().contains("href")){
                return (webElement.toString().contains("href") &&
                        !webElement.toString().contains("href=\"\"") &&
                        !webElement.toString().contains("href=\"#\""));
            }else{
                return !webElement.getAttribute("href").equals("");
            }
        }
        return false;
    }

    public static void clickOnElementWithId(final WebDriver webDriver, final String elementId){
        webDriver.findElement(By.id(elementId)).click();
    }

    public static boolean waitForComponentWithId(final WebDriver webDriver, final String componentId){
        try{
            final WebDriverWait wait = new WebDriverWait(webDriver, DEFAULT_PAGE_LOAD_WAIT);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(componentId)));
            return true;
        }catch ( final TimeoutException te ){
            logger.error(te);
            return false;
        }
    }

    public static boolean waitForComponentWithClassName(final WebDriver webDriver, final String className){
        try{
            final WebDriverWait wait = new WebDriverWait(webDriver, DEFAULT_PAGE_LOAD_WAIT);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(className)));
            return true;
        }catch ( final TimeoutException te ){
            logger.error(te);
            return false;
        }
    }

    public static boolean waitForComponentWithText(final WebDriver webDriver, final String text){
        try{
            final WebDriverWait wait = new WebDriverWait(webDriver, DEFAULT_PAGE_LOAD_WAIT);
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.xpath("//*[contains(text(), '" + text + "')]"), text));
            return true;
        }catch ( final TimeoutException te ){
            logger.error(te);
            return false;
        }
    }

    public static boolean waitForStalenessOfElement(final WebDriver webDriver, final WebElement element) {
        try{
            final WebDriverWait wait = new WebDriverWait(webDriver, DEFAULT_PAGE_LOAD_WAIT);
            wait.until(ExpectedConditions.stalenessOf(element));
            return true;
        } catch (final TimeoutException te) {
            logger.error(te);
            return false;
        }
    }

    public static boolean waitForComponentWithLocator(final WebDriver webDriver, final By locator) {
        try {
            final WebDriverWait wait = new WebDriverWait(webDriver, DEFAULT_PAGE_LOAD_WAIT);
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        }catch ( final TimeoutException te ){
            logger.error(te);
            return false;
        }
    }

    // return true if there are rows in the given table before MAX_WAIT_TIME has passed.
    public static boolean waitForTableRows(final WebDriver webDriver, final String tableId) {
        final WebElement resultTableBody = webDriver.findElement(By.id(tableId));
        boolean found = false;
        int t = 0;

        while(! found && t < MAX_WAIT_TIME) {
            try {
                Thread.sleep(DELAY);
            } catch (final InterruptedException ignore) {
            }
            final List<WebElement> rows = resultTableBody.findElements(By.tagName("tr"));
            found = rows.size() > 0;
            t += DELAY;
        }

        return found;
    }

    public static Document parseXml(final String xml) {
        final SAXReader reader = new SAXReader();
        try {
            return reader.read(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))));
        } catch (final DocumentException | UnsupportedEncodingException ex) {
            logger.error("Error when attempting to parse input data file", ex);
            throw new IllegalStateException(ex);
        }
    }
}


