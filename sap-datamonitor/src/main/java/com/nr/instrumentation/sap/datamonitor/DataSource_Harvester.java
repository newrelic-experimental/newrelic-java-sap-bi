package com.nr.instrumentation.sap.datamonitor;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;

import com.newrelic.agent.HarvestListener;
import com.newrelic.agent.config.AgentConfig;
import com.newrelic.agent.config.AgentConfigListener;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.agent.stats.StatsEngine;
import com.newrelic.api.agent.Config;
import com.newrelic.api.agent.NewRelic;
import com.sap.sql.connect.datasource.DataSourceManager;
import com.sap.sql.jdbc.monitor.ConnectionMonitor;
import com.sap.sql.jdbc.monitor.DataSourceInfo;
import com.sap.sql.jdbc.monitor.impl.ConnectionMonitorImpl;
import com.sap.sql.statistics.DMLStatistics;
import com.sap.sql.statistics.DataSourceStatistics;
import com.sap.sql.statistics.DatabaseStatistics;
import com.sap.sql.statistics.QueryStatistics;

public class DataSource_Harvester implements HarvestListener, AgentConfigListener {

	public static boolean initialized = false;
	// default don't collect
	private static boolean reportQueryStats = false;
	// default don't collect
	private static boolean reportDMLStats = false;
	// default collect
	private static boolean reportDSStats = true;
	// default collect
	private static boolean reportDSInfo = true;

	private static final String REPORT_QUERIES = "SAP.DataMonitor.Queries.Report";
	private static final String REPORT_DSINFO = "SAP.DataMonitor.Datastore.Info.Report";
	private static final String REPORT_DSSTATS = "SAP.DataMonitor.Datastore.Stats.Report";
	private static final String REPORT_DML = "SAP.DataMonitor.DML.Report";
	
	public static void init() {
		ServiceFactory.getHarvestService().addHarvestListener(new DataSource_Harvester());
		initialized = true;
		Config config = NewRelic.getAgent().getConfig();
		processConfig(config);
	}


	@Override
	public void afterHarvest(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeHarvest(String arg0, StatsEngine arg1) {
		boolean notSkipping = reportQueryStats || reportDMLStats || reportDSStats || reportDSInfo;
		// Don't proceed if we're not going to track anything
		if(!notSkipping) return;
		
		if (reportDSStats) {
			ConnectionMonitor monitor = ConnectionMonitorImpl.getInstance();
			DatabaseStatistics dbStats = monitor.getDatabaseStatistics();
			if (dbStats != null) {
				reportDBStats(dbStats);
			} 
		}
		
		if (reportDSInfo) {
			DataSourceManager dsMgr = DataSourceManager.getInstance();
			if (dsMgr != null) {
				String[] dsNames = dsMgr.getDataSourceNames();
				for (String dsName : dsNames) {
					try {
						DataSourceInfo info = dsMgr.getDataSourceInfo(dsName);
						reportDS(info);
					} catch (SQLException e) {
						NewRelic.getAgent().getLogger().log(Level.FINEST, e, "Failed to get datasource info");
					}
				}
			} 
		}
		
	}

	private static void reportDS(DataSourceInfo info) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		putValue(attributes, "DatabaseName", info.getDatabaseName());
		putValue(attributes, "DatabaseServerName", info.getDatabaseServerName());
		putValue(attributes, "DataSourceName", info.getDataSourceName());
		putValue(attributes, "IdleConnectionCount", info.getIdleConnectionCount());
//		putValue(attributes, "InitConnectionCount", info.getInitConnections());
		putValue(attributes, "MaxConnections", info.getMaxConnections());
		putValue(attributes, "SumErrorConnectionRequestCount", info.getSumErrorConnectionRequestCount());
//		putValue(attributes, "SumErrorConnectionRequestRate", info.getSumErrorConnectionRequestRate());
		putValue(attributes, "SumSuccessConnectionRequestCount", info.getSumSuccessConnectionRequestCount());
//		putValue(attributes, "SumSuccessConnectionRequestRate", info.getSumSuccessConnectionRequestRate());
		putValue(attributes, "SumTimeoutConnectionRequestCount", info.getSumTimeoutConnectionRequestCount());
//		putValue(attributes, "SumTimeoutConnectionRequestRate", info.getSumTimeoutConnectionRequestRate());
		putValue(attributes, "UsedConnectionCount", info.getUsedConnectionCount());
//		putValue(attributes, "UsedConnectionRate", info.getUsedConnectionRate());
		putValue(attributes, "ValidConnectionRate", info.getValidConnectionRate());
		putValue(attributes, "VendorName", info.getVendorName());
		putValue(attributes, "WaitingConnectionRequestCount", info.getWaitingConnectionRequestCount());
//		putValue(attributes, "WaitingConnectionRequestRate", info.getWaitingConnectionRequestRate());
		NewRelic.getAgent().getInsights().recordCustomEvent("DataSourceInfo", attributes);
	}

