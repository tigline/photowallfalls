/**
 * 
 */
package com.android.photowallfalls;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

/**
 * @Project PhotoWallFalls	
 * @author houxb
 * @Date 2015-6-24
 */
public class ImageLoader {
	private static LruCache<String, Bitmap> mMemoryCache;
	
	private static ImageLoader mImageLoader;
	private ImageLoader() {
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int cachesize = maxMemory / 8; //设置图片最大缓存为可用内存1/8
		mMemoryCache = new LruCache<String, Bitmap>(cachesize){
			@Override
			protected int sizeOf(String key, Bitmap bitmap){
				return bitmap.getByteCount();
			}
		};
		
	}
	
	/**
	 * 获取实例返回
	 * @return
	 */
	public static ImageLoader getInstance(){
		if (mImageLoader == null) {
			mImageLoader = new ImageLoader();
		}
		return mImageLoader;
	}
	
	/**
	 * 将一张图片保存到lruCache中
	 */
	public void abbBitmapToMemoryCache(String key, Bitmap bitmap){
		if (getBitmapFromMemoryCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}
	
	/***
	 * 从lruCache获取一张图片 如果不存在就返回null
	 */
	public Bitmap getBitmapFromMemoryCache(String key){
		return mMemoryCache.get(key);
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth){
		//源图片宽度
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (width > reqWidth) {
			//计算出实际宽度和目标宽度的比率
			final int widthRatio = Math.round((float)width / (float) reqWidth);
			inSampleSize = widthRatio;
		}
		return inSampleSize;
	}
	
	public static Bitmap decodeSampleBitmapFromResource(String pathName, int reqWidth){
		//第一次解析将inJust 设置为true 来获取图片大小
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);
		//计算inSampleSize值
		options.inSampleSize = calculateInSampleSize(options, reqWidth);
		//使用获取的inSampleSize值再次解析图片
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
