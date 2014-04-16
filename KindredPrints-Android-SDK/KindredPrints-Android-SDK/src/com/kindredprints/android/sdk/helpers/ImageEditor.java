package com.kindredprints.android.sdk.helpers;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.util.Log;

import com.kindredprints.android.sdk.data.PrintProduct;
import com.kindredprints.android.sdk.data.Size;

public class ImageEditor {
	public static final String NO_FILTER = "kp_none";
	public static final String FILTER_DOUBLE = "doublesided";
	private static final float SQUARE_TOLERANCE = 0.05f;
	
	public static ArrayList<PrintProduct> getAllowablePrintableSizesForImageSize(Size size, ArrayList<PrintProduct> allSizes, String filter) {
		ArrayList<PrintProduct> outputArray = new ArrayList<PrintProduct>();
	    for (PrintProduct savedProd : allSizes) {
	    	savedProd.setDpi(Math.min(size.getWidth()/savedProd.getTrimmed().getWidth(), size.getHeight()/savedProd.getTrimmed().getHeight()));
	        if (isSquare(size) && isSquare(savedProd.getTrimmed()) && matchesFilter(savedProd, filter)) {
	        	outputArray.add(savedProd);
	        } else if (!isSquare(size) && !isSquare(savedProd.getTrimmed()) && matchesFilter(savedProd, filter)) {
	        	outputArray.add(savedProd);
	        }
	    }
	    return outputArray;
	}
	
	private static boolean matchesFilter(PrintProduct product, String filter) {
		Log.i("KindredSDK", "filter = " + filter + " title = " + product.getType());
		if (!filter.equals(NO_FILTER)) {
			return product.getType().contains(filter);
		} else {
			if (product.getType().contains(FILTER_DOUBLE)) {
				return false;
		 	} else {
				return true;
			}
		}
	}
	
	public static boolean passMinDpiThreshold(Size imgSize, PrintProduct product) {
		if(imgSize.getWidth()/product.getTrimmed().getWidth() > product.getMinDPI() 
				&& imgSize.getHeight()/product.getTrimmed().getHeight() > product.getMinDPI()) {
			return true;
		}
		return false;
	}
	
	public static boolean passWarnDpiThreshold(Size imgSize, PrintProduct product) {
		if(imgSize.getWidth()/product.getTrimmed().getWidth() > product.getWarnDPI() 
				&& imgSize.getHeight()/product.getTrimmed().getHeight() > product.getWarnDPI()) {
			return true;
		}
		return false;
	}
	
	public static boolean isSquare(Size size) {
		float delta = Math.abs(size.getHeight()-size.getWidth());
		float tolerance = 0.0f;
		if (size.getWidth() > size.getHeight()) {
			tolerance = size.getWidth()*SQUARE_TOLERANCE;
		} else {
			tolerance = size.getHeight()*SQUARE_TOLERANCE;
		}
		if (delta < tolerance) {
			return true;
		}
		return false;
	}
	
	public static PrintProduct getDefaultPrintableSizeForImageSize(Size size, ArrayList<PrintProduct> allSizes) {
		PrintProduct maxDPISize = null;
		float maxDPI = 0;
		
		for (PrintProduct savedProd : allSizes) {
			if (size.getHeight()/savedProd.getTrimmed().getHeight() > maxDPI) {
				maxDPI = size.getHeight()/savedProd.getTrimmed().getHeight();
				maxDPISize = savedProd;
			}
		}
		
		return maxDPISize;
	}	
	
	public static Bitmap format_image(Bitmap bm, int orient, float squareOffset, Size picSize, float borderSize, int borderColor) {
		Bitmap croppedBM = null;
		
		if (isSquare(picSize)) {
			croppedBM = crop_square(bm, squareOffset);
		} else {
			croppedBM = crop_rectangle(bm, picSize, squareOffset);
		}

		if (borderSize > 0) {
			Bitmap outp = Bitmap.createBitmap((int)picSize.getWidth(), (int)picSize.getHeight(), croppedBM.getConfig());
			Canvas c = new Canvas(outp);
			c.drawColor(borderColor);
			

			picSize.setHeight(picSize.getHeight()-borderSize*2.0f);
			picSize.setWidth(picSize.getWidth()-borderSize*2.0f);
			RectF drawingRect = new RectF(borderSize, borderSize, borderSize+picSize.getWidth(), borderSize+picSize.getHeight());
			Bitmap scaledBM = scale_and_rotate(croppedBM, 0, picSize);
			
			c.drawBitmap(scaledBM, null, drawingRect, null);
			
			return outp;
		} else {
			return scale_and_rotate(croppedBM, 0, picSize);
		}
		
		
	}
	
