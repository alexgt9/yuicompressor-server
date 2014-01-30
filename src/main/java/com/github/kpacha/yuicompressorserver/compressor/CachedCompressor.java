package com.github.kpacha.yuicompressorserver.compressor;

import java.io.IOException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.mozilla.javascript.EvaluatorException;

import com.github.kpacha.yuicompressorserver.adapter.UnknownContentTypeException;
import com.github.kpacha.yuicompressorserver.reporter.Reporter;
import com.github.kpacha.yuicompressorserver.utils.BufferedContentHasher;

/**
 * In-memory cache for compression requests
 * 
 * @author kpacha
 */
public class CachedCompressor extends Compressor {

    private Compressor actualCompressor;
    private Cache cache;
    private BufferedContentHasher hasher;

    /**
     * The default constructor
     * 
     * @param actualCompressor
     * @param bufferedContentHasher
     * @param cache
     */
    public CachedCompressor(Compressor actualCompressor,
	    BufferedContentHasher bufferedContentHasher, Cache cache) {
	this.actualCompressor = actualCompressor;
	this.cache = cache;
	hasher = bufferedContentHasher;
    }

    /**
     * If the request is cached get its value. If it's not cached, delegate the
     * compression and cache the result. Finally, write the compressed response
     * into the PrintWriter.
     */
    public String compress(String contentType, String charset, String in,
	    Reporter reporter) throws EvaluatorException, IOException,
	    UnknownContentTypeException {
	String hash = hasher.getHash(in, charset);
	Element element = cache.get(hash);
	if (element == null) {
	    cache.put(new Element(hash, actualCompressor.compress(contentType,
		    charset, in, reporter)));
	    element = cache.get(hash);
	}
	return (String) element.getObjectValue();
    }
}
