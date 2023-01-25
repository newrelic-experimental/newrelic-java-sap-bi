/*
 *
 *  * Copyright 2020 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package java.sql;

import java.sql.CallableStatement;
import java.sql.SQLException;

import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(originalName = "java.sql.Connection", type = MatchType.Interface)
public abstract class Connection_Weaved {

    public PreparedStatement_Weaved prepareStatement(String sql) throws SQLException {
        PreparedStatement_Weaved preparedStatement = Weaver.callOriginal();
        preparedStatement.preparedSql = sql;
        return preparedStatement;
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        CallableStatement callableStatement = Weaver.callOriginal();
        ((PreparedStatement_Weaved) callableStatement).preparedSql = sql;
        return callableStatement;
    }

    public PreparedStatement_Weaved prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        PreparedStatement_Weaved preparedStatement = Weaver.callOriginal();
        preparedStatement.preparedSql = sql;
        return preparedStatement;
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        CallableStatement callableStatement = Weaver.callOriginal();
        ((PreparedStatement_Weaved) callableStatement).preparedSql = sql;
        return callableStatement;
    }

    public PreparedStatement_Weaved prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        PreparedStatement_Weaved preparedStatement = Weaver.callOriginal();
        preparedStatement.preparedSql = sql;
        return preparedStatement;
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        CallableStatement callableStatement = Weaver.callOriginal();
        ((PreparedStatement_Weaved) callableStatement).preparedSql = sql;
        return callableStatement;
    }

    public PreparedStatement_Weaved prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        PreparedStatement_Weaved preparedStatement = Weaver.callOriginal();
        preparedStatement.preparedSql = sql;
        return preparedStatement;
    }

    public PreparedStatement_Weaved prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        PreparedStatement_Weaved preparedStatement = Weaver.callOriginal();
        preparedStatement.preparedSql = sql;
        return preparedStatement;
    }

    public PreparedStatement_Weaved prepareStatement(String sql, String[] columnNames) throws SQLException {
        PreparedStatement_Weaved preparedStatement = Weaver.callOriginal();
        preparedStatement.preparedSql = sql;
        return preparedStatement;
    }

}