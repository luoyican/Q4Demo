package com.rfchina.internet.rfvoiceprintrecognitiondemo.acousticWave;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rfchina.internet.rfvoiceprintrecognitiondemo.R;
import com.rfchina.internet.rfvoiceprintrecognitiondemo.acousticWave.sinvoice.SinVoicePlayer;
import com.rfchina.internet.rfvoiceprintrecognitiondemo.acousticWave.sinvoice.SinVoiceRecognition;

/**
 * Created by luoyican on 2017/10/18.
 */

public class AcousticWaveActivity extends Activity {
    private static final String TAG = "AcousticWaveActivity";
    // 最大数字
    private final static int MAX_NUMBER = 5;
    // 识别成功
    private final static int MSG_SET_RECG_TEXT = 1;
    // 开始识别
    private final static int MSG_RECG_START = 2;
    // 识别结束
    private final static int MSG_RECG_END = 3;

    private final static String CODEBOOK = "12345";
    // 播放
    private SinVoicePlayer mSinVoicePlayer;
    // 录音
    private SinVoiceRecognition mRecognition;
    private Handler mHanlder;

    private ImageView ivBack;
    private EditText etUser;
    private TextView txtPlay;
    private TextView txtRecognizer;
    private TextView txtResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acoustic_wave);

        ivBack = (ImageView) findViewById(R.id.ivBack);
        etUser = (EditText) findViewById(R.id.etUser);
        txtPlay = (TextView) findViewById(R.id.txtPlay);
        txtRecognizer = (TextView) findViewById(R.id.txtRecognizer);
        txtResult = (TextView) findViewById(R.id.txtResult);

        mHanlder = new RegHandler(txtResult);

        init();
    }

    private void init() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        txtPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String platContent = etUser.getText().toString();
                if (TextUtils.isEmpty(platContent)) {
                    showTip("请先输入用户名");
                    return;
                }
                mSinVoicePlayer.play(platContent);

            }
        });

        txtRecognizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecognition.start();
            }
        });

        etUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                for (int i = 2; i <= s.length(); i++) {
                    if (s.charAt(i - 2) == s.charAt(i - 1)) {
                        showTip("连续相同数值" + "无法识别");
                        return;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSinVoicePlayer = new SinVoicePlayer(CODEBOOK);
        mSinVoicePlayer.setListener(new SinVoicePlayer.Listener() {
            @Override
            public void onPlayStart() {
                // 开始播放音频
                Log.d(TAG, "start play");
            }

            @Override
            public void onPlayEnd() {
                // 结束播放
                Log.d(TAG, "stop play");
            }
        });

        mRecognition = new SinVoiceRecognition(CODEBOOK);
        mRecognition.setListener(new SinVoiceRecognition.Listener() {

            @Override
            public void onRecognitionStart() {
                mHanlder.sendEmptyMessage(MSG_RECG_START);
                Log.d("FFFFFF", "" + 11);
            }

            @Override
            public void onRecognition(char ch) {
                mHanlder.obtainMessage(MSG_SET_RECG_TEXT,ch,0).sendToTarget();
                Log.d("FFFFFF", "" + ch);
            }

            @Override
            public void onRecognitionEnd() {
                mHanlder.sendEmptyMessage(MSG_RECG_END);
                Log.d("FFFFFF", "" + 22);
            }
        });
    }

    private class RegHandler extends Handler {
        private StringBuilder mTextBuilder = new StringBuilder("识别结果：");
        private TextView mRecognisedTextView;

        public RegHandler(TextView textView) {
            mRecognisedTextView = textView;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SET_RECG_TEXT:
                    char ch = (char) msg.arg1;
                    mTextBuilder.append(ch);
                    Log.d("FFFFFE", mTextBuilder.toString());
                    if (null != mRecognisedTextView) {
                        mRecognisedTextView.setText(mTextBuilder.toString());
                    }
                    break;

                case MSG_RECG_START:
                    mTextBuilder = new StringBuilder("识别结果：");
                    break;

                case MSG_RECG_END:
                    Log.d(TAG, "recognition end");
                    break;
            }
        }
    }


    private void showTip(final String s) {
        txtResult.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(txtResult.getContext(), s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecognition.stop();
    }
}
