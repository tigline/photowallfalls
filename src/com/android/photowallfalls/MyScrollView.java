/**
 * 
 */
package com.android.photowallfalls;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;


/**
 * @Project PhotoWallFalls	
 * @author houxb
 * @Date 2015-6-24
 */
public class MyScrollView extends ScrollView implements OnTouchListener{

	public static final int PAGE_SIZE = 15;
	
	private int page;
	
	private int columnWidth;
	
	private int firstColumnHeight;
	
	private int secondColumnHeight;
	
	private int thirdColumnHeight;
	
	private boolean loadOnce;
	
	private ImageLoader imageLoader;
	
	private LinearLayout firstColumn;
	
	private LinearLayout secondColumn;
	
	private LinearLayout thirdColumn;
	
	private static Set<LoadImageTask> taskCollection;
	
	private static View scrollLayout;
	
	private static int scrollViewHeight;
	
	private static int lastScrollY = 1;
	
	private List<ImageView> imageViewList = new ArrayList<ImageView>();
	
	private static Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg){
			MyScrollView myScrollView = (MyScrollView) msg.obj;
			int scrollY = myScrollView.getScrollY();
			//如果当前滚动位置和上次相同，表示已停止滚动
			if (scrollY == lastScrollY) {
				//当滚动到最底部，并且当前没有正在下载的任务，开始加载下一页的图片
				if (scrollViewHeight + scrollY >= scrollLayout.getHeight()
						&& taskCollection.isEmpty()) {
					myScrollView.loadMoreImages();
				}
				myScrollView.checkVisibility();
			} else {
				lastScrollY = scrollY;
				Message message = new Message();
				message.obj = myScrollView;
				//5毫秒后再次对滚动位置进行判断
				handler.sendMessageDelayed(message, 5);
			}
		}
		
	};
	/**
	 * @param context
	 * @param attrs
	 */
	public MyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	protected void checkVisibility() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 */
	protected void loadMoreImages() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
	 */
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

		/**
		 * 图片的URL地址
		 */
		private String mImageUrl;
		/**
		 * 可重复使用的ImageView
		 */
		private ImageView mImageView;
		public LoadImageTask(){
			
		}
		public LoadImageTask(ImageView imageView){
			mImageView = imageView;
		}
		
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Bitmap doInBackground(String... params) {
			// TODO Auto-generated method stub
			mImageUrl = params[0];
			Bitmap imageBitmap = imageLoader.getBitmapFromMemoryCache(mImageUrl);
			if (imageBitmap == null) {
				imageBitmap  = LoadImage(mImageUrl);
			}
			return imageBitmap;
		}
		@Override
		protected void onPostExecute(Bitmap bitmap){
			if (bitmap != null) {
				double ratio = bitmap.getWidth() / (columnWidth * 1.0);
				int scaledHeight = (int) (bitmap.getHeight() / ratio);
				addImage(bitmap, columnWidth, scaledHeight);
			}
			taskCollection.remove(this);
		}
		/**
		 * 向ImageView中添加一张图片
		 * @param bitmap
		 *      待添加的图片
		 * @param imageWidth
		 * 		图片的宽度
		 * @param imageHeight
		 * 		图片的高度
		 */
		private void addImage(Bitmap bitmap, int imageWidth, int imageHeight) {
			// TODO Auto-generated method stub
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					imageWidth, imageHeight);
			if (mImageView != null) {
				mImageView.setImageBitmap(bitmap);
			} else {
				ImageView imageView = new ImageView(getContext());
				imageView.setLayoutParams(params);
				imageView.setImageBitmap(bitmap);
				imageView.setScaleType(ScaleType.FIT_XY);
				imageView.setPadding(5, 5, 5, 5);
				imageView.setTag(R.string.image_url, mImageUrl);
				findColumnToAdd(imageView, imageHeight).addView(imageView);
				imageViewList.add(imageView);
			}
		}
		
		/**
		 * 找到此时应该添加图片的一刻 ：对三列的高度进行判断 当前高度最小的一列就是应该添加的一列
		 * @param imageView
		 * @param imageHeight
		 * @return
		 */
		private ViewGroup findColumnToAdd(ImageView imageView, int imageHeight) {
			// TODO Auto-generated method stub
			if (firstColumnHeight <= secondColumnHeight) {
				if (firstColumnHeight <= thirdColumnHeight) {
					imageView.setTag(R.string.border_top, firstColumnHeight);
					firstColumnHeight += imageHeight;
					imageView.setTag(R.string.border_bottom, firstColumnHeight);
					return firstColumn;
				}
				imageView.setTag(R.string.border_top, thirdColumnHeight);
				thirdColumnHeight += imageHeight;
				imageView.setTag(R.string.border_bottom, thirdColumnHeight);
			} else {
				if(secondColumnHeight <= thirdColumnHeight){
					imageView.setTag(R.string.border_top, secondColumnHeight);
					secondColumnHeight += imageHeight;
					imageView.setTag(R.string.border_bottom, secondColumnHeight);
					return secondColumn;
				}
				imageView.setTag(R.string.border_top, thirdColumnHeight);
				thirdColumnHeight += imageHeight;
				imageView.setTag(R.string.border_bottom, thirdColumnHeight);
				return thirdColumn;
			}
			return null;
		}
		/**
		 * 如果已在内存中 则直接读出 否则下载
		 * @param imageUrl
		 * @return
		 */
		private Bitmap LoadImage(String imageUrl) {
			// TODO Auto-generated method stub
			File imageFile = new File(getImagePath(imageUrl));
			if (!imageFile.exists()) {
				downloadImage(imageUrl);
			}
			if (imageUrl != null) {
				Bitmap bitmap = ImageLoader.decodeSampleBitmapFromResource(
						imageFile.getPath(), columnWidth);
				if (bitmap != null) {
					imageLoader.abbBitmapToMemoryCache(imageUrl, bitmap);
				}			
			}
			return null;
		}
		/**
		 * 将图片下载到SD卡缓存起来
		 * @param imageUrl 
		 *    图片的URL地址
		 */
		private void downloadImage(String imageUrl) {
			// TODO Auto-generated method stub
			HttpURLConnection con = null;
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			BufferedInputStream bis = null;
			File imageFile = null;
			try {
				URL url = new URL(imageUrl);
				con = (HttpURLConnection) url.openConnection();
				con.setConnectTimeout(5 * 1000);
				con.setReadTimeout(15 * 1000);
				con.setDoInput(true);
				con.setDoOutput(true);
				bis = new BufferedInputStream(con.getInputStream());
				imageFile = new File(getImagePath(imageUrl));
				fos = new FileOutputStream(imageFile);
				bos = new BufferedOutputStream(fos);
				byte[] b = new byte[1024];
				int length;
				while((length = bis.read(b)) != -1){
					bos.write(b, 0, length);
					bos.flush();
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}finally{
				 try {  
	                    if (bis != null) {  
	                        bis.close();  
	                    }  
	                    if (bos != null) {  
	                        bos.close();  
	                    }  
	                    if (con != null) {  
	                        con.disconnect();  
	                    }  
	                } catch (IOException e) {  
	                    e.printStackTrace();  
	                } 
			}
			if (imageFile != null) {
				Bitmap bitmap = ImageLoader.decodeSampleBitmapFromResource(
						imageFile.getPath(), columnWidth);
				if (bitmap != null) {
					imageLoader.abbBitmapToMemoryCache(imageUrl, bitmap);
				}
			}
		}
		/**
		 * @param imageUrl
		 * @return
		 */
		private String getImagePath(String imageUrl) {
			// TODO Auto-generated method stub
			int lastSlashIndex = imageUrl.lastIndexOf("/");
			String imageName = imageUrl.substring(lastSlashIndex + 1);
			String imageDir = Environment.getDataDirectory().getPath() + "/PhotoWallFalls/";
			File file = new File(imageDir);
			if (!file.exists()) {
				file.mkdir();
			}
			String imagePath = imageDir + imageName;
			return imagePath;
		}
		
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
