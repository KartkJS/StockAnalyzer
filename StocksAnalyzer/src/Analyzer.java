import gvjava.org.json.JSONException;
import gvjava.org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * 
 */

/**
 * @author Kartik
 * 
 */
public class Analyzer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Date d1 = new Date();
		
		// Read stocks file from resources
		readStocks();
		
		System.out.println("Done in : "+ ((new Date()).getTime()-d1.getTime())/1000 + " seconds");
	}

	private static void readStocks() {
		// Open the file
		FileInputStream fstream;
		try {
			fstream = new FileInputStream("resources/Stocks.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fstream));
			String stockName;
			
			PrintWriter pw = null;
			try {
				pw = new PrintWriter(new File("resources/Stocks.csv"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			StringBuilder builder = new StringBuilder();
			
			String ColumnNamesList = "Stock Symbol,Current Price,Year Target Price,Year High,Year Low";
			builder.append(ColumnNamesList + "\n");

			System.out.println("Processing Stocks...");
			
			try {
				while ((stockName = br.readLine()) != null) {
					
					processStock(stockName, builder);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.out.println("Writing Stock Info...");
			
			pw.write(builder.toString());
			pw.close();
			
			// Close the input stream
			br.close();

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void processStock(String stockName, StringBuilder builder) {
		try {

			JSONObject json = readJsonFromUrl("https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(%22"+stockName+"%22)&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys");
			convertJsonToJava(json, builder);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private static StringBuilder convertJsonToJava(JSONObject json, StringBuilder builder) {
		try { 
			
			StockInfo stockInfo = new StockInfo();
			stockInfo.setSymbol(json.get("symbol").toString());
			stockInfo.setYearHigh(json.get("YearHigh")!=null?json.get("YearHigh").toString():"-1");
			stockInfo.setYearLow(json.get("YearLow")!=null?json.get("YearLow").toString():"-1");
			stockInfo.setAsk(json.get("Ask")!=null?json.get("Ask").toString():"-1");
			stockInfo.setEPSEstimateCurrentYear(json.get("EPSEstimateCurrentYear")!=null?json.get("EPSEstimateCurrentYear").toString():"-1");
			
			writeObjectToOutput(stockInfo, builder);
			} 
		catch (JSONException e) {
			e.printStackTrace();
		}
		return builder;
	}

	private static void writeObjectToOutput(StockInfo stockInfo, StringBuilder builder) {
		
		builder.append(stockInfo.getSymbol() + ",");
		builder.append(stockInfo.getAsk() + ",");
		builder.append(stockInfo.getEPSEstimateCurrentYear() + ",");
		builder.append(stockInfo.getYearHigh() + ",");
		builder.append(stockInfo.getYearLow());
		builder.append('\n');
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException,
			JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);

			JSONObject query = (JSONObject) json.get("query");
			JSONObject results = (JSONObject) query.get("results");
			JSONObject quote = (JSONObject) results.get("quote");

			return quote;
		} finally {
			is.close();
		}
	}
}
