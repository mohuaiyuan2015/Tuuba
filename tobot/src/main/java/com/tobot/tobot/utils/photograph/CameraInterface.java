package com.tobot.tobot.utils.photograph;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;

import com.tobot.tobot.presenter.BRealize.BLocal;

/**
 * Created by Javen on 2017/8/11.
 */

public class CameraInterface {
    private static final String TAG = "Javen CameraInterface";
    private Camera mCamera;
    private Camera.Parameters mParams;
    private boolean isPreviewing = false;
    private float mPreviwRate = -1f;
    private static CameraInterface mCameraInterface;
    private BLocal mBLocal;
    private String FilePath;

    public interface CamOpenOverCallback{
        void cameraHasOpened();
    }

    private CameraInterface(){

    }

    public static synchronized CameraInterface getInstance(){
        if(mCameraInterface == null){
            mCameraInterface = new CameraInterface();
        }
        return mCameraInterface;
    }

    /**打开Camera
     * @param callback
     */
    public void doOpenCamera(CamOpenOverCallback callback) throws Exception{
        Log.i(TAG, "Perform to Camera open");
        mCamera = Camera.open();
        Log.i(TAG, "Perform to Camera open over");
        callback.cameraHasOpened();
        mBLocal = (BLocal) callback;
    }

    /**开启预览
     * @param holder
     * @param previewRate
     */
    public void doStartPreview(SurfaceHolder holder, float previewRate){
        Log.i(TAG, "Perform to doStartPreview===>isPreviewing:" + isPreviewing);
        if(isPreviewing){
            mCamera.stopPreview();
            return;
        }
        if(mCamera != null){
            Log.i(TAG, "Perform to doStartPreview===>:mCamera != null");
            mParams = mCamera.getParameters();
            mParams.setPictureFormat(PixelFormat.JPEG);//设置拍照后存储的图片格式
            CamParaUtil.getInstance().printSupportPictureSize(mParams);
            CamParaUtil.getInstance().printSupportPreviewSize(mParams);
            //设置PreviewSize和PictureSize
            Size pictureSize = CamParaUtil.getInstance().getPropPictureSize(
                    mParams.getSupportedPictureSizes(),previewRate, 800);
            mParams.setPictureSize(pictureSize.width, pictureSize.height);
            Size previewSize = CamParaUtil.getInstance().getPropPreviewSize(
                    mParams.getSupportedPreviewSizes(), previewRate, 800);
            mParams.setPreviewSize(previewSize.width, previewSize.height);

            mCamera.setDisplayOrientation(90);//旋转90度

            CamParaUtil.getInstance().printSupportFocusMode(mParams);
            List<String> focusModes = mParams.getSupportedFocusModes();
            if(focusModes.contains("continuous-video")){
                mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            mCamera.setParameters(mParams);

            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();//开启预览
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.i(TAG,"开启预览异常");
            }

            isPreviewing = true;
            mPreviwRate = previewRate;

            mParams = mCamera.getParameters(); //重新get一次
            Log.i(TAG, "最终设置:PreviewSize--With = " + mParams.getPreviewSize().width
                    + "Height = " + mParams.getPreviewSize().height);
            Log.i(TAG, "最终设置:PictureSize--With = " + mParams.getPictureSize().width
                    + "Height = " + mParams.getPictureSize().height);
        }
    }

    /**
     * 停止预览，释放Camera
     */
    public void doStopCamera(){
        Log.i(TAG, "Perform to doStopCamera ");
        if(null != mCamera){
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            isPreviewing = false;
            mPreviwRate = -1f;
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 拍照
     */
    public void doTakePicture(){
        Log.i(TAG, "Perform to doTakePicture===>isPreviewing:"+isPreviewing);
        if(isPreviewing && (mCamera != null)){
            mCamera.takePicture(mShutterCallback, null, mJpegPictureCallback);
        }
    }

    /*为了实现拍照的快门声音及拍照保存照片需要下面三个回调变量*/
    ShutterCallback mShutterCallback = new ShutterCallback()
            //快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作。默认的就是咔嚓。
    {
        public void onShutter() {
            // TODO Auto-generated method stub
            Log.i(TAG, "Perform to myShutterCallback:onShutter");
        }
    };

    PictureCallback mRawCallback = new PictureCallback()
            // 拍摄的未压缩原数据的回调,可以为null
    {
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            Log.i(TAG, "Perform to myRawCallback:onPictureTaken");
        }
    };

    PictureCallback mJpegPictureCallback = new PictureCallback()
            //对jpeg图像数据的回调,最重要的一个回调
    {
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            Log.i(TAG, "Perform to mJpegPictureCallback:onPictureTaken");
            Bitmap bitmap = null;
            if (null != data) {
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
                mCamera.stopPreview();
                isPreviewing = false;
            }
            //保存图片到sdcard
            if (null != bitmap) {
                //设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation", 90)失效。
                //图片竟然不能旋转了，故这里要旋转下
                Bitmap rotaBitmap = ImageUtil.getRotateBitmap(bitmap, 90.0f);
                FilePath = FileUtil.saveBitmap(rotaBitmap);
                mBLocal.upload(FilePath);
            }

            //再次进入预览
//            mCamera.startPreview();//开启预览时使用
//            isPreviewing = true;//开启预览时使用

            CameraInterface.getInstance().doStopCamera();//不开启屏幕时使用

        }
    };


}