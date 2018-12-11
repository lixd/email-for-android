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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private Button btn_email_send;
    private String EMAIL_FROM ;//发件人
    private String EMAIL_TO ;//收件人
    private String EMAIL_TITLE ;//邮件标题
    private String EMAIL_CONTEXT ;//邮件内容
    private String EMAIL_PASSWORD ;//STMP授权密码 不是登录密码
    private EditText et_email_from;
    private EditText et_email_to;
    private EditText et_email_title;
    private EditText et_email_context;
    private EditText et_email_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initData() {
        EMAIL_FROM=et_email_from.getText().toString();
        EMAIL_TO=et_email_to.getText().toString();
        EMAIL_TITLE=et_email_title.getText().toString();
        EMAIL_CONTEXT=et_email_context.getText().toString();
        EMAIL_PASSWORD=et_email_password.getText().toString();
        Log.v("Az","EMAIL_FROM-->"+EMAIL_FROM+"EMAIL_TO-->"+EMAIL_TO
        +"EMAIL_TITLE-->"+EMAIL_TITLE+"EMAIL_CONTEXT-->"+EMAIL_CONTEXT+"EMAIL_PASSWORD-->"+EMAIL_PASSWORD);
    }

    private void initView() {
        btn_email_send = findViewById(R.id.btn_email_send);
        btn_email_send.setOnClickListener(this);

        et_email_from = findViewById(R.id.et_email_from);
        et_email_to = findViewById(R.id.et_email_to);
        et_email_title = findViewById(R.id.et_email_title);
        et_email_context = findViewById(R.id.et_email_context);
        et_email_password = findViewById(R.id.et_email_password);
    }

    public void SendEmail() {
        ThreadPoolExecutor threadPoolExecutor
                =new ThreadPoolExecutor(2,//核心线程池大小
                5,//最大线程池大小
                10,//空闲存活时间
                TimeUnit.SECONDS,//单位
                new LinkedBlockingQueue<Runnable>(),//等待任务队列
                new ThreadPoolExecutor.AbortPolicy());

        //子线程操作
       Thread emailThread= new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EmailHelper sender = new EmailHelper();
                    //设置服务器地址和端口 一般不用改
                    sender.setProperties("smtp.163.com", "25");
                    //设置发件人，邮件标题和文本内容
                    sender.setMessage(EMAIL_FROM,EMAIL_TITLE,EMAIL_CONTEXT);
                    //设置收件人 可以有多个
                    sender.setReceiver(new String[]{EMAIL_TO});
                    //添加附件换成你手机里正确的路径
                    // sender.addAttachment("/sdcard/bug.txt");
                    //发送邮件
                    sender.sendEmail("smtp.163.com", EMAIL_FROM, EMAIL_PASSWORD);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        });
       threadPoolExecutor.execute(emailThread);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_email_send:
                initData();
                SendEmail();
                Toast.makeText(this,"邮件已经发送",Toast.LENGTH_LONG).show();
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
}
