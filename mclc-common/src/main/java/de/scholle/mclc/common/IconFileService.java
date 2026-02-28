package de.scholle.mclc.common;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class IconFileService {

    public File resolveIconFile(File baseDirectory, String configuredPath) {
        File configuredFile = new File(configuredPath);
        if (configuredFile.isAbsolute()) {
            return configuredFile;
        }

        File localFile = new File(baseDirectory, configuredPath);
        if (localFile.exists()) {
            return localFile;
        }

        File rootFile = new File(configuredPath);
        if (rootFile.exists()) {
            return rootFile;
        }

        return localFile;
    }

    public File ensure64x64Png(File sourceFile, File outputDirectory, String outputName) throws IOException {
        BufferedImage sourceImage = ImageIO.read(sourceFile);
        if (sourceImage == null) {
            throw new IOException("Datei ist kein lesbares Bild: " + sourceFile.getPath());
        }

        if (sourceImage.getWidth() == 64 && sourceImage.getHeight() == 64) {
            return sourceFile;
        }

        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            throw new IOException("Ausgabeordner konnte nicht erstellt werden: " + outputDirectory.getPath());
        }

        BufferedImage resizedImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(sourceImage, 0, 0, 64, 64, null);
        graphics.dispose();

        File resizedFile = new File(outputDirectory, outputName);
        ImageIO.write(resizedImage, "png", resizedFile);
        return resizedFile;
    }
}
