package wxdgamin.boot.net.message;

import io.protostuff.Tag;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import pojo.PojoBase;

public class Rpc {

   /** 执行同步等待消息 */
   @Getter
   @Setter
   @Accessors(chain = true)
   public static class ReqRemote extends PojoBase {

       /**  */
       @Tag(1)
       private long rpcId;
       /** 1表示压缩过 */
       @Tag(2)
       private int gzip;
       /** 执行的命令 */
       @Tag(3)
       private String cmd;
       /** 用JsonObject来解析 */
       @Tag(4)
       private String params;
       /** 用于验证的消息 */
       @Tag(5)
       private String rpcToken;

   }

   /** 执行同步等待消息 */
   @Getter
   @Setter
   @Accessors(chain = true)
   public static class ResRemote extends PojoBase {

       /**  */
       @Tag(1)
       private long rpcId;
       /** 1表示压缩过 */
       @Tag(2)
       private int gzip;
       /** 用JsonObject来解析 */
       @Tag(3)
       private String params;
       /** 用于验证的消息 */
       @Tag(4)
       private String rpcToken;

   }
}