package threading;

import com.google.protobuf.Message;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 请求上下文
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-04-24 22:14
 **/
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class ReqContext {

    private long rid;
    private Message message;

}
