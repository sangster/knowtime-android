package ca.knowtime;


import ca.knowtime.comm.cache.CacheCategory;
import ca.knowtime.comm.cache.CacheElementNotFound;
import ca.knowtime.comm.cache.KnowTimeCache;
import ca.knowtime.comm.cache.keys.CacheKey;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO for dev use only
 */
public class RestCache
        implements KnowTimeCache
{
    private HashMap<CacheCategory, Map<CacheKey, Object>> mCache = new HashMap<CacheCategory, Map<CacheKey, Object>>();
    private HashMap<CacheCategory, Map<CacheKey, String>> mEtags = new HashMap<CacheCategory, Map<CacheKey, String>>();


    @Override
    public String eTag( final CacheKey key ) {
        Map<CacheKey, String> cache = categoryEtags( key.getCategory() );
        if( !cache.containsKey( key ) ) {
            throw new CacheElementNotFound( key );
        }
        return cache.get( key );
    }


    private Map<CacheKey, String> categoryEtags( final CacheCategory category ) {
        if( !mEtags.containsKey( category ) ) {
            mEtags.put( category, new HashMap<CacheKey, String>() );
        }
        return mEtags.get( category );
    }


    @Override
    public <T> T get( final CacheKey key ) {
        Map<CacheKey, Object> cache = categoryCache( key.getCategory() );
        if( !cache.containsKey( key ) ) {
            throw new CacheElementNotFound( key );
        }
        return (T) cache.get( key );
    }


    private Map<CacheKey, Object> categoryCache( final CacheCategory category ) {
        if( !mCache.containsKey( category ) ) {
            mCache.put( category, new HashMap<CacheKey, Object>() );
        }
        return mCache.get( category );
    }


    @Override
    public void put( final CacheKey key, final Object data, final String eTag ) {
        categoryCache( key.getCategory() ).put( key, data );
        categoryEtags( key.getCategory() ).put( key, eTag );
    }


    @Override
    public boolean contains( final CacheKey key ) {
        return categoryCache( key.getCategory() ).containsKey( key );
    }
}
