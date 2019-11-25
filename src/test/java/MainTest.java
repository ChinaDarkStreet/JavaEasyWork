import com.mtl.utils.WinUtil;
import org.junit.Test;

/**
 * Created by MTL on 2019/11/25
 */
public class MainTest {
    @Test
    public void myTest() throws InterruptedException {

        WinUtil.sendNotification("title", "text", "C:\\Users\\MTL\\Pictures\\Camera Roll/launcher.png");


        Thread.sleep(10000);

        WinUtil.sendNotification("title2222222", "text", "classpath:images/ic_launcher_round.png");
        Thread.sleep(10000);

    }

    @Test
    public void tmp(){
        String s = "{tskId=2b213c4ca650420db4055dfaac672ca5, ageId=0001, tskNO=20191125174134, tatId=null, tskExcTime=null, tskName=小小肉4, tskBeginDate=2019/11/25 17:41:00, tskEndDate=2019/11/26 17:41:00, tskExtendDate=1, tskType=1, tskTotal=0, userId=a855e8088eda4a10ac9f04eb8e2a186d, tskDate=2019/11/25 17:41:34, tskCreatDate=2019/11/25 17:41:34, tskMark=1, tskNote=分身乏术, tskAtomType=0, tskMixedType=1}";

        System.out.println(s.replaceAll("(\\w+)=([^,}]+)", "\"$1\":\"$2\""));

    }
}
