package website_Crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;



public class Crawler {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Fetch website URL from user input
        System.out.print("Enter the URL of the e-commerce website: ");
        String websiteUrl = scanner.nextLine();

        try {
            // Fetch XPaths from user input
            System.out.println("Enter XPaths for scraping:");
            System.out.print("Product title XPath: ");
            String titleXPath = scanner.nextLine();
            System.out.print("Price XPath: ");
            String priceXPath = scanner.nextLine();
            System.out.print("Next Page XPath: ");
            String nextPageXPath = scanner.nextLine();

            // Crawl and scrape the website
            List<Product> products = new ArrayList<>();
            String currentPageUrl = websiteUrl;

            while (currentPageUrl != null) {
                Document doc = Jsoup.connect(currentPageUrl).get();
                saveSourceCode(doc.html(), "website_source.html");
                products.addAll(crawlAndScrape(doc, titleXPath, priceXPath));
                currentPageUrl = getNextPageUrl(doc, nextPageXPath);
            }

            // Print scraped products
            for (Product product : products) {
                System.out.println(product);
            }

            // Prompt user to save data as CSV
            System.out.print("Want to save as CSV (Y/N)? ");
            String saveAsCsv = scanner.nextLine();

            if (saveAsCsv.equalsIgnoreCase("Y")) {
                saveAsCSV(products, "scraped_data.csv");
                System.out.println("Data saved as scraped_data.csv");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveSourceCode(String html, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(html);
            System.out.println("Source code saved to " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Product> crawlAndScrape(Document doc, String titleXPath, String priceXPath) {
        Elements products = doc.select("article.product_pod");
        List<Product> productList = new ArrayList<>();

        for (Element product : products) {
            Product p = new Product();
            p.setTitle(selectText(product, titleXPath));
            p.setPrice(selectText(product, priceXPath));
            productList.add(p);
        }

        return productList;
    }

    public static String selectText(Element element, String xpath) {
        Elements elements = element.select(xpathToCss(xpath));
        return elements.isEmpty() ? "" : elements.first().text();
    }

    public static String xpathToCss(String xpath) {
        // Basic conversion from XPath to CSS selectors
        // This won't handle all XPath cases, but works for basic paths
        return xpath.replaceAll("//", " ")
                    .replaceAll("/", " > ")
                    .replaceAll("\\[@", "[")
                    .replaceAll("\\]", "]")
                    .replaceAll("@", "");
    }

    public static String getNextPageUrl(Document doc, String nextPageXPath) {
        Elements nextPageElements = doc.select(xpathToCss(nextPageXPath));
        if (!nextPageElements.isEmpty()) {
            String nextPageUrl = nextPageElements.first().absUrl("href");
            return nextPageUrl.isEmpty() ? null : nextPageUrl;
        }
        return null;
    }

    public static void saveAsCSV(List<Product> products, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("Title,Price\n");
            for (Product product : products) {
                writer.write(product.getTitle() + "," + product.getPrice() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


