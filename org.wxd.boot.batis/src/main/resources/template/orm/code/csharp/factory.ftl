using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ${packageName}.Bean;

namespace ${packageName}.Factory
{

    /// <summary>
    /// excel 构建 ${tableComment}
    /// <summary>
    public class ${codeClassName}Factory
    {
        public ${codeClassName}Factory()
        {
            string jsonString = File.ReadAllText("config_json/${tableName}.json");
            Beans = Newtonsoft.Json.JsonConvert.DeserializeObject<List<${codeClassName}Bean>>(jsonString);
            foreach (var bean in Beans)
            {
                beanMap[bean.QId] = bean;
            }
        }

        public List<${codeClassName}Bean> Beans { get; set; }
        
        private Dictionary<int, ${codeClassName}Bean> beanMap = new Dictionary<int, ${codeClassName}Bean>();

        public Dictionary<int, ${codeClassName}Bean> BeanMap
        {
            get { return beanMap; }
            set { beanMap = value; }
        }

    }
}