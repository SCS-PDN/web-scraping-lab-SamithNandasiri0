import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@SuppressWarnings("serial")
@WebServlet("/DownloadCSVServlet")
public class DownloadCSVServlet extends HttpServlet {
protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
HttpSession session = request.getSession();
@SuppressWarnings("unchecked")
List<String[]> data = (List<String[]>) session.getAttribute("csvData");

response.setContentType("text/csv");
response.setHeader("Content-Disposition", "attachment;filename=scraped_data.csv");

PrintWriter out = response.getWriter();
out.println("Type,Data");

for (String[] row : data) {
out.println("\"" + row[0] + "\",\"" + row[1].replace("\"", "\"\"") + "\"");
}
}
}