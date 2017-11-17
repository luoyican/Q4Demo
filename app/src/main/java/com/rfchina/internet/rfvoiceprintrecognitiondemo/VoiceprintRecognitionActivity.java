package com.rfchina.internet.rfvoiceprintrecognitiondemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeakerVerifier;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechListener;
import com.iflytek.cloud.VerifierListener;
import com.iflytek.cloud.VerifierResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by luoyican on 2017/10/9.
 */

public class VoiceprintRecognitionActivity extends Activity {
    private static final String TAG = "VoiceprintRecognitionAc";
    private static final int NUM_TRAIN_TIME = 3;

    private ImageView ivBack;
    private EditText etUser;
    private TextView txtPassword;
    private Spinner spPasswordType;
    private Button btnGetPassword;
    private Button btnSearch;
    private Button btnDelete;
    private Button btnRegister;
    private Button btnVerify;
    private Button btnStopRecord;
    private TextView txtMsg;

    private AlertDialog mTextPwdSelectDialog;
    private static final int PWD_TYPE_TEXT = 1;
    // 自由说由于效果问题，暂不开放
//	private static final int PWD_TYPE_FREE = 2;
    private static final int PWD_TYPE_NUM = 3;
    // 当前声纹密码类型，1、2、3分别为文本、自由说和数字密码
    private int mPwdType = PWD_TYPE_TEXT;
    // 声纹识别对象
    private SpeakerVerifier mVerifier;
    // 声纹AuthId，用户在云平台的身份标识，也是声纹模型的标识
    // 请使用英文字母或者字母和数字的组合，勿使用中文字符
    private String mAuthId = "";
    // 文本声纹密码
    private String mTextPwd = "";
    // 数字声纹密码
    private String mNumPwd = "";
    // 数字声纹密码段，默认有5段
    private String[] mNumPwdSegs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voiceprint_recognition);
        ivBack = (ImageView) findViewById(R.id.ivBack);
        etUser = (EditText) findViewById(R.id.etUser);
        txtPassword = (TextView) findViewById(R.id.txtPassword);
        spPasswordType = (Spinner) findViewById(R.id.spPasswordType);
        btnGetPassword = (Button) findViewById(R.id.btnGetPassword);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnVerify = (Button) findViewById(R.id.btnVerify);
        btnStopRecord = (Button) findViewById(R.id.btnStopRecord);
        txtMsg = (TextView) findViewById(R.id.txtMsg);

        // 初始化SpeakerVerifier，InitListener为初始化完成后的回调接口
        mVerifier = SpeakerVerifier.createVerifier(this, new InitListener() {
            @Override
            public void onInit(int errorCode) {
                if (ErrorCode.SUCCESS == errorCode) {
                    showTip("引擎初始化成功");
                } else {
                    showTip("引擎初始化失败，错误码：" + errorCode);
                }
            }
        });

        if (!checkInstance()) {
            return;
        }
        init();
    }

    private boolean checkAuthId() {
        mAuthId = etUser.getText().toString();
        if (TextUtils.isEmpty(mAuthId)) {
            showTip("请填入用户名");
            return false;
        }
        return true;
    }

    private void init() {
        spPasswordType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mPwdType = PWD_TYPE_TEXT;
                        break;
                    case 1:
                        mPwdType = PWD_TYPE_NUM;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnGetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkAuthId()) return;
                // 获取密码之前先终止之前的注册或验证过程
                mVerifier.cancel();
                // 清空参数
                mVerifier.setParameter(SpeechConstant.PARAMS, null);
                mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);
//                if (mPwdType == PWD_TYPE_NUM) {
                    //在数字数码时，服务器将根据设置的要训练的次数，返回对应有多少组的数字
                    mVerifier.setParameter(SpeechConstant.ISV_RGN, "" + NUM_TRAIN_TIME);
