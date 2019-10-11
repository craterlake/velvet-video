package com.zakgof.velvetvideo.impl.jnr;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.zakgof.velvetvideo.VelvetVideoException;

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.annotations.In;
import jnr.ffi.types.size_t;

public interface LibAVUtil {
	
	static final int AV_DICT_IGNORE_SUFFIX = 2;
	

    static final int ERROR_TEXT_BUFFER_SIZE = 512;

	
	AVFrame av_frame_alloc();

	int av_frame_get_buffer(AVFrame frame, int align);

	void av_frame_free(AVFrame[] frameref);

	int av_image_alloc(Pointer[] pointers, int[] linesizes, int w, int h, AVPixelFormat pix_fmt, int align);

	int av_dict_set(Pointer[] pm, String key, String value, int flags);

	int av_strerror(int errnum, Pointer errbuf, int errbuf_size);

	int av_log_set_level(int val);

	Pointer av_malloc(@size_t int size);

	AVDictionaryEntry av_dict_get(@In Pointer dictionary, @In String key, @In AVDictionaryEntry prev, int flags);

	long av_frame_get_pkt_duration(AVFrame frame);

	// void av_log_set_callback(ILogger logger);

//        interface ILogger {
//        	  // JNR does not support varargs in callbacks
//            @Delegate @StdCall void log(Pointer avcl, int level, String fmt, ??? vars);
//        }

	Pointer av_strdup(String s);

	default Pointer[] createDictionary(Map<String, String> map) {
		Pointer[] opts = new Pointer[1];
		for (Entry<String, String> entry : map.entrySet()) {
			av_dict_set(opts, entry.getKey(), entry.getValue(), 0);
		}
		return opts;
	}
	
	default Map<String, String> dictionaryToMap(Pointer dictionary) {
        Map<String, String> metadata = new LinkedHashMap<>();
        AVDictionaryEntry entry = null;
        do {
            entry = av_dict_get(dictionary, "", entry, AV_DICT_IGNORE_SUFFIX);
            if (entry != null) {
                metadata.put(entry.key.get(), entry.value.get());
            }
        } while (entry != null);
        return metadata;
    }

	default int checkcode(int code) {
		if (code < 0) {
			Pointer ptr = Runtime.getSystemRuntime().getMemoryManager().allocateDirect(ERROR_TEXT_BUFFER_SIZE); // TODO !!!
            av_strerror(code, ptr, ERROR_TEXT_BUFFER_SIZE);
            byte[] bts = new byte[ERROR_TEXT_BUFFER_SIZE];
            ptr.get(0, bts, 0, ERROR_TEXT_BUFFER_SIZE);
            int len = 0;
            for (int i=0; i<ERROR_TEXT_BUFFER_SIZE; i++) if (bts[i] == 0) len = i;
            String s = new String(bts, 0, len);
            throw new VelvetVideoException("FFMPEG error " + code + " : "+ s);
        }
        return code;
	}

}