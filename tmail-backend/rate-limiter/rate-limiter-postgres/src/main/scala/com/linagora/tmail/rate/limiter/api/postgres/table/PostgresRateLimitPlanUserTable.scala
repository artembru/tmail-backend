package com.linagora.tmail.rate.limiter.api.postgres.table

import java.util.UUID

import org.apache.james.backends.postgres.{PostgresIndex, PostgresModule, PostgresTable}
import org.jooq.impl.{DSL, SQLDataType}
import org.jooq.{Field, Record, Table}

object PostgresRateLimitPlanUserTable {
  val TABLE_NAME: Table[Record] = DSL.table("rate_limit_plan_user")
  val USERNAME: Field[String] = DSL.field("username", SQLDataType.VARCHAR.notNull)
  val PLAN_ID: Field[UUID] = DSL.field("plan_id", SQLDataType.UUID.notNull)

  val TABLE: PostgresTable = PostgresTable.name(TABLE_NAME.getName)
    .createTableStep((dsl, tableName) => dsl.createTableIfNotExists(tableName)
      .column(USERNAME)
      .column(PLAN_ID)
      .constraint(DSL.primaryKey(USERNAME)))
    .supportsRowLevelSecurity
    .build

  val PLAN_ID_INDEX: PostgresIndex = PostgresIndex.name("index_rate_limit_plan_user_plan_id")
    .createIndexStep((dslContext, indexName) => dslContext.createIndexIfNotExists(indexName)
      .on(TABLE_NAME, PLAN_ID))

  val MODULE: PostgresModule = PostgresModule
      .builder
      .addTable(TABLE)
      .addIndex(PLAN_ID_INDEX)
      .build
}