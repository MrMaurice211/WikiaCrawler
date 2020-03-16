package me.mrmaurice.wc.function.export;

import java.io.File;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.mrmaurice.wc.ApiFunction;
import me.mrmaurice.wc.objects.WikiaImage;

import static me.mrmaurice.wc.WikiaCrawler.print;
import static me.mrmaurice.wc.WikiaCrawler.readUrl;

public class ExportImages implements ApiFunction {
	
	private String wiki;
	private String next;
	private String fullUrl;
	private File root;
	
	public ExportImages(String wiki, String next) {
		this.wiki = wiki;
		this.next = next;
		this.fullUrl = wiki + getAPI();
	}
	
	public void start() {
		
		root = new File(".");
		root = new File(root, "AllImages");

		List<CompletableFuture<Void>> futures = downloadFromGallery();

		CompletableFuture<Void> future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
		future.exceptionally(e -> {
			e.printStackTrace();
			return null;
		});
		print("Downloading " + futures.size() + " files on " + root.toPath());
		while (future != null && !future.isDone())
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		print("Exported to: " + root.toPath().toAbsolutePath());
	}
	
	private List<CompletableFuture<Void>> downloadFromGallery() {
		JsonObject json = readUrl(fullUrl);

		List<CompletableFuture<Void>> futures = new ArrayList<>();
		if (json == null) {
			print("Cant connect to the wiki " + wiki);
			return futures;
		}

		if (json.has("query-continue")) {
			String next = json.getAsJsonObject("query-continue").getAsJsonObject("allimages").get("aifrom").getAsString();
			ExportImages other = new ExportImages(wiki, next);
			other.root = root;
			futures.addAll(other.downloadFromGallery());
		}

		if (!json.has("query"))
			return futures;

		JsonArray arr = json.getAsJsonObject("query").getAsJsonArray("allimages");

		List<WikiaImage> imgs = new ArrayList<>();
		for (JsonElement obj : arr) {
			String imgSrc = obj.getAsJsonObject().get("url").getAsString();
			String name = obj.getAsJsonObject().get("name").getAsString();
			imgs.add(new WikiaImage(imgSrc, name, root));

		}

		futures.addAll(imgs.stream().map(WikiaImage::toFuture).collect(Collectors.toList()));

		return futures;
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getAPI() {
		String api = "api.php?action=query&list=allimages&ailimit=max&format=json&aifrom={0}";
		return MessageFormat.format(api, URLEncoder.encode(next));
	}

}
