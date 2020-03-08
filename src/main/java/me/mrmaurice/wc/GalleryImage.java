package me.mrmaurice.wc;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import javax.imageio.ImageIO;

import static me.mrmaurice.wc.WikiaCrawler.print;

public class GalleryImage {

	private String parentPage;
	private String url;
	private String name;
	private String extension;
	private File target;

	public GalleryImage(String parentPage, String rawUrl, String dataKey, String category, File rootFolder) {
		this.parentPage = parentPage;
		this.name = dataKey.replace("amp%3B", "");

		int urlIndex = rawUrl.indexOf(name);
		this.url = urlIndex == -1 ? rawUrl : rawUrl.substring(0, urlIndex + name.length());
		
		int index = name.lastIndexOf('.');
		this.extension = index == -1 ? "" : name.substring(index + 1);

		target = rootFolder;
		if (category != null)
			target = new File(target, category.replaceAll("[\\\\/:*?\"<>|]", ""));
		if (!target.exists())
			target.mkdirs();
		target = new File(target, name);
	}

	public CompletableFuture<Void> toFuture() {
		return CompletableFuture.runAsync(() -> {
			if (target == null)
				return;
			try {
				BufferedImage image = ImageIO.read(new URL(url));

				if (image == null) {
					print("No image file was found at " + url + " at " + parentPage);
					return;
				}

				ImageIO.write(image, extension.isEmpty() ? "png" : extension, target);
			} catch (Exception e) {
				print("Error saving " + name + " from " + parentPage);
				e.printStackTrace();
			}
		});
	}

}