//                }
                mVerifier.getPasswordList(mPwdListenter);
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performModelOperation("que", mModelOperationListener);
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performModelOperation("del", mModelOperationListener);
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 清空参数
                mVerifier.setParameter(SpeechConstant.PARAMS, null);
                mVerifier.setParameter(SpeechConstant.ISV_AUDIO_PATH, Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/test.pcm");
                // 对于某些麦克风非常灵敏的机器，如nexus、samsung i9300等，建议加上以下设置对录音进行消噪处理
//			mVerify.setParameter(SpeechConstant.AUDIO_SOURCE, "" + MediaRecorder.AudioSource.VOICE_RECOGNITION);
                if (mPwdType == PWD_TYPE_TEXT) {
                    // 文本密码注册需要传入密码
                    if (TextUtils.isEmpty(mTextPwd)) {
                        showTip("请获取密码后进行操作");
                        return;
                    }
                    mVerifier.setParameter(SpeechConstant.ISV_PWD, mTextPwd);
                    showTip("请读出：" + mTextPwd);
                    txtMsg.setText("训练 第" + 1 + "遍，剩余" + (NUM_TRAIN_TIME - 1) + "遍");
                } else if (mPwdType == PWD_TYPE_NUM) {
                    // 数字密码注册需要传入密码
                    if (TextUtils.isEmpty(mNumPwd)) {
                        showTip("请获取密码后进行操作");
                        return;
                    }
                    mVerifier.setParameter(SpeechConstant.ISV_PWD, mNumPwd);
                    showTip("请读出：" + mNumPwd.substring(0, 8));
                    txtMsg.setText("训练 第" + 1 + "遍，剩余" + (NUM_TRAIN_TIME - 1) + "遍");
                }
                // 设置auth_id，不能设置为空
                mVerifier.setParameter(SpeechConstant.AUTH_ID, mAuthId);
                // 设置业务类型为注册
                mVerifier.setParameter(SpeechConstant.ISV_SST, "train");
                // 设置声纹密码类型
                mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);
                mVerifier.setParameter(SpeechConstant.ISV_RGN, "" + NUM_TRAIN_TIME);
                // 开始注册
                mVerifier.startListening(mRegisterListener);
            }
        });

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 清空参数
                mVerifier.setParameter(SpeechConstant.PARAMS, null);
                mVerifier.setParameter(SpeechConstant.ISV_AUDIO_PATH,
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/verify.pcm");
                mVerifier = SpeakerVerifier.getVerifier();
                // 设置业务类型为验证
                mVerifier.setParameter(SpeechConstant.ISV_SST, "verify");
                // 对于某些麦克风非常灵敏的机器，如nexus、samsung i9300等，建议加上以下设置对录音进行消噪处理
//			mVerify.setParameter(SpeechConstant.AUDIO_SOURCE, "" + MediaRecorder.AudioSource.VOICE_RECOGNITION);

                if (mPwdType == PWD_TYPE_TEXT) {
                    // 文本密码注册需要传入密码
                    if (TextUtils.isEmpty(mTextPwd)) {
                        showTip("请获取密码后进行操作");
                        return;
                    }
                    mVerifier.setParameter(SpeechConstant.ISV_PWD, mTextPwd);
                    showTip("请读出：" + mTextPwd);
                } else if (mPwdType == PWD_TYPE_NUM) {
                    // 数字密码注册需要传入密码,验证码的位数8
                    String verifyPwd = mVerifier.generatePassword(8);
                    mVerifier.setParameter(SpeechConstant.ISV_PWD, verifyPwd);
                    showTip("请读出：" + verifyPwd);
                }
                // 设置auth_id，不能设置为空
                mVerifier.setParameter(SpeechConstant.AUTH_ID, mAuthId);
                mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);
                // 开始验证
                mVerifier.startListening(mVerifyListener);
            }
        });
        btnStopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVerifier.stopListening();
            }
        });
    }

    /**
     * 执行模型查询or删除操作
     *
     * @param operation 操作命令
     * @param listener  操作结果回调对象
     */
    private void performModelOperation(String operation, SpeechListener listener) {
        // 清空参数
        mVerifier.setParameter(SpeechConstant.PARAMS, null);
        mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);

        if (mPwdType == PWD_TYPE_TEXT) {
            // 文本密码删除需要传入密码
            if (TextUtils.isEmpty(mTextPwd)) {
                showTip("请获取密码后进行操作");
                return;
            }
            mVerifier.setParameter(SpeechConstant.ISV_PWD, mTextPwd);
        } else if (mPwdType == PWD_TYPE_NUM) {

        }
        // 设置auth_id，不能设置为空
        mVerifier.sendRequest(operation, mAuthId, listener);
    }

    private String[] items;
    private SpeechListener mPwdListenter = new SpeechListener() {
        @Override
        public void onEvent(int eventType, Bundle params) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {

            String result = new String(buffer);
            switch (mPwdType) {
                case PWD_TYPE_TEXT:
                    try {
                        JSONObject object = new JSONObject(result);
                        if (!object.has("txt_pwd")) {
                            return;
                        }

                        JSONArray pwdArray = object.optJSONArray("txt_pwd");
                        items = new String[pwdArray.length()];
                        for (int i = 0; i < pwdArray.length(); i++) {
                            items[i] = pwdArray.getString(i);
                        }
                        mTextPwdSelectDialog = new AlertDialog.Builder(VoiceprintRecognitionActivity.this)
                                .setTitle("请选择密码文本")
                                .setItems(items,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface arg0, int arg1) {
                                                mTextPwd = items[arg1];
                                                txtPassword.setText("您的密码：" + mTextPwd);
                                            }
                                        }).create();
                        mTextPwdSelectDialog.show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case PWD_TYPE_NUM:
                    StringBuffer numberString = new StringBuffer();
                    try {
                        JSONObject object = new JSONObject(result);
                        if (!object.has("num_pwd")) {
                            return;
                        }

                        JSONArray pwdArray = object.optJSONArray("num_pwd");
                        numberString.append(pwdArray.get(0));
                        for (int i = 1; i < pwdArray.length(); i++) {
                            numberString.append("-" + pwdArray.get(i));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mNumPwd = numberString.toString();
                    mNumPwdSegs = mNumPwd.split("-");
                    txtPassword.setText("您的密码：\n" + mNumPwd);
                    break;
                default:
                    break;
            }

        }

        @Override
        public void onCompleted(SpeechError error) {
            if (null != error && ErrorCode.SUCCESS != error.getErrorCode()) {
                showTip("获取失败：" + error.getErrorCode());
            }
        }
    };

    private SpeechListener mModelOperationListener = new SpeechListener() {

        @Override
        public void onEvent(int eventType, Bundle params) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            String result = new String(buffer);
            try {
                JSONObject object = new JSONObject(result);
                String cmd = object.getString("cmd");
                int ret = object.getInt("ret");

                if ("del".equals(cmd)) {
                    if (ret == ErrorCode.SUCCESS) {
                        showTip("删除成功");
                    } else if (ret == ErrorCode.MSP_ERROR_FAIL) {
                        showTip("删除失败，模型不存在");
                    }
                } else if ("que".equals(cmd)) {
                    if (ret == ErrorCode.SUCCESS) {
                        showTip("模型存在");
                    } else if (ret == ErrorCode.MSP_ERROR_FAIL) {
                        showTip("模型不存在");
                    }
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (null != error && ErrorCode.SUCCESS != error.getErrorCode()) {
                showTip("操作失败：" + error.getPlainDescription(true));
            }
        }
    };

    private VerifierListener mRegisterListener = new VerifierListener() {

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
//            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据：" + data.length);
        }

        @Override
        public void onResult(VerifierResult result) {
            Log.d("TAGG", "onResult");
            Log.d("TAG", result.source+"");
            if (result.ret == ErrorCode.SUCCESS) {
                switch (result.err) {
                    case VerifierResult.MSS_ERROR_IVP_GENERAL:
                        showTip("内核异常");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_EXTRA_RGN_SOPPORT:
                        showTip("训练达到最大次数");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
                        showTip("出现截幅");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
                        showTip("太多噪音");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
                        showTip("录音太短");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
                        showTip("训练失败，您所读的文本不一致");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
                        showTip("音量太低");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
                        showTip("音频长达不到自由说的要求");
                    default:
                        showTip("此次训练成功");
                        break;
                }

                if (result.suc == result.rgn) {
                    showTip("注册成功");

                    if (PWD_TYPE_TEXT == mPwdType) {
                        txtPassword.setText("您的文本密码声纹ID：\n" + result.vid);
                    } else if (PWD_TYPE_NUM == mPwdType) {
                        txtPassword.setText("您的数字密码声纹ID：\n" + result.vid);
                    }

                } else {
                    int nowTimes = result.suc + 1;
                    int leftTimes = result.rgn - nowTimes;

                    if (PWD_TYPE_TEXT == mPwdType) {
                        showTip("请读出：" + mTextPwd);
                    } else if (PWD_TYPE_NUM == mPwdType) {
                        showTip("请读出：" + mNumPwdSegs[nowTimes - 1]);
                    }

                    txtMsg.setText("训练 第" + nowTimes + "遍，剩余" + leftTimes + "遍");
                }

            } else {
                showTip("注册失败，请重新开始。");
            }
        }

        // 保留方法，暂不用
        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle arg3) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }

        @Override
        public void onError(SpeechError error) {
            if (error.getErrorCode() == ErrorCode.MSP_ERROR_ALREADY_EXIST) {
                showTip("模型已存在，如需重新注册，请先删除");
            } else {
                showTip("onError Code：" + error.getPlainDescription(true));
            }
        }

        @Override
        public void onEndOfSpeech() {
            Log.d("TAGG","onEndOfSpeech");
//            showTip("结束说话");
        }

        @Override
        public void onBeginOfSpeech() {
            Log.d("TAGG","onBeginOfSpeech");
            showTip("开始说话");
        }
    };

    private VerifierListener mVerifyListener = new VerifierListener() {

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
//            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据：" + data.length);
        }

        @Override
        public void onResult(VerifierResult result) {
            if (result.ret == 0) {
                // 验证通过
                showTip("验证通过");
            } else {
                // 验证不通过
                switch (result.err) {
                    case VerifierResult.MSS_ERROR_IVP_GENERAL:
                        showTip("内核异常");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
                        showTip("出现截幅");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
                        showTip("太多噪音");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
                        showTip("录音太短");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
                        showTip("验证不通过，您所读的文本不一致");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
                        showTip("音量太低");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
                        showTip("音频长达不到自由说的要求");
                        break;
                    default:
                        showTip("验证不通过");
                        break;
                }
            }
        }

        // 保留方法，暂不用
        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle arg3) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }

        @Override
        public void onError(SpeechError error) {
            switch (error.getErrorCode()) {
                case ErrorCode.MSP_ERROR_NOT_FOUND:
                    showTip("模型不存在，请先注册");
                    break;

                default:
                    showTip("onError Code：" + error.getPlainDescription(true));
                    break;
            }
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }
    };

    @Override
    public void finish() {
        if (null != mTextPwdSelectDialog) {
            mTextPwdSelectDialog.dismiss();
        }
        super.finish();
    }

    @Override
    protected void onDestroy() {
        if (null != mVerifier) {
            mVerifier.stopListening();
//            mVerifier.destroy();
        }
        super.onDestroy();
    }

    private void showTip(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private boolean checkInstance() {
        if (null == mVerifier) {
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            this.showTip("创建对象失败，请确认 libmsc.so 放置正确，\n 且有调用 createUtility 进行初始化");
            return false;
        } else {
            return true;
        }
    }
}
