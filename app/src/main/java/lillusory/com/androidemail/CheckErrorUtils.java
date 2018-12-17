package lillusory.com.androidemail;

import android.util.Log;

public class CheckErrorUtils {
    public static MyEmailHelper.SendStatus checkExcption(String message) {
        if (message.contains("554 DT:SPM")) {
            //发送失败原因有很多 这个是比较常见的问题
            Log.v("Az", "邮件被识别为垃圾邮件了~");
             return MyEmailHelper.SendStatus.BADCONTEXT;
        }
       
         return JEmailHelper.SendStatus.SENDFAIL;
    }
}
