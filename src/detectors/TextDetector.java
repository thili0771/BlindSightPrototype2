package detectors;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

//import com.googlecode.tesseract.android.TessBaseAPI;

import android.graphics.Bitmap;
import android.util.Log;

public class TextDetector implements Filter{
	Bitmap bmp=null;
	String ResultText=null;
	//TessBaseAPI baseApi = new TessBaseAPI();
	public void ocr(Mat tmp){
		
        //baseApi.init("/mnt/sdcard/tesseract/", "eng");
        //baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
        bmp=Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(tmp, bmp);
        //baseApi.setImage(bmp);
        //ResultText = baseApi.getUTF8Text();
	}
	
	@Override
	public void apply(Mat src, Mat dst) {
		if (dst != src) {
			src.copyTo(dst);
		}
	    Mat img_gray,img_sobel, img_threshold, element;
	    
	    img_gray=new Mat();
	    Imgproc.cvtColor(src, img_gray, Imgproc.COLOR_RGB2GRAY);
	    
	    img_sobel=new Mat();
	    Imgproc.Sobel(img_gray, img_sobel, CvType.CV_8U, 1, 0, 3, 1, 0,Core.BORDER_DEFAULT);
	    
	    img_threshold=new Mat();
	    Imgproc.threshold(img_sobel, img_threshold, 0, 255, Imgproc.THRESH_OTSU+Imgproc.THRESH_BINARY);
	    
	    element=new Mat();
	    element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(17, 3) );
	    Imgproc.morphologyEx(img_threshold, img_threshold, Imgproc.MORPH_CLOSE, element);
	    //Does the trick
	    List<MatOfPoint>  contours=new ArrayList<MatOfPoint>();
	    Mat hierarchy = new Mat();
	    Imgproc.findContours(img_threshold, contours, hierarchy, 0, 1);
	    List<MatOfPoint> contours_poly=new ArrayList<MatOfPoint>(contours.size());
	    contours_poly.addAll(contours);
	    
	    MatOfPoint2f mMOP2f1,mMOP2f2;
	    mMOP2f1=new MatOfPoint2f();
	    mMOP2f2=new MatOfPoint2f();

	    for( int i = 0; i < contours.size(); i++ )
	        if (contours.get(i).toList().size()>100)
	        { 
	        	contours.get(i).convertTo(mMOP2f1, CvType.CV_32FC2);
	        	Imgproc.approxPolyDP(mMOP2f1,mMOP2f2, 3, true );
	        	mMOP2f2.convertTo(contours_poly.get(i), CvType.CV_32S);
	            Rect appRect=Imgproc.boundingRect(contours_poly.get(i));
	            if (appRect.width>appRect.height) 
	            {
	            	Imgproc.rectangle(dst, new Point(appRect.x,appRect.y) ,new Point(appRect.x+appRect.width,appRect.y+appRect.height), new Scalar(255,0,0));
	            	/*if(i==1){
	            		Log.i(null, "cut Roi");
	            		Mat roiMat=new Mat(src,appRect);
	            		Log.i(null, "Ocr");
	            		ocr(roiMat);
	            		Log.i(null, "puttext");
	            		Imgproc.putText(dst, ResultText, new Point(appRect.x,appRect.y),Core.FONT_HERSHEY_SIMPLEX, 8, new Scalar(0,255,0));
	            		Log.i(null, "Complete!");
	            	}
	            	*/
	            }
	        }	
	    
	}
}