	public static Bitmap crop_rectangle(Bitmap bm, Size picSize, float squareOffset) {
		int startWidth = bm.getWidth();
		int startHeight = bm.getHeight();
		int xSide = startWidth;
		int ySide = startHeight;

		float actualAspectRatio = (float)startHeight/(float)startWidth;
		float idealAspectRatio = picSize.getHeight()/picSize.getWidth();

		xSide = actualAspectRatio < idealAspectRatio ? (int)(ySide/idealAspectRatio) : startWidth;
		ySide = actualAspectRatio < idealAspectRatio ? startHeight : (int)(xSide*idealAspectRatio);
		int x = (startWidth-xSide)/2;
		int y = (startHeight-ySide)/2;
		
		return Bitmap.createBitmap(
				bm,
				x, 
				y,
				xSide,
				ySide 
				);
	}
	
	public static Bitmap crop_square(Bitmap bm, float squareOffset) {
		int startWidth = bm.getWidth();
		int startHeight = bm.getHeight();
		
		int side = startWidth;
		if (startWidth > startHeight)
			side = startHeight;
		
		int x = (startWidth - side)/2;
		int y = (startHeight - side)/2;
		
		if (squareOffset >= 0) {
			if(startWidth >= startHeight) {
				x = (int)(squareOffset * (float)startWidth);
			} else {
				y = (int)(squareOffset * (float)startHeight);
			}
		}
		
		return Bitmap.createBitmap(
				bm,
				x, 
				y,
				side,
				side 
				);
	}
	
	public static Bitmap scale_and_rotate(Bitmap bm, int orient, Size picSize) {
		Matrix matrix = new Matrix();
		matrix = concatRotateOpsFromExifOrient(matrix, orient);
		
		float scale = 1.0f;
		if (bm.getWidth() <= bm.getHeight()) {
			scale = picSize.getHeight()/bm.getHeight();
		} else {
			scale = picSize.getWidth()/bm.getWidth();
		}
		matrix.postScale(scale, scale);
		
		return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
	}
	
	public static Size rotateSizeFromExifOrient(Size size, int exifOrient) {
		switch (exifOrient) {
		case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
			return size;
		case ExifInterface.ORIENTATION_FLIP_VERTICAL:
			return size;
		case ExifInterface.ORIENTATION_ROTATE_180:
			return size;
		case ExifInterface.ORIENTATION_TRANSPOSE:
			return new Size(size.getHeight(), size.getWidth());
		case ExifInterface.ORIENTATION_ROTATE_90:
			return new Size(size.getHeight(), size.getWidth());
		case ExifInterface.ORIENTATION_TRANSVERSE:
			return new Size(size.getHeight(), size.getWidth());
		case ExifInterface.ORIENTATION_ROTATE_270:
			return new Size(size.getHeight(), size.getWidth());
		default:
			return size;
		}
	}
	
	public static Matrix concatRotateOpsFromExifOrient(Matrix mx, int exifOrient) {
		switch (exifOrient) {
			case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
				mx.postScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_FLIP_VERTICAL:
				mx.postRotate(180);
				mx.postScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				mx.postRotate(180);
				break;
			case ExifInterface.ORIENTATION_TRANSPOSE:
				mx.postRotate(90);
				mx.postScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_ROTATE_90:
				mx.postRotate(90);
				break;
			case ExifInterface.ORIENTATION_TRANSVERSE:
				mx.postRotate(-90);
				mx.postScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				mx.postRotate(-90);
			default:
				break;
		}
		return mx;
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
			
			double divisor = inSampleSize;
			int powerCounter = 0;
			while (divisor > 1) {
				divisor = divisor/2.0;
				powerCounter = powerCounter + 1;
			}
			
			inSampleSize = (int) Math.pow(2, powerCounter);
		}
		
		return inSampleSize;
	}
}
