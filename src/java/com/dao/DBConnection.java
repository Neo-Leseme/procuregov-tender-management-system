package com.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * DBConnection — JNDI DataSource helper for the ProcureGov
 * connection pool.
 *
 * <p>Retrieves database connections from the Tomcat-managed connection
 * pool configured in {@code context.xml}. No
 * {@code DriverManager.getConnection()} calls are used anywhere in the
 * application — all connections are obtained via JNDI lookup as required
 * by the assessment specification (Module 5).
 *
 * <p>Usage:
 * <pre>
 *   try (Connection conn = DBConnection.getConnection()) {
 *       // use connection — automatically returned to pool on close
 *   }
 * </pre>
 *
 * @author Neo Leseme
 * @version 1.0
 */
public final class DBConnection {

    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());
    private static final String JNDI_NAME = "java:comp/env/jdbc/ProcureGovDB";

    private static DataSource dataSource;

    /**
     * Private constructor — utility class, not instantiable.
     */
    private DBConnection() {}

    /**
     * Initialises the {@link DataSource} from JNDI on first call.
     * Uses lazy initialisation with synchronised access for thread safety.
     *
     * @return the configured DataSource
     * @throws RuntimeException if the JNDI lookup fails
     */
    private static synchronized DataSource getDataSource() {
        if (dataSource == null) {
            try {
                Context initCtx = new InitialContext();
                dataSource = (DataSource) initCtx.lookup(JNDI_NAME);
                LOGGER.info("ProcureGov: DataSource acquired from JNDI — " + JNDI_NAME);
            } catch (NamingException e) {
                LOGGER.log(Level.SEVERE,
                    "JNDI lookup failed for " + JNDI_NAME
                    + ". Check context.xml and Tomcat JNDI config.", e);
                throw new RuntimeException("Cannot initialise database connection pool.", e);
            }
        }
        return dataSource;
    }

    /**
     * Returns a {@link Connection} from the connection pool.
     *
     * <p>Always use in a try-with-resources block so the connection
     * is returned to the pool automatically.</p>
     *
     * @return a live JDBC Connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }
}