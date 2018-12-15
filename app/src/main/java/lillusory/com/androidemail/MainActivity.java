package lillusory.com.androidemail;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Button btn_email_send;
    private String EMAIL_FROM;//发件人
    private String EMAIL_TO;//收件人
    private String EMAIL_TITLE;//邮件标题
    private String EMAIL_CONTEXT;//邮件内容
    private EditText et_email_from;
    private EditText et_email_to;
    private EditText et_email_title;
    private EditText et_email_context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initData() {
        EMAIL_FROM = et_email_from.getText().toString();
        EMAIL_TO = et_email_to.getText().toString();
        EMAIL_TITLE = et_email_title.getText().toString();
        EMAIL_CONTEXT = et_email_context.getText().toString();
        Log.v("Az", "EMAIL_FROM-->" + EMAIL_FROM + "EMAIL_TO-->" + EMAIL_TO
                + "EMAIL_TITLE-->" + EMAIL_TITLE + "EMAIL_CONTEXT-->" + EMAIL_CONTEXT);
    }

    private void initView() {
        btn_email_send = findViewById(R.id.btn_email_send);
        btn_email_send.setOnClickListener(this);

        et_email_from = findViewById(R.id.et_email_from);
        et_email_to = findViewById(R.id.et_email_to);
        et_email_title = findViewById(R.id.et_email_title);
        et_email_context = findViewById(R.id.et_email_context);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_email_send:
                initData();
                sendMail(EMAIL_FROM, EMAIL_TO, EMAIL_TITLE, EMAIL_CONTEXT);
                Toast.makeText(this, "邮件已经发送", Toast.LENGTH_LONG).show();
                break;
//            case value:
//
//                break;
//            case value:
//
//                break;
            default:

                break;
        }
    }

    private MyEmailHelper helper = new MyEmailHelper();

    public void sendMail(String from, String to, String title, String context) {
//          附件
//        List<String> files = new ArrayList<String>();
//        files.add("/mnt/sdcard/test.txt");
        //主要接收人的电子邮箱列表
        List<String> toEmail = new ArrayList<String>();
        toEmail.add(to);
        List<String> ccEmail = new ArrayList<String>();
        //抄送人的电子邮箱列表 抄送给自己 防止被检测为垃圾邮件
        ccEmail.add(from);
        helper.setParams(toEmail, ccEmail, title, context, null);
        Log.v(TAG, "toEmail:" + toEmail + " ccEmail:" + ccEmail + " EMAIL_TITLE_APP:" + title + " appEmailContext:" + context);
        helper.setJieEmailInfterface(new MyEmailHelper.EmailInfterface() {
            @Override
            public void startSend() {
                Toast.makeText(MainActivity.this, "邮件发送中~", Toast.LENGTH_LONG).show();
            }

            @Override
            public void SendStatus(MyEmailHelper.SendStatus sendStatus) {
                switch (sendStatus) {
                    case SENDOK:
                        Toast.makeText(MainActivity.this, "发送邮件成功~", Toast.LENGTH_LONG).show();
                        break;
                    case SENDFAIL:
                        Toast.makeText(MainActivity.this, "发送邮件失败~", Toast.LENGTH_LONG).show();
                        break;
                    case SENDING:
                        Toast.makeText(MainActivity.this, "邮件正在发送中，请稍后重试~", Toast.LENGTH_LONG).show();
                        break;
                    case BADCONTEXT:
                        Toast.makeText(MainActivity.this, "邮件内容或标题被识别为垃圾邮件，请修改后重试~", Toast.LENGTH_LONG).show();
                        break;

                }
            }
        });
        helper.sendEmail();
    }
}
