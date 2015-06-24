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
		int cachesize = maxMemory / 8; //����ͼƬ��󻺴�Ϊ�����ڴ�1/8
		mMemoryCache = new LruCache<String, Bitmap>(cachesize){
			@Override
			protected int sizeOf(String key, Bitmap bitmap){
				return bitmap.getByteCount();
			}
		};
		
	}
	
	/**
	 * ��ȡʵ������
	 * @return
	 */
	public static ImageLoader getInstance(){
		if (mImageLoader == null) {
			mImageLoader = new ImageLoader();
		}
		return mImageLoader;
	}
	
	/**
	 * ��һ��ͼƬ���浽lruCache��
	 */
	public void abbBitmapToMemoryCache(String key, Bitmap bitmap){
		if (getBitmapFromMemoryCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}
	
	/***
	 * ��lruCache��ȡһ��ͼƬ ��������ھͷ���null
	 */
	public Bitmap getBitmapFromMemoryCache(String key){
		return mMemoryCache.get(key);
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth){
		//ԴͼƬ���
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (width > reqWidth) {
			//�����ʵ�ʿ�Ⱥ�Ŀ���ȵı���
			final int widthRatio = Math.round((float)width / (float) reqWidth);
			inSampleSize = widthRatio;
		}
		return inSampleSize;
	}
	
	public static Bitmap decodeSampleBitmapFromResource(String pathName, int reqWidth){
		//��һ�ν�����inJust ����Ϊtrue ����ȡͼƬ��С
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);
		//����inSampleSizeֵ
		options.inSampleSize = calculateInSampleSize(options, reqWidth);
		//ʹ�û�ȡ��inSampleSizeֵ�ٴν���ͼƬ
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
