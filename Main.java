import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {
    public static void main(String[] args) {
        // URLs of the webpages to scrape
        String[] urls = {
            "https://www.walmart.com/reviews/product/1567982570",
            "https://www.walmart.com/reviews/product/362570537"
        };

        // Prompt the user to input their preferred words
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your preferred words (separated by commas): ");
        String preferredWordsInput = scanner.nextLine().trim();
        String[] preferredWords = preferredWordsInput.split(",");

        try {
            // Loop through each URL
            for (String url : urls) {
                scrapeReviews(url, preferredWords);
            }
            // Create advertisement CSV for good reviews
            writeAdvertisement("good", "Happy Shopper");
            // Create advertisement CSV for bad reviews
            System.out.println("Successfully scraped and saved reviews to respective CSV files");
        } catch (IOException e) {
            // Print error message if unable to connect to a website
            System.out.println("Error: Unable to connect to a website or write to file.");
            System.exit(1);
        } finally {
            scanner.close();
        }
    }

    // Method to scrape reviews from a given URL based on user's preferred words
    public static void scrapeReviews(String url, String[] preferredWords) throws IOException {
        Document doc = Jsoup.connect(url).get();

        // FileWriter objects to write scraped data to CSV files
        FileWriter scrapedFileWriter = new FileWriter("scraped_" + url.hashCode() + ".csv");
        FileWriter targetWordsFileWriter = new FileWriter("targetWords_" + url.hashCode() + ".csv");

        // Writing headers to all files
        scrapedFileWriter.write("Reviewer Name, Review Text, Rating, Verified, Date, Helpful Votes, Unhelpful Votes, Review Length, Contains Images, Contains Video\n");
        targetWordsFileWriter.write("Reviewer Name, Review Text, Rating, Verified, Date, Helpful Votes, Unhelpful Votes, Review Length, Contains Images, Contains Video\n");

        // Select all review elements from the webpage
        Elements reviews = doc.select("div.w_DHV_.pv3.mv0");

        // Loop through each review element
        for (Element review : reviews) {
            String reviewerName = review.select("div.f6.gray.pr2.mb2").text();
            String reviewText = review.select("span.tl-m.mb3.db-m").text();
            String reviewerRating = review.select("div.flex-grow-1").text();
            String reviewerDate = review.select("div.f7.gray.mt1").text();
            boolean verified = review.select("div.verified-buyer").size() > 0;
            String verifiedStatus = verified ? "Verified" : "Not Verified";
            String helpfulVotes = review.select("div.customer-review-action-footer").select("button.js-vote-helpful").text();
            String unhelpfulVotes = review.select("div.customer-review-action-footer").select("button.js-vote-unhelpful").text();
            int reviewLength = reviewText.split("\\s+").length;
            String containsImages = review.select("img").isEmpty() ? "No Image" : "Has Image";
            String containsVideo = review.select("video").isEmpty() ? "No Video" : "Has Video";

            // Check if the review contains any of the user's preferred words
            boolean containsPreferredWord = false;
            for (String word : preferredWords) {
                if (reviewText.toLowerCase().contains(word.trim().toLowerCase())) {
                    containsPreferredWord = true;
                    break;
                }
            }

            // Writing to targetWords.csv if the review contains any of the preferred words
            if (containsPreferredWord) {
                targetWordsFileWriter.write("\"" + reviewerName + "\", \"" + reviewText + "\", \"" + reviewerRating + "\", \"" + verifiedStatus + "\", \"" + reviewerDate + "\", \"" + helpfulVotes + "\", \"" + unhelpfulVotes + "\", \"" + reviewLength + "\", \"" + containsImages + "\", \"" + containsVideo + "\"\n");
            }

            // Writing to scraped.csv
            scrapedFileWriter.write("\"" + reviewerName + "\", \"" + reviewText + "\", \"" + reviewerRating + "\", \"" + verifiedStatus + "\", \"" + reviewerDate + "\", \"" + helpfulVotes + "\", \"" + unhelpfulVotes + "\", \"" + reviewLength + "\", \"" + containsImages + "\", \"" + containsVideo + "\"\n");

        }

        // Closing FileWriter objects
        targetWordsFileWriter.close();
        scrapedFileWriter.close();
    }

    // Modified method to write advertisement to a CSV file based on review type and reviewer name
    public static void writeAdvertisement(String reviewType, String reviewerName) throws IOException {
        FileWriter advertisementFileWriter = new FileWriter("advertisement_" + reviewType + ".csv");

        // Writing header to advertisement CSV file
        advertisementFileWriter.write("Advertisement\n");

        // Writing advertisement data based on review type and reviewer name
        if (reviewType.equals("good")) {
            String advertisement = reviewerName + ", Great deals on top-rated products! Explore our best-sellers now.";
            advertisementFileWriter.write("\"" + advertisement + "\"\n");
        } else {
            System.out.println("Invalid review type.");
        }

        // Closing FileWriter object
        advertisementFileWriter.close();
    }
}
