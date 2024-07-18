package io.dataease.extensions.datasource.provider;

import com.jcraft.jsch.Session;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.constant.SqlPlaceholderConstants;
import io.dataease.extensions.datasource.dto.*;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import lombok.Getter;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.dialect.*;
import org.apache.calcite.sql.parser.SqlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Statement;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * @Author Junjun
 */
public abstract class Provider {

    public static Logger logger = LoggerFactory.getLogger(Provider.class);

    public abstract List<String> getSchema(DatasourceRequest datasourceRequest);

    public abstract List<DatasetTableDTO> getTables(DatasourceRequest datasourceRequest);

    public abstract ConnectionObj getConnection(DatasourceDTO coreDatasource) throws Exception;

    public abstract String checkStatus(DatasourceRequest datasourceRequest) throws Exception;

    public abstract Map<String, Object> fetchResultField(DatasourceRequest datasourceRequest) throws DEException;

    public abstract List<TableField> fetchTableField(DatasourceRequest datasourceRequest) throws DEException;

    @Getter
    private static final Map<Long, Integer> lPorts = new HashMap<>();
    @Getter
    private static final Map<Long, Session> sessions = new HashMap<>();

    public abstract void hidePW(DatasourceDTO datasourceDTO);

    public Statement getStatement(Connection connection, int queryTimeout) {
        if (connection == null) {
            DEException.throwException("Failed to get connection!");
        }
        Statement stat = null;
        try {
            stat = connection.createStatement();
            stat.setQueryTimeout(queryTimeout);
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
        return stat;
    }

    public String rebuildSQL(String sql, SQLMeta sqlMeta, boolean crossDs, Map<Long, DatasourceSchemaDTO> dsMap) {
        logger.info("calcite sql: " + sql);
        if (crossDs) {
            return sql;
        }

        String s = transSqlDialect(sql, dsMap);
        String tableDialect = sqlMeta.getTableDialect();
        s = replaceTablePlaceHolder(s, tableDialect);
        return replaceCalcFieldPlaceHolder(s, sqlMeta);
    }

    public String transSqlDialect(String sql, Map<Long, DatasourceSchemaDTO> dsMap) throws DEException {
        try {
            DatasourceSchemaDTO value = dsMap.entrySet().iterator().next().getValue();

            SqlParser parser = SqlParser.create(sql, SqlParser.Config.DEFAULT.withLex(Lex.JAVA));
            SqlNode sqlNode = parser.parseStmt();
            return sqlNode.toSqlString(getDialect(value)).toString();
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
        return null;
    }

    public String replaceTablePlaceHolder(String s, String placeholder) {
        s = s.replaceAll("\r\n", " ")
                .replaceAll("\n", " ")
                .replaceAll(SqlPlaceholderConstants.TABLE_PLACEHOLDER_REGEX, Matcher.quoteReplacement(placeholder))
                .replaceAll("ASYMMETRIC", "")
                .replaceAll("SYMMETRIC", "");
        return s;
    }

    public String replaceCalcFieldPlaceHolder(String s, SQLMeta sqlMeta) {
        Map<String, String> fieldsDialect = new HashMap<>();
        if (sqlMeta.getXFieldsDialect() != null && !sqlMeta.getXFieldsDialect().isEmpty()) {
            fieldsDialect.putAll(sqlMeta.getXFieldsDialect());
        }
        if (sqlMeta.getYFieldsDialect() != null && !sqlMeta.getYFieldsDialect().isEmpty()) {
            fieldsDialect.putAll(sqlMeta.getYFieldsDialect());
        }
        if (sqlMeta.getCustomWheresDialect() != null && !sqlMeta.getCustomWheresDialect().isEmpty()) {
            fieldsDialect.putAll(sqlMeta.getCustomWheresDialect());
        }
        if (sqlMeta.getExtWheresDialect() != null && !sqlMeta.getExtWheresDialect().isEmpty()) {
            fieldsDialect.putAll(sqlMeta.getExtWheresDialect());
        }
        if (sqlMeta.getWhereTreesDialect() != null && !sqlMeta.getWhereTreesDialect().isEmpty()) {
            fieldsDialect.putAll(sqlMeta.getWhereTreesDialect());
        }

        if (!fieldsDialect.isEmpty()) {
            for (Map.Entry<String, String> ele : fieldsDialect.entrySet()) {
                s = s.replaceAll(SqlPlaceholderConstants.KEYWORD_PREFIX_REGEX + ele.getKey() + SqlPlaceholderConstants.KEYWORD_SUFFIX_REGEX, Matcher.quoteReplacement(ele.getValue()));
            }
        }
        return s;
    }

    public SqlDialect getDialect(DatasourceSchemaDTO coreDatasource) {
        SqlDialect sqlDialect = null;
        DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(coreDatasource.getType());
        switch (datasourceType) {
            case mysql:
            case mongo:
            case StarRocks:
            case TiDB:
            case mariadb:
                sqlDialect = MysqlSqlDialect.DEFAULT;
                break;
            case doris:
                sqlDialect = DorisSqlDialect.DEFAULT;
                break;
            case impala:
                sqlDialect = ImpalaSqlDialect.DEFAULT;
                break;
            case sqlServer:
                sqlDialect = MssqlSqlDialect.DEFAULT;
                break;
            case oracle:
                sqlDialect = OracleSqlDialect.DEFAULT;
                break;
            case db2:
                sqlDialect = Db2SqlDialect.DEFAULT;
                break;
            case pg:
                sqlDialect = PostgresqlSqlDialect.DEFAULT;
                break;
            case redshift:
                sqlDialect = RedshiftSqlDialect.DEFAULT;
                break;
            case ck:
                sqlDialect = ClickHouseSqlDialect.DEFAULT;
                break;
            case h2:
                sqlDialect = H2SqlDialect.DEFAULT;
                break;
            default:
                sqlDialect = MysqlSqlDialect.DEFAULT;
        }
        return sqlDialect;
    }

    synchronized public Integer getLport(Long datasourceId) throws Exception {
        for (int i = 10000; i < 20000; i++) {
            if (isPortAvailable(i) && !lPorts.values().contains(i)) {
                if (datasourceId == null) {
                    lPorts.put((long) i, i);
                } else {
                    lPorts.put(datasourceId, i);
                }
                return i;
            }
        }
        throw new Exception("localhost无可用端口！");
    }

    public boolean isPortAvailable(int port) {
        try {
            Socket socket = new Socket("127.0.0.1", port);
            socket.close();
            return false;
        } catch (IOException e) {
            return true;
        }
    }
}
