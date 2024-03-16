using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ${packageName}.Bean;

namespace ${packageName}.Table
{

    /// <summary>
    /// excel 构建 ${tableComment}
    /// <summary>
    public class ${codeClassName}Table
    {
        public ${codeClassName}Table()
        {
            string jsonString = File.ReadAllText("config_json/${tableName}.json");
            Rows = Newtonsoft.Json.JsonConvert.DeserializeObject<List<${codeClassName}Row>>(jsonString);
            foreach (var row in Rows)
            {
                rowMap[bean.QId] = row;
            }
        }

        public List<${codeClassName}Row> Rows { get; set; }
        
        private Dictionary<int, ${codeClassName}Row> rowMap = new Dictionary<int, ${codeClassName}Row>();

        public Dictionary<int, ${codeClassName}Row> RowMap
        {
            get { return rowMap; }
            set { rowMap = value; }
        }

    }
}