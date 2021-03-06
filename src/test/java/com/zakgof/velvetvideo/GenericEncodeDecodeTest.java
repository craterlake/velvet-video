package com.zakgof.velvetvideo;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;

public class GenericEncodeDecodeTest extends VelvetVideoTest {

    private static final int FRAMES = 16;

    protected void codeclist(Collection<String> expectedCodecs, MediaType mediaType) {
        List<String> codecs = lib.codecs(Direction.Encode, mediaType);
        System.err.println(codecs);
        Set<String> codecSet = new HashSet<>(expectedCodecs);
        codecSet.removeAll(codecs);
        Assertions.assertTrue(codecSet.isEmpty(), "Missing codecs: " + codecSet);
    }

    protected void formatlist(Collection<String> expectedFormats, Direction dir) {
        List<String> formats = lib.formats(dir);
        System.err.println(formats);
        Set<String> formatSet = new HashSet<>(expectedFormats);
        formatSet.removeAll(formats);
        Assertions.assertTrue(formatSet.isEmpty(), "Missing formats: " + formatSet);
    }

    protected void encodeDecodeCompare(String codec, String format) throws IOException {

        File file = dir.resolve(codec + "." + format).toFile();
        System.err.println(file);

        BufferedImage[] orig = createSingleStreamVideo(codec, format, file, FRAMES);

        double dff = diff(orig[0], orig[1]);
        System.err.println("[0] to [1] " + dff);
        try (IDemuxer demuxer = lib.demuxer(file)) {
            int i = 0;
			for (IVideoFrame frame : demuxer.videoStream(0)) {
				Assertions.assertTrue(i < FRAMES);
				BufferedImage imgrestored = frame.image();
				double diff = diff(orig[i], imgrestored);
				System.err.println("Diff for frame " + i + " = " + diff);
				Assertions.assertEquals(0, diff, 20.0, "Frame " + i + " differs by " + diff);
				i++;
			}
			Assertions.assertEquals(FRAMES, i);
        }
    }


	public void testEncodeDecodeTwoStreams() throws IOException {

		File file = dir.resolve("two.mp4").toFile();
		BufferedImage[][] origs = createTwoStreamVideo(file, FRAMES, "mpeg4", "mp4");

		int colorindex = 0;
		int bwindex = 0;

		try (IDemuxer demuxer = lib.demuxer(file)) {
			for (IDecodedPacket<?> packet : demuxer) {
				if (packet.asVideo().stream().name().equals("color")) {
					BufferedImage imgrestored = packet.asVideo().image();
					double diff = diff(origs[0][colorindex], imgrestored);
					Assertions.assertEquals(0, diff, 10.0, "Color frame " + colorindex + " differs by " + diff);
					colorindex++;
				}
				if (packet.asVideo().stream().name().equals("bw")) {
					BufferedImage imgrestored = packet.asVideo().image();
					double diff = diff(origs[1][bwindex], imgrestored);
					Assertions.assertEquals(0, diff, 10.0, "BW rame " + bwindex + " differs by " + diff);
					bwindex++;
				}
			}
		}
		Assertions.assertEquals(FRAMES, colorindex);
		Assertions.assertEquals(FRAMES, bwindex);
	}







}
