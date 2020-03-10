package me.mrmaurice.wc.function;

import static me.mrmaurice.wc.WikiaCrawler.print;
import static me.mrmaurice.wc.WikiaCrawler.readUrl;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.mrmaurice.wc.ApiFunction;
import me.mrmaurice.wc.WikiaCrawler;
import me.mrmaurice.wc.function.export.ExportPages;

public class ListPages implements ApiFunction {

	private String wiki;
	private String next;
	private String fullUrl;
	
	public ListPages(String wiki, String next) {
		this.wiki = wiki;
		this.next = next;
		this.fullUrl = wiki + getAPI();
	}
	
	public void start() {
		
		List<String> futures = downloadFromGallery();
		Collections.sort(futures);
		print("Found " + futures.size() + " pages on " + wiki);
		print("What you want to do:");
		print("a) Print all pages");
		print("b) Write all pages to a file");
		print("c) Export all pages");
		String option = WikiaCrawler.scan.next();
		
		switch (option.toLowerCase()) {
			case "a":
			case "1":
				print("You selected: Print all pages.");
				futures.forEach(WikiaCrawler::print);
				break;
			case "b":
			case "2":
				print("You selected: Write to a file.");
				File root = new File(".");
				root = new File(root, "PageList.txt");
				writeFile(root, futures);
				print("Saved on: " + root.toPath());
				break;
			case "c":
			case "3":
				print("You selected: Export all pages.");
				new ExportPages(wiki, futures).start();
				break;
			default:
				print("No option selected.");
				break;
		}
		
	}
	
	private List<String> downloadFromGallery() {
		JsonObject json = readUrl(fullUrl);

		List<String> futures = new ArrayList<>();
		if (json == null) {
			print("Cant connect to the wiki " + wiki);
			return futures;
		}

		if (json.has("query-continue")) {
			String next = json.getAsJsonObject("query-continue").getAsJsonObject("allpages").get("apfrom").getAsString();
			ListPages other = new ListPages(wiki, next);
			futures.addAll(other.downloadFromGallery());
		}

		if (!json.has("query"))
			return futures;

		JsonArray arr = json.getAsJsonObject("query").getAsJsonArray("allpages");

		for (JsonElement obj : arr) 
			futures.add(obj.getAsJsonObject().get("title").getAsString());

		return futures;
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getAPI() {
		String api = "api.php?action=query&list=allpages&aplimit=max&format=json";
		if(!next.isEmpty())
			api += "&apfrom={0}";
		return MessageFormat.format(api, URLEncoder.encode(next));
	}
	
	private void writeFile(File file, List<String> toWrite) {
		try {
			Files.write(file.toPath(), toWrite, StandardCharsets.UTF_8);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
