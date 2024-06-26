/**
 * 
 */
package org.sagacity.sqltoy.plugins.ddl.impl;

import org.sagacity.sqltoy.model.ColumnMeta;
import org.sagacity.sqltoy.model.TableMeta;
import org.sagacity.sqltoy.plugins.ddl.DDLUtils;
import org.sagacity.sqltoy.plugins.ddl.DialectDDLGenerator;
import org.sagacity.sqltoy.utils.StringUtil;

/**
 * @project sagacity-sqltoy
 * @description sqlserver数据库
 * @author zhongxuchen
 * @version v1.0, Date:2023年12月27日
 * @modify 2023年12月27日,修改说明
 */
public class SqlServerDDLGenerator implements DialectDDLGenerator {
	private String NEWLINE = "\r\n";
	private String TAB = "   ";

	/**
	 * 生成创建表sql
	 */
	public String createTableSql(TableMeta tableMeta, String schema, int dbType) {
		if (tableMeta == null) {
			return null;
		}
		StringBuilder tableSql = new StringBuilder();
		tableSql.append("create table ").append(tableMeta.getTableName()).append(NEWLINE);
		tableSql.append("(").append(NEWLINE);
		int index = 0;
		String splitSign = ";";
		for (ColumnMeta colMeta : tableMeta.getColumns()) {
			if (index > 0) {
				tableSql.append(",").append(NEWLINE);
			}
			// 字段名
			tableSql.append(TAB).append(colMeta.getColName());
			// 类型
			tableSql.append(" ").append(DDLUtils.convertType(colMeta, dbType));
			// 是否为null
			if (!colMeta.isNullable()) {
				tableSql.append(" not null");
			}
			// 自增
			if (colMeta.isAutoIncrement()) {
				tableSql.append(" IDENTITY ");
			} else if (StringUtil.isNotBlank(colMeta.getDefaultValue())) {
				tableSql.append(" default ");
				if (DDLUtils.isNotChar(colMeta.getDataType())) {
					tableSql.append(colMeta.getDefaultValue());
				} else {
					tableSql.append("'").append(colMeta.getDefaultValue()).append("'");
				}
			}
			index++;
		}
		// 主键
		DDLUtils.wrapTablePrimaryKeys(tableMeta, dbType, tableSql);
		tableSql.append(NEWLINE);
		tableSql.append(")");
		// 表注释，sqlserver比较特殊，不支持comment
		if (StringUtil.isNotBlank(schema)) {
			// 表注释
			if (StringUtil.isNotBlank(tableMeta.getRemarks())) {
				// EXEC sys.sp_addextendedproperty 'MS_Description', '表注释', 'SCHEMA', dbo,
				// 'table' , 表名 , null, null
				tableSql.append(splitSign);
				tableSql.append(NEWLINE);
				tableSql.append(NEWLINE);
				tableSql.append("EXECUTE sp_addextendedproperty 'MS_Description','").append(tableMeta.getRemarks())
						.append("',");
				tableSql.append("'schema','" + schema + "',");
				tableSql.append("'table','").append(tableMeta.getTableName()).append("'");
			}
			// 字段注释
			for (ColumnMeta colMeta : tableMeta.getColumns()) {
				if (StringUtil.isNotBlank(colMeta.getComments())) {
					tableSql.append(splitSign);
					tableSql.append(NEWLINE);
					tableSql.append(NEWLINE);
					tableSql.append("EXECUTE sp_addextendedproperty 'MS_Description','").append(colMeta.getComments())
							.append("',");
					tableSql.append("'schema','" + schema + "',");
					tableSql.append("'table','").append(tableMeta.getTableName()).append("',");
					tableSql.append("'column','").append(colMeta.getColName()).append("'");
				}
			}
		}
		// 索引
		DDLUtils.wrapTableIndexes(tableMeta, dbType, tableSql, true);
		// 外键
		DDLUtils.wrapForeignKeys(tableMeta, dbType, tableSql, true);
		return tableSql.toString();
	}
}