	private static void reportDBStats(DatabaseStatistics dbStats) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		boolean closed = dbStats.getBytesReceived() != -1;
		
		putValue(attributes,"RequestName",dbStats.getRequestName());
		putValue(attributes, "BytesReceived", dbStats.getBytesReceived());
		putValue(attributes, "BytesSent", dbStats.getBytesSent());
		putValue(attributes, "CommitCount", dbStats.getCommitCount());
		putValue(attributes, "CommitTime", dbStats.getCommitTime());
		putValue(attributes, "DatabaseTime", dbStats.getDatabaseTime());
		putValue(attributes, "DMLCount", dbStats.getDMLCount());
		putValue(attributes, "DMLTime", dbStats.getDMLTime());
		putValue(attributes, "ExecuteBatchCount", dbStats.getExecuteBatchCount());
		putValue(attributes, "ExecuteBatchTime", dbStats.getExecuteBatchTime());
		putValue(attributes, "ExecuteQueryCount", dbStats.getExecuteQueryCount());
		putValue(attributes, "ExecuteQueryTime", dbStats.getExecuteQueryTime());
		putValue(attributes, "ExecuteUpdateCount", dbStats.getExecuteUpdateCount());
		putValue(attributes, "ExecuteUpdateTime", dbStats.getExecuteUpdateTime());
		putValue(attributes, "NextCount", dbStats.getNextCount());
		putValue(attributes, "NextTime", dbStats.getNextTime());
		putValue(attributes, "PrepareCount", dbStats.getPrepareCount());
		putValue(attributes, "PrepareTime", dbStats.getPrepareTime());
		putValue(attributes, "QueryCount", dbStats.getQueryCount());
		putValue(attributes, "QueryTime", dbStats.getQueryTime());
		putValue(attributes, "RequestDate", dbStats.getRequestDate());
		putValue(attributes, "RollbackCount", dbStats.getRollbackCount());
		putValue(attributes, "RollbackTime", dbStats.getRollbackTime());
		putValue(attributes, "RowsBatchedCount", dbStats.getRowsBatchedCount());
		putValue(attributes, "RowsModifiedCount", dbStats.getRowsModifiedCount());
		putValue(attributes, "RowsRetrievedCount", dbStats.getRowsRetrievedCount());

		Collection<DataSourceStatistics> dataStoreStats = dbStats.getDataSourceStatistics();
		putValue(attributes, "DataStore-Statistics", dataStoreStats != null ? dataStoreStats.size() : 0);
		if (dataStoreStats != null) {
			for (DataSourceStatistics dsStats : dataStoreStats) {
				reportDataStoreStats(dsStats);
				if (reportDMLStats) {
					Collection<DMLStatistics> dmlStatsList = dsStats.getDMLStatisticsRecords();
					putValue(attributes, "DML-Statistics", dmlStatsList != null ? dmlStatsList.size() : 0);
					if (dmlStatsList != null) {
						for (DMLStatistics dmStats : dmlStatsList) {
							reportDLMStats(dmStats);
						}
					} 
				}
				if (reportQueryStats) {
					Collection<QueryStatistics> queryStatsList = dsStats.getQueryStatisticsRecords();
					putValue(attributes, "Query-Statistics", queryStatsList != null ? queryStatsList.size() : 0);
					if (queryStatsList != null) {
						for (QueryStatistics queryStats : queryStatsList) {
							reportQueryStats(queryStats);
						}
					} 
				}
			} 
		}
		
