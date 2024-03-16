package ${savePack};

<#list imports as row>
import ${row};
</#list>

/**
 * ${comment} file=${filePath}
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: ${times}
 */
@Slf4j
@ProtoController(${serviceClass})
public final class ${saveClassName} implements IController {

    /** ${comment} ${messageName} */
    @ProtoMapping(remarks = "${comment}")
    public void exec(SocketSession session, ${messageName} reqMessage) throws Exception {
        ${userInfo}
        ${res}
    }
}
