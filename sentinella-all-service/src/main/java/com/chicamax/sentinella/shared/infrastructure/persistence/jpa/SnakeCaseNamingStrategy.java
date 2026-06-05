package com.chicamax.sentinella.shared.infrastructure.persistence.jpa;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class SnakeCaseNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        var physicalName = super.toPhysicalTableName(name, jdbcEnvironment);
        if (physicalName == null || physicalName.getText() == null) {
            return physicalName;
        }
        var tableName = physicalName.getText();
        if (tableName.endsWith("s")) {
            return physicalName;
        }
        if (tableName.endsWith("_log")) {
            return physicalName;
        }
        return Identifier.toIdentifier(tableName + "s", physicalName.isQuoted());
    }
}
