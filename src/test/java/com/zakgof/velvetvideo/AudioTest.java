package com.zakgof.velvetvideo;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.zakgof.velvetvideo.impl.VelvetVideoLib;

public class AudioTest extends VelvetVideoTest {

    @ParameterizedTest
    @CsvSource({
    	 "ac3,             ac3",
    	 "matroska,        ac3",
    	 "mp4,             ac3",
         "ogg,            libvorbis",
         "matroska,       libvorbis",
         "mp4,            libvorbis",
         "webm,           libvorbis",
//         "ogg,            vorbis",    // TODO Bad sound
//         "webm,           vorbis",    // TODO Bad sound
//         "ogg,            opus",      // TODO Bad sound
//         "mp4,            opus",      // TODO Bad sound
         "matroska,       aac",       // Warning: large mismatch
         "mp4,            aac",
         "flac,           flac",
         "mp4,            flac",
         "matroska,       flac",
         "mp3,            libmp3lame",
         "matroska,       libmp3lame",
         "ogg,            libopus",
         "matroska,       libopus",
         "webm,           libopus",
         "mp4,            libopus",
    })
	public void testAudioRecodeTest(String format, String codec) throws Exception {
    //	File src = local("http://www.ee.columbia.edu/~dpwe/sounds/musp/msmn1.wav", "msmn1.wav");
    //	File src = local("https://www2.cs.uic.edu/~i101/SoundFiles/CantinaBand3.wav", "cantina.wav");
    	File src = local("https://www.kozco.com/tech/piano2.wav", "piano2.wav");

	    int duration = 6306;
	    // int duration = 1675;
		File dest = file("recode-"+ codec +"." + format);
		System.err.println(dest);

		byte[] bufferorig = readAudio(src, duration);
		IVelvetVideoLib lib = new VelvetVideoLib();
		AudioFormat audioFormat = new AudioFormat(Encoding.PCM_SIGNED, 48000, 16, 2, 4, 48000, false);
		try (IMuxer muxer = lib.muxer(format).audioEncoder(lib.audioEncoder(codec, audioFormat).enableExperimental()).build(dest)) {
			IEncoderAudioStream encoder = muxer.audioEncoder(0);
			for (int offset = 0; offset < bufferorig.length; offset+=encoder.frameBytes()) {
				encoder.encode(bufferorig, offset);
			}
		}
		byte[] bufferrecoded = readAudio(dest, duration);
		assertAudioEqual(audioFormat, bufferorig, bufferrecoded);

	}




}
