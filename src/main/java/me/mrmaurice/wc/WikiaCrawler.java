package me.mrmaurice.wc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WikiaCrawler {

	private File root;

	public static void main(String[] args) throws Exception {

		Scanner scan = new Scanner(System.in);
		print("Paste the url:");
		String start = scan.next();
		print("Paste the folder name:");
		String folder = scan.next();
		scan.close();

		long init = System.currentTimeMillis();
		WikiaCrawler wc = new WikiaCrawler();
		wc.root = new File("C:/Users/MrMaurice211/Desktop");
		wc.root = new File(wc.root, folder);

		List<CompletableFuture<Void>> futures = new ArrayList<>();
		if (!start.endsWith("/"))
			start += "/";
		start += "wiki/Special:Images";
		print("Analyzing wiki at: " + start);
		futures = wc.downloadFromGallery(start);

		CompletableFuture<Void> future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
		future.exceptionally(e -> {
			e.printStackTrace();
			return null;
		});
		print("Downloading " + futures.size() + " files");
		while (future != null && !future.isDone())
			Thread.sleep(500);

		print("Took " + TimeUnit.MILLISECONDS.toSeconds((System.currentTimeMillis() - init)) + "s");
	}

	public List<CompletableFuture<Void>> downloadFromGallery(String url) {
		Document doc = connect(url);

		List<CompletableFuture<Void>> futures = new ArrayList<>();
		if (doc == null) {
			print("Cant connect to the gallery " + url);
			return futures;
		}

		Element nextPage = doc.selectFirst("a.paginator-next.button.secondary");
		if (nextPage != null)
			futures.addAll(downloadFromGallery(nextPage.attr("abs:href")));

		Element src = doc.selectFirst("div#gallery-");
		if (src == null)
			return futures;

		Elements imageList = src.select("div.wikia-gallery-item");

		List<GalleryImage> imgs = new ArrayList<>();
		for (Element galleryElement : imageList) {

			Element image = galleryElement.child(0);
			image = image.selectFirst("img");
			String imgSrc = image.attr("abs:src");
			String name = image.attr("data-image-key").replace(" ", "_");
			
			String category = null;
			Element info = galleryElement.child(1);
			Element post = info.selectFirst("a.wikia-gallery-item-posted");
			if (post != null)
				category = post.attr("title");

			imgs.add(new GalleryImage(url, imgSrc, name, category, root));

		}

		futures.addAll(imgs.stream().map(GalleryImage::toFuture).collect(Collectors.toList()));

		return futures;
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

	private Document connect(String url) {
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

}
