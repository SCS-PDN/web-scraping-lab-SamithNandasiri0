import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/ScrapeServlet")
public class ScrapServelet extends HttpServlet {

    // NOTE: typo in class name, should be "ScrapeServlet" probably, but fixing it might break things

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Grab the target URL from the request. Might need to sanitize this later.
        String incomingUrl = request.getParameter("url");

        // User might request one or more scraping types
        String[] selectedOptions = request.getParameterValues("options");

        List<String[]> collectedInfo = new ArrayList<>();
        Document webPage;

        try {
            webPage = Jsoup.connect(incomingUrl).get();  // This might throw — ideally should timeout after a few secs
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Couldn't fetch the webpage. Please check the URL.");
            return;
        }

        if (selectedOptions != null) {
            // Iterate over the types of data the user wants
            for (String feature : selectedOptions) {
                switch (feature) {
                    case "title":
                        // Add page title to the results
                        collectedInfo.add(new String[]{"Title", webPage.title()});
                        break;

                    case "links":
                        Elements allLinks = webPage.select("a[href]");
                        for (Element link : allLinks) {
                            // TODO: consider skipping mailto or javascript links later
                            String href = link.attr("abs:href");
                            collectedInfo.add(new String[]{"Link", href});
                        }
                        break;

                    case "images":
                        Elements imgTags = webPage.select("img");
                        for (Element img : imgTags) {
                            String src = img.attr("src");  // May be relative
                            collectedInfo.add(new String[]{"Image", src});
                        }
                        break;

                    default:
                        // Unrecognized option? Log it maybe? Not sure yet.
                        break;
                }
            }
        }

        // User visit tracking — useful for debugging or fun metrics
        HttpSession userSession = request.getSession();
        Integer visitCount = (Integer) userSession.getAttribute("visitCount");
        if (visitCount == null) {
            visitCount = 0;  // First visit
        }
        visitCount++;  // Count this one
        userSession.setAttribute("visitCount", visitCount);

        // Time to respond with HTML
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<html><head><title>Scrape Results</title></head><body>");
        out.println("<h2>Scraped Data</h2>");
        out.println("<p>You have visited this page " + visitCount + " times.</p>");

        if (collectedInfo.isEmpty()) {
            out.println("<p>No data found based on selected options.</p>");
        } else {
            out.println("<table border='1' cellpadding='5'><tr><th>Type</th><th>Data</th></tr>");
            for (String[] entry : collectedInfo) {
                out.println("<tr><td>" + entry[0] + "</td><td>" + entry[1] + "</td></tr>");
            }
            out.println("</table>");
        }

        // Button for CSV download — form submits to another servlet
        out.println("<br/><form method='post' action='DownloadCSVServlet'>");
        out.println("<input type='submit' value='Download as CSV'>");
        out.println("</form>");

        // Save data into the session for CSV download
        userSession.setAttribute("csvData", collectedInfo);

        // Also dump JSON version of scraped data — mostly for testing/debugging or API
        Gson gson = new Gson();
        String jsonDump = gson.toJson(collectedInfo);
        out.println("<h3>Raw JSON Output</h3>");
        out.println("<pre>" + jsonDump + "</pre>");

        out.println("</body></html>");
    }
}
