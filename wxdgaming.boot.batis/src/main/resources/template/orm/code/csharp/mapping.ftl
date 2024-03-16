using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ${packageName}.Bean.Mapping
{

    /// <summary>
    /// excel 构建 ${tableComment}
    /// </summary>
    public class ${codeClassName}Mapping
    {
    <#list columns as column>
        /// <summary>
        /// ${column.columnComment}
        /// </summary>
        public ${column.fieldTypeString} ${column.fieldNameUpper} { get; set; }
    </#list>
    }
}