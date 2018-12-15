package lillusory.com.androidemail;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MyEmailHelper {
    private static final String TAG = MyEmailHelper.class.getSimpleName();
    private int port = 25;  //smtp协议使用的端口
    private String host = "smtp.163.com"; // 发件人邮件服务器
    //TODO 需要改成自己的账号和授权密码
    private String user = "xxx@163.com";   // 使用者账号
    private String password = "xxx"; //使用者SMTP授权密码
    private List<String> emailTos;
    private List<String> emailCCs;
    private String title;
    private String context;
    private List<String> paths;

    public enum SendStatus {
        SENDING, UNDO, SENDOK, SENDFAIL, BADCONTEXT
    }

    private SendStatus sendStatus;

    public interface EmailInfterface {
        void startSend();

        void SendStatus(SendStatus sendStatus);
    }

    private EmailInfterface EmailInfterface;

    public void setJieEmailInfterface(EmailInfterface EmailInfterface) {
        this.EmailInfterface = EmailInfterface;
    }


    public MyEmailHelper() {
        sendStatus = SendStatus.UNDO;
    }

    //构造发送邮件帐户的服务器，端口，帐户，密码
    public MyEmailHelper(String host, int port, String user, String password) {
        this.port = port;
        this.user = user;
        this.password = password;
        this.host = host;
        sendStatus = SendStatus.UNDO;
    }

    /**
     * @param emailTos 主要接收人的电子邮箱列表
     * @param emailCCs 抄送人的电子邮箱列表
     * @param title    邮件标题
     * @param context  正文内容
     * @param paths    发送的附件路径集合
     */
    public void setParams(List<String> emailTos, List<String> emailCCs, String title, String context,
                          List<String> paths) {
        this.emailTos = emailTos;
        this.emailCCs = emailCCs;
        this.title = title;
        this.context = context;
        this.paths = paths;
    }

    public void sendEmail() {
        new MyAsynTask().execute();
    }

    private void sendEmailBg() throws Exception {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");//true一定要加引号
        properties.put("mail.transport.protocol", "smtp");

        MyAuthenticator jieAuth = new MyAuthenticator(user, password);

        Session session = Session.getInstance(properties, jieAuth);
        //创建一个消息
        MimeMessage msg = new MimeMessage(session);

        //设置发送人
        msg.setFrom(new InternetAddress(user));

        //设置主要接收人
        if (emailTos != null && !emailTos.isEmpty()) {
            int size = emailTos.size();
            InternetAddress[] addresses = new InternetAddress[size];
            for (int i = 0; i < size; i++) {
                addresses[i] = new InternetAddress(emailTos.get(i));
            }
            msg.setRecipients(Message.RecipientType.TO, addresses);
        }

        //设置抄送人的电子邮件
        if (emailCCs != null && !emailCCs.isEmpty()) {
            int size = emailCCs.size();
            InternetAddress[] addresses = new InternetAddress[size];
            for (int i = 0; i < size; i++) {
                addresses[i] = new InternetAddress(emailCCs.get(i));
            }
            msg.setRecipients(Message.RecipientType.CC, addresses);
        }

        msg.setSubject(title);

        //创建一个消息体
        MimeBodyPart msgBodyPart = new MimeBodyPart();
        msgBodyPart.setText(context);

        //创建Multipart增加其他的parts
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(msgBodyPart);

        //创建文件附件
        if (paths != null) {
            for (String path : paths) {
                MimeBodyPart fileBodyPart = new MimeBodyPart();
                fileBodyPart.attachFile(path);
                mp.addBodyPart(fileBodyPart);
            }
        }
        //增加Multipart到消息体中
        msg.setContent(mp);
        //设置日期
        msg.setSentDate(new Date());
        //设置附件格式
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);
        //发送消息
        Transport.send(msg);
    }

    class MyAuthenticator extends Authenticator {
        private String strUser;
        private String strPwd;

        public MyAuthenticator(String user, String password) {
            this.strUser = user;
            this.strPwd = password;
        }


        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(strUser, strPwd);
        }
    }

    class MyAsynTask extends AsyncTask<Void, Void, SendStatus> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (EmailInfterface != null) {
                EmailInfterface.startSend();
            }
        }

        @Override
        protected void onPostExecute(SendStatus result) {
            super.onPostExecute(result);
            if (EmailInfterface != null) {
                EmailInfterface.SendStatus(result);
            }
            sendStatus = SendStatus.UNDO;
        }

        @Override
        protected SendStatus doInBackground(Void... params) {
            try {
                sendStatus = SendStatus.SENDING;
                sendEmailBg();
                sendStatus = SendStatus.SENDOK;
            } catch (Exception e) {
                String message = e.getMessage();
                Log.v(TAG, "邮件发送失败的原因--》" + message);
                SendStatus sendStatus = CheckErrorUtils.checkExcption(message);
                e.printStackTrace();
//                MyEmailHelper.this.sendStatus = SendStatus.SENDFAIL;
                MyEmailHelper.this.sendStatus = sendStatus;
            }
            return sendStatus;
        }
    }
}
