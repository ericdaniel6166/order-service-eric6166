BEGIN;

CREATE TABLE IF NOT EXISTS T_ORDER
(
    ID                 BIGSERIAL PRIMARY KEY,
    UUID               VARCHAR(255) NOT NULL,
    USERNAME           VARCHAR(255) NOT NULL,
    ORDER_DATE         TIMESTAMP(6) NOT NULL,
    ORDER_STATUS_VALUE INTEGER      NOT NULL,
    ORDER_DETAIL       TEXT,
    TOTAL_AMOUNT       NUMERIC(19, 4),
    CREATED_BY         VARCHAR(255),
    CREATED_DATE       TIMESTAMP(6),
    LAST_MODIFIED_BY   VARCHAR(255),
    LAST_MODIFIED_DATE TIMESTAMP(6)
);

CREATE INDEX ORDER_UUID_ORDER_STATUS_VALUE_INDEX
    ON T_ORDER (UUID, ORDER_STATUS_VALUE);

CREATE INDEX ORDER_USERNAME_ORDER_DATE_ORDER_STATUS_VALUE_INDEX
    ON T_ORDER (USERNAME, ORDER_DATE, ORDER_STATUS_VALUE);

COMMIT;