		if(closed) {
			NewRelic.getAgent().getInsights().recordCustomEvent("DatabaseStatistics", attributes);
		}
	}

	private static void reportQueryStats(QueryStatistics querystats) {

		HashMap<String, Object> attributes = new HashMap<String, Object>();
		putValue(attributes, "AvgExecuteQueryTime", querystats.getAvgExecuteQueryTime());
		putValue(attributes, "getAvgNextTime", querystats.getAvgNextTime());
		putValue(attributes, "AvgPrepareTime", querystats.getAvgPrepareTime());
		putValue(attributes, "AvgRowsRetrieved", querystats.getAvgRowsRetrieved());
		putValue(attributes, "ExecuteQueryCount", querystats.getExecuteQueryCount());
		putValue(attributes, "ExecuteQueryTime", querystats.getExecuteQueryTime());
		putValue(attributes, "MaxExecuteQueryTime", querystats.getMaxExecuteQueryTime());
		putValue(attributes, "MaxNextTime", querystats.getMaxNextTime());
		putValue(attributes, "NextCount", querystats.getNextCount());
		putValue(attributes, "NextTime", querystats.getNextTime());
		putValue(attributes, "MaxPrepareTime", querystats.getMaxPrepareTime());
		putValue(attributes, "PooledPrepareCount", querystats.getPooledPrepareCount());
		putValue(attributes, "PrepareCount", querystats.getPrepareCount());
		putValue(attributes, "PrepareTime", querystats.getPrepareTime());
		putValue(attributes, "SQL", querystats.getSQL());
		putValue(attributes, "RowsRetrievedCount", querystats.getRowsRetrievedCount());
		putValue(attributes, "StmtPoolRefCount", querystats.getStmtPoolRefCount());
		putValue(attributes, "TotalTime", querystats.getTotalTime());

		NewRelic.getAgent().getInsights().recordCustomEvent("QueryStatistics", attributes);
	}

	private static void reportDLMStats(DMLStatistics dmlstats) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		putValue(attributes, "AvgExecuteBatchTime", dmlstats.getAvgExecuteBatchTime());
		putValue(attributes, "AvgExecuteUpdateTime", dmlstats.getAvgExecuteUpdateTime());
		putValue(attributes, "AvgPrepareTime", dmlstats.getAvgPrepareTime());
		putValue(attributes, "AvgRowsBatched", dmlstats.getAvgRowsBatched());
		putValue(attributes, "AvgRowsModified", dmlstats.getAvgRowsModified());
		putValue(attributes, "ExecuteBatchCount", dmlstats.getExecuteBatchCount());
		putValue(attributes, "ExecuteBatchTime", dmlstats.getExecuteBatchTime());
		putValue(attributes, "ExecuteUpdateCount", dmlstats.getExecuteUpdateCount());
		putValue(attributes, "ExecuteUpdateTime", dmlstats.getExecuteUpdateTime());
		putValue(attributes, "MaxExecuteBatchTime", dmlstats.getMaxExecuteBatchTime());
		putValue(attributes, "MaxExecuteUpdateTime", dmlstats.getMaxExecuteUpdateTime());
		putValue(attributes, "MaxPrepareTime", dmlstats.getMaxPrepareTime());
		putValue(attributes, "PooledPrepareCount", dmlstats.getPooledPrepareCount());
		putValue(attributes, "PrepareCount", dmlstats.getPrepareCount());
		putValue(attributes, "PrepareTime", dmlstats.getPrepareTime());
		putValue(attributes, "RowsBatchedCount", dmlstats.getRowsBatchedCount());
		putValue(attributes, "RowsModifiedCount", dmlstats.getRowsModifiedCount());
		putValue(attributes, "SQL", dmlstats.getSQL());
		putValue(attributes, "StmtPoolRefCount", dmlstats.getStmtPoolRefCount());
		putValue(attributes, "TotalTime", dmlstats.getTotalTime());
		NewRelic.getAgent().getInsights().recordCustomEvent("DMLStatistics", attributes);
	}

	private static void reportDataStoreStats(DataSourceStatistics dsStats) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		putValue(attributes, "DatasourceName", dsStats.getDataSourceName());
		putValue(attributes, "DatabaseTime", dsStats.getDatabaseTime());
		putValue(attributes, "CommitCount", dsStats.getCommitCount());
		putValue(attributes, "SavedCommitCount", dsStats.getSavedCommitCount());
		putValue(attributes, "CommitTime", dsStats.getCommitTime());
		putValue(attributes, "AvgCommitTime", dsStats.getAvgCommitTime());
		putValue(attributes, "RollbackCount", dsStats.getRollbackCount());
		putValue(attributes, "SavedRollbackCount", dsStats.getSavedRollbackCount());
		putValue(attributes, "RollbackTime", dsStats.getRollbackTime());
		putValue(attributes, "AvgRollbackTime", dsStats.getAvgRollbackTime());
		putValue(attributes, "PrepareCount", dsStats.getPrepareCount());
		putValue(attributes, "PooledPrepareCount", dsStats.getPooledPrepareCount());
		putValue(attributes, "PrepareTime", dsStats.getPrepareTime());
		putValue(attributes, "AvgPrepareTime", dsStats.getAvgPrepareTime());
		putValue(attributes, "ExecuteQueryCount", dsStats.getExecuteQueryCount());
		putValue(attributes, "ExecuteQueryTime", dsStats.getExecuteQueryTime());
		putValue(attributes, "AvgExecuteQueryTime", dsStats.getAvgExecuteQueryTime());
		putValue(attributes, "NextCount", dsStats.getNextCount());
		putValue(attributes, "NextTime", dsStats.getNextTime());
		putValue(attributes, "AvgNextTime", dsStats.getAvgNextTime());
		putValue(attributes, "RowsRetrievedCount", dsStats.getRowsRetrievedCount());
		putValue(attributes, "AvgRowsRetrieved", dsStats.getAvgRowsRetrieved());
		putValue(attributes, "ExecuteUpdateCount", dsStats.getExecuteUpdateCount());
		putValue(attributes, "ExecuteUpdateTime", dsStats.getExecuteUpdateTime());
		putValue(attributes, "AvgExecuteUpdateTime", dsStats.getAvgExecuteUpdateTime());
		putValue(attributes, "RowsModifiedCount", dsStats.getRowsModifiedCount());
		putValue(attributes, "AvgRowsModified", dsStats.getAvgRowsModified());
		putValue(attributes, "ExecuteBatchCount", dsStats.getExecuteBatchCount());
		putValue(attributes, "ExecuteBatchTime", dsStats.getExecuteBatchTime());
		putValue(attributes, "AvgExecuteBatchTime", dsStats.getAvgExecuteBatchTime());
		putValue(attributes, "RowsBatchedCount", dsStats.getRowsBatchedCount());
		putValue(attributes, "AvgRowsBatched", dsStats.getAvgRowsBatched());
		putValue(attributes, "QueryCount", dsStats.getQueryCount());
		putValue(attributes, "QueryTime", dsStats.getQueryTime());
		putValue(attributes, "DMLCount", dsStats.getDMLCount());
		putValue(attributes, "DMLTime", dsStats.getDMLTime());
		putValue(attributes, "AvgDMLTime", dsStats.getAvgDMLTime());

		putValue(attributes, "BytesReceived", dsStats.getBytesReceived());
		putValue(attributes, "BytesSent", dsStats.getBytesSent());
		NewRelic.getAgent().getInsights().recordCustomEvent("DataStoreStatistics", attributes);
	}

	private static void putValue(HashMap<String, Object> attributes, String key, Object value) {
		if(value != null) {
			attributes.put(key, value);
		}
	}


	@Override
	public void configChanged(String category, AgentConfig config) {
		processConfig(config);
	}
	
	private static void processConfig(Config config) {
		Object value = config.getValue(REPORT_QUERIES);
		if(value != null) {
			Boolean b = null;
			if(value instanceof Boolean) {
				b = (Boolean)value;
			} else if(value instanceof String) {
				String s = (String)value;
				try {
					b = Boolean.parseBoolean(s);
				} catch (Exception e) {
				}
			}
			if(b != null && b != reportQueryStats) {
				reportQueryStats = b;
			}
		}
		value = config.getValue(REPORT_DML);
		if(value != null) {
			Boolean b = null;
			if(value instanceof Boolean) {
				b = (Boolean)value;
			} else if(value instanceof String) {
				String s = (String)value;
				try {
					b = Boolean.parseBoolean(s);
				} catch (Exception e) {
				}
			}
			if(b != null && b != reportDMLStats) {
				reportDMLStats = b;
			}
		}
		value = config.getValue(REPORT_DSINFO);
		if(value != null) {
			Boolean b = null;
			if(value instanceof Boolean) {
				b = (Boolean)value;
			} else if(value instanceof String) {
				String s = (String)value;
				try {
					b = Boolean.parseBoolean(s);
				} catch (Exception e) {
				}
			}
			if(b != null && b != reportDSInfo) {
				reportDSInfo = b;
			}
		}
		value = config.getValue(REPORT_DSSTATS);
		if(value != null) {
			Boolean b = null;
			if(value instanceof Boolean) {
				b = (Boolean)value;
			} else if(value instanceof String) {
				String s = (String)value;
				try {
					b = Boolean.parseBoolean(s);
				} catch (Exception e) {
				}
			}
			if(b != null && b != reportDSStats) {
				reportDSStats = b;
			}
		}
	}
}
