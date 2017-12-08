package com.tobot.tobot.presenter.BRealize;

import android.content.Context;
import android.os.Message;

import com.tobot.tobot.MainActivity;
import com.tobot.tobot.base.Constants;
import com.tobot.tobot.presenter.ICommon.ISceneV;
import com.tobot.tobot.presenter.IPort.IFrame;
import com.tobot.tobot.scene.CustomScenario;
import com.tobot.tobot.utils.TobotUtils;
import com.turing123.robotframe.RobotFrameManager;
import com.turing123.robotframe.RobotFramePreparedListener;
import com.turing123.robotframe.config.SystemConfig;
import com.turing123.robotframe.function.FunctionManager;
import com.turing123.robotframe.function.cloud.Cloud;
import com.turing123.robotframe.function.expression.Expression;
import com.turing123.robotframe.function.motor.Motor;
import com.turing123.robotframe.function.tts.TTS;
import com.turing123.robotframe.interceptor.StateBuilder;
import com.turing123.robotframe.multimodal.expression.FacialExpression;

/**
 * Created by Javen on 2017/12/7.
 */

public class BFrame implements IFrame{

    private static BFrame mBFarme;
    private Context mContent;
    private ISceneV mISceneV;
    private MainActivity mainActivity;
    private boolean whence;
    private  static RobotFrameManager mRobotFrameManager;
    private FunctionManager functionManager;
    private CustomScenario customScenario;
    private FacialExpression mFacialExpression;
    private Expression mExpression;
    private Motor motor;
    private Cloud mCloud;
    private TTS tts;


    public static synchronized BFrame instance(ISceneV mISceneV) {
        if (mBFarme == null) {
            mBFarme = new BFrame(mISceneV);
        }
        return mBFarme;
    }

    private  BFrame(ISceneV mISceneV){
        this.mISceneV = mISceneV;
        this.mContent = (Context) mISceneV;
        this.mainActivity = (MainActivity) mISceneV;
        onInitiate(false);
    }

    @Override
    public void onInitiate(boolean whence) {
        this.whence = whence;
        //0. 因为各功能的使用都需要携带使用该功能的场景，所以先创建一个场景，如果脱离场景使用，请使用FailOver 类。
        customScenario = new CustomScenario(mContent);
        //1. 设置对话模式为自动对话，主场景将维护对话的输入和输出。
        startRobotFramework(SystemConfig.CHAT_MODE_AUTO);
    }

    @Override
    public RobotFrameManager startRobotFramework(int mode) {
        // 取得框架实例
        mRobotFrameManager = RobotFrameManager.getInstance(mContent);
        //设置apikey
        mRobotFrameManager.setApiKeyAndSecret(Constants.APIKEY, Constants.SERVICE);
        // 设置框架聊天模式
        mRobotFrameManager.setChatMode(mode);
        // 设置状态机工作模式。查看API Ref以了解更多关于框架工作模式的信息
        int state = new StateBuilder(StateBuilder.DefaultMode).build();
        // prepare（）这个方法必须在你做任何事情之前被调用
        mRobotFrameManager.prepare(state, new RobotFramePreparedListener() {

            @Override
            public void onPrepared() {
                // 激活
                mRobotFrameManager.start();
                // 可选的控制场景
                // mRobotFrameManager.toLostScenario();
                // 回到默认场景
                // mRobotFrameManager.backMainScenario();
//                mainHandler.sendEmptyMessage(Constants.START_SUCESS_MSG);
            }

            @Override
            public void onError(String errorMsg) {
                // error occurred, check errorMsg and have all error fixed
                Message message = Message.obtain();
                message.what = Constants.START_ERROR_MSG;
                message.obj = errorMsg;
//                mainHandler.sendMessage(message);
            }
        });
        return null;
    }

    public static RobotFrameManager getRobotFrameManager(){
        if (TobotUtils.isNotEmpty(mRobotFrameManager)){
            return mRobotFrameManager;
        }else {
            return null;
        }
    }



}
