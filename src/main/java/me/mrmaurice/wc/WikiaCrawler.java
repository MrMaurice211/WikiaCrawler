package me.mrmaurice.wc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.mrmaurice.wc.function.ListPages;
import me.mrmaurice.wc.function.export.ExportImages;
import me.mrmaurice.wc.function.imports.ImportPages;

public class WikiaCrawler {
	
	public static Scanner scan;

	public static void main(String[] args) throws Exception {

		scan = new Scanner(System.in);
		print("Select an option:");
		print("a) Image Download");
		print("b) Page List");
		print("c) Import");
		String option = scan.next();
		print("Paste the url:");
		String start = scan.next();
		
		if (!start.endsWith("/"))
			start += "/";
		
		ApiFunction func = null;
		switch (option.toLowerCase()) {
			case "a":
			case "1":
				print("You selected: Image Download.");
				func = new ExportImages(start, "");
				break;
			case "b":
			case "2":
				print("You selected: Page List.");
				func = new ListPages(start, "");
				break;
			case "c":
			case "3":
				print("You selected: Import.");
				func = new ImportPages();
				break;
			default:
				print("No option selected.");
				break;
		}

		long init = System.currentTimeMillis();
		print("Analyzing wiki at: " + start);
		
		if(func != null)
			func.start();

		print("Took " + TimeUnit.MILLISECONDS.toSeconds((System.currentTimeMillis() - init)) + "s");
		scan.close();
	}

	public static void print(Object... list) {
		print(Arrays.asList(list));
	}

	public static void print(Collection<?> list) {
		list.forEach(System.out::println);
	}

	public static void print(Map<?, ?> map) {
		print(map.toString());
	}

	public static Document connect(String url) {
		Document doc = null;
		try {
			doc = Jsoup.connect(url)
					// .proxy("166.249.185.131", 33767)
					.userAgent(
							"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36")
					.ignoreHttpErrors(true).timeout(15000).get();
			return doc;
		} catch (IOException e1) {
			print("Error connecting to " + url);
			e1.printStackTrace();
			return null;
		}
	}
	
	public static JsonObject readUrl(String rawUrl) {
		try {
			URL url = new URL(rawUrl);
			URLConnection con = url.openConnection();
			con.setConnectTimeout(2000);
			con.setReadTimeout(2000);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			JsonObject obj = JsonParser.parseReader(in).getAsJsonObject();
			
			in.close();
			return obj;
		} catch (Exception e) {
			print(rawUrl);
			return null;
		}
	}

}
