package me.mrmaurice.wc.function.export;

import static me.mrmaurice.wc.WikiaCrawler.print;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import me.mrmaurice.wc.ApiFunction;

public class ExportPages implements ApiFunction {

	private List<String> pages;
	private String wiki;
	private File root;

	public ExportPages(String wiki, List<String> pages) {
		this.wiki = wiki;
		this.pages = pages;
		root = new File(".");
		root = new File(root, "AllPages");
		if (!root.exists())
			root.mkdirs();
	}

	@Override
	public void start() {

		List<CompletableFuture<Void>> futures = pages.stream().map(this::savePage).collect(Collectors.toList());

		print("Downloading " + futures.size() + " pages  on " + root.toURI());
		CompletableFuture<Void> future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
		future.exceptionally(e -> {
			e.printStackTrace();
			return null;
		});
		while (future != null && !future.isDone())
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

		print("Exported " + pages.size() + " pages to: " + root.toURI());
	}

	@SuppressWarnings("deprecation")
	private CompletableFuture<Void> savePage(String name) {
		return CompletableFuture.runAsync(() -> {
			try {
				URL nurl = new URL(wiki + MessageFormat.format(getAPI(), URLEncoder.encode(name)));
				URLConnection uconn = nurl.openConnection();

				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document doc = builder.parse(uconn.getInputStream());

				TransformerFactory tfactory = TransformerFactory.newInstance();
				Transformer xform = tfactory.newTransformer();

				File file = new File(root, name.replaceAll("[\\\\/:*?\"<>|]", "_") + ".xml");
				FileWriter writer = new FileWriter(file);
				xform.transform(new DOMSource(doc), new StreamResult(writer));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public String getAPI() {
		return "wiki/Special:Export?curonly=1&pages={0}&templates=1&action=submit";
	}
}
