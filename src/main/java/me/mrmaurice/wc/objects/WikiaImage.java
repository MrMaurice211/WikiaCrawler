package me.mrmaurice.wc.objects;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import javax.imageio.ImageIO;

import static me.mrmaurice.wc.WikiaCrawler.print;

public class WikiaImage {

	private String url;
	private String name;
	private String extension;
	private File target;

	public WikiaImage(String rawUrl, String dataKey, File rootFolder) {
		this.name = dataKey;
		this.url = rawUrl;
		
		int index = name.lastIndexOf('.');
		this.extension = index == -1 ? "" : name.substring(index + 1);

		target = rootFolder;
		if (!target.exists())
			target.mkdirs();
		target = new File(target, name.replaceAll("[\\\\/:*?\"<>|]", ""));
	}

	public CompletableFuture<Void> toFuture() {
		return CompletableFuture.runAsync(() -> {
			if (target == null)
				return;
			try {
				BufferedImage image = ImageIO.read(new URL(url));

				if (image == null) {
					print("No image file was found at " + url);
					return;
				}

				ImageIO.write(image, extension.isEmpty() ? "png" : extension, target);
			} catch (Exception e) {
				print("Error saving " + name + " from " + url);
			}
		});
	}

}
